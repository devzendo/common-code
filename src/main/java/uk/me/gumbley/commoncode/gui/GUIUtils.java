package uk.me.gumbley.commoncode.gui;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

public class GUIUtils {
    private static Logger myLogger = Logger.getLogger(GUIUtils.class);
    private GUIUtils() {
        super();
    }
    public static void runOnEventThread(final Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(run);
            } catch (InterruptedException e) {
                myLogger.warn(run.getClass().getSimpleName() + " was interrupted");
            } catch (InvocationTargetException e) {
                myLogger.warn("IncocationTargetExcpetion running " + run.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }
    }
    public static void invokeLaterOnEventThread(final Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
}
