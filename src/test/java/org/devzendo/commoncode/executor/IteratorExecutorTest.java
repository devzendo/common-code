package org.devzendo.commoncode.executor;

import org.apache.log4j.Logger;
import org.devzendo.commoncode.CCTestCase;
import org.devzendo.commoncode.executor.IteratorExecutor;
import org.junit.Test;


/**
 * Tests for IteratorExecutor
 * 
 * @author matt
 *
 */
public final class IteratorExecutorTest extends CCTestCase {
    private static final Logger LOGGER = Logger.getLogger(IteratorExecutor.class);
    
    /**
     * ls -l
     */
    @Test
    public void testLSL() {
        LOGGER.info("testLSL");
        final IteratorExecutor ie = new IteratorExecutor(new String[] {"ls", "-l" });
        while (ie.hasNext()) {
            final String line = (String) ie.next();
            LOGGER.info("Line: '" + line + "'");
        }
        LOGGER.info("Exit code is " + ie.getExitValue());
        LOGGER.info("IOException is " + ie.getIOException());
    }

    /**
     * mkisofs
     */
    @Test
    public void testMkISOFS() {
        LOGGER.info("testMkISOFS");
        final IteratorExecutor ie = new IteratorExecutor(new String[] {
                "mkisofs",
                "-r",
                "-gui",
                "-o",
                "/home/matt/Desktop/crap/testmkisofs.iso",
                "/home/matt/Desktop/crap/apache-tomcat-5.5.15"
        });
        ie.useStdErr();
        while (ie.hasNext()) {
            final String line = (String) ie.next();
            LOGGER.info("Line: '" + line + "'");
        }
        LOGGER.info("Exit code is " + ie.getExitValue());
        LOGGER.info("IOException is " + ie.getIOException());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
