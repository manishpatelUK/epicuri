package com.thinktouchsee.util.menuuploader;

import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: manishpatel
 * Date: 25/06/2015
 */
public class MenuReader {
    public static void main(String[] args) {
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Authentication authentication = api.login("24","epicuriadmin","keshavroshan");
        //System.out.println(authentication.getAuthKey());
        List<MenuItem> items = api.getMenuItems(authentication.getAuthKey());
        Map<Integer,Printer> printerMap = api.getPrinterIds(authentication.getAuthKey()).stream().collect(Collectors.toMap(Printer::getId, Function.identity()));
        List<Menu> menus = api.getMenus(authentication.getAuthKey());

        Map<Integer,String> itemTrail = new HashMap<>();
        for(Menu menu : menus) {
            for(Menu.MenuCategory category : menu.getMenuCategories()) {
                for(Menu.MenuCategory.MenuGroup group : category.getMenuGroups()) {
                    for(int item : group.getMenuItemIds()) {
                        itemTrail.put(item, menu.getMenuName() + ">" + category.getCategoryName() + ">" + group.getGroupName());
                    }
                }
            }
        }

        Set<Integer> done = new HashSet<>();

        for(MenuItem item : items) {
            if(!done.contains(item.getId())){
                done.add(item.getId());
            } else {
                continue;
            }

            StringBuilder buffer = new StringBuilder();
            buffer.append(item.getName())
                    .append(",")
                    .append(item.getPrice())
                    .append(",")
                    .append("Description")
                    .append(",UK VAT 20%,")
                    .append(printerMap.get(item.getDefaultPrinter()).getName())
                    .append(",")
                    .append(item.getMenuItemTypeId() == 0 ? "Food" : "Drink")
                    .append(",FALSE,")
                    .append(itemTrail.get(item.getId()));

            System.out.println(buffer.toString());
        }
    }
}
