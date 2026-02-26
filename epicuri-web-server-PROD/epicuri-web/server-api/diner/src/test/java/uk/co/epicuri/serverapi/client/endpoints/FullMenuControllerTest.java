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
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 1.8.16..
 */
public class FullMenuControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        List<String> courseIds1 = new ArrayList<>();
        courseIds1.add(course1.getId());
        courseIds1.add(course2.getId());

        List<String> courseIds2 = new ArrayList<>();
        courseIds2.add(course3.getId());

        category1.setCourseIds(courseIds1);
        category2.setCourseIds(courseIds2);

        List<Category> categories = new ArrayList<>();
        categories.add(category1);
        categories.add(category2);

        menu1.setRestaurantId(restaurant1.getId());
        menu1.setId("m1");
        menu1.setCategories(categories);

        menuRepository.save(menu1);


    }

    @Test
    public void getFullMenu() throws Exception {
        Response response;


        //OK if everything is set accordingly
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/FullMenu/"+menu1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        MenuView menuView = response.getBody().as(MenuView.class, ObjectMapperType.JACKSON_2);

        assertEquals(menuView.getId(), menu1.getId());
        assertEquals(1, menuView.getMenuModifierGroups().size());
        assertEquals(2, menuView.getMenuCategories().size());
        assertEquals(restaurant1.getId(), menuView.getRestaurantId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/FullMenu/0");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void getFullMenuWhenDeleted() throws Exception {
        menu1.setDeleted(0L);
        menuRepository.save(menu1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("/FullMenu/"+menu1.getId());

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

}