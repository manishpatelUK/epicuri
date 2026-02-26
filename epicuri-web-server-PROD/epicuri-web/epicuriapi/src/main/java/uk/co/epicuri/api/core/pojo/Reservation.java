package uk.co.epicuri.api.core.pojo;

import java.util.Comparator;

/**
 * 25/08/2014
 */
public class Reservation implements Comparable<Reservation> {
    private String Telephone;
    private String Notes;
    private int Id;
    private int RestaurantId;
    private int SessionId;
    private boolean Accepted;
    private boolean Rejected;
    private boolean Deleted = false;
    private String RejectionNotice;
    private int InstantiatedFromId = 2;  // for analytics
    private double ReservationTime;
    private String Name;
    private int NumberOfPeople;

    public int getNumberOfPeople() {
        return NumberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        NumberOfPeople = numberOfPeople;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getReservationTime() {
        return ReservationTime;
    }

    public void setReservationTime(double reservationTime) {
        ReservationTime = reservationTime;
    }

    public String getTelephone() {
        return Telephone;
    }

    public void setTelephone(String telephone) {
        Telephone = telephone;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getRestaurantId() {
        return RestaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        RestaurantId = restaurantId;
    }

    public int getSessionId() {
        return SessionId;
    }

    public void setSessionId(int sessionId) {
        SessionId = sessionId;
    }

    public boolean isAccepted() {
        return Accepted;
    }

    public void setAccepted(boolean accepted) {
        Accepted = accepted;
    }

    public boolean isRejected() {
        return Rejected;
    }

    public void setRejected(boolean rejected) {
        Rejected = rejected;
    }

    public boolean isDeleted() {
        return Deleted;
    }

    public void setDeleted(boolean deleted) {
        Deleted = deleted;
    }

    public String getRejectionNotice() {
        return RejectionNotice;
    }

    public void setRejectionNotice(String rejectionNotice) {
        RejectionNotice = rejectionNotice;
    }

    public int getInstantiatedFromId() {
        return InstantiatedFromId;
    }

    public void setInstantiatedFromId(int instantiatedFromId) {
        InstantiatedFromId = instantiatedFromId;
    }

    @Override
    public int compareTo(Reservation o) {
        return Double.compare(getReservationTime(),o.getReservationTime());
    }
}
