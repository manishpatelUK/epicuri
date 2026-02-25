package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class TakeawayLine extends AbstractReportLine implements IBookingLine{
    @CsvBindByName(column = "Session Id")
    @CsvSortOrder(order = 0)
    private String sessionId;
    @CsvBindByName(column = "Date/Time")
    @CsvSortOrder(order = 1)
    private String time;
    @CsvBindByName(column = "Takeaway Type")
    @CsvSortOrder(order = 2)
    private String takeawayType;
    @CsvBindByName(column = "Booking State")
    @CsvSortOrder(order = 3)
    private String bookingState;
    @CsvBindByName(column = "Name")
    @CsvSortOrder(order = 4)
    private String name;
    @CsvBindByName(column = "Phone")
    @CsvSortOrder(order = 5)
    private String telephone;
    @CsvBindByName(column = "Email")
    @CsvSortOrder(order = 6)
    private String email;
    @CsvBindByName(column = "Address")
    @CsvSortOrder(order = 7)
    private String address;
    @CsvBindByName(column = "Notes")
    @CsvSortOrder(order = 8)
    private String notes;
    @CsvBindByName(column = "Total")
    @CsvSortOrder(order = 9)
    private String total;
    @CsvBindByName(column = "Payment Status")
    @CsvSortOrder(order = 10)
    private String paymentStatus;
    @CsvBindByName(column = "Time Created")
    @CsvSortOrder(order = 11)
    private String creationTime;
    @CsvBindByName(column = "Booking Via")
    @CsvSortOrder(order = 12)
    private String origin;
    @CsvBindByName(column = "Booked by Staff")
    @CsvSortOrder(order = 13)
    private String bookedByStaff;
    @CsvBindByName(column = "Voided?")
    @CsvSortOrder(order = 14)
    private String voided;

    public TakeawayLine() {
        super(TakeawayLine.class);
    }


    public String getTakeawayType() {
        return takeawayType;
    }

    public void setTakeawayType(String takeawayType) {
        this.takeawayType = takeawayType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getBookedByStaff() {
        return bookedByStaff;
    }

    public void setBookedByStaff(String bookedByStaff) {
        this.bookedByStaff = bookedByStaff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCovers() {
        return "0";
    }

    @Override
    public void setCovers(String covers) {

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getVoided() {
        return voided;
    }

    public void setVoided(String voided) {
        this.voided = voided;
    }
}
