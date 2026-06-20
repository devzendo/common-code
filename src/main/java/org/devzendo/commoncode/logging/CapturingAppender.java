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

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A log4j Appender that captures events that it receives.
 *
 * Note that log4j is now a provided dependency of this project,
 * having switched over to slf4j. This class would be of use only if you
 * are using log4j as your top-level application logging framework.
 *
 * @author matt
 *
 */
@Plugin(
        name = CapturingAppender.PLUGIN_NAME,
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class CapturingAppender extends AbstractAppender {
    public static final String PLUGIN_NAME = "Capturing";

    private final List<LogEvent> events = new ArrayList<LogEvent>();

    @PluginFactory
    public static CapturingAppender createAppender(
            @PluginAttribute(value = "name", defaultString = "Capturing") final String name) {
        return new CapturingAppender(name);
    }

    public CapturingAppender(final String name) {
        super(name, null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(final LogEvent event) {
        synchronized (events) {
            // LogEvent instances may be reused by Log4j 2 (garbage-free mode);
            // take an immutable copy.
            events.add(event.toImmutable());
        }
    }

    public List<LogEvent> getEvents() {
        synchronized (events) {
            return Collections.unmodifiableList(events);
        }
    }

    public void clear() {
        synchronized (events) {
            events.clear();
        }
    }
}