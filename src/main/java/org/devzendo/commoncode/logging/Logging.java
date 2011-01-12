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

package org.devzendo.commoncode.logging;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Initialisation toolkit for log4j logging, given command line
 * options.
 * 
 * @author matt
 *
 */
public final class Logging {
    private static Logging myInstance = null;
    
    private Logging() {
        super();
    }
    
    /**
     * Singleton constructor for Logging
     * @return the single instance of Logging.
     */
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
     * 
     * This is a shim version of the variant that just takes and returns a List,
     * but using ArrayLists, for backwards compatibility. You should use the
     * List variant.
     * 
     * @param origArgs the command line arguments
     * @return those arguments with the logging arguments removed
     * @deprecated since you should use the List variant
     */
    @Deprecated
    public ArrayList<String> setupLoggingFromArgs(final ArrayList<String> origArgs) {
        return new ArrayList<String>(internalSetupLoggingFromArgs(origArgs));
    }
    
    /**
     * Sets up log4j given command line arguments, called only once at the start
     * of main, with the command line args. Changes to the layout (for example)
     * can be made after this call.
     * @param origArgs the command line arguments
     * @return those arguments with the logging arguments removed
     */
    public List<String> setupLoggingFromArgs(final List<String> origArgs) {
        return internalSetupLoggingFromArgs(origArgs);
    }

    private List<String> internalSetupLoggingFromArgs(
            final List<String> origArgs) {
        BasicConfigurator.resetConfiguration();
        final ArrayList<String> out = new ArrayList<String>();
        boolean bLevel = false;
        boolean bDebug = false;
        boolean bClasses = false;
        boolean bThreads = false;
        boolean bTimes = false;
        for (final String arg : origArgs) {
            if (arg.equals("-debugall")) {
                bLevel = true;
                bDebug = true;
                bClasses = true;
                bThreads = true;
                bTimes = true;
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
        myLayout = createLayout(bLevel, bClasses, bThreads, bTimes);
        final Logger root = Logger.getRootLogger();
        root.removeAllAppenders();
        final Appender appender = new ConsoleAppender(myLayout);
        root.addAppender(appender);
        root.setLevel(bDebug ? Level.DEBUG : Level.INFO);
        return out;
    }

    private PatternLayout createLayout(
            final boolean bLevel,
            final boolean bClasses,
            final boolean bThreads,
            final boolean bTimes) {
        final StringBuilder sb = new StringBuilder();
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
        return new PatternLayout(sb.toString());
    }
    
    /**
     * @return the PatternLayout in use
     */
    public PatternLayout getLayout() {
        return myLayout;
    }
    
    /**
     * Set a new PatternLayout to use
     * @param layout the new layout
     */
    public void setLayout(final PatternLayout layout) {
        myLayout = layout;
        final Logger root = Logger.getRootLogger();
        final Enumeration<?> en = root.getAllAppenders();
        while (en.hasMoreElements()) {
            final Appender appender = (Appender) en.nextElement();
            appender.setLayout(myLayout);
        }
    }

    /**
     * Sets the log threshold level of a given package so that only logs of that
     * level are emitted (given overall logging threshold, i.e. use -debug to
     * emit EVERYTHING) 
     * 
     * @param packageName e.g. org.springframework
     * @param level e.g. Level.WARN
     */
    public void setPackageLoggingLevel(final String packageName, final Level level) {
        final LoggerRepository defaultHierarchy = LogManager.getLoggerRepository();
        final Logger logger = defaultHierarchy.getLogger(packageName);
        if (logger != null) {
            logger.setLevel(level);
        }
    }
}
