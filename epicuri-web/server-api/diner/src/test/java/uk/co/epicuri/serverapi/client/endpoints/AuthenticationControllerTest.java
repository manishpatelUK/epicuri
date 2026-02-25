package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.customer.*;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Lazar on 5/20/2016.
 */
public class AuthenticationControllerTest extends BaseIT {

    @Test
    public void testSMSLoginNegatives() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        smsRegistrationView.setName(new Name("foo", "bar"));
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        smsRegistrationView.setPhoneNumber("12345");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        smsRegistrationView.setInternationalCode("44");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testSMSLoginDuplication1() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();

        //phone number doesn't exist, email does exist
        smsRegistrationView.setPhoneNumber("00000000000");
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setEmail("foo@bar.com");
        smsRegistrationView.setName(new Name("foo", "bar"));
        customer1.setEmail("foo@bar.com");
        customerRepository.save(customer1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Customer newSaved1 = customerRepository.findOne(response.getBody().as(IdPojo.class).getId());
        assertNotEquals(newSaved1.getId(), customer1.getId());
    }

    @Test
    public void testSMSLoginDuplication2() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();

        //phone number exists, email does exist
        customer1.setEmail("foo@bar.com");
        customer1.setPhoneNumber("7984688477");
        customerRepository.save(customer1);
        smsRegistrationView.setPhoneNumber(customer1.getPhoneNumber());
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setEmail("foo@bar.com");
        smsRegistrationView.setName(new Name("foo", "bar"));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Customer newSaved1 = customerRepository.findOne(response.getBody().as(IdPojo.class).getId());
        assertEquals(newSaved1.getId(), customer1.getId());
        assertEquals(customer1.getEmail(), newSaved1.getEmail());
        assertEquals(customer1.getPhoneNumber(), newSaved1.getPhoneNumber());
    }

    @Test
    public void testSMSLoginDuplication3() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();

        //phone number exists, email does exist (on a different customer) and current customer has null email
        smsRegistrationView.setPhoneNumber("7984688477");
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setEmail("foo@bar.com");
        smsRegistrationView.setName(new Name("foo", "bar"));
        customer2.setEmail("foo@bar.com");
        customer1.setPhoneNumber("7984688477");
        customer1.setEmail(null);
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Customer newSaved1 = customerRepository.findOne(response.getBody().as(IdPojo.class).getId());
        assertEquals(newSaved1.getId(), customer1.getId());
        assertEquals("foo@bar.com", newSaved1.getEmail());
    }

    @Test
    public void testSMSLoginDuplication4() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();

        //phone number exists, email does exist (on a different customer) and current customer has non-null email
        smsRegistrationView.setPhoneNumber("7984688477");
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setEmail("foo@bar.com");
        smsRegistrationView.setName(new Name("foo", "bar"));
        customer2.setEmail("foo@bar.com");
        customer1.setPhoneNumber("7984688477");
        customer1.setEmail("man@chu.com");
        customerRepository.save(customer1);
        customerRepository.save(customer2);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Customer newSaved1 = customerRepository.findOne(response.getBody().as(IdPojo.class).getId());
        assertEquals(newSaved1.getId(), customer1.getId());
        assertEquals("man@chu.com", newSaved1.getEmail());
    }

    @Test
    public void testSMSLogin() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();
        smsRegistrationView.setPhoneNumber("07984656449");
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setName(new Name("foo", "bar"));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        IdPojo idPojo = response.getBody().as(IdPojo.class);
        Customer customer = customerRepository.findOne(idPojo.getId());
        assertNotNull(customer);
        assertEquals("000000", customer.getConfirmationCode());
        assertEquals("7984656449", customer.getPhoneNumber());
        assertEquals("44", customer.getInternationalCode());
        assertNotEquals(customer1.getId(), customer.getId());
        assertNotEquals(customer2.getId(), customer.getId());
        assertNotEquals(customer3.getId(), customer.getId());
    }

    @Test
    public void testSMSLoginWithEmail() throws Exception {
        Customer customer = doLoginWithNumberAndEmail("7984688477", "foo@bar.com");
        assertEquals("foo@bar.com", customer.getEmail());
        assertNotEquals(customer1.getId(), customer.getId());
        assertNotEquals(customer2.getId(), customer.getId());
        assertNotEquals(customer3.getId(), customer.getId());
    }

    private Customer doLoginWithNumberAndEmail(String number, String email) {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();
        smsRegistrationView.setPhoneNumber(number);
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setEmail(email);
        smsRegistrationView.setName(new Name("foo", "bar"));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsRegistrationView)
                .post("Authentication/loginSMS");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        IdPojo idPojo = response.getBody().as(IdPojo.class);
        Customer customer = customerRepository.findOne(idPojo.getId());
        assertNotNull(customer);
        assertEquals("000000", customer.getConfirmationCode());
        assertEquals(smsRegistrationView.getPhoneNumber(), customer.getPhoneNumber());
        return customer;
    }

    @Test
    public void testSMSLoginUserExists1() throws Exception {
        customer1.setPhoneNumber("7984688477");
        customer1.setEmail("foo@bar.com");
        customerRepository.save(customer1);

        Customer customer = doLoginWithNumberAndEmail("7984688477", "foo@bar.com");
        assertEquals("foo@bar.com", customer.getEmail());
        assertEquals(customer1.getId(), customer.getId());
    }

    @Test
    public void testSMSLoginUserExists2() throws Exception {
        customer1.setPhoneNumber("7984688477");
        customerRepository.save(customer1);

        Customer customer = doLoginWithNumberAndEmail("7984688477", "foo@bar.com");
        assertEquals(customer1.getId(), customer.getId());
    }

    @Test
    public void testCheckCodeNegatives() throws Exception {
        SMSAuthenticationRequest smsAuthenticationRequest = new SMSAuthenticationRequest();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsAuthenticationRequest)
                .post("Authentication/checkCode");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        smsAuthenticationRequest.setCode("000000");

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsAuthenticationRequest)
                .post("Authentication/checkCode");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        smsAuthenticationRequest.setId(customer1.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsAuthenticationRequest)
                .post("Authentication/checkCode");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        customer1.setConfirmationCode("1111111");
        customerRepository.save(customer1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsAuthenticationRequest)
                .post("Authentication/checkCode");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
    }

    @Test
    public void testCheckCode() throws Exception {
        customer1.setConfirmationCode("000000");
        customer1.setAuthKey(null);
        customerRepository.save(customer1);

        SMSAuthenticationRequest smsAuthenticationRequest = new SMSAuthenticationRequest();
        smsAuthenticationRequest.setCode("000000");
        smsAuthenticationRequest.setId(customer1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(smsAuthenticationRequest)
                .post("Authentication/checkCode");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Customer customer = customerRepository.findOne(customer1.getId());
        assertNotNull(customer.getAuthKey());
    }
}