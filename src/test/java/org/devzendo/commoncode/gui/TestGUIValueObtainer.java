package org.devzendo.commoncode.gui;

import java.util.concurrent.Callable;
import javax.swing.JLabel;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.devzendo.commoncode.CCTestCase;
import org.devzendo.commoncode.gui.GUIUtils;
import org.devzendo.commoncode.gui.GUIValueObtainer;
import org.junit.Test;


/**
 * @author matt
 *
 */
public final class TestGUIValueObtainer extends CCTestCase {
    private static final Logger LOGGER = Logger
            .getLogger(TestGUIValueObtainer.class);
    
    private JLabel label;
    private Object lock;
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
    
    /**
     * @throws Exception but won't
     */
    @Test
    public void shouldGetValuesFromEDT() throws Exception {
        lock = new Object();
        synchronized (lock) {
            GUIUtils.runOnEventThread(new Runnable() {
                public void run() {
                    label = new JLabel("hello");
                }
            });
        }
        
        final GUIValueObtainer<String> obtainer = new GUIValueObtainer<String>();
        final String labelText = obtainer.obtainFromEventThread(new Callable<String>() {

            public String call() throws Exception {
                synchronized (lock) {
                    return label.getText();
                }
            }
            
        }); 
        Assert.assertEquals("hello", labelText);
    }
}
