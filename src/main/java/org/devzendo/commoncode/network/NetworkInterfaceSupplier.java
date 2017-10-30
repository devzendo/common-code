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
import java.util.Enumeration;
import java.util.function.Supplier;

/**
 * A NetworkInterfaceSupplier is given to the NetworkMonitor to be called as necessary to obtain the current
 * view of any NetworkInterfaces that the monitor should check.
 *
 * The implementation of this should filter out any types of NetworkInterface (or states, addresses) that the
 * NetworkMonitor should not be checking - for instance, you may not want to monitor loopback interfaces, or those that
 * have a Class C non-routable address.
 */
public interface NetworkInterfaceSupplier extends Supplier<Enumeration<NetworkInterface>> {
    // marker interface; type alias
}
