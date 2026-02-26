package uk.co.epicuri.bookingapi.pojo.aurusit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 22/06/2015.
 */
public class AurusitOrderRequest {
    private List<AurusitOrder> items = new ArrayList<>();
    private String tableName;
    private String adhocName;

    public List<AurusitOrder> getItems() {
        return items;
    }

    public void setItems(List<AurusitOrder> items) {
        this.items = items;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getAdhocName() {
        return adhocName;
    }

    public void setAdhocName(String adhocName) {
        this.adhocName = adhocName;
    }

    private String itemsToString() {
        String s = "[";
        for(AurusitOrder item : items) {
            s += item.toString();
        }
        return s + "]";
    }

    @Override
    public String toString() {
        return "AurusitOrderRequest{" +
                "items=" + items +
                ", tableName='" + tableName + '\'' +
                ", adhocName='" + adhocName + '\'' +
                '}';
    }
}
