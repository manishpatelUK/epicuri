package uk.co.epicuri.serverapi.common.pojo.customer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by manish on 03/11/2017.
 */
public class OrderAggregation {
    private String menuItemId;
    private Set<String> orderIds = new HashSet<>();
    private int totalQuantity;

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public Set<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(Set<String> orderIds) {
        this.orderIds = orderIds;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void increment(int quantity) {
        totalQuantity += quantity;
    }
}
