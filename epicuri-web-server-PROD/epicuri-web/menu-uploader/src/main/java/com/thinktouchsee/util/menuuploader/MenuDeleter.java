package com.thinktouchsee.util.menuuploader;

import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.Menu;
import uk.co.epicuri.api.core.pojo.MenuItem;

import java.io.File;
import java.util.List;

/**
 * Created by Manish Patel
 */
public class MenuDeleter {
    public static void main(String[] args) {
        if(args.length != 4) {
            System.out.println("Usage: <program> " + "<restaurantId> <username> <password> <PROD|DEV>");
        }

        String restaurantId = args[0].trim();
        String userName = args[1].trim();
        String password = args[2].trim();

        EpicuriAPI api;
        if(args[3].equalsIgnoreCase("PROD")) {
            System.getProperties().put("epicuri_env","prod");
            System.out.println("Connect to PRODUCTION database");
            api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        }
        else if(args[3].equalsIgnoreCase("STAGING")){
            System.getProperties().put("epicuri_env","staging");
            System.out.println("Connect to STAGING database");
            api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);
        }
        else {
            System.getProperties().put("epicuri_env","dev");
            System.out.println("Connect to DEV database");
            api = new EpicuriAPI(EpicuriAPI.Environment.DEV);
        }

        System.out.println("Attempt authentication: " + restaurantId + "/" + userName + "/" +password);

        Authentication login = api.login(restaurantId, userName, password);

        if(login == null) {
            System.out.println("Connection/auth error");
            System.exit(1);
        }

        if(!login.isManager()) {
            System.out.println("Cannot proceed with this login - need to be a manager!");
            System.exit(1);
        }

        System.out.println("Auth success");

        List<MenuItem> items = api.getMenuItems(login.getAuthKey());
        for(MenuItem item : items) {
            System.out.print("Item to delete: " + item.getId());
            api.deleteMenuItem(item.getId(), login.getAuthKey());
            System.out.println("...done");
        }
    }
}
