package org.devzendo.commoncode.concurrency;

import org.apache.log4j.Logger;
import org.devzendo.commoncode.CCTestCase;
import org.devzendo.commoncode.concurrency.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author matt
 */
public final class TestMinimumSleepResolution extends CCTestCase {
    private static final Logger LOGGER = Logger
            .getLogger(TestMinimumSleepResolution.class);

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
