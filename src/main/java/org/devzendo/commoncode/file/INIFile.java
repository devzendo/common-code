/*
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.devzendo.commoncode.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API for access to Windows-.ini style files.
 * 
 * @author matt
 *
 */
public class INIFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(INIFile.class);

    private final Map<String, Map<String, String> > mySectionMap;

    private final File myFile;
    
    private boolean bDirty = false;

    private int myWriteSuspensions = 0;
    
    /**
     * Create a new .ini file, or load an existing one with a given path.
     * @param fileName the path of the file to create.
     */
    public INIFile(final String fileName) {
        super();
        myWriteSuspensions = 0;
        mySectionMap = new HashMap < String, Map<String, String> > ();
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
        final String lineSep = System.getProperty("line.separator");
        try {
            final FileOutputStream fos = new FileOutputStream(myFile);
            final FileDescriptor fd = fos.getFD();
            final FileWriter fw = new FileWriter(fd);
            try {
                for (String sectionName : mySectionMap.keySet()) {
                    fw.write("[" + sectionName + "]" + lineSep);
                    final Map<String, String> nvps = mySectionMap.get(sectionName);
                    for (String name : nvps.keySet()) {
                        final String valueObject = nvps.get(name);
                        //final String value = valueObject == null ? "" : valueObject.toString();
                        final String value = valueObject.toString();
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
        } catch (final IOException e) {
            LOGGER.error("Could not write INI file " + myFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private void loadFile() {
        final Matcher sectionMatcher = Pattern.compile("^\\[(.*)\\]$").matcher("");
        final Matcher nvpMatcher = Pattern.compile("^(.*?)=(.*)$$").matcher("");
        Map<String, String> currentSectionMap = null;
        String currentSectionName = null;
        int lineNo = 0;
        try {
            final BufferedReader br = new BufferedReader(new FileReader(myFile));
            try {
                while (true) {
                    final String line = br.readLine();
                    LOGGER.debug("Read line '" + line + "'");
                    if (line == null) {
                        break;
                    }
                    lineNo++;
                    sectionMatcher.reset(line);
                    if (sectionMatcher.lookingAt()) {
                        currentSectionName = sectionMatcher.group(1);
                        LOGGER.debug("Found section [" + currentSectionName + "]");
                        final Map<String, String> newSectionMap = new HashMap<String, String>();
                        currentSectionMap = newSectionMap;
                        mySectionMap.put(currentSectionName, newSectionMap);
                    } else {
                        nvpMatcher.reset(line);
                        if (nvpMatcher.lookingAt()) {
                            if (currentSectionMap == null) {
                                LOGGER.error("Line " + lineNo + " name=value line not under any [section]: '" + line  + "'");
                            } else {
                                final String name = nvpMatcher.group(1);
                                final String value = nvpMatcher.group(2);
                                LOGGER.debug("[" + currentSectionName + "] " + name + "=" + value);
                                currentSectionMap.put(name, value);
                            }
                        } else {
                            LOGGER.error("Line " + lineNo + " not matched against [section] or name=value: '" + line + "'");
                        }
                    }
                }
            } catch (final IOException ioe) {
                LOGGER.error("Could not load INI file: " + ioe.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (final IOException e1) {
                        LOGGER.error("Could not close BufferedReader: " + e1.getMessage());
                    }
                }
            }
        } catch (final FileNotFoundException e) {
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
        if (!mySectionMap.containsKey(sectionName)) {
            LOGGER.debug("getValue(" + sectionName + "," + name + "): not found [section]");
            return null;
        } else {
            final Map<String, String> sectionMap = mySectionMap.get(sectionName);
            final String value = sectionMap.get(name); // returns null on 'not found'
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
        final String value = getValue(sectionName, name);
        return value == null ? defaultValue : value;
    }

    /**
     * Remove a name=value pair from the file. If the removed entry is the final
     * entry in this [section name], remove the section name also.
     * @param sectionName the [section name] under which the name will be deleted.
     * @param name the name= that will be deleted, if it exists.
     */
    public final synchronized void removeValue(final String sectionName, final String name) {
        if (!mySectionMap.containsKey(sectionName)) {
            LOGGER.debug("removeValue(" + sectionName + ", " + name + "): not found [section]");
            return;
        }
        bDirty = true;
        final Map<String, String> sectionMap = mySectionMap.get(sectionName);
        sectionMap.remove(name);
        if (sectionMap.size() == 0) {
            LOGGER.debug("removeValue(" + sectionName + ", " + name
                    + "): final name returned from [section]; removing [section]");
            mySectionMap.remove(sectionName);
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
        if (sectionName == null || name == null || value == null) {
            throw new IllegalArgumentException("Null values cannot be stored: sectionName: "
                + sectionName + " name: "
                + name + " value: "
                + value);
        }
        Map<String, String> sectionMap = null;
        bDirty = true;
        if (!mySectionMap.containsKey(sectionName)) {
            sectionMap = new HashMap<String, String>();
            mySectionMap.put(sectionName, sectionMap);
            LOGGER.debug("setValue(" + sectionName + "," + name + "," + value + "): created new [class]");
        } else {
            sectionMap = mySectionMap.get(sectionName);
        }
        LOGGER.debug("setValue(" + sectionName + "," + name + "," + value + "): saving");
        sectionMap.put(name, value);
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
        final String value = getValue(sectionName, name, "0");
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException e) {
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
        final String value = getValue(sectionName, name, "0");
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            LOGGER.warn("Value of section [" + sectionName + "], name '" + name + "' is '" + value + "' which is not an integer");
            return 0;
        }
    }
    
    /**
     * Obtain a boolean value in the file.
     * @param sectionName the [section name] from which this value will be obtained.
     * @param name the name= key to the given value
     * @return the value, or false if this name does not exist.
     */
    public final boolean getBooleanValue(final String sectionName, final String name) {
        final String value = getValue(sectionName, name, "FALSE");
        try {
            return Boolean.parseBoolean(value);
        } catch (final NumberFormatException e) {
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
        if (!mySectionMap.containsKey(sectionName)) {
            LOGGER.debug("getArray(" + sectionName + "): not found [section]");
            return new String[0];
        } else {
            final Map<String, String> sectionMap = mySectionMap.get(sectionName);
            final ArrayList<String> array = new ArrayList<String>();
            for (int i = 0; i < sectionMap.size(); i++) {
                array.add(sectionMap.get("" + (i + 1)));
            }
            return array.toArray(new String[0]);
        }
    }
    
    /**
     * Set an array of values under a section name.
     * @param sectionName the [section name]
     * @param array the array of values
     */
    public final synchronized void setArray(final String sectionName, final String[] array) {
        bDirty = true;
        final Map<String, String> arrayMap = new HashMap<String, String>();
        for (int i = 0; i < array.length; i++) {
            arrayMap.put("" + (i + 1), array[i]);
        }
        mySectionMap.put(sectionName, arrayMap);
        LOGGER.debug("setArray(" + sectionName + ", ...): saving");
        saveFile();
    }
    
    /**
     * Completely remove a [section name], even if populated.
     * @param sectionName the [section name]
     */
    public final synchronized void removeSection(final String sectionName) {
        if (!mySectionMap.containsKey(sectionName)) {
            LOGGER.debug("removeSection(" + sectionName + "): not found [section]");
        } else {
            bDirty = true;
            mySectionMap.remove(sectionName);
            LOGGER.debug("removeSection(" + sectionName + "): saving");
            saveFile();
        }
    }
}
