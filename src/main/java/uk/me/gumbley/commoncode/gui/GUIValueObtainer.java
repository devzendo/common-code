package uk.me.gumbley.commoncode.gui;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

/**
 * Allows values to be obtained from the Swing Event Thread, presumably from
 * some GUI component. 
 * @author matt
 *
 * @param <V> the type of the value to be obtained.
 */
public class GUIValueObtainer<V>  {
    private static final Logger LOGGER = Logger
            .getLogger(GUIValueObtainer.class);
    private CountDownLatch latch;
    private Object lock = new Object();
    private V returnObject;
    private Exception exception;
    
    /**
     * Obtain a value from some GUI component, by calling it on the event thread
     * @param call a Callable of type V that will be executed on the event
     * thread
     * @return the object returned by the Callable on the event thread
     * @throws Exception if the Callable 
     */
    public V obtainFromEventThread(final Callable<V> call) throws Exception {
        synchronized (lock) {
            returnObject = null;
            exception = null;
            latch = new CountDownLatch(1);
        }
        if (SwingUtilities.isEventDispatchThread()) {
            callAndStoreResultAndException(call);
        } else {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    callAndStoreResultAndException(call);
                }
            });
        }
        latch.await();
        synchronized (lock) {
            if (exception != null) {
                LOGGER.warn("Rethrowing exception created on the event thread: " + exception.getMessage(), exception);
                throw exception;
            }
            return returnObject;
        }
    }

    private void callAndStoreResultAndException(final Callable<V> call) {
        assert SwingUtilities.isEventDispatchThread();
        
        synchronized (lock) {
            try {
                returnObject = call.call();
            } catch (final Exception e) {
                exception = e;
            }
            latch.countDown();
        }
    }
}
