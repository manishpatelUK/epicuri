package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostEventView;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class EventControllerTest extends SessionSetupBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUpSession();
        super.setUpNotifications();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        session1.setStartTime(0L);
        sessionRepository.save(session1);
    }

    @Test
    public void testGetEvents() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Event");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        HostEventView[] hostEventViews = response.getBody().as(HostEventView[].class, ObjectMapperType.JACKSON_2);

        assertEquals(3, hostEventViews.length);
    }
}