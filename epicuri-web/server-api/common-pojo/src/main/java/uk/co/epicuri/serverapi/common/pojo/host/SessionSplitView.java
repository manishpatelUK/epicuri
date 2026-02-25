package uk.co.epicuri.serverapi.common.pojo.host;


import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import java.util.ArrayList;
import java.util.List;

public class SessionSplitView {
    private List<String> orderIds = new ArrayList<>();
    private SessionType sessionType = SessionType.TAB;

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }
}
