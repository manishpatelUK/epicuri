package uk.co.epicuri.serverapi.engines;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import uk.co.epicuri.serverapi.BadStateException;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.*;

/**
 * Created by manish
 */
public class FuseBoxTest extends BaseIT {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private static NoticeAggregator dummyFunctionDoNothing(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        return noticeAggregator;
    }

    private static NoticeAggregator dummyFunctionDoSomething(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        noticeAggregator.add("foobar");
        return noticeAggregator;
    }

    private static NoticeAggregator dummyFunctionDoSomethingWithHttpStatus(NoticeAggregator noticeAggregator, FuseBoxAggregationProxy fuseBoxAggregationProxy) {
        noticeAggregator.add("foobar", HttpStatus.BAD_REQUEST);
        return noticeAggregator;
    }

    @Test
    public void testCheck() throws Exception {
        FuseBox fuseBox = new FuseBox();
        fuseBox.add(FuseBoxTest::dummyFunctionDoNothing);

        session1.setRestaurantId(restaurant1.getId());

        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking1, session1);
        NoticeAggregator output = fuseBox.check(proxy);
        assertEquals(0, output.getIndividualNotices().size());

        fuseBox.add(FuseBoxTest::dummyFunctionDoSomething);
        proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking1, session1);
        output = fuseBox.check(proxy);
        assertEquals(1, output.getIndividualNotices().size());
    }

    @Test(expected = BadStateException.class)
    public void testCheckThrowsError() throws Exception {
        FuseBox fuseBox = new FuseBox();
        fuseBox.add(FuseBoxTest::dummyFunctionDoSomethingWithHttpStatus);

        session1.setRestaurantId(restaurant1.getId());
        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking1, session1);
        NoticeAggregator output = fuseBox.check(true, proxy);
    }

    @Test
    public void testCheckReservationMinTime() throws Exception {
        long bookingTimeInSeconds = System.currentTimeMillis()/1000;

        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.RESERVATION_MINIMUM_TIME, 30);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getBookingTimeSeconds()).andReturn(bookingTimeInSeconds + (25 * 60));
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationMinTime(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getBookingTimeSeconds()).andReturn(bookingTimeInSeconds + (35 * 60));
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationMinTime(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckTimeBeforeNow() throws Exception {
        long bookingTimeInSeconds = System.currentTimeMillis()/1000;

        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.getBookingTimeSeconds()).andReturn(bookingTimeInSeconds + (25 * 60));
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        replay(proxy, noticeAggregator);

        FuseBox.checkTimeBeforeNow(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getBookingTimeSeconds()).andReturn(bookingTimeInSeconds - 1);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkTimeBeforeNow(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckReservationsBlackouts() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.isClosedForReservations()).andReturn(false);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationsBlackouts(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.isClosedForReservations()).andReturn(true);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationsBlackouts(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckBlackMarks() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.exceedsBlackMarks()).andReturn(false);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        replay(proxy, noticeAggregator);

        FuseBox.checkBlackMarks(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.exceedsBlackMarks()).andReturn(true);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkBlackMarks(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckMaxCoversPerReservation() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MAX_COVERS_PER_RESERVATION, 8);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getRequestedNumberOfPeople()).andReturn(9);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxCoversPerReservation(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getRequestedNumberOfPeople()).andReturn(7);
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxCoversPerReservation(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckMaxActiveReservations() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MAX_ACTIVE_RESERVATIONS, 100);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getNumberOfActiveReservationsInTimeSlot()).andReturn(101);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxActiveReservations(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getNumberOfActiveReservationsInTimeSlot()).andReturn(99);
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxActiveReservations(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckMaxActiveReservationsCovers() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MAX_ACTIVE_RESERVATIONS_COVERS, 10);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getNumberOfPeopleInTimeSlot()).andReturn(11);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxActiveReservationsCovers(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getNumberOfPeopleInTimeSlot()).andReturn(9);
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxActiveReservationsCovers(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckReservationAlreadyExists() throws Exception {
        List<Booking> reservations = new ArrayList<>();
        reservations.add(booking1);
        reservations.add(booking2);
        reservations.add(booking3);
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.getCurrentBookingsAroundRequestForCustomer()).andReturn(reservations);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationAlreadyExists(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckReservationLockWindow() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.RESERVATION_LOCK_WINDOW, 120);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getBookingTimeSeconds()).andReturn((System.currentTimeMillis() + (119*60*1000))/1000);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationLockWindow(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getBookingTimeSeconds()).andReturn((System.currentTimeMillis() + (121*60*1000))/1000);
        replay(proxy, noticeAggregator);

        FuseBox.checkReservationLockWindow(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckAddressExistence() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.checkIsAddressFormatValidAndExists()).andReturn(false);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkAddressExistence(noticeAggregator,proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.checkIsAddressFormatValidAndExists()).andReturn(true);
        replay(proxy, noticeAggregator);

        FuseBox.checkAddressExistence(noticeAggregator,proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckDuplicateTakeaway() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        bookings.add(booking2);
        bookings.add(booking3);
        booking1.setRestaurantId(restaurant1.getId());
        booking2.setRestaurantId(restaurant1.getId());
        booking3.setRestaurantId(restaurant1.getId());
        booking1.setCustomerId(null);
        booking2.setCustomerId(customer1.getId());
        booking3.setCustomerId(customer2.getId());

        Booking testBooking = new Booking();

        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.getCurrentBookingsAroundRequestForCustomer()).andReturn(bookings);
        testBooking.setCustomerId(null);
        expect(proxy.getBooking()).andReturn(testBooking);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        replay(proxy, noticeAggregator);

        FuseBox.checkDuplicateTakeaway(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getCurrentBookingsAroundRequestForCustomer()).andReturn(bookings);
        expect(proxy.getBooking()).andReturn(null);
        replay(proxy, noticeAggregator);

        FuseBox.checkDuplicateTakeaway(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getCurrentBookingsAroundRequestForCustomer()).andReturn(bookings);
        testBooking.setCustomerId(customer1.getId());
        expect(proxy.getBooking()).andReturn(testBooking);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkDuplicateTakeaway(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getCurrentBookingsAroundRequestForCustomer()).andReturn(new ArrayList<>());
        expect(proxy.getBooking()).andReturn(testBooking);
        replay(proxy, noticeAggregator);

        FuseBox.checkDuplicateTakeaway(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckTakeawayMinimumTime() throws Exception {
        Booking testBooking = new Booking();

        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.TAKEAWAY_MINIMUM_TIME, 120);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getBooking()).andReturn(testBooking);
        testBooking.setTargetTime((System.currentTimeMillis() + (119*60*1000)));
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkTakeawayMinimumTime(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getBooking()).andReturn(null);
        replay(proxy, noticeAggregator);

        FuseBox.checkTakeawayMinimumTime(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        testBooking.setTargetTime((System.currentTimeMillis() + (121*60*1000)));
        expect(proxy.getBooking()).andReturn(testBooking);
        replay(proxy, noticeAggregator);

        FuseBox.checkTakeawayMinimumTime(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckDeliveryAddressPresence() throws Exception {
        booking1.setDeliveryAddress(null);
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTakeawayType(TakeawayType.DELIVERY);

        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.getBooking()).andReturn(booking1);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkDeliveryAddressPresence(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        booking1.setBookingType(BookingType.RESERVATION);
        expect(proxy.getBooking()).andReturn(booking1);
        replay(proxy, noticeAggregator);

        FuseBox.checkDeliveryAddressPresence(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTakeawayType(TakeawayType.COLLECTION);
        expect(proxy.getBooking()).andReturn(booking1);
        replay(proxy, noticeAggregator);

        FuseBox.checkDeliveryAddressPresence(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        booking1.setTakeawayType(TakeawayType.DELIVERY);
        booking1.setDeliveryAddress(new Address());
        expect(proxy.getBooking()).andReturn(booking1);
        replay(proxy, noticeAggregator);

        FuseBox.checkDeliveryAddressPresence(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        booking1.setTakeawayType(TakeawayType.DELIVERY);
        booking1.setDeliveryAddress(null);
        expect(proxy.getBooking()).andReturn(booking1);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkDeliveryAddressPresence(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckMaxDeliveryRadius() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Booking booking = createMock(Booking.class);
        expect(proxy.getBooking()).andReturn(booking);
        expect(booking.getTakeawayType()).andReturn(TakeawayType.DELIVERY).anyTimes();
        expect(proxy.calculateDistance()).andReturn(11D);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MAX_DELIVERY_RADIUS, 10D);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, booking, noticeAggregator);

        FuseBox.checkMaxDeliveryRadius(noticeAggregator, proxy);
        verify(proxy, booking, noticeAggregator);

        reset(proxy, booking, noticeAggregator);
        expect(proxy.getBooking()).andReturn(booking);
        expect(booking.getTakeawayType()).andReturn(TakeawayType.DELIVERY).anyTimes();
        expect(proxy.calculateDistance()).andReturn(9D);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        replay(proxy, booking, noticeAggregator);

        FuseBox.checkMaxDeliveryRadius(noticeAggregator, proxy);
        verify(proxy, booking, noticeAggregator);
    }

    @Test
    public void testCheckMaxTakeawaysPerHour() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking1);
        bookings.add(booking2);
        bookings.add(booking3);
        booking1.setRestaurantId(restaurant1.getId());
        booking2.setRestaurantId(restaurant1.getId());
        booking3.setRestaurantId(restaurant1.getId());
        booking1.setCustomerId(null);
        booking2.setCustomerId(customer1.getId());
        booking3.setCustomerId(customer2.getId());

        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MAX_TAKEAWAYS_PER_HOUR, 2);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCurrentBookingsAroundRequest()).andReturn(bookings);
        expect(proxy.getBooking()).andReturn(null);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxTakeawaysPerHour(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCurrentBookingsAroundRequest()).andReturn(bookings);
        expect(proxy.getBooking()).andReturn(booking2);
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxTakeawaysPerHour(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        bookings.clear();

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCurrentBookingsAroundRequest()).andReturn(bookings);
        expect(proxy.getBooking()).andReturn(booking2);
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxTakeawaysPerHour(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckTakeawaysBlackouts() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        expect(proxy.isClosedForTakeaways()).andReturn(false);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        replay(proxy, noticeAggregator);

        FuseBox.checkTakeawaysBlackouts(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.isClosedForTakeaways()).andReturn(true);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkTakeawaysBlackouts(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckMaxOrder() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MAX_TAKEAWAY_VALUE, 20D);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCalculatedSessionTotal()).andReturn(2001);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxOrder(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCalculatedSessionTotal()).andReturn(1999);
        replay(proxy, noticeAggregator);

        FuseBox.checkMaxOrder(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckMinOrder() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        Map<String,Object> map = new HashMap<>();
        map.put(FixedDefaults.MIN_TAKEAWAY_VALUE, 5D);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCalculatedSessionTotal()).andReturn(499);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        FuseBox.checkMinOrder(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getRestaurantDefaults()).andReturn(map);
        expect(proxy.getCalculatedSessionTotal()).andReturn(501);
        replay(proxy, noticeAggregator);

        FuseBox.checkMinOrder(noticeAggregator, proxy);
        verify(proxy, noticeAggregator);
    }

    @Test
    public void testCheckCCPresent() throws Exception {
        FuseBoxAggregationProxy proxy = createMock(FuseBoxAggregationProxy.class);
        NoticeAggregator noticeAggregator = createMock(NoticeAggregator.class);

        reset(proxy, noticeAggregator);
        expect(proxy.getCustomer()).andReturn(null);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        expect(proxy.getCustomer()).andReturn(customer1);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        customer1.setCcData(new CreditCardData());
        expect(proxy.getCustomer()).andReturn(customer1);
        noticeAggregator.add(anyString(), anyObject(HttpStatus.class));
        expectLastCall();
        replay(proxy, noticeAggregator);

        reset(proxy, noticeAggregator);
        CreditCardData ccData = new CreditCardData();
        ccData.setCcToken("123");
        customer1.setCcData(ccData);
        expect(proxy.getCustomer()).andReturn(customer1);
        replay(proxy, noticeAggregator);
    }
}