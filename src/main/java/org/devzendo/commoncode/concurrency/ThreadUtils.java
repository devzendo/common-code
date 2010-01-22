package org.devzendo.commoncode.concurrency;

/**
 * Thread handling utility code
 * 
 * @author matt
 *
 */
public final class ThreadUtils {
    private ThreadUtils() {
        // no instances
    }
    
    /**
     * Sleep and throw away any InterruptedException
     * @param ms duration in milliseconds
     */
    public static void waitNoInterruption(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (final InterruptedException e) {
            // do nothing
        }
    }
}
