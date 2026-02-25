package uk.co.epicuri.serverapi.service;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class LiveDataServiceTest extends BaseIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiveDataServiceTest.class);

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        //whitebox PaymentSense
        PaymentSenseRestService mock = EasyMock.createMock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService,mock);
    }

    @Test
    public void testGetCheckInByCustomer() throws Exception {
        checkIn1.setCustomerId(customer1.getId());
        checkInRepository.save(checkIn1);

        CheckIn checkIn = liveDataService.getCheckInByCustomer(customer1.getId());

        assertEquals(checkIn1.getId(), checkIn.getId());

        checkIn = liveDataService.getCheckInByCustomer(customer2.getId());

        assertNull(checkIn);
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsert() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetCheckInById() throws Exception {

    }

    @Test
    public void testGetCheckInsByRestIdAndExpiration() throws Exception {
        checkIn1.setTime(System.currentTimeMillis() - 5000);
        checkIn1.setRestaurantId(restaurant1.getId());
        checkInRepository.save(checkIn1);

        List<CheckIn> list1 = liveDataService.getCheckIns(restaurant1.getId(), 1000);
        List<CheckIn> list2 = liveDataService.getCheckIns(restaurant1.getId(), 7000);

        assertEquals(0, list1.size());
        assertEquals(1, list2.size());
        assertEquals(checkIn1.getId(), list2.get(0).getId());

        checkIn1.setTime(System.currentTimeMillis() - 5000);
        checkIn1.setDeleted(System.currentTimeMillis());
        checkInRepository.save(checkIn1);

        list2 = liveDataService.getCheckIns(restaurant1.getId(), 7000);
        assertEquals(0, list2.size());
    }

    @Test
    public void testGetCheckInsByBookingIds() throws Exception {
        checkIn1.setRestaurantId(restaurant1.getId());
        checkIn1.setBookingId(booking1.getId());
        checkIn2.setRestaurantId(restaurant1.getId());
        checkIn2.setBookingId(booking2.getId());
        checkIn3.setRestaurantId(restaurant1.getId());
        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);

        List<String> ids = new ArrayList<>();
        ids.add(booking1.getId());
        ids.add(booking2.getId());
        ids.add("dudd");

        List<CheckIn> list1 = liveDataService.getCheckInsByBookingIds(restaurant1.getId(),ids);
        List<CheckIn> list2 = liveDataService.getCheckInsByBookingIds(restaurant2.getId(),ids);
        checkIn1.setDeleted(0L);
        checkInRepository.save(checkIn1);
        List<CheckIn> list3 = liveDataService.getCheckInsByBookingIds(restaurant1.getId(),ids);
        ids.clear();
        List<CheckIn> list4 = liveDataService.getCheckInsByBookingIds(restaurant1.getId(),ids);

        assertEquals(2, list1.size());
        List<String> cids = list1.stream().map(CheckIn::getId).collect(Collectors.toList());
        assertTrue(cids.contains(checkIn1.getId()));
        assertTrue(cids.contains(checkIn2.getId()));

        assertEquals(0, list2.size());

        assertEquals(1, list3.size());
        assertEquals(checkIn2, list3.get(0));

        assertEquals(0, list4.size());
    }

    @Test
    public void testGetCheckInsByBookingId() throws Exception {
        checkIn1.setRestaurantId(restaurant1.getId());
        checkIn1.setBookingId(booking1.getId());
        checkInRepository.save(checkIn1);

        List<CheckIn> list1 = liveDataService.getCheckInsByBookingId(restaurant1.getId(), booking1.getId());
        List<CheckIn> list2 = liveDataService.getCheckInsByBookingId(restaurant2.getId(), booking1.getId());

        assertEquals(1, list1.size());
        assertEquals(checkIn1.getId(), list1.get(0).getId());

        assertEquals(0, list2.size());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDeleteCheckIn() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testSoftDeleteCheckIn() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testSoftDeleteCheckIn1() throws Exception {

    }

    @Test
    public void testTiePartyCheckIn() throws Exception {
        party2.setRestaurantId("some other rest id");
        partyRepository.save(party2);

        liveDataService.tiePartyCheckIn(customer1.getId(), party2);

        CheckIn c1 = checkInRepository.findAll().stream()
                .filter(c -> c.getCustomerId() != null && c.getCustomerId().equals(customer1.getId())).findFirst().orElse(null);

        assertNull(c1);

        party2.setRestaurantId(restaurant2.getId());
        partyRepository.save(party2);

        CheckIn c2 = checkInRepository.findAll().stream()
                .filter(c -> c.getCustomerId() != null && c.getCustomerId().equals(customer1.getId())).findFirst().orElse(null);

        assertNull(c2);

        checkIn2.setCustomerId(customer1.getId());
        checkIn2.setRestaurantId(restaurant2.getId());
        checkInRepository.save(checkIn2);

        liveDataService.tiePartyCheckIn(customer1.getId(), party2);

        Party party = partyRepository.findOne(party2.getId());
        CheckIn checkIn = checkInRepository.findOne(checkIn2.getId());

        assertEquals(customer1.getId(), party.getCustomerId());
        assertEquals(customer1.getId(), checkIn.getCustomerId());
        assertEquals(party2.getId(), checkIn.getPartyId());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testInsert() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetParties() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetParty() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetPartyByBookingId() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertParty() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testInsertParty() throws Exception {

    }

    @Test
    public void testGetUnacknowledgedNotifications() throws Exception {
        notification1.setRestaurantId(restaurant2.getId());
        notification1.setSessionId(session1.getId());
        notification2.setRestaurantId(restaurant2.getId());
        notification2.setSessionId(session2.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        List<Notification> list1 = liveDataService.getUnacknowledgedNotifications(restaurant2.getId(), session1.getId());
        List<Notification> list2 = liveDataService.getUnacknowledgedNotifications(restaurant2.getId(), session2.getId());
        List<Notification> list3 = liveDataService.getUnacknowledgedNotifications(restaurant1.getId(), session1.getId());

        notification2.setAcknowledged(0L);
        notificationRepository.save(notification2);
        List<Notification> list4 = liveDataService.getUnacknowledgedNotifications(restaurant2.getId(), session1.getId());
        List<Notification> list5 = liveDataService.getUnacknowledgedNotifications(restaurant2.getId(), session2.getId());

        assertEquals(1, list1.size());
        assertEquals(notification1.getId(), list1.get(0).getId());

        assertEquals(1, list2.size());
        assertEquals(notification2.getId(), list2.get(0).getId());

        assertEquals(0, list3.size());

        assertEquals(1, list4.size());
        assertEquals(notification1.getId(), list4.get(0).getId());

        assertEquals(0, list5.size());
    }

    @Test
    public void testGetAllUnacknowledgedNotificationsBySession() throws Exception {
        setUpNotifications();

        Map<Session,List<Notification>> map1 = liveDataService.getAllUnacknowledgedNotificationsBySession(restaurant1.getId());
        Map<Session,List<Notification>> map2 = liveDataService.getAllUnacknowledgedNotificationsBySession(restaurant2.getId());

        notification2.setAcknowledged(0L);
        notificationRepository.save(notification2);

        assertEquals(1, map1.size());
        assertEquals(notification3.getId(), map1.values().iterator().next().get(0).getId());

        assertEquals(2, map2.size());
        assertTrue(map2.values().stream().anyMatch(l -> l.stream().anyMatch(n -> n.getId().equals(notification1.getId()))));
        assertTrue(map2.values().stream().anyMatch(l -> l.stream().anyMatch(n -> n.getId().equals(notification2.getId()))));

        Map<Session,List<Notification>> map3 = liveDataService.getAllUnacknowledgedNotificationsBySession(restaurant2.getId());
        assertEquals(1, map3.size());
        assertTrue(map3.values().stream().anyMatch(l -> l.stream().anyMatch(n -> n.getId().equals(notification1.getId()))));


        map1.values().forEach(x -> assertTrue(checkAscending(x)));
        map2.values().forEach(x -> assertTrue(checkAscending(x)));
        map3.values().forEach(x -> assertTrue(checkAscending(x)));
    }

    private void setUpNotifications() {
        notification1.setRestaurantId(restaurant2.getId());
        notification1.setSessionId(session1.getId());
        notification2.setRestaurantId(restaurant2.getId());
        notification2.setSessionId(session2.getId());
        notification3.setRestaurantId(restaurant1.getId());
        notification3.setSessionId(session3.getId());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);
    }

    @Test
    public void testGetAllUnacknowledgedNotificationsBySessionRecurring() throws Exception {
        setUpNotifications();
        notification2.setNotificationType(NotificationType.RECURRING);
        notification2.setRecurrence(60000);
        notificationRepository.save(notification2);

        Map<Session,List<Notification>> map2 = liveDataService.getAllUnacknowledgedNotificationsBySession(restaurant2.getId());
        assertEquals(2, map2.size());
        assertTrue(map2.values().stream().anyMatch(l -> l.stream().anyMatch(n -> n.getId().equals(notification1.getId()))));
        assertTrue(map2.values().stream().anyMatch(l -> l.stream().anyMatch(n -> n.getId().equals(notification2.getId()))));

    }

    private boolean checkAscending(List<Notification> list) {
        if(list.size() <= 1) {
            return true;
        }

        for(int i = 1; i < list.size(); i++) {
            if(list.get(i).getTime() < list.get(i-1).getTime()) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void testGetAllNotificationsForSession() throws Exception {
        notification1.setRestaurantId(restaurant2.getId());
        notification1.setSessionId(session1.getId());
        notification1.setTime(10);
        notification2.setRestaurantId(restaurant2.getId());
        notification2.setSessionId(session1.getId());
        notification2.setTime(20);
        notification3.setRestaurantId(restaurant1.getId());
        notification3.setSessionId(session3.getId());
        notification3.setTime(10);

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        List<Notification> list1 = liveDataService.getAllNotifications(restaurant2.getId(), session1.getId());
        List<Notification> list2 = liveDataService.getAllNotifications(restaurant2.getId(), session2.getId());
        List<Notification> list3 = liveDataService.getAllNotifications(restaurant1.getId(), session1.getId());
        List<Notification> list4 = liveDataService.getAllNotifications(restaurant1.getId(), session3.getId());

        assertEquals(2, list1.size());
        assertEquals(notification1.getId(), list1.get(0).getId());
        assertEquals(notification2.getId(), list1.get(1).getId());

        assertEquals(0, list2.size());
        assertEquals(0, list3.size());

        assertEquals(1, list4.size());
        assertEquals(notification3.getId(), list4.get(0).getId());

        notification2.setAcknowledged(0L);
        notificationRepository.save(notification2);

        List<Notification> list5 = liveDataService.getAllNotifications(restaurant2.getId(), session1.getId());
        assertEquals(2, list5.size());
        assertEquals(notification1.getId(), list5.get(0).getId());
        assertEquals(notification2.getId(), list5.get(1).getId());

    }

    @Test
    public void testGetAllNotificationsForRestaurant() throws Exception {
        notification1.setRestaurantId(restaurant2.getId());
        notification1.setSessionId(session1.getId());
        notification1.setTime(10);
        notification2.setRestaurantId(restaurant2.getId());
        notification2.setSessionId(session1.getId());
        notification2.setTime(20);
        notification3.setRestaurantId(restaurant1.getId());
        notification3.setSessionId(session3.getId());
        notification3.setTime(10);

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        List<Notification> list1 = liveDataService.getAllNotifications(restaurant1.getId());
        List<Notification> list2 = liveDataService.getAllNotifications(restaurant2.getId());

        assertEquals(1, list1.size());
        assertEquals(2, list2.size());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertNotification() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertNotifications() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetNotification() throws Exception {

    }

    @Test
    public void testDeleteNotifications() throws Exception {
        notification1.setRestaurantId(restaurant2.getId());
        notification1.setSessionId(session1.getId());
        notification1.setTime(10);
        notification2.setRestaurantId(restaurant2.getId());
        notification2.setSessionId(session1.getId());
        notification2.setTime(20);
        notification3.setRestaurantId(restaurant1.getId());
        notification3.setSessionId(session3.getId());
        notification3.setTime(10);

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);

        liveDataService.deleteNotifications(restaurant2.getId(), session3.getId());

        assertEquals(2, liveDataService.getAllNotifications(restaurant2.getId(), session1.getId()).size());
        assertEquals(1, liveDataService.getAllNotifications(restaurant1.getId(), session3.getId()).size());

        liveDataService.deleteNotifications(restaurant2.getId(), session1.getId());

        assertEquals(0, liveDataService.getAllNotifications(restaurant2.getId(), session1.getId()).size());
        assertEquals(1, liveDataService.getAllNotifications(restaurant1.getId(), session3.getId()).size());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDeleteNotification() throws Exception {

    }

    @Test
    public void testGetDiner() throws Exception {
        diner2.setCustomerId(customer3.getId());
        session2.getDiners().add(diner2);
        session2.getDiners().add(diner1);
        session2.setRestaurantId(restaurant2.getId());

        sessionRepository.save(session2);

        Diner d1 = liveDataService.getDiner(session2.getId(), customer3.getId());
        Diner d2 = liveDataService.getDiner(session2.getId(), "foo");
        Diner d3 = liveDataService.getDiner("foo", customer3.getId());
        Diner d4 = liveDataService.getDiner("foo", "bar");

        assertEquals(diner2.getId(), d1.getId());
        assertNull(d2);
        assertNull(d3);
        assertNull(d4);
    }

    @Test
    public void testGetDiners() throws Exception {
        diner2.setCustomerId(customer3.getId());
        session2.getDiners().add(diner2);
        session2.getDiners().add(diner1);
        session2.setRestaurantId(restaurant2.getId());

        sessionRepository.save(session2);

        List<Diner> d1 = liveDataService.getDiners(session1.getId());
        List<Diner> d2 = liveDataService.getDiners(session2.getId());

        assertEquals(0, d1.size());
        assertEquals(diner2.getId(), d2.get(0).getId());
        assertEquals(diner1.getId(), d2.get(1).getId());

    }

    @Test
    public void testGetDinerByDinerId() throws Exception {
        diner2.setCustomerId(customer3.getId());
        session2.getDiners().add(diner2);
        session2.getDiners().add(diner1);
        session2.setRestaurantId(restaurant2.getId());
        diner1.setId(session2.getId() + IDAble.SEPARATOR + "x");
        diner2.setId(session2.getId() + IDAble.SEPARATOR + "y");
        sessionRepository.save(session2);

        Diner d1 = liveDataService.getDiner(diner1.getId());
        Diner d2 = liveDataService.getDiner(diner2.getId());
        Diner d3 = liveDataService.getDiner("foo" + IDAble.SEPARATOR + "bar");
        Diner d4 = liveDataService.getDiner("foo");

        assertEquals(diner1.getId(), d1.getId());
        assertEquals(diner2.getId(), d2.getId());
        assertNull(d3);
        assertNull(d4);
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertDinerToSession() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testInsertOrders() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertBatches() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testSetPrintedTime() throws Exception {

    }

    @Test
    public void testCancelBatchByOrder() throws Exception {
        prepBatchesAndOrders();

        List<String> cancel = new ArrayList<>();
        cancel.add(order2.getId());
        cancel.add("foo");
        cancel.add(order1.getId());

        liveDataService.cancelBatchByOrder(cancel);

        assertNull(batchRepository.findOne(batch1.getId()));
        assertNotNull(batchRepository.findOne(batch2.getId()));

        prepBatchesAndOrders();

        cancel.clear();
        cancel.add(order2.getId());
        cancel.add("foo");

        liveDataService.cancelBatchByOrder(cancel);

        assertEquals(batch1.getId(), batchRepository.findOne(batch1.getId()).getId());
        assertEquals(order1.getId(), batchRepository.findOne(batch1.getId()).getOrderIds().iterator().next());
        assertEquals(1, batchRepository.findOne(batch1.getId()).getOrderIds().size());
    }

    private void prepBatchesAndOrders() {
        batch1.getOrderIds().clear();
        batch2.getOrderIds().clear();
        order1.setSessionId(session1.getId());
        order2.setSessionId(session1.getId());
        order3.setSessionId(session2.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.setSessionId(session1.getId());
        batch2.getOrderIds().add(order3.getId());
        batch2.setSessionId(session2.getId());

        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);
        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
    }

    @Test
    public void testGetOrders() throws Exception {
        order1.setVoided(true);
        order2.setDeleted(1L);
        order3.setRemoveFromReports(true);
        Order order4 = new Order();
        order1.setSessionId(session2.getId());
        order2.setSessionId(session2.getId());
        order3.setSessionId(session2.getId());
        order4.setSessionId(session2.getId());

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);

        List<Order> orders = liveDataService.getOrders(session2.getId());

        assertEquals(1, orders.size());
        assertEquals(order4.getId(), orders.get(0).getId());
    }

    @Test
    public void testInsertOrders1() throws Exception {
        setUpOrders();
        orderRepository.deleteAll();

        List<MenuItem> items = new ArrayList<>();
        items.add(menuItem1);
        items.add(menuItem2);
        items.add(menuItem3);

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        liveDataService.insertOrders(session2, items, orders, true, true);

        List<Order> savedOrders = orderRepository.findAll();
        List<Batch> savedBatches = batchRepository.findAll();

        assertEquals(3, savedOrders.size());
        assertTrue(savedOrders.stream().anyMatch(o -> o.getId().equals(order1.getId())));
        assertTrue(savedOrders.stream().anyMatch(o -> o.getId().equals(order2.getId())));
        assertTrue(savedOrders.stream().anyMatch(o -> o.getId().equals(order3.getId())));

        assertTrue(savedOrders.stream().anyMatch(o -> o.getModifiers().size() > 0 && o.getModifiers().get(0).getId().equals(modifier1.getId())));

        List<String> orderSessionIds = savedOrders.stream().map(Order::getSessionId).distinct().collect(Collectors.toList());
        assertTrue(orderSessionIds.size() == 1);
        assertEquals(session2.getId(), orderSessionIds.get(0));

        assertEquals(2, savedBatches.size());

        for(Batch batch : savedBatches) { //have to do it this way coz order of items isn't guaranteed
            assertTrue(batch.getOrderIds().contains(order1.getId())
                        || batch.getOrderIds().contains(order2.getId())
                        || batch.getOrderIds().contains(order3.getId()));
        }

        List<String> batchSessionIds = savedBatches.stream().map(Batch::getSessionId).distinct().collect(Collectors.toList());
        assertTrue(batchSessionIds.size() == 1);
        assertEquals(session2.getId(), batchSessionIds.get(0));
        assertNull(savedBatches.get(0).getPrintedTime());
        assertNull(savedBatches.get(1).getPrintedTime());

        setUpOrders();
        orderRepository.deleteAll();
        printer2.setIp(null);
        printerRepository.save(printer2);

        liveDataService.insertOrders(session2, items, orders, true, true);

        savedBatches = batchRepository.findAll();

        for(Batch batch : savedBatches) {
            assertTrue(batch.getOrderIds().contains(order1.getId())
                    || batch.getOrderIds().contains(order2.getId())
                    || batch.getOrderIds().contains(order3.getId()));
            assertNull(batch.getPrintedTime());
        }
    }

    @Test
    public void testInsertOrders_noBatchesWithRefund() throws Exception {
        setUpOrders();
        orderRepository.deleteAll();

        List<MenuItem> items = new ArrayList<>();
        items.add(menuItem1);
        items.add(menuItem2);
        items.add(menuItem3);

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        session2.setSessionType(SessionType.REFUND);
        liveDataService.insertOrders(session2, items, orders, true, true);

        List<Order> savedOrders = orderRepository.findAll();
        List<Batch> savedBatches = batchRepository.findAll();
        assertEquals(3, savedOrders.size());
        assertEquals(0, savedBatches.size());
    }

    @Test
    public void testInsertOrders_duplicateBatches() throws Exception {
        setUpOrders();
        orderRepository.deleteAll();

        Printer printer = printerRepository.findOne(menuItem1.getDefaultPrinter());
        printer.setDuplicateTo(printer3.getId());
        printerRepository.save(printer);
        printer3.setRestaurantId(printer.getRestaurantId());
        printerRepository.save(printer3);

        List<MenuItem> items = new ArrayList<>();
        items.add(menuItem1);

        List<Order> orders = new ArrayList<>();
        orders.add(order1);

        liveDataService.insertOrders(session2, items, orders, true, true);

        List<Order> savedOrders = orderRepository.findAll();
        List<Batch> savedBatches = batchRepository.findAll();

        assertEquals(1, savedOrders.size());
        assertTrue(savedOrders.stream().anyMatch(o -> o.getId().equals(order1.getId())));

        assertEquals(2, savedBatches.size());
        savedBatches.get(0).setId(null);
        savedBatches.get(1).setId(null);
        assertEquals(savedBatches.get(0), savedBatches.get(1));

        for(Batch batch : savedBatches) {
            assertTrue(batch.getOrderIds().contains(order1.getId()));
        }

        List<String> batchSessionIds = savedBatches.stream().map(Batch::getSessionId).distinct().collect(Collectors.toList());
        assertEquals(1, batchSessionIds.size());
        assertEquals(session2.getId(), batchSessionIds.get(0));
        assertNull(savedBatches.get(0).getPrintedTime());
        assertNull(savedBatches.get(1).getPrintedTime());
    }

    @Test
    public void testInsertOrders_duplicateBatchesMultiple() throws Exception {
        setUpOrders();
        orderRepository.deleteAll();

        Printer printer = printerRepository.findOne(menuItem1.getDefaultPrinter());
        printer.setDuplicateTo(printer3.getId() + "," + printer.getId());
        printerRepository.save(printer);
        printer3.setRestaurantId(printer.getRestaurantId());
        printerRepository.save(printer3);

        List<MenuItem> items = new ArrayList<>();
        items.add(menuItem1);
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        liveDataService.insertOrders(session2, items, orders, true, true);

        List<Order> savedOrders = orderRepository.findAll();
        List<Batch> savedBatches = batchRepository.findAll();

        assertEquals(1, savedOrders.size());
        assertTrue(savedOrders.stream().anyMatch(o -> o.getId().equals(order1.getId())));

        assertEquals(3, savedBatches.size());
        savedBatches.get(0).setId(null);
        savedBatches.get(1).setId(null);
        savedBatches.get(2).setId(null);
        assertEquals(savedBatches.get(0), savedBatches.get(1));
        assertEquals(savedBatches.get(0), savedBatches.get(2));

        for(Batch batch : savedBatches) {
            assertTrue(batch.getOrderIds().contains(order1.getId()));
        }

        List<String> batchSessionIds = savedBatches.stream().map(Batch::getSessionId).distinct().collect(Collectors.toList());
        assertEquals(1, batchSessionIds.size());
        assertEquals(session2.getId(), batchSessionIds.get(0));
        assertNull(savedBatches.get(0).getPrintedTime());
        assertNull(savedBatches.get(1).getPrintedTime());
        assertTrue(savedBatches.get(0).isAwaitingImmediatePrint());
        assertTrue(savedBatches.get(1).isAwaitingImmediatePrint());
    }

    @Test
    public void testInsertOrdersNotImmediateBatches() throws Exception{
        setUpOrders();
        orderRepository.deleteAll();

        List<MenuItem> items = new ArrayList<>();
        items.add(menuItem1);
        items.add(menuItem2);
        items.add(menuItem3);

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        batchRepository.deleteAll();
        liveDataService.insertOrders(session2, items, orders, false, true);

        List<Batch> savedBatches = batchRepository.findAll();
        assertEquals(0, savedBatches.size());
    }

    @Test
    public void testInsertOrders2() throws Exception {
        setUpPrinters();
        setUpMenuItems();
        session2.setRestaurantId(restaurant2.getId());
        sessionRepository.save(session2);

        OrderRequest request1 = new OrderRequest();
        request1.setQuantity(0); //should be filtered out
        request1.setCourseId("1");
        request1.setMenuItemId(menuItem2.getId());
        request1.setDinerId(session2.getId() + IDAble.SEPARATOR + "0");
        request1.getModifiers().add(modifier1.getId());

        OrderRequest request2 = new OrderRequest();
        request2.setQuantity(2);
        request2.setCourseId("1");
        request2.setMenuItemId(menuItem2.getId());
        request2.setDinerId(session2.getId() + IDAble.SEPARATOR + "0");
        request2.getModifiers().add(modifier1.getId());

        List<OrderRequest> requests = new ArrayList<>();
        requests.add(request1);
        requests.add(request2);

        orderRepository.deleteAll();
        batchRepository.deleteAll();
        liveDataService.insertOrders(restaurant2.getId(), requests, null, true);

        List<Order> savedOrders = orderRepository.findAll();

        assertEquals(1, savedOrders.size());
        assertEquals(menuItem2.getId(), savedOrders.get(0).getMenuItemId());

        orderRepository.deleteAll();
        batchRepository.deleteAll();
        request1.setQuantity(1);

        liveDataService.insertOrders(restaurant2.getId(), requests, null, true);
        savedOrders = orderRepository.findAll();

        assertEquals(2, savedOrders.size());
        assertEquals(session2.getId(), savedOrders.get(0).getSessionId());
        assertEquals(session2.getId(), savedOrders.get(1).getSessionId());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertOrder() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertOrders() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetOrder() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetOrdersBySessionId() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetOrdersBySessionIds() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testVoidOrders() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsertDeviceDetail() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetBatches() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetBatchesToPrintBySessionId() throws Exception {
    }

    @Test
    public void testTablesInUse() throws Exception {
        setUpTablesInUse();

        List<String> list = new ArrayList<>();

        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list));
        list.add(table2.getId());
        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list));
        list.add(table1.getId());
        assertTrue(liveDataService.tablesInUse(restaurant2.getId(), list));
        list.add(table3.getId());
        assertTrue(liveDataService.tablesInUse(restaurant2.getId(), list));
        assertFalse(liveDataService.tablesInUse(restaurant1.getId(), list));
        assertFalse(liveDataService.tablesInUse(restaurant3.getId(), list));
    }

    private void setUpTablesInUse() {
        restaurant2.getFloors().add(floor1);
        floor1.getLayouts().get(0).getTables().add(table1.getId());
        floor1.getLayouts().get(0).getTables().add(table2.getId());
        floor1.getLayouts().get(0).getTables().add(table3.getId());
        floor1.setActiveLayout(floor1.getLayouts().get(0).getId());
        restaurant2.getTables().add(table1);
        restaurant2.getTables().add(table2);
        restaurant2.getTables().add(table3);
        restaurantRepository.save(restaurant2);

        session2.setRestaurantId(restaurant2.getId());
        session2.getTables().add(table1.getId());
        session2.getTables().add(table3.getId());
        session2.setSessionType(SessionType.SEATED);
        sessionRepository.save(session2);
    }

    @Test
    public void testTablesInUse2() throws Exception {
        setUpTablesInUse();

        List<String> list = new ArrayList<>();
        List<String> except = new ArrayList<>();

        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        list.add(table2.getId());
        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        list.add(table1.getId());
        assertTrue(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        list.add(table3.getId());
        assertTrue(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        assertFalse(liveDataService.tablesInUse(restaurant1.getId(), list, except));
        assertFalse(liveDataService.tablesInUse(restaurant3.getId(), list, except));

        list.clear();
        except.add(table1.getId());
        except.add(table3.getId());

        list.add(table2.getId());
        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        list.add(table1.getId());
        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        list.add(table3.getId());
        assertFalse(liveDataService.tablesInUse(restaurant2.getId(), list, except));
        assertFalse(liveDataService.tablesInUse(restaurant1.getId(), list, except));
        assertFalse(liveDataService.tablesInUse(restaurant3.getId(), list, except));
    }

    @Test
    public void testTablesInUseAsList() throws Exception {
        restaurant2.getFloors().add(floor1);
        floor1.getLayouts().get(0).getTables().add(table1.getId());
        floor1.getLayouts().get(0).getTables().add(table2.getId());
        floor1.getLayouts().get(0).getTables().add(table3.getId());
        floor1.setActiveLayout(floor1.getLayouts().get(0).getId());
        restaurant2.getTables().add(table1);
        restaurant2.getTables().add(table2);
        restaurant2.getTables().add(table3);
        restaurantRepository.save(restaurant2);

        session2.setRestaurantId(restaurant2.getId());
        session2.getTables().add(table1.getId());
        session2.setSessionType(SessionType.SEATED);
        session2.setStartTime(System.currentTimeMillis());
        session3.setRestaurantId(restaurant2.getId());
        session3.getTables().add(table3.getId());
        session3.setSessionType(SessionType.SEATED);
        session3.setStartTime(System.currentTimeMillis());
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        List<String> tablesInUse = liveDataService.tablesInUse(restaurant2.getId());

        assertTrue(tablesInUse.contains(table1.getId()));
        assertTrue(tablesInUse.contains(table3.getId()));
        assertFalse(tablesInUse.contains(table2.getId()));

        tablesInUse = liveDataService.tablesInUse(restaurant1.getId());

        assertEquals(0, tablesInUse.size());
    }

    @Test
    public void testClearCheckIn() throws Exception {
        checkIn2.setRestaurantId(restaurant2.getId());
        checkIn2.setCustomerId(customer3.getId());
        checkInRepository.save(checkIn2);

        liveDataService.clearCheckIn(restaurant2.getId(), customer2.getId());
        assertNull(checkInRepository.findOne(checkIn2.getId()).getDeleted());

        liveDataService.clearCheckIn(restaurant1.getId(), customer3.getId());
        assertNull(checkInRepository.findOne(checkIn2.getId()).getDeleted());

        liveDataService.clearCheckIn(restaurant2.getId(), customer3.getId());
        assertNotNull(checkInRepository.findOne(checkIn2.getId()).getDeleted());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDeleteOrders() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetBatchesBySessionId() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDeleteBatches() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDeleteParty() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetCheckInsByRestaurantId() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDeleteCheckIns() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testAddSpooledTime() throws Exception {

    }

    @Test
    public void testGetAssociatedSessions() throws Exception {
        session1.setRestaurantId(restaurant2.getId());
        session1.setStartTime(System.currentTimeMillis());
        session2.setRestaurantId(restaurant2.getId());
        session2.setStartTime(System.currentTimeMillis());
        session3.setRestaurantId(restaurant1.getId());
        session3.setStartTime(System.currentTimeMillis());

        booking1.setCustomerId(customer1.getId());
        booking1.setTargetTime(System.currentTimeMillis());
        booking1.setBookingType(BookingType.RESERVATION);
        booking2.setCustomerId(customer2.getId());
        booking2.setTargetTime(System.currentTimeMillis());
        booking2.setBookingType(BookingType.RESERVATION);
        booking3.setCustomerId(customer3.getId());
        booking3.setTargetTime(System.currentTimeMillis());
        booking3.setBookingType(BookingType.RESERVATION);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        session1.setOriginalBooking(booking1);
        session2.setOriginalBooking(booking2);
        session3.setOriginalBooking(booking3);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        Map<Booking,Session> map1 = liveDataService.getAssociatedSessionsByBookingRecent(customer1.getId(), BookingType.RESERVATION);
        Map<Booking,Session> map2 = liveDataService.getAssociatedSessionsByBookingRecent(customer1.getId(), BookingType.TAKEAWAY);

        assertEquals(1, map1.size());
        assertEquals(booking1.getId(), map1.keySet().iterator().next().getId());
        assertEquals(session1.getId(), map1.values().iterator().next().getId());
        assertEquals(0, map2.size());

        booking1.setBookingType(BookingType.TAKEAWAY);
        bookingRepository.save(booking1);

        map2 = liveDataService.getAssociatedSessionsByBookingRecent(customer1.getId(), BookingType.TAKEAWAY);
        assertEquals(1, map2.size());
        assertEquals(booking1.getId(), map2.keySet().iterator().next().getId());
        assertEquals(session1.getId(), map2.values().iterator().next().getId());

        booking1.setTargetTime(0);
        bookingRepository.save(booking1);

        map2 = liveDataService.getAssociatedSessionsByBookingRecent(customer1.getId(), BookingType.TAKEAWAY);
        assertEquals(0, map2.size());
    }

    @Test
    public void testCancelOrders() {
        List<Order> orders = new ArrayList<>();
        order1.setSessionId(session1.getId());
        order2.setSessionId(session1.getId());
        order3.setSessionId(session1.getId());
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);
        orderRepository.save(orders);

        batch1.setSessionId(session1.getId());
        batchRepository.save(batch1);

        liveDataService.cancelOrders(session1.getId(), orders);

        assertNull(orderRepository.findOne(order1.getId()));
        assertNull(orderRepository.findOne(order2.getId()));
        assertNull(orderRepository.findOne(order3.getId()));
        assertNull(batchRepository.findOne(batch1.getId()));
        assertNotNull(batchRepository.findOne(batch2.getId()));
    }

    @Test
    public void pushPublicFacingId() {
        List<Order> orders = new ArrayList<>();
        order1.setSessionId(session1.getId());
        order2.setSessionId(session1.getId());
        orders.add(order1);
        orders.add(order2);
        orderRepository.save(orders);

        String id = liveDataService.pushSelfServiceParameters(orders, "location");
        assertEquals(5, id.length());
        List<Order> fromDb = orderRepository.findBySessionId(session1.getId());
        for(Order order : fromDb) {
            assertEquals(id, order.getPublicFacingOrderId());
            assertEquals("location", order.getDeliveryLocation());
        }
    }

    @Test
    public void updateStockControl() {
        createItemsForStockControl();
        List<Order> orders = createOrders();
        liveDataService.updateStockControl(restaurant1, orders, true, false);

        List<StockLevel> allStock = stockLevelRepository.findAll();
        assertEquals(2, allStock.size());
        assertEquals(5, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(3, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());
        assertNull(stockLevelRepository.findByRestaurantIdAndPlu(restaurant3.getId(), "plu1"));

        liveDataService.updateStockControl(restaurant1, orders, true,false);
        allStock = stockLevelRepository.findAll();
        assertEquals(2, allStock.size());
        assertEquals(10, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(6, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());

        liveDataService.updateStockControl(restaurant1, orders, false,false);
        allStock = stockLevelRepository.findAll();
        assertEquals(2, allStock.size());
        assertEquals(5, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(3, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());

        liveDataService.updateStockControl(restaurant1, orders, false,false);
        liveDataService.updateStockControl(restaurant1, orders, false,false); //make it go negative
        assertEquals(2, allStock.size());
        assertEquals(0, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(0, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());
    }

    @Test
    public void updateStockControlUnavailable() {
        createItemsForStockControl();
        List<Order> orders = createOrders();
        liveDataService.updateStockControl(restaurant1, orders, true, true); //increments
        liveDataService.updateStockControl(restaurant1, orders, false, true);
        liveDataService.updateStockControl(restaurant1, orders, false, true);

        List<StockLevel> allStock = stockLevelRepository.findAll();
        assertEquals(2, allStock.size());
        List<MenuItem> byRestaurantIdAndPlu = masterDataService.getMenuItemsByPlu(restaurant1.getId(), Collections.singletonList("plu1"));
        assertEquals(2, byRestaurantIdAndPlu.size());
        assertTrue(byRestaurantIdAndPlu.stream().allMatch(MenuItem::isUnavailable));

        liveDataService.updateStockControl(restaurant1, orders, true, true);
        liveDataService.updateStockControl(restaurant1, orders, true, true);

        allStock = stockLevelRepository.findAll();
        assertEquals(2, allStock.size());
        byRestaurantIdAndPlu = masterDataService.getMenuItemsByPlu(restaurant1.getId(), Collections.singletonList("plu1"));
        assertEquals(2, byRestaurantIdAndPlu.size());
        assertTrue(byRestaurantIdAndPlu.stream().noneMatch(MenuItem::isUnavailable));
    }

    @Test
    public void updateStockControlTrackable() {
        createItemsForStockControl();
        List<Order> orders = createOrders();
        assertEquals(0, stockLevelRepository.findAll().size());

        // get the items to be created as StockLevels
        liveDataService.updateStockControl(restaurant1, orders, true, true);
        assertEquals(5, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(3, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());

        StockLevel plu1 = stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1");
        plu1.setTrackable(false);
        stockLevelRepository.save(plu1);
        liveDataService.updateStockControl(restaurant1, orders, true, true);
        assertEquals(5, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(6, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());

        plu1.setTrackable(true);
        stockLevelRepository.save(plu1);
        liveDataService.updateStockControl(restaurant1, orders, true, true);
        assertEquals(10, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu1").getLevel());
        assertEquals(9, stockLevelRepository.findByRestaurantIdAndPlu(restaurant1.getId(), "plu2").getLevel());
    }

    @Test
    public void testInsertCustomerInteraction() {
        CustomerInteractionDeferredSession customerInteractionDeferredSession = new CustomerInteractionDeferredSession(customer1.getId(), restaurant1.getId(), session1.getId(), staff1.getId());
        CustomerInteractionDeferredSession saved = liveDataService.saveInteraction(customerInteractionDeferredSession);
        assertNotNull(saved.getId());
        assertEquals(customerInteractionDeferredSession, customerInteractionRepository.findOne(saved.getId()));
    }

    @Test
    public void testGetCustomerInteractions() {
        CustomerInteractionDeferredSession customerInteractionDeferredSession1 = new CustomerInteractionDeferredSession(customer1.getId(), restaurant1.getId(), session1.getId(), staff1.getId());
        CustomerInteractionDeferredSession customerInteractionDeferredSession2 = new CustomerInteractionDeferredSession(customer1.getId(), restaurant1.getId(), session2.getId(), staff1.getId());
        customerInteractionDeferredSession2.setPaid(true);
        CustomerInteractionDeferredSession customerInteractionDeferredSession3 = new CustomerInteractionDeferredSession(customer1.getId(), restaurant2.getId(), session3.getId(), staff1.getId());
        CustomerInteractionDeferredSession customerInteractionDeferredSession4 = new CustomerInteractionDeferredSession(customer1.getId(), restaurant2.getId(), "foo", staff1.getId());
        customerInteractionDeferredSession4.setPaid(true);
        CustomerInteractionDeferredSession c1 = liveDataService.saveInteraction(customerInteractionDeferredSession1);
        CustomerInteractionDeferredSession c2 = liveDataService.saveInteraction(customerInteractionDeferredSession2);
        CustomerInteractionDeferredSession c3 = liveDataService.saveInteraction(customerInteractionDeferredSession3);
        CustomerInteractionDeferredSession c4 = liveDataService.saveInteraction(customerInteractionDeferredSession4);

        List<CustomerInteractionDeferredSession> deferredSessions1 = liveDataService.getDeferredSessions(restaurant1.getId(), false);
        assertEquals(1, deferredSessions1.size());
        assertEquals(c1.getId(), deferredSessions1.get(0).getId());
        List<CustomerInteractionDeferredSession> deferredSessions2 = liveDataService.getDeferredSessions(restaurant1.getId(), true);
        assertEquals(1, deferredSessions2.size());
        assertEquals(c2.getId(), deferredSessions2.get(0).getId());
    }

    private List<Order> createOrders() {
        Order order1 = new Order();
        order1.setMenuItem(menuItem1);
        order1.setQuantity(2);
        Order order2 = new Order();
        order2.setMenuItem(menuItem2);
        order2.setQuantity(3);
        order2.setModifiers(Collections.singletonList(modifier1));
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        return orders;
    }

    private void createItemsForStockControl() {
        menuItem1.getModifierGroupIds().clear();
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setPlu("plu1");
        menuItem1 = menuItemRepository.save(menuItem1);
        menuItem2.getModifierGroupIds().clear();
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem2.setPlu("plu1");
        menuItem2.getModifierGroupIds().clear();
        menuItem2.getModifierGroupIds().add(modifierGroup1.getId());
        modifier1.setPlu("plu2");
        modifierRepository.save(modifier1);
        menuItem2 = menuItemRepository.save(menuItem2);
        modifierGroup1.setRestaurantId(restaurant1.getId());
        modifierGroup1.getModifiers().clear();
        modifierGroup1.getModifiers().add(modifier1);
        modifierGroupRepository.save(modifierGroup1);
        menuItem3.setRestaurantId(restaurant3.getId());
        menuItem3.getModifierGroupIds().clear();
        menuItem3.setPlu("plu1");
        menuItem3 = menuItemRepository.save(menuItem3);
    }
}
