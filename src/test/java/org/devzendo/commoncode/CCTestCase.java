package org.devzendo.commoncode;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Before;

/**
 * Base class for commoncode test classes - initialise logging
 * 
 * @author matt
 *
 */
public abstract class CCTestCase {
    private static final Logger LOGGER = Logger.getLogger(CCTestCase.class);
    private static boolean bInitialised = false;
    
    /**
     * Initialise log4j
     * @throws Exception on failure
     */
    @Before
    public final void setUpLogging() throws Exception {
        if (!bInitialised) {
            bInitialised = true;
            BasicConfigurator.resetConfiguration();
            BasicConfigurator.configure();
            LOGGER.debug("Initialising logging");
        }
    }
    
    /**
     * @return the Logger for the base class
     */
    protected abstract Logger getLogger();
}
