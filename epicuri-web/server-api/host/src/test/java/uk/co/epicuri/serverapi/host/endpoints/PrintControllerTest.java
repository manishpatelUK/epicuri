package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdList;
import uk.co.epicuri.serverapi.common.pojo.host.HostOrderView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostBatchView;
import uk.co.epicuri.serverapi.common.pojo.model.session.PrintBatchRequest;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.BatchService;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by Manish Patel
 */
public class PrintControllerTest extends BaseIT {

    @Value("${epicuri.waiter.print.window}")
    private long printSpoolWindow;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        printer1.setIp("192.168.0.1");
        printerRepository.save(printer1);
        menuItem1.setDefaultPrinter(printer1.getId());
        modifier1.setPrice(1);
        modifier1.setPriceOverride(1);
        modifierRepository.save(modifier1);
        menuItemRepository.save(menuItem1);

        printer3.setIp("192.168.0.1");
        printerRepository.save(printer3);
        menuItem2.setDefaultPrinter(printer3.getId());
        menuItemRepository.save(menuItem2);

        table1.setId(IDAble.generateId(restaurant1.getId()));
        table2.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testPutBatch() throws Exception {
        long time = System.currentTimeMillis();
        batch1.setPrintedTime(time);
        batch2.setPrintedTime(null);
        batch3.setPrintedTime(null);

        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);

        PrintBatchRequest payload = new PrintBatchRequest();
        payload.getBatchId().add(batch1.getId());
        payload.getBatchId().add(batch2.getId());
        payload.getBatchId().add(batch3.getId());

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .put("Print");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(time, batchRepository.findOne(batch1.getId()).getPrintedTime().longValue());
        assertTrue(batchRepository.findOne(batch2.getId()).getPrintedTime() > time);
        assertTrue(batchRepository.findOne(batch3.getId()).getPrintedTime() > time);
        assertEquals(batchRepository.findOne(batch3.getId()).getPrintedTime(), batchRepository.findOne(batch2.getId()).getPrintedTime());

        payload.getBatchId().clear();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .put("Print");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testDeleteBatches() {
        List<String> ids = new ArrayList<>();
        ids.add(batch1.getId());
        ids.add(batch3.getId());
        ids.add(null);
        ids.add("foo");

        IdList payload = new IdList();
        payload.setIds(ids);
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .delete("Print");

        List<Batch> batches = batchRepository.findAll();
        assertEquals(3, batches.size());
        assertNotNull(batches.stream().filter(b->b.getId().equals(batch1.getId())).findFirst().orElse(null).getDeleted());
        assertNotNull(batches.stream().filter(b->b.getId().equals(batch3.getId())).findFirst().orElse(null).getDeleted());
        assertNull(batches.stream().filter(b->b.getId().equals(batch2.getId())).findFirst().orElse(null).getDeleted());
    }

    @Test
    public void testDeleteAllBatches() {
        setUpBatchesToDelete();

        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Print/all");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNotNull(batchRepository.findOne(batch1.getId()).getDeleted());
        assertNotNull(batchRepository.findOne(batch2.getId()).getDeleted());
        assertNull(batchRepository.findOne(batch3.getId()).getDeleted());
    }

    @Test
    public void testDeleteAllBatchesTakeaways() {
        setUpBatchesToDelete();
        session1.setSessionType(SessionType.TAKEAWAY);
        sessionRepository.save(session1);

        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Print/all");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNull(batchRepository.findOne(batch1.getId()).getDeleted());
        assertNotNull(batchRepository.findOne(batch2.getId()).getDeleted());
        assertNull(batchRepository.findOne(batch3.getId()).getDeleted());
    }

    public void setUpBatchesToDelete() {
        session1.setRestaurantId(staff1.getRestaurantId());
        session1.setStartTime(System.currentTimeMillis());
        session2.setRestaurantId(staff1.getRestaurantId());
        session2.setStartTime(System.currentTimeMillis());
        session3.setRestaurantId(staff1.getRestaurantId());
        session3.setStartTime(System.currentTimeMillis()-1000);
        session3.setClosedTime(System.currentTimeMillis()-1000);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        batch1.setSessionId(session1.getId());
        batch2.setSessionId(session2.getId());
        batch3.setSessionId(session3.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);
    }

