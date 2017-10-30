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

import org.devzendo.commoncode.collection.FilteringEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static java.util.Collections.emptyList;
import static java.util.Collections.enumeration;

/**
 * A NetworkInterfaceSupplier that returns non-loopback interfaces.
 */
public class DefaultNonLoopbackNetworkInterfaceSupplier implements NetworkInterfaceSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNonLoopbackNetworkInterfaceSupplier.class);
    private final NetworkInterfaceSupplier supplier;

    DefaultNonLoopbackNetworkInterfaceSupplier(final NetworkInterfaceSupplier supplier) {
        this.supplier = supplier;
    }

    public DefaultNonLoopbackNetworkInterfaceSupplier() {
        this(() -> {
            try {
                return NetworkInterface.getNetworkInterfaces();
            } catch (final SocketException e) {
                LOGGER.warn("Could not obtain network interfaces: " + e.getMessage());
                return enumeration(emptyList());
            }
        });
    }

    @Override
    public Enumeration<NetworkInterface> get() {
        return new FilteringEnumeration<NetworkInterface>(supplier.get()) {
            @Override
            public boolean test(final NetworkInterface networkInterface) {
                try {
                    return !networkInterface.isLoopback();
                } catch (final SocketException e) {
                    LOGGER.warn("Could not determine whether network interface '" + networkInterface.getName() + "' is loopback: " + e.getMessage());
                    return false;
                }
            }
        };
    }
}
