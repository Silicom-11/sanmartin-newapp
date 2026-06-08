package com.iepca.app.util;

import com.iepca.app.config.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date formatting utilities.
 */
public final class DateUtils {

    private DateUtils() {}

    private static final SimpleDateFormat API_FORMAT =
            new SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT =
            new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault());

    public static String formatForApi(Date date) {
        return API_FORMAT.format(date);
    }

    public static String formatForDisplay(String apiDate) {
        try {
            Date date = API_FORMAT.parse(apiDate);
            return date != null ? DISPLAY_FORMAT.format(date) : apiDate;
        } catch (ParseException e) {
            return apiDate;
        }
    }

    public static String formatTime(String isoDateTime) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat(Constants.DATETIME_FORMAT_API, Locale.getDefault());
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = iso.parse(isoDateTime);
            return date != null ? TIME_FORMAT.format(date) : "";
        } catch (ParseException e) {
            return "";
        }
    }

    public static String todayApiFormat() {
        return API_FORMAT.format(new Date());
    }

    public static String timeAgo(String isoDateTime) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat(Constants.DATETIME_FORMAT_API, Locale.getDefault());
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = iso.parse(isoDateTime);
            if (date == null) return "";

            long diff = System.currentTimeMillis() - date.getTime();
            long mins = diff / 60_000;
            if (mins < 1) return "Ahora";
            if (mins < 60) return mins + " min";
            long hours = mins / 60;
            if (hours < 24) return hours + "h";
            long days = hours / 24;
            if (days < 7) return days + "d";
            return DISPLAY_FORMAT.format(date);
        } catch (ParseException e) {
            return "";
        }
    }
}
