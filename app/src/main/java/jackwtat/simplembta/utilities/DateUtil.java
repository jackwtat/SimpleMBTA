package jackwtat.simplembta.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
    /**
     * Convert Date/Time from MBTA's string format to Java's Data object
     */

    public static Date parse(String mbtaDate) {
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

    public static String getCurrentMbtaDate() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(Calendar.HOUR_OF_DAY) <= 2) {
            calendar.add(Calendar.DATE, -1);
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
}
