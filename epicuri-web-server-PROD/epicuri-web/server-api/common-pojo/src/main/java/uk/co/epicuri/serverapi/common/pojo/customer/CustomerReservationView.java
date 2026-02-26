package uk.co.epicuri.serverapi.common.pojo.customer;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.booking.BookingRequest;
import uk.co.epicuri.serverapi.common.pojo.booking.TimeSlots;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerReservationView implements Comparable<CustomerReservationView> {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("Telephone")
    private String telephone;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("ReservationTime")
    private Long reservationTime; //unixtime in seconds, in UTC

    @JsonProperty("NumberOfPeople")
    private int numberOfPeople;

    @JsonProperty("RestaurantId")
    private String restaurantId;

    @JsonProperty("Restaurant")
    private CustomerRestaurantView restaurant;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("ArrivedTime")
    private Long arrivedTime;

    @JsonProperty("Accepted")
    private boolean accepted;

    @JsonProperty("Rejected")
    private boolean rejected;

    @JsonProperty("RejectionNotice")
    private String rejectionNotice;

    @JsonProperty("Deleted")
    private boolean deleted;

    @JsonProperty("InstantiatedFromId")
    private int instantiatedFromId;

    public CustomerReservationView(){}

    public CustomerReservationView(Booking reservation, Restaurant restaurant) {
        id = reservation.getId();
        notes = reservation.getNotes();
        telephone = reservation.getTelephone();
        email = reservation.getEmail();
        reservationTime = reservation.getTargetTime() / 1000;
        numberOfPeople = reservation.getNumberOfPeople();
        restaurantId = reservation.getRestaurantId();
        this.restaurant = new CustomerRestaurantView(restaurant);
        accepted = reservation.isAccepted();
        rejected = reservation.isRejected();
        rejectionNotice = reservation.getRejectionNotice();
        deleted = reservation.isCancelled();
        if(reservation.getInstantiatedFrom() != null) {
            instantiatedFromId = reservation.getInstantiatedFrom().getId();
        } else {
            instantiatedFromId = ActivityInstantiationConstant.WAITER.getId();
        }
    }

    public CustomerReservationView(BookingRequest request, Restaurant restaurant) {
        notes = request.getNotes();
        telephone = request.getTelephone();
        email = request.getEmail();
        ZonedDateTime utc = TimeUtil.getUTCDateTime(request.getDate(), request.getTime(), restaurant.getIANATimezone());
        reservationTime = utc.toEpochSecond();
        numberOfPeople = request.getNumberOfPeople();
        this.restaurant = new CustomerRestaurantView(restaurant);
        this.restaurantId = restaurant.getId();
        accepted = request.isAccepted();
        rejected = false;
    }

    public CustomerReservationView(TimeSlots request, long time, Restaurant restaurant) {
        reservationTime = time;
        numberOfPeople = request.getNumberOfPeople();
        this.restaurant = new CustomerRestaurantView(restaurant);
        this.restaurantId = restaurant.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(Long reservationTime) {
        this.reservationTime = reservationTime;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public CustomerRestaurantView getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(CustomerRestaurantView restaurant) {
        this.restaurant = restaurant;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(Long arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public String getRejectionNotice() {
        return rejectionNotice;
    }

    public void setRejectionNotice(String rejectionNotice) {
        this.rejectionNotice = rejectionNotice;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getInstantiatedFromId() {
        return instantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        this.instantiatedFromId = instantiatedFromId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(email != null && ControllerUtil.EMAIL_REGEX.matcher(email).matches()) {
            this.email = email.toLowerCase().trim();
        } else {
            this.email = null;
        }
    }

    @Override
    public int compareTo(CustomerReservationView o) {
        if(reservationTime != null) {
            return reservationTime.compareTo(o.reservationTime != null ? o.reservationTime : 0);
        }

        return 0;
    }
}
