package uk.co.epicuri.serverapi.service.external;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.external.PaymentSenseReport;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish on 29/07/2017.
 */
@Ignore
public class PaymentSenseRestServiceTest extends BaseIT{

    protected static String host = "https://ss890b840000.test.connect.paymentsense.cloud";
    protected static String key = "784067a1-3b45-48d2-986a-d5f6a5350ae8";
    /*protected static String host = "https://im-legend.test.connect.paymentsense.cloud";
    protected static String key = "Payment1";*/

    @Autowired
    private PaymentSenseRestService paymentSenseRestService;

    protected static void cleanUp(Restaurant restaurant, PaymentSenseRestService paymentSenseRestService, String table) {
        TableResponse tableResponse = paymentSenseRestService.getTable(restaurant, table);

        String plainCreds = "mp:" + key;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Basic " + base64Creds);
        SequentialTableRequest deleteTableRequest = new SequentialTableRequest();
        deleteTableRequest.setLastKnownAmount(tableResponse.getAmount());
        deleteTableRequest.setLastKnownPaidAmount(tableResponse.getAmountPaid());
        HttpEntity<SequentialTableRequest> entity = new HttpEntity<>(deleteTableRequest, headers);

        new RestTemplate().exchange(host + "/pat/tables/" + table, HttpMethod.DELETE, entity, String.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        KVData kvData = new KVData();
        kvData.setHost(host);
        kvData.setKey(key);
        restaurant1.getIntegrations().put(ExternalIntegration.PAYMENT_SENSE, kvData);
        restaurant1.setISOCurrency("GBP");
        restaurantRepository.save(restaurant1);

        try {
            cleanUp(restaurant1, paymentSenseRestService, "t1");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void postAndDeleteTable() throws Exception {
        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"t1", 10));
        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"t1", 10, 0));
    }

    @Ignore
    @Test
    public void testGetTable() throws Exception {
        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"t1", 10));
        TableResponse response = paymentSenseRestService.getTable(restaurant1,"t1");
        assertEquals("t1", response.getTableName());
        assertEquals(10, response.getAmount());
        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"t1", 10, 0));
    }

    @Ignore
    @Test
    public void testGetTables() throws Exception {
        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"t1", 10));
        try {
            cleanUp(restaurant1, paymentSenseRestService, "tab2"); //just in case
        } catch (Exception ex){}
        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"tab2", 20));
        List<TableResponse> response = paymentSenseRestService.getTables(restaurant1);
        assertEquals(10, response.stream().filter(r -> r.getTableName().equals("t1")).findAny().orElse(new TableResponse()).getAmount());
        assertEquals(20, response.stream().filter(r -> r.getTableName().equals("tab2")).findAny().orElse(new TableResponse()).getAmount());

        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"t1", 10, 0));
        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"tab2", 20, 0));
    }

    @Ignore
    @Test
    public void testUpdateTable() throws Exception {
        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"t1", 10));
        assertTrue(paymentSenseRestService.updateAmount("1",restaurant1,"t1", 10, 0, 20));

        TableResponse response = paymentSenseRestService.getTable(restaurant1,"t1");
        assertEquals(20,response.getAmount());

        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"t1", 20, 0));
    }

    @Ignore
    @Test
    public void testLock() throws Exception {
        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"t1", 10));
        assertTrue(paymentSenseRestService.lock("1", restaurant1, "t1", 10, 0, true, false));
        assertTrue(paymentSenseRestService.lock("1", restaurant1, "t1", 10, 0, false, false));
        assertTrue(paymentSenseRestService.lock("1", restaurant1, "t1", 10, 0, true, true));
        assertTrue(paymentSenseRestService.lock("1", restaurant1, "t1", 10, 0, false, true));
        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"t1", 10, 0));
    }

    @Ignore
    @Test
    public void testReceipt() throws Exception {
        Order order = new Order();
        order.setTaxRate(tax1);
        menuItem1.setTaxTypeId(tax1.getId());
        order.setMenuItem(menuItem1);
        order.setItemPrice(menuItem1.getPrice());
        order.setSessionId(session1.getId());

        assertTrue(paymentSenseRestService.postTable("1",restaurant1,"t1", 10));
        assertTrue(paymentSenseRestService.updateReceipt("1", restaurant1, "t1", 10, 0, Collections.singletonList(order)));
        assertTrue(paymentSenseRestService.deleteTable("1",restaurant1,"t1", 10, 0));
    }

    @Ignore
    @Test
    public void testGetTerminals() throws Exception {
        TerminalList terminalList = paymentSenseRestService.getTerminals(restaurant1);
        assertNotNull(terminalList);
        assertTrue((terminalList.getTerminalList().size()>0));
    }
    //@Ignore
    /*@Test
    public void testGetReports() throws Exception {
        PACReports PACReports = paymentSenseRestService.getReport("1", restaurant1);
        assertTrue(PACReports.getReports().size() > 0);
    }*/

    @Ignore
    @Test
    public void testPostReport() throws Exception {
        KVData data = restaurant1.getIntegrations().get(ExternalIntegration.PAYMENT_SENSE);
        TerminalList terminals = paymentSenseRestService.getTerminals(restaurant1);
        String tpi = terminals.getTerminalList().get(0).getId();

        ReportType reportType = new ReportType();
        reportType.setReportType("END_OF_DAY");
        PaymentSenseReport response = paymentSenseRestService.postReportAndPollOnTerminal(restaurant1.getId(), data, tpi, reportType);

        assertNotNull(response);
    }
}