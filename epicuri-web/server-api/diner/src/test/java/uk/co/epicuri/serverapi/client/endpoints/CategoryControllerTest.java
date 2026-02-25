package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.common.CuisineView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 14.7.16..
 */
public class CategoryControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void getCuisineCategories() throws Exception {
        //OK if the user(customer) is logged in and all values from cuisine are returned
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/Category");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<CuisineView> cuisines = Arrays.asList(response.getBody().as(CuisineView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, cuisines.size());

        cuisineRepository.deleteAll();

        //OK if the user(customer) is logged in and all values from cuisine are returned
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/Category");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        cuisines = Arrays.asList(response.getBody().as(CuisineView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, cuisines.size());
    }

}