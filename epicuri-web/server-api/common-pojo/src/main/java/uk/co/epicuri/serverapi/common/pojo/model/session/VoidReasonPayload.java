package uk.co.epicuri.serverapi.common.pojo.model.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VoidReasonPayload {

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("VoidTime")
    private long voidTime; //in seconds

    @JsonProperty("Staff")
    private StaffView staff;

    public VoidReasonPayload(){}

    public VoidReasonPayload(String reason, long voidTime, Staff staff) {
        this.reason = reason;
        this.voidTime = voidTime / 1000;
        this.staff = new StaffView(staff);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getVoidTime() {
        return voidTime;
    }

    public void setVoidTime(long voidTime) {
        this.voidTime = voidTime;
    }

    public StaffView getStaff() {
        return staff;
    }

    public void setStaff(StaffView staff) {
        this.staff = staff;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
