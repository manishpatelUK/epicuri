package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.response.Response;
import de.flapdoodle.embed.process.collections.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.host.endpoints.MenuBaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

/**
 * Created by manish on 28/03/2017.
 */
public class MenuItemEditWorkflow extends MenuBaseIT{

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Category category  = new Category();
        menu1.getCategories().add(category);
        category.setId(IDAble.generateId(menu1.getId()));
        category.getCourseIds().add(course1.getId());
        category.setName("Cat 1");
        Group group = new Group();
        category.getGroups().add(group);
        group.setId(IDAble.generateId(category.getId()));
        group.setName("Group 1");
        group.getItems().add(menuItem1.getId());

        menuRepository.save(menu1);
    }

    @Test
    public void testMenuItemEditWorkflow() throws Exception{
        String token = getTokenForStaff(staff1);

        Response response1 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu1.getId())
                .get("Menu/{id}");

        MenuView menuView = response1.getBody().as(MenuView.class);
        MenuItemView menuItemView = menuView.getMenuCategories().get(0).getMenuGroups().get(0).getMenuItems().get(0);

        menuItemView.setName("Edited");

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(menuItemView)
                .pathParam("id", menuItemView.getId())
                .put("MenuItem/{id}");

        assertEquals(HttpStatus.ACCEPTED.value(),response2.getStatusCode());

        Response response3 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu1.getId())
                .get("Menu/{id}");

        menuView = response3.getBody().as(MenuView.class);
        assertEquals(1, menuView.getMenuCategories().get(0).getMenuGroups().get(0).getMenuItems().size());
        assertEquals(menuItemView.getId(), menuView.getMenuCategories().get(0).getMenuGroups().get(0).getMenuItems().get(0).getId());
        assertEquals(menuItemView.getName(), menuView.getMenuCategories().get(0).getMenuGroups().get(0).getMenuItems().get(0).getName());

    }

}
