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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Can log events be captured?
 * 
 * @author matt
 *
 */
public final class TestCapturingAppender {
    private LoggerContext context;

    /**
     * 
     */
    @Before
    public void setupLogging() {
        ConfigurationBuilder<BuiltConfiguration> builder =
                ConfigurationBuilderFactory.newConfigurationBuilder();

        LayoutComponentBuilder layout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n");

        AppenderComponentBuilder consoleAppender = builder.newAppender("CONSOLE", "Console")
                .addAttribute("target", "SYSTEM_OUT")
                .add(layout);
        builder.add(consoleAppender);

        AppenderComponentBuilder appender = builder.newAppender("CAPTURE", "Capturing");
        builder.add(appender);   // <-- register the appender with the builder itself

        // Root logger, referencing the appender by NAME (not the builder object)
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG)
                .add(builder.newAppenderRef("CAPTURE"));
        builder.add(rootLogger); // <-- register the root logger with the builder itself

        context = new LoggerContext("CapturingTestContext");
        context.start(builder.build());
    }

    @After
    public void teardownLogging() {
        if (context != null) {
            context.stop();
        }
    }

    /**
     * 
     */
    @Test
    public void testCapture() {
        final Logger logger = (Logger) context.getLogger(this.getClass());
        logger.debug("Hello logger");
        CapturingAppender appender = (CapturingAppender) context.getConfiguration().getAppender("CAPTURE");

        Assert.assertEquals(1, appender.getEvents().size());
        final LogEvent logEvent = appender.getEvents().get(0);
        Assert.assertEquals(Level.DEBUG, logEvent.getLevel());
        Assert.assertEquals("Hello logger", logEvent.getMessage().getFormattedMessage());
    }
}
