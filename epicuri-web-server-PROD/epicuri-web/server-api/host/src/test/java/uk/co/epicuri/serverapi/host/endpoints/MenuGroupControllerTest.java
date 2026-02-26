package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupCloneView;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class MenuGroupControllerTest extends MenuBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Ignore
    @Test
    public void testGetMenuGroups() throws Exception {

    }

    @Test
    public void testPostMenuGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        long modifiedTime = System.currentTimeMillis();
        menu2.setLastUpdate(modifiedTime);
        menuRepository.save(menu2);

        Category toChange = menu2.getCategories().get(0);

        GroupView groupView = new GroupView();
        groupView.setMenuCategoryId(toChange.getId());
        groupView.setGroupName("some new group");
        groupView.setOrder(10);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .post("MenuGroup");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        GroupView groupViewResponse = response.getBody().as(GroupView.class, ObjectMapperType.JACKSON_2);
        assertEquals(groupView.getMenuCategoryId(), groupViewResponse.getMenuCategoryId());
        assertEquals(groupView.getGroupName(), groupViewResponse.getGroupName());
        assertEquals(groupView.getOrder(), groupViewResponse.getOrder());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        modifiedTime = menu2.getLastUpdate();

        //check bad category id
        groupView.setMenuCategoryId("some bad id");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .post("MenuGroup");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertEquals(modifiedTime, menu2.getLastUpdate());

        //null category id
        groupView.setMenuCategoryId(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .post("MenuGroup");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertEquals(modifiedTime, menu2.getLastUpdate());
    }

    @Test
    public void testPutMenuGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        long modifiedTime = System.currentTimeMillis();
        menu2.setLastUpdate(modifiedTime);
        menuRepository.save(menu2);

        Category category = menu2.getCategories().get(0);
        Group groupToChange = category.getGroups().get(1);

        GroupView groupView = new GroupView();
        groupView.setMenuCategoryId(category.getId());
        groupView.setGroupName("some new name");
        groupView.setOrder(10);
        groupView.setMenuItemIds(groupToChange.getItems());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .pathParam("id", groupToChange.getId())
                .put("MenuGroup/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        testEquality(groupView, menu2.getCategories().stream().filter(c -> c.getId().equals(category.getId())).findFirst().get().getGroups().stream().filter(g -> g.getId().equals(groupToChange.getId())).findFirst().get());

        //change items
        groupView.getMenuItemIds().remove(0);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .pathParam("id", groupToChange.getId())
                .put("MenuGroup/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        testEquality(groupView, menu2.getCategories().stream().filter(c -> c.getId().equals(category.getId())).findFirst().get().getGroups().stream().filter(g -> g.getId().equals(groupToChange.getId())).findFirst().get());

        // null category id
        groupView.setMenuCategoryId(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .pathParam("id", groupToChange.getId())
                .put("MenuGroup/{id}");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        // none existing group id
        groupView.setMenuCategoryId(category.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .pathParam("id", "a bad id")
                .put("MenuGroup/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    private void testEquality(GroupView groupView, Group group) {
        assertEquals(groupView.getGroupName(), group.getName());
        assertEquals(groupView.getOrder(), group.getOrder());
        assertEquals(groupView.getMenuItemIds(), group.getItems());
    }

    @Test
    public void testDeleteMenuGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        long modifiedTime = System.currentTimeMillis();
        menu2.setLastUpdate(modifiedTime);
        menuRepository.save(menu2);

        Category category = menu2.getCategories().get(0);
        Group groupToChange = category.getGroups().get(1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", groupToChange.getId())
                .delete("MenuGroup/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        assertFalse(menu2.getCategories().stream().filter(c -> c.getId().equals(category.getId())).findFirst().get().getGroups().stream().anyMatch(g -> g.getId().equals(groupToChange.getId())));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "a bad id")
                .delete("MenuGroup/{id}");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testPostCloneGroup() throws Exception {
        setUpMenu1();

        String token = getTokenForStaff(staff1);
        IdPojoAndName idPojoAndName = new IdPojoAndName();
        idPojoAndName.setId(group1.getId());
        idPojoAndName.setName("group1 clone");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", group1.getId())
                .body(idPojoAndName)
                .post("MenuGroup/clone/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Menu testMenu = menuRepository.findOne(menu1.getId());
        assertEquals(1, testMenu.getCategories().size());
        Category testCategory = testMenu.getCategories().get(0);
        assertEquals(2, testCategory.getGroups().size());
        assertEquals(group1, testCategory.getGroups().get(0));
        Group testGroup = testCategory.getGroups().get(1);
        assertEquals(idPojoAndName.getName(), testGroup.getName());
        assertEquals(group1.getItems(), testGroup.getItems());
        assertNotEquals(group1.getId(), testGroup.getId());
        assertTrue(testGroup.getOrder() > group1.getOrder());
    }

    @Test
    public void testPostCloneGroupToDifferentCategory() throws Exception {
        setUpMenu1();
        setUpMenu2();
        String token = getTokenForStaff(staff1);
        GroupCloneView groupCloneView = new GroupCloneView();
        groupCloneView.setId(group1.getId());
        groupCloneView.setName("group1 clone");
        groupCloneView.setCategoryId(category2.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", group1.getId())
                .body(groupCloneView)
                .post("MenuGroup/clone/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());

        Menu testMenu1 = menuRepository.findOne(menu1.getId());
        assertEquals(menu1, testMenu1);
        Menu testMenu2 = menuRepository.findOne(menu2.getId());
        assertEquals(1, testMenu2.getCategories().size());
        Category testCategory = testMenu2.getCategories().get(0);
        assertEquals(1, testCategory.getGroups().size());
        Group testGroup = testCategory.getGroups().get(0);
        assertEquals(groupCloneView.getName(), testGroup.getName());
        assertEquals(group1.getItems(), testGroup.getItems());
        assertNotEquals(group1.getId(), testGroup.getId());
    }

    private void setUpMenu1() {
        category1.setId(IDAble.generateId(menu1.getId()));
        group1.setId(IDAble.generateId(category1.getId()));
        group1.setName("ketchup");
        menu1.getCategories().clear();
        menu1.getCategories().add(category1);
        category1.getGroups().clear();
        category1.getGroups().add(group1);
        menu1.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);
    }

    private void setUpMenu2() {
        category2.setId(IDAble.generateId(menu2.getId()));
        menu2.getCategories().clear();
        menu2.getCategories().add(category2);
        category2.getGroups().clear();
        menu2.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu2);
    }
}