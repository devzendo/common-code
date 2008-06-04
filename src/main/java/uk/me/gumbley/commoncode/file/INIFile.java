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

/**
 * API for access to Windows-.ini style files.
 * 
 * @author matt
 *
 */
public class INIFile {
    private static final Logger LOGGER = Logger.getLogger(INIFile.class);

    private HashMap < String, Properties> mySectionProperties;

    private File myFile;
    
    private boolean bDirty = false;

    private int myWriteSuspensions = 0;
    
    /**
     * Create a new .ini file, or load an existing one with a given path.
     * @param fileName the path of the file to create.
     */
    public INIFile(final String fileName) {
        super();
        myWriteSuspensions = 0;
        mySectionProperties = new HashMap < String, Properties> ();
        myFile = new File(fileName);
        if (myFile.exists()) {
            LOGGER.debug("Loading existing INI file: " + fileName);
            loadFile();
        } else {
            LOGGER.debug("Creating new INI file: " + fileName);
            saveFile();
        }
        bDirty = false;
    }
    
    /**
     * Allow the writing of the file to be suspended. Calls to this can
     * be nested, although all such calls must be matched with calls
     * to resumeWrite, else the file will never be updated.
     */
    public final synchronized void suspendWrite() {
        myWriteSuspensions++;
    }
    
