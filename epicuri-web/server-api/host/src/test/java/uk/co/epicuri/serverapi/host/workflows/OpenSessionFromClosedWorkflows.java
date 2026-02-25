package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyOrderPayload;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentRequest;
import uk.co.epicuri.serverapi.common.pojo.model.session.NumericalAdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

/**
 * Created by manish on 08/07/2017.
 */
public class OpenSessionFromClosedWorkflows extends OrderingWorkflow{

    public void setUp() throws Exception{
        super.setUp();
        menuItem1.setPrice(10);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        Course course = new Course(service1);
        course.setName("test course");
        service1.getCourses().add(course);
        service1.setSessionType(SessionType.ADHOC);
        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testOpenAdhocToTab() throws Exception {
        String token = getTokenForStaff(staff1);

        WaitingPartyOrderPayload payload = new WaitingPartyOrderPayload();
        WaitingPartyPayload waitingPartyPayload = new WaitingPartyPayload();
        waitingPartyPayload.setAdHoc(true);
        waitingPartyPayload.setCreateSession(true);
        waitingPartyPayload.setServiceId(service1.getId());
        waitingPartyPayload.setNumberOfPeople(0);
        payload.setParty(waitingPartyPayload);

        WaitingPartyOrderPayload.OrderPayload orderPayload = new WaitingPartyOrderPayload.OrderPayload();
        orderPayload.setCourseId("-1");
        orderPayload.setDinerId("-1");
        orderPayload.setInstantiatedFromId(ActivityInstantiationConstant.WAITER.getId());
        orderPayload.setMenuItemId(menuItem1.getId());
        orderPayload.getModifiers().add(modifier1.getId());
        orderPayload.setNote("foobar");
        orderPayload.setQuantity(1);
        payload.getOrder().add(orderPayload);

        Response response1 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Waiting/PostWaitingWithOrder");
        assertEquals(HttpStatus.OK.value(), response1.getStatusCode());

        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setSessionId(session1.getId());
        adjustmentRequest.setAdjustmentTypeId(adjustmentType2.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest.setValue(10);

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");
        assertEquals(HttpStatus.OK.value(), response2.getStatusCode());

        Response response3 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .put("Session/PayBill");
        assertEquals(HttpStatus.OK.value(), response2.getStatusCode());

        //to finish
    }


}
