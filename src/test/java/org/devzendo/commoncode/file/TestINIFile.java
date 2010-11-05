/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org <http://devzendo.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.file;

import java.io.File;
import java.io.IOException;

import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Tests the INIFile (not written TDD)
 * @author matt
 *
 */
public final class TestINIFile {
    private static final String SECTION_ARRAY = "ARRAY";
    private INIFile iniFile;
    private File tempFile;

    /**
     * 
     */
    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
    }

    /**
     * Create temporary ini file
     * @throws IOException on failure
     */
    @Before
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
        tempFile.delete();
    }

    /**
     * It's OK if your array is empty
     */
    @Test
    public void setEmptyArrayOK() {
        iniFile.setArray(SECTION_ARRAY, new String[0]);
        final String[] array = iniFile.getArray(SECTION_ARRAY);
        Assert.assertNotNull(array);
        Assert.assertEquals(0, array.length);
    }
    
    /**
     * An array should be stored and retrieved in the same order
     */
    @Test
    public void arraysShouldBeStoredAndRetrievedOrdered() {
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
    public void testSetArrayTruncates() {
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
    
    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullSectionIsDisallowed() {
        iniFile.setValue(null, "foo", "bar");
    }

    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullNameIsDisallowed() {
        iniFile.setValue("section", null, "bar");
    }

    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullValueIsDisallowed() {
        iniFile.setValue("section", "name", null);
    }
}
