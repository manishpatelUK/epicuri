package uk.co.epicuri.bookingapi.endpoints.aurusit;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.exxeleron.qjava.QTable;
import junit.framework.Assert;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.bookingapi.endpoints.auth.LogonController;
import uk.co.epicuri.bookingapi.pojo.LogonResponse;
import uk.co.epicuri.bookingapi.pojo.aurusit.AurusitOrder;
import uk.co.epicuri.bookingapi.pojo.aurusit.AurusitOrderRequest;
import uk.co.epicuri.bookingapi.pojo.auth.LoginRequest;


public class AurusitControllerTest {

    @Test
    public void testOrderOnTable() throws Exception {
        createOrders(true);
    }

    @Test
    public void testOrderOnTab() throws Exception {
        createOrders(false);
    }

    @Test
    public void testStress() throws Exception {

    }

   /* @Test
    public void testAddOne() throws Exception {
        EpicuriAPI epicuriAPI = new EpicuriAPI(EpicuriAPI.Environment.PROD, "16");
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection aurusitdb = new QBasicConnection("localhost",11000,"","");
        securitydb.open();
        aurusitdb.open();

        String token = new String((char[]) securitydb.sync("getInternalTokenById", "5"));
        if(token.length() == 0) {
            LogonController controller = new LogonController(epicuriAPI, securitydb);
            LoginRequest login = new LoginRequest();
            login.setUsername("mp");
            login.setPassword("mp");
            LogonResponse response = controller.authentication("5",login);
            token = response.getToken();
        }

        AurusitController aurusitController = new AurusitController(epicuriAPI, securitydb, aurusitdb);

        AurusitOrderRequest orders = new AurusitOrderRequest();
        orders.getItems().add(createOrder(0,10,1,null));
        orders.getItems().add(createOrder(1,10,2,null));
        orders.getItems().add(createOrder(2,10,3,null));
    }*/

    private static void createOrders(boolean onTable) throws Exception{
        EpicuriAPI epicuriAPI = new EpicuriAPI(EpicuriAPI.Environment.STAGING, "3");
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection aurusitdb = new QBasicConnection("localhost",11000,"","");
        securitydb.open();
        aurusitdb.open();

        String token = new String((char[]) securitydb.sync("getInternalTokenById", "5"));
        if(token.length() == 0) {
            LogonController controller = new LogonController(epicuriAPI, securitydb);
            LoginRequest login = new LoginRequest();
            login.setUsername("mp");
            login.setPassword("mp");
            LogonResponse response = controller.authentication("5",login);
            token = response.getToken();
        }

        AurusitController aurusitController = new AurusitController(epicuriAPI, securitydb, aurusitdb);

        AurusitOrderRequest orders = new AurusitOrderRequest();
        orders.getItems().add(createOrder(0,10,1,null));
        orders.getItems().add(createOrder(1,10,2,null));
        orders.getItems().add(createOrder(2,10,3,null));

        // make sure table 15 is clear before you run this
        if(onTable) {
            orders.setTableName("15");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            aurusitController.orderOnTable(5, token, orders); // will add to table
            orders.setTableName("10");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("11");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("1");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("2");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("3");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("4");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("5");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("6");
            aurusitController.orderOnTable(5, token, orders); // will create the table
            orders.setTableName("Non Existant Table Number");
            aurusitController.orderOnTable(5, token, orders); // will create a tab instead
        }
        else {
            orders.setAdhocName("test");
            aurusitController.orderOnTab(5, token, orders); // will create tab
            aurusitController.orderOnTab(5, token, orders); // will add to tab
            orders.setAdhocName("test 2");
            aurusitController.orderOnTab(5, token, orders); // will create tab
            orders.setAdhocName("test 3");
            aurusitController.orderOnTab(5, token, orders); // will create tab
            orders.setAdhocName("test 4");
            aurusitController.orderOnTab(5, token, orders); // will create tab
            orders.setAdhocName("test 5");
            aurusitController.orderOnTab(5, token, orders); // will create tab
            orders.setAdhocName("test 6");
            aurusitController.orderOnTab(5, token, orders); // will create tab
            orders.setAdhocName("test 7");
            aurusitController.orderOnTab(5, token, orders); // will create tab
        }

        // check if table 15 has the right orders
    }

    @Test
    public void testDetermineTableExists() throws Exception {
        EpicuriAPI epicuriAPI = new EpicuriAPI(EpicuriAPI.Environment.STAGING, "3");
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection aurusitdb = new QBasicConnection("localhost",11000,"","");
        securitydb.open();
        aurusitdb.open();

        String token = new String((char[]) securitydb.sync("getInternalTokenById", "5"));
        if(token.length() == 0) {
            LogonController controller = new LogonController(epicuriAPI, securitydb);
            LoginRequest login = new LoginRequest();
            login.setUsername("mp");
            login.setPassword("mp");
            LogonResponse response = controller.authentication("5",login);
            token = response.getToken();
        }

        AurusitController aurusitController = new AurusitController(epicuriAPI, securitydb, aurusitdb);
        QTable sessionInfo = aurusitController.getSessionInfo(5);
        Assert.assertTrue(aurusitController.determineTableExists(5, "15", sessionInfo));
        Assert.assertTrue(aurusitController.determineTableExists(5, "1", sessionInfo));
        Assert.assertFalse(aurusitController.determineTableExists(5, "101", sessionInfo));
    }

    private static AurusitOrder createOrder(int id, int price, int quantity, String note) {
        AurusitOrder order = new AurusitOrder();
        order.setItemId(id);
        order.setPrice(price);
        order.setQuantity(quantity);
        if(note != null) {
            order.setNote(note);
        }
        return order;
    }
}