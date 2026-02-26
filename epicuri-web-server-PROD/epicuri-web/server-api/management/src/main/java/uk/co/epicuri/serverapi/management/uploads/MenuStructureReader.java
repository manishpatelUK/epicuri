package uk.co.epicuri.serverapi.management.uploads;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterView;
import uk.co.epicuri.serverapi.common.pojo.host.HostServiceView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTaxView;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryView;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MenuStructureReader {
    private final List<MenuItemView> menuItemList = new ArrayList<>();

    private final List<MenuItemView> originalMenuItemViews;
    private final List<MenuItemView> existingMenuItemViews;
    private final Map<String,List<MenuItemView>> groupToMenuItemDupes = new HashMap<>();
    private final List<HostTaxView> taxRates;
    private final List<HostPrinterView> printers;
    private List<MenuView> menus;
    private final List<HostServiceView> services;
    private final WebService api;

    public MenuStructureReader(WebService api) {
        this.api = api;
        taxRates = api.getAsList(Endpoints.TAX_PATH, HostTaxView.class);
        printers = api.getAsList(Endpoints.PRINTER_PATH, HostPrinterView.class);
        originalMenuItemViews = getMenuItemsFromAPI(api);
        services = api.getAsList(Endpoints.SERVICE, HostServiceView.class);
        existingMenuItemViews = new ArrayList<>(originalMenuItemViews);
        refreshMenusList();
    }

    private List<MenuItemView> getMenuItemsFromAPI(WebService api) {
        return api.getAsList(Endpoints.MENU_ITEM_PATH, MenuItemView.class);
    }

    private void refreshMenusList() {
        menus = api.getAsList(Endpoints.MENU_PATH, MenuView.class);
    }

    public void initiateRead(File file) throws IOException {
        // format
        // Name,Price,Description,Tax Type,HostPrinterView,Item,Type,Unavailable,MenuView Structure,Courses,

        read(file);
    }

    public List<MenuItemView> getMenuItemViewList() {
        return menuItemList;
    }

    public void ensureGroupMembers() {
        refreshMenusList();

        List<GroupView> groupsChanged = new ArrayList<>();

        for(MenuView menu : menus) {
            for(CategoryView categoryView : menu.getMenuCategories()) {
                for(GroupView groupView : categoryView.getMenuGroups()) {
                    boolean groupChanged = false;
                    if(groupToMenuItemDupes.containsKey(groupView.getId())) {
                        List<MenuItemView> menuItemViews = groupToMenuItemDupes.get(groupView.getId());
                        for(MenuItemView menuItemView : menuItemViews) {
                            if( menuItemView.getId() != null
                                && !groupView.getMenuItemIds().contains(menuItemView.getId())) {

                                groupView.getMenuItemIds().add(menuItemView.getId());
                                groupChanged = true;
                            }
                        }
                    }

                    if(groupChanged) {
                        groupsChanged.add(groupView);
                    }
                }
            }
        }

        System.out.println(groupsChanged.size() + " groups will be updated with items");
        for(GroupView groupView : groupsChanged) {
            api.put(Endpoints.MENU_GROUP_PATH + "/" + groupView.getId(), groupView);
        }
    }

    private void read(File file) throws IOException {
        //BufferedReader reader = new BufferedReader(new FileReader(file));

        FileReader in = new FileReader(file);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

        boolean firstLine = true;
        for (CSVRecord record : records) {
            //String line = reader.readLine().trim();
            //System.out.println(line);
            System.out.println(record.toString());

            if (firstLine) {
                firstLine = false;
                continue;
            }

            if (record.toString().isEmpty()) {
                continue;
            }

            String name = record.get(0).trim();

            String priceString = record.get(1).trim();
            if (priceString.matches("\\D.*")) {
                priceString = priceString.substring(1);
            }
            try {
                Double.parseDouble(priceString);
            } catch (NumberFormatException ex) {
                System.out.println("Barfed on " + name + ":" + priceString);
                throw new RuntimeException();
            }
            double price = Double.parseDouble(priceString);
            String description = record.get(2).trim();
            String taxType = record.get(3).trim();
            String printer = record.get(4).trim();
            ItemType itemType = getItemType(record.get(5).trim());
            boolean unavailable = getBoolean(record);
            String menuGroup = record.get(7).trim();
            String course = record.get(8).trim();

            MenuItemView potentialDupe = isAlreadyInDB(name, price, description);
            if (potentialDupe != null) {
                System.out.println("\tDuplicate item, skipping line - but will add later to the relevant group");
                String groupId = determineStructureAndGetGroupId(menuGroup, course);
                groupToMenuItemDupes.computeIfAbsent(groupId, k -> new ArrayList<>());
                groupToMenuItemDupes.get(groupId).add(potentialDupe);
                continue;
            }

            String alias = null;
            if(record.size() >= 10) {
                alias = record.get(9).trim();
            }

            if (StringUtils.isBlank(name)
                    || StringUtils.isBlank(taxType)
                    || StringUtils.isBlank(printer)) {
                System.out.println("\tBad line, skipping");
                continue;
            }

            try {
                MenuItemView item = new MenuItemView();
                item.setName(name);
                item.setPrice(price);
                item.setDescription(description);
                item.setTaxTypeId(getTaxId(taxType));
                item.setDefaultPrinter(getPrinterId(printer));
                item.setMenuItemTypeId(itemType.getId());
                item.setUnavailable(unavailable);
                item.setShortCode(alias);

                //ids
                List<String> list = new ArrayList<>();
                list.add(determineStructureAndGetGroupId(menuGroup, course));

                item.setMenuGroups(list);
                if (!duped(item)) {
                    menuItemList.add(item);
                } else {
                    System.out.println("This item was a dupe, but have updated the groups that it may be replicated to" + item.toString());
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                System.out.println("\tAn identifier for " + name + " is wrong: " + ex.getMessage() + " Ignoring this item, continuing...");
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("\t" + ex + "... cannot recover, exiting");
                System.exit(1);
            }
        }

        //reader.close();
    }

    private ItemType getItemType(String name) {
        if(name.toLowerCase().contains("food")) {
            return ItemType.FOOD;
        } else if(name.toLowerCase().contains("drink")) {
            return ItemType.DRINK;
        } else {
            return ItemType.OTHER;
        }
    }

    private boolean getBoolean(CSVRecord record) {
        String bool = record.get(6).trim();
        if (StringUtils.isBlank(bool)) {
            return false;
        }
        if (bool.toLowerCase().equals("true")) {
            return true;
        }
        if (bool.toLowerCase().equals("false")) {
            return false;
        }
        return Boolean.valueOf(bool);
    }

    private MenuItemView isAlreadyInDB(String name, double price, String description) {
        for (MenuItemView item : existingMenuItemViews) {
            if (item.getName().equalsIgnoreCase(name) && almostEqual(item.getPrice(), price, 0.001) && item.getDescription().equalsIgnoreCase(description)) {
                return item;
            }
        }

        return null;
    }

    public static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    private boolean duped(MenuItemView item) {
        boolean hasDupes = false;
        for (MenuItemView currentItemInList : menuItemList) {
            if (currentItemInList.getName().equalsIgnoreCase(item.getName())
                    && almostEqual(currentItemInList.getPrice(), item.getPrice(), 0.001)
                    && currentItemInList.getDescription().equalsIgnoreCase(item.getDescription())) {
                Set<String> set = new HashSet<>();
                set.addAll(currentItemInList.getMenuGroups());
                set.addAll(item.getMenuGroups());

                currentItemInList.setMenuGroups(new ArrayList<>(set));
                hasDupes = true;
            }
        }

        return hasDupes;
    }

    private String getTaxId(String name) {
        for (HostTaxView rate : taxRates) {
            if (rate.getName().equalsIgnoreCase(name)) {
                return rate.getId();
            }
        }
        System.out.println("\tTax name: " + name + " is invalid");
        throw new IllegalArgumentException("Tax type: " + name + " not recognised");
    }

    private String getPrinterId(String name) {
        for (HostPrinterView printer : printers) {
            if (printer.getName().equalsIgnoreCase(name)) {
                return printer.getId();
            }
        }
        System.out.println("\tPrinter name: " + name + " is invalid");
        throw new IllegalArgumentException("Printer: " + name + " not recognised");
    }

    private String determineStructureAndGetGroupId(String name, String course) {
        // split by >
        String[] bits = name.split(">");
        // make sure is length 3, if not, ignore this one
        if (bits.length != 3) {
            throw new IllegalArgumentException("Bad menu structure: \"" + name + "\", expected structure: MenuView>Category>Group");
        }

        String menuName = bits[0].trim();
        String categoryName = bits[1].trim();
        String groupName = bits[2].trim();
        // if [1] menu doesn't exist, create a new menu
        if (getMenuId(menuName) == null) {
            MenuView menuView = new MenuView();
            menuView.setMenuName(menuName);
            api.post(Endpoints.MENU_PATH, menuView, String.class);
            refreshMenusList();
        }
        // if [1]..[2] category doesn't exist, create a new category
        if (getCategoryId(menuName, categoryName) == null) {
            int orderIndex = 0;
            for (MenuView menu : menus) {
                if (menu.getMenuName().equals(menuName)) {
                    orderIndex = menu.getMenuCategories().size();
                }
            }

            CategoryView category = new CategoryView();
            category.setCategoryName(categoryName);
            category.setMenuId(getMenuId(menuName));
            category.setOrder(orderIndex);
            category.setDefaultCourses(new ArrayList<>());
            category.setDefaultCourseIds(new ArrayList<>());

            api.post(Endpoints.MENU_CATEGORY_PATH, category, String.class);
            refreshMenusList();
        }
        // if [1]..[2]..[3] group doesn't exist, create a new group
        if (getGroupId(menuName, categoryName, groupName) == null) {
            int orderIndex = 0;
            for (MenuView menu : menus) {
                if (menu.getMenuName().equals(menuName)) {
                    for (CategoryView category : menu.getMenuCategories()) {
                        orderIndex = category.getMenuGroups().size();
                    }
                }
            }

            GroupView menuGroup = new GroupView();
            menuGroup.setGroupName(groupName);
            menuGroup.setMenuCategoryId(getCategoryId(menuName, categoryName));
            menuGroup.setOrder(orderIndex);
            api.post(Endpoints.MENU_GROUP_PATH, menuGroup, GroupView.class);
            refreshMenusList();
        }

        return getGroupId(menuName, categoryName, groupName);
    }

    private String getMenuId(String menuName) {
        for (MenuView menu : menus) {
            if (menu.getMenuName().equals(menuName)) {
                return menu.getId();
            }
        }

        return null;
    }

    private String getCategoryId(String menuName, String category) {
        for (MenuView menu : menus) {
            for (CategoryView menuCategory : menu.getMenuCategories()) {
                if (menu.getMenuName().equals(menuName) && menuCategory.getCategoryName().equals(category)) {
                    return menuCategory.getId();
                }
            }
        }

        return null;
    }

    private String getGroupId(String menuName, String categoryName, String groupName) {
        for (MenuView menu : menus) {
            for (CategoryView menuCategory : menu.getMenuCategories()) {
                for (GroupView menuGroup : menuCategory.getMenuGroups()) {
                    if (menu.getMenuName().equals(menuName) && menuCategory.getCategoryName().equals(categoryName) && menuGroup.getGroupName().equals(groupName)) {
                        return menuGroup.getId();
                    }
                }
            }
        }

        return null;
    }
}