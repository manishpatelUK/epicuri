package uk.co.epicuri.serverapi.common.pojo.host;

import java.util.ArrayList;
import java.util.List;

public class OrderAttributionView {
    private String dinerId;
    private List<String> orderIds = new ArrayList<>();
    private List<String> unassignedOrderIds = new ArrayList<>();

    public String getDinerId() {
        return dinerId;
    }

    public void setDinerId(String dinerId) {
        this.dinerId = dinerId;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public List<String> getUnassignedOrderIds() {
        return unassignedOrderIds;
    }

    public void setUnassignedOrderIds(List<String> unassignedOrderIds) {
        this.unassignedOrderIds = unassignedOrderIds;
    }
}
