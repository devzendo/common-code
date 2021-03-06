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

package org.devzendo.commoncode.datetime;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;


/**
 * test Date normalisation
 *
 * @author matt
 *
 */
public final class TestSQLDateUtils {
    /**
     *
     */
    @Test
    public void testDateNormalisation() {
        final long millisWithHourMinuteSecondMillis = createSQLDateWithMillis();
        final Date startDate = new Date(millisWithHourMinuteSecondMillis);
        final Date normalisedDate = SQLDateUtils.normalise(startDate);
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(normalisedDate);
        Assert.assertEquals(0, calendar.get(Calendar.HOUR));
        Assert.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assert.assertEquals(0, calendar.get(Calendar.SECOND));
        Assert.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }


    /**
     * @return a time in milliseconds that does have hours, minutes, seconds, and millisecond
     * values that are positive
     */
    private long createSQLDateWithMillis() {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2009);
        calendar.set(Calendar.MONDAY, 2);
        calendar.set(Calendar.DAY_OF_MONTH, 22);
        calendar.set(Calendar.HOUR, 4);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 23);
        calendar.set(Calendar.MILLISECOND, 223);
        return calendar.getTimeInMillis();
    }
}
