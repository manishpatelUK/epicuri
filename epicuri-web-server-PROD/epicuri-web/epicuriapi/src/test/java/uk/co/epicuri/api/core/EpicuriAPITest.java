package uk.co.epicuri.api.core;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.co.epicuri.api.core.pojo.*;
import uk.co.epicuri.api.core.pojo.floor.Floor;
import uk.co.epicuri.api.core.pojo.floor.Layout;
import uk.co.epicuri.api.core.pojo.order.OrderItemRequest;
import uk.co.epicuri.api.core.pojo.session.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class EpicuriAPITest {

    public static String USERNAME = "epicuriadmin";
    public static String PASSWORD = "keshavroshan";
    public static String ID = "1";

    public static EpicuriAPI api;

    @BeforeClass
    public static void setup() throws Exception {
        api = new EpicuriAPI(EpicuriAPI.Environment.PROD,"4");
    }

    @Test
    public void testLogin() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        Assert.assertTrue(login != null);

/*        Authentication login2 = api.login(ID, USERNAME, "some random password");
        Assert.assertTrue(login2 == null);*/
    }

    @Test
    public void testGetPrinterIds() throws Exception{
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<Printer> list = api.getPrinterIds(login.getAuthKey());
        Assert.assertTrue(list.size()>0);
    }

    @Test
    public void testGetVATTypes() throws Exception{
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<TaxRate> list = api.getVATTypes(login.getAuthKey());
        Assert.assertTrue(list.size()>0);
    }

    @Test
    public void testGetMenuItems() throws Exception{
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<MenuItem> list = api.getMenuItems(login.getAuthKey());
        for(MenuItem item : list) {
            System.out.println(item);
        }
        Assert.assertTrue(list.size()>0);
    }

    @Test
    public void testMenuGroups() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<Menu.MenuCategory.MenuGroup> list = api.getGroups(login.getAuthKey());
        Assert.assertTrue(list.size()>0);
    }

    @Test
    public void testInsertMenuItem() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        MenuItem item = new MenuItem();
        item.setName("Test via console");
        item.setDescription("This is a test from java");
        item.setPrice(1.0);
        item.setDefaultPrinter(6);
        item.setMenuItemTypeId(ItemType.DRINK.getId());
        List<Integer> groups = new ArrayList<>();
        groups.add(2077);
        item.setMenuGroups(groups);
        item.setTaxTypeId(1);

        api.insertMenuItem(item,login.getAuthKey());
    }

    @Test
    public void testGetMenus() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<Menu> menus = api.getMenus(login.getAuthKey());
        //System.out.println(menus);
        System.out.println(menus.get(0).toString());
    }


    @Test
    public void testGetCourses() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<Service.Course> courses = api.getCourses(2,login.getAuthKey());
        Assert.assertTrue(courses.size()>0);
    }

    @Test
    public void testGetServices() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<Service> services = api.getServices(login.getAuthKey());
        Assert.assertTrue(services.size()>0);
    }

    @Test
    public void testReservation() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        Reservation reservation = new Reservation();
        reservation.setRestaurantId(Integer.valueOf(ID));
        reservation.setId(Integer.valueOf(ID));
        //todo
    }

    @Test
    public void testReservationRequest() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setName("Manish test");
        reservationRequest.setNotes("");
        reservationRequest.setNumberOfPeople(2);
        reservationRequest.setTelephone("0");
        //GregorianCalendar calendar = new GregorianCalendar(2015,4,22,20,30);
        //reservationRequest.setReservationTime(calendar.getTimeInMillis()/1000);
        //reservationRequest.setReservationTime((System.currentTimeMillis() + (1000*60*60*24))/1000);
        reservationRequest.setReservationTime(1435253400);
        Reservation newReq = api.createReservation(reservationRequest, login.getAuthKey(), ID,false);
        //Assert.assertFalse(newReq.isAccepted());
    }

    @Test
    public void testCreateReservationRequest() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setName("Manish test");
        reservationRequest.setNotes("foobar");
        reservationRequest.setNumberOfPeople(1);
        reservationRequest.setTelephone("07986669899");
        GregorianCalendar calendar = new GregorianCalendar(2015,4,9,21,0);
        //reservationRequest.setReservationTime(calendar.getTimeInMillis()/1000);
        reservationRequest.setReservationTime(1432405800L);
        api.createReservation(reservationRequest, login.getAuthKey(), ID,true);
    }

    @Test
    public void testGetReservations() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Reservation> reservations = api.getReservations(login.getAuthKey());
        System.out.println("Name,Time,NumberOfPeople,Phone,Notes");
        DateFormat format = new SimpleDateFormat("YYYY.MM.dd HH:mm");
        for(Reservation reservation : reservations) {
            if(reservation.isDeleted()) {
                continue;
            }

            long actualMillis = (long)reservation.getReservationTime() * 1000;
            Date date = new Date(actualMillis);
            System.out.println(reservation.getName()
                    + "," + format.format(date)
                    + "," + reservation.getNumberOfPeople()
                    + "," + reservation.getTelephone()
                    + "," + reservation.getNotes());
        }
    }

    @Test
    public void testGetRestaurantClient() throws Exception {
        Restaurant restaurant = api.getRestaurant(Integer.parseInt(ID));
        Assert.assertTrue(restaurant.getId() == 5);
    }

    @Test
    public void testDeleteMenuItem() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<MenuItem> items = api.getMenuItems(login.getAuthKey());

        Assert.assertTrue(items.size()>0);

        int idToDelete = items.get(0).getId();
        System.out.println("Try to delete: " + idToDelete);
        api.deleteMenuItem(idToDelete, login.getAuthKey());

        items = api.getMenuItems(login.getAuthKey());
        for(MenuItem item : items) {
            Assert.assertFalse(item.getId() == idToDelete);
        }
    }

    @Test
    public void testSimulateCashup() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        GregorianCalendar calendar1 = new GregorianCalendar(2015,1,1);
        GregorianCalendar calendar2 = new GregorianCalendar(2015,3,1);
        String cashup = api.simulateCashUp(calendar1.getTime(),calendar2.getTime(),login.getAuthKey());
        System.out.println(cashup);
    }

    @Test
    public void testGetSessions() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        SessionResponse response = api.getSessions(login.getAuthKey());
        Assert.assertTrue(response.getSessions().size() > 0);
    }

    @Test
    public void testGetSession() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Table> tables = api.getTables(login.getAuthKey());
        List<Service> services = api.getServices(login.getAuthKey());
        SessionResponse sessionResponse = api.getSessions(login.getAuthKey());
        Layout layout = api.getLayout(80,login.getAuthKey());

        // get a free table
        List<Integer> usedTables = new ArrayList<>();
        for(Session session : sessionResponse.getSessions()) {
            for(Table table : session.getTables()) {
                usedTables.add(table.getId());
            }
        }

        int freeTable = -1;
        for(Table table : layout.getTables()) {
            if(!usedTables.contains(table.getId())) {
                freeTable = table.getId();
                break;
            }
        }


        NewPartyRequest newPartyRequest = new NewPartyRequest();
        newPartyRequest.setName("From API");
        newPartyRequest.setCreateSession(true);
        newPartyRequest.setNumberOfPeople(1);
        newPartyRequest.setServiceId(services.get(0).getId());
        List<Integer> seated = new ArrayList<>();
        seated.add(freeTable);
        newPartyRequest.setTables(seated);

        NewPartyResponse response = api.addNewParty(newPartyRequest, login.getAuthKey());

        Session session = api.getSession(response.getSessionId(), login.getAuthKey());

        Assert.assertTrue(session.getId() == response.getSessionId());
    }

    @Test
    public void testAddOrder() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        SessionResponse response = api.getSessions(login.getAuthKey());
        Session openSession = null;
        for(Session session : response.getSessions()) {
            if(session.getClosedTime() < session.getStartTime()) {
                openSession = session;
                break;
            }
        }

        List<MenuItem> items = api.getMenuItems(login.getAuthKey());
        List<Service> services = api.getServices(login.getAuthKey());
        List<Service.Course> courses = api.getCourses(services.get(0).getId(), login.getAuthKey());
        OrderItemRequest request = new OrderItemRequest();
        request.setDinerId(openSession.getDiners().get(0).getId());
        request.setInstantiatedFromId(0);
        request.setMenuItemId(items.get(0).getId());
        request.setQuantity(2);
        request.setCourseId(courses.get(0).getId());
        List<OrderItemRequest> requests = new ArrayList<>();
        requests.add(request);
        api.addOrders(requests, login.getAuthKey());
    }

    @Test
    public void testGetTables() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        List<Table> tables = api.getTables(login.getAuthKey());
        Assert.assertTrue(tables.size()>0);
    }

    @Test
    public void testAddNewParty() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Table> tables = api.getTables(login.getAuthKey());
        List<Service> services = api.getServices(login.getAuthKey());
        SessionResponse sessionResponse = api.getSessions(login.getAuthKey());
        Layout layout = api.getLayout(80,login.getAuthKey());

        // get a free table
        List<Integer> usedTables = new ArrayList<>();
        for(Session session : sessionResponse.getSessions()) {
            for(Table table : session.getTables()) {
                usedTables.add(table.getId());
            }
        }

        int freeTable = -1;
        for(Table table : layout.getTables()) {
            if(!usedTables.contains(table.getId())) {
                freeTable = table.getId();
                break;
            }
        }

        System.out.println("Try to seat on table " + freeTable);

        NewPartyRequest newPartyRequest = new NewPartyRequest();
        newPartyRequest.setName("From API");
        newPartyRequest.setCreateSession(true);
        newPartyRequest.setNumberOfPeople(1);
        newPartyRequest.setServiceId(services.get(0).getId());
        List<Integer> seated = new ArrayList<>();
        seated.add(freeTable);
        newPartyRequest.setTables(seated);

        NewPartyResponse response = api.addNewParty(newPartyRequest, login.getAuthKey());
    }

    @Test
    public void testAddNewUnSeatedParty() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Table> tables = api.getTables(login.getAuthKey());
        List<Service> services = api.getServices(login.getAuthKey());
        SessionResponse sessionResponse = api.getSessions(login.getAuthKey());

        // get a free table
        List<Integer> usedTables = new ArrayList<>();
        for(Session session : sessionResponse.getSessions()) {
            for(Table table : session.getTables()) {
                usedTables.add(table.getId());
            }
        }

        NewPartyRequest newPartyRequest = new NewPartyRequest();
        newPartyRequest.setName("From API");
        newPartyRequest.setCreateSession(true);
        newPartyRequest.setNumberOfPeople(1);
        newPartyRequest.setServiceId(services.get(0).getId());
        List<Integer> seated = new ArrayList<>();
        newPartyRequest.setTables(seated);

        api.addNewUnseatedParty(newPartyRequest, login.getAuthKey());
    }

    @Test
    public void testCloseSession() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Table> tables = api.getTables(login.getAuthKey());
        List<Service> services = api.getServices(login.getAuthKey());
        SessionResponse sessionResponse = api.getSessions(login.getAuthKey());
        Layout layout = api.getLayout(80,login.getAuthKey());

        // get a free table
        List<Integer> usedTables = new ArrayList<>();
        for(Session session : sessionResponse.getSessions()) {
            for(Table table : session.getTables()) {
                usedTables.add(table.getId());
            }
        }

        int freeTable = -1;
        for(Table table : layout.getTables()) {
            if(!usedTables.contains(table.getId())) {
                freeTable = table.getId();
                break;
            }
        }


        NewPartyRequest newPartyRequest = new NewPartyRequest();
        newPartyRequest.setName("Session immediately closed");
        newPartyRequest.setCreateSession(true);
        newPartyRequest.setNumberOfPeople(1);
        newPartyRequest.setServiceId(services.get(0).getId());
        List<Integer> seated = new ArrayList<>();
        seated.add(freeTable);
        newPartyRequest.setTables(seated);

        NewPartyResponse response = api.addNewParty(newPartyRequest, login.getAuthKey());

        api.forceCloseSession(response.getSessionId(),false,login.getAuthKey());
    }

    @Test
    public void testGetFloors() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Floor> floors = api.getFloors(login.getAuthKey());
        Assert.assertTrue(floors.size()>0);
    }

    @Test
    public void testGetLayout() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        Layout layout = api.getLayout(80,login.getAuthKey());
        Assert.assertTrue(layout != null);
    }

    @Test
    public void testRequestBillAndReopen() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);

        List<Table> tables = api.getTables(login.getAuthKey());
        List<Service> services = api.getServices(login.getAuthKey());
        SessionResponse sessionResponse = api.getSessions(login.getAuthKey());
        Layout layout = api.getLayout(80,login.getAuthKey());

        // get a free table
        List<Integer> usedTables = new ArrayList<>();
        for(Session session : sessionResponse.getSessions()) {
            for(Table table : session.getTables()) {
                usedTables.add(table.getId());
            }
        }

        int freeTable = -1;
        for(Table table : layout.getTables()) {
            if(!usedTables.contains(table.getId())) {
                freeTable = table.getId();
                break;
            }
        }

        System.out.println("Try to seat on table " + freeTable);

        NewPartyRequest newPartyRequest = new NewPartyRequest();
        newPartyRequest.setName("From API");
        newPartyRequest.setCreateSession(true);
        newPartyRequest.setNumberOfPeople(1);
        newPartyRequest.setServiceId(services.get(0).getId());
        List<Integer> seated = new ArrayList<>();
        seated.add(freeTable);
        newPartyRequest.setTables(seated);

        NewPartyResponse response = api.addNewParty(newPartyRequest, login.getAuthKey());
        Session session = api.getSession(response.getSessionId(),login.getAuthKey());

        List<MenuItem> items = api.getMenuItems(login.getAuthKey());
        OrderItemRequest request = new OrderItemRequest();
        request.setCourseId(services.get(0).getCourses().get(0).getId());
        request.setDinerId(session.getDiners().get(0).getId());
        request.setMenuItemId(items.get(0).getId());
        request.setNote("");
        request.setQuantity(1);
        List<OrderItemRequest> requests = new ArrayList<>();
        requests.add(request);
        api.addOrders(requests,login.getAuthKey());

        api.requestBill(response.getSessionId(), login.getAuthKey());
        api.reopenSession(response.getSessionId(), login.getAuthKey());
    }

    @Test
    public void requestBill() throws Exception {
        Authentication login = api.login(ID, USERNAME, PASSWORD);
        api.requestBill(3323,login.getAuthKey());
    }

    /*@Test void testDeleteAllMenuItems() throws Exception {}*/
    /*@Test
    public void testAddCategory() throws Exception {
        Authentication login = EpicuriAPI.login(ID, USERNAME, PASSWORD);
        List<Course> courses = EpicuriAPI.getCourses(2,login.getAuthKey());
        Category category = new Category();
        category.setCategoryName("New Category");
        category.setMenuId(getMenuId(menuName));
        category.setOrder(orderIndex);
        category.setDefaultCourseIds(getCourseIds(menuName,categoryName));
    }*/

    /*@Test
    public void testAddMenus() throws Exception {
        Authentication login = EpicuriAPI.login(ID, USERNAME, PASSWORD);
        EpicuriAPI.insertMenu("TestMenu",true,login.getAuthKey());
    }*/

    /*@Test
    public void testToPojo() throws Exception {
        String json = "{\"Id\":1098,\"DefaultPrinter\":6,\"Name\":\"Horse\",\"Price\":55.0,\"Description\":\"Horse long description \",\"ImageUrl\":\"\",\"DefaultCourses\":null,\"ModifierGroups\":[9],\"MenuGroups\":null,\"TaxTypeId\":1,\"Tags\":[],\"TagIds\":[],\"MenuItemTypeId\":0,\"Unavailable\":false}";
        EpicuriAPI.toPojoObject(json,MenuItem.class);
    }
*/

}