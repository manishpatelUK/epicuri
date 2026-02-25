package uk.co.epicuri.serverapi.service.util;

import uk.co.epicuri.serverapi.common.pojo.model.restaurant.HourSpan;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Created by manish.
 */
public class OpeningHoursUtil {

    public static OpeningHours createDefaultOpeningHoursClosedAllDay(BookingType bookingType, String restaurantId) {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setBookingType(bookingType);
        putDayAndHours(openingHours, DayOfWeek.MONDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        putDayAndHours(openingHours, DayOfWeek.TUESDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        putDayAndHours(openingHours, DayOfWeek.WEDNESDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        putDayAndHours(openingHours, DayOfWeek.THURSDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        putDayAndHours(openingHours, DayOfWeek.FRIDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        putDayAndHours(openingHours, DayOfWeek.SATURDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        putDayAndHours(openingHours, DayOfWeek.SUNDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        openingHours.setRestaurantId(restaurantId);
        return openingHours;
    }

    public static OpeningHours createDefaultOpeningHoursOpenAllDay(BookingType bookingType, String restaurantId) {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setBookingType(bookingType);
        putDayAndHours(openingHours, DayOfWeek.MONDAY, OpeningHours.getDefaultOpenAllDaysHours());
        putDayAndHours(openingHours, DayOfWeek.TUESDAY, OpeningHours.getDefaultOpenAllDaysHours());
        putDayAndHours(openingHours, DayOfWeek.WEDNESDAY, OpeningHours.getDefaultOpenAllDaysHours());
        putDayAndHours(openingHours, DayOfWeek.THURSDAY, OpeningHours.getDefaultOpenAllDaysHours());
        putDayAndHours(openingHours, DayOfWeek.FRIDAY, OpeningHours.getDefaultOpenAllDaysHours());
        putDayAndHours(openingHours, DayOfWeek.SATURDAY, OpeningHours.getDefaultOpenAllDaysHours());
        putDayAndHours(openingHours, DayOfWeek.SUNDAY, OpeningHours.getDefaultOpenAllDaysHours());
        openingHours.setRestaurantId(restaurantId);
        return openingHours;
    }

    private static void putDayAndHours(OpeningHours openingHours, DayOfWeek day, List<HourSpan> list) {
        openingHours.getHours().put(day, list);
    }

    public static boolean isOverlapping(HourSpan span1, HourSpan span2) {
        return isTailOverlapping(span1, span2) || isInside(span1, span2);
    }

    public static int[] calculateMinutes(HourSpan span1, HourSpan span2) {
        int open1 = (span1.getHourOpen() * 60) + span1.getMinuteOpen();
        int close1 = (span1.getHourClose() * 60) + span1.getMinuteClose();
        int open2 = (span2.getHourOpen() * 60) + span2.getMinuteOpen();
        int close2 = (span2.getHourClose() * 60) + span2.getMinuteClose();

        return new int[]{open1,close1,open2,close2};
    }

    public static boolean isTailOverlapping(HourSpan span1, HourSpan span2) {
        int[] mins = calculateMinutes(span1,span2);
        int open1 = mins[0];
        int close1 = mins[1];
        int open2 = mins[2];
        int close2 = mins[3];

        return isTailOverlapping(open1, close1, open2, close2);
    }

    private static boolean isTailOverlapping(int open1, int close1, int open2, int close2) {
        return isTail1OverlappingHead2(open1, close1, open2, close2)
                || isHead1OverlappingTail2(open1, close1, open2, close2);
    }

    private static boolean isHead1OverlappingTail2(int open1, int close1, int open2, int close2) {
        return close2 > open1 && open1 > open2 && close1 > close2;
    }

    private static boolean isTail1OverlappingHead2(int open1, int close1, int open2, int close2) {
        return close1 > open2 && open2 > open1 && close2 > close1;
    }

    public static boolean isInside(HourSpan span1, HourSpan span2) {
        int[] mins = calculateMinutes(span1,span2);
        int open1 = mins[0];
        int close1 = mins[1];
        int open2 = mins[2];
        int close2 = mins[3];

        return isInside(open1, close1, open2, close2) || isInside(open2, close2, open1, close1);
    }

    public static void joinAdjacent(HourSpan span1, HourSpan span2) {
        int[] mins = calculateMinutes(span1,span2);
        int open1 = mins[0];
        int close1 = mins[1];
        int open2 = mins[2];
        int close2 = mins[3];

        if(isTail1OverlappingHead2(open1, close1, open2, close2)) {
            span1.setHourClose(span2.getHourOpen());
            span1.setMinuteClose(span2.getMinuteOpen());
        } else if(isHead1OverlappingTail2(open1, close1, open2, close2)) {
            span2.setHourClose(span1.getHourOpen());
            span2.setMinuteClose(span1.getMinuteOpen());
        } else if(isInside(span1,span2)) {
            span1.setHourOpen(span2.getHourOpen());
            span1.setMinuteOpen(span2.getMinuteOpen());
            span2.setHourOpen(span1.getHourClose());
            span2.setMinuteOpen(span1.getMinuteOpen());
        } else if(isInside(span2,span1)) {
            span2.setHourOpen(span1.getHourOpen());
            span2.setMinuteOpen(span1.getMinuteOpen());
            span1.setHourOpen(span2.getHourClose());
            span1.setMinuteOpen(span2.getMinuteOpen());
        }
    }

    private static boolean isInside(int open1, int close1, int open2, int close2) {
        return (open1 > open2) && (close1 < close2);
    }
}
