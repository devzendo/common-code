/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org <http://devzendo.org>
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

package org.devzendo.commoncode.concurrency;

import org.apache.log4j.Logger;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author matt
 */
public final class TestMinimumSleepResolution {
    private static final Logger LOGGER = Logger
            .getLogger(TestMinimumSleepResolution.class);

    /**
     * 
     */
    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
    }

    /**
     * 
     */
    @Test
    @Ignore
    public void testBenchmark() {
        for (long dur = 0; dur < 50L; dur++) {
            long min = 99999L;
            long max = 0L;
            long avg = 0L;
            final int iters = 10;
            for (int i = 0; i < iters; i++) {
                final long start = System.currentTimeMillis();
                ThreadUtils.waitNoInterruption(dur);
                final long elapsed = System.currentTimeMillis() - start;
                if (elapsed > max) {
                    max = elapsed;
                }
                if (elapsed < min) {
                    min = elapsed;
                }
                avg += elapsed;
            }
            final double davg = (double) avg / (double) iters;
            final long favg = (long) davg;
            LOGGER.info(String.format("dur %d (+%d) min %d avg %d max %d", dur,
                (favg - dur), min, favg, max));
        }
    }
}
