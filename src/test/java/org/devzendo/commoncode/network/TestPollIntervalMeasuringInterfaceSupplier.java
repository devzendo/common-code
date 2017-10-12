package org.devzendo.commoncode.network;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.SocketException;

import static org.devzendo.commoncode.concurrency.ThreadUtils.waitNoInterruption;

/**
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TestPollIntervalMeasuringInterfaceSupplier {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final long MONITOR_INTERVAL = 2000L;

    private void expectInsufficentCallsToThrow() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Insufficient supply calls to measure intervals between calls");
    }

    @Test
    public void insufficientMeasuringCallsZero() throws SocketException {
        expectInsufficentCallsToThrow();

        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(MONITOR_INTERVAL);

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void insufficientMeasuringCallsOne() throws SocketException {
        expectInsufficentCallsToThrow();

        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(MONITOR_INTERVAL);
        interfaceSupplier.get();

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void measuringCallsShorterThanInterval() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(MONITOR_INTERVAL);
        interfaceSupplier.get();
        final long shortInterval = MONITOR_INTERVAL / 2;
        waitNoInterruption(shortInterval); // just subtracting 1 wouldn't be good enough, as this is timing-vague
        interfaceSupplier.get();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Supply call interval was < " + MONITOR_INTERVAL + "ms");

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void measuringCallsEqualToInterval() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(MONITOR_INTERVAL);
        interfaceSupplier.get();
        waitNoInterruption(MONITOR_INTERVAL);
        interfaceSupplier.get();

        interfaceSupplier.validateIntervals(); // all ok
    }

    @Test
    public void measuringCallsShorterThanIntervalWithThreeMeasurements() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(MONITOR_INTERVAL);
        interfaceSupplier.get();
        waitNoInterruption(MONITOR_INTERVAL);
        interfaceSupplier.get();

        final long shortInterval = MONITOR_INTERVAL / 2;
        waitNoInterruption(shortInterval); // just subtracting 1 wouldn't be good enough, as this is timing-vague
        interfaceSupplier.get();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Supply call interval was < " + MONITOR_INTERVAL + "ms");

        interfaceSupplier.validateIntervals();
    }

    @Test
    public void measuringCallsEqualToIntervalWithThreeMeasurements() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(MONITOR_INTERVAL);
        interfaceSupplier.get();
        waitNoInterruption(MONITOR_INTERVAL);
        interfaceSupplier.get();
        waitNoInterruption(MONITOR_INTERVAL);
        interfaceSupplier.get();

        interfaceSupplier.validateIntervals(); // all ok
    }
}
