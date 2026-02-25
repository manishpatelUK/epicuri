package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.mews.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.external.MewsService;

import java.util.Collection;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by manish on 12/05/2017.
 */
public class MewsControllerTest extends BaseIT{
    private String accessToken = "0F7F56DBB8B342B08B532DF4C8A87997-D3FFAC0F8E438572A1B142B0203CAEA";

    @Autowired
    private MewsService mewsService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void getCustomers() throws Exception {
        RestTemplate restTemplate = EasyMock.createMock(RestTemplate.class);
        Whitebox.setInternalState(mewsService,"restTemplate",restTemplate);

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Mews/Customers");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        MewsCustomerSearchResponse mewsCustomerSearchResponse = new MewsCustomerSearchResponse();
        mewsCustomerSearchResponse.getCustomers().add(new MewsCustomer());
        ResponseEntity<MewsCustomerSearchResponse> responseEntity = new ResponseEntity<>(mewsCustomerSearchResponse, HttpStatus.OK);
        //noinspection unchecked
        expect(restTemplate.exchange(anyObject(String.class), anyObject(HttpMethod.class), anyObject(HttpEntity.class), anyObject(Class.class))).andReturn(responseEntity);
        replay(restTemplate);

        KVData kvData = new KVData();
        kvData.setToken(accessToken);
        restaurant1.getIntegrations().put(ExternalIntegration.MEWS, kvData);
        restaurantRepository.save(restaurant1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Mews/Customers");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        @SuppressWarnings("unchecked")
        Collection<MewsCustomer> customers = response.getBody().as(Collection.class, ObjectMapperType.JACKSON_2);

        verify(restTemplate);
        assertEquals(1, customers.size());
    }

    @Ignore //for now because dev seems unstable
    @Test
    public void postAdjustment() throws Exception {
        RestTemplate restTemplate = EasyMock.createMock(RestTemplate.class);
        Whitebox.setInternalState(mewsService,"restTemplate",restTemplate);

        String token = getTokenForStaff(staff1);

        MewsRequest mewsRequest = new MewsRequest();
        MewsCustomer mewsCustomer = new MewsCustomer();
        mewsCustomer.setId("foo");
        mewsRequest.setCustomer(mewsCustomer);
        mewsRequest.setSessionId(session1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(mewsRequest)
                .post("Mews/Adjustment");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setName(MewsConstants.MEWS_ADJUSTMENT_TYPE);
        adjustmentType.setType(AdjustmentTypeType.PAYMENT);
        adjustmentTypeRepository.insert(adjustmentType);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(mewsRequest)
                .post("Mews/Adjustment");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        KVData kvData = new KVData();
        kvData.setToken(accessToken);
        kvData.setKey("abc");
        restaurant1.getIntegrations().put(ExternalIntegration.MEWS, kvData);
        restaurantRepository.save(restaurant1);

        MewsChargeResponse mewsChargeResponse = new MewsChargeResponse();
        mewsChargeResponse.setOrderId("foobar");
        ResponseEntity<MewsChargeResponse> responseEntity = new ResponseEntity<>(mewsChargeResponse, HttpStatus.OK);
        //noinspection unchecked
        expect(restTemplate.exchange(anyObject(String.class), anyObject(HttpMethod.class), anyObject(HttpEntity.class), anyObject(Class.class))).andReturn(responseEntity);
        replay(restTemplate);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(mewsRequest)
                .post("Mews/Adjustment");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        verify(restTemplate);
        session1 = sessionRepository.findOne(session1.getId());
        assertEquals(1, session1.getAdjustments().size());
        assertEquals(adjustmentType.getName(), session1.getAdjustments().get(0).getAdjustmentType().getName());
    }

}