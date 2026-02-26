package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.db.TableNames;

import java.time.DayOfWeek;
import java.util.*;

@Document(collection = TableNames.OPENING_HOURS)
public class OpeningHours extends IDAble {
    @Indexed
    private String restaurantId;

    private BookingType bookingType;
    private List<AbsoluteBlackout> absoluteBlackouts = new ArrayList<>();
    private List<AbsoluteBlackout> historicalBlackouts = new ArrayList<>();

    public static transient final List<HourSpan> DEFAULT_CLOSED_ALL_DAYS_HOURS = Collections.unmodifiableList(new ArrayList<>());
    public static List<HourSpan> getDefaultOpenAllDaysHours() {
        return Collections.singletonList(new HourSpan(0,0,24,0));
    }

    private Map<DayOfWeek, List<HourSpan>> hours = new HashMap<>();

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<AbsoluteBlackout> getAbsoluteBlackouts() {
        return absoluteBlackouts;
    }

    public void setAbsoluteBlackouts(List<AbsoluteBlackout> absoluteBlackouts) {
        this.absoluteBlackouts = absoluteBlackouts;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public Map<DayOfWeek, List<HourSpan>> getHours() {
        return hours;
    }

    public void setHours(Map<DayOfWeek, List<HourSpan>> hours) {
        this.hours = hours;
    }

    public List<AbsoluteBlackout> getHistoricalBlackouts() {
        return historicalBlackouts;
    }

    public void setHistoricalBlackouts(List<AbsoluteBlackout> historicalBlackouts) {
        this.historicalBlackouts = historicalBlackouts;
    }
}
