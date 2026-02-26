package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostPartyView {
    @JsonProperty("LeadCustomer")
    private HostCustomerView leadCustomer;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("NumberOfPeople")
    private int numberInParty;

    @JsonProperty("Created")
    private long created; // in seconds

    @JsonProperty("ReservationTime")
    private Long reservationTime; // in seconds

    @JsonProperty("ArrivedTime")
    private Long arrivedTime; // in seconds

    @JsonProperty("Accepted")
    private boolean accepted;

    @JsonProperty("sessionType")
    private SessionType sessionType;

    @JsonProperty("bookingId")
    private String bookingId;

    public HostPartyView(){}

    public HostPartyView(Party party, Customer customer, Map<String,Preference> preferenceMap, Session session){
        this.leadCustomer = new HostCustomerView(customer, preferenceMap);
        updatePartyDetails(party);
        if(session != null) {
            updateSessionDetails(session);
        } else {
            sessionId = "0";
        }
    }

    public HostPartyView(Party party, Session session, Booking booking, Map<String,Preference> preferenceMap, Customer customer){
        this(party, session, booking);
        this.leadCustomer = new HostCustomerView(customer, preferenceMap);
    }

    public HostPartyView(Party party, Session session){
        updatePartyDetails(party);
        if(session != null) {
            updateSessionDetails(session);
        }
    }

    public HostPartyView(Party party, Session session, Booking booking){
        updatePartyDetails(party);
        if(session != null) {
            updateSessionDetails(session);
        }
        if(booking != null) {
            updateBookingDetails(booking, party);
        }
    }

    private void updatePartyDetails(Party party) {
        this.id = party.getId();
        this.name = party.getName();
        this.numberInParty = party.getNumberOfPeople();
        this.created = party.getTime() / 1000;
    }

    private void updateSessionDetails(Session session) {
        this.sessionId = session.getId();
        this.sessionType = session.getSessionType();
    }

    private void updateBookingDetails(Booking booking, Party party) {
        this.accepted = booking.isAccepted();
        this.reservationTime = booking.getTargetTime() / 1000;
        this.arrivedTime = (party.getArrivedTime() == null ? booking.getTargetTime() : party.getArrivedTime()) / 1000;
        this.bookingId = booking.getId();
    }

    public HostCustomerView getLeadCustomer() {
        return leadCustomer;
    }

    public void setLeadCustomer(HostCustomerView leadCustomer) {
        this.leadCustomer = leadCustomer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberInParty() {
        return numberInParty;
    }

    public void setNumberInParty(int numberInParty) {
        this.numberInParty = numberInParty;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public Long getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(Long reservationTime) {
        this.reservationTime = reservationTime;
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

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
}
