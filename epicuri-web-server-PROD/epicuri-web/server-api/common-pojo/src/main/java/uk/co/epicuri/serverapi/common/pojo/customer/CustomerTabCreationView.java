package uk.co.epicuri.serverapi.common.pojo.customer;

/**
 * Created by manish on 19/01/2018.
 */
public class CustomerTabCreationView {
    private String sessionId;
    private String checkInId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(String checkInId) {
        this.checkInId = checkInId;
    }
}