    @Test
    public void testPutSpoolBatches() {
        List<String> ids = new ArrayList<>();
        ids.add(batch1.getId());
        ids.add(batch3.getId());
        ids.add(null);
        ids.add("foo");

        IdList payload = new IdList();
        payload.setIds(ids);
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .put("Print/spool");

        assertEquals(0, batchRepository.findOne(batch2.getId()).getSpoolTime().size());
        assertEquals(1, batchRepository.findOne(batch1.getId()).getSpoolTime().size());
        assertEquals(1, batchRepository.findOne(batch3.getId()).getSpoolTime().size());

        batch1.setPrintedTime(System.currentTimeMillis());
        batchRepository.save(batch1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .put("Print/spool");

        assertNull(batchRepository.findOne(batch1.getId()).getPrintedTime());
    }

    @Test
    public void testGetBatches() throws Exception {
        String token = getTokenForStaff(staff1);
        long now = System.currentTimeMillis();

        reset(now, now+(24*60*60*1000), now+(24*60*60*1000), SessionType.SEATED, BookingType.NONE);

        Response response = callGetPrint(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostBatchView> batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, batches.size());

        reset(now, now, now, SessionType.SEATED, BookingType.NONE);

        response = callGetPrint(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());
        assertTrue(batches.get(0).getTables().contains(table1.getName()));
        assertTrue(batches.get(0).getTables().contains(table2.getName()));

        reset(now, now, now, SessionType.ADHOC, BookingType.NONE);

        response = callGetPrint(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());

        reset(now, now, now, SessionType.ADHOC, BookingType.NONE);
        sessionRepository.save(session1);

        response = callGetPrint(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());

        reset(now, now, now, SessionType.ADHOC, BookingType.NONE);
        session1.setClosedTime(now);
        sessionRepository.save(session1);

        response = callGetPrint(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());

        reset(now, now, now, SessionType.ADHOC, BookingType.NONE);
        session1.setClosedTime(now);
        sessionRepository.save(session1);

        response = callGetPrint(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());
    }

    private Response callGetPrint(String token) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Print");
    }

