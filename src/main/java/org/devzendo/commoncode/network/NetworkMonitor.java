package org.devzendo.commoncode.network;

import org.devzendo.commoncode.patterns.observer.ObserverList;
import org.devzendo.commoncode.time.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

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
public class NetworkMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkMonitor.class);

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

    private final ObserverList<NetworkChangeEvent> changeListeners = new ObserverList<NetworkChangeEvent>();

    public NetworkMonitor(final NetworkInterfaceSupplier interfaceSupplier, final Sleeper sleeper, final long monitorInterval) {
        this.interfaceSupplier = interfaceSupplier;
        this.sleeper = sleeper;
        this.monitorInterval = monitorInterval;
        monitorThread.setDaemon(true); // TODO test for this
        monitorThread.setName("network-monitor"); // TODO test for this
    }

    public NetworkMonitor(final NetworkInterfaceSupplier interfaceSupplier, final long monitorInterval) {
        this(interfaceSupplier, new Sleeper(), monitorInterval);
    }

    /**
     * Obtain the current interface list. If the monitoring thread has not yet been triggered to get the list,
     * call the interface supplier for it. If the thread has recently obtained the list, return what it found.
     * The idea is that this doesn't necessarily call the interface supplier, so it couldn't get overloaded.
     * @return the current network interface list.
     */
    public List<NetworkInterface> getCurrentInterfaceList() {
        synchronized (interfacesLock) {
            // only update first time, or if we're running the poll thread
            if (currentNetworkInterfaceList == null || running) {
                currentNetworkInterfaceList = Collections.list(interfaceSupplier.get());

                // Log the initial interface states...
                if (firstCall) {
                    firstCall = false;
                    firstCallTime = sleeper.currentTimeMillis();
                    currentNetworkInterfaceList.forEach((NetworkInterface ni) -> {
                        LOGGER.info(ni.getName() + ": " + state(ni));
                    });
                }
            }
            return Collections.unmodifiableList(currentNetworkInterfaceList);
        }
    }

    public void start() {
        LOGGER.info("Starting network monitor");
        monitorThread.start();
    }

    public void stop() {
        LOGGER.info("Stopping network monitor");
        stopThread = true;
        monitorThread.interrupt();
    }

    // Mostly for testing...
    public boolean isRunning() {
        return running;
    }

    public void addNetworkChangeListener(final NetworkChangeListener listener) {
        changeListeners.addObserver(listener);
    }

    public void removeNetworkChangeListener(final NetworkChangeListener listener) {
        changeListeners.removeListener(listener);
    }

    private class NetworkMonitorRunnable implements Runnable {

        @Override
        public void run() {
            running = true;
            LOGGER.info("Network monitor started");

            List<NetworkInterface> lastNetworkInterfaceList = null;
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
                synchronized (interfacesLock) {
                    LOGGER.debug("Network monitor calling supplier");
                    final List<NetworkInterface> newNetworkInterfaceList = getCurrentInterfaceList();
                    // TODO this could emit events into listeners, needs to emit outside the sync block
                    determineDifferences(lastNetworkInterfaceList, newNetworkInterfaceList);
                    lastNetworkInterfaceList = newNetworkInterfaceList;
                }
                sleeper.sleep(monitorInterval);
            }

            LOGGER.info("Network monitor stopped");
            running = false;
        }
    }

    private void determineDifferences(final List<NetworkInterface> lastInterfaces, final List<NetworkInterface> newInterfaces) {
        final Map<String, NetworkInterface> lastInts = toMap(lastInterfaces);
        final Map<String, NetworkInterface> newInts = toMap(newInterfaces);
        // Added interfaces
        newInts.forEach((String name, NetworkInterface ni) -> {
            if (!lastInts.containsKey(name)) {
                emit(new NetworkChangeEvent(ni, name, NetworkChangeEvent.NetworkChangeType.INTERFACE_ADDED, state(ni)));
            }
        });
        // Removed interfaces
        lastInts.forEach((String name, NetworkInterface ni) -> {
            if (!newInts.containsKey(name)) {
                // it's gone, so we can't know its current state
                emit(new NetworkChangeEvent(ni, name, NetworkChangeEvent.NetworkChangeType.INTERFACE_REMOVED, NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE));
            }
        });
        // Changed interfaces - those who appear in both last and new, ie intersection, via Java 8's bassackwards way
        // of doing it...
        final Set<String> intersectionNames = new HashSet<>(lastInts.keySet());
        intersectionNames.retainAll(newInts.keySet());

        intersectionNames.forEach(name -> {
            final NetworkInterface lastNi = lastInts.get(name);
            final NetworkInterface newNi = newInts.get(name);
            final NetworkChangeEvent.NetworkStateType lastState = state(lastNi);
            final NetworkChangeEvent.NetworkStateType newState = state(newNi);
            LOGGER.debug("Interface '" + name + "' last state " + lastState + " new state " + newState);
            if (lastState != newState) {
                emit(new NetworkChangeEvent(newNi, name, NetworkChangeEvent.NetworkChangeType.INTERFACE_STATE_CHANGED, newState));
            }
        });
    }

    private void emit(final NetworkChangeEvent event) {
        LOGGER.info(event.toString());
        changeListeners.eventOccurred(event);
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
