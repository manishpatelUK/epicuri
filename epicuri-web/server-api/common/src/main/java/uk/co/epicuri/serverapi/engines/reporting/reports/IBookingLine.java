package uk.co.epicuri.serverapi.engines.reporting.reports;

public interface IBookingLine {
    String getTime();
    void setTime(String time);
    String getName();
    void setName(String name);
    String getCovers();
    void setCovers(String covers);
    String getTelephone();
    void setTelephone(String telephone);
    String getEmail();
    void setEmail(String email);
    String getBookingState();
    void setBookingState(String bookingState);
    String getOrigin();
    void setOrigin(String origin);
    String getCreationTime();
    void setCreationTime(String creationTime);
    String getNotes();
    void setNotes(String notes);
    String getBookedByStaff();
    void setBookedByStaff(String bookedByStaff);
}
