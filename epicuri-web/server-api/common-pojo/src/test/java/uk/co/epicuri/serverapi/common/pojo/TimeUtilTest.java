package uk.co.epicuri.serverapi.common.pojo;

import org.junit.Test;

import java.time.*;

import static org.junit.Assert.*;

public class TimeUtilTest {
    long oneDay = 1000 * 60 * 60 * 24;

    @Test
    public void isBirthday() throws Exception {
        long june281979 = 299376000000L;

        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        assertTrue(TimeUtil.isBirthday(june281979, 1, LocalDateTime.of(now.getYear(), Month.JUNE, 28, 0, 0)));
        assertFalse(TimeUtil.isBirthday(june281979, 1, LocalDateTime.of(now.getYear(), Month.JUNE, 30, 0, 0)));
        assertTrue(TimeUtil.isBirthday(june281979, 2, LocalDateTime.of(now.getYear(), Month.JUNE, 27, 0, 0)));
        assertFalse(TimeUtil.isBirthday(june281979, 1, LocalDateTime.of(now.getYear(), Month.JUNE, 26, 0, 0)));
    }

    @Test
    public void getRestaurantTime1() throws Exception {
        long current = 1513274759635L; //Dec 14 2017 18:05 in London, Dec 15 2017 07:05 in Auckland
        ZonedDateTime zonedDateTime = TimeUtil.getRestaurantTime(current, "Pacific/Auckland");
        assertContrivedDateTime(zonedDateTime);
    }

    @Test
    public void getRestaurantTime2() throws Exception {
        long current = 1587909919509L; //26 Apr 2020 15:05:19 in London; 15:05:19 in UTC
        ZonedDateTime zonedDateTime = TimeUtil.getRestaurantTime(current, "Europe/London");
        assertEquals(15, zonedDateTime.getHour());
        assertEquals(5, zonedDateTime.getMinute());
        assertEquals(26, zonedDateTime.getDayOfMonth());
        assertEquals(Month.APRIL, zonedDateTime.getMonth());
        assertEquals(2020, zonedDateTime.getYear());
        assertEquals(current, TimeUtil.toEpochMillis(zonedDateTime));
    }

    private void assertContrivedDateTime(ZonedDateTime zonedDateTime) {
        assertEquals(7, zonedDateTime.getHour());
        assertEquals(5, zonedDateTime.getMinute());
        assertEquals(15, zonedDateTime.getDayOfMonth());
        assertEquals(Month.DECEMBER, zonedDateTime.getMonth());
        assertEquals(2017, zonedDateTime.getYear());
    }

    @Test
    public void getZonedDateTime() throws Exception {
        ZonedDateTime zonedDateTime = TimeUtil.getZonedDateTime("2017.12.15", "07:05", ZoneId.of("Pacific/Auckland"));
        assertContrivedDateTime(zonedDateTime);
    }

    @Test
    public void getUTCDateTime() throws Exception {
        ZonedDateTime zonedDateTime = TimeUtil.getUTCDateTime("2017.12.15", "07:05", "Pacific/Auckland");
        assertEquals(18, zonedDateTime.getHour());
        assertEquals(5, zonedDateTime.getMinute());
        assertEquals(14, zonedDateTime.getDayOfMonth());
        assertEquals(Month.DECEMBER, zonedDateTime.getMonth());
        assertEquals(2017, zonedDateTime.getYear());
    }

    @Test
    public void getHourComponent() throws Exception {
        assertEquals(0, TimeUtil.getHourComponent("00:12"));
        assertEquals(1, TimeUtil.getHourComponent("01:12"));
        assertEquals(10, TimeUtil.getHourComponent("10:12"));
    }

    @Test
    public void getMinuteComponent() throws Exception {
        assertEquals(12, TimeUtil.getMinuteComponent("00:12"));
        assertEquals(1, TimeUtil.getMinuteComponent("01:01"));
        assertEquals(0, TimeUtil.getMinuteComponent("10:00"));
    }

    @Test
    public void toEpochMillisToday() throws Exception {
        // this function uses current time, so use an anchor time to compare to
        long t0 = TimeUtil.toEpochMillisToday("00:00","Europe/London");
        long t1 = TimeUtil.toEpochMillisToday("01:00","Europe/London");
        long t2 = TimeUtil.toEpochMillisToday("02:00","Europe/London");
        assertEquals(1000*60*60, t2-t1);
        assertEquals(2*1000*60*60, t2-t0);
    }

    @Test
    public void getLastMidnight() {
        long current = 1587909919509L; //26 Apr 2020 15:05:19 in London; 15:05:19 in UTC
        ZonedDateTime zonedDateTime = TimeUtil.getLastMidnight("Europe/London", 0, current);
        assertEquals(1587855600000L, TimeUtil.toEpochMillis(zonedDateTime)); //Sat Apr 25 2020 23:00:00 UTC
    }
}