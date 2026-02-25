package uk.co.epicuri.api.core;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import uk.co.epicuri.api.core.pojo.*;
import uk.co.epicuri.api.core.pojo.floor.Floor;
import uk.co.epicuri.api.core.pojo.floor.Layout;
import uk.co.epicuri.api.core.pojo.order.OrderItemRequest;
import uk.co.epicuri.api.core.pojo.session.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * 28/08/2014
 */
public class EpicuriAPI {
    //public static String API_PATH = "https://api.epicuri.co.uk/cpe/api";
    //public static String API_PATH = "http://79.170.44.205/epicuri.co.uk/cpe/api";
    public static String LOGIN_PATH = "/Authentication/Login";
    public static String PRINTER_PATH = "/Printer";
    public static String TAX_PATH = "/TaxType";
    public static String MENU_ITEM_PATH = "/MenuItem";
    public static String MENU_PATH = "/Menu";
    public static String COURSE_PATH = "/Course";
    public static String MENU_CATEGORY_PATH = "/MenuCategory";
    public static String MENU_GROUP_PATH = "/MenuGroup";
    public static String RESERVATION_PATH = "/Reservation";
    public static String RESERVATION_CHECK_PATH = "/Reservation/ReservationCheck";
    public static String RESTAURANT_PATH = "/Restaurant";
    public static String SIMULATE_CASHUP = "/CashUp/Simulate";
    public static String SESSION = "/Session";
    public static String ORDER = "/Order";
    public static String TABLE = "/Table";
    public static String NEW_PARTY = "/Waiting";
    public static String FLOOR = "/Floor";
    public static String LAYOUT = "/Layout";

    private static Gson gson = new Gson();
    private static final JsonParser parser = new JsonParser();

    public enum Environment {PROD,PROD2,DEV,STAGING}
    private Environment environment;
    private String apiVersion = "3";

    public EpicuriAPI(Environment environment) {
        this.environment = environment;
    }
    public EpicuriAPI(Environment environment, String apiVersion) {
        this.environment = environment;
        this.apiVersion = apiVersion;
    }

    public String getApiPath() {
        if(environment.equals(Environment.PROD)) {
            return "https://api.epicuri.co.uk/cpe/api";
        }
        else if(environment.equals(Environment.PROD2)) {
            return "https://api-prod.epicuri.co.uk";
        }
        else if(environment.equals(Environment.DEV)) {
            return "http://79.170.44.205/epicuri.co.uk/cpe/api";
        }
        else if(environment.equals(Environment.STAGING)) {
            return "http://api.staging.epicuri.co.uk/cpe/api";
        }

        return "";

    }

    public String getClientPath() {
        if(environment.equals(Environment.PROD)) {
            return "https://api.epicuri.co.uk/client/api";
        }
        else if(environment.equals(Environment.DEV)) {
            return "http://79.170.44.205/epicuri.co.uk/client/api";
        }
        else if(environment.equals(Environment.STAGING)) {
            return "http://api.staging.epicuri.co.uk/client/api";
        }

        return "";

    }

    public Authentication login(String restaurantId, String userName, String password) {
        Login login = new Login();
        login.setRestaurantId(restaurantId);
        login.setUsername(userName);
        login.setPassword(password);


        String json = toJson(login);

        try {
            String response = callAPI("GET", getApiPath() + EpicuriAPI.LOGIN_PATH, json, null);
            if(StringUtils.isBlank(response) || response.startsWith("error") ) {
                return null;
            }
            Authentication authentication = toPojoObject(response, Authentication.class);
            authentication.setAuthKey("Basic " + authentication.getAuthKey());
            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Will exit, cannot auth");
            return null;
        }
    }

