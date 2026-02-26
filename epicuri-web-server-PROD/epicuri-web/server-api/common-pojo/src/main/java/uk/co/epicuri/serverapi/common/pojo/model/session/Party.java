package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;

@Document(collection = TableNames.PARTIES)
public class Party extends Deletable {
    @Indexed
    private String restaurantId;

    private long time = System.currentTimeMillis();
    private int numberOfPeople;
    private String name;
    private ActivityInstantiationConstant instantiatedFrom;
    private String bookingId;
    private PartyType partyType;
    private Long arrivedTime;
    private String customerId; 

    public Party(){}

    public Party(Booking booking) {
        this.restaurantId = booking.getRestaurantId();
        this.time = booking.getTargetTime();
        this.numberOfPeople = booking.getNumberOfPeople();
        this.name = booking.getName();
        this.instantiatedFrom = booking.getInstantiatedFrom();
        this.bookingId = booking.getId();
        this.partyType = PartyType.RESERVATION;
        this.arrivedTime = System.currentTimeMillis();
        this.customerId = booking.getCustomerId();
    }

    public Party(WaitingPartyPayload payload, String restaurantId) {
        this.restaurantId = restaurantId;
        this.numberOfPeople = payload.getNumberOfPeople();
        this.name = payload.getName();
        this.instantiatedFrom = ActivityInstantiationConstant.WAITER;
        this.partyType = PartyType.WALK_IN;
        this.arrivedTime = System.currentTimeMillis();
        this.time = arrivedTime;
        if(payload.getCustomer() != null) {
            this.customerId = payload.getCustomer().getId();
        }

    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public ActivityInstantiationConstant getInstantiatedFrom() {
        return instantiatedFrom;
    }

    public void setInstantiatedFrom(ActivityInstantiationConstant instantiatedFrom) {
        this.instantiatedFrom = instantiatedFrom;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public void setPartyType(PartyType partyType) {
        this.partyType = partyType;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(Long arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object.getClass() == getClass() && EqualsBuilder.reflectionEquals(this,object);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}

