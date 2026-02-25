package uk.co.epicuri.serverapi.client.endpoints;

import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCustomerView;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class UserControllerTest extends BaseIT {

    private Preference preference1, preference2, preference3, preference4;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        preferencesRepository.deleteAll();
        preference1 = insertPreference("foo", PreferenceType.ALLERGY);
        preference2 = insertPreference("bar", PreferenceType.DIETARY);
        preference3 = insertPreference("man", PreferenceType.FOOD);
        preference4 = insertPreference("chu", PreferenceType.FOOD);

        customerLogin.setFirstName("foo");
        customerLogin.setLastName("bar");
        customerRepository.save(customerLogin);
    }

    public Preference insertPreference(String name, PreferenceType preferenceType) {
        Preference preference = new Preference();
        preference.setPreferenceType(preferenceType);
        preference.setName(name);

        return preferencesRepository.insert(preference);
    }


    private String getToken() {
        return getTokenForCustomer(customerLogin);
    }

    @Test
    public void testGetUserOptionsOld() throws Exception {
        testUserOptions("1");
    }

    @Test
    public void testGetUserOptions() throws Exception {
        testUserOptions("options");
    }

    private void testUserOptions(String endpoint) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, "")
                .get("/User/" + endpoint);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        String token = getToken();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/User/1");

        @SuppressWarnings("unchecked") Map<String,List<Map<String,String>>> options = response.getBody().as(Map.class);

        assertEquals(3, options.size());
        assertEquals(preference1.getName(), options.get(PreferenceType.ALLERGY.getKey()).get(0).get("Value"));
        assertEquals(preference2.getName(), options.get(PreferenceType.DIETARY.getKey()).get(0).get("Value"));
        assertTrue(options.get(PreferenceType.FOOD.getKey()).get(0).get("Value").equals(preference3.getName()) || options.get(PreferenceType.FOOD.getKey()).get(0).get("Value").equals(preference4.getName()));
        assertTrue(options.get(PreferenceType.FOOD.getKey()).get(1).get("Value").equals(preference3.getName()) || options.get(PreferenceType.FOOD.getKey()).get(1).get("Value").equals(preference4.getName()));
    }

    @Test
    public void testPutUser() throws Exception {
        String token = getToken();

        customerLogin.setEmail("foo@bar.com");
        customerRepository.save(customerLogin);

        CustomerCustomerView customerCustomerView = getCustomerCustomerView();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCustomerView)
                .put("/User");

        CustomerCustomerView responseView = response.getBody().as(CustomerCustomerView.class);
        assertCustomersEqual(customerRepository.findOne(customerLogin.getId()), responseView);

        assertFalse(responseView.getAllergies().contains("dummy"));
        assertFalse(responseView.getDietaryRequirements().contains("dummy"));
        assertFalse(responseView.getFoodPreferences().contains("dummy"));
    }

    @Test
    public void testPutUserEquivalence() throws Exception {
        String token = getToken();

        Customer before = customerRepository.findOne(customerLogin.getId());
        CustomerCustomerView customerCustomerView = new CustomerCustomerView();

        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCustomerView)
                .put("/User");

        Customer after = customerRepository.findOne(customerLogin.getId());
        assertTrue(EqualsBuilder.reflectionEquals(before, after, "email"));
    }

    @Test
    public void testPutUserExistingEmail() throws Exception {
        String token = getToken();

        customerLogin.setEmail("foo@bar.com");
        customerRepository.save(customerLogin);

        CustomerCustomerView customerCustomerView = getCustomerCustomerView();
        customer1.setEmail(customerCustomerView.getEmail());
        customerRepository.save(customer1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCustomerView)
                .put("/User");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        customerCustomerView.setEmail("anytingelse@foo.com");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCustomerView)
                .put("/User");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(customerCustomerView.getEmail(), customerRepository.findOne(customerLogin.getId()).getEmail());
    }

    @Test
    public void testPutUserDeleteEmail() throws Exception {
        String token = getToken();

        customerLogin.setEmail("foo@bar.com");
        customerRepository.save(customerLogin);

        CustomerCustomerView customerCustomerView = getCustomerCustomerView();
        customerCustomerView.setEmail(null);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(customerCustomerView)
                .put("/User");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNull(customerRepository.findOne(customerLogin.getId()).getEmail());
    }

    @Test
    public void testPostCC() throws Exception {
        String token = getToken();

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        com.stripe.model.Customer c = new com.stripe.model.Customer();
        c.setId("foo");
        expect(stripeService.acquireCustomer(anyString(), anyString(), anyString())).andReturn(c);
        replay(stripeService);

        CreditCardData creditCardData = createCreditCardData();
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(creditCardData)
                .post("/User/cc");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        verify(stripeService);

        CreditCardData creditCardDataResponse = response.as(CreditCardData.class);
        assertEquals("123", creditCardDataResponse.getCcToken());
        assertEquals("foo", creditCardDataResponse.getExternalId());

        Customer customer = customerRepository.findOne(customerLogin.getId());
        assertEquals("123", customer.getCcData().getCcToken());
        assertEquals("foo", customer.getCcData().getExternalId());
    }

    @Test
    public void deleteCC() throws Exception {
        String token = getToken();

        CreditCardData creditCardData = createCreditCardData();
        customerLogin.setCcData(creditCardData);
        customerRepository.save(customerLogin);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(creditCardData)
                .delete("/User/cc");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Customer customer = customerRepository.findOne(customerLogin.getId());
        assertNull(customer.getCcData());

        customerLogin.setCcData(creditCardData);
        customerRepository.save(customerLogin);
        creditCardData.setCcToken("foo");

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(creditCardData)
                .delete("/User/cc");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    private CreditCardData createCreditCardData() {
        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setCcToken("123");
        creditCardData.setDigits("1234");
        creditCardData.setMonthExpiry("01");
        creditCardData.setYearExpiry("17");
        return creditCardData;
    }

    @Test
    public void testGetCustomer() throws Exception {
        String token = getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/User");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        CustomerCustomerView customerCustomerView = response.as(CustomerCustomerView.class);
        assertCustomersEqual(customerLogin, customerCustomerView);
    }

    public CustomerCustomerView getCustomerCustomerView() {
        CustomerCustomerView customerCustomerView = new CustomerCustomerView(customerLogin);

        customerCustomerView.getName().setFirstName("n");
        customerCustomerView.getName().setLastName("f");
        customerCustomerView.setEmail("fa@foo.com");
        customerCustomerView.setPassword("bar");
        Address address = new Address();
        address.setStreet("f");
        address.setCity("d");
        customerCustomerView.setAddress(address);
        customerCustomerView.setAllergies(Lists.newArrayList(preference1.getId(),"dummy"));
        customerCustomerView.setDietaryRequirements(Lists.newArrayList(preference2.getId(), "dummy"));
        customerCustomerView.setFoodPreferences(Lists.newArrayList(preference4.getId(), "dummy"));
        long now = System.currentTimeMillis() / 1000;
        customerCustomerView.setBirthday(now);
        customerCustomerView.setFavouriteDrink("q");
        customerCustomerView.setFavouriteDrink("e");
        customerCustomerView.setHatedFood("y");
        return customerCustomerView;
    }

    private void assertCustomersEqual(Customer customer, CustomerCustomerView customerCustomerView) {
        assertEquals(customer.getFirstName(), customerCustomerView.getName().getFirstName());
        assertEquals(customer.getLastName(), customerCustomerView.getName().getLastName());
        assertEquals(customer.getEmail(), customerCustomerView.getEmail());
        assertEquals(customer.getAddress(), customerCustomerView.getAddress());
        assertEquals(customer.getAllergies(), customerCustomerView.getAllergies());
        assertEquals(customer.getDietaryRequirements(), customerCustomerView.getDietaryRequirements());
        assertEquals(customer.getFoodPreferences(), customerCustomerView.getFoodPreferences());
        if (customer.getBirthday() == null) {
            assertNull(customerCustomerView.getBirthday());
        } else {
            assertEquals(customer.getBirthday() / 1000, customerCustomerView.getBirthday().longValue());
        }
        assertEquals(customer.getFavouriteFood(), customerCustomerView.getFavouriteFood());
        assertEquals(customer.getFavouriteDrink(), customerCustomerView.getFavouriteDrink());
        assertEquals(customer.getHatedFood(), customerCustomerView.getHatedFood());
    }
}

