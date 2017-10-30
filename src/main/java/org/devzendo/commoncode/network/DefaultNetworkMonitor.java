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

import org.devzendo.commoncode.patterns.observer.ObserverList;
import org.devzendo.commoncode.time.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import static java.util.Collections.list;

/**
 * The default implementation of NetworkMonitor that's given a NetworkInterfaceSupplier, and an interval between polls
 * of the supplier.
 */
public class DefaultNetworkMonitor implements NetworkMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNetworkMonitor.class);

    private final NetworkInterfaceSupplier interfaceSupplier;
    private final Sleeper sleeper;
    private final long monitorInterval;

    private final Thread monitorThread = new Thread(new NetworkMonitorRunnable());
    private volatile boolean stopThread = false;
    private volatile boolean running = false;
    private boolean firstCall = true;
    private long firstCallTime = 0L;

    private final Object interfacesLock = new Object();
    private List<NetworkInterface> currentNetworkInterfaceList = null; // guarded by lock on interfacesLock

    private final ObserverList<NetworkChangeEvent> changeListeners = new ObserverList<>();

    DefaultNetworkMonitor(final NetworkInterfaceSupplier interfaceSupplier, final Sleeper sleeper, final long monitorInterval) {
        this.interfaceSupplier = interfaceSupplier;
        this.sleeper = sleeper;
        this.monitorInterval = monitorInterval;
        monitorThread.setDaemon(true);
        monitorThread.setName("network-monitor");
    }

    public DefaultNetworkMonitor(final NetworkInterfaceSupplier interfaceSupplier, final long monitorInterval) {
        this(interfaceSupplier, new Sleeper(), monitorInterval);
    }

    @Override
    public List<NetworkInterface> getCurrentInterfaceList() {
        synchronized (interfacesLock) {
            // only update first time, or if we're running the poll thread
            if (currentNetworkInterfaceList == null || running) {
                currentNetworkInterfaceList = list(interfaceSupplier.get());

                // Log the initial interface states...
                if (firstCall) {
                    firstCall = false;
                    firstCallTime = sleeper.currentTimeMillis();
                    currentNetworkInterfaceList.forEach((NetworkInterface ni) ->
                            LOGGER.info(ni.getName() + ": " + state(ni)));
                }
            }
            return Collections.unmodifiableList(currentNetworkInterfaceList);
        }
    }

    @Override
    public void start() {
        LOGGER.info("Starting network monitor");
        monitorThread.start();
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping network monitor");
        stopThread = true;
        monitorThread.interrupt();
    }

    // Mostly for testing...
    public boolean isRunning() {
        return running;
    }

    @Override
    public void addNetworkChangeListener(final NetworkChangeListener listener) {
        changeListeners.addObserver(listener);
    }

    @Override
    public void removeNetworkChangeListener(final NetworkChangeListener listener) {
        changeListeners.removeListener(listener);
    }

    private class NetworkMonitorRunnable implements Runnable {

        @Override
        public void run() {
            running = true;
            LOGGER.info("Network monitor started");

            List<NetworkInterface> lastNetworkInterfaceList;
            synchronized (interfacesLock) {
                if (firstCall) {
                    LOGGER.debug("Calling supplier for first time in monitor thread");
                    getCurrentInterfaceList();
                    sleeper.sleep(monitorInterval);
                } else {
                    final long initialWait = monitorInterval - (sleeper.currentTimeMillis() - firstCallTime);
                    LOGGER.debug("Waiting until monitor interval has expired before starting loop (for " + initialWait + "ms)");
                    sleeper.sleep(initialWait);
                }

                lastNetworkInterfaceList = Collections.unmodifiableList(currentNetworkInterfaceList);
            }

            while (!stopThread) {
                final List<NetworkChangeEvent> events = new ArrayList<>();
                synchronized (interfacesLock) {
                    final List<NetworkInterface> newNetworkInterfaceList = getCurrentInterfaceList();
                    events.addAll(determineDifferences(lastNetworkInterfaceList, newNetworkInterfaceList));
                    lastNetworkInterfaceList = newNetworkInterfaceList;
                }

                events.forEach((NetworkChangeEvent nce) -> {
                    LOGGER.info(nce.toString());
                    changeListeners.eventOccurred(nce);
                });

                sleeper.sleep(monitorInterval);
            }

            LOGGER.info("Network monitor stopped");
            running = false;
        }
    }

    private List<NetworkChangeEvent> determineDifferences(final List<NetworkInterface> lastInterfaces, final List<NetworkInterface> newInterfaces) {
        final List<NetworkChangeEvent> events = new ArrayList<>();

        final Map<String, NetworkInterface> lastInts = toMap(lastInterfaces);
        final Map<String, NetworkInterface> newInts = toMap(newInterfaces);
        // Added interfaces
        newInts.forEach((String name, NetworkInterface ni) -> {
            if (!lastInts.containsKey(name)) {
                events.add(new NetworkChangeEvent(ni, name, NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED, state(ni)));
            }
        });
        // Removed interfaces
        lastInts.forEach((String name, NetworkInterface ni) -> {
            if (!newInts.containsKey(name)) {
                // it's gone, so we can't know its current state
                events.add(new NetworkChangeEvent(ni, name, NetworkChangeEvent.NetworkChangeType.INTERFACE_REMOVED, NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE));
            }
        });
        // Changed interfaces - those who appear in both last and new, ie intersection, via Java 8's bassackwards way
        // of doing it...
        final Set<String> intersectionNames = new HashSet<>(lastInts.keySet());
        intersectionNames.retainAll(newInts.keySet());

        intersectionNames.forEach(name -> {
            final NetworkInterface lastNi = lastInts.get(name);
            final NetworkChangeEvent.NetworkStateType lastState = state(lastNi);
            final List<InetAddress> lastAddresses = list(lastNi.getInetAddresses());

            final NetworkInterface newNi = newInts.get(name);
            final NetworkChangeEvent.NetworkStateType newState = state(newNi);
            final List<InetAddress> newAddresses = list(newNi.getInetAddresses());

            LOGGER.debug("Interface '" + name + "' last state " + lastState + "; addresses " + lastAddresses +
                    "/ new state " + newState + "; addresses " + newAddresses);
            if (lastState != newState || !lastNi.equals(newNi)) {
                events.add(new NetworkChangeEvent(newNi, name, NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED, newState));
            }
        });

        return events;
    }

    private static NetworkChangeEvent.NetworkStateType state(final NetworkInterface ni) {
        try {
            return ni.isUp() ? NetworkChangeEvent.NetworkStateType.INTERFACE_UP : NetworkChangeEvent.NetworkStateType.INTERFACE_DOWN;
        } catch (final SocketException e) {
            LOGGER.warn("Could not determine up/down state of interface '" + ni.getName() + "': " + e.getMessage());
            return NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE;
        }
    }

    private Map<String, NetworkInterface> toMap(final List<NetworkInterface> interfaces) {
        final Map<String, NetworkInterface> map = new HashMap<>();
        interfaces.forEach((NetworkInterface ni) -> map.put(ni.getName(), ni));
        return map;
    }
}
