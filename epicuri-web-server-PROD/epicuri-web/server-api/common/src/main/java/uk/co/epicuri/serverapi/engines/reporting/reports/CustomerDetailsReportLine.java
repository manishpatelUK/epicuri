package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;

public class CustomerDetailsReportLine extends AbstractReportLine  {
    @CsvBindByName(column = "Name")
    @CsvSortOrder(order = 0)
    private String name;
    @CsvBindByName(column = "Phone")
    @CsvSortOrder(order = 1)
    private String phone;
    @CsvBindByName(column = "Email")
    @CsvSortOrder(order = 2)
    private String email;
    @CsvBindByName(column = "Reservations")
    @CsvSortOrder(order = 3)
    private String reservations;
    @CsvBindByName(column = "Takeaways")
    @CsvSortOrder(order = 4)
    private String takeaways;
    @CsvBindByName(column = "First Booking")
    @CsvSortOrder(order = 5)
    private String firstBooking;
    @CsvBindByName(column = "Last Booking")
    @CsvSortOrder(order = 6)
    private String lastBooking;
    @CsvBindByName(column = "Total Value")
    @CsvSortOrder(order = 7)
    private String totalValue;

    public CustomerDetailsReportLine() {
        super(CustomerDetailsReportLine.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReservations() {
        return reservations;
    }

    public void setReservations(String reservations) {
        this.reservations = reservations;
    }

    public String getTakeaways() {
        return takeaways;
    }

    public void setTakeaways(String takeaways) {
        this.takeaways = takeaways;
    }

    public String getFirstBooking() {
        return firstBooking;
    }

    public void setFirstBooking(String firstBooking) {
        this.firstBooking = firstBooking;
    }

    public String getLastBooking() {
        return lastBooking;
    }

    public void setLastBooking(String lastBooking) {
        this.lastBooking = lastBooking;
    }

    public String getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(String totalValue) {
        this.totalValue = totalValue;
    }
}
