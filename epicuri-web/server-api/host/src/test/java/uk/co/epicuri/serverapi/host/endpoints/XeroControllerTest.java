package uk.co.epicuri.serverapi.host.endpoints;

import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import com.xero.api.OAuthAccessToken;
import com.xero.api.OAuthAuthorizeToken;
import com.xero.api.OAuthRequestToken;
import com.xero.model.Account;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.BooleanMessage;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.KVDataConstants;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroAccountView;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroMappingsResponse;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.external.XeroInterface;
import uk.co.epicuri.serverapi.service.external.XeroService;

import java.io.IOException;
import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XeroControllerTest extends BaseIT {

    @Autowired
    private XeroService xeroService;

    @Test
    public void preAuthentication() throws Exception {
        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        OAuthRequestToken oAuthRequestToken = mock(OAuthRequestToken.class);
        when(xeroInterface.executeAuthRequest(anyString())).thenReturn(oAuthRequestToken);
        OAuthAuthorizeToken oAuthAuthorizeToken = mock(OAuthAuthorizeToken.class);
        when(xeroInterface.createAuthorizeToken(oAuthRequestToken, restaurant1.getId())).thenReturn(oAuthAuthorizeToken);

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        setUpIntegration(false);
        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero");
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());

        HashMap<String,String> map = new HashMap<>();
        map.put("tempToken", "A");
        map.put("tempTokenSecret", "B");
        when(oAuthRequestToken.getAll()).thenReturn(map);
        when(oAuthAuthorizeToken.getAuthUrl()).thenReturn("https://xxx/" + restaurant1.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        checkXeroState(response);
        KVData xeroData = restaurantRepository.findOne(restaurant1.getId()).getIntegrations().get(ExternalIntegration.XERO);
        assertNull(xeroData.getData().get(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE));
        assertNotNull(xeroData.getData().get(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE));
    }

    private void setUpIntegration(boolean addDummyTokens) {
        KVData data = new KVData();
        restaurant1.getIntegrations().put(ExternalIntegration.XERO, data);
        if(addDummyTokens) {
            data.getData().put(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE, "true");
            data.setToken("A");
            data.setSecret("B");
        }
        restaurantRepository.save(restaurant1);
    }


    @Ignore
    @Test
    public void preAuthentication_temp() {
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        setUpIntegration(false);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        checkXeroState(response);
    }

    private void checkXeroState(Response response) {
        StringMessage message = response.getBody().as(StringMessage.class);
        assertNotNull(message.getMessage());
        assertTrue(message.getMessage().startsWith("https"));
        assertTrue(message.getMessage().contains("/" + restaurant1.getId()));
        KVData xeroData = restaurantRepository.findOne(restaurant1.getId()).getIntegrations().get(ExternalIntegration.XERO);
        assertNotNull(xeroData.getToken());
        assertNotNull(xeroData.getSecret());
    }

    @Test
    public void verify() throws Exception {
        setUpIntegration(true);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        OAuthAccessToken oAuthAccessToken = mock(OAuthAccessToken.class);
        when(xeroInterface.verify("abc", "A", "B")).thenReturn(oAuthAccessToken);
        when(oAuthAccessToken.isSuccess()).thenReturn(true);
        when(oAuthAccessToken.getToken()).thenReturn("Z");
        when(oAuthAccessToken.getTokenSecret()).thenReturn("Y");
        when(oAuthAccessToken.getSessionHandle()).thenReturn("X");
        when(oAuthAccessToken.getTokenTimestamp()).thenReturn("1");

        Response response = given()
                .accept(MediaType.TEXT_HTML_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("oauth_verifier","abc")
                .get("xero/auth/" + restaurant1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        KVData xeroData = restaurantRepository.findOne(restaurant1.getId()).getIntegrations().get(ExternalIntegration.XERO);
        assertEquals("Z", xeroData.getToken());
        assertEquals("Y",xeroData.getSecret());
        assertEquals("X",xeroData.getKey());
        assertEquals(1L, xeroData.getTokenExpiration());
        assertNull(xeroData.getData().get(KVDataConstants.XERO_ERROR));
        assertNull(xeroData.getData().get(KVDataConstants.XERO_TEMP_TOKEN_AVAILABLE));
        assertTrue(Boolean.valueOf(xeroData.getData().get(KVDataConstants.XERO_REAL_TOKEN_AVAILABLE)));
    }

    @Test
    public void getTokenValidity_valid() throws Exception {
        setUpIntegration(true);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        when(xeroInterface.isTokenStale(anyString())).thenReturn(false);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero/connection");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        BooleanMessage message = response.getBody().as(BooleanMessage.class);
        assertTrue(message.isFlag());
    }

    @Test
    public void getTokenValidity_public() throws Exception {
        setUpIntegration(true);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        when(xeroInterface.isTokenStale(anyString())).thenReturn(true);
        when(xeroInterface.isPublicApp()).thenReturn(false);
        when(xeroInterface.refreshToken(any(KVData.class))).thenReturn(true);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero/connection");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        BooleanMessage message = response.getBody().as(BooleanMessage.class);
        assertFalse(message.isFlag());
    }

    @Test
    public void getMappings() throws Exception{
        setUpIntegration(true);
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        tax1.setName("tax1");
        tax1.setCountryId(country1.getId());
        taxRateRepository.save(tax1);
        restaurant1.setCountryId(country1.getId());
        restaurant1.getAdjustmentTypes().clear();
        restaurant1.getAdjustmentTypes().add(adjustmentType1.getId());
        restaurantRepository.save(restaurant1);

        XeroInterface xeroInterface = mock(XeroInterface.class);
        Whitebox.setInternalState(xeroService, "xeroInterface", xeroInterface);
        when(xeroInterface.isTokenStale(anyString())).thenReturn(false);
        when(xeroInterface.isPublicApp()).thenReturn(false);
        Account account = mock(Account.class);
        when(xeroInterface.getAccounts(anyString(),anyString())).thenReturn(Lists.newArrayList(account));
        when(account.getName()).thenReturn("A");
        when(account.getAccountID()).thenReturn("B");
        when(account.getCode()).thenReturn("C");
        when(account.getType()).thenReturn(null);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("xero/mappings");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        XeroMappingsResponse xeroMappingsResponse = response.getBody().as(XeroMappingsResponse.class);

        assertEquals(2, xeroMappingsResponse.getAccounts().size());
        assertEquals(XeroAccountView.DEFAULT_NONE, xeroMappingsResponse.getAccounts().get(0));
        XeroAccountView view = xeroMappingsResponse.getAccounts().get(1);
        assertEquals("A", view.getName());
        assertEquals("B", view.getId());
        assertEquals("C", view.getCode());
        assertNull(view.getType());

    }
}