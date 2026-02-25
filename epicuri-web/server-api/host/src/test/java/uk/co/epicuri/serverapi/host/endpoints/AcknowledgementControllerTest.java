package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostAcknowledgementView;
import uk.co.epicuri.serverapi.common.pojo.host.NotifySessionId;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class AcknowledgementControllerTest extends SessionSetupBaseIT {

    @Test
    public void testPostAcknowledgement() throws Exception {
        Response response;
        String token;
        String notificationId = "notifId";

        setUpSession();

        token = getTokenForStaff(staff1);

        //User is unauthorised (staff is not assigned to a restaurant)
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .post("Acknowledgement/"+notificationId);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        token = getTokenForStaff(staff1);

        //There is no body in request, so it's a bad request
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .post("Acknowledgement/"+notificationId);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        NotifySessionId request = new NotifySessionId();
        request.setSessionId(session1.getId()+"i");

        //SessionId is not found
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Acknowledgement/"+notificationId);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        request.setSessionId(session1.getId());
        notificationId = notification1.getId();

        //Acknowledgement created
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .post("Acknowledgement/"+notificationId);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertNotNull(notificationRepository.findOne(notificationId).getAcknowledged());

        HostAcknowledgementView entity  = response.getBody().as(HostAcknowledgementView.class, ObjectMapperType.JACKSON_2);
        assertEquals(notificationId, entity.getNotificationId());
        assertEquals(request.getSessionId(), entity.getSessionId());
        assertTrue(entity.getTime()>(System.currentTimeMillis()/1000)-10);
    }
}