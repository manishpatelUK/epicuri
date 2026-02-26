package uk.co.epicuri.waiter.interfaces;

import java.util.ArrayList;

import uk.co.epicuri.waiter.model.EpicuriMenu;

/**
 * Created by Home on 7/18/16.
 */
public interface OnMenuItemsSelectedListener {
    void selectMenuItems(EpicuriMenu.Group group, ArrayList<String> menuItemIds, String menuId);
}
