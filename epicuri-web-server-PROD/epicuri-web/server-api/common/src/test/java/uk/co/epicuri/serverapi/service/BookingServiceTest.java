package uk.co.epicuri.serverapi.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class BookingServiceTest extends BaseIT {
    @Before
    public void setUp() throws Exception{
        super.setUp();


        restaurant1.setIANATimezone("Europe/London");
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testUpsertReservation() throws Exception {
        Booking booking = new Booking();
        booking.setName("foo");

        Booking savedBooking = bookingService.upsert(booking);

        Booking check = bookingRepository.findOne(savedBooking.getId());
        assertEquals(booking.getName(), check.getName());

        int size = bookingRepository.findAll().size();
        bookingService.upsert(savedBooking);
        assertEquals(size, bookingRepository.findAll().size());
    }

    @Test
    public void testGetReservations() throws Exception {
        booking1.setRestaurantId(restaurant2.getId());
        booking1.setBookingType(BookingType.RESERVATION);
        booking1.setTargetTime(10);
        booking2.setRestaurantId(restaurant2.getId());
        booking2.setBookingType(BookingType.RESERVATION);
        booking2.setTargetTime(20);
        booking3.setRestaurantId(restaurant2.getId());
        booking3.setBookingType(BookingType.TAKEAWAY);
        booking3.setTargetTime(20);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> list1 = bookingService.getReservations(restaurant2.getId(), 0, 5);
        List<Booking> list2 = bookingService.getReservations(restaurant2.getId(), 0, 10);
        List<Booking> list3 = bookingService.getReservations(restaurant2.getId(), 10, 20);
        List<Booking> list4 = bookingService.getReservations(restaurant1.getId(), 10, 20);

        assertEquals(0, list1.size());
        assertEquals(1, list2.size());
        assertEquals(2, list3.size());
        assertEquals(0, list4.size());

    }

    @Test
    public void testGetTakeaways() throws Exception {
        booking1.setRestaurantId(restaurant2.getId());
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking1.setTargetTime(10);
        booking2.setRestaurantId(restaurant2.getId());
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setTargetTime(20);
        booking3.setRestaurantId(restaurant2.getId());
        booking3.setBookingType(BookingType.RESERVATION);
        booking3.setTargetTime(20);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> list1 = bookingService.getTakeaways(restaurant2.getId(), 0, 5);
        List<Booking> list2 = bookingService.getTakeaways(restaurant2.getId(), 0, 10);
        List<Booking> list3 = bookingService.getTakeaways(restaurant2.getId(), 10, 20);
        List<Booking> list4 = bookingService.getTakeaways(restaurant1.getId(), 10, 20);

        assertEquals(0, list1.size());
        assertEquals(1, list2.size());
        assertEquals(2, list3.size());
        assertEquals(0, list4.size());

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetReservationsFrom() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetUpcomingReservations() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetBooking() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetBookingIncludeCancelled() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsert() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testInsert() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetBookings() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testDelete() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testAcceptBooking() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testRejectBooking() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testCancelBooking() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetReservationsByCustomerId() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testExists() throws Exception {

    }

    @Test
    public void testCheckTimesFuseBoxTimeAllOk() {
        resetOpeningHours();
        CustomerReservationView reservationView = getCustomerReservationView();
        assertFalse(bookingService.checkTimesFuseBox(reservationView));
    }

    @Test
    public void testCheckTimesFuseBoxTimeBeforeNow() {
        resetOpeningHours();
        CustomerReservationView reservationView = getCustomerReservationView();

        //check time before now
        reservationView.setReservationTime((System.currentTimeMillis()/1000) - 1);
        assertTrue(bookingService.checkTimesFuseBox(reservationView));
    }

    @Test
    public void testCheckTimesFuseBoxTimeReservationMinTime() {
        resetOpeningHours();
        CustomerReservationView reservationView = getCustomerReservationView();
        int minDefault = ((Number)restaurant1.getRestaurantDefaults()
                        .stream().filter(d -> d.getName().equals(FixedDefaults.RESERVATION_MINIMUM_TIME)).findFirst()
                        .orElse(null).getValue()).intValue();

        //check time before min time
        reservationView.setReservationTime((System.currentTimeMillis() + (1000*60*minDefault) + 5000)/1000);
        assertFalse(bookingService.checkTimesFuseBox(reservationView));
        reservationView.setReservationTime((System.currentTimeMillis() + (1000*60*minDefault) - 5000)/1000);
        assertTrue(bookingService.checkTimesFuseBox(reservationView));
    }

    @Test
    public void testCheckTimesFuseBoxTimeReservationBlackout() {
        resetOpeningHours();
        CustomerReservationView reservationView = getCustomerReservationView();

        long bookingTime = System.currentTimeMillis() + (1000 * 60 * 60 * 3);
        reservationView.setReservationTime(bookingTime/1000);
        Instant instant = Instant.ofEpochMilli(bookingTime);
        ZonedDateTime utcDT = ZonedDateTime.ofInstant(instant, TimeUtil.UTC);
        DayOfWeek dayOfWeek = utcDT.getDayOfWeek();
        OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        openingHours.getHours().put(dayOfWeek, OpeningHours.DEFAULT_CLOSED_ALL_DAYS_HOURS);
        openingHoursRepository.save(openingHours);

        assertTrue(bookingService.checkTimesFuseBox(reservationView));
    }

    @Test
    public void testCheckTimesFuseBoxTimeMaxCoversPerReservation() {
        resetOpeningHours();
        CustomerReservationView reservationView = getCustomerReservationView();

        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.MAX_COVERS_PER_RESERVATION)).findFirst().orElse(null);
        restaurantDefault.setValue(3);
        restaurantRepository.save(restaurant1);

        reservationView.setNumberOfPeople(4);
        assertTrue(bookingService.checkTimesFuseBox(reservationView));
    }

    @Test
    public void testCheckTimesFuseBoxTimeMaxActiveReservations() {
        resetOpeningHours();
        CustomerReservationView reservationView = getCustomerReservationView();

        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.MAX_ACTIVE_RESERVATIONS)).findFirst().orElse(null);
        restaurantDefault.setValue(2);
        restaurantRepository.save(restaurant1);

        bookingRepository.insert(new Booking(reservationView, null, System.currentTimeMillis(), 120));
        bookingRepository.insert(new Booking(reservationView, null, System.currentTimeMillis(), 120));
        bookingRepository.insert(new Booking(reservationView, null, System.currentTimeMillis(), 120));

        assertTrue(bookingService.checkTimesFuseBox(reservationView));
    }

    private void resetOpeningHours() {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setBookingType(BookingType.RESERVATION);
        openingHours.setRestaurantId(restaurant1.getId());
        Map<DayOfWeek, List<HourSpan>> map = new HashMap<>();
        for(DayOfWeek dayOfWeek : DayOfWeek.values()) {
            map.put(dayOfWeek, OpeningHours.getDefaultOpenAllDaysHours());
        }
        openingHours.setHours(map);
        openingHoursRepository.deleteAll();
        openingHoursRepository.insert(openingHours);
    }

    private CustomerReservationView getCustomerReservationView() {
        CustomerReservationView reservationView = new CustomerReservationView();
        reservationView.setRestaurantId(restaurant1.getId());
        reservationView.setReservationTime((System.currentTimeMillis() + (1000 * 60 * 60 * 3))/1000);
        reservationView.setTelephone("1234");
        reservationView.setEmail("sfsdf");
        reservationView.setNotes("24324");
        reservationView.setNumberOfPeople(2);
        reservationView.setInstantiatedFromId(ActivityInstantiationConstant.ANDROID.getId());
        return reservationView;
    }
}