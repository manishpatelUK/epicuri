package uk.co.epicuri.bookingapi;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.Authentication;
import uk.co.epicuri.api.core.pojo.floor.Floor;
import uk.co.epicuri.api.core.pojo.floor.Layout;
import uk.co.epicuri.api.core.pojo.session.Session;
import uk.co.epicuri.api.core.pojo.session.SessionResponse;
import uk.co.epicuri.api.core.pojo.session.Table;
import uk.co.epicuri.bookingapi.endpoints.aurusit.AurusitController;
import uk.co.epicuri.bookingapi.pojo.aurusit.AurusitOrder;
import uk.co.epicuri.bookingapi.pojo.aurusit.AurusitOrderRequest;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Manish on 24/06/2015.
 */
public class StressTests {

    @Test
    public void testCrazy() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost", 12000, "", "");
        QConnection aurusitdb = new QBasicConnection("localhost", 11000, "", "");

        securitydb.open();
        aurusitdb.open();

        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING,"5");

        if(((char[])securitydb.sync("getInternalTokenById`5")).length == 0) {
            Authentication authentication = api.login("5", "mp", "mp");
            securitydb.sync("insertPass", "5", authentication.getAuthKey().toCharArray());
        }
        String token = new String((char[])securitydb.sync("getToken getInternalTokenById[`5]"));
        String internalToken = new String((char[])securitydb.sync("getInternalTokenById[`5]"));

        AurusitController aurusitController = new AurusitController(api,securitydb,aurusitdb);

        Map<String,Boolean> occupation = Collections.synchronizedMap(new HashMap<String, Boolean>());
        List<Floor> floors = api.getFloors(token);
        for(Floor floor : floors) {
            Layout layout = api.getLayout(floor.getLayout(), token);
            for(Table table : layout.getTables()) {
                occupation.put(table.getName(), false);
            }
        }

        //ExecutorService executor = Executors.newCachedThreadPool();
        //executor.execute(new SessionCreator(aurusitController, occupation, internalToken, 2));
        new SessionCreator(aurusitController, occupation, internalToken, 2).run();
        //executor.shutdown();
        //executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private class SessionCreator implements Runnable {

        private final AurusitController aurusitController;
        private final Map<String, Boolean> occupation;
        private final String token;
        private final int maxCalls;

        public SessionCreator(AurusitController aurusitController, Map<String,Boolean> occupation, String token, int maxCalls) {
            this.aurusitController = aurusitController;
            this.occupation = occupation;
            this.token = token;
            this.maxCalls = maxCalls;
        }

        @Override
        public void run() {
            int iteration = 0;
            Random random = new Random();
            while (iteration < maxCalls) {

                System.out.println("Order creation iteration " + iteration);

                boolean isSeated = true;//random.nextBoolean();



                if(isSeated) {
                    String next = nextAvailableTable(occupation);
                    AurusitOrderRequest orders = new AurusitOrderRequest();
                    orders.getItems().add(createOrder(0,10,random.nextInt(9),null));
                    orders.getItems().add(createOrder(1,10,random.nextInt(9),null));
                    orders.getItems().add(createOrder(2,10,random.nextInt(9),"with foo"));
                    orders.setTableName(next);
                    aurusitController.orderOnTable(5,token,orders);
                    System.out.println("Ordered " + orders.getItems().size() + " on table " + next);
                }
                else {
                    AurusitOrderRequest orders = new AurusitOrderRequest();
                    orders.getItems().add(createOrder(0,10,random.nextInt(9),null));
                    orders.getItems().add(createOrder(1,10,random.nextInt(9),null));
                    orders.getItems().add(createOrder(2,10,random.nextInt(9),"with bar"));
                    aurusitController.orderOnTab(5,token,orders);
                    System.out.println("Ordered " + orders.getItems().size() + " on tab");
                }

                iteration++;

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.printf("Creation thread complete");
        }
    }

    private class SessionAmender implements Runnable {

        private final EpicuriAPI api;
        private final AurusitController aurusitController;
        private final Map<String, Boolean> occupation;
        private final String token;
        private final int maxCalls;

        public SessionAmender(EpicuriAPI api, AurusitController aurusitController, Map<String,Boolean> occupation, String token, int maxCalls) {
            this.api = api;
            this.aurusitController = aurusitController;
            this.occupation = occupation;
            this.token = token;
            this.maxCalls = maxCalls;
        }

        @Override
        public void run() {
            Random random = new Random();
            int iteration = 0;
            while (iteration < maxCalls) {
                System.out.print("Order creation iteration " + iteration);

                SessionResponse response = api.getSessions(token);
                OUTER:
                for (Session session : response.getSessions()) {
                    if (random.nextBoolean()) {
                        for (Session.Diners diners : session.getDiners()) {
                            if (diners.getOrders().size() > 0) {
                                api.forceCloseSession(session.getId(), false, token);
                                break OUTER;
                            }
                        }
                    } else {
                        AurusitOrderRequest orders = new AurusitOrderRequest();
                        if(session.getSessionType().equals("Seated")) {
                            String next = nextAvailableTable(occupation);
                            orders.getItems().add(createOrder(0,10,random.nextInt(9),null));
                            orders.getItems().add(createOrder(1,10,random.nextInt(9),null));
                            orders.getItems().add(createOrder(2,10,random.nextInt(9),"with foo"));
                            orders.setTableName(next);
                            aurusitController.orderOnTable(5, token, orders);
                            System.out.println("Ordered " + orders.getItems().size() + " on table " + next);
                        }
                        else {
                            orders.getItems().add(createOrder(0,10,random.nextInt(9),null));
                            orders.getItems().add(createOrder(1,10,random.nextInt(9),null));
                            orders.getItems().add(createOrder(2,10,random.nextInt(9),"with bar"));
                            aurusitController.orderOnTab(5, token, orders);
                            System.out.println("Ordered " + orders.getItems().size() + " on new tab");
                        }
                    }
                }
                iteration++;

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("Creation thread complete");
        }
    }

    private static AurusitOrder createOrder(int id, int price, int quantity, String note) {
        AurusitOrder order = new AurusitOrder();
        order.setItemId(id);
        order.setPrice(price);
        order.setQuantity(quantity);

        return order;
    }

    private static String nextAvailableTable(Map<String,Boolean> occupation) {
        String toR = null;
        synchronized (occupation) {
            for (String key : occupation.keySet()) {
                if (!occupation.get(key)) {
                    toR = key;
                }
            }
            if (toR != null) {
                occupation.put(toR, true);
            }
        }
        return toR;
    }
}
