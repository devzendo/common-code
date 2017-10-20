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

public interface NetworkMonitor {
    /**
     * Obtain the current interface list. If the monitoring thread has not yet been triggered to get the list,
     * call the interface supplier for it. If the thread has recently obtained the list, return what it found.
     * The idea is that this doesn't necessarily call the interface supplier, so it couldn't get overloaded.
     * @return the current network interface list.
     */
    List<NetworkInterface> getCurrentInterfaceList();

    void start();
    void stop();
    void addNetworkChangeListener(final NetworkChangeListener listener);
    void removeNetworkChangeListener(final NetworkChangeListener listener);

}
