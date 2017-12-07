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

import org.apache.log4j.Logger;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.hamcrest.text.MatchesPattern;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Tests the INIFile (not written TDD)
 * @author matt
 *
 */
public final class TestINIFile {
    private static final Logger LOGGER = Logger.getLogger(TestINIFile.class);

    private static final String SECTION_ARRAY = "ARRAY";
    private INIFile iniFile;
    private File tempFile;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
    }

    /**
     * Create temporary ini file
     * @throws IOException on failure
     */
    public void getINIFile() throws IOException {
        tempFile = File.createTempFile("common-unit-test", "ini").getAbsoluteFile();
        tempFile.deleteOnExit();
        iniFile = new INIFile(tempFile.getAbsolutePath());
    }
    
    /**
     * Tidy up
     * @throws IOException on failure
     */
    @After
    public void deleteTempFile() throws IOException {
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    /**
     * It's OK if your array is empty
     */
    @Test
    public void setEmptyArrayOK() throws IOException {
        getINIFile();

        iniFile.setArray(SECTION_ARRAY, new String[0]);
        final String[] array = iniFile.getArray(SECTION_ARRAY);
        Assert.assertNotNull(array);
        Assert.assertEquals(0, array.length);
    }
    
    /**
     * An array should be stored and retrieved in the same order
     */
    @Test
    public void arraysShouldBeStoredAndRetrievedOrdered() throws IOException {
        getINIFile();

        final String[] one = new String[] {
                "one", "two", "three", "four"
        };
        iniFile.setArray(SECTION_ARRAY, one);
        final String[] array = iniFile.getArray(SECTION_ARRAY);
        Assert.assertNotNull(array);
        Assert.assertEquals(one.length, array.length);
        for (int i = 0; i < one.length; i++) {
            Assert.assertEquals(one[i], array[i]);
        }
    }
    
    /**
     * Test to verify that setArray twice, second time with a smaller array
     * truncates the array.
     */
    @Test
    public void testSetArrayTruncates() throws IOException {
        getINIFile();

        final String[] one = new String[] {
                "one", "two", "three", "four"
        };
        iniFile.setArray(SECTION_ARRAY, one);
        final String[] two = new String[] {
                "hippo", "axolotl"
        };
        iniFile.setArray(SECTION_ARRAY, two);
        final String[] array = iniFile.getArray(SECTION_ARRAY);
        Assert.assertNotNull(array);
        Assert.assertEquals(two.length, array.length);
        for (int i = 0; i < two.length; i++) {
            Assert.assertEquals(two[i], array[i]);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullSectionIsDisallowed() throws IOException {
        getINIFile();

        iniFile.setValue(null, "foo", "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullNameIsDisallowed() throws IOException {
        getINIFile();

        iniFile.setValue("section", null, "bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullValueIsDisallowed() throws IOException {
        getINIFile();

        iniFile.setValue("section", "name", null);
    }

    /**
     * Prior to 1.1.0, failure to save and load was logged but then silently swallowed. In 1.1.0, the
     * IOExceptions are converted to UncheckedIOExceptions.
     */
    @Test
    public void loadFailureDueToFileNotFound() throws IOException {
        thrown.expect(UncheckedIOException.class);
        thrown.expectMessage(MatchesPattern.matchesPattern("INI file .* not found"));

        // try to load from a 'file' that's actually a directory
        File itsADirectory = new File(getTempDir(), "its-a-directory").getAbsoluteFile();
        LOGGER.info("directory is " + itsADirectory.getAbsolutePath());
        itsADirectory.deleteOnExit();
        Assert.assertTrue(itsADirectory.mkdir());
        Assert.assertTrue(itsADirectory.isDirectory());
        Assert.assertTrue(itsADirectory.exists());

        new INIFile(itsADirectory.getAbsolutePath());
    }

    @Test
    public void nameValuePairNotUnderSectionThrows() throws IOException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(MatchesPattern.matchesPattern("Line 1 name=value line not under any \\[section\\]: 'foo=bar'"));

        tempFile = writeToTempFile("foo=bar");

        new INIFile(tempFile.getAbsolutePath());
    }

    @Test
    public void lineNotNameValuePairNorSectionThrows() throws IOException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(MatchesPattern.matchesPattern("Line 1 not matched against \\[section\\] or name=value: 'barafundle'"));

        tempFile = writeToTempFile("barafundle");

        new INIFile(tempFile.getAbsolutePath());
    }

    @Test
    public void saveFailureDueToIOException() throws IOException {
        getINIFile();
        Assert.assertTrue(tempFile.setWritable(false));

        thrown.expect(UncheckedIOException.class);
        thrown.expectMessage(MatchesPattern.matchesPattern("Could not write INI file .*: .*"));

        iniFile.setValue("foo", "bar", "quux");
    }

    private File writeToTempFile(final String... strings) throws IOException {
        final File t =  File.createTempFile("common-unit-test", "ini").getAbsoluteFile();
        t.deleteOnExit();
        try (final FileWriter ft = new FileWriter(t)) {
            for (String string: strings) {
                ft.write(string + System.lineSeparator());
            }
        };
        return t;
    }

    private File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
