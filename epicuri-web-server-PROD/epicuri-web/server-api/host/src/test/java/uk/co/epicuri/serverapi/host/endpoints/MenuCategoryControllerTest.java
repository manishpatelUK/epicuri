package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryCloneView;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class MenuCategoryControllerTest extends MenuBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testPostMenuCategory() throws Exception {
        String token = getTokenForStaff(staff1);

        long modifiedTime = System.currentTimeMillis();
        menu2.setLastUpdate(modifiedTime);
        menuRepository.save(menu2);

        CategoryView categoryView = new CategoryView();
        categoryView.setCategoryName("a category");
        categoryView.setMenuId(menu2.getId());
        categoryView.setDefaultCourseIds(new ArrayList<>());
        categoryView.getDefaultCourseIds().add(course1.getId());
        categoryView.getDefaultCourseIds().add(course2.getId());
        categoryView.getDefaultCourseIds().add(course2.getId());
        categoryView.getDefaultCourseIds().add(course2.getId());
        categoryView.getDefaultCourseIds().add("some none existent id");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(categoryView)
                .post("MenuCategory");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        CategoryView saved = response.getBody().as(CategoryView.class, ObjectMapperType.JACKSON_2);
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getCategories().stream().anyMatch(c -> c.getId().equals(saved.getId())));
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        assertEquals(2, menu2.getCategories().stream().filter(c -> c.getId().equals(saved.getId())).findFirst().get().getCourseIds().size());


        //throw error without menu id
        categoryView.setMenuId(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(categoryView)
                .post("MenuCategory");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testPutMenuCategory() throws Exception {
        String token = getTokenForStaff(staff1);

        long modifiedTime = System.currentTimeMillis();
        menu2.setLastUpdate(modifiedTime);
        menuRepository.save(menu2);

        Category toChange = menu2.getCategories().get(0);
        List<Group> groups = toChange.getGroups();

        CategoryView categoryView = new CategoryView();
        categoryView.setId(toChange.getId());
        categoryView.setCategoryName("a changed name");
        categoryView.setMenuId(menu2.getId());
        categoryView.setDefaultCourseIds(toChange.getCourseIds());
        categoryView.setOrder(toChange.getOrder());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(categoryView)
                .pathParam("id", categoryView.getId())
                .put("MenuCategory/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        modifiedTime = menu2.getLastUpdate();
        Category toTest = menu2.getCategories().stream().filter(c -> c.getId().equals(toChange.getId())).findFirst().get();
        assertEquals(categoryView.getCategoryName(), toTest.getName());
        assertEquals(categoryView.getDefaultCourseIds(), toTest.getCourseIds());
        assertEquals(groups, toTest.getGroups());
        assertEquals(categoryView.getOrder(), toTest.getOrder());

        categoryView.getDefaultCourseIds().remove(course2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(categoryView)
                .pathParam("id", categoryView.getId())
                .put("MenuCategory/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        modifiedTime = menu2.getLastUpdate();
        toTest = menu2.getCategories().stream().filter(c -> c.getId().equals(toChange.getId())).findFirst().get();
        assertEquals(categoryView.getCategoryName(), toTest.getName());
        assertEquals(categoryView.getDefaultCourseIds(), toTest.getCourseIds());
        assertEquals(groups, toTest.getGroups());
        assertEquals(categoryView.getOrder(), toTest.getOrder());

        categoryView.setOrder(10);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(categoryView)
                .pathParam("id", categoryView.getId())
                .put("MenuCategory/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        modifiedTime = menu2.getLastUpdate();
        toTest = menu2.getCategories().stream().filter(c -> c.getId().equals(toChange.getId())).findFirst().get();
        assertEquals(categoryView.getCategoryName(), toTest.getName());
        assertEquals(categoryView.getDefaultCourseIds(), toTest.getCourseIds());
        assertEquals(groups, toTest.getGroups());
        assertEquals(categoryView.getOrder(), toTest.getOrder());
    }

    @Test
    public void testDeleteMenuCategory() throws Exception {
        String token = getTokenForStaff(staff1);

        long modifiedTime = System.currentTimeMillis();
        menu2.setLastUpdate(modifiedTime);
        menuRepository.save(menu2);

        Category toDelete = menu2.getCategories().get(0);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", toDelete.getId())
                .delete("MenuCategory/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        assertFalse(menu2.getCategories().stream().anyMatch(c -> c.getId().equals(toDelete.getId())));

        int currentSize = menu2.getCategories().size();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", toDelete.getId())
                .delete("MenuCategory/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        assertEquals(currentSize, menu2.getCategories().size());

        menu2.getCategories().clear();
        menuRepository.save(menu2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", toDelete.getId())
                .delete("MenuCategory/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        menu2 = menuRepository.findOne(menu2.getId());
        assertTrue(menu2.getLastUpdate() > modifiedTime);
        assertEquals(0, menu2.getCategories().size());
    }

    @Test
    public void testPostCloneCategory() throws Exception {
        setUpMenu1();

        String token = getTokenForStaff(staff1);
        IdPojoAndName idPojoAndName = new IdPojoAndName();
        idPojoAndName.setId(category1.getId());
        idPojoAndName.setName("cat1 clone");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", category1.getId())
                .body(idPojoAndName)
                .post("MenuCategory/clone/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Menu testMenu = menuRepository.findOne(menu1.getId());
        assertNotNull(testMenu);
        assertEquals(2, testMenu.getCategories().size());
        Category testCat = testMenu.getCategories().get(1);
        assertCategoriesEqual(testCat, idPojoAndName.getName(), true);
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
        menu2.getCategories().clear();
        menu2.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu2);
    }

    @Test
    public void testPostCloneCategoryToOtherMenu() throws Exception {
        setUpMenu1();
        setUpMenu2();

        String token = getTokenForStaff(staff1);
        CategoryCloneView categoryCloneView = new CategoryCloneView();
        categoryCloneView.setId(category1.getId());
        categoryCloneView.setName("cat1 clone");
        categoryCloneView.setMenuId(menu2.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", category1.getId())
                .body(categoryCloneView)
                .post("MenuCategory/clone/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Menu testMenu1 = menuRepository.findOne(menu1.getId());
        assertNotNull(testMenu1);
        assertEquals(menu1, testMenu1);
        Menu testMenu2 = menuRepository.findOne(menu2.getId());
        assertEquals(1, testMenu2.getCategories().size());
        Category testCat = testMenu2.getCategories().get(0);
        assertCategoriesEqual(testCat, categoryCloneView.getName(), false);
    }

    private void assertCategoriesEqual(Category testCat, String name, boolean testOrder) {
        assertEquals(name, testCat.getName());
        assertEquals(category1.getCourseIds(), testCat.getCourseIds());
        assertEquals(category1.getImageURL(), testCat.getImageURL());
        if(testOrder) {
            assertTrue(category1.getOrder() < testCat.getOrder());
        }
        assertEquals(category1.getGroups().size(), testCat.getGroups().size());
        Group testGroup = testCat.getGroups().get(0);
        assertEquals(group1.getName(), testGroup.getName());
        assertEquals(group1.getItems(), testGroup.getItems());
        assertEquals(group1.getOrder(), testGroup.getOrder());
    }
}