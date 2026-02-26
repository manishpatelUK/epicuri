package com.thinktouchsee.util.menuuploader;

import uk.co.epicuri.api.core.pojo.*;
import uk.co.epicuri.api.core.EpicuriAPI;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 28/08/2014
 */
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

        File file = new File(args[4].trim());
        if(!file.exists()) {
            System.out.println("File does not exist or is not readable!");
            System.exit(1);
        }

        String restaurantId = args[0].trim();
        String userName = args[1].trim();
        String password = args[2].trim();

        EpicuriAPI api;
        if(args[3].equalsIgnoreCase("PROD")) {
            throw new IllegalArgumentException("No longer allowed to add to old production");
        }
        else if(args[3].equalsIgnoreCase("PROD2")){
            System.getProperties().put("epicuri_env","prod2");
            System.out.println("Connect to NEW PRODUCTION database");
            api = new EpicuriAPI(EpicuriAPI.Environment.PROD2);
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

        System.out.println("Auth success. Rock on. Read file " + file.getAbsolutePath() + File.separator + file.getName());
        Reader reader = new Reader(api, login);
        try {
            reader.initiateRead(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<MenuItem> items = reader.getMenuItemList();

        System.out.println("Attempt insertion of " + items.size() + " menu items");
        for(MenuItem item : items) {
            api.insertMenuItem(item,login.getAuthKey());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
