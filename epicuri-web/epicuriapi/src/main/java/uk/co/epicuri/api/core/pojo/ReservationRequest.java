package uk.co.epicuri.api.core.pojo;

import java.util.Date;

/**
 * 05/02/2015
 */
public class ReservationRequest {

    private String Name;
    private String Telephone;
    private String Notes;
    private long ReservationTime;
    private int NumberOfPeople;
    private int LeadCustomerId;
    private String Email;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
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

    public long getReservationTime() {
        return ReservationTime;
    }

    public void setReservationTime(long reservationTime) {
        ReservationTime = reservationTime;
    }

    public int getNumberOfPeople() {
        return NumberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        NumberOfPeople = numberOfPeople;
    }

    public int getLeadCustomerId() {
        return LeadCustomerId;
    }

    public void setLeadCustomerId(int leadCustomerId) {
        LeadCustomerId = leadCustomerId;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
