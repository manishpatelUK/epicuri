package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.comms.EmailRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class CommsControllerTest extends BaseIT {

    @Test
    public void postEmailComms() {
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);
        session1.setRestaurantId(restaurant1.getId());
        session1.setReadableId("1234556");
        sessionRepository.save(session1);
        restaurant1.setISOCurrency("GBP");
        restaurant1.setIANATimezone("Europe/London");
        Address address = new Address();
        address.setStreet("sakdjaslkjd");
        address.setTown("33sadmas;msd");
        address.setPostcode("sdkjas");
        restaurant1.setAddress(address);
        restaurant1.setInternalEmailAddress("noreply@epicuri.email");
        restaurantRepository.save(restaurant1);

        EmailRequest request = new EmailRequest();
        request.setEmail("epicuriblackhole@mailinator.com");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Collections.singleton(request))
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .post("/comms/email/{id}");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
    }
}