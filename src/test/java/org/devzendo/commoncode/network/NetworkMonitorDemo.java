package org.devzendo.commoncode.network;

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.devzendo.commoncode.concurrency.ThreadUtils;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;

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
public class NetworkMonitorDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkMonitorDemo.class);

    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
        final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

        final Enumeration allAppenders = rootLogger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            final Appender appender = (Appender) allAppenders.nextElement();
            appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p %c{1}:%L - %m%n"));
        }
    }

    // test harness
    public static void main(final String[] args) {
        setupLogging();
        LOGGER.info("Starting Network Monitor Demo...");
        final DefaultNetworkMonitor monitor = new DefaultNetworkMonitor(new DefaultNonLoopbackNetworkInterfaceSupplier(), 2000L);
        monitor.addNetworkChangeListener(observableEvent -> {
            LOGGER.info("NetworkChangeEvent: " + observableEvent);
        });
        monitor.start();

        ThreadUtils.waitNoInterruption(60000);
    }
}
