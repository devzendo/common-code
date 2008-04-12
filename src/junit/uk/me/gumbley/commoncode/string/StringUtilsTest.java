package uk.me.gumbley.commoncode.string;

import org.apache.log4j.Logger;
import uk.me.gumbley.commoncode.CCTestCase;
import uk.me.gumbley.commoncode.string.StringUtils;

public class StringUtilsTest extends CCTestCase {
    private static Logger myLogger = Logger.getLogger(StringUtilsTest.class);
    public StringUtilsTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Logger getLogger() {
        return myLogger;
    }
    
    public void testJoin0() {
        myLogger.info("start testJoin0");
        assertEquals("", StringUtils.join(new String[] {}, "; "));
        myLogger.info("end testJoin0");
    }
    public void testJoin1() {
        myLogger.info("start testJoin1");
        assertEquals("XYZ", StringUtils.join(new String[] {"XYZ"}, "; "));
        myLogger.info("end testJoin1");
    }
    public void testJoin2() {
        myLogger.info("start testJoin2");
        assertEquals("XYZ; ABC", StringUtils.join(new String[] {"XYZ", "ABC"}, "; "));
        myLogger.info("end testJoin2");
    }
    public void testJoin3() {
        myLogger.info("start testJoin3");
        assertEquals("XYZ; ABC; DEF", StringUtils.join(new String[] {"XYZ", "ABC", "DEF"}, "; "));
        myLogger.info("end testJoin3");
    }

}
