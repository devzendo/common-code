package uk.me.gumbley.commoncode;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import junit.framework.TestCase;

public abstract class CCTestCase extends TestCase {
    private static Logger myLogger = Logger.getLogger(CCTestCase.class);
    private static boolean bInitialised = false;
    protected CCTestCase(String arg) {
        super(arg);
    }
    protected void setUp() throws Exception {
        super.setUp();
        if (!bInitialised) {
            bInitialised = true;
            BasicConfigurator.resetConfiguration();
            BasicConfigurator.configure();
            myLogger.debug("Initialising logging");
        }
    }
    protected abstract Logger getLogger();
}
