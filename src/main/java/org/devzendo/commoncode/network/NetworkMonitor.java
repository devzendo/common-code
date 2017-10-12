package org.devzendo.commoncode.network;

import org.devzendo.commoncode.concurrency.ThreadUtils;
import org.devzendo.commoncode.patterns.observer.ObserverList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

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
public class NetworkMonitor {
    private static final Logger logger = LoggerFactory.getLogger(NetworkMonitor.class);

    private final long monitorInterval;
    private final Thread monitorThread = new Thread(new NetworkMonitorRunnable());
    private volatile boolean stopThread = false;
    private volatile boolean running = false;
    private boolean firstCall = true;
    private long firstCallTime = 0L;

    private final NetworkInterfaceSupplier interfaceSupplier;

    private final Object interfacesLock = new Object();
    private List<NetworkInterface> currentNetworkInterfaceList = null; // guarded by lock on interfacesLock


    private final ObserverList<NetworkChangeEvent> changeListeners = new ObserverList<NetworkChangeEvent>();

    public NetworkMonitor(final NetworkInterfaceSupplier interfaceSupplier, final long monitorInterval) {
        this.interfaceSupplier = interfaceSupplier;
        this.monitorInterval = monitorInterval;
        monitorThread.setDaemon(true);
        monitorThread.setName("network-monitor");
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
                    firstCallTime = System.currentTimeMillis();
                    currentNetworkInterfaceList.forEach((NetworkInterface ni) -> {
                        logger.info(ni.getName() + ": " + state(ni));
                    });
                }
            }
            return Collections.unmodifiableList(currentNetworkInterfaceList);
        }
    }

    public void start() {
        logger.info("Starting network monitor");
        monitorThread.start();
    }

    public void stop() {
        logger.info("Stopping network monitor");
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
            logger.info("Network monitor started");

            List<NetworkInterface> lastNetworkInterfaceList = null;
            synchronized (interfacesLock) {
                if (firstCall) {
                    logger.debug("Calling supplier for first time in monitor thread");
                    getCurrentInterfaceList();
                    waitNoInterruption(monitorInterval);
                } else {
                    final long initialWait = monitorInterval - (System.currentTimeMillis() - firstCallTime);
                    logger.debug("Waiting until monitor interval has expired before starting loop (for " + initialWait + "ms)");
                    waitNoInterruption(initialWait);
                }

                lastNetworkInterfaceList = Collections.unmodifiableList(currentNetworkInterfaceList);
            }

            while (!stopThread) {
                synchronized (interfacesLock) {
                    logger.debug("Network monitor calling supplier");
                    final List<NetworkInterface> newNetworkInterfaceList = getCurrentInterfaceList();
                    // TODO this could emit events into listeners, needs to emit outside the sync block
                    determineDifferences(lastNetworkInterfaceList, newNetworkInterfaceList);
                    lastNetworkInterfaceList = newNetworkInterfaceList;
                }
                waitNoInterruption(monitorInterval);
            }

            logger.info("Network monitor stopped");
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

    }

    private void emit(final NetworkChangeEvent event) {
        logger.info(event.toString());
        changeListeners.eventOccurred(event);
    }

    private static NetworkChangeEvent.NetworkStateType state(final NetworkInterface ni) {
        try {
            return ni.isUp() ? NetworkChangeEvent.NetworkStateType.INTERFACE_UP : NetworkChangeEvent.NetworkStateType.INTERFACE_DOWN;
        } catch (final SocketException e) {
            logger.warn("Could not determine up/down state of interface '" + ni.getName() + "': " + e.getMessage());
            return NetworkChangeEvent.NetworkStateType.INTERFACE_UNKNOWN_STATE;
        }
    }

    private Map<String, NetworkInterface> toMap(final List<NetworkInterface> interfaces) {
        final Map<String, NetworkInterface> map = new HashMap<>();
        interfaces.forEach((NetworkInterface ni) -> map.put(ni.getName(), ni));
        return map;
    }
}
