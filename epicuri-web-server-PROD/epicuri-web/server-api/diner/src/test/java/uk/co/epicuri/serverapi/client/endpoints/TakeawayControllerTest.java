package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayResponseView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class TakeawayControllerTest extends MenuSetupBaseIT {
    public void setUp() throws Exception {
        super.setUp();

        long now = System.currentTimeMillis();
        long fiveHours = 1000 * 60 * 60 * 5;

        booking1.setRestaurantId(restaurant1.getId());
        booking1.setTargetTime(1);
        booking1.setCustomerId(customerLogin.getId());
        booking1.setInstantiatedFrom(ActivityInstantiationConstant.WAITER);
        booking1.setEmail("foo@bar.com");
        booking1.setNotes("foobar");
        booking1.setNumberOfPeople(3);
        booking1.setAccepted(true);
        session1.setOriginalBooking(booking1);
        session1.setRestaurantId(booking1.getRestaurantId());
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking2.setRestaurantId(restaurant2.getId());
        booking2.setTargetTime(now - (1000*60*60*24*365));
        booking2.setCustomerId(customerLogin.getId());
        booking2.setTelephone("123");
        booking2.setInstantiatedFrom(ActivityInstantiationConstant.ANDROID);
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setAccepted(true);
        session2.setOriginalBooking(booking2);
        session2.setRestaurantId(booking2.getRestaurantId());
        booking3.setRestaurantId(restaurant2.getId());
        booking3.setTargetTime(now + (fiveHours*3));
        booking3.setCustomerId(customerLogin.getId());
        booking3.setInstantiatedFrom(ActivityInstantiationConstant.IOS);
        booking3.setBookingType(BookingType.TAKEAWAY);
        session3.setOriginalBooking(booking3);
        session3.setRestaurantId(booking3.getRestaurantId());

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);
        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
    }

    @Test
    public void getTakeaways() {
        String token = getTokenForCustomer(customerLogin);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("history","all")
                .get("/takeaway");

        CustomerTakeawayResponseView[] reservationViews = response.getBody().as(CustomerTakeawayResponseView[].class);
        assertEquals(3, reservationViews.length);
    }

    @Test
    public void getTakeawaysWithRestaurantId() {
        String token = getTokenForCustomer(customerLogin);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("history","all")
                .queryParam("restaurantId", restaurant1.getId())
                .get("/takeaway");

        CustomerTakeawayResponseView[] reservationViews = response.getBody().as(CustomerTakeawayResponseView[].class);
        assertEquals(1, reservationViews.length);
    }
}