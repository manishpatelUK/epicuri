package uk.co.epicuri.serverapi.management.uploads;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierGroupView;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModifierUpdater {
    public static void main(String[] args) throws Exception{
        MenuUploader.turnOffSsl();

        if(args.length != 5) {
            System.out.println("Usage: <program> " + "<restaurantId> <username> <password> <PROD2|PROD|DEV> <file>");
        }

        MenuUploader.waitForKill();

        File file = MenuUploader.getFile(args[4]);
        String restaurantId = args[0].trim();
        String userName = args[1].trim();
        String password = args[2].trim();

        WebService api = MenuUploader.getWebService(args, restaurantId, userName, password);
        ModifierUpdater modifierUpdater = new ModifierUpdater(api, restaurantId);
        modifierUpdater.upload(file);
    }

    private WebService api;
    private String restaurantId;
    public ModifierUpdater(WebService api, String id) {
        this.api = api;
        this.restaurantId = id;
    }

    public List<String> upload(File file) throws IOException {
        List<String> messages = new ArrayList<>();
        StaffView staffView = api.doLogin(restaurantId, "epicuriadmin", "keshavroshan");

        if(staffView == null) {
            messages.add("Connection/auth error: does the restaurant have the admin staff member set up?");
            return messages;
        }

        if(!staffView.isManager()) {
            messages.add("Cannot proceed with this login - need to be a manager!");
            return messages;
        }

        System.out.println("Auth success. Rock on. Read file " + file.getAbsolutePath() + File.separator + file.getName());
        FileReader in = new FileReader(file);

        Map<String, MenuItemView> menuItemViewMap = new HashMap<>();
        List<String> menuItemViewNameMap = new ArrayList<>();
        List<MenuItemView> menuItems = api.getAsList(Endpoints.MENU_ITEM_PATH, MenuItemView.class);
        for(MenuItemView menuItemView : menuItems) {
            menuItemViewMap.put(menuItemView.getId(), menuItemView);
            menuItemViewNameMap.add(menuItemView.getName());
        }

        Map<String, ModifierGroupView> modifierViewMap = new HashMap<>();
        List<ModifierGroupView> modifierGroupViews = api.getAsList(Endpoints.MODIFIER_GROUP_PATH, ModifierGroupView.class);
        for(ModifierGroupView modifierGroupView : modifierGroupViews) {
            modifierViewMap.put(modifierGroupView.getName(), modifierGroupView);
        }

        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        boolean firstLine = true;
        Map<String,MenuItemView> modified = new HashMap<>();
        for(CSVRecord record : records) {
            System.out.println(record.toString());
            if (firstLine) {
                firstLine = false;
                continue;
            }

            if (record.toString().isEmpty()) {
                continue;
            }

            String modifierName = record.get(0).trim();
            String menuItemName = record.get(1).trim();

            if(!menuItemViewNameMap.contains(menuItemName)) {
                System.out.println("SKIP this line - menu item not found");
                continue;
            }
            if(!modifierViewMap.containsKey(modifierName)) {
                System.out.println("SKIP this line - modifier not found");
                continue;
            }

            List<MenuItemView> menuItemViews = find(menuItemName, menuItemViewMap);

            for(MenuItemView menuItemView : menuItemViews) {
                ModifierGroupView modifierGroupView = modifierViewMap.get(modifierName);
                if(!menuItemView.getModifierGroups().contains(modifierGroupView.getId())) {
                    menuItemView.getModifierGroups().add(modifierGroupView.getId());
                    if(!modified.containsKey(menuItemView.getId())) {
                        modified.put(menuItemView.getId(), menuItemView);
                    }
                }
            }
        }

        messages.add("Attempted insertion of " + modified.size() + " menu items");
        for(String key : modified.keySet()) {
            MenuItemView item = modified.get(key);
            System.out.println("Item: " + item.getName());
            api.put(Endpoints.MENU_ITEM_PATH + "/" + key, item);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    private List<MenuItemView> find(String menuItemName, Map<String, MenuItemView> menuItemViewMap) {
        List<MenuItemView> list = new ArrayList<>();
        for(Map.Entry<String,MenuItemView> key : menuItemViewMap.entrySet()) {
            if(key.getValue() != null && key.getValue().getName() != null && key.getValue().getName().equals(menuItemName)) {
                list.add(key.getValue());
            }
        }
        return list;
    }
}
