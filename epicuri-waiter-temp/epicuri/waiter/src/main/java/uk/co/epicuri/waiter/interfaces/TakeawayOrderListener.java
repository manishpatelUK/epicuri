package uk.co.epicuri.waiter.interfaces;

import java.util.ArrayList;

import uk.co.epicuri.waiter.model.EpicuriOrderItem;

public interface TakeawayOrderListener {
    void setOrder(ArrayList<EpicuriOrderItem> items);
    ArrayList<EpicuriOrderItem> getOrder();
    void finishAdding();
}
