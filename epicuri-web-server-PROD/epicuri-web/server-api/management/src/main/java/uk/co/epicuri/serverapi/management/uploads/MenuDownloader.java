package uk.co.epicuri.serverapi.management.uploads;

import uk.co.epicuri.serverapi.common.pojo.host.HostPrinterView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTaxView;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryView;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.management.SSLUtil;
import uk.co.epicuri.serverapi.management.model.BaseURL;
import uk.co.epicuri.serverapi.management.model.Environment;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
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

        doCopy(api, id1);
    }

    private static void doCopy(WebService api, String id1) {
        StaffView staffView = api.doLogin(id1, "epicuriadmin", "keshavroshan");

        if(staffView == null) {
            System.out.println("ERROR: login did not work");
        }

        List<MenuView> asList = api.getAsList(Endpoints.MENU_PATH, MenuView.class);
        Map<String, HostPrinterView> printers = api.getAsList(Endpoints.PRINTER_PATH, HostPrinterView.class).stream().collect(Collectors.toMap(HostPrinterView::getId, Function.identity()));
        Map<String, HostTaxView> taxes = api.getAsList(Endpoints.TAX_PATH, HostTaxView.class).stream().collect(Collectors.toMap(HostTaxView::getId, Function.identity()));
        System.out.println("Item,Price,Description,Tax,Printer,Type,Unavailable,Menu Structure,Course");
        for(MenuView menuView : asList) {
            writeLines(menuView, printers, taxes);
        }
    }

    private static void writeLines(MenuView menuView, Map<String, HostPrinterView> printers, Map<String, HostTaxView> taxes) {
        for(CategoryView categoryView : menuView.getMenuCategories()) {
            for(GroupView groupView : categoryView.getMenuGroups()) {
                for(MenuItemView menuItemView : groupView.getMenuItems()) {
                    System.out.printf("%s,%.2f,%s,%s,%s,%s,%s,%s,,",
                            menuItemView.getName().contains(",") ? "\""+menuItemView.getName()+"\"" : menuItemView.getName(),
                            menuItemView.getPrice(),
                            menuItemView.getDescription().contains(",") ? "\""+menuItemView.getDescription()+"\"" : menuItemView.getDescription(),
                            taxes.get(menuItemView.getTaxTypeId()).getName(),
                            printers.get(menuItemView.getDefaultPrinter()).getName(),
                            ItemType.valueOf(menuItemView.getMenuItemTypeId()).toString(),
                            menuItemView.isUnavailable() ? "TRUE" : "FALSE",
                            menuView.getMenuName() + ">" + categoryView.getCategoryName() + ">" + groupView.getGroupName());
                    System.out.print("\n");
                }
            }
        }
    }
}
