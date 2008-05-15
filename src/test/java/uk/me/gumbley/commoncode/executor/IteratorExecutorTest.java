package uk.me.gumbley.commoncode.executor;

import org.apache.log4j.Logger;
import uk.me.gumbley.commoncode.CCTestCase;

public class IteratorExecutorTest extends CCTestCase {
    private static Logger myLogger = Logger.getLogger(IteratorExecutor.class);
    public IteratorExecutorTest(String arg0) {
        super(arg0);
    }
    
    public void testLSL() {
        myLogger.info("testLSL");
        IteratorExecutor ie = new IteratorExecutor(new String[] { "ls", "-l" });
        while (ie.hasNext()) {
            String line = (String)ie.next();
            myLogger.info("Line: '" + line + "'");
        }
        myLogger.info("Exit code is " + ie.getExitValue());
        myLogger.info("IOException is " + ie.getIOException());
    }

    public void testMkISOFS() {
        myLogger.info("testMkISOFS");
        IteratorExecutor ie = new IteratorExecutor( new String[] {
                "mkisofs",
                "-r",
                "-gui",
                "-o",
                "/home/matt/Desktop/crap/testmkisofs.iso",
                "/home/matt/Desktop/crap/apache-tomcat-5.5.15"
        } );
        ie.useStdErr();
        while (ie.hasNext()) {
            String line = (String)ie.next();
            myLogger.info("Line: '" + line + "'");
        }
        myLogger.info("Exit code is " + ie.getExitValue());
        myLogger.info("IOException is " + ie.getIOException());
    }
    protected Logger getLogger() {
        return myLogger;
    }
}
