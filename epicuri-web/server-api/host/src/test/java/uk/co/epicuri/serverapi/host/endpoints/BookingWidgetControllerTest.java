package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.booking.BookingRequest;
import uk.co.epicuri.serverapi.common.pojo.booking.BookingStaticsView;
import uk.co.epicuri.serverapi.common.pojo.booking.StaticsRequest;
import uk.co.epicuri.serverapi.common.pojo.booking.TimeSlots;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.HourSpan;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.util.OpeningHoursUtil;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.requestSpecification;
import static org.junit.Assert.*;

public class BookingWidgetControllerTest extends BaseIT {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        restaurant1.setPhoneNumber1("123445");
        restaurant1.setPublicEmailAddress("dskfj@asdfkls.com");
        restaurant1.setIANATimezone("Europe/London");
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void postStatics() throws Exception {
        StaticsRequest staticsRequest = new StaticsRequest();
        staticsRequest.setLanguage("en");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(staticsRequest)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/statics");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        BookingStaticsView bookingStaticsView = response.getBody().as(BookingStaticsView.class);
        assertNotNullEachField(bookingStaticsView);
    }

    private void assertNotNullEachField(BookingStaticsView bookingStaticsView) throws Exception{
        Field[] fields = BookingStaticsView.class.getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(bookingStaticsView);
            assertNotNull(value);
        }
    }

    @Test
    public void postReserve() throws Exception {
        BookingRequest request = setUpDataAndRequest();

        int nBookingsBefore = bookingService.getReservations(restaurant1.getId(), Long.MIN_VALUE, Long.MAX_VALUE).size();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/reserve");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        request = response.getBody().as(BookingRequest.class);
        assertTrue(request.isAccepted());
        List<Booking> reservations = bookingService.getReservations(restaurant1.getId(), Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(nBookingsBefore+1, reservations.size());
        Booking booking = reservations.stream().filter(r -> "dsfds@sada.com".equals(r.getEmail())).findFirst().orElse(null);
        assertNotNull(booking);
        assertNotNull(booking.getOptedIntoMarketing());
        assertTrue(booking.getOptedIntoMarketing());
    }

    private BookingRequest setUpDataAndRequest() {
        ZonedDateTime now = ZonedDateTime.now(TimeUtil.UTC);
        ZonedDateTime bookingTime = now.plusHours(4);

        String hour = String.format("%02d", bookingTime.getHour());
        String day = String.format("%02d", bookingTime.getDayOfMonth());
        String month = String.format("%02d", bookingTime.getMonthValue());
        String year = String.valueOf(bookingTime.getYear());

        OpeningHours openHours = OpeningHoursUtil.createDefaultOpeningHoursOpenAllDay(BookingType.RESERVATION, restaurant1.getId());
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurant1.getId()));
        openingHoursRepository.save(openHours);

        BookingRequest request = createBookingRequest(year + "." + month + "." + day, hour + ":00", 3);
        request.setToken(authenticationService.bookingWidgetLogin(restaurant1.getStaffFacingId()));
        return request;
    }

    @Test
    public void postReserveNameTelephoneCheck() throws Exception {
        BookingRequest request = setUpDataAndRequest();
        request.setName("");
        Response response1 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/reserve");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response1.getStatusCode());

        request.setName(null);
        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/reserve");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response2.getStatusCode());

        request.setName("foo");
        request.setTelephone(null);
        Response response3 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/reserve");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response3.getStatusCode());
    }

    private BookingRequest createBookingRequest(String date, String time, int nPeople) {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setDate(date);
        bookingRequest.setTime(time);
        bookingRequest.setEmail("dsfds@sada.com");
        bookingRequest.setLanguage("en");
        bookingRequest.setMarketingOpt(true);
        bookingRequest.setName("foo bar");
        bookingRequest.setMessage("sefsdfds");
        bookingRequest.setNumberOfPeople(nPeople);
        bookingRequest.setTelephone("309483204");

        return bookingRequest;
    }

    @Test
    public void postReserveCheckWhenNoToken() throws Exception {
        BookingRequest request = createBookingRequest("2095.12.01", "15:00", 3);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getId())
                .post("/booking/{id}/reservecheck");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void postReserveCheckWhenClosed() throws Exception {
        ZonedDateTime now = ZonedDateTime.now(TimeUtil.UTC);
        ZonedDateTime bookingTime = now.plusHours(4);

        String hour = String.format("%02d", bookingTime.getHour());
        String day = String.format("%02d", bookingTime.getDayOfMonth());
        String month = String.format("%02d", bookingTime.getMonthValue());
        String year = String.valueOf(bookingTime.getYear());

        BookingRequest request = createBookingRequest(year + "." + month + "." + day, hour + ":00", 3);
        request.setToken(authenticationService.bookingWidgetLogin(restaurant1.getStaffFacingId()));

        OpeningHours openHours = OpeningHoursUtil.createDefaultOpeningHoursOpenAllDay(BookingType.RESERVATION, restaurant1.getId());
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurant1.getId()));
        openingHoursRepository.save(openHours);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/reservecheck");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        OpeningHours closedHours = OpeningHoursUtil.createDefaultOpeningHoursClosedAllDay(BookingType.RESERVATION, restaurant1.getId());
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantId(restaurant1.getId()));
        openingHoursRepository.save(closedHours);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/reservecheck");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void postTimeSlots() throws Exception {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setRestaurantId(restaurant1.getId());
        openingHours.setBookingType(BookingType.RESERVATION);
        List<HourSpan> section = createHourSpans();
        openingHours.getHours().put(DayOfWeek.MONDAY, section);
        openingHours.getHours().put(DayOfWeek.TUESDAY, section);
        openingHours.getHours().put(DayOfWeek.WEDNESDAY, section);
        openingHours.getHours().put(DayOfWeek.THURSDAY, section);
        openingHours.getHours().put(DayOfWeek.FRIDAY, section);
        openingHours.getHours().put(DayOfWeek.SATURDAY, section);
        openingHours.getHours().put(DayOfWeek.SUNDAY, section);
        openingHoursRepository.delete(openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION));
        openingHoursRepository.insert(openingHours);

        ZonedDateTime now = ZonedDateTime.now(TimeUtil.UTC);
        ZonedDateTime bookingTime = now.plusDays(1);
        String day = String.format("%02d", bookingTime.getDayOfMonth());
        String month = String.format("%02d", bookingTime.getMonthValue());
        String year = String.valueOf(bookingTime.getYear());
        TimeSlots request = new TimeSlots();
        request.setDate(year + "." + month + "." + day);
        request.setNumberOfPeople(3);
        request.setToken(authenticationService.bookingWidgetLogin(restaurant1.getStaffFacingId()));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .pathParam("id", restaurant1.getStaffFacingId())
                .post("/booking/{id}/timeslots");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        TimeSlots respondedSlots = response.getBody().as(TimeSlots.class);
        assertEquals(8, respondedSlots.getTimes().size());
        assertEquals("20:00", respondedSlots.getTimes().get(0));
        assertEquals("20:30", respondedSlots.getTimes().get(1));
        assertEquals("21:00", respondedSlots.getTimes().get(2));
        assertEquals("21:30", respondedSlots.getTimes().get(3));
        assertEquals("22:00", respondedSlots.getTimes().get(4));
        assertEquals("22:30", respondedSlots.getTimes().get(5));
        assertEquals("23:00", respondedSlots.getTimes().get(6));
        assertEquals("23:30", respondedSlots.getTimes().get(7));
    }


}