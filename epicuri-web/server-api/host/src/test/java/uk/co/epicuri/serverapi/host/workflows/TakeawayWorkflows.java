package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostSessionView;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by manish on 16/04/2017.
 */
public class TakeawayWorkflows extends WorkflowsBaseIT {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testRestaurant.setCountryId(country1.getId());
        restaurantRepository.save(testRestaurant);
        country1.setAcronym("GB");
        countryRepository.save(country1);
    }

    @Test
    public void testCreateTakeaway() throws Exception {
        String token = getTokenForStaff(testStaff);

        long now = System.currentTimeMillis();
        TakeawayPayload request = new TakeawayPayload();
        request.setName("Fooman Chu");
        request.setTelephone("12354234");
        request.setMessage("a message");
        request.setRequestedTime((now + (1000*60*24))/1000);
        request.setDelivery(true);
        request.setAddress(booking3.getDeliveryAddress());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Takeaway");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Map map = response.getBody().as(Map.class, ObjectMapperType.JACKSON_2);
        assertTrue(map.containsKey("Warning"));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Takeaway");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostSessionView hostSessionView = response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
        Session session = sessionRepository.findOne(hostSessionView.getId());
        Booking booking = session.getOriginalBooking();
        assertNotNull(booking);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .parameter("fromTime", (now - 1000*60*60)/1000)
                .parameter("toTime", (now + 1000*60*60)/1000)
                .get("Takeaway");

        HostSessionView[] hostSessionViews = response.getBody().as(HostSessionView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(1, hostSessionViews.length);
        assertEquals(session.getId(), hostSessionView.getId());
    }
}
