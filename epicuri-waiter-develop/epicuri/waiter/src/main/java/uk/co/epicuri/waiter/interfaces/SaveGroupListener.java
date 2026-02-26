package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriMenu;

/**
 * Created by Home on 7/18/16.
 */
public interface SaveGroupListener {
    void createGroup(CharSequence name, String categoryId, String menuId);
    void saveGroup(EpicuriMenu.Group group, CharSequence name, String categoryId, String menuId);
    void deleteGroup(String id, String menuId);
}