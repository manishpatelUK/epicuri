package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class PaymentSenseControllerTest extends RestaurantControllerTest{

    @Test
    public void getTerminals() throws Exception {
        String token = getTokenForStaff(staff1);

        restaurant1.getIntegrations().clear();
        restaurantRepository.save(restaurant1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("External/PaymentSense/terminals");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        //no need to test the rest, get terminals definately works
    }
}
