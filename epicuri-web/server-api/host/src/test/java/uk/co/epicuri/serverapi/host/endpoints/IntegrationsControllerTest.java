package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HostIntegrationsView;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class IntegrationsControllerTest extends BaseIT {

    @Test
    public void getIntegrations() {
        restaurant1.getIntegrations().clear();
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("integrations");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostIntegrationsView[] views = response.getBody().as(HostIntegrationsView[].class);
        assertEquals(0, views.length);

        KVData data1 = new KVData();
        data1.setSecret("1");
        KVData data2 = new KVData();
        data2.setSecret("2");
        restaurant1.getIntegrations().put(ExternalIntegration.XERO, data1);
        restaurant1.getIntegrations().put(ExternalIntegration.PAYMENT_SENSE, data2);
        restaurantRepository.save(restaurant1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("integrations");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        views = response.getBody().as(HostIntegrationsView[].class);
        assertEquals(2, views.length);
        for(HostIntegrationsView view : views) {
            if(view.getIntegration().equals(ExternalIntegration.XERO.getKey())) {
                assertEquals("1", view.getKvData().getSecret());
            } else {
                assertEquals("2", view.getKvData().getSecret());
            }
        }
    }
}