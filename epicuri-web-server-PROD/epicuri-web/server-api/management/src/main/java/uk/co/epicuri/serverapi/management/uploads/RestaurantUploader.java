package uk.co.epicuri.serverapi.management.uploads;

import uk.co.epicuri.serverapi.management.model.BaseURL;
import uk.co.epicuri.serverapi.management.model.Environment;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.File;
import java.io.IOException;

public class RestaurantUploader {
    public static void main(String[] args) throws IOException {
        if(args.length != 4) {
            System.out.println("file PROD|DEV userName password -- something is missing");
            System.exit(1);
        }
        File file = new File(args[0].trim());
        if(!file.exists()) {
            System.out.println("File does not exist");
            System.exit(1);
        }

        String environment = args[1].trim();
        String userName = args[2].trim();
        String password = args[3].trim();

        WebService api = WebService.getWebService();
        if(environment.equalsIgnoreCase("PROD")){
            System.out.println("Connect to NEW PRODUCTION database");
            api.setBaseURL(BaseURL.PROD_BASE_URL);
            api.setSelectedEnvironment(Environment.PROD);
        }
        else if(environment.equalsIgnoreCase("DEV")){
            System.out.println("Connect to DEVELOPMENT database");
            api.setBaseURL(BaseURL.DEV_BASE_URL);
            api.setSelectedEnvironment(Environment.DEV);
        }
        else {
            System.out.println("No environment");
            System.exit(0);
        }

        api.doLogin(userName, password);

        new RestaurantReader(file, api).read();
    }
}
