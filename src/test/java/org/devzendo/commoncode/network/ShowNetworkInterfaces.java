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

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import static java.util.Collections.list;

public class ShowNetworkInterfaces {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowNetworkInterfaces.class);

    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
        final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

        final Enumeration allAppenders = rootLogger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            final Appender appender = (Appender) allAppenders.nextElement();
            appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p %c{1}:%L - %m%n"));
        }
    }

    // Display the network interfaces, their addresses and features
    public static void main(final String[] args) {
        setupLogging();
        try {
            list(NetworkInterface.getNetworkInterfaces()).forEach((NetworkInterface ni) -> {
                LOGGER.info("network interface " + ni.toString());
                try {
                    LOGGER.info(" " + (ni.isLoopback() ? " loopback" : " not loopback"));
                    LOGGER.info(" " + (ni.isUp() ? " up" : " down"));
                    LOGGER.info(" " + (ni.isPointToPoint() ? " point-to-point" : " not point-to-point"));
                    LOGGER.info(" " + (ni.isVirtual() ? " virtual" : " not virtual"));
                    final List<InetAddress> list = list(ni.getInetAddresses());
                    list.forEach((InetAddress ia) -> {
                        LOGGER.info("  address " + ia.toString());
                    });
                } catch (final SocketException e) {
                    e.printStackTrace();
                }
            });
        } catch (final SocketException e) {
            e.printStackTrace();
        }
    }

}
