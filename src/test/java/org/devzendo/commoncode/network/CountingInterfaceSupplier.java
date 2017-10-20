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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;

class CountingInterfaceSupplier implements NetworkInterfaceSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CountingInterfaceSupplier.class);

    private final List<List<NetworkInterface>> toBeReturned;
    private final CountDownLatch exhausted = new CountDownLatch(1);
    int count = 0;

    @SafeVarargs
    CountingInterfaceSupplier(final List<NetworkInterface>... toBeReturned) {
        this.toBeReturned = new ArrayList<>();
        this.toBeReturned.addAll(asList(toBeReturned));
        LOGGER.info("Supplier can be called " + toBeReturned.length + " time(s)");
    }

    @Override
    public synchronized Enumeration<NetworkInterface> get() {
        LOGGER.info("Supplier called");
        if (count == toBeReturned.size()) {
            throw new IllegalStateException("Interface supplier called more frequently than expected");
        }
        final List<NetworkInterface> networkInterfaces = toBeReturned.get(count++);
        if (count == toBeReturned.size()) {
            exhausted.countDown();
        }
        return enumeration(networkInterfaces);
    }

    public synchronized int numberOfTimesCalled() {
        return count;
    }

    public void waitForDataExhaustion() {
        try {
            exhausted.await();
        } catch (final InterruptedException e) {
            LOGGER.debug("Interrupted waiting for exhaustion");
        }
    }
}
