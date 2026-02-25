package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.cayman.CaymanGatewayConstants;
import uk.co.epicuri.serverapi.common.pojo.external.cayman.CaymanGatewayPayload;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class CaymanControllerTest extends SessionSetupBaseIT {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        Optional<AdjustmentType> adjustmentType = adjustmentTypeRepository.findAll().stream().filter(a -> a.getName().equals(CaymanGatewayConstants.CAYMAN_PAYMENT_TYPE)).findFirst();
        adjustmentType.ifPresent(a -> adjustmentTypeRepository.delete(a));
    }

    @Test
    public void testPost() throws Exception {
        setUpTakeawayDeliverySession();
        AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setName(CaymanGatewayConstants.CAYMAN_PAYMENT_TYPE);
        adjustmentType.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType = adjustmentTypeRepository.save(adjustmentType);

        restaurant1.getAdjustmentTypes().add(adjustmentType.getId());
        restaurantRepository.save(restaurant1);

        CaymanGatewayPayload payload = new CaymanGatewayPayload();
        payload.setTransactionId("foobar");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .post("/External/CaymanGateway?bookingId="+booking1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertEquals(CaymanGatewayConstants.CAYMAN_PAYMENT_TYPE, session.getAdjustments().get(0).getAdjustmentType().getName());
        assertEquals(366, session.getAdjustments().get(0).getValue());
    }
}
