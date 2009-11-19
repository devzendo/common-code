package uk.me.gumbley.commoncode.datetime;

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
