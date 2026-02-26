package uk.co.epicuri.serverapi.engines;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayType;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.co.epicuri.serverapi.engines.NoticeAggregator.BOOKING_TIME_IN_PAST_MESSAGE;

public class FuseBox {

    private Set<BiFunction<NoticeAggregator, FuseBoxAggregationProxy, NoticeAggregator>> checks;

    public FuseBox() {
        checks = new HashSet<>();
    }

    public synchronized void finalise() {
        checks = Collections.unmodifiableSet(checks);
    }

    public void add(BiFunction<NoticeAggregator, FuseBoxAggregationProxy, NoticeAggregator> function) {
        checks.add(function);
    }

    public NoticeAggregator check(FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        return check(false, fuseBoxAggregationProxy);
    }

    public NoticeAggregator check(boolean throwExceptions, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        NoticeAggregator output = new NoticeAggregator();
        output.setThrowExceptionOnTrigger(throwExceptions);
        for(BiFunction<NoticeAggregator, FuseBoxAggregationProxy, NoticeAggregator> function : checks) {
            function.apply(output, fuseBoxAggregationProxy);
        }

        return output;
    }

    public static NoticeAggregator checkReservationMinTime(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int minDefault = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().getOrDefault(FixedDefaults.RESERVATION_MINIMUM_TIME, 30)).intValue();
        double minTime = (System.currentTimeMillis() + (1000*60*minDefault))/(double)1000;
        if(fuseBoxAggregationProxy.getBookingTimeSeconds() < minTime) {
            noticeAggregator.add(NoticeAggregator.BOOKING_TIME_TOO_SOON_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkTimeBeforeNow(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if((fuseBoxAggregationProxy.getBookingTimeSeconds()*1000) < System.currentTimeMillis()) {
            noticeAggregator.add(BOOKING_TIME_IN_PAST_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkReservationsBlackouts(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if(fuseBoxAggregationProxy.isClosedForReservations()) {
            noticeAggregator.add(NoticeAggregator.BOOKING_IN_BLACKOUT_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkBlackMarks(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if(fuseBoxAggregationProxy.exceedsBlackMarks()) {
            noticeAggregator.add(NoticeAggregator.BOOKING_NOT_AVAILABLE_BLACK_MARKS_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkBlackMarksAnonymous(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if(fuseBoxAggregationProxy.exceedsBlackMarks()) {
            noticeAggregator.add(NoticeAggregator.BOOKING_NOT_AVAILABLE_BLACK_MARKS_FRIENDLY_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkMaxCoversPerReservation(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int limit = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MAX_COVERS_PER_RESERVATION)).intValue();
        if(fuseBoxAggregationProxy.getRequestedNumberOfPeople() > limit) {
            noticeAggregator.add("Exceeds max number of covers (" + limit + ").", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkMaxActiveReservations(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int limit = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MAX_ACTIVE_RESERVATIONS)).intValue();
        if(fuseBoxAggregationProxy.getNumberOfActiveReservationsInTimeSlot() >= limit) {
            noticeAggregator.add("Restaurant floor is reaching its capacity for reservations during this time.", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkMaxActiveReservationsCovers(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int limit = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MAX_ACTIVE_RESERVATIONS_COVERS)).intValue();
        if((fuseBoxAggregationProxy.getNumberOfPeopleInTimeSlot()) > limit) {
            noticeAggregator.add("Restaurant floor is reaching its capacity for diners during this time.", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkReservationAlreadyExists(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        List<Booking> reservations = fuseBoxAggregationProxy.getCurrentBookingsAroundRequestForCustomer();
        reservations = reservations.stream().filter(r -> !r.isCancelled()).collect(Collectors.toList());
        if(reservations.size() > 0) {
            noticeAggregator.add(NoticeAggregator.BOOKING_ALREADY_EXISTS_MESSAGE, HttpStatus.NOT_FOUND);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkReservationLockWindow(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int limit = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.RESERVATION_LOCK_WINDOW)).intValue();
        long lockTime = (fuseBoxAggregationProxy.getBookingTimeSeconds()*1000) - (limit * 60000);
        if(System.currentTimeMillis() > lockTime) {
            noticeAggregator.add(NoticeAggregator.BOOKING_TOO_LATE_TO_CANCEL_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkTakeawayLockWindow(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int limit = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.TAKEAWAY_LOCK_WINDOW)).intValue();
        long lockTime = (fuseBoxAggregationProxy.getBookingTimeSeconds()*1000) - (limit * 60000);
        if(System.currentTimeMillis() > lockTime) {
            noticeAggregator.add(NoticeAggregator.TAKEAWAY_WITHIN_LOCK_WINDOW_PARTIAL_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkAddressExistence(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if(!fuseBoxAggregationProxy.checkIsAddressFormatValidAndExists()) {
            noticeAggregator.add("Address is invalid", HttpStatus.BAD_REQUEST);
        }

        return noticeAggregator;
    }

    public static NoticeAggregator checkDuplicateTakeaway(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        List<Booking> bookings = fuseBoxAggregationProxy.getCurrentBookingsAroundRequestForCustomer();
        Booking booking = fuseBoxAggregationProxy.getBooking();

        if(booking != null && StringUtils.isNotBlank(booking.getCustomerId())) {
            if(bookings.stream().anyMatch(b -> StringUtils.isNotBlank(b.getCustomerId()) && b.getCustomerId().equals(booking.getCustomerId()))) {
                noticeAggregator.add("A takeaway for this guest already exists around the same time", HttpStatus.BAD_REQUEST);
            }
        }

        return noticeAggregator;
    }

    public static NoticeAggregator checkTakeawayMinimumTime(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int takeawayMinimumTimeMinutes = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.TAKEAWAY_MINIMUM_TIME)).intValue();
        Booking booking = fuseBoxAggregationProxy.getBooking();

        if(booking != null) {
            long limitTime = (takeawayMinimumTimeMinutes * 60 * 1000) + System.currentTimeMillis();
            if(booking.getTargetTime() < limitTime) {
                noticeAggregator.add("Due within " + takeawayMinimumTimeMinutes + " minutes", HttpStatus.BAD_REQUEST);
            }
        }

        return noticeAggregator;
    }

    public static NoticeAggregator checkDeliveryAddressPresence(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        //check if takeaway is a delivery and there is no address
        Booking booking = fuseBoxAggregationProxy.getBooking();
        if(booking != null
                && booking.getBookingType() == BookingType.TAKEAWAY
                && booking.getTakeawayType() == TakeawayType.DELIVERY
                && booking.getDeliveryAddress() == null) {
            noticeAggregator.add("Address required for delivery", HttpStatus.BAD_REQUEST);
        }

        return noticeAggregator;
    }

    public static NoticeAggregator checkMaxDeliveryRadius(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        Booking booking = fuseBoxAggregationProxy.getBooking();
        if(booking != null && booking.getTakeawayType() != null && booking.getTakeawayType() != TakeawayType.DELIVERY) {
            return noticeAggregator;
        }

        double distance = fuseBoxAggregationProxy.calculateDistance();
        double limit = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MAX_DELIVERY_RADIUS)).doubleValue();
        if(distance > limit) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            noticeAggregator.add("Address outside delivery radius (Max " + decimalFormat.format(limit) + ")", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkMaxTakeawaysPerHour(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        int takeawaysPerHour = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MAX_TAKEAWAYS_PER_HOUR)).intValue();
        List<Booking> bookings = fuseBoxAggregationProxy.getCurrentBookingsAroundRequest();
        Booking booking = fuseBoxAggregationProxy.getBooking();
        if(booking != null && StringUtils.isNotBlank(booking.getId())) {
            bookings = bookings.stream().filter(b -> b.getId() != null && !b.getId().equals(booking.getId())).collect(Collectors.toList());
        }
        if(bookings.size() > takeawaysPerHour) {
            noticeAggregator.add("Reached capacity for takeaways during this time.", HttpStatus.BAD_REQUEST);
        }

        return noticeAggregator;
    }

    public static NoticeAggregator checkTakeawaysBlackouts(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if(fuseBoxAggregationProxy.isClosedForTakeaways()) {
            noticeAggregator.add("Restaurant not accepting takeaways for this date/time.", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkMaxOrder(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        double maxValue = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MAX_TAKEAWAY_VALUE)).doubleValue();
        int value = MoneyService.toPenniesRoundNearest(maxValue);
        if(fuseBoxAggregationProxy.getCalculatedSessionTotal() > value) {
            noticeAggregator.add("Takeaway value exceeds maximum allowable amount.", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkMinOrder(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        double minValue = ((Number)fuseBoxAggregationProxy.getRestaurantDefaults().get(FixedDefaults.MIN_TAKEAWAY_VALUE)).doubleValue();
        int value = MoneyService.toPenniesRoundNearest(minValue);
        if(fuseBoxAggregationProxy.getCalculatedSessionTotal() < value) {
            noticeAggregator.add("Takeaway value below minimum amount.", HttpStatus.BAD_REQUEST);
        }
        return noticeAggregator;
    }

    public static NoticeAggregator checkCCPresent(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        if(fuseBoxAggregationProxy.getCustomer() == null) {
            noticeAggregator.add(NoticeAggregator.CREDIT_CARD_NOT_PRESENT, HttpStatus.BAD_REQUEST);
        } else if(fuseBoxAggregationProxy.getCustomer().getCcData() == null) {
            noticeAggregator.add(NoticeAggregator.CREDIT_CARD_NOT_PRESENT, HttpStatus.BAD_REQUEST);
        }

        return noticeAggregator;
    }
}
