package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.menu.CustomerSummaryMenuView;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 1.8.16..
 */
public class MenuControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void getRestaurantMenus() throws Exception {
        Response response;

        //NOT_FOUND if menu is not found for restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/Menu/RestaurantMenus/"+restaurant1.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        menu1.setRestaurantId(restaurant1.getId());
        menu2.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);
        menuRepository.save(menu2);

        //OK if menu is found for restaurant and everything else is set accordingly
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/Menu/RestaurantMenus/"+restaurant1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<CustomerSummaryMenuView> result = Arrays.asList(response.getBody().as(CustomerSummaryMenuView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(result.size(), 2);

        restaurant1.setTakeawayMenu(menu2.getId());
        restaurantRepository.save(restaurant1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/Menu/RestaurantMenus/"+restaurant1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        result = Arrays.asList(response.getBody().as(CustomerSummaryMenuView[].class, ObjectMapperType.JACKSON_2));
        assertTrue(result.stream().filter(m -> m.getMenuId().equals(menu2.getId())).findFirst().orElse(null).isTakeawayMenu());
        assertFalse(result.stream().filter(m -> m.getMenuId().equals(menu1.getId())).findFirst().orElse(null).isTakeawayMenu());
    }

    @Test
    public void getRestaurantMenusWhenDeleted() throws Exception {
        menu1.setRestaurantId(restaurant1.getId());
        menu1.setDeleted(0L);
        menu2.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);
        menuRepository.save(menu2);

        //OK if menu is found for restaurant and everything else is set accordingly
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/Menu/RestaurantMenus/"+restaurant1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<CustomerSummaryMenuView> result = Arrays.asList(response.getBody().as(CustomerSummaryMenuView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(result.size(), 1);
        assertEquals(menu2.getId(), result.get(0).getMenuId());
    }
}