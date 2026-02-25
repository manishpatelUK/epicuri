package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationCheck;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.AbsoluteBlackout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.engines.NoticeAggregator;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.util.OpeningHoursUtil;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class ReservationControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        long now = System.currentTimeMillis();
        long fiveHours = 1000 * 60 * 60 * 5;

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setTargetTime(now + fiveHours);
        booking1.setCustomerId(customerLogin.getId());
        booking1.setInstantiatedFrom(ActivityInstantiationConstant.WAITER);
        booking1.setEmail("foo@bar.com");
        booking1.setNotes("foobar");
        booking1.setNumberOfPeople(3);
        booking1.setAccepted(true);
        booking1.setBookingType(BookingType.RESERVATION);
        booking2.setRestaurantId(restaurant2.getId());
        booking2.setTargetTime(now + (fiveHours*2));
        booking2.setCustomerId(customerLogin.getId());
        booking2.setTelephone("123");
        booking2.setInstantiatedFrom(ActivityInstantiationConstant.ANDROID);
        booking2.setBookingType(BookingType.RESERVATION);
        booking2.setAccepted(true);
        booking3.setRestaurantId(restaurant2.getId());
        booking3.setTargetTime(now + (fiveHours*3));
        booking3.setCustomerId(customer2.getId());
        booking3.setInstantiatedFrom(ActivityInstantiationConstant.IOS);
        booking3.setBookingType(BookingType.RESERVATION);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        restaurant1.setIANATimezone("Europe/London");
        restaurantRepository.save(restaurant1);

        //clear opening hours
        OpeningHours openingHours = OpeningHoursUtil.createDefaultOpeningHoursOpenAllDay(BookingType.RESERVATION, restaurant1.getId());
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurant1.getId()));
        openingHoursRepository.save(openingHours);
    }

    @Test
    public void testGetReservations() throws Exception{
        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Reservation");

        CustomerReservationView[] reservationViews = response.getBody().as(CustomerReservationView[].class);
        assertEquals(2, reservationViews.length);
        assertBookingEquals(booking1, reservationViews[0]);
        assertBookingEquals(booking2, reservationViews[1]);
    }

    @Test
    public void testGetReservationsAll() throws Exception{
        String token = getToken();

        booking1.setTargetTime(1);
        booking2.setTargetTime(System.currentTimeMillis() - (1000*60*60*24*365L));
        booking3.setTargetTime(System.currentTimeMillis() - 1);
        booking3.setCustomerId(customerLogin.getId());
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("history","all")
                .get("/Reservation");

        CustomerReservationView[] reservationViews = response.getBody().as(CustomerReservationView[].class);
        assertEquals(3, reservationViews.length);
        assertBookingEquals(booking1, reservationViews[0]);
        assertBookingEquals(booking2, reservationViews[1]);
        assertBookingEquals(booking3, reservationViews[2]);
    }

    @Test
    public void testGetReservationsAllWithRestaurantId() throws Exception{
        String token = getToken();

        booking1.setTargetTime(1);
        booking2.setTargetTime(System.currentTimeMillis() - (1000*60*60*24*365L));
        booking3.setTargetTime(System.currentTimeMillis() - 1);
        booking3.setCustomerId(customerLogin.getId());
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("history","all")
                .queryParam("restaurantId", restaurant1.getId())
                .get("/Reservation");

        CustomerReservationView[] reservationViews = response.getBody().as(CustomerReservationView[].class);
        assertEquals(1, reservationViews.length);
        assertBookingEquals(booking1, reservationViews[0]);
    }


    @Test
    public void testGetReservationsCancelled() throws Exception{
        booking1.setCancelled(true);
        booking2.setCancelled(true);
        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Reservation");

        CustomerReservationView[] reservationViews = response.getBody().as(CustomerReservationView[].class);
        assertEquals(2, reservationViews.length);
    }

    @Test
    public void testGetReservationsCancelledWhenInPast() throws Exception{
        booking1.setCancelled(true);
        booking1.setTargetTime(System.currentTimeMillis() - 3000);
        bookingRepository.save(booking1);

        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Reservation");

        CustomerReservationView[] reservationViews = response.getBody().as(CustomerReservationView[].class);
        assertEquals(1, reservationViews.length);
        assertBookingEquals(booking2, reservationViews[0]);
    }

    @Test
    public void testPostReservation() throws Exception {
        String token = getToken();

        long now = System.currentTimeMillis();
        long bookingTime = now + (1000 * 60 * 60 * 24 * 2);
        CustomerReservationView customerReservationView = createReservationView(bookingTime);

        Response response = postReservation(token, customerReservationView);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        CustomerReservationView responseReservation = response.getBody().as(CustomerReservationView.class);
        assertBookingEquals(bookingRepository.findOne(responseReservation.getId()), responseReservation);
    }

    @Test
    public void testPostReservationCheckTimeBeforeNow() throws Exception {
        String token = getToken();

        long now = System.currentTimeMillis();
        long bookingTime = now - 1001;
        CustomerReservationView customerReservationView = createReservationView(bookingTime);

        Response response = postReservation(token, customerReservationView);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getBody().as(StringMessage.class).getMessage().contains(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE));
    }

    @Test
    public void testPostReservationCheckReservationMinTime() throws Exception {
        String token = getToken();

        int reservationMinTimeDefault = Integer.valueOf(masterDataService.getDefaultByName(FixedDefaults.RESERVATION_MINIMUM_TIME).getValue().toString());

        long now = System.currentTimeMillis();
        long bookingTime = now + (reservationMinTimeDefault * 60 * 1000) - 2000;
        CustomerReservationView customerReservationView = createReservationView(bookingTime);

        Response response = postReservation(token, customerReservationView);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getBody().as(StringMessage.class).getMessage().contains(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE));
    }

    @Test
    public void testPostReservationCheckBlackout() throws Exception {
        String token = getToken();

        OpeningHours openingHours = masterDataService.getOpeningHours(restaurant1.getId(), BookingType.RESERVATION);
        AbsoluteBlackout absoluteBlackout = new AbsoluteBlackout();
        long now = System.currentTimeMillis();
        absoluteBlackout.setStart(now);
        absoluteBlackout.setEnd(now + (1000 * 60 * 60 * 24));
        openingHours.getHours().clear();
        openingHours.getAbsoluteBlackouts().add(absoluteBlackout);
        masterDataService.upsert(openingHours);

        long bookingTime = now + (60 * 1000 * 60 * 5);
        CustomerReservationView customerReservationView = createReservationView(bookingTime);

        Response response = postReservation(token, customerReservationView);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getBody().as(StringMessage.class).getMessage().contains(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE));
    }

    @Test
    public void testPostReservationCheckMaxCoversPerReservation() throws Exception {
        String token = getToken();

        long now = System.currentTimeMillis();
        long bookingTime = now + (1000 * 60 * 60 * 24 * 2);
        CustomerReservationView customerReservationView = createReservationView(bookingTime);
        int reservationThreshold = Integer.valueOf(masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.MAX_COVERS_PER_RESERVATION).getValue().toString());
        customerReservationView.setNumberOfPeople(reservationThreshold+1);

        postAndAssertReservationGoneToApproval(token, customerReservationView);
    }

    private void postAndAssertReservationGoneToApproval(String token, CustomerReservationView customerReservationView) {
        Response response = postReservation(token, customerReservationView);
        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode());

        CustomerReservationView responseReservation = response.getBody().as(CustomerReservationView.class);
        Booking booking = bookingRepository.findOne(responseReservation.getId());
        assertBookingEquals(booking, responseReservation);
        assertFalse(booking.isAccepted());
        assertFalse(booking.isRejected());
        assertFalse(booking.isCancelled());
    }

    @Test
    public void testPostReservationCheckMaxActiveReservations() throws Exception {
        String token = getToken();
        long now = System.currentTimeMillis();
        long bookingTime = now + (1000 * 60 * 60 * 24 * 2);

        int reservationThreshold = Integer.valueOf(masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.MAX_ACTIVE_RESERVATIONS).getValue().toString());
        for(int i = 0; i < reservationThreshold+2; i++) {
            insertBooking(bookingTime, 2);
        }

        CustomerReservationView customerReservationView = createReservationView(bookingTime);
        postAndAssertReservationGoneToApproval(token, customerReservationView);
    }

    @Test
    public void testPostReservationCheckMaxActiveReservationCovers() throws Exception {
        String token = getToken();
        long now = System.currentTimeMillis();
        long bookingTime = now + (1000 * 60 * 60 * 24 * 2);

        int reservationThreshold = Integer.valueOf(masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.MAX_ACTIVE_RESERVATIONS_COVERS).getValue().toString());

        insertBooking(bookingTime, reservationThreshold + 1);
        CustomerReservationView customerReservationView = createReservationView(bookingTime);
        postAndAssertReservationGoneToApproval(token, customerReservationView);
    }

    @Test
    public void testPostReservationCheckReservationAlreadyExists() throws Exception {
        String token = getToken();

        CustomerReservationView customerReservationView = createReservationView(booking1.getTargetTime());
        Response response = postReservation(token, customerReservationView);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getBody().as(StringMessage.class).getMessage().contains(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE));
    }

    @Test
    public void testPostReservationCheckBlackMarks() throws Exception {
        String token = getToken();

        CustomerReservationView customerReservationView = createReservationView(booking1.getTargetTime());
        long now = System.currentTimeMillis();
        customerLogin.getBlackMarks().add(new BlackMark("", now-1));
        customerLogin.getBlackMarks().add(new BlackMark("", now-1));
        customerLogin.getBlackMarks().add(new BlackMark("", now-1));
        customerRepository.save(customerLogin);
        Response response = postReservation(token, customerReservationView);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getBody().as(StringMessage.class).getMessage().contains(NoticeAggregator.BOOKING_NOT_AVAILABLE_BLACK_MARKS_FRIENDLY_MESSAGE));
    }

    @Test
    public void testDeleteReservation() throws Exception {
        String token = getToken();

        long now = System.currentTimeMillis();
        long bookingTime = now + (1000 * 60 * 60 * 24 * 2);
        booking1.setTargetTime(bookingTime);
        bookingRepository.save(booking1);

        Response response = given()
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .pathParam("id", booking1.getId())
                            .header(Params.AUTHORIZATION, token)
                            .delete("/Reservation/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertTrue(bookingRepository.findOne(booking1.getId()).isCancelled());
    }

    @Test
    public void testDeleteReservationWithinLockWindow() throws Exception {
        String token = getToken();

        int reservationThreshold = Integer.valueOf(masterDataService.getRestaurantDefault(restaurant1.getId(), FixedDefaults.RESERVATION_LOCK_WINDOW).getValue().toString());

        long now = System.currentTimeMillis();
        long bookingTime = now + (1000 * 60 * reservationThreshold) - 2000;
        booking1.setTargetTime(bookingTime);
        bookingRepository.save(booking1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", booking1.getId())
                .header(Params.AUTHORIZATION, token)
                .delete("/Reservation/{id}");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertTrue(response.getBody().as(StringMessage.class).getMessage().contains(NoticeAggregator.BOOKING_TOO_LATE_TO_CANCEL_MESSAGE));
    }

    @Test
    public void testPutReservation() throws Exception {
        String token = getToken();

        CustomerReservationView customerReservationView = new CustomerReservationView(booking1, restaurant1);
        customerReservationView.setReservationTime(booking1.getTargetTime() + (60*60*24));
        customerReservationView.setNumberOfPeople(4);
        customerReservationView.setNotes("foobar");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .pathParam("id", booking1.getId())
                .put("/Reservation/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        CustomerReservationView customerReservationView1 = response.getBody().as(CustomerReservationView.class);
        assertBookingEquals(bookingRepository.findOne(booking1.getId()), customerReservationView1);

        customerReservationView.setNumberOfPeople(400);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .pathParam("id", booking1.getId())
                .put("/Reservation/{id}");

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode());
        customerReservationView1 = response.getBody().as(CustomerReservationView.class);
        assertBookingEquals(bookingRepository.findOne(booking1.getId()), customerReservationView1);

        customerLogin.getBlackMarks().add(new BlackMark("",System.currentTimeMillis()));
        customerLogin.getBlackMarks().add(new BlackMark("",System.currentTimeMillis()));
        customerLogin.getBlackMarks().add(new BlackMark("",System.currentTimeMillis()));
        customerRepository.save(customerLogin);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .pathParam("id", booking1.getId())
                .put("/Reservation/{id}");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPutReservationCheck() throws Exception {
        String token = getToken();

        CustomerReservationView customerReservationView = new CustomerReservationView(booking1, restaurant1);
        customerReservationView.setReservationTime(booking1.getTargetTime() + (60*60*24));
        customerReservationView.setNumberOfPeople(4);
        customerReservationView.setNotes("foobar");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .put("/Reservation");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        CustomerReservationCheck customerReservationCheck = response.getBody().as(CustomerReservationCheck.class);
        assertNull(customerReservationCheck.getWarning());

        customerReservationView.setNumberOfPeople(400);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .put("/Reservation");

        customerReservationCheck = response.getBody().as(CustomerReservationCheck.class);
        assertNotNull(customerReservationCheck.getWarning());
        assertTrue(customerReservationCheck.getWarning().size() > 0);
    }

    @Test
    public void testPutReservationCheckInBlackout() throws Exception {
        String token = getToken();

        CustomerReservationView customerReservationView = new CustomerReservationView(booking1, restaurant1);
        customerReservationView.setReservationTime(booking1.getTargetTime() + (60 * 60* 2));
        customerReservationView.setNumberOfPeople(4);
        customerReservationView.setNotes("foobar");

        OpeningHours openingHours = OpeningHoursUtil.createDefaultOpeningHoursClosedAllDay(BookingType.RESERVATION, restaurant1.getId());
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurant1.getId()));
        openingHoursRepository.save(openingHours);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .put("/Reservation");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    private void insertBooking(long time, int numberOfPeople) {
        Booking booking = new Booking();
        booking.setRestaurantId(restaurant1.getId());
        booking.setAccepted(true);
        booking.setRejected(false);
        booking.setTargetTime(time);
        booking.setBookingType(BookingType.RESERVATION);
        booking.setNumberOfPeople(numberOfPeople);
        bookingRepository.insert(booking);
    }

    private String getToken() {
        return getTokenForCustomer(customerLogin);
    }

    private Response postReservation(String token, CustomerReservationView customerReservationView) {

        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerReservationView)
                .post("/Reservation");
    }

    private CustomerReservationView createReservationView(long bookingTime) {
        CustomerReservationView customerReservationView = new CustomerReservationView();
        customerReservationView.setRestaurantId(restaurant1.getId());
        customerReservationView.setNumberOfPeople(3);
        customerReservationView.setNotes("foobar");
        customerReservationView.setEmail("foo");
        customerReservationView.setTelephone("123");
        customerReservationView.setReservationTime(bookingTime / 1000);
        return customerReservationView;
    }

    private static void assertBookingEquals(Booking booking, CustomerReservationView customerReservationView) {
        assertNull(customerReservationView.getArrivedTime());
        assertEquals(booking.getEmail(), customerReservationView.getEmail());
        assertEquals(booking.getId(), customerReservationView.getId());
        assertEquals(booking.getInstantiatedFrom().getId(), customerReservationView.getInstantiatedFromId());
        assertEquals(booking.getNotes(), customerReservationView.getNotes());
        assertEquals(booking.getNumberOfPeople(), customerReservationView.getNumberOfPeople());
        assertEquals(booking.getRejectionNotice(), customerReservationView.getRejectionNotice());
        assertEquals(booking.getTargetTime() / 1000, customerReservationView.getReservationTime().longValue());
        assertEquals(booking.getRestaurantId(), customerReservationView.getRestaurantId());
        assertEquals(booking.getTelephone(), customerReservationView.getTelephone());
        assertEquals(booking.isAccepted(), customerReservationView.isAccepted());
        assertEquals(booking.isRejected(), customerReservationView.isRejected());
    }
}
