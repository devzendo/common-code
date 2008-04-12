package uk.me.gumbley.commoncode.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public final class Logging {
    private static Logging myInstance = null;
    private Logging() {
        super();
    }
    public static synchronized Logging getInstance() {
        if (myInstance == null) {
            myInstance = new Logging();
        }
        return myInstance;
    }
    
    private PatternLayout myLayout;
    
    /**
     * Sets up log4j given command line arguments, called only once at the start
     * of main, with the command line args. Changes to the layout (for example)
     * can be made after this call.
     * @param origArgs the command line arguments
     * @return those arguments with the logging arguments removed
     */
    public ArrayList<String> setupLoggingFromArgs(final ArrayList<String> origArgs) {
        BasicConfigurator.resetConfiguration();
        Level lev = Level.INFO;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> out = new ArrayList<String>();
        boolean bLevel = false;
        boolean bDebug = false;
        boolean bClasses = false;
        boolean bThreads = false;
        boolean bTimes = false;
        for (String arg: origArgs) {
            if (arg.equals("-debugall")) {
                bLevel = bDebug = bClasses = bThreads = bTimes = true;
                continue;
            }
            if (arg.equals("-level")) {
                bLevel = true;
                continue;
            }
            if (arg.equals("-debug")) {
                bDebug = true;
                continue;
            }
            if (arg.equals("-classes")) {
                bClasses = true;
                continue;
            }
            if (arg.equals("-threads")) {
                bThreads = true;
                continue;
            }
            if (arg.equals("-times")) {
                bTimes = true;
                continue;
            }
            out.add(arg);
        }
        if (bDebug) {
            lev = Level.DEBUG;
        }
        if (bLevel) {
            sb.append("%-5p ");
        }
        if (bTimes) {
            sb.append("%d{ISO8601} ");
        }
        if (bClasses) {
            sb.append("%c{1} ");
        }
        if (bThreads) {
            sb.append("[%t] ");
        }
        sb.append("%m\n");
        Logger root = Logger.getRootLogger();
        root.removeAllAppenders();
        myLayout = new PatternLayout(sb.toString());
        Appender appender = new ConsoleAppender(myLayout);
        root.addAppender(appender);
        root.setLevel(lev);
        return out;
    }
    public PatternLayout getLayout() {
        return myLayout;
    }
    public void setLayout(final PatternLayout layout) {
        myLayout = layout;
        Logger root = Logger.getRootLogger();
        Enumeration en = root.getAllAppenders();
        while (en.hasMoreElements()) {
            Appender appender = (Appender)en.nextElement();
            appender.setLayout(myLayout);
        };
    }
}
