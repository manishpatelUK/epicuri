package uk.co.epicuri.serverapi.management.uploads;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTaxView;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.management.SSLUtil;
import uk.co.epicuri.serverapi.management.model.BaseURL;
import uk.co.epicuri.serverapi.management.model.Environment;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MenuDownloader {
    public static void main(String[] args) {
        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            System.exit(0);
        }
        if(args.length != 1) {
            System.out.println("Usage: <program> " + "<restaurant id>");
        }

        String id1 = args[0];

        System.out.println("Download from " + id1);

        WebService api = WebService.getWebService();
        System.out.println("Connect to NEW PRODUCTION database");
        api.setBaseURL(BaseURL.PROD_BASE_URL);
        api.setSelectedEnvironment(Environment.PROD);

        StaffView staffView = api.doLogin(id1, "epicuriadmin", "keshavroshan");
        getMenus(api, staffView);
        System.out.println("-----------------------");
        getModifiers(api, staffView);
    }

    private static void getMenus(WebService api, StaffView staffView) {


        if(staffView == null) {
            System.out.println("ERROR: login did not work");
        }

        List<MenuView> asList = api.getAsList(Endpoints.MENU_PATH, MenuView.class);
        Map<String, HostPrinterView> printers = api.getAsList(Endpoints.PRINTER_PATH, HostPrinterView.class).stream().collect(Collectors.toMap(HostPrinterView::getId, Function.identity()));
        Map<String, HostTaxView> taxes = api.getAsList(Endpoints.TAX_PATH, HostTaxView.class).stream().collect(Collectors.toMap(HostTaxView::getId, Function.identity()));
        System.out.println("Item,Price,Description,Tax,Printer,Type,Unavailable,Menu Structure,Course,ModifierGroups");
        List<MenuItemView> allItems = api.getAsList(Endpoints.MENU_ITEM_PATH, MenuItemView.class);

        Set<String> itemsDone = new HashSet<>();
        for(MenuView menuView : asList) {
            itemsDone.addAll(writeLines(menuView, printers, taxes));
        }
        allItems.removeIf(m -> itemsDone.contains(m.getId()));
        for(MenuItemView menuItemView : allItems) {
            writeLine(null, printers, taxes, null, null, menuItemView);
        }
    }

    private static void getModifiers(WebService api, StaffView staffView) {

        if(staffView == null) {
            System.out.println("ERROR: login did not work");
        }

        List<ModifierGroupView> modifierGroupViews = api.getAsList(Endpoints.MODIFIER_GROUP_PATH, ModifierGroupView.class);
        System.out.println("GroupId,Name,LowerLimit,UpperLimit,ModifierValues");
        modifierGroupViews.forEach(m -> {
            System.out.printf("%s,%s,%s,%s,%s",
                    m.getId(),
                    m.getName().contains(",") ? "\"" + m.getName() + "\"" : m.getName(),
                    m.getLowerLimit(),
                    m.getUpperLimit(),
                    StringUtils.join(m.getModifiers().stream().map(ModifierView::getModifierValue).collect(Collectors.toList()),"|"));
            System.out.print("\n");
        });
    }

    private static List<String> writeLines(MenuView menuView, Map<String, HostPrinterView> printers, Map<String, HostTaxView> taxes) {
        List<String> itemsDone = new ArrayList<>();
        for(CategoryView categoryView : menuView.getMenuCategories()) {
            for(GroupView groupView : categoryView.getMenuGroups()) {
                for(MenuItemView menuItemView : groupView.getMenuItems()) {
                    writeLine(menuView, printers, taxes, categoryView, groupView, menuItemView);
                    itemsDone.add(menuItemView.getId());
                }
            }
        }
        return itemsDone;
    }

    private static void writeLine(MenuView menuView, Map<String, HostPrinterView> printers, Map<String, HostTaxView> taxes, CategoryView categoryView, GroupView groupView, MenuItemView menuItemView) {
        System.out.printf("%s,%.2f,%s,%s,%s,%s,%s,%s,%s,,",
                menuItemView.getName().contains(",") ? "\""+menuItemView.getName()+"\"" : menuItemView.getName(),
                menuItemView.getPrice(),
                (menuItemView.getDescription() != null && menuItemView.getDescription().contains(",")) ? "\""+menuItemView.getDescription()+"\"" : menuItemView.getDescription(),
                taxes.get(menuItemView.getTaxTypeId()).getName(),
                printers.get(menuItemView.getDefaultPrinter()).getName(),
                ItemType.valueOf(menuItemView.getMenuItemTypeId()).toString(),
                menuItemView.isUnavailable() ? "TRUE" : "FALSE",
                getMenuStructure(menuView, categoryView, groupView),
                StringUtils.join(menuItemView.getModifierGroups(),"|"));
        System.out.print("\n");
    }

    private static String getMenuStructure(MenuView menuView, CategoryView categoryView, GroupView groupView) {
        if(menuView == null) {
            return "";
        }
        return menuView.getMenuName() + ">" + categoryView.getCategoryName() + ">" + groupView.getGroupName();
    }
}
