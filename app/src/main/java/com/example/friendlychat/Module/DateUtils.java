package com.example.friendlychat.Module;

import android.content.Context;

import com.example.friendlychat.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    /*a very important method, any time the users sends a message the time fo method should
     * always be in UTC date, the reason for this is it will make it easy for two people chatting from
     * different places in the world with different timestamp, the time might likely vary*/

    public static long getNormalizedUtcDateForToday() {
        // the local date in milliseconds
        long utcNowMillis = System.currentTimeMillis();
        // the timezone will be used to get UTC date *_*
        TimeZone currentTimeZone = TimeZone.getDefault();
        // lastly this is what will be used adding this value to the local date would give the app the
        // time in UTC date.
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);
        // milliseconds in UTC date, but might contains some fractional (extra part) days.
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;
        /* This method simply converts milliseconds to days, disregarding any fractional days */
        /*long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);*/
        /*
         * Finally, we convert back to milliseconds. This time stamp represents today's date at
         * midnight in GMT time. We will need to account for local time zone offsets when
         * extracting this information from the database.
         */
        return timeSinceEpochLocalTimeMillis;
    }

    public static long getLocalDateFromUTC(long dateInUTC) {

        /* The timeZone object will provide us the current user's time zone offset */
        TimeZone timeZone = TimeZone.getDefault();
        long gmtOffset = timeZone.getOffset(dateInUTC);
        return dateInUTC + gmtOffset;
    }

    public static String getFriendlyDateString(Context context, long localDate, boolean showFullDate) {

        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(localDate);
        // no need to covert the date below because I want to compare between them locally
        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());
        if (daysFromEpochToProvidedDate == daysFromEpochToToday || showFullDate) {
            /*
             * If the date we're building the String for is today's date, the format
             * is "Today, June 24"
             */
            String dayName = getDayName(context, localDate); // can be today tomorrow or sat 17 june 2021
            String readableDate = getReadableDateString(context, localDate);
            if (daysFromEpochToProvidedDate - daysFromEpochToToday < 2) {
                /*
                 * Since there is no localized format that returns "Today" or "Tomorrow" in the API
                 * levels we have to support, we take the name of the day (from SimpleDateFormat)
                 * and use it to replace the date from DateUtils. This isn't guaranteed to work,
                 * but our testing so far has been conclusively positive.
                 *
                 * For information on a simpler API to use (on API > 18), please check out the
                 * documentation on DateFormat#getBestDateTimePattern(Locale, String)
                 * https://developer.android.com/reference/android/text/format/DateFormat.html#getBestDateTimePattern
                 */
                String localizedDayName = new SimpleDateFormat("EEEE").format(localDate);
                return readableDate.replace(localizedDayName, dayName);
            } else {
                return readableDate;
            }
        } else if (daysFromEpochToProvidedDate < daysFromEpochToToday + 7) {
            /* If the input date is less than a week in the future, just return the day name. */
            return getDayName(context, localDate);
        } else {
            int flags = android.text.format.DateUtils.FORMAT_SHOW_DATE
                    | android.text.format.DateUtils.FORMAT_NO_YEAR
                    | android.text.format.DateUtils.FORMAT_ABBREV_ALL
                    | android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;

            return android.text.format.DateUtils.formatDateTime(context, localDate, flags);
        }
    }
    private static String getReadableDateString(Context context, long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_NO_YEAR
                | android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY; // sat, 17 june.

        return android.text.format.DateUtils.formatDateTime(context, timeInMillis, flags);
    }


    private static String getDayName(Context context, long dateInMillis) {
        /*
         * If the date is today, return the localized version of "Today" instead of the actual
         * day name.
         */
        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(dateInMillis);
        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());

        /*since the messages will always be in the past, so its time will always be smaller than the current time*/
        int daysAfterToday = (int) (daysFromEpochToToday - daysFromEpochToProvidedDate );
        switch (daysAfterToday) {
            case 0:
                return context.getString(R.string.today);
            case 1:
                return context.getString(R.string.yesterday);
            default:
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                return dayFormat.format(dateInMillis);
        }
    }
    private static long elapsedDaysSinceEpoch(long utcDate) {
        return TimeUnit.MILLISECONDS.toDays(utcDate);
    }
}
