package jackwtat.simplembta.utilities;

import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
    /**
     * Convert Date/Time from MBTA's string format to Java's Data object
     */

    public static Date parse(String date) {
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7)) - 1;
            int day = Integer.parseInt(date.substring(8, 10));
            int hour = Integer.parseInt(date.substring(11, 13));
            int minute = Integer.parseInt(date.substring(14, 16));
            int second = Integer.parseInt(date.substring(17, 19));

            return new GregorianCalendar(year, month, day, hour, minute, second).getTime();
        } catch (Exception e) {
            return null;
        }
    }
}
