package jackwtat.simplembta.utilities;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
    /**
     * Convert Date/Time from MBTA's string format to Java's Data object
     */

    public static Date parseTime(String mbtaDate) {
        try {
            int year = Integer.parseInt(mbtaDate.substring(0, 4));
            int month = Integer.parseInt(mbtaDate.substring(5, 7)) - 1;
            int day = Integer.parseInt(mbtaDate.substring(8, 10));
            int hour = Integer.parseInt(mbtaDate.substring(11, 13));
            int minute = Integer.parseInt(mbtaDate.substring(14, 16));
            int second = Integer.parseInt(mbtaDate.substring(17, 19));

            return new GregorianCalendar(year, month, day, hour, minute, second).getTime();
        } catch (Exception e) {
            return null;
        }
    }

    // Gets the time zone offset from MBTA date, in minutes
    public static int parseTimeZoneOffset(String mbtaDate) {
        return Integer.parseInt(mbtaDate.substring(19, 22)) * 60 +
                Integer.parseInt(mbtaDate.substring(23, 25));
    }

    public static String getCurrentMbtaDate() {
        return getMbtaDate(new Date());
    }

    public static String getMbtaDate(Date date) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);

        if (calendar.get(Calendar.HOUR_OF_DAY) < 3) {
            calendar.add(Calendar.DATE, -1);
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    public static String getMbtaDate(int dayOffset) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(Calendar.HOUR_OF_DAY) < 3) {
            calendar.add(Calendar.DATE, dayOffset - 1);
        } else {
            calendar.add(Calendar.DATE, dayOffset);
        }

        return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
    }

    public static String getMbtaTime(int hourOffset) {
        String mbtaTime;
        int hour;
        int minute;

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        hour = calendar.get(Calendar.HOUR_OF_DAY) + hourOffset;
        minute = calendar.get(Calendar.MINUTE);

        if (hour < 3) {
            hour += 24;
        }

        if (minute < 10) {
            mbtaTime = hour + ":0" + minute;
        } else {
            mbtaTime = hour + ":" + minute;
        }

        return mbtaTime;
    }

    public static int getDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static Date getTimeZoneAdjustedDate(int timeZoneOffset) {
        Calendar time = Calendar.getInstance();
        time.add(Calendar.MILLISECOND, -time.getTimeZone().getOffset(time.getTimeInMillis()));
        time.add(Calendar.MINUTE, timeZoneOffset);

        return time.getTime();
    }
}
