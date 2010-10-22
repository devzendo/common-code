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
/**
 * 
 */
package org.devzendo.commoncode.logging;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A log4j Appender that captures events that it receives.
 * 
 * @author matt
 *
 */
public class CapturingAppender implements Appender {
    private final List<LoggingEvent> events = new ArrayList<LoggingEvent>();

    /**
     * @return the list of all received events 
     */
    public final List<LoggingEvent> getEvents() {
        return events;
    }

    /**
     * {@inheritDoc}
     */
    public void addFilter(final Filter arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void clearFilters() {
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
    }

    /**
     * {@inheritDoc}
     */
    public void doAppend(final LoggingEvent event) {
        events.add(event);
    }

    /**
     * {@inheritDoc}
     */
    public ErrorHandler getErrorHandler() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Filter getFilter() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Layout getLayout() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean requiresLayout() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setErrorHandler(final ErrorHandler arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void setLayout(final Layout arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void setName(final String arg0) {
    }
}