package org.devzendo.commoncode.network;

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.assertj.core.api.Assertions;
import org.devzendo.commoncode.logging.CapturingAppender;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static java.util.Collections.list;
import static org.assertj.core.api.Assertions.assertThat;
import static org.devzendo.commoncode.network.NetworkInterfaceFixture.*;

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
public class TestDefaultNonLoopbackNetworkInterfaceSupplier {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDefaultNonLoopbackNetworkInterfaceSupplier.class);

    private final NetworkInterface localUp = local(true);
    private final NetworkInterface ethernetUp = ethernet(true);

    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
        final org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

        final Enumeration allAppenders = rootLogger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            final Appender appender = (Appender) allAppenders.nextElement();
            appender.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS} %t %-5p %c{1}:%L - %m%n"));
        }
    }

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private final class FakeSupplier implements NetworkInterfaceSupplier {
        @Override
        public Enumeration<NetworkInterface> get() {
            return enumeration(asList(localUp, ethernetUp));
        }
    }

    @Test
    public void filterNonLoopbackInterfaces() throws SocketException {
        Mockito.when(localUp.isLoopback()).thenReturn(true);
        Mockito.when(ethernetUp.isLoopback()).thenReturn(false);

        final DefaultNonLoopbackNetworkInterfaceSupplier nonLoopback = new DefaultNonLoopbackNetworkInterfaceSupplier(new FakeSupplier());
        final Enumeration<NetworkInterface> enumeration = nonLoopback.get();
        assertThat(enumeration.hasMoreElements()).isTrue();
        assertThat(enumeration.nextElement()).isEqualTo(ethernetUp);
        assertThat(enumeration.hasMoreElements()).isFalse();
    }
}
