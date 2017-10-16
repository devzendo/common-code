package org.devzendo.commoncode.network;

import org.devzendo.commoncode.concurrency.ThreadUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.ethernet;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.local;

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
public class TestCountingInterfaceSupplier {

    private final NetworkInterface localUp = local(true);
    private final NetworkInterface ethernetDown = ethernet(false);

    private final List<NetworkInterface> justLocalUp = singletonList(localUp);
    private final List<NetworkInterface> bothInterfaces = Arrays.asList(localUp, ethernetDown);
    private final CountingInterfaceSupplier cis = new CountingInterfaceSupplier(justLocalUp, bothInterfaces);

    private final Thread waitForExhaustionThread = new Thread(cis::waitForDataExhaustion);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void noCallsAtFirst() {
        assertThat(cis.numberOfTimesCalled()).isZero();
    }

    @Test
    public void givesYouTheConstructorSupplies() {
        assertThat(list(cis.get())).contains(localUp);
        assertThat(cis.numberOfTimesCalled()).isEqualTo(1);

        assertThat(list(cis.get())).contains(localUp, ethernetDown);
        assertThat(cis.numberOfTimesCalled()).isEqualTo(2);
    }

    @Test
    public void calledMoreFrequentlyThanYouHaveDataForCausesThrow() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Interface supplier called more frequently than expected");

        cis.get();
        cis.get();
        cis.get(); // boom
    }

    @Test(timeout = 2000)
    public void waitForExhaustionWaitsIfNotExhausted() {
        waitForExhaustionThread.start();

        ThreadUtils.waitNoInterruption(250);

        final Thread.State state = waitForExhaustionThread.getState();
        try {
            assertThat(state).isEqualTo(Thread.State.WAITING);
        } finally {
            waitForExhaustionThread.interrupt();
        }
    }

    @Test(timeout = 2000)
    public void waitForExhaustionReturnsIfExhausted() {
        waitForExhaustionThread.start();

        cis.get();
        cis.get();

        ThreadUtils.waitNoInterruption(250);

        final Thread.State state = waitForExhaustionThread.getState();
        try {
            assertThat(state).isEqualTo(Thread.State.TERMINATED);
        } finally {
            waitForExhaustionThread.interrupt();
        }
    }
}
