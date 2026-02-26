package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationRequest;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

@Document(collection = TableNames.BOOKING)
public class Booking extends Deletable implements Comparable<Booking>{
    @Indexed
    private String restaurantId;

    @Indexed(sparse = true)
    private String customerId;

    private String name;
    private String notes;

    @Indexed
    private long targetTime; // reservation time or takeaway expected time

    private long createdTime = System.currentTimeMillis();
    private String email;
    private String telephone;
    private String rejectionNotice;
    private boolean rejected;
    private boolean accepted;
    private boolean cancelled;
    private ActivityInstantiationConstant instantiatedFrom;
    private BookingType bookingType;
    private boolean omitFromChecks;

    //reservations only
    private int numberOfPeople;
    private String tableId;//new
    private int duration;

    //takeaway only
    private TakeawayType takeawayType;
    private Address deliveryAddress;

    //booking widget only
    private Boolean optedIntoMarketing;

    public Booking(){}

    public Booking(Booking original) {
        this.setId(original.getId());
        this.setDeleted(original.getDeleted());
        this.restaurantId = original.getRestaurantId();
        this.customerId = original.getCustomerId();
        if(original.getName() != null) {
            this.name = original.getName();
        } else {
            this.name = original.getBookingType() == BookingType.RESERVATION ? "Reservation" : "Takeaway";
        }
        this.notes = original.getNotes();
        this.targetTime = original.getTargetTime();
        this.createdTime = original.getCreatedTime();
        this.email = original.getEmail();
        this.telephone = original.getTelephone();
        this.rejectionNotice = original.getRejectionNotice();
        this.rejected = original.isRejected();
        this.accepted = original.isAccepted();
        this.cancelled = original.isCancelled();
        this.instantiatedFrom = original.getInstantiatedFrom();
        this.bookingType = original.getBookingType();
        this.numberOfPeople = original.getNumberOfPeople();
        this.tableId = original.getTableId();
        this.takeawayType = original.getTakeawayType();
        this.deliveryAddress = original.getDeliveryAddress();
        this.tableId = original.getTableId();
        this.duration = original.getDuration();
        this.omitFromChecks = original.isOmitFromChecks();
    }

    public Booking(CustomerReservationView customerReservationView, Customer customer, long createdTime, int duration) {
        this.restaurantId = customerReservationView.getRestaurantId();
        this.numberOfPeople = customerReservationView.getNumberOfPeople();
        if(customer != null) {
            this.name = Customer.determineName(customer);
            this.customerId = customer.getId();
        } else {
            this.name = "Reservation for " + customerReservationView.getNumberOfPeople();
        }
        this.createdTime = createdTime;
        this.notes = customerReservationView.getNotes();
        this.email = customerReservationView.getEmail();
        this.telephone = customerReservationView.getTelephone();
        this.targetTime = customerReservationView.getReservationTime() * 1000; //store in milliseconds
        this.rejectionNotice = customerReservationView.getRejectionNotice();
        this.rejected = customerReservationView.isRejected();
        this.accepted = customerReservationView.isAccepted();
        this.instantiatedFrom = ActivityInstantiationConstant.valueOf(customerReservationView.getInstantiatedFromId());
        this.bookingType = BookingType.RESERVATION;
        if(customerReservationView.isDeleted()) {
            setDeleted(System.currentTimeMillis());
        }
        this.duration = duration;
    }

    public Booking(String restaurantId, HostReservationRequest request, Customer customer) {
        this(restaurantId, request);
        if(customer != null) {
            this.customerId = customer.getId();
            this.email = customer.getEmail();
            this.name = Customer.determineName(customer);
            this.telephone = customer.getPhoneNumber();
        }
        this.tableId = request.getTableId();
        this.duration = request.getDuration();
        this.omitFromChecks = request.isOmitFromChecks();
    }

    public Booking(String restaurantId, HostReservationRequest request) {
        this.restaurantId = restaurantId;
        if(request.getName() != null) {
            this.name = request.getName();
        } else {
            this.name = "Reservation for " + request.getNumberInParty();
        }
        this.notes = request.getNotes();
        this.numberOfPeople = request.getNumberInParty();
        this.telephone = request.getPhoneNumber();
        this.targetTime = request.getReservationTime() * 1000; //store in milliseconds
        this.tableId = request.getTableId();
        this.duration = request.getDuration();
        this.bookingType = BookingType.RESERVATION;
        this.omitFromChecks = request.isOmitFromChecks();
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public long getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(long targetTime) {
        this.targetTime = targetTime;
    }

    public String getRejectionNotice() {
        return rejectionNotice;
    }

    public void setRejectionNotice(String rejectionNotice) {
        this.rejectionNotice = rejectionNotice;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ActivityInstantiationConstant getInstantiatedFrom() {
        return instantiatedFrom;
    }

    public void setInstantiatedFrom(ActivityInstantiationConstant instantiatedFrom) {
        this.instantiatedFrom = instantiatedFrom;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public TakeawayType getTakeawayType() {
        return takeawayType;
    }

    public void setTakeawayType(TakeawayType takeawayType) {
        this.takeawayType = takeawayType;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Boolean getOptedIntoMarketing() {
        return optedIntoMarketing;
    }

    public void setOptedIntoMarketing(Boolean optedIntoMarketing) {
        this.optedIntoMarketing = optedIntoMarketing;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isOmitFromChecks() {
        return omitFromChecks;
    }

    public void setOmitFromChecks(boolean omitFromChecks) {
        this.omitFromChecks = omitFromChecks;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && this.getClass() == object.getClass() && EqualsBuilder.reflectionEquals(this,object);
    }

    @Override
    public int compareTo(Booking o) {
        return Long.compare(this.targetTime, o.targetTime);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
