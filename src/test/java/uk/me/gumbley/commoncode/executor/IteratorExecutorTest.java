package uk.me.gumbley.commoncode.executor;

import org.apache.log4j.Logger;
import org.junit.Test;
import uk.me.gumbley.commoncode.CCTestCase;

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
        IteratorExecutor ie = new IteratorExecutor(new String[] {"ls", "-l" });
        while (ie.hasNext()) {
            String line = (String) ie.next();
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
        IteratorExecutor ie = new IteratorExecutor(new String[] {
                "mkisofs",
                "-r",
                "-gui",
                "-o",
                "/home/matt/Desktop/crap/testmkisofs.iso",
                "/home/matt/Desktop/crap/apache-tomcat-5.5.15"
        });
        ie.useStdErr();
        while (ie.hasNext()) {
            String line = (String) ie.next();
            LOGGER.info("Line: '" + line + "'");
        }
        LOGGER.info("Exit code is " + ie.getExitValue());
        LOGGER.info("IOException is " + ie.getIOException());
    }

    /**
     * {@inheritDoc}
     */
    protected Logger getLogger() {
        return LOGGER;
    }
}
