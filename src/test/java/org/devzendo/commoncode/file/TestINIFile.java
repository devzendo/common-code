package org.devzendo.commoncode.file;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.devzendo.commoncode.CCTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



/**
 * Tests the INIFile (not written TDD)
 * @author matt
 *
 */
public final class TestINIFile extends CCTestCase {
    private static final Logger LOGGER = Logger.getLogger(TestINIFile.class);
    private static final String SECTION_ARRAY = "ARRAY";
    private INIFile iniFile;
    private File tempFile;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
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
