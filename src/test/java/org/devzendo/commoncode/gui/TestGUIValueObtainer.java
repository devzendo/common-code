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

import javax.swing.JLabel;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.devzendo.commoncode.CCTestCase;
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
