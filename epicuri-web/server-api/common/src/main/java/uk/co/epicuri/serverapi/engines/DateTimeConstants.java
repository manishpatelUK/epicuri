package uk.co.epicuri.serverapi.engines;

import uk.co.epicuri.serverapi.common.pojo.TimeUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Created by manish.
 */
public class DateTimeConstants {
    public static final DateTimeFormatter REPORTING_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter REPORTING_DATETIME_SS_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter REPORTING_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static long convertToLong(ZoneId zoneId, String date, LocalTime addOn) {
        DateTimeFormatter reportingDateFormatter = determineFormatter(date);
        return convertToLong(reportingDateFormatter, zoneId, date, addOn);
    }

    public static long convertToLong(DateTimeFormatter dateFormatter, ZoneId zoneId, String date, LocalTime addOn) {
        if(dateFormatter == null) {
            return 0;
        }
        ZonedDateTime zonedDateTime;

        if(addOn == null) {
            if(containsTime(date)) {
                zonedDateTime = ZonedDateTime.of(LocalDateTime.parse(date,dateFormatter), zoneId);
            } else {
                zonedDateTime = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(date, dateFormatter), LocalTime.MIN),zoneId);
            }
        } else {
            zonedDateTime = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(date.substring(0,10), dateFormatter), addOn),zoneId);
        }

        zonedDateTime = zonedDateTime.withZoneSameInstant(TimeUtil.UTC);
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static String convertToDateTime(String zoneId, long utcTime) {
        return convertToDateTime(ZoneId.of(zoneId), utcTime);
    }

    public static String convertToDateTime(ZoneId zoneId, long utcTime) {
        return convertToDateTime(REPORTING_DATETIME_SS_FORMATTER, zoneId, utcTime);
    }

    public static String convertToDateTime(DateTimeFormatter formatter, ZoneId zoneId, long utcTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(utcTime), TimeUtil.UTC);
        zonedDateTime = zonedDateTime.withZoneSameInstant(zoneId);
        return formatter.format(zonedDateTime);
    }

    private static DateTimeFormatter determineFormatter(String date) {
        if(date == null) {
            return null;
        }

        if(date.length() == 10) {
            return REPORTING_DATE_FORMATTER;
        } else if(date.length() == 19) {
            return REPORTING_DATETIME_SS_FORMATTER;
        } else if(date.length() == 16) {
            return REPORTING_DATETIME_FORMATTER;
        }

        throw new IllegalArgumentException("Cannot format: " + date);
    }

    public static boolean containsTime(String dateTime) {
        return dateTime.length() == 16 || dateTime.length() == 19;
    }
}
