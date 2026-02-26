package uk.co.epicuri.serverapi.common.pojo;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Created by manish
 */
public class TimeUtil {

    public static final ZoneId UTC = ZoneId.of("UTC");
    private static final DateTimeFormatter SIMPLE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static boolean isBirthday(Long birthdayDate, int daysDifference) {
        if(birthdayDate == null || birthdayDate == 0) {
            return false;
        }

        return isBirthday(birthdayDate, daysDifference, LocalDateTime.now(Clock.systemUTC()));
    }

    public static boolean isBirthday(Long birthdayDate, int daysDifference, LocalDateTime comparisonDateTime) {
        Instant instant = Instant.ofEpochMilli(birthdayDate);
        LocalDateTime birthday = LocalDateTime.ofInstant(instant, UTC);
        LocalDateTime birthdayThisYear = LocalDateTime.of(comparisonDateTime.getYear(),birthday.getMonthValue(),birthday.getDayOfMonth(),0,0);
        LocalDateTime birthdayNextYear = LocalDateTime.of(comparisonDateTime.getYear()+1,birthday.getMonthValue(),birthday.getDayOfMonth(),0,0);
        LocalDateTime birthdayLastYear = LocalDateTime.of(comparisonDateTime.getYear()-1,birthday.getMonthValue(),birthday.getDayOfMonth(),0,0);
        return isBirthdayWithinBounds(daysDifference, comparisonDateTime, birthdayThisYear)
                || isBirthdayWithinBounds(daysDifference, comparisonDateTime, birthdayNextYear)
                || isBirthdayWithinBounds(daysDifference, comparisonDateTime, birthdayLastYear);
    }

    private static boolean isBirthdayWithinBounds(int daysDifference, LocalDateTime comparisonDateTime, LocalDateTime birthday) {
        return birthday.isAfter(comparisonDateTime.minusDays(daysDifference)) && birthday.isBefore(comparisonDateTime.plusDays(daysDifference));
    }

    public static ZonedDateTime getRestaurantTime(long utcMillis, String restaurantZoneId) {
        return getRestaurantTime(utcMillis, ZoneId.of(restaurantZoneId));
    }

    public static ZonedDateTime getRestaurantTime(long utcMillis, ZoneId restaurantZoneId) {
        Instant instant = Instant.ofEpochMilli(utcMillis);
        ZonedDateTime utcDT = ZonedDateTime.ofInstant(instant, UTC);
        return utcDT.withZoneSameInstant(restaurantZoneId);
    }

    public static ZonedDateTime getZonedDateTime(String yyyyMMdd, String hhmm, ZoneId zoneId) throws DateTimeParseException {
        LocalDateTime localDateTime = LocalDateTime.parse(yyyyMMdd + " " + hhmm, SIMPLE_DATETIME_FORMATTER);
        return localDateTime.atZone(zoneId);
    }

    public static ZonedDateTime getUTCDateTime(String yyyyMMdd, String hhmm, String restaurantZoneId) throws DateTimeParseException {
        ZonedDateTime zonedDateTime = getZonedDateTime(yyyyMMdd, hhmm, ZoneId.of(restaurantZoneId));
        return zonedDateTime.withZoneSameInstant(UTC);
    }

    public static ZonedDateTime getLastMidnight(String restaurantZoneId) {
        return getRestaurantTime(System.currentTimeMillis(), restaurantZoneId).truncatedTo(ChronoUnit.DAYS);
    }

    public static long toEpochMillisToday(String hhmm, String restaurantZoneId) {
        ZonedDateTime lastMidnight = getLastMidnight(restaurantZoneId);
        ZonedDateTime requestedLocalTime = lastMidnight.plusHours(TimeUtil.getHourComponent(hhmm)).plusMinutes(TimeUtil.getMinuteComponent(hhmm));
        return toEpochMillis(requestedLocalTime);
    }

    public static long toEpochMillis(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static int getHourComponent(String hhmm) {
        return Integer.parseInt(hhmm.substring(0,2));
    }

    public static int getMinuteComponent(String hhmm) {
        return Integer.parseInt(hhmm.substring(hhmm.length()-2));
    }
}
