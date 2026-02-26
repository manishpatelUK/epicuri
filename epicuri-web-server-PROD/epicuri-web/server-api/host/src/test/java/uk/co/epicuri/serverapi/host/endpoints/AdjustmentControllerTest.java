package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PaymentSenseConstants;
import uk.co.epicuri.serverapi.common.pojo.host.HostAdjustmentView;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HostRefundAdjustmentView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class AdjustmentControllerTest extends SessionSetupBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUpSession();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        //ensure restaurant doesn't have paymentsense stuff, otherwise will start calling PS service
        restaurant1.getIntegrations().clear();
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void postAdjustment() throws Exception {
        String token = getTokenForStaff(staff1);

        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setSessionId(session1.getId());
        adjustmentRequest.setAdjustmentTypeId(adjustmentType2.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest.setValue(113.13);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1,session.getAdjustments().size());
        testEquals(session.getAdjustments().get(0), adjustmentRequest, adjustmentType2, NumericalAdjustmentType.ABSOLUTE);

        session.getAdjustments().clear();
        sessionRepository.save(session);

        adjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.PERCENTAGE));
        adjustmentRequest.setValue(0.5);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        session = sessionRepository.findOne(session1.getId());
        assertEquals(1,session.getAdjustments().size());
        testEquals(session.getAdjustments().get(0), adjustmentRequest, adjustmentType1, NumericalAdjustmentType.PERCENTAGE);
    }

    @Test
    public void postAdjustmentGratuity() throws Exception {
        String token = getTokenForStaff(staff1);

        adjustmentType1.setType(AdjustmentTypeType.GRATUITY);
        adjustmentTypeRepository.save(adjustmentType1);

        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setSessionId(session1.getId());
        adjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest.setValue(100.5);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1,session.getAdjustments().size());
        assertEquals((int)(100.5 * 100), session.getAdjustments().get(0).getValue());
    }

    @Test
    public void postAdjustmentAbsoluteDiscount1() throws Exception {
        String token = getTokenForStaff(staff1);


        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setSessionId(session1.getId());
        adjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest.setValue(113.13);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1,session.getAdjustments().size());
        assertEquals(366, session.getAdjustments().get(0).getValue());
    }

    @Test
    public void postAdjustmentAbsoluteDiscount2() throws Exception {
        String token = getTokenForStaff(staff1);

        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setSessionId(session1.getId());
        adjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest.setValue(1.13);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1,session.getAdjustments().size());
        assertEquals(113, session.getAdjustments().get(0).getValue());
    }

    private void testEquals(Adjustment adjustment, AdjustmentRequest request, AdjustmentType adjustmentType, NumericalAdjustmentType numericalAdjustmentType) {
        assertEquals(adjustment.getAdjustmentType(), adjustmentType);
        if(numericalAdjustmentType == NumericalAdjustmentType.ABSOLUTE) {
            assertEquals(adjustment.getValue(), MoneyService.toPenniesRoundNearest(request.getValue()));
        } else {
            assertEquals(adjustment.getValue(), (int)(request.getValue() * 10));
        }
        assertTrue(adjustment.getCreated() <= System.currentTimeMillis() && adjustment.getCreated() > 0);
        assertEquals(adjustment.getNumericalType(), numericalAdjustmentType);
        assertEquals(adjustment.getStaffId(), staff1.getId());
    }

    @Test
    public void postDiscountAdjustment() throws Exception {
        String token = getTokenForStaff(staff1);

        HostSessionView view = getSession(token);
        assertEquals(3.66D, view.getRemainingTotal(), 0.001);

        AdjustmentRequest adjustmentRequest = new AdjustmentRequest();
        adjustmentRequest.setSessionId(session1.getId());
        adjustmentRequest.setAdjustmentTypeId(adjustmentType2.getId());
        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest.setValue(0.66);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        view = getSession(token);
        assertEquals(3D, view.getRemainingTotal(), 0.001);

        adjustmentRequest.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.PERCENTAGE));
        adjustmentRequest.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest.setValue(50);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(adjustmentRequest)
                .post("Adjustment");

        view = getSession(token);
        assertEquals((3.66D/2D)-0.66, view.getRemainingTotal(), 0.001);

    }

    @Test
    public void postAdjustments() throws Exception {
        String token = getTokenForStaff(staff1);

        HostSessionView view = getSession(token);
        assertEquals(3.66D, view.getRemainingTotal(), 0.001);

        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType2.setType(AdjustmentTypeType.DISCOUNT);
        adjustmentTypeRepository.save(adjustmentType1);
        adjustmentTypeRepository.save(adjustmentType2);

        AdjustmentRequest adjustmentRequest1 = new AdjustmentRequest();
        adjustmentRequest1.setSessionId(session1.getId());
        adjustmentRequest1.setAdjustmentTypeId(adjustmentType1.getId());
        adjustmentRequest1.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest1.setValue(0.65);

        AdjustmentRequest adjustmentRequest2 = new AdjustmentRequest();
        adjustmentRequest2.setSessionId(session1.getId());
        adjustmentRequest2.setAdjustmentTypeId(adjustmentType2.getId());
        adjustmentRequest2.setNumericalTypeId(NumericalAdjustmentType.toClientId(NumericalAdjustmentType.ABSOLUTE));
        adjustmentRequest2.setValue(0.01);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(Lists.newArrayList(adjustmentRequest1, adjustmentRequest2))
                .post("Adjustment/multiple");
        HostAdjustmentView[] adjustmentViews = response.getBody().as(HostAdjustmentView[].class);
        assertEquals(2, adjustmentViews.length);

        view = getSession(token);
        assertEquals(3, view.getRemainingTotal(), 0.001);
    }

    private HostSessionView getSession(String token) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", session1.getId())
                .get("Session/{id}");

        return response.getBody().as(HostSessionView.class, ObjectMapperType.JACKSON_2);
    }

    @Test
    public void deleteAdjustment() throws Exception {
        String token = getTokenForStaff(staff1);

        Adjustment adjustment = new Adjustment(session1.getId());
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", adjustment.getId())
                .delete("Adjustment/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(0, session.getAdjustments().size());
    }

    @Test
    public void putAdjustmentRefund() throws Exception {
        String token = getTokenForStaff(staff1);

        Adjustment adjustment = new Adjustment(session1.getId());
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        staff1.setRole(StaffRole.ASSISTANT_MANAGER);
        staffRepository.save(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", adjustment.getId())
                .body(new HostRefundAdjustmentView())
                .put("Adjustment/refund/{id}");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertRefunded(session.getAdjustments().get(0));
    }

    @Test
    public void putAdjustmentRefundWithLink() throws Exception {
        String token = getTokenForStaff(staff1);

        Adjustment adjustment = new Adjustment(session1.getId());
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Adjustment adjustmentLinked = new Adjustment(session1.getId());
        adjustmentLinked.setLinkedTo(adjustment.getId());
        session1.getAdjustments().add(adjustmentLinked);
        sessionRepository.save(session1);

        staff1.setRole(StaffRole.ASSISTANT_MANAGER);
        staffRepository.save(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", adjustment.getId())
                .body(new HostRefundAdjustmentView())
                .put("Adjustment/refund/{id}");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertRefunded(session.getAdjustments().get(0));
        assertRefunded(session.getAdjustments().get(1));
    }

    @Test
    public void putAdjustmentRefundWithPaymentSenseGratuity1() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpPSTypes();

        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.setAdjustmentType(adjustmentType1);
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Adjustment gratuity = new Adjustment(session1.getId());
        gratuity.setAdjustmentType(adjustmentType2);
        session1.getAdjustments().add(gratuity);
        sessionRepository.save(session1);

        staff1.setRole(StaffRole.ASSISTANT_MANAGER);
        staffRepository.save(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", adjustment.getId())
                .body(new HostRefundAdjustmentView())
                .put("Adjustment/refund/{id}");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertRefunded(session.getAdjustments().get(0));
        assertNotRefunded(session.getAdjustments().get(1));
    }

    @Test
    public void putAdjustmentRefundWithPaymentSenseGratuity2() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpPSTypes();

        Adjustment adjustment = new Adjustment(session1.getId());
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.getSpecialAdjustmentData().put(Adjustment.REFERENCE, "foobar");
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Adjustment gratuity = new Adjustment(session1.getId());
        gratuity.setAdjustmentType(adjustmentType2);
        gratuity.getSpecialAdjustmentData().put(Adjustment.REFERENCE, "foobar");
        session1.getAdjustments().add(gratuity);
        sessionRepository.save(session1);

        staff1.setRole(StaffRole.ASSISTANT_MANAGER);
        staffRepository.save(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", adjustment.getId())
                .body(new HostRefundAdjustmentView())
                .put("Adjustment/refund/{id}");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertRefunded(session.getAdjustments().get(0));
        assertRefunded(session.getAdjustments().get(1));
    }

    @Test
    public void putAdjustmentRefundWithPaymentSenseGratuity3() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpPSTypes();

        Adjustment adjustment = new Adjustment(session1.getId());
        adjustmentType1.setName(adjustmentType1.getName()+"foo");
        adjustment.setAdjustmentType(adjustmentType1);
        adjustment.getSpecialAdjustmentData().put(Adjustment.REFERENCE, "foobar");
        session1.getAdjustments().add(adjustment);
        sessionRepository.save(session1);

        Adjustment gratuity = new Adjustment(session1.getId());
        gratuity.setAdjustmentType(adjustmentType2);
        gratuity.getSpecialAdjustmentData().put(Adjustment.REFERENCE, "foobar");
        session1.getAdjustments().add(gratuity);
        sessionRepository.save(session1);

        staff1.setRole(StaffRole.ASSISTANT_MANAGER);
        staffRepository.save(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", adjustment.getId())
                .body(new HostRefundAdjustmentView())
                .put("Adjustment/refund/{id}");

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertRefunded(session.getAdjustments().get(0));
        assertNotRefunded(session.getAdjustments().get(1));
    }

    private void setUpPSTypes() {
        adjustmentType1.setName(PaymentSenseConstants.PS_ADJUSTMENT_TYPE);
        adjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType2.setName(PaymentSenseConstants.PS_ADJUSTMENT_GRATUITY_TYPE);
        adjustmentType2.setType(AdjustmentTypeType.GRATUITY);
    }

    public void assertRefunded(Adjustment adjustment) {
        assertTrue(adjustment.isVoided());
        assertEquals(staff1.getId(), adjustment.getVoidedByStaffId());
        assertNotNull(adjustment.getSpecialAdjustmentData().get(RestaurantConstants.REFUND_ADJUSTMENT));
    }

    public void assertNotRefunded(Adjustment adjustment) {
        assertFalse(adjustment.isVoided());
        assertNull(adjustment.getVoidedByStaffId());
        assertNull(adjustment.getSpecialAdjustmentData().get(RestaurantConstants.REFUND_ADJUSTMENT));
    }
}