    public List<Printer> getPrinterIds(String token) {
        System.out.println("Get all printers");
        List<Printer> list = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + PRINTER_PATH, null, token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                Printer printer = toPojoObject(element.toString(),Printer.class);
                if(printer != null) {
                    list.add(printer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return list;
        }

        return list;
    }

    public List<TaxRate> getVATTypes(String token) {
        System.out.println("Get all tax rates");
        List<TaxRate> list = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + TAX_PATH,null,token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                TaxRate rate = toPojoObject(element.toString(),TaxRate.class);
                if(rate != null) {
                    list.add(rate);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Service.Course> getCourses(int service,String token) {
        System.out.println("Get Courses for Service Id " + service);
        List<Service.Course> list = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + COURSE_PATH + "/" + service,null,token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                Service.Course course = toPojoObject(element.toString(),Service.Course.class);
                if(course != null) {
                    list.add(course);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public List<Service> getServices(String token) {
        System.out.println("Get Services");
        List<Service> list = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + "/Service" ,null,token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                Service service = toPojoObject(element.toString(),Service.class);
                if(service != null) {
                    list.add(service);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<MenuItem> getMenuItems(String token) {
        System.out.println("Get menu items");
        List<MenuItem> list = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + MENU_ITEM_PATH,null,token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                MenuItem menuItem = toPojoObject(element.toString(),MenuItem.class);
                if(menuItem != null) {
                    list.add(menuItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public List<Menu> getMenus(String token) {
        System.out.println("Get menus");
        List<Menu> menus = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + MENU_PATH,null,token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                Menu menu = toPojoObject(element.toString(),Menu.class);
                if(menu != null) {
                    menus.add(menu);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return menus;
    }

    public List<Menu.MenuCategory.MenuGroup> getGroups(String token) {
        System.out.println("Get groups");
        List<Menu.MenuCategory.MenuGroup> menuGroups = new ArrayList<>();
        try {
            String response = callAPI("GET", getApiPath() + MENU_PATH,null,token);
            JsonArray array = parser.parse(response).getAsJsonArray();
            for(JsonElement element : array) {
                JsonArray categories = element.getAsJsonObject().get("MenuCategories").getAsJsonArray();
                for(JsonElement category : categories) {
                    JsonArray groups = category.getAsJsonObject().get("MenuGroups").getAsJsonArray();
                    for(JsonElement group : groups) {
                        Menu.MenuCategory.MenuGroup menuGroup = toPojoObject(group.toString(),Menu.MenuCategory.MenuGroup.class);
                        menuGroups.add(menuGroup);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return menuGroups;
    }

    public void insertMenuItem(MenuItem item, String token) {
        JsonElement element = toJsonTree(item);
        element.getAsJsonObject().remove("Id");
        String json = element.toString();
        System.out.println("\tInsert menu item: " + json);

        try {
            String response = callAPI("POST", getApiPath() + MENU_ITEM_PATH, json, token);
            if(response.matches("4\\d\\d") || response.matches("5\\d\\d")) {
                System.out.println("\tError (" + response + "): POST " + item);
            }
            else System.out.println("\tSuccess: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertMenu(String menuName, boolean active, String token) {
        JsonObject object = new JsonObject();
        object.addProperty("MenuName",menuName);
        object.addProperty("Active", active);
        System.out.println("\tInsert menu: " + object.toString());
        try {
            String response = callAPI("POST", getApiPath() + MENU_PATH, object.toString(), token);
            if(response.matches("4\\d\\d") || response.matches("5\\d\\d")) {
                System.out.println("\tError (" + response + "): POST " + object.toString());
            }
            else System.out.println("\tSuccess: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertCategory(Category category, String token) {
        JsonElement element = toJsonTree(category);
        String json = element.toString();
        System.out.println("\tInsert category: " + json);
        try {
            String response = callAPI("POST", getApiPath() + MENU_CATEGORY_PATH, json, token);
            if(response.matches("4\\d\\d") || response.matches("5\\d\\d")) {
                System.out.println("\tError (" + response + "): POST " + json);
            }
            else System.out.println("\tSuccess: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Restaurant getRestaurant(int id) throws IOException {
        String json = callAPI("GET", getClientPath() + RESTAURANT_PATH + "/" + id, null, null);
        // check for error
        if(json.contains("An error has occurred")) {
            throw new IOException("No such restaurant");
        }

        return gson.fromJson(json, Restaurant.class);
    }

    public Reservation createReservation(ReservationRequest request, String token, String restaurantId, boolean submit) {
        JsonElement element = toJsonTree(request);
        // manual labor
        JsonObject jsonObject = element.getAsJsonObject();
        if(jsonObject.has("LeadCustomerId")) {
            jsonObject.remove("LeadCustomerId");
        }

        String json = jsonObject.toString();
        System.out.println("\t"  + (submit ? "Create" : "Check") + " reservation: " + json);

        Reservation reservation = new Reservation();
        reservation.setTelephone(request.getTelephone());
        reservation.setNotes(request.getNotes());
        reservation.setRestaurantId(Integer.parseInt(restaurantId));

        String method = "POST";

        try {
            String response = callAPI(method,
                                      getApiPath() +
                                              (submit ? RESERVATION_PATH : RESERVATION_CHECK_PATH),
                                              json, token);
            if(response.matches("4\\d\\d") || response.matches("5\\d\\d")) {
                System.out.println("\tError (" + response + "): "+method+" " + json);

                reservation.setRejected(true);
                reservation.setAccepted(false);
                reservation.setRejectionNotice("Booking system is unavailable");
            }
            else {
                System.out.println("\tSuccess: " + response);

                JsonObject responseJson = parser.parse(response).getAsJsonObject();
                if(responseJson.has("Warning") && responseJson.get("Warning").getAsJsonArray().size() > 0) {
                    reservation.setRejected(true);
                    reservation.setAccepted(false);
                    String reasons = "";
                    Iterator<JsonElement> warnings = responseJson.get("Warning").getAsJsonArray().iterator();
                    while(warnings.hasNext()) {
                        reasons += warnings.next().getAsString() + "|";
                    }
                    reservation.setRejectionNotice(reasons);
                }
                else {
                    //create a reservation
                    reservation.setRejected(false);
                    reservation.setAccepted(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reservation;
    }

    public List<Reservation> getReservations(String token) {
        String path = getApiPath() + RESERVATION_PATH;
                //+ "?fromTime=" + String.valueOf(reservationQuery.getFromTime())
                //+ "&toTime=" + String.valueOf(reservationQuery.getToTime());
        List<Reservation> reservations = new ArrayList<>();
        try{
            String response = callAPI("GET",path, null, token);
            JsonArray responseJson = parser.parse(response).getAsJsonArray();
            for(JsonElement element : responseJson) {
                reservations.add(toPojoObject(element.toString(), Reservation.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public List<Reservation> getReservations(String token, String fromTime, String toTime, boolean pendingWaiterAction) {
        String path = getApiPath() + RESERVATION_PATH + "?fromTime=" + fromTime + "&toTime="+toTime+"&pendingWaiterAction=false";
        List<Reservation> reservations = new ArrayList<>();
        try{
            String response = callAPI("GET", path, null, token);
            JsonArray responseJson = parser.parse(response).getAsJsonArray();
            for(JsonElement element : responseJson) {
                reservations.add(toPojoObject(element.toString(), Reservation.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reservations;
    }


    public void insertGroup(Menu.MenuCategory.MenuGroup menuGroup, String token) {
        JsonElement element = toJsonTree(menuGroup);
        String json = element.toString();
        System.out.println("\tInsert group: " + json);
        try {
            String response = callAPI("POST", getApiPath() + MENU_GROUP_PATH, json, token);
            if(response.matches("4\\d\\d") || response.matches("5\\d\\d")) {
                System.out.println("\tError (" + response + "): POST " + json);
            }
            else System.out.println("\tSuccess: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteMenuItem(int menuItemId, String token) {
        try {
            callAPI("DELETE", getApiPath() + MENU_ITEM_PATH + "/" + menuItemId,null,token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String simulateCashUp(Date from, Date to, String token) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("StartTime", from.getTime() / 1000l);
        jsonObject.addProperty("EndTime", to.getTime() / 1000l);
        try {
            return callAPI("POST", getApiPath() + SIMULATE_CASHUP, jsonObject.toString(), token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public SessionResponse getSessions(String token) {
        String response = "";
        try {
            response = callAPI("GET", getApiPath() + SESSION, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SessionResponse sessionResponse = new SessionResponse();
        JsonArray array = parser.parse(response).getAsJsonArray();
        for(JsonElement element : array) {
            Session session = toPojoObject(element.toString(), Session.class);

            if(session != null) {
                sessionResponse.getSessions().add(session);
            }
        }
        return sessionResponse;
    }

    public Session getSession(int id, String token) {
        String response = "";
        try {
            response = callAPI("GET", getApiPath() + SESSION + "/" + id, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return toPojoObject(response,Session.class);
    }

    public void addOrders(List<OrderItemRequest> orders, String token) {
        JsonArray array = new JsonArray();
        for(OrderItemRequest orderItemRequest : orders) {
            array.add(toJsonTree(orderItemRequest));
        }
        String response = "";
        try {
            response = callAPI("POST", getApiPath() + ORDER, array.toString(), token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Table> getTables(String token) {
        String response = null;
        try {
            response = callAPI("GET", getApiPath() + TABLE, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Table> tables = new ArrayList<>();
        JsonArray array = parser.parse(response).getAsJsonArray();
        for(JsonElement element : array) {
            Table table = toPojoObject(element.toString(),Table.class);
            if(table != null) {
                tables.add(table);
            }
        }
        return tables;
    }

    public NewPartyResponse addNewParty(NewPartyRequest request, String token) {
        String response = null;
        try {
            response = callAPI("POST", getApiPath() + NEW_PARTY, toJson(request), token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return toPojoObject(response,NewPartyResponse.class);
    }

    public NewPartyResponse addNewUnseatedParty(NewPartyRequest request, String token) {
        request.getTables().clear();
        return addNewParty(request, token);
    }

    public List<Floor> getFloors(String token) {
        String response = null;
        try {
            response = callAPI("GET", getApiPath() + FLOOR, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Floor> floors = new ArrayList<>();
        JsonArray array = parser.parse(response).getAsJsonArray();
        for(JsonElement element : array) {
            Floor floor = toPojoObject(element.toString(),Floor.class);
            if(floor != null) {
                floors.add(floor);
            }
        }
        return floors;
    }

    public Layout getLayout(int layout, String token) {
        String response = null;
        try {
            response = callAPI("GET", getApiPath() + LAYOUT + "/" + layout, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return toPojoObject(response,Layout.class);
    }

    public void forceCloseSession(int sessionId, boolean applyBlackMarks, String token) {
        JsonObject body = new JsonObject();
        body.add("GiveBlackMark", new JsonPrimitive(applyBlackMarks));
        try {
            callAPI("PUT", getApiPath() + SESSION+ "/Close/" + sessionId, body.toString(), token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestBill(int sessionId, String token) {
        try {
            callAPI("PUT", getApiPath() + SESSION+ "/RequestBill/" + sessionId, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reopenSession(int sessionId, String token) {
        try {
            callAPI("PUT", getApiPath() + SESSION+ "/Reopen/" + sessionId, null, token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String callAPI(String method, String path, String json, String token) throws IOException {
        System.out.println("\t\tCall path: " + path);
        URL url = new URL(path);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setReadTimeout(20000);
            urlConnection.setConnectTimeout(8000);
            urlConnection.setUseCaches(false);
            String userAgent = "EpicuriWaiter/0";
            urlConnection.setRequestProperty(HTTP.USER_AGENT, userAgent);
            urlConnection.setRequestProperty(HTTP.CONTENT_TYPE, "application/json");
            urlConnection.setRequestProperty("X-Epicuri-API-Version", apiVersion);

            if (StringUtils.isNotBlank(token)) {
                urlConnection.setRequestProperty("Authorization", token);
            }

            if(json != null) {
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                HttpEntity entity = new StringEntity(json, "UTF-8");
                InputStream requestBody = new BufferedInputStream(entity.getContent());
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                byte[] buffer = new byte[1024];
                int available = -1;
                while (0 < (available = requestBody.read(buffer))) {
                    out.write(buffer, 0, available);
                }
                out.flush();
                out.close();
            }

            urlConnection.connect();

            if(urlConnection.getResponseCode() > 400 || urlConnection.getErrorStream() != null) {
                return String.valueOf(urlConnection.getResponseCode());
            }

            return streamToString(urlConnection.getInputStream());

        } catch(Exception ex) {
            ex.printStackTrace();
            return "error";
        }
        finally {
            assert urlConnection != null;
            urlConnection.disconnect();
        }
    }

    public static String toJson(Object pojo) {
        return gson.toJson(pojo);
    }

    public static JsonElement toJsonTree(Object pojo) {
        return gson.toJsonTree(pojo);
    }

    public static  <T> T toPojoObject(String json, Class<T> clazz) {
        return gson.fromJson(parser.parse(json).getAsJsonObject(), clazz);
    }

    private String streamToString(InputStream in){
        InputStream bufferedin = new BufferedInputStream(in);
        String response = null;
        Scanner scanner = new Scanner(bufferedin, "UTF-8").useDelimiter("\\A");
        if (scanner.hasNext()){
            response = scanner.next();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
