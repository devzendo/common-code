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

package org.devzendo.commoncode.time;

import org.devzendo.commoncode.concurrency.ThreadUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the Sleeper
 * @author matt
 *
 */
public final class TestSleeper {
    
    @Test(expected = IllegalArgumentException.class)
    public void negativeSleeperDisallowed() {
        new Sleeper(-2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSleeperDisallowed() {
        new Sleeper(0);
    }
    
    @Test(timeout = 8000)
    public void timeSpeedsUp() {
        final Sleeper fourTimesFaster = new Sleeper(4);
        final long dur = sleepFor(fourTimesFaster, 1000);
        Assert.assertTrue(dur >= 250 && dur <= 500); // upper bound dodgy?
    }
    
    @Test(timeout = 4000)
    public void realTimeIsNormal() {
        final Sleeper normalTime = new Sleeper();
        final long dur = sleepFor(normalTime, 250);
        Assert.assertTrue(dur >= 250 && dur <= 500); // upper bound dodgy?
    }

    @Test(timeout = 4000)
    public void getTimeMillisIsNormal() {
        final Sleeper normalTime = new Sleeper();
        final long realNow = System.currentTimeMillis();
        final long sleeperNow = normalTime.currentTimeMillis();
        // can't be more than 100ms different.
        Assert.assertThat(Math.abs(realNow - sleeperNow), Matchers.lessThan(100L));

        // Now really sleep.
        ThreadUtils.waitNoInterruption(2000);

        // Check again; they should be in sync by < 100ms
        final long realLater = System.currentTimeMillis();
        final long sleeperLater = normalTime.currentTimeMillis();
        Assert.assertThat(Math.abs(realLater - sleeperLater), Matchers.lessThan(100L));
    }

    @Test(timeout = 4000)
    public void getTimeMillisCanBeSpedUp() {
        final Sleeper normalTime = new Sleeper(2);
        final long sleeperNow = normalTime.currentTimeMillis();

        // Now really sleep.
        ThreadUtils.waitNoInterruption(2000);

        // Check again; should be 4000 ms different
        final long sleeperLater = normalTime.currentTimeMillis();
        final long spedUpDuration = Math.abs(sleeperNow - sleeperLater);
        // 4000 ms +/- 100ms
        Assert.assertThat(Math.abs(spedUpDuration - 4000), Matchers.lessThan(100L));
    }


    private long sleepFor(final Sleeper sleeper, final long duration) {
        final long start = System.currentTimeMillis();
        sleeper.sleep(duration);
        return System.currentTimeMillis() - start;
    }
}
