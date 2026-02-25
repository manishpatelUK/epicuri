package uk.co.epicuri.serverapi.service;

import de.flapdoodle.embed.process.collections.Collections;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderItemView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.host.*;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HistoricalDataWrapper;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostSessionView;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionPayload;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class SessionServiceTest extends SessionSetupBaseIT{

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSession() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSessions() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpsert() throws Exception {

    }

    @Test
    public void testGetSessionView() throws Exception {
        setUpSession();

        //session without booking
        HostSessionView view1 = sessionService.getSessionView(session1);
        assertEqual(view1, session1, table1, order1, order2, order3, diner1, diner2, diner3);

        //add adjustments
        session1.getAdjustments().add(adjustment1);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertEquals(1, view1.getAdjustments().size());

        //session with booking + rejected
        booking1.setCustomerId(customer1.getId());
        booking1 = bookingRepository.save(booking1);
        session1.setOriginalBooking(booking1);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        /*assertNotNull(view1.getCustomer());
        assertEquals(customer1.getId(), view1.getCustomer().getId());*/
        assertEquals(booking1.getTargetTime()/1000, view1.getExpectedTime().longValue());

        booking1.setRejected(true);
        booking1.setAccepted(false);
        booking1.setRejectionNotice("you reject");
        booking1 = bookingRepository.save(booking1);
        session1.setOriginalBooking(booking1);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertFalse(view1.isAccepted());
        assertTrue(view1.isRejected());
        assertEquals(booking1.getRejectionNotice(), view1.getRejectionNotice());

        // tips
        session1.setTipPercentage(12.5);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertTrue(view1.getTips() > 0);
        assertEquals(session1.getTipPercentage(), view1.getTipTotal(), 0.001);

        //notifications + delays
        booking1.setTargetTime(System.currentTimeMillis() - 60000);
        booking1 = bookingRepository.save(booking1);
        session1.setStartTime(System.currentTimeMillis());
        session1.setDelay(60000);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertTrue(view1.getDelay() > 0);

        Notification adhoc = new Notification();
        adhoc.setNotificationType(NotificationType.ADHOC);
        adhoc.setRestaurantId(restaurant1.getId());
        adhoc.setSessionId(session1.getId());
        adhoc.setTime(System.currentTimeMillis());
        notificationRepository.save(adhoc);

        view1 = sessionService.getSessionView(session1);
        assertEquals(1, view1.getAdhocEvents().size());

        adhoc.setAcknowledged(System.currentTimeMillis());
        notificationRepository.save(adhoc);

        view1 = sessionService.getSessionView(session1);
        assertEquals(1, view1.getAdhocEvents().size());
        assertEquals(1, view1.getAdhocEvents().get(0).getAcknowledgements().size());

        ScheduledItem recurring = new ScheduledItem();
        recurring.setId("s4");
        recurring.setNotificationType(NotificationType.RECURRING);
        recurring.setText("Slap out the menus");
        recurring.setInitialDelay(3);
        schedule1.getRecurringItems().add(recurring);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertNull(view1.getRecurringEvents());

        Notification recurringNotification = new Notification();
        recurringNotification.setSessionId(session1.getId());
        recurringNotification.setRestaurantId(restaurant1.getId());
        recurringNotification.setScheduledItemId(recurring.getId());
        recurringNotification.setNotificationType(NotificationType.RECURRING);
        notificationRepository.save(recurringNotification);

        view1 = sessionService.getSessionView(session1);
        assertEquals(1, view1.getRecurringEvents().size());

        //voided
        VoidReason voidReason = new VoidReason();
        voidReason.setDescription("foo");
        staff1.setRestaurantId(restaurant1.getId());
        staff1 = staffRepository.save(staff1);
        voidReason.setStaffId(staff1.getId());
        voidReason.setTime(System.currentTimeMillis());
        session1.setVoidReason(voidReason);

        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertTrue(view1.isVoided());

        //bill requested
        session1.setBillRequested(true);
        view1 = sessionService.getSessionView(session1);
        assertTrue(view1.isBillRequested());

        //takeaway session
        setUpTakeawaySession(TakeawayType.COLLECTION);

        view1 = sessionService.getSessionView(session1);
        assertEquals(session1.getId(), view1.getId());
        assertEquals("Collection",view1.getSessionType());
        assertEquals(session1.getService().getId(), view1.getServiceId());
        assertEquals(session1.getService().getName(), view1.getServiceName());
        assertEquals(false, view1.getAdhoc());
        assertEquals(session1.isBillRequested(), view1.isBillRequested());
        assertEquals(session1.getVoidReason() == null, !view1.isVoided());
        assertEquals(session1.getDelay()/1000, view1.getDelay());
        assertEquals(null, view1.getMessage());
        assertEquals(null, view1.getDeliveryAddress());
        assertEquals(session1.getStartTime()/1000, view1.getStartTime());
        assertEquals(0, view1.getClosedTime());
        assertEquals(session1.getService().getDefaultMenuId(), view1.getMenuId());
        //assertEquals(null, view1.getCustomer());
        assertEquals(null, view1.getTelephone());
        assertEquals(session1.getOriginalParty().getName(), view1.getPartyName());
        assertEquals(session1.getName(), view1.getName());
        assertEquals(null, view1.getExpectedTime());
        assertEquals(session1.getService().getDefaultMenuId(), view1.getTakeawayMenuId());
        assertNull(view1.getTables());
        assertEquals(1, view1.getDiners().size());
        assertTrue(view1.getDiners().get(0).isTable());
        assertEquals(3, view1.getOrders().size());
        assertNull(view1.getScheduledEvents());
        assertNull(view1.getRecurringEvents());
        assertNull(view1.getAdhocEvents());
        assertEquals(0, view1.getAdjustments().size());
        assertEquals(0, Double.compare(0D,view1.getTips()));
        assertTrue(view1.isAccepted());
        assertFalse(view1.isRejected());
        assertNull(view1.getRejectionNotice());
        assertFalse(view1.isPaid());
        assertNull(view1.getVoidReason());
        assertTrue(StringUtils.isBlank(view1.getChairData()));

        //takeaway with booking
        booking2.setTelephone("1234");
        booking2 = bookingRepository.save(booking2);
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setOriginalBooking(booking2);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertEquals(booking2.getTelephone(), view1.getTelephone());

        //delivery
        setUpTakeawaySession(TakeawayType.DELIVERY);
        booking2.setTelephone("1234");
        Address address = new Address();
        address.setStreet("df");
        address.setTown("df");
        address.setCity("df");
        address.setPostcode("df");
        booking2.setDeliveryAddress(address);
        booking2 = bookingRepository.save(booking2);
        session1.setOriginalBooking(booking2);
        session1 = sessionRepository.save(session1);

        view1 = sessionService.getSessionView(session1);
        assertEquals(booking2.getTelephone(), view1.getTelephone());
        assertEquals(booking2.getDeliveryAddress(), view1.getDeliveryAddress());

        booking2.setCustomerId(customer1.getId());
        booking2 = bookingRepository.save(booking2);
        view1 = sessionService.getSessionView(session1);
        //assertEquals(customer1.getId(), view1.getCustomer().getId());

        //add payments
        Adjustment payment = new Adjustment();
        payment.setAdjustmentType(adjustmentType2);
        payment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        payment.setStaffId(staff1.getId());
        payment.setValue(1);
        payment.setId(session1.getId() + IDAble.SEPARATOR + "sdf");
        session1.getAdjustments().add(payment);

        view1 = sessionService.getSessionView(session1);
        assertTrue(view1.getRemainingTotal() > 0);
        assertEquals(0.01, view1.getAdjustments().get(0).getValue(), 0.01);

        payment.setValue(100000);
        view1 = sessionService.getSessionView(session1);
        assertEquals(-996.34, view1.getRemainingTotal(), 0.001);


        //add discounts
        session1.getAdjustments().clear();
        Adjustment discount = new Adjustment();
        discount.setAdjustmentType(adjustmentType1);
        discount.setNumericalType(NumericalAdjustmentType.PERCENTAGE);
        discount.setStaffId(staff1.getId());
        discount.setValue(MoneyService.percentageDiscountToInt(50));
        discount.setId(session1.getId() + IDAble.SEPARATOR + "sdf");
        session1.getAdjustments().add(discount);

        view1 = sessionService.getSessionView(session1);
        assertTrue(view1.getRemainingTotal() > 0);
        assertEquals(50, view1.getAdjustments().get(0).getValue(), 0.01);
        HostOrderView hostOrderView = view1.getOrders().stream().filter(o -> o.getId().equals(order1.getId())).findFirst().orElse(null);
        assertNull(hostOrderView.getPriceOverride());

        Adjustment adjustment = new Adjustment(adjustmentType1.getId());
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setValue(order1.getPriceOverride());
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setStaffId(staff1.getId());
        order1.setAdjustment(adjustment);
        orderRepository.save(order1);

        view1 = sessionService.getSessionView(session1);
        hostOrderView = view1.getOrders().stream().filter(o -> o.getId().equals(order1.getId())).findFirst().orElse(null);
        assertNotNull(hostOrderView.getPriceOverride());
        assertEquals(0D, hostOrderView.getPriceOverride(), 0.01);
    }

    @Test
    public void testGetSessionViewDeletedOrders() throws Exception{
        setUpSession();

        order1.setDeleted(1L);
        orderRepository.save(order1);
        HostSessionView view = sessionService.getSessionView(session1);
        assertFalse(view.getOrders().stream().anyMatch(o -> o.getId().equals(order1.getId())));
    }

    public static void assertEqual(HostSessionView view, Session session, Table table1, Order order1, Order order2, Order order3, Diner diner1, Diner diner2, Diner diner3) {
        assertEquals(session.getId(), view.getId());
        assertEquals("Seated", view.getSessionType());
        assertEquals(session.getService().getId(), view.getServiceId());
        assertEquals(session.getService().getName(), view.getServiceName());
        assertEquals(false, view.getAdhoc());
        assertEquals(session.isBillRequested(), view.isBillRequested());
        assertEquals(session.getVoidReason() == null, !view.isVoided());
        assertEquals(session.getDelay()/1000, view.getDelay());
        assertEquals(null, view.getMessage());
        assertEquals(null, view.getDeliveryAddress());
        assertEquals(session.getStartTime()/1000, view.getStartTime());
        assertEquals(0, view.getClosedTime());
        assertEquals(session.getService().getDefaultMenuId(), view.getMenuId());
        //assertEquals(null, view.getCustomer());
        assertEquals(null, view.getTelephone());
        assertEquals(session.getOriginalParty().getName(), view.getPartyName());
        assertEquals(session.getName(), view.getName());
        assertEquals(null, view.getExpectedTime());
        assertEquals(null, view.getTakeawayMenuId());
        assertEquals(1, view.getTables().size());
        assertEquals(table1.getId(), view.getTables().get(0).getId());
        assertEquals(3, view.getDiners().size());
        assertTrue(view.getDiners().get(0).isTable());
        assertEquals(3, view.getOrders().size());
        assertEquals(diner1.getId(), view.getOrders().stream().filter(o -> o.getId().equals(order1.getId())).findFirst().get().getDinerId());
        assertEquals(diner2.getId(), view.getOrders().stream().filter(o -> o.getId().equals(order2.getId())).findFirst().get().getDinerId());
        assertEquals(diner3.getId(), view.getOrders().stream().filter(o -> o.getId().equals(order3.getId())).findFirst().get().getDinerId());
        assertEquals(3, view.getScheduledEvents().size());
        assertNull(view.getRecurringEvents());
        assertNull(view.getAdhocEvents());
        assertEquals(0, view.getAdjustments().size());
        assertEquals(0, Double.compare(0D, view.getTips()));
        if(session.getOriginalBooking() != null) {
            Booking booking = session.getOriginalBooking();
            assertEquals(booking.isAccepted(), view.isAccepted());
            assertEquals(booking.isRejected(), view.isRejected());
            if(booking.getDeleted() != null) {
                assertTrue(view.isDeleted());
            }
            assertEquals(booking.getRejectionNotice(), view.getRejectionNotice());
        }
        assertFalse(view.isPaid());
        assertNull(view.getVoidReason());
        assertTrue(StringUtils.isBlank(view.getChairData()));
    }

    public static void assertEqual(HostSessionView view, Session session, Booking booking) throws Exception{
        assertEquals(session.getId(), view.getId());

        if(session.getSessionType() == SessionType.NONE) {
            fail();
        } else if(session.getSessionType() == SessionType.ADHOC) {
            assertEquals("Seated", view.getSessionType());
        } else if(session.getSessionType() == SessionType.SEATED) {
            assertEquals("Seated", view.getSessionType());
        } else if(session.getSessionType() == SessionType.TAB) {
            assertEquals("Seated", view.getSessionType());
        } else if(booking == null) {
            assertNotEquals("Delivery", view.getSessionType());
            assertNotEquals("Collection", view.getSessionType());
        }

        assertEquals(session.getService().getId(), view.getServiceId());
        assertEquals(session.getService().getName(), view.getServiceName());
        if(session.getService().getDefaultMenuId() != null) {
            assertEquals(session.getService().getDefaultMenuId(), view.getMenuId());
        }
        assertEquals(false, view.getAdhoc());
        assertEquals(session.isBillRequested(), view.isBillRequested());
        assertEquals(session.getVoidReason() == null, !view.isVoided());
        assertEquals(session.getVoidReason() != null, view.isVoided());
        assertEquals(session.getDelay()/1000, view.getDelay());
        assertEquals(session.getStartTime()/1000, view.getStartTime());
        if(session.getClosedTime() != null) {
            assertEquals(session.getClosedTime()/1000, view.getClosedTime());
        } else {
            assertEquals(0, view.getClosedTime());
        }

        if(session.getOriginalPartyId() != null) {
            assertNotNull(view.getPartyName());
        }

        assertEquals(session.getName(), view.getName());

        if(session.getSessionType() == SessionType.TAKEAWAY) {
            assertEquals(1, view.getDiners().size());
            assertFalse(view.getDiners().get(0).isTable());
            assertNull(view.getPartyName());
            assertEquals(session.getService().getDefaultMenuId(), view.getTakeawayMenuId());
            assertNotNull(view.getCustomer());
            assertNotNull(view.getCustomer().getId());
            assertNotNull(view.getCustomer().getOrderIds().size());
        }

        if(session.getTipPercentage() == null) {
            assertEquals(0D, view.getTipTotal(), 0.001);
        } else {
            assertEquals(session.getTipPercentage(), view.getTipTotal(), 0.001);
        }

        if(booking != null) {

            if(booking.getTakeawayType() == TakeawayType.DELIVERY) {
                assertEquals("Delivery", view.getSessionType());
                assertNotNull(view.getDeliveryCost());
            } else if (booking.getTakeawayType() == TakeawayType.COLLECTION) {
                assertEquals("Collection", view.getSessionType());
                assertEquals(0D,view.getDeliveryCost(),0.001);
            }

            assertEquals(booking.isAccepted(), view.isAccepted());
            assertEquals(booking.isRejected(), view.isRejected());

            if (booking.getDeliveryAddress() != null) {
                assertEquals(view.getDeliveryAddress(), booking.getDeliveryAddress());
            }
            if (booking.getNotes() != null) {
                assertEquals(booking.getNotes(), view.getMessage());
            }
            if (booking.getTelephone() != null) {
                assertEquals(booking.getTelephone(), view.getTelephone());
            }
            if (booking.getRejectionNotice() != null) {
                assertEquals(booking.getRejectionNotice(), view.getRejectionNotice());
            }
            assertEquals(booking.getTargetTime() / 1000, (long) view.getExpectedTime());
        } else {
            assertNull(view.getAccepted());
            assertNull(view.getRejected());
            assertNull(view.getDeliveryCost());
        }

        if(session.getDiners().stream().anyMatch(d -> d.getCustomerId() != null)) {
            Diner diner = session.getDiners().stream().filter(d -> d.getCustomerId() != null).findFirst().orElse(null);
            String customerId = diner.getCustomerId();
            assertNotNull(view.getCustomer());
            assertEquals(customerId, view.getCustomer().getId());
        }

        if(session.getSessionType() != SessionType.TAKEAWAY) {
            assertEquals(session.getTables(), view.getTables().stream().map(HostTableView::getId).collect(Collectors.toList()));
        } else {
            assertNull(view.getTables());
            assertEquals(session.getService().getDefaultMenuId(), view.getTakeawayMenuId());
        }
        assertEquals(session.getAdjustments().size(), view.getAdjustments().size());

        if(session.getVoidReason() != null) {
            assertEquals(session.getVoidReason().getDescription(), view.getVoidReason().getReason());
            assertEquals(session.getVoidReason().getTime() / 1000, view.getVoidReason().getVoidTime());
        }

        if(session.getSessionType() != SessionType.TAKEAWAY) {
            if (session.getChairData().size() > 0) {
                assertEquals(ControllerUtil.OBJECT_MAPPER.writeValueAsString(session.getChairData()), view.getChairData());
            } else {
                assertEquals("", view.getChairData());
            }
        } else {
            assertNull(view.getChairData());
        }

        if(session.getDeleted() != null) {
            assertTrue(view.isDeleted());
        }
    }

    private void setUpTakeawaySession(TakeawayType type) throws Exception{
        setUpSession();

        notificationRepository.deleteAll();
        session1.setSessionType(SessionType.TAKEAWAY);
        session1.setTakeawayType(type);
        session1.getDiners().remove(1);
        session1.getDiners().remove(1);
        session1.getTables().clear();
        order1.setDinerId(session1.getDiners().get(0).getId());
        order2.setDinerId(session1.getDiners().get(0).getId());
        order3.setDinerId(session1.getDiners().get(0).getId());
        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
        order3 = orderRepository.save(order3);
        session1.getService().setSchedule(null);
        session1 = sessionRepository.save(session1);
    }

    @Test
    public void testGetAllTakeawaySessions() throws Exception {
        setUpMixedSessions();

        List<HostSessionView> takeaways = sessionService.getAllTakeawaySessions(restaurant1.getId(), 0, 10);
        assertEquals(2, takeaways.size());
        takeaways = sessionService.getAllTakeawaySessions(restaurant1.getId(), 0, 11);
        assertEquals(2, takeaways.size());
        takeaways = sessionService.getAllTakeawaySessions(restaurant1.getId(), 0, 9);
        assertEquals(0, takeaways.size());

        session1.setRestaurantId(restaurant2.getId());
        booking1.setRestaurantId(restaurant2.getId());
        sessionRepository.save(session1);
        bookingRepository.save(booking1);
        takeaways = sessionService.getAllTakeawaySessions(restaurant1.getId(), 0, 11);
        assertEquals(1, takeaways.size());

        session2.setClosedTime(11L);
        sessionRepository.save(session2);
        takeaways = sessionService.getAllTakeawaySessions(restaurant1.getId(), 0, 11);
        assertEquals(1, takeaways.size());
    }

    @Test
    public void testGetAllSessions() throws Exception {
        setUpMixedSessions();

        List<HostSessionView> list = sessionService.getAllSessions(restaurant1.getId());
        assertEquals(4, list.size());

        for(HostSessionView view : list) {
            Session session = sessionRepository.findOne(view.getId());
            Booking booking = session.getOriginalBooking();
            assertEqual(view, session, booking);
        }
    }

    @Test
    public void testGetAllSessionsAndOrdersByOpenTime() throws Exception {
        sessionRepository.deleteAll();
        orderRepository.deleteAll();

        Session s1, s2, s3, s4, s5;
        sessionRepository.insert(s1 = createSession(0, 0L));
        sessionRepository.insert(s2 = createSession(1, null));
        sessionRepository.insert(s3 = createSession(2, 3L));
        sessionRepository.insert(s4 = createSession(3, 3L));
        sessionRepository.insert(s5 = createSession(4, 4L));

        addOrderToSession(s1);
        addOrderToSession(s2);
        addOrderToSession(s3);
        addOrderToSession(s4);
        addOrderToSession(s5);

        HistoricalDataWrapper historicalDataWrapper = sessionService.getAllSessionsAndOrdersByOpenTime(restaurant1.getId(), 1, 3);
        assertEquals(0, historicalDataWrapper.getOldData().size());
        assertEquals(0, historicalDataWrapper.getOldOrders().size());
        assertEquals(3, historicalDataWrapper.getLiveData().size());
        assertEquals(3, historicalDataWrapper.getLiveOrders().size());
    }

    @Test
    public void testGetAllSessionsAndOrdersByCloseTime() throws Exception {
        sessionRepository.deleteAll();
        orderRepository.deleteAll();

        Session s1, s2, s3, s4, s5;
        sessionRepository.insert(s1 = createSession(0, 0L));
        sessionRepository.insert(s2 = createSession(1, null));
        sessionRepository.insert(s3 = createSession(2, 3L));
        sessionRepository.insert(s4 = createSession(3, 3L));
        sessionRepository.insert(s5 = createSession(4, 4L));

        addOrderToSession(s1);
        addOrderToSession(s2);
        addOrderToSession(s3);
        addOrderToSession(s4);
        addOrderToSession(s5);

        HistoricalDataWrapper historicalDataWrapper = sessionService.getAllSessionsAndOrdersByCloseTime(restaurant1.getId(), 1, 3);
        assertEquals(0, historicalDataWrapper.getOldData().size());
        assertEquals(0, historicalDataWrapper.getOldOrders().size());
        assertEquals(2, historicalDataWrapper.getLiveData().size());
        assertEquals(2, historicalDataWrapper.getLiveOrders().size());
    }

    @Test
    public void testGetAllSessionsAndOrdersByOpenWhenArchived() throws Exception {
        sessionRepository.deleteAll();
        orderRepository.deleteAll();

        Session s1, s2, s3, s4, s5;
        sessionRepository.insert(s1 = createSession(0, 0L));
        sessionRepository.insert(s2 = createSession(1, null));
        sessionRepository.insert(s3 = createSession(2, 3L));
        sessionRepository.insert(s4 = createSession(3, 3L));
        sessionRepository.insert(s5 = createSession(4, 4L));

        addOrderToSession(s1);
        addOrderToSession(s2);
        addOrderToSession(s3);
        addOrderToSession(s4);
        addOrderToSession(s5);

        sessionService.clearWithSession(s3, true, true, true, true, true);
        sessionService.clearWithSession(s4, true, true, true, true, true);
        sessionService.clearWithSession(s5, true, true, true, true, true);

        HistoricalDataWrapper historicalDataWrapper = sessionService.getAllSessionsAndOrdersByOpenTime(restaurant1.getId(), 1, 3);
        assertEquals(2, historicalDataWrapper.getOldData().size());
        assertEquals(2, historicalDataWrapper.getOldOrders().size());
        assertEquals(1, historicalDataWrapper.getLiveData().size());
        assertEquals(1, historicalDataWrapper.getLiveOrders().size());
    }

    @Test
    public void testGetAllSessionsAndOrdersByCloseWhenArchived() throws Exception {
        sessionRepository.deleteAll();
        orderRepository.deleteAll();

        Session s1, s2, s3, s4, s5;
        sessionRepository.insert(s1 = createSession(0, 0L));
        sessionRepository.insert(s2 = createSession(1, null));
        sessionRepository.insert(s3 = createSession(2, 3L));
        sessionRepository.insert(s4 = createSession(3, 3L));
        sessionRepository.insert(s5 = createSession(4, 4L));

        addOrderToSession(s1);
        addOrderToSession(s2);
        addOrderToSession(s3);
        addOrderToSession(s4);
        addOrderToSession(s5);

        sessionService.clearWithSession(s3, true, true, true, true, true);
        sessionService.clearWithSession(s4, true, true, true, true, true);
        sessionService.clearWithSession(s5, true, true, true, true, true);

        HistoricalDataWrapper historicalDataWrapper = sessionService.getAllSessionsAndOrdersByCloseTime(restaurant1.getId(), 1, 3);
        assertEquals(2, historicalDataWrapper.getOldData().size());
        assertEquals(2, historicalDataWrapper.getOldOrders().size());
        assertEquals(0, historicalDataWrapper.getLiveData().size());
        assertEquals(0, historicalDataWrapper.getLiveOrders().size());
    }

    private Session createSession(long time, Long closedTime) {
        Session session = new Session();
        session.setRestaurantId(restaurant1.getId());
        session.setStartTime(time);
        session.setClosedTime(closedTime);
        return session;
    }

    private void addOrderToSession(Session session) {
        Order order = new Order();
        order.setQuantity(2);
        order.setSessionId(session.getId());
        order.setTime(session.getStartTime());
        orderRepository.insert(order);
    }

    @Ignore("thin layer to repository")
    @Test
    public void testAddDiners() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testAddDinerToSession() throws Exception {

    }

    @Test
    public void testRemoveDinerFromSession() throws Exception {
        setUpSession();

        assertTrue(session1.getDiners().stream().anyMatch(d -> d.getId().equals(diner2.getId())));
        sessionService.removeDinerFromSession(diner2.getId());
        session1 = sessionRepository.findOne(session1.getId());

        assertFalse(session1.getDiners().stream().anyMatch(d -> d.getId().equals(diner2.getId())));
        assertEquals(2, session1.getDiners().size());
    }

    @Ignore("thin layer to repository")
    @Test
    public void testRemoveDinerFromSession1() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateService() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateName() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateType() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateTables() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testRequestBill() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUnRequestBill() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testRemoveFromReports() throws Exception {

    }

    @Test
    public void testMarkPaid() throws Exception {
        setUpSession();

        sessionService.markPaid(session1.getId(), true, staff1.getId());
        session1 = sessionRepository.findOne(session1.getId());

        assertNotNull(session1.getClosedTime());
        assertTrue(session1.isMarkedAsPaid());
        assertEquals(staff1.getId(), session1.getClosedBy());

        setUpSession();

        sessionService.markPaid(session1.getId(), false, staff1.getId());
        session1 = sessionRepository.findOne(session1.getId());

        assertNull(session1.getClosedTime());
        assertFalse(session1.isMarkedAsPaid());
        assertNull(session1.getClosedBy());

        setUpSession();

        session1.setSessionType(SessionType.TAB);
        sessionRepository.save(session1);
        sessionService.markPaid(session1.getId(), true, staff1.getId());
        session1 = sessionRepository.findOne(session1.getId());

        assertNotNull(session1.getClosedTime());
        assertTrue(session1.isMarkedAsPaid());
        assertEquals(staff1.getId(), session1.getClosedBy());

        setUpSession();

        session1.setSessionType(SessionType.TAKEAWAY);
        sessionRepository.save(session1);
        sessionService.markPaid(session1.getId(), true, staff1.getId());
        session1 = sessionRepository.findOne(session1.getId());

        assertNotNull(session1.getClosedTime());
        assertTrue(session1.isMarkedAsPaid());
        assertEquals(staff1.getId(), session1.getClosedBy());
    }

    @Test
    public void markDeferredPaid() throws Exception {
        CustomerInteractionDeferredSession customerInteractionDeferredSession = new CustomerInteractionDeferredSession(customer1.getId(), restaurant1.getId(), session1.getId(), staff1.getId());
        CustomerInteractionDeferredSession saved = liveDataService.saveInteraction(customerInteractionDeferredSession);

        assertFalse(saved.isPaid());
        String id = saved.getId();
        sessionService.markDeferredPaid(id, session1.getId(), true);
        CustomerInteractionDeferredSession updated = (CustomerInteractionDeferredSession) customerInteractionRepository.findOne(id);
        assertTrue(updated.isPaid());
        assertEquals(session1.getId(), updated.getSettlementSessionId());
        sessionService.markDeferredPaid(id, null, false);
        updated = (CustomerInteractionDeferredSession) customerInteractionRepository.findOne(id);
        assertFalse(updated.isPaid());
        assertNull(updated.getSettlementSessionId());
    }

    @Ignore("thin layer to repository")
    @Test
    public void testMarkClosed() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateClosed() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateVoidReason() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateUnVoid() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testExists() throws Exception {

    }

    @Test
    public void testCreateTakeaway() throws Exception {
        customer2.setFirstName("foo");
        customer2.setLastName("bar");
        customerRepository.save(customer2);
        setUpMenuAndRestaurant();

        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest();
        Session session = sessionService.createTakeaway(request, customer2.getId(), new ArrayList<>(), null).getA();

        assertEquals(SessionType.TAKEAWAY, session.getSessionType());
        assertEquals(TakeawayType.COLLECTION, session.getTakeawayType());
        assertEquals(restaurant2.getId(), session.getRestaurantId());
        assertEquals(1, orderRepository.findBySessionId(session.getId()).size());
        assertNotNull(session.getOriginalBooking());
        assertEquals(customer2.getId(), session.getOriginalBooking().getCustomerId());
        assertEquals(customer2.getFirstName() + " " + customer2.getLastName(), session.getName());
        assertNotNull(session.getOriginalBookingId());
        assertEquals(session.getOriginalBooking().getId(), session.getOriginalBookingId());
        Booking booking = bookingRepository.findOne(session.getOriginalBookingId());
        assertNotNull(booking);
        assertNotNull(booking.getRestaurantId());
        assertEquals(session.getRestaurantId(), booking.getRestaurantId());
        assertEquals(customer2.getFirstName() + " " + customer2.getLastName(), booking.getName());
        List<Order> orders = liveDataService.getOrders(session.getId());
        assertEquals(1, orders.size());
        assertEquals(course2.getId(), orders.get(0).getCourseId());
        final String sessionId = session.getId();
        final long sessionStart = session.getStartTime();
        List<Batch> batches = batchRepository.findAll().stream().filter(b -> b.getSessionId().equals(sessionId)).collect(Collectors.toList());
        /*assertTrue(batches.size()>0);
        assertTrue(batches.stream().allMatch(b -> b.getIntendedPrintTime() < sessionStart));*/
        try {
            assertTrue(Integer.parseInt(session.getReadableId()) > 0);
        } catch (NumberFormatException ex) {
            fail();
        }


        setUp();//clear stuff out
        setUpMenuAndRestaurant();
        request = getCustomerTakeawayOrderRequest();
        request.getItems().get(0).setModifiers(new ArrayList<>());
        request.getItems().get(0).getModifiers().add(modifier1.getId());
        modifier1.setTaxTypeId(tax1.getId());
        modifierRepository.save(modifier1);
        session = sessionService.createTakeaway(request, customer2.getId(), new ArrayList<>(), null).getA();

        assertEquals(SessionType.TAKEAWAY, session.getSessionType());
        assertEquals(restaurant2.getId(), session.getRestaurantId());
        assertEquals(1, orderRepository.findBySessionId(session.getId()).get(0).getModifiers().size());

        setUp();//clear stuff out
        setUpMenuAndRestaurant();
        request = getCustomerTakeawayOrderRequest();
        request.getItems().get(0).setModifiers(new ArrayList<>());
        request.getItems().get(0).getModifiers().add(modifier1.getId());
        modifier1.setTaxTypeId(tax1.getId());
        modifierRepository.save(modifier1);
        request.setNotes("Some note");
        session = sessionService.createTakeaway(request, customer2.getId(), new ArrayList<>(), null).getA();

        assertEquals(request.getNotes(), session.getOriginalBooking().getNotes());

        setUp();//clear stuff out
        setUpMenuAndRestaurant();
        request = getCustomerTakeawayOrderRequest();
        Address address = new Address();
        address.setCity("leicester");
        address.setPostcode("le7 9ud");
        address.setStreet("pulford drive");
        customer2.setAddress(address);
        request.setAddress(address);
        customerRepository.save(customer2);
        request.setDelivery(true);
        session = sessionService.createTakeaway(request, customer2.getId(), new ArrayList<>(), null).getA();
        booking = bookingRepository.findOne(session.getOriginalBookingId());

        assertEquals(session.getOriginalBooking().getDeliveryAddress(), address);
        assertEquals(booking.getDeliveryAddress(), address);
        assertEquals(TakeawayType.DELIVERY, session.getTakeawayType());
    }

    private void setUpMenuAndRestaurant() {
        setUpMenuItems(restaurant2);

        // set up the menu
        menu2.setRestaurantId(restaurant2.getId());
        Category testCategory = new Category();
        testCategory.setId(IDAble.generateId(menu2.getId()));
        testCategory.getCourseIds().add(course2.getId());
        menu2.getCategories().add(testCategory);
        Group testGroup = new Group();
        testGroup.setId(IDAble.generateId(testCategory.getId()));
        testGroup.getItems().add(menuItem1.getId());
        testGroup.getItems().add(menuItem2.getId());
        testGroup.getItems().add(menuItem3.getId());
        testCategory.getGroups().add(testGroup);
        menuRepository.save(menu2);

        restaurant2.setTakeawayMenu(menu2.getId());
        restaurantRepository.save(restaurant2);
    }

    @Test
    public void testCreateTakeawaysCausesRejectionNotice() throws Exception{
        setUpMenuAndRestaurant();
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest();
        Session session = sessionService.createTakeaway(request, customer2.getId(), Collections.newArrayList("foo", "bar"), null).getA();
        Booking booking = session.getOriginalBooking();

        assertFalse(booking.isAccepted());
        assertEquals("foo, bar", booking.getRejectionNotice());
    }

    private CustomerTakeawayOrderRequest getCustomerTakeawayOrderRequest() {
        CustomerTakeawayOrderRequest request = new CustomerTakeawayOrderRequest();
        request.setRestaurantId(restaurant2.getId());
        request.setTelephone("1");
        long requestedTime = System.currentTimeMillis() + 60000;
        request.setRequestedTime(requestedTime/1000);
        request.setDelivery(false);
        request.setInstantiatedFromId(1);

        CustomerOrderItemView orderItemView = new CustomerOrderItemView();
        orderItemView.setInstantiatedFromId(1);
        orderItemView.setMenuItemId(menuItem1.getId());
        orderItemView.setQuantity(1);
        request.getItems().add(orderItemView);
        return request;
    }

    @Test
    public void testCreateTakeaway1() throws Exception {
        TakeawayPayload payload = getStaffTakeawayRequest();
        Session session = sessionService.createTakeaway(payload, "", false, staff1.getId());

        assertTrue(payload.getRequestedTime() <= session.getStartTime());
        assertEquals(SessionType.TAKEAWAY, session.getSessionType());
        assertEquals(TakeawayType.COLLECTION, session.getTakeawayType());
        Booking originalBooking = session.getOriginalBooking();
        assertNotNull(originalBooking);
        assertNotNull(session.getOriginalBookingId());
        assertEquals(payload.getRequestedTime()*1000, originalBooking.getTargetTime());
        try {
            assertTrue(Integer.parseInt(session.getReadableId()) > 0);
        } catch (NumberFormatException ex) {
            fail();
        }

        //with customer
        payload.setLeadCustomerId(customer1.getId());
        session = sessionService.createTakeaway(payload, "", false, staff1.getId());

        originalBooking = session.getOriginalBooking();
        assertEquals(customer1.getId(), originalBooking.getCustomerId());
        assertTrue(originalBooking.isAccepted());
        assertFalse(originalBooking.isRejected());

    }

    @Test
    public void testCreateTakeawaySetPendingWaiterAction() throws Exception {
        TakeawayPayload payload = getStaffTakeawayRequest();
        Session session = sessionService.createTakeaway(payload, "", true, staff1.getId());

        Booking booking = session.getOriginalBooking();
        assertFalse(booking.isRejected());
        assertFalse(booking.isRejected());
    }

    private TakeawayPayload getStaffTakeawayRequest() {
        TakeawayPayload payload = new TakeawayPayload();
        payload.setAccepted(true);
        payload.setDelivery(false);
        payload.setExpectedTime((System.currentTimeMillis() + 60000)/1000);
        payload.setName("Foo");
        payload.setMessage("foo");
        payload.setRequestedTime(System.currentTimeMillis());
        payload.setRestaurantId(restaurant1.getId());

        return payload;
    }

    @Test
    public void testCreateSession() throws Exception {
        //waiting party no customer id
        WaitingPartyPayload payload = getWaitingPartyPayload(true);

        Session session = sessionService.createSession(payload, restaurant1.getId());

        assertTrue(session.getStartTime() > System.currentTimeMillis() - 100);
        assertNotNull(session.getOriginalParty().getId());
        assertEquals(4, session.getDiners().size());
        assertEquals(table1.getId(), session.getTables().get(0));
        assertEquals(payload.getName(), session.getName());
        try {
            assertTrue(Integer.parseInt(session.getReadableId()) > 0);
        } catch (NumberFormatException ex) {
            fail();
        }

        // with customer id
        payload = getWaitingPartyPayload(true);
        HostCustomerView hostCustomerView = new HostCustomerView(customer2, new HashMap<>());
        payload.setCustomer(hostCustomerView);
        session = sessionService.createSession(payload, restaurant1.getId());

        assertEquals(customer2.getId(), session.getDiners().get(1).getCustomerId());
        assertNotEquals(customer2.getId(), session.getDiners().get(2).getCustomerId());

        // with customer that already has an existing checkin (should delete other one)
        CheckIn checkIn = new CheckIn();
        checkIn.setCustomerId(customer2.getId());
        checkIn.setRestaurantId(restaurant1.getId());
        checkIn.setTime(System.currentTimeMillis());
        checkInRepository.save(checkIn);

        payload = getWaitingPartyPayload(true);
        hostCustomerView = new HostCustomerView(customer2, new HashMap<>());
        payload.setCustomer(hostCustomerView);
        session = sessionService.createSession(payload, restaurant1.getId());

        assertNull(checkInRepository.findOne(checkIn.getId()));
    }

    @Test
    public void testCreateSessionCreatesRefund() throws Exception {
        //waiting party no customer id
        WaitingPartyPayload payload = getWaitingPartyPayload(true);
        payload.setAdHoc(true);
        payload.setRefund(true);

        Session session = sessionService.createSession(payload, restaurant1.getId());
        assertEquals(SessionType.REFUND, session.getSessionType());
    }

    @Test
    public void testCreateSessionTriggersAutotip() throws Exception {
        //waiting party no customer id
        WaitingPartyPayload payload = getWaitingPartyPayload(true);
        payload.setNumberOfPeople(4);

        Session session = sessionService.createSession(payload, restaurant1.getId());
        assertTrue(session.getTipPercentage() > 0);
    }

    @Test
    public void testCreateSessionTriggersAutotipWithAdhoc() throws Exception {
        //waiting party no customer id
        WaitingPartyPayload payload = getWaitingPartyPayload(true);
        payload.setAdHoc(true);

        Session session = sessionService.createSession(payload, restaurant1.getId());
        assertNull(session.getTipPercentage());

        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.APPLY_AUTOTIP_TO_QO)).findFirst().orElse(null);
        assertNotNull(restaurantDefault);
        restaurantDefault.setValue(true);
        restaurantRepository.save(restaurant1);

        session = sessionService.createSession(payload, restaurant1.getId());
        assertTrue(session.getTipPercentage() > 0);
    }

    @Test
    public void testCreateSessionTriggersAutotipWithAdhocNull1() throws Exception {
        //waiting party no customer id
        WaitingPartyPayload payload = getWaitingPartyPayload(true);
        payload.setAdHoc(true);

        Session session = sessionService.createSession(payload, restaurant1.getId());
        assertNull(session.getTipPercentage());

        restaurant1.getRestaurantDefaults().removeIf(d -> d.getName().equals(FixedDefaults.APPLY_AUTOTIP_TO_QO));
        restaurantRepository.save(restaurant1);

        session = sessionService.createSession(payload, restaurant1.getId());
        assertNull(session.getTipPercentage());
    }

    @Test
    public void testCreateSessionTriggersAutotipWithAdhocNull2() throws Exception {
        //waiting party no customer id
        WaitingPartyPayload payload = getWaitingPartyPayload(true);
        payload.setAdHoc(true);

        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.APPLY_AUTOTIP_TO_QO)).findFirst().orElse(null);
        assertNotNull(restaurantDefault);
        restaurantDefault.setValue(false);
        restaurantRepository.save(restaurant1);

        Session session = sessionService.createSession(payload, restaurant1.getId());
        assertNull(session.getTipPercentage());

        restaurant1.getRestaurantDefaults().removeIf(d -> d.getName().equals(FixedDefaults.APPLY_AUTOTIP_TO_QO));
        restaurantRepository.save(restaurant1);

        session = sessionService.createSession(payload, restaurant1.getId());
        assertNull(session.getTipPercentage());
    }

    private WaitingPartyPayload getWaitingPartyPayload(boolean withTable) throws Exception{
        setUp();
        WaitingPartyPayload payload = new WaitingPartyPayload();
        payload.setAdHoc(false);
        payload.setName("foo");
        payload.setNumberOfPeople(3);
        payload.setServiceId(service1.getId());
        List<String> list = new ArrayList<>();
        if(withTable) {
            list.add(table1.getId());
        }
        payload.setTables(list);
        return payload;
    }

    @Test
    public void testCreateFromParty() throws Exception {
        WaitingPartyPayload wp = getWaitingPartyPayload(true);
        Party party = new Party(wp, restaurant1.getId());
        party = partyRepository.save(party);
        SessionPayload payload = new SessionPayload();
        payload.setServiceId(service1.getId());
        List<String> list = new ArrayList<>();
        list.add(table1.getId());
        payload.setTables(list);

        checkCreatedSession(party, payload, null);

        //with booking
        wp = getWaitingPartyPayload(true);
        party = new Party(wp, restaurant1.getId());
        party = partyRepository.save(party);
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setAccepted(false);
        bookingRepository.save(booking1);
        party.setBookingId(booking1.getId());
        payload = new SessionPayload();
        payload.setServiceId(service1.getId());
        list = new ArrayList<>();
        list.add(table1.getId());
        payload.setTables(list);

        checkCreatedSession(party, payload, booking1);

        //tab
        wp = getWaitingPartyPayload(false);
        party = new Party(wp, restaurant1.getId());
        party = partyRepository.save(party);
        payload = new SessionPayload();
        payload.setServiceId(service1.getId());
        list = new ArrayList<>();
        payload.setTables(list);

        checkCreatedSession(party, payload, null);
    }

    @Test
    public void testCreateFromPartyWhenBookingIsAlreadyTiedToSession() throws Exception {
        WaitingPartyPayload wp = getWaitingPartyPayload(true);
        Party party = new Party(wp, restaurant1.getId());
        party = partyRepository.save(party);
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setAccepted(false);
        bookingRepository.save(booking1);
        party.setBookingId(booking1.getId());
        SessionPayload payload = new SessionPayload();
        payload.setServiceId(service1.getId());
        List<String> list = new ArrayList<>();
        list.add(table1.getId());
        payload.setTables(list);

        checkCreatedSession(party, payload, booking1);
        // now create another but the session shouldn't be tied
        list.clear();
        Session session = checkCreatedSession(party, payload, null);
        assertNull(session.getOriginalBookingId());
    }


    @Test
    public void testCreateFromPartyTriggersAutotip() throws Exception {
        WaitingPartyPayload wp = getWaitingPartyPayload(true);
        wp.setNumberOfPeople(4);
        Party party = new Party(wp, restaurant1.getId());
        party = partyRepository.save(party);
        SessionPayload payload = new SessionPayload();
        payload.setServiceId(service1.getId());
        List<String> list = new ArrayList<>();
        list.add(table1.getId());
        payload.setTables(list);

        Session session = sessionService.createFromParty(party, payload);
        assertTrue(session.getTipPercentage() > 0);
    }

    private Session checkCreatedSession(Party party, SessionPayload payload, Booking booking) throws Exception {
        Session session = sessionService.createFromParty(party, payload);
        assertTrue(session.getStartTime() > System.currentTimeMillis() - 5000);
        assertNotNull(session.getOriginalParty().getId());
        assertEquals(4, session.getDiners().size());
        if(payload.getTables().size() > 0) {
            assertEquals(table1.getId(), session.getTables().get(0));
        }
        if(booking != null) {
            assertTrue(bookingRepository.findOne(booking.getId()).isAccepted());
        }
        try {
            assertTrue(Integer.parseInt(session.getReadableId()) > 0);
        } catch (NumberFormatException ex) {
            fail();
        }

        return session;
    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateStart() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateChairData() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateTip() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateDeliveryCost() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateDelete() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateDelay() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testIncrementDelay() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testRemoveAdjustment() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testAddAdjustment() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSessionsByBookingIds() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSessionsByBookingIds1() throws Exception {

    }

    @Test
    public void testGetLiveSessions() throws Exception {
        setUpMixedSessions();
        session1.setStartTime(System.currentTimeMillis());
        session2.setStartTime(System.currentTimeMillis());
        session3.setStartTime(System.currentTimeMillis());
        session4.setStartTime(System.currentTimeMillis());
        sessionRepository.save(Collections.newArrayList(session1, session2, session3, session4));

        List<Session> sessions = sessionService.getLiveSessions(restaurant1.getId());
        assertEquals(4, sessions.size());

        session1.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(session1);
        sessions = sessionService.getLiveSessions(restaurant1.getId());
        assertEquals(3, sessions.size());

        long limit = System.currentTimeMillis() + 10000 + (1000*60*(Integer)masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.TAKEAWAY_LOCK_WINDOW).getValue());
        session2.setStartTime(limit);
        sessionRepository.save(session2);
        sessions = sessionService.getLiveSessions(restaurant1.getId());
        assertEquals(2, sessions.size());
    }

    @Test
    public void testGetLiveSessionsIncludeClosedWithinLockWindow() throws Exception {
        setUpMixedSessions();
        int minTime = ((Number)restaurant1.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.TAKEAWAY_LOCK_WINDOW)).findFirst().orElse(null).getValue()).intValue();
        long closedMinTimeMillis = minTime * 60 * 1000;
        session1.setStartTime(System.currentTimeMillis());
        session2.setStartTime(System.currentTimeMillis());
        session3.setStartTime(System.currentTimeMillis());
        session3.setClosedTime(System.currentTimeMillis() - closedMinTimeMillis - 5000);
        session4.setStartTime(System.currentTimeMillis());
        session4.setClosedTime(System.currentTimeMillis() - closedMinTimeMillis + 5000);
        sessionRepository.save(Collections.newArrayList(session1, session2, session3, session4));

        List<Session> sessions = sessionService.getLiveSessionsIncludeClosedWithinLockWindow(restaurant1.getId());
        assertEquals(3, sessions.size());
        assertNull(sessions.stream().filter(s -> s.getId().equals(session3.getId())).findFirst().orElse(null));
    }

    @Test
    public void testGetClosedSessions() throws Exception {
        setUpMixedSessions();

        session1.setStartTime(System.currentTimeMillis()-100);
        session2.setStartTime(System.currentTimeMillis()-100);
        session3.setStartTime(System.currentTimeMillis()-100);
        session4.setStartTime(System.currentTimeMillis()-100);
        sessionRepository.save(Collections.newArrayList(session1, session2, session3, session4));

        List<HostSessionView> list = sessionService.getClosedSessions(restaurant1.getId());
        assertEquals(0, list.size());

        session1.setClosedTime(System.currentTimeMillis());
        session2.setClosedTime(System.currentTimeMillis());
        session3.setClosedTime(System.currentTimeMillis());
        session4.setClosedTime(System.currentTimeMillis());
        sessionRepository.save(Collections.newArrayList(session1, session2, session3, session4));

        list = sessionService.getClosedSessions(restaurant1.getId());
        assertEquals(4, list.size());
    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSessionByPartyId() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSessionByPartyIds() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testGetSessionsBetween() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testDelete() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testClear() throws Exception {

    }

    @Ignore("thin layer to repository")
    @Test
    public void testUpdateCashUpId() throws Exception {

    }

    @Test
    public void testCreateDefaultSession() throws Exception {
        boolean error = false;
        try {
            sessionService.createDefaultSession(restaurant1, "foo");
        } catch (IllegalStateException ex) {
            error = true;
        }
        assertTrue(error);

        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        service1.setDefaultService(true);
        restaurantRepository.save(restaurant1);

        Session session = sessionService.createDefaultSession(restaurant1, "foo");
        assertNotNull(session);
        assertEquals(2, session.getDiners().size());
    }

    @Test
    public void testCancelSession() throws Exception {
        order1.setSessionId(session1.getId());
        orderRepository.save(order1);
        batch1.setSessionId(session1.getId());
        batch2.setSessionId(session1.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);
        session1.setOriginalBooking(booking1);
        session1.setOriginalParty(party1);
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);
        booking1.setRestaurantId(session1.getRestaurantId());
        bookingRepository.save(booking1);
        checkIn1.setTime(System.currentTimeMillis());
        checkIn1.setRestaurantId(session1.getRestaurantId());
        checkIn1.setSessionId(session1.getId());
        checkInRepository.save(checkIn1);
        notification1.setRestaurantId(session1.getRestaurantId());
        notification1.setSessionId(session1.getId());
        notificationRepository.save(notification1);

        sessionService.cancelSession(session1);

        assertNull(orderRepository.findOne(order1.getId()));
        assertNull(batchRepository.findOne(batch1.getId()));
        assertNull(batchRepository.findOne(batch2.getId()));
        assertNull(sessionRepository.findOne(session1.getId()));
        assertNull(bookingRepository.findOne(booking1.getId()));
        assertNull(checkInRepository.findOne(checkIn1.getId()));
        assertNull(notificationRepository.findOne(notification1.getId()));
    }

    @Test
    public void testAddDiners2() throws Exception {
        session1.getDiners().clear();
        diner1.setDefaultDiner(true);
        diner2.setDefaultDiner(false);
        session1.getDiners().add(diner1);
        session1.getDiners().add(diner2);
        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);

        sessionService.addDiners(session1, 1);
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getNumberOfRealDiners());
        assertEquals("Guest 2", session.getDiners().get(2).getName());
    }

    @Test
    public void testRemoveDiners() throws Exception {
        session1.getDiners().clear();
        diner1.setDefaultDiner(true);
        diner2.setDefaultDiner(false);
        session1.getDiners().add(diner1);
        session1.getDiners().add(diner2);
        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);

        sessionService.removeDiners(session1, 1);
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(0, session.getNumberOfRealDiners());
    }

    @Test
    public void getTableName() throws Exception {
        restaurant1.getTables().clear();
        table1.setName("t1");
        restaurant1.getTables().add(table1);
        session1.getTables().add(table1.getId());
        session1.setSessionType(SessionType.SEATED);
        session1.setRestaurantId(restaurant1.getId());

        assertEquals("t1", SessionService.getTableName(restaurant1,session1));

        table2.setName("t2");
        table2.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        session1.getTables().add(table2.getId());

        assertEquals("t1,t2", SessionService.getTableName(restaurant1,session1));
    }

    @Test
    public void testDefermentExists() throws Exception {
        assertFalse(sessionService.defermentExists(restaurant1.getId(), session1.getId()));
        CustomerInteractionDeferredSession customerInteractionDeferredSession = new CustomerInteractionDeferredSession(customer1.getId(), restaurant1.getId(), session1.getId(), staff1.getId());
        customerInteractionRepository.save(customerInteractionDeferredSession);
        assertTrue(sessionService.defermentExists(restaurant1.getId(), session1.getId()));
        assertFalse(sessionService.defermentExists(restaurant1.getId(), session2.getId()));
        assertFalse(sessionService.defermentExists(restaurant2.getId(), session1.getId()));
    }

    @Test
    public void testDeferSession() throws Exception {
        setUpDeferSession();

        CustomerInteractionDeferredSession deferredSession = customerInteractionRepository.findByRestaurantIdAndSessionId(restaurant1.getId(), session1.getId());
        assertEquals(staff1.getId(), deferredSession.getStaffId());
        assertEquals(session1.getId(), deferredSession.getSessionId());
        assertEquals(restaurant1.getId(), deferredSession.getRestaurantId());
        assertEquals(customer1.getId(), deferredSession.getCustomerId());
        assertFalse(deferredSession.isPaid());
        assertNull(deferredSession.getArchiveTime());
        assertNotEquals(0, deferredSession.getCreationTime());

        Session session = sessionRepository.findOne(session1.getId());
        assertNotNull(session.getClosedTime());
        assertEquals(staff1.getId(), session.getClosedBy());
        assertEquals(1, session.getAdjustments().size());
        Adjustment adjustment = session.getAdjustments().get(0);
        assertEquals(adjustmentType1, adjustment.getAdjustmentType());
        assertEquals(1000, adjustment.getValue());
        assertEquals(NumericalAdjustmentType.PERCENTAGE, adjustment.getNumericalType());
        assertNotNull(adjustment.getSpecialAdjustmentData().get(RestaurantConstants.ADJUSTMENT_DATA_KEY_DEFERMENT_NOTE));
    }

    @Test
    public void testCopyDeferredSessionAndOrders1() {
        try {
            sessionService.copyDeferredSessionAndOrders("foobar");
        } catch(Exception ex) {
            return;
        }
        fail();
    }

    @Test
    public void testCopyDeferredSessionAndOrders2() {
        try {
            sessionService.copyDeferredSessionAndOrders(session1.getId());
        } catch(Exception ex) {
            return;
        }
        fail();
    }

    @Test
    public void testCopyDeferredSessionAndOrders3() {
        setUpOrdersForDeferred();
        setUpSessionAndDeferSession();
        Session session = sessionService.copyDeferredSessionAndOrders(session1.getId()).getMiddle();
        assertCopyEquals(restaurant1, session1, session);
        List<Order> orders = liveDataService.findOrders(session);
        assertEquals(3, orders.size());
    }

    @Test
    public void testCopyDeferredSessionAndOrders4() {
        setUpOrdersForDeferred();
        setUpSessionAndDeferSession();
        Session sessionFirstCopy = sessionService.copyDeferredSessionAndOrders(session1.getId()).getMiddle();
        int numberOfSessions = sessionRepository.findByRestaurantId(session1.getRestaurantId()).size();
        int numberOfOrders = orderRepository.findBySessionId(sessionFirstCopy.getId()).size();
        Session sessionSecondCopy = sessionService.copyDeferredSessionAndOrders(session1.getId()).getMiddle();
        assertEquals(sessionFirstCopy.getId(), sessionSecondCopy.getId());
        assertEquals(numberOfSessions, sessionRepository.findByRestaurantId(session1.getRestaurantId()).size());
        assertEquals(numberOfOrders, orderRepository.findBySessionId(sessionFirstCopy.getId()).size());
    }

    @Test
    public void testCopyDeferredSession() throws Exception {
        setUpSessionAndDeferSession();
        Session session = sessionService.copyDeferredSession(session1);
        assertCopyEquals(restaurant1, session1, session);
    }

    private void assertCopyEquals(Restaurant restaurant, Session original, Session copy) {
        assertEquals(copy, sessionRepository.findOne(copy.getId()));
        assertEquals(restaurant.getId(), copy.getRestaurantId());
        assertNotEquals(original.getId(), copy.getId());
        assertNotNull(copy.getId());
        assertTrue(copy.getName().contains(original.getName()));
        assertTrue(copy.getLinkedSession().contains(original.getId()));
        assertEquals(original.getDiners().size(), copy.getDiners().size());
        assertEquals(0, copy.getAdjustments().size());
    }

    @Test
    public void testSettleDeferredSession1() throws Exception {
        setUpOrdersForDeferred();
        setUpSessionAndDeferSession();
        Session copy = sessionService.copyDeferredSessionAndOrders(session1.getId()).getLeft();

        sessionRepository.delete(session1.getId());
        sessionArchiveRepository.deleteAll();
        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.PERCENTAGE));

        boolean error = false;
        try {
            sessionService.settleDeferredSession(copy.getId(), staff1.getId(), adjustmentRequest);
        } catch (IllegalStateException ex) {
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void testSettleDeferredSession2() throws Exception {
        setUpOrdersForDeferred();
        setUpSessionAndDeferSession();
        Session copy = sessionService.copyDeferredSessionAndOrders(session1.getId()).getMiddle();

        AdjustmentRequest paymentRequest = new AdjustmentRequest();
        paymentRequest.setAdjustmentTypeId(adjustmentType2.getId());
        paymentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        paymentRequest.setValue(0.60);

        sessionService.settleDeferredSession(copy.getId(), staff1.getId(), paymentRequest);
        Session oldSession = sessionRepository.findOne(session1.getId());
        assertNotNull(oldSession.getClosedTime());
        copy = sessionRepository.findOne(copy.getId());
        assertNotNull(copy);
        assertTrue(copy.getLinkedSession().contains(session1.getId()));
        assertNotNull(copy.getClosedTime());
        assertTrue(copy.isMarkedAsPaid());
        assertEquals(1, copy.getAdjustments().size());
        Adjustment adjustment = copy.getAdjustments().get(0);
        assertEquals(adjustmentType2, adjustment.getAdjustmentType());
        assertEquals(60, adjustment.getValue());
        assertEquals(NumericalAdjustmentType.ABSOLUTE, adjustment.getNumericalType());
        List<Order> orders = liveDataService.findOrders(copy);
        assertEquals(3, orders.size());

        CustomerInteractionDeferredSession deferredSession = customerInteractionRepository.findByRestaurantIdAndSessionId(restaurant1.getId(), session1.getId());
        assertTrue(deferredSession.isPaid());
    }

    @Test
    public void testVoidDeferredSession() throws Exception {
        setUpOrdersForDeferred();
        setUpSessionAndDeferSession();

        CustomerInteractionDeferredSession deferred = (CustomerInteractionDeferredSession)customerInteractionRepository.findAll().get(0);

        Session copy = sessionService.voidDeferredSessionAndOrders(deferred.getSessionId(), staff1.getId());

        assertNull(customerInteractionRepository.findOne(deferred.getId()));
        assertNotEquals(copy.getId(), deferred.getSessionId());
        assertNotNull(copy.getVoidReason());
        assertFalse(copy.isMarkedAsPaid());
        assertNotNull(copy.getId());
        assertNotNull(copy.getClosedTime());
        assertNotNull(copy.getClosedBy());
        assertTrue(copy.getLinkedSession().contains(session1.getId()));
    }

    private void setUpOrdersForDeferred() {
        setUpPrinters();
        restaurant1.setCountryId(country1.getId());
        restaurantRepository.save(restaurant1);
        tax1.setCountryId(country1.getId());
        tax1.setName("tax1");
        tax2.setCountryId(country1.getId());
        tax2.setName("tax2");
        tax3.setCountryId(country1.getId());
        tax3.setName("tax3");
        taxRateRepository.save(tax1);
        taxRateRepository.save(tax2);
        taxRateRepository.save(tax3);
        setUpMenuItems(restaurant1);
        setUpOrders(session1, restaurant1, true, null);
    }

    private void setUpSessionAndDeferSession() {
        session1.setReadableId("1");
        session1.setName("foo");
        session1.getDiners().add(diner1);
        sessionRepository.save(session1);
        setUpDeferSession();
    }

    private void setUpDeferSession() {
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);
        adjustmentType1.setName(RestaurantConstants.DEFER_ADJUSTMENT);
        adjustmentTypeRepository.save(adjustmentType1);
        HostCustomerViewBasic hostCustomerViewBasic = new HostCustomerViewBasic(customer1);
        sessionService.deferPayment(session1.getId(), adjustmentType1, staff1.getId(), hostCustomerViewBasic);
    }
}