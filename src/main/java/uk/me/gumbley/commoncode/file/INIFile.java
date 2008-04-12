package uk.me.gumbley.commoncode.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class INIFile {
    private static Logger myLogger = Logger.getLogger(INIFile.class);

    private HashMap < String, Properties> mySectionProperties;

    private File myFile;
    
    private boolean bDirty = false;

    private int myWriteSuspensions = 0;
    
    public INIFile(final String fileName) {
        super();
        myWriteSuspensions = 0;
        mySectionProperties = new HashMap < String, Properties> ();
        myFile = new File(fileName);
        if (myFile.exists()) {
            myLogger.debug("Loading existing INI file: " + fileName);
            loadFile();
        } else {
            myLogger.debug("Creating new INI file: " + fileName);
            saveFile();
        }
        bDirty = false;
    }
    
    public synchronized void suspendWrite() {
        myWriteSuspensions ++;
    }
    public synchronized void resumeWrite() {
        myWriteSuspensions --;
        if (myWriteSuspensions == 0 && bDirty) {
            saveFile();
        }
    }

    private void saveFile() {
        if (myWriteSuspensions > 0 || !bDirty) {
            return;
        }
        String lineSep = System.getProperty("line.separator");
        try {
            FileOutputStream fos = new FileOutputStream(myFile);
            FileDescriptor fd = fos.getFD();
            FileWriter fw = new FileWriter(fd);
            try {
                Iterator sectionIterator = mySectionProperties.keySet().iterator();
                while (sectionIterator.hasNext()) {
                    String sectionName = (String) sectionIterator.next();
                    fw.write("[" + sectionName + "]" + lineSep);
                    Properties nvps = (Properties) mySectionProperties.get(sectionName);
                    Iterator nvpIterator = nvps.keySet().iterator();
                    while (nvpIterator.hasNext()) {
                        String name = (String) nvpIterator.next();
                        String value = nvps.getProperty(name).toString();
                        fw.write(name + "=" + value + lineSep);
                    }
                }
            } finally {
                fw.flush();
                fos.flush();
                fd.sync();
                fw.close();
                fos.close();
            }
        } catch (IOException e) {
            myLogger.error("Could not write INI file " + myFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private void loadFile() {
        Pattern sectionPattern = Pattern.compile("^\\[(.*)\\]$");
        Matcher sectionMatcher = sectionPattern.matcher("");
        Pattern nvpPattern = Pattern.compile("^(.*?)=(.*)$$");
        Matcher nvpMatcher = nvpPattern.matcher("");
        Properties currentSectionProperties = null;
        String currentSectionName = null;
        int lineNo = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(myFile));
            try {
                while (true) {
                    String line = br.readLine();
                    myLogger.debug("Read line '" + line + "'");
                    if (line == null) {
                        break;
                    }
                    lineNo++;
                    sectionMatcher.reset(line);
                    if (sectionMatcher.lookingAt()) {
                        currentSectionName = sectionMatcher.group(1);
                        myLogger.debug("Found section [" + currentSectionName + "]");
                        Properties newSectionProperties = new Properties();
                        currentSectionProperties = newSectionProperties;
                        mySectionProperties.put(currentSectionName, newSectionProperties);
                    } else {
                        nvpMatcher.reset(line);
                        if (nvpMatcher.lookingAt()) {
                            if (currentSectionProperties == null) {
                                myLogger.error("Line " + lineNo + " name=value line not under any [section]: '" + line  + "'");
                            } else {
                                String name = nvpMatcher.group(1);
                                String value = nvpMatcher.group(2);
                                myLogger.debug("[" + currentSectionName + "] " + name + "=" + value);
                                currentSectionProperties.put(name, value);
                            }
                        } else {
                            myLogger.error("Line " + lineNo + " not matched against [section] or name=value: '" + line + "'");
                        }
                    }
                }
            } catch (IOException ioe) {
                myLogger.error("Could not load INI file: " + ioe.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e1) {
                        myLogger.error("Could not close BufferedReader: " + e1.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            myLogger.error("INI file " + myFile.getAbsolutePath() + " not found");
        }
    }

    public final String getValue(final String sectionName, final String name) {
        if (!mySectionProperties.containsKey(sectionName)) {
            myLogger.debug("getValue(" + sectionName + "," + name + "): not found [section]");
            return null;
        } else {
            Properties sectionProperties = (Properties) mySectionProperties.get(sectionName);
            String value = (String) sectionProperties.get(name); // returns null on 'not found'
            myLogger.debug("getValue(" + sectionName + "," + name + "): returning '" + value + "'");
            return value;
        }
    }

    public final String getValue(final String sectionName, final String name, final String defaultValue) {
        String value = getValue(sectionName, name);
        return value == null ? defaultValue : value;
    }

    public final synchronized void removeValue(final String sectionName, final String name) {
        if (!mySectionProperties.containsKey(sectionName)) {
            myLogger.debug("removeValue(" + sectionName + ", " + name + "): not found [section]");
            return;
        }
        bDirty = true;
        Properties sectionProperties = (Properties) mySectionProperties.get(sectionName);
        sectionProperties.remove(name);
        if (sectionProperties.size() == 0) {
            myLogger.debug("removeValue(" + sectionName + ", " + name
                    + "): final name returned from [section]; removing [section]");
            mySectionProperties.remove(sectionName);
        }
        saveFile();
    }

    public final synchronized void setValue(final String sectionName, final String name, final String value) {
        Properties sectionProperties = null;
        bDirty = true;
        if (!mySectionProperties.containsKey(sectionName)) {
            sectionProperties = new Properties();
            mySectionProperties.put(sectionName, sectionProperties);
            myLogger.debug("setValue(" + sectionName + "," + name + "," + value + "): created new [class]");
        } else {
            sectionProperties = (Properties) mySectionProperties.get(sectionName);
        }
        myLogger.debug("setValue(" + sectionName + "," + name + "," + value + "): saving");
        sectionProperties.put(name, value);
        saveFile();
    }
    
    public final void setLongValue(final String sectionName, final String name, final long value) {
        setValue(sectionName, name, "" + value);
    }

    public final void setIntegerValue(final String sectionName, final String name, final int value) {
        setValue(sectionName, name, "" + value);
    }

    public final void setBooleanValue(final String sectionName, final String name, final boolean value) {
        setValue(sectionName, name, value ? "TRUE" : "FALSE");
    }

    public final long getLongValue(final String sectionName, final String name) {
        String value = getValue(sectionName, name, "0");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            myLogger.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not a long integer");
            return 0L;
        }
    }

    public final int getIntegerValue(final String sectionName, final String name) {
        String value = getValue(sectionName, name, "0");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            myLogger.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not an integer");
            return 0;
        }
    }
    
    public final boolean getBooleanValue(final String sectionName, final String name) {
        String value = getValue(sectionName, name, "FALSE");
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            myLogger.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not a boolean");
            return false;
        }
    }
    public final String[] getArray(final String sectionName) {
        if (!mySectionProperties.containsKey(sectionName)) {
            myLogger.debug("getArray(" + sectionName + "): not found [section]");
            return new String[0];
        } else {
            Properties sectionProperties = (Properties) mySectionProperties.get(sectionName);
            return (String[]) sectionProperties.values().toArray(new String[0]);
        }
    }
    
    public final synchronized void setArray(final String sectionName, final String[] array) {
        bDirty = true;
        Properties arrayProperties = new Properties();
        for (int i=0; i< array.length; i++) {
            arrayProperties.put("" + (i+1), array[i]);
        }
        mySectionProperties.put(sectionName, arrayProperties);
        myLogger.debug("setArray(" + sectionName + ", ...): saving");
        saveFile();
    }
    
    public final synchronized void removeSection(final String sectionName) {
        if (!mySectionProperties.containsKey(sectionName)) {
            myLogger.debug("removeSection(" + sectionName + "): not found [section]");
        } else {
            bDirty = true;
            mySectionProperties.remove(sectionName);
            myLogger.debug("removeSection(" + sectionName + "): saving");
            saveFile();
        }
    }
}
