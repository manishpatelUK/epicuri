package uk.co.epicuri.waiter.interfaces;

/**
 * Created by Home on 7/18/16.
 */
public interface SaveMenuListener {
    void createMenu(CharSequence name, boolean active);
    void saveMenu(String menuId, CharSequence name, boolean active, int order);
    void deleteMenu(String menuId);
}
