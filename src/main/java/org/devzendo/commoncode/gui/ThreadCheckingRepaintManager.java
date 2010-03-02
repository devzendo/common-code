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

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * A repaint manager that checks that Swing updates are happening
 * on an event thread.
 * 
 * See http://www.clientjava.com/blog/2004/08/31/1093972473000.html
 * for source.
 *
 */
public class ThreadCheckingRepaintManager extends RepaintManager {
    private int mTabCount = 0;
    private boolean mCheckIsShowing = false;

    /**
     * Construct the repaint manager
     */
    public ThreadCheckingRepaintManager() {
        super();
    }

    /**
     * Construct the repaint manager
     * @param checkIsShowing whether 'isShowing' should be checked
     */
    public ThreadCheckingRepaintManager(final boolean checkIsShowing) {
        super();
        mCheckIsShowing = checkIsShowing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addInvalidComponent(final JComponent jComponent) {
        checkThread(jComponent);
        super.addInvalidComponent(jComponent);
    }

    private void checkThread(final JComponent c) {
        if (!SwingUtilities.isEventDispatchThread() && checkIsShowing(c)) {
            System.out.println("----------Wrong Thread START");
            System.out.println(getStracktraceAsString(new Exception()));
            dumpComponentTree(c);
            System.out.println("----------Wrong Thread END");
        }
    }

    private String getStracktraceAsString(final Exception e) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        e.printStackTrace(printStream);
        printStream.flush();
        return byteArrayOutputStream.toString();
    }

    private boolean checkIsShowing(final JComponent c) {
        if (!this.mCheckIsShowing) {
            return true;
        } else {
            return c.isShowing();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addDirtyRegion(final JComponent jComponent,
            final int i, final int i1,
            final int i2,
            final int i3) {
        checkThread(jComponent);
        super.addDirtyRegion(jComponent, i, i1, i2, i3);
    }

    private void dumpComponentTree(final Component component) {
        System.out.println("----------Component Tree");
        resetTabCount();
        Component c = component;
        for (; c != null; c = c.getParent()) {
            printTabIndent();
            System.out.println(c);
            printTabIndent();
            System.out.println("Showing:" + c.isShowing() + " Visible: " + c.isVisible());
            incrementTabCount();
        }
    }

    private void resetTabCount() {
        this.mTabCount = 0;
    }

    private void incrementTabCount() {
        this.mTabCount++;
    }

    private void printTabIndent() {
        for (int i = 0; i < this.mTabCount; i++) {
            System.out.print("\t");
        }
    }
    
    /**
     * Initialise the repaint manager with the thread-checking
     * version.
     */
    public static void initialise() {
        RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
    }
}
