package org.devzendo.commoncode.network;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
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
public class TestNetworkMonitor {

    private static final String ETHERNET_INTERFACE_NAME = "eth0"; // for tests, use a linuxy name

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private NetworkMonitor monitor;
    public static final long MONITOR_INTERVAL = 2000L;

    @After
    public void stopMonitor() {
        if (monitor != null) {
            monitor.stop();
        }
    }

    private static class CountingInterfaceSupplier implements Supplier<Enumeration<NetworkInterface>> {
        private final List<NetworkInterface>[] toBeReturned;
        int count = 0;
        @SafeVarargs
        public CountingInterfaceSupplier(final List<NetworkInterface> ... toBeReturned) {
            this.toBeReturned = toBeReturned;
        }

        @Override
        public synchronized Enumeration<NetworkInterface> get() {
            if (count == toBeReturned.length) {
                throw new IllegalStateException("Interface supplier called more frequently than expected");
            }
            final List<NetworkInterface> networkInterfaces = toBeReturned[count++];
            return enumeration(networkInterfaces);
        }

        public synchronized int numberOfTimesCalled() {
            return count;
        }
    }

    private static class EmptyInterfaceSupplier implements Supplier<Enumeration<NetworkInterface>> {
        @Override
        public Enumeration<NetworkInterface> get() {
            return enumeration(emptyList());
        }
    }

    @Test
    public void monitorNotRunningUntilStartedThenStopsWhenStopped() {
        monitor = new NetworkMonitor(new EmptyInterfaceSupplier(), MONITOR_INTERVAL);
        assertThat(monitor.isRunning()).isFalse();
        monitor.start();
        waitNoInterruption(250);
        assertThat(monitor.isRunning()).isTrue();
        waitNoInterruption(250);
        monitor.stop();
        waitNoInterruption(250);
        assertThat(monitor.isRunning()).isFalse();
    }

    @Test
    public void getCurrentInterfaceListBeforeThreadPolls() {
        final NetworkInterface local = Mockito.mock(NetworkInterface.class);
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(singletonList(local));

        monitor = new NetworkMonitor(interfaceSupplier, MONITOR_INTERVAL);

        final List<NetworkInterface> initial = monitor.getCurrentInterfaceList();
        assertThat(initial).hasSize(1);
        assertThat(initial.get(0)).isEqualTo(local);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1);
    }

    @Test
    public void getCurrentInterfaceListCalledAgainBeforeThreadPollsDoesNotCallSupplierAgain() {
        final NetworkInterface local = Mockito.mock(NetworkInterface.class);
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(singletonList(local));

        monitor = new NetworkMonitor(interfaceSupplier, MONITOR_INTERVAL);

        monitor.getCurrentInterfaceList();
        waitNoInterruption(250);

        final List<NetworkInterface> secondCall = monitor.getCurrentInterfaceList();
        assertThat(secondCall).hasSize(1);
        assertThat(secondCall.get(0)).isEqualTo(local);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1); // hasn't been re-called
    }

    @Test
    public void interfaceSupplierNotCalledUntilThreadStartsIfNotExplicitlyCalledFirst() {
        final NetworkInterface local = Mockito.mock(NetworkInterface.class);
        final NetworkInterface ethernet = Mockito.mock(NetworkInterface.class);

        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(local), asList(local, ethernet));
        monitor = new NetworkMonitor(interfaceSupplier, MONITOR_INTERVAL);

        waitNoInterruption(250);

        monitor.start();

        waitNoInterruption(250);

        // should have called the interface supplier for the first time now
        final List<NetworkInterface> initial = monitor.getCurrentInterfaceList();
        assertThat(initial).hasSize(1);
        assertThat(initial.get(0)).isEqualTo(local);
        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1);

        waitNoInterruption(MONITOR_INTERVAL + 250);

        // should have called the interface supplier again now

        final List<NetworkInterface> secondCall = monitor.getCurrentInterfaceList();
        assertThat(secondCall).hasSize(2);
        assertThat(secondCall.get(0)).isEqualTo(local);
        assertThat(secondCall.get(1)).isEqualTo(ethernet);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(2);
    }

    @Test
    public void getCurrentInterfaceListReturnsSubsequentChangesOnceThreadStarted() {
        final NetworkInterface local = Mockito.mock(NetworkInterface.class);
        final NetworkInterface ethernet = Mockito.mock(NetworkInterface.class);

        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(local), asList(local, ethernet));
        monitor = new NetworkMonitor(interfaceSupplier, MONITOR_INTERVAL);

        final List<NetworkInterface> initial = monitor.getCurrentInterfaceList();
        assertThat(initial).hasSize(1);
        assertThat(initial.get(0)).isEqualTo(local);

        waitNoInterruption(250);

        monitor.start(); // calls interface supplier immediately

        waitNoInterruption( 250);

        // should have called the interface supplier again now

        final List<NetworkInterface> secondCall = monitor.getCurrentInterfaceList();
        assertThat(secondCall).hasSize(2);
        assertThat(secondCall.get(0)).isEqualTo(local);
        assertThat(secondCall.get(1)).isEqualTo(ethernet);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(2);
    }

    private static class CollectingNetworkChangeListener implements NetworkChangeListener {
        private final List<NetworkChangeEvent> events = Collections.synchronizedList(new ArrayList<>());

        public List<NetworkChangeEvent> getEvents() {
            return Collections.unmodifiableList(events);
        }

        @Override
        public void eventOccurred(final NetworkChangeEvent observableEvent) {
            events.add(observableEvent);
        }
    }

    @Test
    public void changesRequireTwoSuppliesFirstIsByGetCurrentInterfaceList() throws SocketException {
        final NetworkInterface local = Mockito.mock(NetworkInterface.class);
        final NetworkInterface ethernet = Mockito.mock(NetworkInterface.class);
        Mockito.when(ethernet.getName()).thenReturn(ETHERNET_INTERFACE_NAME);
        Mockito.when(ethernet.isUp()).thenReturn(true);

        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(local), asList(local, ethernet));
        monitor = new NetworkMonitor(interfaceSupplier, MONITOR_INTERVAL);
        final CollectingNetworkChangeListener listener = new CollectingNetworkChangeListener();
        monitor.addNetworkChangeListener(listener);

        // call the interface supplier for the first time
        final List<NetworkInterface> initial = monitor.getCurrentInterfaceList();

        waitNoInterruption(250);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1);
        assertThat(listener.getEvents()).isEmpty();

        monitor.start();

        waitNoInterruption(MONITOR_INTERVAL + 250);

        // should have called supplier twice now, and notified listener.
        final List<NetworkChangeEvent> events = listener.getEvents();
        assertThat(events).hasSize(1);
        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(2);

        final NetworkChangeEvent event = events.get(0);
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
    }

    @Test
    public void changesRequireTwoSuppliesFirstIsByFirstPoll() {

    }
}
