package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.Map;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostReservationView extends HostReservationRequest {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("ArrivedTime")
    private Long arrivedTime; //in seconds

    @JsonProperty("LeadCustomer")
    private HostCustomerView leadCustomer;

    @JsonProperty("SessionId")
    private String sessionId = "0";

    @JsonProperty("Accepted")
    private boolean accepted;

    @JsonProperty("Deleted")
    private boolean deleted;

    @JsonProperty("TimedOut")
    private boolean timedOut = false;

    @JsonProperty("RejectionNotice")
    private String rejectedReason;

    @JsonProperty("IsBirthday")
    private boolean birthday;

    private boolean omitFromChecks;

    private int duration;
    private ActivityInstantiationConstant origin;

    public HostReservationView(){}

    // nb party and session and customer could be null
    public HostReservationView(Booking booking,
                               Party party,
                               Session session,
                               Customer customer,
                               RestaurantDefault birthdayTimeSpan,
                               CheckIn checkIn,
                               RestaurantDefault walkInExpiration,
                               Map<String,Preference> allPreferences) {
        this.id = booking.getId();
        setReservationTime(booking.getTargetTime()/1000);
        if(party != null && party.getArrivedTime() != null) {
            this.arrivedTime = party.getArrivedTime()/1000;
        }
        if(party != null && checkIn != null) {
            long now = System.currentTimeMillis();
            if(checkIn.getTime() < now - (1000*60*((Number)walkInExpiration.getValue()).intValue())) {
                timedOut = true;
            }
        }
        if(session != null) {
            this.sessionId = session.getId();
        }
        setName(booking.getName());
        setNotes(booking.getNotes());
        setNumberInParty(booking.getNumberOfPeople());
        setEmail(booking.getEmail());
        this.deleted = booking.isCancelled();
        this.rejectedReason = booking.getRejectionNotice();
        setPhoneNumber(booking.getTelephone());
        if(customer != null && customer.isRegisteredViaApp()) {
            this.leadCustomer = new HostCustomerView(customer, allPreferences);
            setLeadCustomerId(leadCustomer.getId());
            this.birthday = TimeUtil.isBirthday(customer.getBirthday(),((Number)birthdayTimeSpan.getValue()).intValue());
        }
        setAccepted(booking.isAccepted());
        this.origin = booking.getInstantiatedFrom();
        this.omitFromChecks = booking.isOmitFromChecks();
    }

    public Long getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(Long arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public HostCustomerView getLeadCustomer() {
        return leadCustomer;
    }

    public void setLeadCustomer(HostCustomerView leadCustomer) {
        this.leadCustomer = leadCustomer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public boolean isBirthday() {
        return birthday;
    }

    public void setBirthday(boolean birthday) {
        this.birthday = birthday;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ActivityInstantiationConstant getOrigin() {
        return origin;
    }

    public void setOrigin(ActivityInstantiationConstant origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean isOmitFromChecks() {
        return omitFromChecks;
    }

    @Override
    public void setOmitFromChecks(boolean omitFromChecks) {
        this.omitFromChecks = omitFromChecks;
    }
}
