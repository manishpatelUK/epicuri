package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostReservationRequest {
    @JsonProperty("NumberOfPeople")
    private int numberInParty;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("Telephone")
    private String phoneNumber;

    @JsonProperty("ReservationTime")
    private long reservationTime; //in seconds

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Email")
    private String email;//new!

    @JsonProperty("LeadCustomerId")
    private String leadCustomerId;

    private boolean omitFromChecks = false;

    private String tableId;
    private int duration;

    public int getNumberInParty() {
        return numberInParty;
    }

    public void setNumberInParty(int numberInParty) {
        this.numberInParty = numberInParty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(long reservationTime) {
        this.reservationTime = reservationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLeadCustomerId() {
        return leadCustomerId;
    }

    public void setLeadCustomerId(String leadCustomerId) {
        this.leadCustomerId = leadCustomerId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
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
}
