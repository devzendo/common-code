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

package org.devzendo.commoncode.network;

import org.devzendo.commoncode.time.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;

public class PollIntervalMeasuringInterfaceSupplier implements NetworkInterfaceSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollIntervalMeasuringInterfaceSupplier.class);

    private final Sleeper sleeper;
    private final long monitorInterval;
    private final List<Long> callTimes = new ArrayList<>();

    public PollIntervalMeasuringInterfaceSupplier(final Sleeper sleeper, final long monitorInterval) {
        this.sleeper = sleeper;
        this.monitorInterval = monitorInterval;
        LOGGER.info("Monitor interval is " + monitorInterval + " ms");
    }

    public void validateIntervals() {
        final int size = callTimes.size();
        if (size < 2) {
            throw new IllegalStateException("Insufficient supply calls to measure intervals between calls");
        }

        final String callTimesString = callTimes.stream().map(Object::toString).collect(Collectors.joining(", "));
        LOGGER.info("Poll Call Times: [" + callTimesString + "]");

        final List<Long> intervals = new ArrayList<>();
        for (int i = 0; i < size - 1; i++) {
            final Long first = callTimes.get(i);
            final Long second = callTimes.get(i + 1);
            final Long interval = second - first; // shouldn't go back in time here, so no need for abs
            intervals.add(interval);
        }
        final String intervalsString = intervals.stream().map(Object::toString).collect(Collectors.joining(", "));
        LOGGER.info("Poll Intervals: [" + intervalsString + "]");
        intervals.forEach((Long interval) -> {
            if (interval < monitorInterval) {
                throw new IllegalStateException("Supply call interval was < " + monitorInterval + "ms");
            }
        });
    }

    @Override
    public Enumeration<NetworkInterface> get() {
        final long millis = sleeper.currentTimeMillis();
        LOGGER.debug("Calling fake supplier at " + millis);
        callTimes.add(millis);
        return enumeration(emptyList());
    }
}
