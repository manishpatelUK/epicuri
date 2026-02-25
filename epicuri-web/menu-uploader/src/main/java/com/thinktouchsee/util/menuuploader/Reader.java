package com.thinktouchsee.util.menuuploader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.api.core.pojo.*;
import uk.co.epicuri.api.core.EpicuriAPI;
import java.io.*;
import java.util.*;

/**
 * 28/08/2014
 */
public class Reader {
    private final Authentication authentication;

    private final List<MenuItem> menuItemList = new ArrayList<>();

    private final List<MenuItem> originalMenuItems;
    private final List<MenuItem> existingMenuItems;
    private final List<TaxRate> taxRates;
    private final List<Printer> printers;
    private List<Menu> menus;
    private final List<Service> services;
    private final EpicuriAPI api;

    public Reader(EpicuriAPI api, Authentication authentication) {
        this.authentication = authentication;

        this.api = api;
        taxRates = api.getVATTypes(authentication.getAuthKey());
        printers = api.getPrinterIds(authentication.getAuthKey());
        originalMenuItems = api.getMenuItems(authentication.getAuthKey());
        services = api.getServices(authentication.getAuthKey());
        existingMenuItems = new ArrayList<>(originalMenuItems);
        refreshMenusList();
    }

    private void refreshMenusList() {
        menus = api.getMenus(authentication.getAuthKey());
    }

    public void initiateRead(File file) throws IOException {
        // format
        // Name,Price,Description,Tax Type,Printer,Item,Type,Unavailable,Menu Structure,Courses

        read(file);
    }

    public List<MenuItem> getMenuItemList() {
        return menuItemList;
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

            if(firstLine) {
                firstLine = false;
                continue;
            }

            if(record.toString().isEmpty()) {
                continue;
            }

            //String[] bits = line.split(",");
            //if(bits.length < 8) {
            //    System.out.println("\tCannot parse above line");
            //    continue;
            //}

            String name = record.get(0).trim();

            String priceString = record.get(1).trim();
            if(priceString.matches("\\D.*")) {
                priceString = priceString.substring(1);
            }
            double price = Double.parseDouble(priceString);
            String description = record.get(2).trim();
            String taxType = record.get(3).trim();
            String printer = record.get(4).trim();
            ItemType itemType = getItemType(record.get(5).trim());
            boolean unavailable = getBoolean(record);
            String menuGroup = record.get(7).trim();

            if(isAlreadyInDB(name,price,description)) {
                System.out.println("\tDuplicate item, skipping line");
                continue;
            }

            String course = record.get(8).trim();
            //if(bits.length == 9) {
            //    course = bits[8].trim();
            //}

            if(StringUtils.isBlank(name)
                    || StringUtils.isBlank(taxType)
                    || StringUtils.isBlank(printer)) {
                System.out.println("\tBad line, skipping");
                continue;
            }

            try {
                MenuItem item = new MenuItem();
                item.setName(name);
                item.setPrice(price);
                item.setDescription(description);
                item.setTaxTypeId(getTaxId(taxType));
                item.setDefaultPrinter(getPrinterId(printer));
                item.setMenuItemTypeId(itemType.getId());
                item.setUnavailable(unavailable);

                //ids
                List<Integer> list = new ArrayList<>();
                list.add(determineStructureAndGetGroupId(menuGroup, course));

                item.setMenuGroups(list);
                if(!duped(item)) {
                    menuItemList.add(item);
                }
                else {
                    System.out.println("Skip this item, is duped: " + item.toString());
                }
            }
            catch (IllegalArgumentException ex) {
                System.out.println("\tAn identifier for "+name+" is wrong: "+ex.getMessage()+ " Ignoring this item, continuing...");
            }
            catch (Exception ex) {
                System.out.println("\t"+ex + "... cannot recover, exiting");
                System.exit(1);
            }
        }

