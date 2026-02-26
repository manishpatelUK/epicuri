package uk.co.epicuri.waiter.interfaces;

import java.util.ArrayList;

import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;

public interface RemoveOrderListener {
    void removeOrderItems(ArrayList<EpicuriOrderItem> items, EpicuriAdjustmentType reason);
}
