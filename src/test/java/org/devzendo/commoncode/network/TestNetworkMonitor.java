package org.devzendo.commoncode.network;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.devzendo.commoncode.logging.CapturingAppender;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.devzendo.commoncode.time.Sleeper;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.devzendo.commoncode.logging.IsLoggingEvent.loggingEvent;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.*;
import static org.hamcrest.Matchers.hasItems;

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
    private static final CapturingAppender CAPTURING_APPENDER = new CapturingAppender();
    private static final Sleeper SLEEPER = new Sleeper(20);

    private final NetworkInterface localUp = localLAN(true);
    private final NetworkInterface localDown = localLAN(false);
    private final NetworkInterface ethernetUp = ethernetLAN(true);
    private final NetworkInterface ethernetDown = ethernetLAN(false);
    private final NetworkInterface ethernetUnknown = ethernetLANUnknown();
    private final NetworkInterface tetheredEthernet = withTetheredAddress(ethernet(true));

    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
        // Want to see detailed logs, for diagnostics including milliseconds
        final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        rootLogger.addAppender(CAPTURING_APPENDER);

        final Enumeration allAppenders = rootLogger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            final Appender appender = (Appender) allAppenders.nextElement();
            appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p %c{1}:%L - %m%n"));
        }
    }

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private NetworkMonitor monitor;
    private static final long MONITOR_INTERVAL = 2000L;

    @After
    public void stopMonitor() {
        if (monitor != null) {
            monitor.stop();
        }
    }

    private static class EmptyInterfaceSupplier implements NetworkInterfaceSupplier {
        @Override
        public Enumeration<NetworkInterface> get() {
            return enumeration(emptyList());
        }
    }

    @Test
    public void monitorNotRunningUntilStartedThenStopsWhenStopped() {
        monitor = new NetworkMonitor(new EmptyInterfaceSupplier(), SLEEPER, MONITOR_INTERVAL);
        assertThat(monitor.isRunning()).isFalse();
        monitor.start();
        SLEEPER.sleep(250);
        assertThat(monitor.isRunning()).isTrue();
        SLEEPER.sleep(250);
        monitor.stop();
        SLEEPER.sleep(250);
        assertThat(monitor.isRunning()).isFalse();
    }

    @Test
    public void getCurrentInterfaceListBeforeThreadStartedCallsSupplier() throws SocketException {
        final NetworkInterface local = local(true);
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(singletonList(local));

        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        final List<NetworkInterface> initial = monitor.getCurrentInterfaceList();
        assertThat(initial).hasSize(1);
        assertThat(initial.get(0)).isEqualTo(local);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1);
    }

    @Test
    public void getCurrentInterfaceListCalledAgainBeforeThreadStartedDoesNotCallSupplierAgain() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(singletonList(localUp));

        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        monitor.getCurrentInterfaceList();
        SLEEPER.sleep(250);

        final List<NetworkInterface> secondCall = monitor.getCurrentInterfaceList();
        assertThat(secondCall).hasSize(1);
        assertThat(secondCall.get(0)).isEqualTo(localUp);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1); // hasn't been re-called
    }

    @Test(timeout = 8000)
    public void interfaceSupplierNotCalledUntilThreadStartsIfNotExplicitlyCalledFirst() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(localUp), asList(localUp, ethernetUp));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        monitor.start();

        interfaceSupplier.waitForDataExhaustion();

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(2);
    }

    @Test(timeout = 8000)
    public void getCurrentInterfaceListReturnsSubsequentChangesOnceThreadStarted() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(localUp), asList(localUp, ethernetUp));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        final List<NetworkInterface> initial = monitor.getCurrentInterfaceList();
        assertThat(initial).hasSize(1);
        assertThat(initial.get(0)).isEqualTo(localUp);

        SLEEPER.sleep(250);

        monitor.start(); // calls interface supplier immediately, but waits for the virtualToReal before polling again

        interfaceSupplier.waitForDataExhaustion();

        // should have called the interface supplier again now
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

    @Test(timeout = 8000)
    public void changesRequireTwoSuppliesFirstIsByGetCurrentInterfaceList() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(localUp), asList(localUp, ethernetUp));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);
        final CollectingNetworkChangeListener listener = new CollectingNetworkChangeListener();
        monitor.addNetworkChangeListener(listener);

        // call the interface supplier for the first time
        monitor.getCurrentInterfaceList();

        SLEEPER.sleep(250);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(1);
        assertThat(listener.getEvents()).isEmpty();

        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        // should have called supplier twice now, and notified listener.
        final List<NetworkChangeEvent> events = listener.getEvents();
        assertThat(events).hasSize(1);
        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(2);

        final NetworkChangeEvent event = events.get(0);
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
    }

    @Test(timeout = 8000)
    public void changesRequireTwoSuppliesFirstIsByFirstPoll() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(localUp), asList(localUp, ethernetUp));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);
        final CollectingNetworkChangeListener listener = new CollectingNetworkChangeListener();
        monitor.addNetworkChangeListener(listener);

        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(0);
        assertThat(listener.getEvents()).isEmpty();

        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        // should have called supplier twice now, and notified listener.
        final List<NetworkChangeEvent> events = listener.getEvents();
        assertThat(events).hasSize(1);
        assertThat(interfaceSupplier.numberOfTimesCalled()).isEqualTo(2);

        final NetworkChangeEvent event = events.get(0);
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
    }

    @Test
    public void logsInitialStatesViaGetCurrentInterfaces() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                asList(localUp, ethernetDown));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        monitor.getCurrentInterfaceList();

        SLEEPER.sleep(250);

        final List<LoggingEvent> events = CAPTURING_APPENDER.getEvents();
        MatcherAssert.assertThat(events, hasItems(
                loggingEvent(Level.INFO, "lo: INTERFACE_UP"),
                loggingEvent(Level.INFO, "eth0: INTERFACE_DOWN")));
    }

    @Test(timeout = 8000)
    public void logsInitialStatesOnFirstPoll() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(asList(localDown, ethernetUp));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);
        final CollectingNetworkChangeListener listener = new CollectingNetworkChangeListener();
        monitor.addNetworkChangeListener(listener);

        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        final List<LoggingEvent> events = CAPTURING_APPENDER.getEvents();
        MatcherAssert.assertThat(events, hasItems(
                loggingEvent(Level.INFO, "lo: INTERFACE_DOWN"),
                loggingEvent(Level.INFO, "eth0: INTERFACE_UP")));
    }

    @Test(timeout = 16000)
    public void supplierCalledWithinFrequencyIfGetCurrentInterfaceListCalledFirst() throws SocketException {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);

        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);
        monitor.getCurrentInterfaceList();
        monitor.start();

        SLEEPER.sleep(MONITOR_INTERVAL * 5);

        interfaceSupplier.validateIntervals(); // flaky last interval sometimes 1999
    }

    @Test(timeout = 16000)
    public void supplierCalledWithinFrequencyIfGetCurrentInterfaceListNotCalledFirst() {
        final PollIntervalMeasuringInterfaceSupplier interfaceSupplier = new PollIntervalMeasuringInterfaceSupplier(SLEEPER, MONITOR_INTERVAL);

        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);
        monitor.start();

        SLEEPER.sleep(MONITOR_INTERVAL * 5);

        interfaceSupplier.validateIntervals(); // flaky last interval sometimes 1999
    }

    @Test(timeout = 8000)
    public void logsFirstChangeIfGetCurrentInterfaceCalledFirst() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(ethernetUp), singletonList(ethernetDown));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        // call the interface supplier for the first time
        monitor.getCurrentInterfaceList();
        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        final List<LoggingEvent> events = CAPTURING_APPENDER.getEvents();
        MatcherAssert.assertThat(events, hasItems(
                loggingEvent(Level.INFO, "eth0: INTERFACE_STATE_CHANGED / INTERFACE_DOWN")));
    }

    @Test(timeout = 8000)
    public void logsFirstChangeIfGetCurrentInterfaceIsNotCalled() throws SocketException {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                singletonList(ethernetUp), singletonList(ethernetDown));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);

        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        final List<LoggingEvent> events = CAPTURING_APPENDER.getEvents();
        MatcherAssert.assertThat(events, hasItems(
                loggingEvent(Level.INFO, "eth0: INTERFACE_STATE_CHANGED / INTERFACE_DOWN")));
    }

    @SafeVarargs
    private final NetworkChangeEvent runChangeDetectionTest(final List<NetworkInterface> ... supplies) {
        final List<NetworkChangeEvent> events = runChangeDetectionTests(supplies);
        assertThat(events).hasSize(1);
        return events.get(0);
    }

    @SafeVarargs
    private final List<NetworkChangeEvent> runChangeDetectionTests(final List<NetworkInterface> ... supplies) {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(supplies);
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);
        final CollectingNetworkChangeListener listener = new CollectingNetworkChangeListener();
        monitor.addNetworkChangeListener(listener);
        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        return listener.getEvents();
    }

    @Test(timeout = 8000)
    public void interfaceAddedUpDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(singletonList(localUp), asList(localUp, ethernetUp));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
    }

    @Test(timeout = 8000)
    public void interfaceAddedDownDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(singletonList(localUp), asList(localUp, ethernetDown));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_DOWN);
    }

    @Test(timeout = 8000)
    public void interfaceAddedUnknownDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(singletonList(localUp), asList(localUp, ethernetUnknown));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE);
    }

    @Test(timeout = 8000)
    public void interfaceRemovedDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(asList(localUp, ethernetUp), singletonList(localUp));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_REMOVED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE);
    }

    @Test(timeout = 8000)
    public void interfaceChangedUpDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(asList(localUp, ethernetDown), asList(localUp, ethernetUp));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
    }

    @Test(timeout = 8000)
    public void interfaceChangedDownDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(asList(localUp, ethernetUp), asList(localUp, ethernetDown));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_DOWN);
    }

    @Test(timeout = 8000)
    public void interfaceChangedUnknownDetected() throws SocketException {
        final NetworkChangeEvent event = runChangeDetectionTest(asList(localUp, ethernetDown), asList(localUp, ethernetUnknown));
        assertThat(event.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED);
        assertThat(event.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(event.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE);
    }

    @Test(timeout = 8000)
    public void interfaceChangedAddressDetected() throws SocketException {
        final List<NetworkChangeEvent> events = runChangeDetectionTests(singletonList(ethernetUp),
                singletonList(tetheredEthernet), singletonList(ethernetUp));

        assertThat(events).hasSize(2);

        final NetworkChangeEvent tethered = events.get(0);
        assertThat(tethered.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED);
        assertThat(tethered.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(tethered.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
        assertThat(tethered.getNetworkInterface().getInetAddresses().nextElement().getAddress()).
                isEqualTo(NetworkInterfaceFixture.TETHERED_ADDRESS);

        final NetworkChangeEvent lan = events.get(1);
        assertThat(lan.getChangeType()).isEqualTo(NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED);
        assertThat(lan.getNetworkInterfaceName()).isEqualTo(NetworkInterfaceFixture.ETHERNET_INTERFACE_NAME);
        assertThat(lan.getStateType()).isEqualTo(NetworkChangeEvent.NetworkStateType.INTERFACE_UP);
        assertThat(lan.getNetworkInterface().getInetAddresses().nextElement().getAddress()).
                isEqualTo(NetworkInterfaceFixture.LAN_ADDRESS);
    }

    @Test
    public void listenersCanBeRemoved() {
        final CountingInterfaceSupplier interfaceSupplier = new CountingInterfaceSupplier(
                asList(localDown, ethernetUp),
                asList(localUp, ethernetUp),
                asList(localDown, ethernetUp));
        monitor = new NetworkMonitor(interfaceSupplier, SLEEPER, MONITOR_INTERVAL);


        final AtomicInteger callCount = new AtomicInteger(0);
        final NetworkChangeListener oneShotListener = new NetworkChangeListener() {
            @Override
            public void eventOccurred(final NetworkChangeEvent observableEvent) {
                callCount.incrementAndGet();
                monitor.removeNetworkChangeListener(this);
            }
        };

        monitor.addNetworkChangeListener(oneShotListener);

        monitor.start();

        interfaceSupplier.waitForDataExhaustion();
        SLEEPER.sleep(250);

        assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    public void monitorThreadProperties() {
        monitor = new NetworkMonitor(new EmptyInterfaceSupplier(), SLEEPER, MONITOR_INTERVAL);
        monitor.start();
        SLEEPER.sleep(250);

        final Set<Thread> threads = Thread.getAllStackTraces().keySet();
        assertThat(threads.stream().anyMatch(thread ->
                thread.isDaemon() && thread.getName().equals("network-monitor"))).isTrue();
    }


    // TODO test the first delayed call in polling after getCurrentInterfaceList has been called.
}
