package uk.co.epicuri.serverapi.management.uploads;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RestaurantReader {
    private File file;
    private WebService api;

    public RestaurantReader(File file, WebService api) {
        this.file = file;
        this.api = api;
    }

    public void read() throws IOException {
        FileReader in = new FileReader(file);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

        boolean firstLine = true;
        for (CSVRecord record : records) {
            if(firstLine) {
                firstLine = false;
                continue;
            }
            if (record.toString().isEmpty()) {
                continue;
            }
            System.out.println(record.toString());

            Restaurant restaurant = readRestaurant(record);
            if(restaurant != null) {
                api.post(Endpoints.MANAGEMENT + "/" + Restaurant.class.getCanonicalName(), restaurant, Restaurant.class);
            }
        }

    }

    public Restaurant readRestaurant(CSVRecord record) {
        Restaurant restaurant = new Restaurant();
        restaurant.setInternalEmailAddress(record.get(1).trim());
        restaurant.setPhoneNumber2(record.get(2).trim());
        restaurant.setName(record.get(3).trim());
        restaurant.setPublicEmailAddress(record.get(4).trim());
        restaurant.setPhoneNumber1(record.get(5).trim());

        Address address = new Address();
        address.setStreet(record.get(6).trim());
        address.setCity(record.get(7).trim());
        address.setPostCode(record.get(8).trim());
        restaurant.setAddress(address);

        restaurant.setWebsite(record.get(10).trim());
        restaurant.setDescription(record.get(11).trim());

        //assume currency and timezone
        restaurant.setISOCurrency("GBP");
        restaurant.setIANATimezone("Europe/London");

        //enable for diner
        restaurant.setEnabledForDiner(true);

        return restaurant;
    }
}
