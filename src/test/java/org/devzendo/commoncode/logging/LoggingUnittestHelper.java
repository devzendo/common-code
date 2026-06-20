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
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

/**
 * Helper class for initialising log4j.
 * @author matt
 *
 */
public final class LoggingUnittestHelper {
    /**
     * Initialise logging.
     */
    public static void setupLogging() {
        ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();

        LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p %c{1}:%L - %m%n");

        AppenderComponentBuilder console = builder.newAppender("CONSOLE", "Console")
                .addAttribute("target", "SYSTEM_OUT")
                .add(layout);
        builder.add(console);

        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG)
                .add(builder.newAppenderRef("CONSOLE"));
        builder.add(rootLogger);

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.reconfigure(builder.build());
    }
}
