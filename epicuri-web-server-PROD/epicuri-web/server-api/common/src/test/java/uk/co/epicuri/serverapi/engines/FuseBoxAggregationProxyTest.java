package uk.co.epicuri.serverapi.engines;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerBlackMarkUtil;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.service.GoogleMapsService;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class FuseBoxAggregationProxyTest extends SessionSetupBaseIT {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private final long targetTime = 1464292860000L; //Thu May 26 2016 20:01:00 UTC (21:01:00 London time)

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setUpOrders();

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setTargetTime(targetTime);
        booking1.setNumberOfPeople(3);

        booking2.setRestaurantId(restaurant1.getId());
        booking2.setTargetTime(targetTime - (1000*60*30));
        booking2.setNumberOfPeople(10);

        booking3.setRestaurantId(restaurant1.getId());
        booking3.setTargetTime(targetTime + (1000*60*30));
        booking3.setNumberOfPeople(20);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        session1.setRestaurantId(restaurant1.getId());
        session1.setOriginalBooking(booking1);
        sessionRepository.save(session1);

        restaurant1.setIANATimezone("Europe/London");
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testCreateTakeawayProxy() throws Exception {
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTakeawayType(TakeawayType.COLLECTION);
        session1.setSessionType(SessionType.TAKEAWAY);
        bookingRepository.save(booking1);
        sessionRepository.save(session1);
        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking1, session1);

        assertEquals(booking1.getId(), proxy.getBooking().getId());
        assertEquals(booking1.getTargetTime()/1000, proxy.getBookingTimeSeconds());
        assertEquals(session1.getId(), proxy.getSession().getId());
    }

    @Test
    public void testCreateTakeawayProxy1() throws Exception {
        session1.setSessionType(SessionType.TAKEAWAY);
        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking1, session1, restaurant1);

        assertEquals(booking1.getId(), proxy.getBooking().getId());
        assertEquals(booking1.getTargetTime()/1000, proxy.getBookingTimeSeconds());
        assertEquals(session1.getId(), proxy.getSession().getId());
        assertEquals(restaurant1.getId(), proxy.getRestaurant().getId());
    }

    @Test
    public void testCreateReservationsProxy() throws Exception {
        HostReservationRequest request = new HostReservationRequest();
        request.setLeadCustomerId(customer1.getId());
        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createReservationsProxy(autowireCapableBeanFactory, restaurant1.getId(), request);

        assertTrue(proxy.getRestaurantDefaults().size()>0);
        assertNotNull(proxy.getBooking());
        assertEquals(restaurant1.getId(), proxy.getRestaurant().getId());
        assertEquals(customer1.getId(), proxy.getCustomerId());
    }

    @Test
    public void testCreateReservationsProxy2() throws Exception {
        fillUpReservations();

        Booking temp = new Booking();
        temp.setCustomerId(customer1.getId());
        temp.setTargetTime(targetTime + 60000);
        temp.setRestaurantId(restaurant1.getId());
        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createReservationsProxy(autowireCapableBeanFactory, temp);

        assertTrue(proxy.getRestaurantDefaults().size()>0);
        assertEquals(restaurant1.getId(), proxy.getRestaurant().getId());
        assertNotNull(proxy.getBooking());
        assertEquals(customer1.getId(), proxy.getCustomerId());
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());
    }

    private void fillUpReservations() {
        bookingRepository.deleteAll();
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setBookingType(BookingType.RESERVATION);
        booking1.setTargetTime(targetTime);
        booking2.setRestaurantId(restaurant1.getId());
        booking2.setBookingType(BookingType.RESERVATION);
        booking2.setTargetTime(targetTime);
        booking3.setRestaurantId(restaurant1.getId());
        booking3.setBookingType(BookingType.RESERVATION);
        booking3.setTargetTime(targetTime);
        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
    }

    private void fillUpTakeaways() {
        bookingRepository.deleteAll();
        booking1.setRestaurantId(restaurant1.getId());
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTargetTime(targetTime);
        booking2.setRestaurantId(restaurant1.getId());
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setTargetTime(targetTime);
        booking3.setRestaurantId(restaurant1.getId());
        booking3.setBookingType(BookingType.TAKEAWAY);
        booking3.setTargetTime(targetTime);
        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
    }

    private void fillUpWalkins() {
        long time = System.currentTimeMillis();
        party1.setRestaurantId(restaurant1.getId());
        party1.setTime(time-100);
        party1.setArrivedTime(time-50);
        party1.setPartyType(PartyType.WALK_IN);
        party2.setRestaurantId(restaurant1.getId());
        party2.setTime(time-100);
        party2.setArrivedTime(time-50);
        party2.setPartyType(PartyType.WALK_IN);
        party3.setRestaurantId(restaurant1.getId());
        party3.setTime(time-(1000*60*60*48));
        party3.setArrivedTime(time-(1000*60*60*48));
        party3.setPartyType(PartyType.WALK_IN);
        partyRepository.save(party1);
        partyRepository.save(party2);
        partyRepository.save(party3);
    }

    @Test
    public void testCreateCustomerReservationsProxy() throws Exception {
        fillUpReservations();

        CustomerReservationView request = new CustomerReservationView();
        request.setRestaurantId(restaurant1.getId());
        request.setReservationTime(targetTime/1000);

        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createCustomerReservationsProxy(autowireCapableBeanFactory, request, customer1);
        assertTrue(proxy.getRestaurantDefaults().size()>0);
        assertEquals(restaurant1.getId(), proxy.getRestaurant().getId());
        assertNotNull(proxy.getBooking());
        assertEquals(customer1.getId(), proxy.getCustomerId());
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());
    }

    @Test
    public void testCreateTakeawayProxy2() throws Exception {
        fillUpTakeaways();

        CustomerTakeawayOrderRequest request = new CustomerTakeawayOrderRequest();
        request.setRequestedTime(targetTime/1000);
        request.setRestaurantId(restaurant1.getId());

        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, request, customer1.getId());
        assertTrue(proxy.getRestaurantDefaults().size()>0);
        assertEquals(restaurant1.getId(), proxy.getRestaurant().getId());
        assertNotNull(proxy.getBooking());
        assertEquals(customer1.getId(), proxy.getCustomerId());
        assertNotNull(proxy.getSession());
        assertNotNull(proxy.getOrders());
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());
    }

    @Test
    public void testCreateTakeawayProxy3() throws Exception {
        fillUpTakeaways();

        TakeawayPayload payload = new TakeawayPayload();
        payload.setRestaurantId(restaurant1.getId());
        payload.setLeadCustomerId(customer1.getId());
        payload.setAddress(new Address());
        payload.setDelivery(true);
        payload.setRequestedTime(targetTime/1000);

        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, payload);
        assertTrue(proxy.getRestaurantDefaults().size()>0);
        assertEquals(restaurant1.getId(), proxy.getRestaurant().getId());
        assertNotNull(proxy.getBooking());
        assertEquals(customer1.getId(), proxy.getCustomerId());
        assertNotNull(proxy.getSession());
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());
    }

    private FuseBoxAggregationProxy getTakeawayProxyWithTakeawayPayload() {
        TakeawayPayload payload = new TakeawayPayload();
        payload.setRestaurantId(restaurant1.getId());
        payload.setLeadCustomerId(customer1.getId());
        payload.setAddress(new Address());
        payload.setDelivery(true);
        payload.setRequestedTime(targetTime/1000);
        return FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, payload);
    }

    private FuseBoxAggregationProxy getTakeawayProxyWithCustomerTakeawayOrderAndCustomer() {
        CustomerTakeawayOrderRequest request = new CustomerTakeawayOrderRequest();
        request.setRestaurantId(restaurant1.getId());
        request.setRequestedTime(targetTime/1000);
        return FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, request, customer1.getId());
    }

    private FuseBoxAggregationProxy getReservationProxyWithCustomerReservationAndCustomer() {
        CustomerReservationView customerReservationView = new CustomerReservationView();
        customerReservationView.setRestaurantId(restaurant1.getId());
        customerReservationView.setReservationTime(targetTime/1000);
        customerReservationView.setNumberOfPeople(5);
        return FuseBoxAggregationProxy.createCustomerReservationsProxy(autowireCapableBeanFactory, customerReservationView, customer1);
    }

    private FuseBoxAggregationProxy getReservationProxyWithBooking() {
        Booking booking = new Booking();
        booking.setTargetTime(targetTime);
        booking.setRestaurantId(restaurant1.getId());
        booking.setBookingType(BookingType.RESERVATION);
        booking.setNumberOfPeople(5);
        return FuseBoxAggregationProxy.createReservationsProxy(autowireCapableBeanFactory, booking);
    }

    private FuseBoxAggregationProxy getReservationProxyWithRestaurantAndHostReservationRequest() {
        return getReservationProxyWithRestaurantAndHostReservationRequest(targetTime);
    }

    private FuseBoxAggregationProxy getReservationProxyWithRestaurantAndHostReservationRequest(long time) {
        HostReservationRequest hostReservationRequest = new HostReservationRequest();
        hostReservationRequest.setLeadCustomerId(customer1.getId());
        hostReservationRequest.setReservationTime(time/1000);
        hostReservationRequest.setNumberInParty(5);
        return FuseBoxAggregationProxy.createReservationsProxy(autowireCapableBeanFactory, restaurant1.getId(), hostReservationRequest);
    }

    private FuseBoxAggregationProxy getTakeawayProxyWithBookingAndSessionAndRestaurant() {
        Booking booking = new Booking();
        booking.setBookingType(BookingType.TAKEAWAY);
        booking.setRestaurantId(restaurant1.getId());
        session1.setSessionType(SessionType.TAKEAWAY);
        booking.setTargetTime(targetTime);
        booking = bookingRepository.save(booking);
        return FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking, session1, restaurant1);
    }

    private FuseBoxAggregationProxy getTakeawayProxyWithBookingAndSession() {
        Booking booking = new Booking();
        booking.setBookingType(BookingType.TAKEAWAY);
        booking.setTakeawayType(TakeawayType.COLLECTION);
        booking.setTargetTime(targetTime);
        booking.setRestaurantId(restaurant1.getId());
        session1.setSessionType(SessionType.TAKEAWAY);
        bookingRepository.save(booking);
        sessionRepository.save(session1);
        return FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking, session1);
    }

    @Test
    public void testGetBookingTimeSeconds() throws Exception {
        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());

        proxy = getReservationProxyWithBooking();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());

        proxy = getTakeawayProxyWithTakeawayPayload();
        assertEquals(targetTime/1000, proxy.getBookingTimeSeconds());
    }

    @Test
    public void testGetRestaurantDefaults() throws Exception {
        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);

        proxy = getReservationProxyWithBooking();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);

        proxy = getTakeawayProxyWithTakeawayPayload();
        assertTrue(proxy.getRestaurantDefaults().size() > 0);
    }

    @Test
    public void testGetRequestedNumberOfPeople() throws Exception {
        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        assertEquals(1, proxy.getRequestedNumberOfPeople());

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        assertEquals(1, proxy.getRequestedNumberOfPeople());

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(5, proxy.getRequestedNumberOfPeople());

        proxy = getReservationProxyWithBooking();
        assertEquals(5, proxy.getRequestedNumberOfPeople());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(5, proxy.getRequestedNumberOfPeople());

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        assertEquals(1, proxy.getRequestedNumberOfPeople());

        proxy = getTakeawayProxyWithTakeawayPayload();
        assertEquals(1, proxy.getRequestedNumberOfPeople());
    }

    @Test
    public void testGetNumberOfActiveReservationsInTimeSlot() throws Exception {
        fillUpReservations();

        assertEqualsBookings(3);
        FuseBoxAggregationProxy proxy;

        // not applicable - just make sure it doesn't throw an error
        fillUpTakeaways();
        proxy = getTakeawayProxyWithBookingAndSession();
        proxy.getNumberOfActiveReservationsInTimeSlot();
        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        proxy.getNumberOfActiveReservationsInTimeSlot();
        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        proxy.getNumberOfActiveReservationsInTimeSlot();
        proxy = getTakeawayProxyWithTakeawayPayload();
        proxy.getNumberOfActiveReservationsInTimeSlot();
    }

    @Test
    public void testOmittedReservationsCounts() throws Exception {
        fillUpReservations();

        booking2.setOmitFromChecks(true);
        bookingRepository.save(booking2);

        assertEqualsBookings(2);
    }

    private void assertEqualsBookings(int expected) {
        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(expected, proxy.getNumberOfActiveReservationsInTimeSlot());

        proxy = getReservationProxyWithBooking();
        assertEquals(expected, proxy.getNumberOfActiveReservationsInTimeSlot());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(expected, proxy.getNumberOfActiveReservationsInTimeSlot());
    }

    @Test
    public void testCancelledReservationsCounts() throws Exception {
        fillUpReservations();

        booking2.setCancelled(true);
        bookingRepository.save(booking2);

        assertEqualsBookings(2);
    }

    @Test
    public void testGetNumberOfPeopleInTimeSlot() throws Exception {
        fillUpReservations();

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(33, proxy.getNumberOfPeopleInTimeSlot());

        proxy = getReservationProxyWithBooking();
        assertEquals(33, proxy.getNumberOfPeopleInTimeSlot());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(33, proxy.getNumberOfPeopleInTimeSlot());

        // not applicable - just make sure it doesn't throw an error
        fillUpTakeaways();
        proxy = getTakeawayProxyWithBookingAndSession();
        proxy.getNumberOfPeopleInTimeSlot();
        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        proxy.getNumberOfPeopleInTimeSlot();
        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        proxy.getNumberOfPeopleInTimeSlot();
        proxy = getTakeawayProxyWithTakeawayPayload();
        proxy.getNumberOfPeopleInTimeSlot();
    }

    @Test
    public void testOmittedReservationsPeopleCounts() throws Exception {
        fillUpReservations();

        booking2.setOmitFromChecks(true);
        bookingRepository.save(booking2);

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(33-booking2.getNumberOfPeople(), proxy.getNumberOfPeopleInTimeSlot());

        proxy = getReservationProxyWithBooking();
        assertEquals(33-booking2.getNumberOfPeople(), proxy.getNumberOfPeopleInTimeSlot());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(33-booking2.getNumberOfPeople(), proxy.getNumberOfPeopleInTimeSlot());
    }

    @Test
    public void testGetNumberOfWalkInsWithinExpirationTime() throws Exception {
        fillUpWalkins();

        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());

        proxy = getReservationProxyWithBooking();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());

        proxy = getTakeawayProxyWithTakeawayPayload();
        assertEquals(2, proxy.getNumberOfWalkInsWithinExpirationTime());
    }

    @Test
    public void testIsClosedForReservations() throws Exception {
        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForReservations());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForReservations());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForReservations());

        proxy = getReservationProxyWithBooking();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForReservations());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForReservations());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForReservations());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForReservations());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForReservations());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForReservations());

        // not applicable - just make sure it doesn't throw an error
        proxy = getTakeawayProxyWithBookingAndSession();
        proxy.isClosedForReservations();
        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        proxy.isClosedForReservations();
        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        proxy.isClosedForReservations();
        proxy = getTakeawayProxyWithTakeawayPayload();
        proxy.isClosedForReservations();
    }


    private void setToAbsoluteHoliday(Booking booking) {
        // 25th December 2025
        booking.setTargetTime(LocalDateTime.of(2025, Month.DECEMBER,25,12,0).toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    private void setToRegularClosedTime(Booking booking) {
        // Monday 3rd Feb 2025 6am
        booking.setTargetTime(LocalDateTime.of(2025, Month.FEBRUARY,3,6,0).toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    private void setToRegularOpenTime(Booking booking) {
        // Monday 3rd Feb 2025 11am
        booking.setTargetTime(LocalDateTime.of(2025, Month.FEBRUARY,3,11,0).toEpochSecond(ZoneOffset.UTC) * 1000);
    }

    @Test
    public void testGetDeliverySurcharge() throws Exception {
        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        proxy.getBooking().setDeliveryAddress(null);

        assertEquals(0,proxy.getDeliverySurcharge());

        proxy = getTakeawayProxyWithBookingAndSession();
        proxy.getBooking().setDeliveryAddress(new Address());

        GoogleMapsService googleMapsService = EasyMock.createMock(GoogleMapsService.class);
        Whitebox.setInternalState(proxy,googleMapsService);
        EasyMock.expect(googleMapsService.getDistanceMiles(EasyMock.anyObject(Address.class), EasyMock.anyObject(Address.class))).andReturn(10D);
        EasyMock.replay(googleMapsService);

        Map<String,Object> restaurantDefaults = RestaurantDefault.asMap(restaurant1.getRestaurantDefaults());
        double charge = (Double)restaurantDefaults.getOrDefault(FixedDefaults.DELIVERY_SURCHARGE, 1.5);
        charge = MoneyService.toPenniesRoundNearest(charge);

        assertEquals(charge,proxy.getDeliverySurcharge(),0.1);
    }

    @Test
    public void testCalculateDistance() throws Exception {
        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();

        GoogleMapsService googleMapsService = EasyMock.createMock(GoogleMapsService.class);
        Whitebox.setInternalState(proxy,googleMapsService);
        EasyMock.expect(googleMapsService.getDistanceMiles(EasyMock.anyObject(Address.class), EasyMock.anyObject(Address.class))).andReturn(10D);
        EasyMock.replay(googleMapsService);

        assertEquals(10D,proxy.calculateDistance(),0.1);
    }

    @Test
    public void testExceedsBlackMarks() throws Exception {
        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());

        proxy = getReservationProxyWithBooking();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());

        proxy = getTakeawayProxyWithTakeawayPayload();
        blackmarkCustomer(proxy.getBooking(), proxy);
        assertTrue(proxy.exceedsBlackMarks());
        unblackmarkCustomer(proxy.getBooking(), proxy);
        assertFalse(proxy.exceedsBlackMarks());
    }

    private void blackmarkCustomer(Booking booking, FuseBoxAggregationProxy proxy) throws Exception {
        booking.setCustomerId(customer1.getId());
        bookingRepository.save(booking);

        for(int i = 0; i < 4; i++) {
            BlackMark blackMark = new BlackMark();
            blackMark.setTime(System.currentTimeMillis()-10);
            blackMark.setReason("Foo");
            customer1.getBlackMarks().add(blackMark);
        }
        customerRepository.save(customer1);

        // have to force proxy to refresh customer
        Class<? extends FuseBoxAggregationProxy> clazz = proxy.getClass();
        Field customerField = clazz.getDeclaredField("customer");
        customerField.setAccessible(true);
        customerField.set(proxy, null);
    }

    private void unblackmarkCustomer(Booking booking, FuseBoxAggregationProxy proxy) throws Exception{
        booking.setCustomerId(customer1.getId());
        customer1.getBlackMarks().clear();
        customerRepository.save(customer1);
        bookingRepository.save(booking);

        // have to force proxy to refresh customer
        Class<? extends FuseBoxAggregationProxy> clazz = proxy.getClass();
        Field customerField = clazz.getDeclaredField("customer");
        customerField.setAccessible(true);
        customerField.set(proxy, null);
    }

    @Test
    public void testExceedsBlackMarks1() throws Exception {
        Customer customer = new Customer();
        assertFalse(FuseBoxAggregationProxy.exceedsBlackMarks(customer));

        for(int i = 0; i < 2; i++) {
            BlackMark blackMark = new BlackMark();
            blackMark.setTime(System.currentTimeMillis()-10);
            blackMark.setReason("Foo");
            customer.getBlackMarks().add(blackMark);
        }

        assertFalse(FuseBoxAggregationProxy.exceedsBlackMarks(customer));

        BlackMark blackMark = new BlackMark();
        blackMark.setTime(System.currentTimeMillis()-10);
        blackMark.setReason("Foo");
        customer.getBlackMarks().add(blackMark);

        assertTrue(FuseBoxAggregationProxy.exceedsBlackMarks(customer));

        customer.getBlackMarks().remove(0);
        assertFalse(FuseBoxAggregationProxy.exceedsBlackMarks(customer));

        blackMark = new BlackMark();
        blackMark.setTime(System.currentTimeMillis()- CustomerBlackMarkUtil.BLACK_MARK_EXPIRY - 1);
        blackMark.setReason("Foo");
        customer.getBlackMarks().add(blackMark);

        assertFalse(FuseBoxAggregationProxy.exceedsBlackMarks(customer));
    }

    @Test
    public void testIsBirthday() throws Exception {
        RestaurantDefault birthdayDefault = new RestaurantDefault(new Default(FixedDefaults.BIRTHDAY_TIMESPAN,10,null,null,0));
        Customer customer = new Customer();
        //set birthday +- 10 days from now
        long oneDay = 1000*60*60*24;
        long tenDays = oneDay*10;

        customer.setBirthday(System.currentTimeMillis() - tenDays + oneDay);
        assertTrue(FuseBoxAggregationProxy.isBirthday(customer, birthdayDefault));

        customer.setBirthday(System.currentTimeMillis() - tenDays - oneDay);
        assertFalse(FuseBoxAggregationProxy.isBirthday(customer, birthdayDefault));

        customer.setBirthday(System.currentTimeMillis() + tenDays - oneDay);
        assertTrue(FuseBoxAggregationProxy.isBirthday(customer, birthdayDefault));

        customer.setBirthday(System.currentTimeMillis() + tenDays + oneDay);
        assertFalse(FuseBoxAggregationProxy.isBirthday(customer, birthdayDefault));
    }

    @Ignore("simple getter")
    @Test
    public void testGetCurrentBookingsAroundRequestForCustomer() throws Exception {

    }

    @Test
    public void testGetCurrentBookingsAroundRequest() throws Exception {
        fillUpReservations();

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());

        fillUpReservations();

        proxy = getReservationProxyWithBooking();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());

        fillUpReservations();

        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());

        fillUpTakeaways();

        proxy = getTakeawayProxyWithBookingAndSession();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());

        fillUpTakeaways();

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());

        fillUpTakeaways();

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());

        fillUpTakeaways();

        proxy = getTakeawayProxyWithTakeawayPayload();
        assertEquals(3, proxy.getCurrentBookingsAroundRequest().size());
    }

    @Test
    public void testIsClosedForTakeaways() throws Exception {
        openingHours1.setBookingType(BookingType.TAKEAWAY);
        openingHoursRepository.save(openingHours1);

        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForTakeaways());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForTakeaways());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForTakeaways());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());

        proxy = getTakeawayProxyWithTakeawayPayload();
        setToAbsoluteHoliday(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());
        setToRegularOpenTime(proxy.getBooking());
        assertFalse(proxy.isClosedForTakeaways());
        setToRegularClosedTime(proxy.getBooking());
        assertTrue(proxy.isClosedForTakeaways());

        // not applicable - just make sure it doesn't throw an error
        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        proxy.isClosedForTakeaways();
        proxy = getReservationProxyWithBooking();
        proxy.isClosedForTakeaways();
        proxy = getReservationProxyWithCustomerReservationAndCustomer();
        proxy.isClosedForTakeaways();
    }

    @Test
    public void setClosedIntraDay1() throws Exception {
        openingHours1.setBookingType(BookingType.RESERVATION);
        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertFalse(proxy.isClosedForReservations());
    }

    @Test
    public void testClosedIntradayInclusive1() throws Exception {
        // create a blackout for between 21:00 and 22:00, then create a reservation for 21:00 - should be rejected
        assertClosedStatusWithBlackout(targetTime - 60000, true);//Thu May 26 2016 20:00:00 UTC (21:00:00 London time)
    }

    private void assertClosedStatusWithBlackout(long time, boolean expectation) {
        openingHours1.setBookingType(BookingType.RESERVATION);
        AbsoluteBlackout absoluteBlackout = new AbsoluteBlackout();
        absoluteBlackout.setStart(targetTime - 60000);//Thu May 26 2016 20:00:00 UTC (21:00:00 London time)
        absoluteBlackout.setEnd(targetTime + (1000 * 60 * 60));
        openingHours1.getAbsoluteBlackouts().clear();
        openingHours1.getAbsoluteBlackouts().add(absoluteBlackout);
        openingHoursRepository.save(openingHours1);

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest(time);
        assertEquals(expectation, proxy.isClosedForReservations());
    }

    @Test
    public void testClosedIntradayInclusive2() throws Exception {
        // create a blackout for between 21:00 and 22:00, then create a reservation for 21:00:01 - should be rejected
        assertClosedStatusWithBlackout(targetTime - 60000+1, true);
    }

    @Test
    public void testClosedIntradayInclusive3() throws Exception {
        // create a blackout for between 21:00 and 22:00, then create a reservation for 20:59:59 - should be accepted
        assertClosedStatusWithBlackout(targetTime - (2*60000), false);
    }

    @Test
    public void testIsClosedForAllDay() throws Exception {
        openingHours1.setBookingType(BookingType.RESERVATION);
        openingHours1.getHours().put(DayOfWeek.THURSDAY, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        openingHoursRepository.save(openingHours1);

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertTrue(proxy.isClosedForReservations());
    }

    @Test
    public void testIsOpenForAllDay() throws Exception {
        openingHours1.setBookingType(BookingType.RESERVATION);
        openingHours1.getHours().put(DayOfWeek.THURSDAY, OpeningHours.getDefaultOpenAllDaysHours());
        openingHoursRepository.save(openingHours1);

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertFalse(proxy.isClosedForReservations());
    }

    @Test
    public void testWhenMidnightSlot() throws Exception {
        openingHours1.setBookingType(BookingType.RESERVATION);
        List<HourSpan> spans = new ArrayList<>();
        spans.add(new HourSpan(21,0,24,0));

        openingHours1.getHours().put(DayOfWeek.THURSDAY, spans);
        openingHoursRepository.save(openingHours1);

        FuseBoxAggregationProxy proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertFalse(proxy.isClosedForReservations());

        spans.clear();
        spans.add(new HourSpan(21,0,22,0));
        openingHoursRepository.save(openingHours1);

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertFalse(proxy.isClosedForReservations());

        spans.clear();
        spans.add(new HourSpan(22,0,23,0));
        openingHoursRepository.save(openingHours1);

        proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertTrue(proxy.isClosedForReservations());
    }

    @Test
    public void testGetCalculatedSessionTotal() throws Exception {
        setUpSession();

        FuseBoxAggregationProxy proxy = getTakeawayProxyWithBookingAndSession();
        assertEquals(366, proxy.getCalculatedSessionTotal());

        proxy = getTakeawayProxyWithBookingAndSessionAndRestaurant();
        assertEquals(366, proxy.getCalculatedSessionTotal());

        /*proxy = getReservationProxyWithRestaurantAndHostReservationRequest();
        assertEquals(366, proxy.getCalculatedSessionTotal());*/

        /*proxy = getReservationProxyWithBooking();
        assertEquals(366, proxy.getCalculatedSessionTotal());*/

        /*proxy = getReservationProxyWithCustomerReservationAndCustomer();
        assertEquals(366, proxy.getCalculatedSessionTotal());*/

        proxy = getTakeawayProxyWithCustomerTakeawayOrderAndCustomer();
        assertEquals(0, proxy.getCalculatedSessionTotal());

        proxy = getTakeawayProxyWithTakeawayPayload();
        assertEquals(0, proxy.getCalculatedSessionTotal());
    }

    @Ignore("simple getter")
    @Test
    public void testGetBooking() throws Exception {

    }

    @Test
    public void testCheckIsAddressFormatValidAndExists() throws Exception {
        FuseBoxAggregationProxy proxy = getReservationProxyWithBooking();

        assertTrue(proxy.checkIsAddressFormatValidAndExists());

        proxy.getBooking().setBookingType(BookingType.TAKEAWAY);
        proxy.getBooking().setTakeawayType(TakeawayType.COLLECTION);

        assertTrue(proxy.checkIsAddressFormatValidAndExists());
    }
}