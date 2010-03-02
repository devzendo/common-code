/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org <http://devzendo.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.gui;

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
