package uk.co.epicuri.serverapi.common.pojo.host.reporting;

import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manish.
 */
public class HistoricalDataWrapper {
    private List<Session> liveData;
    private List<Session> oldData;
    private Map<String,List<Order>> liveOrders;
    private Map<String,List<Order>> oldOrders;

    public List<Session> getLiveData() {
        return liveData;
    }

    public void setLiveData(List<Session> liveData) {
        this.liveData = liveData;
    }

    public List<Session> getOldData() {
        return oldData;
    }

    public void setOldData(List<Session> oldData) {
        this.oldData = oldData;
    }

    public Map<String, List<Order>> getLiveOrders() {
        return liveOrders;
    }

    public void setLiveOrders(Map<String, List<Order>> liveOrders) {
        this.liveOrders = liveOrders;
    }

    public Map<String, List<Order>> getOldOrders() {
        return oldOrders;
    }

    public void setOldOrders(Map<String, List<Order>> oldOrders) {
        this.oldOrders = oldOrders;
    }

    public List<Session> allSessions() {
        List<Session> all = new ArrayList<>();
        all.addAll(oldData);
        all.addAll(liveData);
        return all;
    }

    public Map<String,List<Order>> allOrders() {
        Map<String,List<Order>> all = new HashMap<>();
        all.putAll(oldOrders);
        all.putAll(liveOrders);
        return all;
    }
}
