package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerCheckInView {
    @JsonProperty("Id")
    private String id = "-1";

    @JsonProperty("RestaurantId")
    private String restaurantId = "-1";

    @JsonProperty("ReservationId")
    private String reservationId;

    @JsonProperty("Time")
    private long time;

    @JsonProperty("SessionId")
    private String sessionId;

    private int numberOfPeople = 1;

    public CustomerCheckInView(){}
    public CustomerCheckInView(CheckIn checkIn) {
        id = checkIn.getId();
        restaurantId = checkIn.getRestaurantId();
        reservationId = checkIn.getBookingId();
        time = checkIn.getTime() / 1000;
        sessionId = checkIn.getSessionId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }
}
