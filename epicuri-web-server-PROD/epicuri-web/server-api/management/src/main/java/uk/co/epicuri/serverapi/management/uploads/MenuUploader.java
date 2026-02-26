package uk.co.epicuri.serverapi.management.uploads;

import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.management.SSLUtil;
import uk.co.epicuri.serverapi.management.model.BaseURL;
import uk.co.epicuri.serverapi.management.model.Environment;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MenuUploader {
    public static void main(String[] args) {
        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            System.exit(0);
        }

        if(args.length != 5) {
            System.out.println("Usage: <program> " + "<restaurantId> <username> <password> <PROD2|PROD|DEV> <file>");
        }

        System.out.println("Wait for STOP in case you didn't mean to do this :)");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        File file = new File(args[4].trim());
        if(!file.exists()) {
            System.out.println("File does not exist or is not readable!");
            System.exit(1);
        }

        String restaurantId = args[0].trim();
        String userName = args[1].trim();
        String password = args[2].trim();

        WebService api = WebService.getWebService();
        if(args[3].equalsIgnoreCase("PROD")){
            System.out.println("Connect to NEW PRODUCTION database");
            api.setBaseURL(BaseURL.PROD_BASE_URL);
            api.setSelectedEnvironment(Environment.PROD);
        }
        else if(args[3].equalsIgnoreCase("DEV")){
            System.out.println("Connect to DEVELOPMENT database");
            api.setBaseURL(BaseURL.DEV_BASE_URL);
            api.setSelectedEnvironment(Environment.DEV);
        }
        else {
            throw new IllegalArgumentException("No environment");
        }

        System.out.println("Attempt authentication: " + restaurantId + "/" + userName + "/" +password);

        MenuUploader menuUploader = new MenuUploader(api, restaurantId);
        menuUploader.upload(file);
    }

    private WebService api;
    private String restaurantId;
    public MenuUploader(WebService api, String id) {
        this.api = api;
        this.restaurantId = id;
    }

    public List<String> upload(File file) {
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
        MenuStructureReader reader = new MenuStructureReader(api);
        try {
            reader.initiateRead(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<MenuItemView> items = reader.getMenuItemViewList();

        messages.add("Attempted insertion of " + items.size() + " menu items");
        for(MenuItemView item : items) {
            System.out.println("Item: " + item.getName());
            api.post(Endpoints.MENU_ITEM_PATH, item, MenuItemView.class);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        reader.ensureGroupMembers();

        return messages;
    }
}
