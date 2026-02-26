package uk.co.epicuri.waiter.interfaces;

import java.util.ArrayList;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;

public interface TakeAweyOrderRemoveListener {
    void showConfirmScreen();
    void addItems();

    void setOrder(ArrayList<EpicuriOrderItem> items);
    ArrayList<EpicuriOrderItem> getOrder();
}
