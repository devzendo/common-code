package uk.me.gumbley.commoncode.gui;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Various GUI utilitiy toolkit methods.
 * 
 * @author matt
 *
 */
public final class GUIUtils {
    private static final Logger LOGGER = Logger.getLogger(GUIUtils.class);
   
    private GUIUtils() {
        super();
    }
   
    /**
     * Pass a Runnable to be run immediately on the event thread. If we're
     * already on the event thread, run it immediately.
     * @param run the Runnable to run.
     */
    public static void runOnEventThread(final Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(run);
            } catch (final InterruptedException e) {
                LOGGER.warn(run.getClass().getSimpleName() + " was interrupted", e);
            } catch (final InvocationTargetException e) {
                LOGGER.warn("InvocationTargetExcpetion running " + run.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Start a Runnable on the event thread and wait for it to complete.
     * If we're already on the event thread, run it immediately.
     * @param run the Runnable to run.
     */
    public static void invokeLaterOnEventThread(final Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
    

    private static final double FACTOR = 0.92;

    /**
     * Compute a colour slightly darker than the one passed in
     * @param color a colour
     * @return a shade darker
     */
    public static Color slightlyDarkerColor(final Color color) {
        return new Color(Math.max((int) (color.getRed() * FACTOR), 0), 
                 Math.max((int) (color.getGreen() * FACTOR), 0),
                 Math.max((int) (color.getBlue() * FACTOR), 0));
    }
}
