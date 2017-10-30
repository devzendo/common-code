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

import java.net.NetworkInterface;
import java.util.List;

/**
 * A NetworkMonitor continuously monitors the state of the system's NetworkInterfaces, by calling a
 * NetworkInterfaceSupplier. It starts a daemon thread to achieve this, polling at a user-defined frequency.
 *
 * Any additions, removals, changes in up/down state, or changes of address in the list of
 * interfaces are notified to connected NetworkChangeListeners, by sending appropriate NetworkChangeEvents.
 */
public interface NetworkMonitor {
    /**
     * Obtain the current interface list. If the monitoring thread has not yet been triggered to get the list,
     * call the interface supplier for it. If the thread has recently obtained the list, return what it found.
     * The idea is that this doesn't necessarily call the interface supplier, so it couldn't get overloaded.
     *
     * It is intended that this call is to be used to get the initial state of the NetworkInterfaces, and that
     * attached NetworkChangeListeners will be used to be notified of any subsequent changes.
     *
     * @return the current network interface list.
     */
    List<NetworkInterface> getCurrentInterfaceList();

    /**
     * Start the NetworkMonitor's monitor thread. Changes in interface state will be notified to any
     * NetworkChangeListeners.
     */
    void start();

    /**
     * Stop the NetworkMonitor's monitor thread. No more changes will be notified.
     */
    void stop();

    /**
     * Add a NetworkChangeListener to the NetworkMonitor. Once the monitor is started, and its thread is regularly
     * polling the state of the NetworkInterfaces, any connected NetworkChangeListeners will be notified of the
     * changes.
     * @param listener a NetworkChangeListener to attach to the NetworkMonitor.
     */
    void addNetworkChangeListener(final NetworkChangeListener listener);

    /**
     * Remove a formerly-attached NetworkChangeListener from the NetworkMonitor. This listener will no longer be
     * notified of changes in the NetworkInterface state.
     * @param listener a NetworkChangeListener to remove from the NetworkMonitor.
     */
    void removeNetworkChangeListener(final NetworkChangeListener listener);
}