    /**
     * Resume the writing of the file on calls to setXXXX. If the file had
     * changed during write suspension, it is now written. 
     */
    public final synchronized void resumeWrite() {
        myWriteSuspensions--;
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
            LOGGER.error("Could not write INI file " + myFile.getAbsolutePath() + ": " + e.getMessage());
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
                    LOGGER.debug("Read line '" + line + "'");
                    if (line == null) {
                        break;
                    }
                    lineNo++;
                    sectionMatcher.reset(line);
                    if (sectionMatcher.lookingAt()) {
                        currentSectionName = sectionMatcher.group(1);
                        LOGGER.debug("Found section [" + currentSectionName + "]");
                        Properties newSectionProperties = new Properties();
                        currentSectionProperties = newSectionProperties;
                        mySectionProperties.put(currentSectionName, newSectionProperties);
                    } else {
                        nvpMatcher.reset(line);
                        if (nvpMatcher.lookingAt()) {
                            if (currentSectionProperties == null) {
                                LOGGER.error("Line " + lineNo + " name=value line not under any [section]: '" + line  + "'");
                            } else {
                                String name = nvpMatcher.group(1);
                                String value = nvpMatcher.group(2);
                                LOGGER.debug("[" + currentSectionName + "] " + name + "=" + value);
                                currentSectionProperties.put(name, value);
                            }
                        } else {
                            LOGGER.error("Line " + lineNo + " not matched against [section] or name=value: '" + line + "'");
                        }
                    }
                }
            } catch (IOException ioe) {
                LOGGER.error("Could not load INI file: " + ioe.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e1) {
                        LOGGER.error("Could not close BufferedReader: " + e1.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("INI file " + myFile.getAbsolutePath() + " not found");
        }
    }

    /**
     * Obtain a value from the file, with no default if it does not exist.
     * @param sectionName the [section name] under which this value could be found
     * @param name the name= key to the given value
     * @return the value, if one exists, null if it does not exist.
     */
    public final String getValue(final String sectionName, final String name) {
        if (!mySectionProperties.containsKey(sectionName)) {
            LOGGER.debug("getValue(" + sectionName + "," + name + "): not found [section]");
            return null;
        } else {
            Properties sectionProperties = (Properties) mySectionProperties.get(sectionName);
            String value = (String) sectionProperties.get(name); // returns null on 'not found'
            LOGGER.debug("getValue(" + sectionName + "," + name + "): returning '" + value + "'");
            return value;
        }
    }


    /**
     * Obtain a value from the file, with a default if it does not exist.
     * @param sectionName the [section name] under which this value could be found
     * @param name the name= key to the given value
     * @param defaultValue the value, if the name does not exist.
     * @return the value, if one exists, defaultValue if it does not exist.
     */
    public final String getValue(final String sectionName, final String name, final String defaultValue) {
        String value = getValue(sectionName, name);
        return value == null ? defaultValue : value;
    }

    /**
     * Remove a name=value pair from the file. If the removed entry is the final
     * entry in this [section name], remove the section name also.
     * @param sectionName the [section name] under which the name will be deleted.
     * @param name the name= that will be deleted, if it exists.
     */
    public final synchronized void removeValue(final String sectionName, final String name) {
        if (!mySectionProperties.containsKey(sectionName)) {
            LOGGER.debug("removeValue(" + sectionName + ", " + name + "): not found [section]");
            return;
        }
        bDirty = true;
        Properties sectionProperties = (Properties) mySectionProperties.get(sectionName);
        sectionProperties.remove(name);
        if (sectionProperties.size() == 0) {
            LOGGER.debug("removeValue(" + sectionName + ", " + name
                    + "): final name returned from [section]; removing [section]");
            mySectionProperties.remove(sectionName);
        }
        saveFile();
    }


    /**
     * Store a String value in the file.
     * @param sectionName the [section name] under which this value will be stored.
     * @param name the name= key to the given value
     * @param value the value.
     */
    public final synchronized void setValue(final String sectionName, final String name, final String value) {
        Properties sectionProperties = null;
        bDirty = true;
        if (!mySectionProperties.containsKey(sectionName)) {
            sectionProperties = new Properties();
            mySectionProperties.put(sectionName, sectionProperties);
            LOGGER.debug("setValue(" + sectionName + "," + name + "," + value + "): created new [class]");
        } else {
            sectionProperties = (Properties) mySectionProperties.get(sectionName);
        }
        LOGGER.debug("setValue(" + sectionName + "," + name + "," + value + "): saving");
        sectionProperties.put(name, value);
        saveFile();
    }
    
    /**
     * Store a long value in the file.
     * @param sectionName the [section name] under which this value will be stored.
     * @param name the name= key to the given value
     * @param value the value.
     */
    public final void setLongValue(final String sectionName, final String name, final long value) {
        setValue(sectionName, name, "" + value);
    }

    /**
     * Store an integer value in the file.
     * @param sectionName the [section name] under which this value will be stored.
     * @param name the name= key to the given value
     * @param value the value.
     */
    public final void setIntegerValue(final String sectionName, final String name, final int value) {
        setValue(sectionName, name, "" + value);
    }

    /**
     * Store a boolean value in the file.
     * @param sectionName the [section name] under which this value will be stored.
     * @param name the name= key to the given value
     * @param value the value.
     */
    public final void setBooleanValue(final String sectionName, final String name, final boolean value) {
        setValue(sectionName, name, value ? "TRUE" : "FALSE");
    }

    /**
     * Obtain a long value from the file.
     * @param sectionName the [section name] from which this value will be obtained.
     * @param name the name= key to the given value
     * @return the value.
     */
    public final long getLongValue(final String sectionName, final String name) {
        String value = getValue(sectionName, name, "0");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not a long integer");
            return 0L;
        }
    }

    /**
     * Obtain an integer value in the file.
     * @param sectionName the [section name] from which this value will be obtained.
     * @param name the name= key to the given value
     * @return the value.
     */
    public final int getIntegerValue(final String sectionName, final String name) {
        String value = getValue(sectionName, name, "0");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not an integer");
            return 0;
        }
    }
    
    /**
     * Obtain a boolean value in the file.
     * @param sectionName the [section name] from which this value will be obtained.
     * @param name the name= key to the given value
     * @return the value.
     */
    public final boolean getBooleanValue(final String sectionName, final String name) {
        String value = getValue(sectionName, name, "FALSE");
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not a boolean");
            return false;
        }
    }

    /**
     * Obtain an array of values from a section name in the file.
     * @param sectionName the [section name] from which these values will be obtained.
     * @return the values.
     */
    public final String[] getArray(final String sectionName) {
        if (!mySectionProperties.containsKey(sectionName)) {
            LOGGER.debug("getArray(" + sectionName + "): not found [section]");
            return new String[0];
        } else {
            Properties sectionProperties = (Properties) mySectionProperties.get(sectionName);
            return (String[]) sectionProperties.values().toArray(new String[0]);
        }
    }
    
    /**
     * Set an array of values under a section name.
     * @param sectionName the [section name]
     * @param array the array of values
     */
    public final synchronized void setArray(final String sectionName, final String[] array) {
        bDirty = true;
        Properties arrayProperties = new Properties();
        for (int i = 0; i < array.length; i++) {
            arrayProperties.put("" + (i + 1), array[i]);
        }
        mySectionProperties.put(sectionName, arrayProperties);
        LOGGER.debug("setArray(" + sectionName + ", ...): saving");
        saveFile();
    }
    
    /**
     * Completely remove a [section name], even if populated.
     * @param sectionName the [section name]
     */
    public final synchronized void removeSection(final String sectionName) {
        if (!mySectionProperties.containsKey(sectionName)) {
            LOGGER.debug("removeSection(" + sectionName + "): not found [section]");
        } else {
            bDirty = true;
            mySectionProperties.remove(sectionName);
            LOGGER.debug("removeSection(" + sectionName + "): saving");
            saveFile();
        }
    }
}
