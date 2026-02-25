package uk.co.epicuri.serverapi.common.pojo.customer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CustomerInteractionDeferredSession extends CustomerInteraction {
    private String sessionId;
    private String staffId;
    private boolean paid;
    private String settlementSessionId;

    public CustomerInteractionDeferredSession(){
        super();
    }

    public CustomerInteractionDeferredSession(String customerId, String restaurantId, String sessionId, String staffId) {
        super(customerId, restaurantId);
        this.sessionId = sessionId;
        this.staffId = staffId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getSettlementSessionId() {
        return settlementSessionId;
    }

    public void setSettlementSessionId(String settlementSessionId) {
        this.settlementSessionId = settlementSessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CustomerInteractionDeferredSession that = (CustomerInteractionDeferredSession) o;

        if (paid != that.paid) return false;
        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;
        if (staffId != null ? !staffId.equals(that.staffId) : that.staffId != null) return false;
        return settlementSessionId != null ? settlementSessionId.equals(that.settlementSessionId) : that.settlementSessionId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
        result = 31 * result + (staffId != null ? staffId.hashCode() : 0);
        result = 31 * result + (paid ? 1 : 0);
        result = 31 * result + (settlementSessionId != null ? settlementSessionId.hashCode() : 0);
        return result;
    }
}
