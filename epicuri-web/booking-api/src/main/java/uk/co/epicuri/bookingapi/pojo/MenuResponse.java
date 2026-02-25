package uk.co.epicuri.bookingapi.pojo;

import uk.co.epicuri.bookingapi.pojo.menustructure.Menu;

import java.util.ArrayList;
import java.util.List;

/**
 * 27/05/2015
 */
public class MenuResponse {
    private List<Menu> menus = new ArrayList<>();

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }
}
