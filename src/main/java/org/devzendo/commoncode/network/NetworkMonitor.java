package org.devzendo.commoncode.network;

import org.devzendo.commoncode.concurrency.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

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
    private final Supplier<Enumeration<NetworkInterface>> interfaceSupplier;
    private final long monitorInterval;
    private List<NetworkInterface> currentNetworkInterfaceList = Collections.emptyList();
    private Thread monitorThread = new Thread(new NetworkMonitorRunnable());
    private volatile boolean stopThread = false;
    private volatile boolean running = false;

    public NetworkMonitor(final Supplier<Enumeration<NetworkInterface>> interfaceSupplier, final long monitorInterval) {
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
        synchronized (interfaceSupplier) {
            if (currentNetworkInterfaceList.isEmpty()) {
                currentNetworkInterfaceList = Collections.list(interfaceSupplier.get());
            }
        }
        return currentNetworkInterfaceList;
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

    private class NetworkMonitorRunnable implements Runnable {

        @Override
        public void run() {
            running = true;
            logger.info("Network monitor started");
            while (!stopThread) {
                synchronized (interfaceSupplier) {
                    currentNetworkInterfaceList = Collections.list(interfaceSupplier.get());
                }
                ThreadUtils.waitNoInterruption(monitorInterval);
            }
            logger.info("Network monitor stopped");
            running = false;
        }
    }
}
