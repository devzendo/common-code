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

package org.devzendo.commoncode.time;

/**
 * A Toolkit for sleeping. Allows tests to speed time up.
 * @author matt
 *
 */
public final class Sleeper {
    private final int fasterBy;

    /**
     * Create a Sleeper where time is some number of times faster than
     * reality. Useful for unit tests.
     * @param timesFaster the number of times that time is faster by
     */
    public Sleeper(final int timesFaster) {
        if (timesFaster <= 0) {
            final String warning = "Multiplier cannot be zero or negative";
            throw new IllegalArgumentException(warning);
        }
        this.fasterBy = timesFaster;
    }

    /**
     * Create a Sleeper that sleeps in real time. This is the normal Sleeper
     * used in the real system.
     */
    public Sleeper() {
        this.fasterBy = 1;
    }
    
    /**
     * Sleep for a number of milliseconds, modified by the speedup of this
     * Sleeper.
     * @param millis a number of milliseconds that would be slept for if this
     * Sleeper was sleeping in real time.
     */
    public void sleep(final long millis) {
        try {
            Thread.sleep(millis / fasterBy);
        } catch (final InterruptedException e) {
            // do nothing...
        }
    }

    /**
     * Get the number of milliseconds since the start of the UNIX epoch, but
     * speeded up.
     * @return milliseconds
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis() * fasterBy;
    }
}
