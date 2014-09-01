package com.dialog.common;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtility {

    /**
     * String Constant for a date timestamp e.g. 20070928
     */
    public static final DateTimeFormat DATE_FORMAT = new DateTimeFormat(
            "yyyyMMdd");

    /**
     * String Constant for a 24 hour time e.g. 231021
     */
    public static final DateTimeFormat TIME_FORMAT = new DateTimeFormat(
            "HHmmss");

    /**
     * String Constant for a short english date representation e.g. 27 Nov 09
     */
    public static final DateTimeFormat DATE_FORMAT_SHORT_ENGLISH = new DateTimeFormat(
            "ddMMMyy");
    
    /**
     * A type-safe container for storing Date/Time format strings. If needed, we
     * can add date time format string validation here.
     */
    public static final class DateTimeFormat {

        private String format;

        /**
         * DateTimeFormat objects should only be constructed internally from the
         * TimeUtility class
         */
        protected DateTimeFormat(String format) {
            this.format = format;
        }

        public String toString() {
            return format;
        }
    }

    /**
     * Converts a date string to a Calendar object
     * 
     * @param String
     * @return Calendar
     */
    public static Calendar dateToCalendar(String dateString, String formatString) {

        SimpleDateFormat formater = new SimpleDateFormat(formatString);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(formater.parse(dateString));
        } catch (ParseException e) {

        }
        return calendar;
    }

    /**
     * Determines the current date time and returns a calendar object
     * 
     * @return Calendar
     */
    public static Calendar getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date());
        return calendar;
    }
    
}
