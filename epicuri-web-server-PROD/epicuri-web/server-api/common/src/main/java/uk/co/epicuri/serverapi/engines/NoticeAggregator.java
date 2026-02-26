package uk.co.epicuri.serverapi.engines;

import org.springframework.http.HttpStatus;
import uk.co.epicuri.serverapi.BadStateException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

public class NoticeAggregator {
    private StringBuilder notice;
    private Set<String> individualNotices = new TreeSet<>();
    private boolean throwExceptionOnTrigger = false;
    private Map<Integer,HttpStatus> statusMap = new HashMap<>();

    public static final String BOOKING_TIME_TOO_SOON_MESSAGE = "Booking time too soon.";
    public static final String BOOKING_TIME_IN_PAST_MESSAGE = "Booking time is in past.";
    public static final String BOOKING_IN_BLACKOUT_MESSAGE = "The venue is not accepting bookings for this date/time.";
    public static final String BOOKING_ALREADY_EXISTS_MESSAGE = "You already have accepted or pending reservations at or around this time.";
    public static final String BOOKING_TOO_LATE_TO_CANCEL_MESSAGE = "It is too close to the booking time to edit or cancel.";
    public static final String BOOKING_NOT_AVAILABLE_BLACK_MARKS_MESSAGE = "Warning: this customer has a history of no-shows or similar behaviour.";
    public static final String BOOKING_NOT_AVAILABLE_BLACK_MARKS_FRIENDLY_MESSAGE = "Cannot take bookings at this time.";
    public static final String BOOKING_IMMEDIATE_REJECT_MESSAGE = "Cannot take bookings for the requested date and time as the venue is either closed or cannot accept online bookings.";
    public static final String TAKEAWAY_WITHIN_LOCK_WINDOW_PARTIAL_MESSAGE  = "Due within %s minutes";

    public static final String CREDIT_CARD_NOT_PRESENT  = "Credit card not present.";

    public String getNotice() {
        if(notice == null) {
            return null;
        }
        return notice.toString().trim();
    }

    public void add(String message, HttpStatus status) {
        if(throwExceptionOnTrigger) {
            throw new BadStateException(status.getReasonPhrase() + ":" + message);
        }
        statusMap.put(add(message),status);
    }

    public int add(String s) {
        if(notice == null) {
            notice = new StringBuilder();
        }
        notice.append("- ").append(s).append("\n");
        individualNotices.add(s);
        return individualNotices.size()-1;
    }

    public static NoticeAggregator merge(NoticeAggregator currentOutput, NoticeAggregator newOutput) {
        if(currentOutput == null && newOutput == null) {
            return null;
        }
        else if(currentOutput == null) {
            return newOutput;
        }
        else if(newOutput == null) {
            return currentOutput;
        }

        String notice = newOutput.getNotice();
        if(notice != null) {
            currentOutput.add(notice);
        }

        return currentOutput;
    }

    public Set<String> getIndividualNotices() {
        return individualNotices;
    }

    public boolean isThrowExceptionOnTrigger() {
        return throwExceptionOnTrigger;
    }

    public void setThrowExceptionOnTrigger(boolean throwExceptionOnTrigger) {
        this.throwExceptionOnTrigger = throwExceptionOnTrigger;
    }
}