    @Test
    public void testGetBatchesGetSpooled() throws Exception {
        String token = getTokenForStaff(staff1);
        long now = System.currentTimeMillis();

        reset(now, now - (24 * 60 * 60 * 1000), now - (24 * 60 * 60 * 1000), SessionType.SEATED, BookingType.RESERVATION);

        //order have just been created; spool them for print. The very next call should not yield anything
        Response response = callGetPrint(token);
        List<HostBatchView> batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertTrue(batches.size() > 0);
        response = callGetPrint(token);
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, batches.size());
    }

    @Test
    public void testGetBatchesGetReSpooled() throws Exception {
        String token = getTokenForStaff(staff1);
        long now = System.currentTimeMillis();

        reset(now, now - (60 * 60 * 1000), now - (2 * 60 * 60 * 1000), SessionType.SEATED, BookingType.RESERVATION);

        //order have just been created; spool them for print. The very next call should not yield anything
        Response response = callGetPrint(token);
        List<HostBatchView> batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertTrue(batches.size() > 0);
        //artificially set the spool time to window+2 seconds for these batches
        Iterable<Batch> dbBatches = batchRepository.findAll(batches.stream().map(HostBatchView::getId).collect(Collectors.toList()));
        List<String> previouslySpooled = new ArrayList<>();
        dbBatches.forEach(b -> {
            setToRespool(now, previouslySpooled, b);
        });

        response = callGetPrint(token);
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(batches.size(), previouslySpooled.size());
        assertTrue(batches.stream().map(HostBatchView::getId).collect(Collectors.toList()).stream().allMatch(previouslySpooled::contains));
    }

    @Test
    public void testGetBatchesDoNotDuplicate() throws Exception {
        String token = getTokenForStaff(staff1);
        long now = System.currentTimeMillis() - (10 * 60 * 1000);

        reset(now, now - (60 * 60 * 1000), now - (2 * 60 * 60 * 1000), SessionType.SEATED, BookingType.RESERVATION);

        //order have just been created; spool them for print. Add an order. Only new items should print.
        Response response = callGetPrint(token);
        List<HostBatchView> batches1 = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        //set these batches to printed
        Iterable<Batch> dbBatches = batchRepository.findAll(batches1.stream().map(HostBatchView::getId).collect(Collectors.toList()));
        dbBatches.forEach(b -> {
            b.setPrintedTime(now);
            batchRepository.save(b);
        });

        createOrders(session1, now);
        response = callGetPrint(token);
        List<HostBatchView> batches2 = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(batches1.size(), batches2.size());

        List<String> ids = batches2.stream().map(HostBatchView::getId).collect(Collectors.toList());
        batches1.stream().map(HostBatchView::getId).forEach(b -> assertFalse(ids.contains(b)));
        List<String> orderIds = new ArrayList<>();
        batches1.forEach(b -> orderIds.addAll(b.getOrders().stream().map(HostOrderView::getId).collect(Collectors.toList())));
        batches2.forEach(b -> assertFalse(b.getOrders().stream().map(HostOrderView::getId).anyMatch(orderIds::contains)));
    }

    @Test
    public void testGetPrintTakeaways() throws Exception {
        String token = getTokenForStaff(staff1);
        restaurant1.setDefaultTakeawayPrinterId(printer3.getId());
        restaurantRepository.save(restaurant1);
        printer3.setRestaurantId(staff1.getRestaurantId());
        printer3.setIp("192.168.1.1");
        printerRepository.save(printer3);

        long now = System.currentTimeMillis();
        RestaurantDefault lockWindow = masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.TAKEAWAY_LOCK_WINDOW);

        reset(now, now+(24*60*60*1000), now+(24*60*60*1000), SessionType.TAKEAWAY, BookingType.TAKEAWAY);
        Response response = callGetPrint(token);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostBatchView> batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, batches.size());

        reset(now, now, now, SessionType.TAKEAWAY, BookingType.TAKEAWAY);
        response = callGetPrint(token);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());
        assertEquals(printer3.getId(), batches.get(0).getPrinterId());

        reset(now-(24*60*60*1000), now-(24*60*60*1000), (now + (((Number) lockWindow.getValue()).intValue() * 60 * 1000)) + 20000, SessionType.TAKEAWAY, BookingType.TAKEAWAY);
        response = callGetPrint(token);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, batches.size());

        reset(now-(24*60*60*1000), now-(24*60*60*1000), (now + (((Number)lockWindow.getValue()).intValue() * 60 * 1000)) - 20000, SessionType.TAKEAWAY, BookingType.TAKEAWAY);
        response = callGetPrint(token);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());
    }

    @Test
    public void testGetPrintBatchTakeawaysSplitOrder() throws Exception {
        String token = getTokenForStaff(staff1);
        restaurant1.setDefaultTakeawayPrinterId(printer3.getId());
        restaurantRepository.save(restaurant1);
        printer3.setRestaurantId(staff1.getRestaurantId());
        printer3.setIp("192.168.1.1");
        printerRepository.save(printer3);

        long now = System.currentTimeMillis();
        RestaurantDefault lockWindow = masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.TAKEAWAY_LOCK_WINDOW);

        //2 menu items on different printers - ensure they both print on takeaway printer, not separately
        long orderTime = now-(24*60*60*1000);
        reset(now-(24*60*60*1000), (now + (((Number)lockWindow.getValue()).intValue() * 60 * 1000)) - 20000, SessionType.TAKEAWAY, BookingType.TAKEAWAY);
        createSplitOrders(session1, orderTime);
        Response response = callGetPrint(token);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostBatchView> batches = Arrays.asList(response.getBody().as(HostBatchView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, batches.size());
        assertEquals(printer3.getId(), batches.get(0).getPrinterId());
    }

    private void setToRespool(long now, List<String> previouslySpooled, Batch b) {
        List<Long> list = new ArrayList<>();
        list.add(now - (printSpoolWindow+2000));
        b.setSpoolTime(list);
        batchRepository.save(b);
        previouslySpooled.add(b.getId());
    }

    private void reset(long orderTime, long sessionStartTime, long bookingTime, SessionType sessionType, BookingType bookingType) {
        reset(sessionStartTime, bookingTime, sessionType, bookingType);
        createOrders(session1, orderTime);
    }

    private void reset(long sessionStartTime, long bookingTime, SessionType sessionType, BookingType bookingType) {
        resetOrdersAndBatches();
        session1.setSessionType(sessionType);
        if(bookingType != BookingType.NONE) {
            booking1.setBookingType(bookingType);
            booking1.setTargetTime(bookingTime);
            bookingRepository.save(booking1);
            session1.setOriginalBooking(booking1);
        }
        session1.setRestaurantId(restaurant1.getId());
        session1.setStartTime(sessionStartTime);

        if(sessionType == SessionType.SEATED) {
            if(!session1.getTables().contains(table1.getId())) session1.getTables().add(table1.getId());
            if(!session1.getTables().contains(table2.getId())) session1.getTables().add(table2.getId());
        }

        sessionRepository.save(session1);
    }

    private void resetOrdersAndBatches() {
        sessionRepository.deleteAll();
        orderRepository.deleteAll();
        batchRepository.deleteAll();
    }

    private void createOrders(Session session, long time) {
        Order order = createOrders(menuItem1, session, time);

        List<Order> orders = liveDataService.insertOrders(session, Collections.singletonList(menuItem1), Collections.singletonList(order), true, true);

        //reset immediate printing for testing purposes
        resetImmediatePrintingToFalse(session);
    }

    private void createSplitOrders(Session session, long time) {
        Order order1 = createOrders(menuItem1, session, time);
        Order order2 = createOrders(menuItem2, session, time);

        List<Order> orders = liveDataService.insertOrders(session, Lists.newArrayList(menuItem1, menuItem2), Lists.newArrayList(order1, order2), true, true);

        //reset immediate printing for testing purposes
        resetImmediatePrintingToFalse(session);
    }

    private Order createOrders(MenuItem menuItem, Session session, long time) {
        Order order = new Order();
        order.setCourseId(course1.getId());
        order.setItemPrice(menuItem.getPrice());
        order.setMenuItem(menuItem);
        order.setMenuItemId(menuItem.getId());
        order.getModifiers().add(modifier1);
        order.setPriceOverride(menuItem.getPrice());
        order.setQuantity(1);
        order.setSessionId(session.getId());
        order.setTime(time);
        order.setStaffId(staff1.getId());

        return order;
    }

    private void resetImmediatePrintingToFalse(Session session) {
        List<Batch> batchesBySessionId = liveDataService.getBatchesBySessionId(session.getId());
        batchesBySessionId.forEach(b -> b.setAwaitingImmediatePrint(false));
        batchRepository.save(batchesBySessionId);
    }
}