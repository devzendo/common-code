package org.devzendo.commoncode.network;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

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
    public static final byte[] TETHERED_ADDRESS = {10, 8, 17, 90};
    public static final byte[] LAN_ADDRESS = {(byte) 192, (byte) 168, 7, 15};
    public static final byte[] LOCAL_ADDRESS = {127, 0, 0, 1};

    public static NetworkInterface withTetheredAddress(final NetworkInterface ni) {
        return mockAddress(ni, TETHERED_ADDRESS);
    }

    public static NetworkInterface withLANAddress(final NetworkInterface ni) {
        return mockAddress(ni, LAN_ADDRESS);
    }

    public static NetworkInterface localLAN(final boolean up) {
        return mockAddress(local(up), LOCAL_ADDRESS);
    }

    public static NetworkInterface ethernetLAN(final boolean up) {
        return mockAddress(ethernet(up), LAN_ADDRESS);
    }

    public static NetworkInterface ethernetLANUnknown() {
        return mockAddress(ethernetUnknown(), LAN_ADDRESS);
    }

    private static NetworkInterface mockAddress(final NetworkInterface ni, final byte[] address) {
        final Inet4Address i4 = Mockito.mock(Inet4Address.class);
        Mockito.when(i4.getAddress()).thenReturn(address);

        // O the horror, converting to an IPv4 address...
        final List<Integer> bytes = new ArrayList<>();
        for (int i=0; i< address.length; i++) {
            bytes.add((int) address[i] & 0x00ff);
        }
        final String addressString = bytes.stream().map(Object::toString).collect(joining("."));

        System.out.println("address string is '" + addressString + "'");
        Mockito.when(i4.toString()).thenReturn(addressString);
        // need a fresh enumeration on each call, so can't just thenReturn...
        Mockito.when(ni.getInetAddresses()).thenAnswer((Answer<Enumeration<InetAddress>>) invocation ->
                (enumeration(singletonList(i4))));
        return ni;
    }

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
