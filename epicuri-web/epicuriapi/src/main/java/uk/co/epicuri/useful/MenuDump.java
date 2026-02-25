package uk.co.epicuri.useful;

import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.Menu;
import uk.co.epicuri.api.core.pojo.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 02/06/2015
 */
public class MenuDump {

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Args: restaurantId");
        }

        MenuDump dump = new MenuDump(args[0]);
        dump.dump();
    }

    private final EpicuriAPI api;
    private final String restaurantId;

    public MenuDump(String restaurantId) {
        this.restaurantId = restaurantId;
        api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
    }

    public void dump() {
        Authentication authentication = api.login(restaurantId,"epicuriadmin","keshavroshan");
        List<Menu> menus = api.getMenus(authentication.getAuthKey());
        List<MenuItem> items = api.getMenuItems(authentication.getAuthKey());
        Map<Integer,MenuItem> map = new HashMap<>();
        for(MenuItem item : items) {
            map.put(item.getId(),item);
        }

        System.out.println("MenuId|MenuName|CategoryId|CategoryName|GroupId|GroupName|MenuItemId|MenuItemName");

        for(Menu menu : menus) {
            for(Menu.MenuCategory category : menu.getMenuCategories()) {
                for(Menu.MenuCategory.MenuGroup group : category.getMenuGroups()) {
                    for(int id : group.getMenuItemIds()) {
                        String string = menu.getId() + "|" + menu.getMenuName() + "|"
                                        + category.getId() + "|" + category.getCategoryName() + "|"
                                        + group.getId() + "|" + group.getGroupName() + "|"
                                        + id + "|" + map.get(id).getName();
                        System.out.println(string);

                    }
                }
            }
        }
    }
}