        //reader.close();
    }

    private boolean getBoolean(CSVRecord record) {
        String bool = record.get(6).trim();
        if(StringUtils.isBlank(bool)) {
            return false;
        }
        if(bool.toLowerCase().equals("true")) {
            return true;
        }
        if(bool.toLowerCase().equals("false")) {
            return false;
        }
        return Boolean.valueOf(bool);
    }

    private boolean isAlreadyInDB(String name, double price, String description) {
        for(MenuItem item : existingMenuItems) {
            if(item.getName().equalsIgnoreCase(name) && almostEqual(item.getPrice(),price,0.001) && item.getDescription().equalsIgnoreCase(description)) {
                return true;
            }
        }

        return false;
    }

    public static boolean almostEqual(double a, double b, double eps){
        return Math.abs(a-b)<eps;
    }

    private boolean duped(MenuItem item) {
        boolean hasDupes = false;
        for(MenuItem currentItemInList : menuItemList) {
            if(currentItemInList.getName().equalsIgnoreCase(item.getName())
                    && almostEqual(currentItemInList.getPrice(),item.getPrice(),0.001)
                    && currentItemInList.getDescription().equalsIgnoreCase(item.getDescription())) {
                Set<Integer> set = new HashSet<>();
                set.addAll(currentItemInList.getMenuGroups());
                set.addAll(item.getMenuGroups());

                currentItemInList.setMenuGroups(new ArrayList<>(set));
                hasDupes = true;
            }
        }

        return hasDupes;
    }

    private ItemType getItemType(String type) {
        if(type.toLowerCase().contains("food")) {
            return ItemType.FOOD;
        }
        else if(type.toLowerCase().contains("drink")) {
            return ItemType.DRINK;
        }
        else {
            return ItemType.OTHER;
        }
    }

    private int getTaxId(String name) {
        for(TaxRate rate : taxRates) {
            if(rate.getName().equalsIgnoreCase(name)) {
                return rate.getId();
            }
        }
        System.out.println("\tTax name: "+name+" is invalid");
        throw new IllegalArgumentException("Tax type: " + name + " not recognised");
    }

    private int getPrinterId(String name) {
        for(Printer printer : printers) {
            if(printer.getName().equalsIgnoreCase(name)) {
                return printer.getId();
            }
        }
        System.out.println("\tPrinter name: "+name+" is invalid");
        throw new IllegalArgumentException("Printer: " + name + " not recognised");
    }

    private int determineStructureAndGetGroupId(String name, String course) {
        // split by >
        String[] bits = name.split(">");
        // make sure is length 3, if not, ignore this one
        if(bits.length != 3) {
            throw new IllegalArgumentException("Bad menu structure: \"" + name + "\", expected structure: Menu>Category>Group");
        }

        String menuName = bits[0].trim();
        String categoryName = bits[1].trim();
        String groupName = bits[2].trim();
        // if [1] menu doesn't exist, create a new menu
        if(getMenuId(menuName) < 0) {
            api.insertMenu(menuName,true,authentication.getAuthKey());
            refreshMenusList();
        }
        // if [1]..[2] category doesn't exist, create a new category
        if(getCategoryId(menuName, categoryName) < 0) {
            int orderIndex = 0;
            for(Menu menu : menus) {
                if(menu.getMenuName().equals(menuName)) {
                    orderIndex = menu.getMenuCategories().size();
                }
            }

            Category category = new Category();
            category.setCategoryName(categoryName);
            category.setMenuId(getMenuId(menuName));
            category.setOrder(orderIndex);
            category.setDefaultCourseIds(getCourseId(course));

            api.insertCategory(category,authentication.getAuthKey());
            refreshMenusList();
        }
        // if [1]..[2]..[3] group doesn't exist, create a new group
        if(getGroupId(menuName, categoryName, groupName) < 0) {
            int orderIndex = 0;
            for(Menu menu : menus) {
                if(menu.getMenuName().equals(menuName)) {
                    for(Menu.MenuCategory category : menu.getMenuCategories()) {
                        orderIndex = category.getMenuGroups().size();
                    }
                }
            }

            Menu.MenuCategory.MenuGroup menuGroup = new Menu.MenuCategory.MenuGroup();
            menuGroup.setGroupName(groupName);
            menuGroup.setMenuCategoryId(getCategoryId(menuName, categoryName));
            menuGroup.setOrder(orderIndex);
            api.insertGroup(menuGroup,authentication.getAuthKey());
            refreshMenusList();
        }

        return getGroupId(menuName,categoryName,groupName);
    }

    private int getMenuId(String menuName) {
        for(Menu menu : menus) {
            if(menu.getMenuName().equals(menuName)) {
                return menu.getId();
            }
        }

        return -1;
    }

    private int getCategoryId(String menuName, String category) {
        for(Menu menu : menus) {
            for(Menu.MenuCategory menuCategory : menu.getMenuCategories()) {
                if (menu.getMenuName().equals(menuName) && menuCategory.getCategoryName().equals(category)) {
                    return menuCategory.getId();
                }
            }
        }

        return -1;
    }

    private int getGroupId(String menuName, String categoryName, String groupName) {
        for(Menu menu : menus) {
            for(Menu.MenuCategory menuCategory : menu.getMenuCategories()) {
                for(Menu.MenuCategory.MenuGroup menuGroup : menuCategory.getMenuGroups()) {
                    if (menu.getMenuName().equals(menuName) && menuCategory.getCategoryName().equals(categoryName) && menuGroup.getGroupName().equals(groupName)) {
                        return menuGroup.getId();
                    }
                }
            }
        }

        return -1;
    }

    private List<Integer> getCourseId(String course) {
        return new ArrayList<>();//todo - fix below
        // if course is null, return an empty array
        /*if(StringUtils.isBlank(course)) {
            return new ArrayList<>();
        }

        // split course by ">" to denote ServiceName>Course. If no > in string, assume 1st match
        String serviceName = null;
        String courseName = course;
        if(course.contains(">")) {
            String[] bits = course.split(">");
            serviceName = bits[0].trim();
            courseName = bits[1].trim();
        }

        Set<Integer> set = new HashSet<>();
        OUTER:for(Service service : services) {
            for(Service.Course specifiedCourse : service.getCourses()) {
                if((serviceName == null && specifiedCourse.getName().equalsIgnoreCase(courseName))
                        || (serviceName != null && service.getServiceName().equals(serviceName) && specifiedCourse.getName().equalsIgnoreCase(courseName))) {
                    set.add(specifiedCourse.getId());
                    break OUTER;
                }
            }

        }
        return new ArrayList<>(set);*/
    }
}
