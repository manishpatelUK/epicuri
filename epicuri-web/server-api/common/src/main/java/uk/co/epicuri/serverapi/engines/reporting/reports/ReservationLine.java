package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class ReservationLine extends AbstractReportLine implements IBookingLine {
    @CsvBindByName(column = "Date/Time")
    @CsvSortOrder(order = 0)
    private String time;
    @CsvBindByName(column = "Booking State")
    @CsvSortOrder(order = 1)
    private String bookingState;
    @CsvBindByName(column = "Booking Via")
    @CsvSortOrder(order = 2)
    private String origin;
    @CsvBindByName(column = "Booked by Staff")
    @CsvSortOrder(order = 3)
    private String bookedByStaff;
    @CsvBindByName(column = "Name")
    @CsvSortOrder(order = 4)
    private String name;
    @CsvBindByName(column = "Covers")
    @CsvSortOrder(order = 5)
    private String covers;
    @CsvBindByName(column = "Phone")
    @CsvSortOrder(order = 6)
    private String telephone;
    @CsvBindByName(column = "Email")
    @CsvSortOrder(order = 7)
    private String email;
    @CsvBindByName(column = "Time Created")
    @CsvSortOrder(order = 8)
    private String creationTime;
    @CsvBindByName(column = "Notes")
    @CsvSortOrder(order = 9)
    private String notes;

    public ReservationLine() {
        super(ReservationLine.class);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCovers() {
        return covers;
    }

    public void setCovers(String covers) {
        this.covers = covers;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBookingState() {
        return bookingState;
    }

    public void setBookingState(String bookingState) {
        this.bookingState = bookingState;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getBookedByStaff() {
        return bookedByStaff;
    }

    public void setBookedByStaff(String bookedByStaff) {
        this.bookedByStaff = bookedByStaff;
    }
}
