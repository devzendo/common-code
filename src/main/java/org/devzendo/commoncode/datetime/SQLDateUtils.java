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

/**
 * Toolkit for SQL Date manipulation.
 *
 * @author matt
 *
 */
public final class SQLDateUtils {

    /**
     * Given a Date, possibly having Hour, Minute, Second, Millisecond values, return a
     * new Date with the same Year, Month, Day, with the time parts set to zero.
     * @param initialDate the date with time components
     * @return the date without time components
     */
    public static Date normalise(final Date initialDate) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(initialDate);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        return new Date(calendar.getTimeInMillis());
    }
}
