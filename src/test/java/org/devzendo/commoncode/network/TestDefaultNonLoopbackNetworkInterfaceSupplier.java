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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.devzendo.commoncode.logging.LogCapturingUnittestHelper;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.devzendo.commoncode.logging.IsLogEvent.logEvent;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.ethernet;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.local;
import static org.hamcrest.Matchers.hasItems;

public class TestDefaultNonLoopbackNetworkInterfaceSupplier extends LogCapturingUnittestHelper {
    private final NetworkInterface localUp = local(true);
    private final NetworkInterface ethernetUp = ethernet(true);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final class FakeSupplier implements NetworkInterfaceSupplier {
        @Override
        public Enumeration<NetworkInterface> get() {
            return enumeration(asList(localUp, ethernetUp));
        }
    }

    @Test
    public void filterNonLoopbackInterfaces() throws SocketException {
        Mockito.when(localUp.isLoopback()).thenReturn(true);
        Mockito.when(ethernetUp.isLoopback()).thenReturn(false);

        final DefaultNonLoopbackNetworkInterfaceSupplier nonLoopback = new DefaultNonLoopbackNetworkInterfaceSupplier(new FakeSupplier());
        final Enumeration<NetworkInterface> enumeration = nonLoopback.get();
        assertThat(enumeration.hasMoreElements()).isTrue();
        assertThat(enumeration.nextElement()).isEqualTo(ethernetUp);
        assertThat(enumeration.hasMoreElements()).isFalse();
    }

    @Test
    public void realSupplierActuallyReturnsSomething() {
        final DefaultNonLoopbackNetworkInterfaceSupplier real = new DefaultNonLoopbackNetworkInterfaceSupplier();
        final Enumeration<NetworkInterface> enumeration = real.get();
        assertThat(enumeration.hasMoreElements()).isTrue();
    }

    public static NetworkInterface explodingEthernet() {
        final NetworkInterface ethernet = Mockito.mock(NetworkInterface.class);
        try {
            Mockito.when(ethernet.getName()).thenReturn("boom0");
            Mockito.when(ethernet.isLoopback()).thenThrow(SocketException.class);
        } catch (final SocketException e) {
            throw new IllegalStateException(e);
        }
        return ethernet;
    }

    private final class ExplodingSupplier implements NetworkInterfaceSupplier {
        @Override
        public Enumeration<NetworkInterface> get() {
            return enumeration(singletonList(explodingEthernet()));
        }
    }

    @Test
    public void whenSupplierBlowsUp() {
        final DefaultNonLoopbackNetworkInterfaceSupplier nonLoopback = new DefaultNonLoopbackNetworkInterfaceSupplier(new ExplodingSupplier());
        final Enumeration<NetworkInterface> enumeration = nonLoopback.get();

        assertThat(enumeration.hasMoreElements()).isFalse();

        final List<LogEvent> events = getLogEvents();
        assertThat(events).hasSize(1);
        MatcherAssert.assertThat(events, hasItems(
                logEvent(Level.WARN, "Could not determine whether network interface 'boom0' is loopback: null")));
    }
}
