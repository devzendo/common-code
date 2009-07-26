package uk.me.gumbley.commoncode.string;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import uk.me.gumbley.commoncode.CCTestCase;

/**
 * Tests for String Utilities
 * 
 * @author matt
 *
 */
public final class StringUtilsTest extends CCTestCase {
    private static final Logger LOGGER = Logger.getLogger(StringUtilsTest.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin0() {
        LOGGER.info("start testJoin0");
        Assert.assertEquals("", StringUtils.join(new String[] {}, "; "));
        LOGGER.info("end testJoin0");
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin1() {
        LOGGER.info("start testJoin1");
        Assert.assertEquals("XYZ", StringUtils.join(new String[] {"XYZ"}, "; "));
        LOGGER.info("end testJoin1");
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin2() {
        LOGGER.info("start testJoin2");
        Assert.assertEquals("XYZ; ABC", StringUtils.join(new String[] {"XYZ", "ABC"}, "; "));
        LOGGER.info("end testJoin2");
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin3() {
        LOGGER.info("start testJoin3");
        Assert.assertEquals("XYZ; ABC; DEF", StringUtils.join(new String[] {"XYZ", "ABC", "DEF"}, "; "));
        LOGGER.info("end testJoin3");
    }
    
    /**
     * Test pluralisation
     */
    @Test
    public void testPluralise() {
        Assert.assertEquals("Files", StringUtils.pluralise("File", 2));
        Assert.assertEquals("File", StringUtils.pluralise("File", 1));
    }
    
    /**
     * Test return of are or is
     */
    @Test
    public void testAreIs() {
        Assert.assertEquals("are", StringUtils.getAreIs(2));
        Assert.assertEquals("is", StringUtils.getAreIs(1));
    }
    
    /**
     * Tests ASCII conversion
     */
    @Test
    public void testStrToASCII() {
        final byte[] ascii = StringUtils.stringToASCII("ABC");
        Assert.assertEquals(3, ascii.length);
        Assert.assertEquals((byte) 65, ascii[0]);
        Assert.assertEquals((byte) 66, ascii[1]);
        Assert.assertEquals((byte) 67, ascii[2]);
    }
    
    /**
     * Test the masking of sensitive text
     */
    @Test
    public void maskString() {
        Assert.assertEquals("***", StringUtils.maskSensitiveText("abc"));
        Assert.assertEquals("****", StringUtils.maskSensitiveText("****"));
        Assert.assertEquals("", StringUtils.maskSensitiveText(""));
        Assert.assertEquals("", StringUtils.maskSensitiveText(null));
    }
}
