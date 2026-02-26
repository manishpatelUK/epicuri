package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 14.7.16..
 */
public class CustomerControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void testGetCustomer() throws Exception {
        Response response;

        String token = "";

        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("phoneNumber", customer1.getPhoneNumber())
                .queryParam("email", customer1.getEmail())
                .get("/Customer");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);

        //OK if staff member exists in that restaurant and customer exists
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("phoneNumber", customer1.getPhoneNumber())
                .queryParam("email", customer1.getEmail())
                .get("/Customer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostCustomerView> hostCustomerViews = Arrays.asList(response.getBody().as(HostCustomerView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, hostCustomerViews.size());
        assertEqual(customer1, hostCustomerViews.stream().filter(c -> c.getId().equals(customer1.getId())).findFirst().orElse(null));

        //OK if staff member exists in that restaurant and customer doesnt exist

        customerRepository.deleteAll();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("phoneNumber", customer1.getPhoneNumber())
                .queryParam("email", customer1.getEmail())
                .get("/Customer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        hostCustomerViews = Arrays.asList(response.getBody().as(HostCustomerView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, hostCustomerViews.size());

        // null phonenumber
        String expectedEmail = "foomanchu@foomanhcu.com";
        String expectedPhoneNumber = "123345679";
        customer1.setEmail(expectedEmail);
        customer1.setPhoneNumber(expectedPhoneNumber);
        customerRepository.save(customer1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("email", expectedEmail)
                .get("/Customer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        hostCustomerViews = Arrays.asList(response.getBody().as(HostCustomerView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, hostCustomerViews.size());
        assertEqual(customer1, hostCustomerViews.stream().filter(c -> c.getId().equals(customer1.getId())).findFirst().orElse(null));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("phoneNumber", customer1.getPhoneNumber())
                .get("/Customer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        hostCustomerViews = Arrays.asList(response.getBody().as(HostCustomerView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, hostCustomerViews.size());
        assertEqual(customer1, hostCustomerViews.stream().filter(c -> c.getId().equals(customer1.getId())).findFirst().orElse(null));
    }

    private void assertEqual(Customer customer, HostCustomerView view) {
        assertEquals(customer.getId(), view.getId());
    }

    @Test
    public void testGetCustomerByPartialNumber() throws Exception {
        String token = getTokenForStaff(staff1);

        String expectedPhoneNumber = "4478882228882";
        customer1.setPhoneNumber(expectedPhoneNumber);
        customerRepository.save(customer1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("phoneNumber", "078882228882")
                .get("/Customer");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostCustomerView> hostCustomerViews = Arrays.asList(response.getBody().as(HostCustomerView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, hostCustomerViews.size());
        assertEqual(customer1, hostCustomerViews.get(0));
    }
}