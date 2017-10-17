/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.executor;

import org.apache.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for IteratorExecutor
 * 
 * @author matt
 *
 */
public final class TestIteratorExecutor {
    private static final Logger LOGGER = Logger.getLogger(IteratorExecutor.class);

    /**
     * 
     */
    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
    }

    /**
     * ls -l
     */
    @Test
    public void testLSL() {
        LOGGER.info("testLSL");
        final IteratorExecutor ie = new IteratorExecutor(new String[] {"ls", "-l" });
        while (ie.hasNext()) {
            final String line = (String) ie.next();
            LOGGER.info("Line: '" + line + "'");
        }
        LOGGER.info("Exit code is " + ie.getExitValue());
        LOGGER.info("IOException is " + ie.getIOException());
        assertThat(ie.getExitValue()).isEqualTo(0);
    }

    /**
     * mkisofs
     */
    @Test
    public void testMkISOFS() {
        LOGGER.info("testMkISOFS");
        final IteratorExecutor ie = new IteratorExecutor(new String[] {
                "mkisofs",
                "-r",
                "-gui",
                "-o",
                "/home/matt/Desktop/crap/testmkisofs.iso",
                "/home/matt/Desktop/crap/apache-tomcat-5.5.15"
        });
        ie.useStdErr();
        while (ie.hasNext()) {
            final String line = (String) ie.next();
            LOGGER.info("Line: '" + line + "'");
        }
        LOGGER.info("Exit code is " + ie.getExitValue());
        LOGGER.info("IOException is " + ie.getIOException());
        assertThat(ie.getExitValue()).isEqualTo(-1);
    }

    @Test
    public void singleLineOutput() {
        final ArrayList<Object> list = new ArrayList<>();

        final IteratorExecutor ie = new IteratorExecutor(new String[] {"echo", "hello" });
        ie.forEachRemaining(list::add);

        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isEqualTo("hello");

        assertThat(ie.getExitValue()).isEqualTo(0);
        assertThat(ie.getIOException()).isNull();
    }

}
