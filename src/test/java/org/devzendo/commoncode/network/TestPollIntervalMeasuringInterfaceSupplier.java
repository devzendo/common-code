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

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.devzendo.commoncode.time.Sleeper;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.Enumeration;

public class TestPollIntervalMeasuringInterfaceSupplier {
    private final Logger LOGGER = LoggerFactory.getLogger(TestPollIntervalMeasuringInterfaceSupplier.class);

    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
        // Want to see detailed logs, for diagnostics including milliseconds
        final Enumeration allAppenders = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            final Appender appender = (Appender) allAppenders.nextElement();
            appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p %c{1}:%L - %m%n"));
        }
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final long MONITOR_INTERVAL = 2000L;
    private static final Sleeper SLEEPER = new Sleeper(20);

    private void expectInsufficentCallsToThrow() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Insufficient supply calls to measure intervals between calls");
    }

    @Test
    public void insufficientMeasuringCallsZero() throws SocketException {
        expectInsufficentCallsToThrow();

        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void insufficientMeasuringCallsOne() throws SocketException {
        expectInsufficentCallsToThrow();

        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);
        interfaceSupplier.get();

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void measuringCallsShorterThanInterval() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);
        interfaceSupplier.get();
        final long shortInterval = MONITOR_INTERVAL / 2;
        LOGGER.info("sleeping for short interval " + shortInterval);
        SLEEPER.sleep(shortInterval); // just subtracting 1 wouldn't be good enough, as this is timing-vague
        interfaceSupplier.get();

        SLEEPER.sleep(250);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Supply call interval was < " + MONITOR_INTERVAL + "ms");

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void measuringCallsEqualToInterval() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);
        interfaceSupplier.get();
        SLEEPER.sleep(MONITOR_INTERVAL);
        interfaceSupplier.get();

        interfaceSupplier.validateIntervals(); // all ok
    }

    @Test
    public void measuringCallsShorterThanIntervalWithThreeMeasurements() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);
        interfaceSupplier.get();
        SLEEPER.sleep(MONITOR_INTERVAL);
        interfaceSupplier.get();

        final long shortInterval = MONITOR_INTERVAL / 2;
        SLEEPER.sleep(shortInterval); // just subtracting 1 wouldn't be good enough, as this is timing-vague
        interfaceSupplier.get();

        SLEEPER.sleep(250);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Supply call interval was < " + MONITOR_INTERVAL + "ms");

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void measuringCallsEqualToIntervalWithThreeMeasurements() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);
        interfaceSupplier.get();
        SLEEPER.sleep(MONITOR_INTERVAL);
        interfaceSupplier.get();
        SLEEPER.sleep(MONITOR_INTERVAL);
        interfaceSupplier.get();

        interfaceSupplier.validateIntervals(); // all ok
    }
}
