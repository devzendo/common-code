/*
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.devzendo.commoncode.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;
/**
 * Initialisation toolkit for log4j logging, given command line
 * options.
 * Note that log4j is now a provided dependency of this project,
 * having switched over to slf4j. This class would be of use only if you
 * are using log4j as your top-level application logging framework.
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
    private boolean mDebug;
    private boolean mWarn;

    /**
     * Sets up log4j given command line arguments, called only once at the start
     * of main, with the command line args. Changes to the layout (for example)
     * can be made after this call.
     * @param origArgs the command line arguments
     * @return those arguments with the logging arguments removed
     */
    public List<String> setupLoggingFromArgs(final List<String> origArgs) {
        final ArrayList<String> out = new ArrayList<String>();
        boolean bLevel = false;
        mDebug = false;
        mWarn = false;
        boolean bClasses = false;
        boolean bThreads = false;
        boolean bTimes = false;
        for (final String arg : origArgs) {
            if (arg.equals("-debugall")) {
                bLevel = true;
                mDebug = true;
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
                mDebug = true;
                continue;
            }
            if (arg.equals("-warn")) {
                mWarn = true;
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

        ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();

        LayoutComponentBuilder myLayout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", createLayout(bLevel, bClasses, bThreads, bTimes));

        AppenderComponentBuilder consoleAppender = builder.newAppender("CONSOLE", "Console")
                .addAttribute("target", "SYSTEM_OUT")
                .add(myLayout);
        builder.add(consoleAppender);

        // Configure the root logger to use only this appender
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(mDebug ? Level.DEBUG : mWarn ? Level.WARN : Level.INFO)
                .add(builder.newAppenderRef("CONSOLE"));
        builder.add(rootLogger);

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure(builder.build());

        return out;
    }

    private String createLayout(
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
        return sb.toString();
    }
    
    /**
     * @return the PatternLayout in use
     */
    public PatternLayout getLayout() {
        return myLayout;
    }
    
    /**
     * Set a new pattern to use
     * @param pattern the new layout
     */
    public void setLayout(final String pattern) {
        final ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();

        final LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", pattern);

        final AppenderComponentBuilder consoleAppender = builder.newAppender("CONSOLE", "Console")
                .addAttribute("target", "SYSTEM_OUT")
                .add(layout);
        builder.add(consoleAppender);

        final RootLoggerComponentBuilder rootLogger =
                builder.newRootLogger(((Logger) LogManager.getRootLogger()).getLevel())
                .add(builder.newAppenderRef("CONSOLE"));
        builder.add(rootLogger);

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure(builder.build());
    }

    /**
     * Sets the log threshold level of a given package so that only logs of that
     * level are emitted.
     * <p>
     * <b>If debug mode has been set previously, this will have
     * no effect.</b>
     * 
     * @param packageName e.g. org.springframework
     * @param level e.g. Level.WARN
     */
    public void setPackageLoggingLevel(final String packageName, final Level level) {
        if (mDebug) {
            return; // We want to see EVERYTHING in debug mode.
        }

        Configurator.setLevel(packageName, level);
    }
}
