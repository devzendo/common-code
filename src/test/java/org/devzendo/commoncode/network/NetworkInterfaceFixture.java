package org.devzendo.commoncode.network;

import org.mockito.Mockito;

import java.net.NetworkInterface;
import java.net.SocketException;

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
public class NetworkInterfaceFixture {
    public static final String LOCAL_INTERFACE_NAME = "lo"; // for tests, use a linuxy name
    public static final String ETHERNET_INTERFACE_NAME = "eth0"; // for tests, use a linuxy name

    public static NetworkInterface ethernet(final boolean up) {
        final NetworkInterface ethernet = Mockito.mock(NetworkInterface.class);
        try {
            Mockito.when(ethernet.getName()).thenReturn(ETHERNET_INTERFACE_NAME);
            Mockito.when(ethernet.isUp()).thenReturn(up);
        } catch (final SocketException e) {
            throw new IllegalStateException(e);
        }
        return ethernet;
    }

    public static NetworkInterface ethernetUnknown() {
        final NetworkInterface ethernet = Mockito.mock(NetworkInterface.class);
        try {
            Mockito.when(ethernet.getName()).thenReturn(ETHERNET_INTERFACE_NAME);
            Mockito.when(ethernet.isUp()).thenThrow(new SocketException("Exception when detecting up/down"));
        } catch (final SocketException e) {
            // should not happen in test setup
        }
        return ethernet;
    }

    public static NetworkInterface local(final boolean up) {
        final NetworkInterface local = Mockito.mock(NetworkInterface.class);
        try {
            Mockito.when(local.getName()).thenReturn(LOCAL_INTERFACE_NAME);
            Mockito.when(local.isUp()).thenReturn(up);
        } catch (final SocketException e) {
            throw new IllegalStateException(e);
        }
        return local;
    }
}
