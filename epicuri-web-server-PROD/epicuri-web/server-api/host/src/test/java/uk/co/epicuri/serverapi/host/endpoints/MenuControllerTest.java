package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;
import uk.co.epicuri.serverapi.common.pojo.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class MenuControllerTest extends MenuBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetMenus() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        MenuView[] menuViews = response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2);
        List<MenuView> menuViewsList = Arrays.asList(menuViews);
        assertTrue(menuViewsList.stream().allMatch(MenuView::isActive));

        testMenuView(menuViewsList);

        menu2.setActive(false);
        menuRepository.save(menu2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu");
        menuViews = response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(0, menuViews.length);

        menu2.setActive(true);
        menu2.setDeleted(System.currentTimeMillis());
        menuRepository.save(menu2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu");
        menuViews = response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(0, menuViews.length);
    }

    @Test
    public void testGetAllMenus() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu/All");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        MenuView[] menuViews = response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2);
        List<MenuView> menuViewsList = Arrays.asList(menuViews);

        testMenuView(menuViewsList);

        menu2.setActive(false);
        menuRepository.save(menu2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu/All");
        menuViews = response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(1, menuViews.length);

        menu2.setActive(true);
        menu2.setDeleted(System.currentTimeMillis());
        menuRepository.save(menu2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu/All");
        menuViews = response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(0, menuViews.length);
    }

    @Test
    public void testGetMenusOrdering() throws Exception {
        menu1.setRestaurantId(restaurant1.getId());
        menu1.setActive(true);
        menuRepository.save(menu1);
        menu2.setRestaurantId(restaurant1.getId());
        menu2.setActive(true);
        menuRepository.save(menu2);
        menu3.setRestaurantId(restaurant1.getId());
        menu3.setActive(true);
        menuRepository.save(menu3);

        List<String> naturalOrder = menuRepository.findByRestaurantId(restaurant1.getId()).stream().map(Menu::getId).collect(Collectors.toList());

        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu/All");
        List<MenuView> menuViewsList = Arrays.asList(response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(naturalOrder, menuViewsList.stream().map(MenuView::getId).collect(Collectors.toList()));

        menu3.setOrder(0);
        menuRepository.save(menu3);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu/All");
        menuViewsList = Arrays.asList(response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, menuViewsList.size());
        assertEquals(menu3.getId(), menuViewsList.get(0).getId());
        assertEquals(menu1.getId(), menuViewsList.get(1).getId());
        assertEquals(menu2.getId(), menuViewsList.get(2).getId());

        menu2.setOrder(1);
        menuRepository.save(menu2);
        menu1.setOrder(2);
        menuRepository.save(menu1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Menu/All");
        menuViewsList = Arrays.asList(response.getBody().as(MenuView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, menuViewsList.size());
        assertEquals(menu3.getId(), menuViewsList.get(0).getId());
        assertEquals(menu2.getId(), menuViewsList.get(1).getId());
        assertEquals(menu1.getId(), menuViewsList.get(2).getId());
    }

    private void testMenuView(List<MenuView> menuViewsList) throws Exception {
        assertEquals(1, menuViewsList.size());

        MenuView menuView2 = menuViewsList.stream().filter(m -> m.getId().equals(menu2.getId())).findFirst().get();
        assertEquals(menu2.getName(), menuView2.getMenuName());
        assertEquals(menu2.getRestaurantId(), menuView2.getRestaurantId());
        assertEquals(1, menuView2.getMenuModifierGroups().size());
        CustomerModifierGroupView modifierGroupView = menuView2.getMenuModifierGroups().get(0);
        assertEquals(modifierGroup1.getId(), modifierGroupView.getId());
        assertEquals(modifierGroup1.getName(), modifierGroupView.getGroupName());
        assertEquals(modifierGroup1.getLowerLimit(), modifierGroupView.getLowerLimit());
        assertEquals(modifierGroup1.getUpperLimit(), modifierGroupView.getUpperLimit());
        assertEquals(3, modifierGroupView.getModifiers().size());
        for(ModifierView modifierView : modifierGroupView.getModifiers()) {
            Modifier testModifier = findModifier(modifierView.getId());
            assertNotNull(testModifier);
            assertEquals(testModifier.getModifierValue(), modifierView.getModifierValue());
            assertEquals(testModifier.getPrice(), (int)(modifierView.getPrice() * 100));
            assertEquals(testModifier.getTaxTypeId(), modifierView.getTaxTypeId());
            assertEquals(modifierGroup1.getId(), modifierView.getModifierGroupId());
        }

        //check category
        assertEquals(3, menuView2.getMenuCategories().size());
        for(CategoryView categoryView : menuView2.getMenuCategories()) {
            Category testCategory = findCategory(categoryView.getId());
            assertNotNull(testCategory);
            assertEquals(testCategory.getName(), categoryView.getCategoryName());
            assertEquals(testCategory.getOrder(), categoryView.getOrder());
            assertEquals(testCategory.getCourseIds(), categoryView.getDefaultCourseIds());
            List<CourseView> courses = categoryView.getDefaultCourses();
            CourseView courseView1 = courses.stream().filter(c -> c.getId().equals(course1.getId())).findFirst().orElse(new CourseView());
            CourseView courseView2 = courses.stream().filter(c -> c.getId().equals(course2.getId())).findFirst().orElse(new CourseView());
            if(testCategory.getId().equals(category1.getId())) {
                assertEquals(course1.getId(), courseView1.getId());
                assertEquals(course1.getName(), courseView1.getName());
                assertEquals(service1.getId(), courseView1.getServiceId());
                assertEquals(course1.getOrdering(), courseView1.getOrdering().shortValue());

                assertEquals(course2.getId(), courseView2.getId());
                assertEquals(course2.getName(), courseView2.getName());
                assertEquals(service1.getId(), courseView2.getServiceId());
                assertEquals(course2.getOrdering(), courseView2.getOrdering().shortValue());

                //check groups
                assertEquals(3, categoryView.getMenuGroups().size());
                assertEquals(3, categoryView.getMenuGroupsIds().size());
                assertTrue(categoryView.getMenuGroupsIds().contains(group1.getId()));
                assertTrue(categoryView.getMenuGroupsIds().contains(group2.getId()));
                assertTrue(categoryView.getMenuGroupsIds().contains(group3.getId()));
                GroupView groupView1 = categoryView.getMenuGroups().stream().filter(g -> g.getId().equals(group1.getId())).findFirst().orElse(new GroupView());
                assertEquals(group1.getName(), groupView1.getGroupName());
                assertEquals(category1.getId(), groupView1.getMenuCategoryId());

                //check menu items
                assertTrue(groupView1.getMenuItemIds().contains(menuItem1.getId()));
                assertTrue(groupView1.getMenuItemIds().contains(menuItem2.getId()));
                assertTrue(groupView1.getMenuItemIds().contains(menuItem3.getId()));
                MenuItemView menuItemView1 = groupView1.getMenuItems().stream().filter(m -> m.getId().equals(menuItem1.getId())).findFirst().orElse(new MenuItemView());
                CourseView mCourseView1 = menuItemView1.getDefaultCourses().stream().filter(c -> c.getId().equals(course1.getId())).findFirst().orElse(new CourseView());
                CourseView mCourseView2 = menuItemView1.getDefaultCourses().stream().filter(c -> c.getId().equals(course2.getId())).findFirst().orElse(new CourseView());
                assertEquals(course1.getId(), mCourseView1.getId());
                assertEquals(course1.getName(), mCourseView1.getName());
                assertEquals(service1.getId(), mCourseView1.getServiceId());
                assertEquals(course1.getOrdering(), mCourseView1.getOrdering().shortValue());
                assertEquals(course2.getId(), mCourseView2.getId());
                assertEquals(course2.getName(), mCourseView2.getName());
                assertEquals(service1.getId(), mCourseView2.getServiceId());
                assertEquals(course2.getOrdering(), mCourseView2.getOrdering().shortValue());

                assertEquals(menuItem1.getDefaultPrinter(), menuItemView1.getDefaultPrinter());
                assertEquals(menuItem1.getDescription(), menuItemView1.getDescription());
                assertEquals(menuItem1.getImageURL(), menuItemView1.getImageUrl());
                assertEquals(menuItem1.getName(), menuItemView1.getName());
                assertEquals(service1.getId(), menuItemView1.getServiceId());
                assertEquals(menuItem1.getTaxTypeId(), menuItemView1.getTaxTypeId());
                assertEquals(menuItem1.getType().getName(), menuItemView1.getTypeName());
                assertEquals(modifierGroup1.getId(), menuItemView1.getModifierGroups().get(0));

                MenuItemView menuItemView2 = groupView1.getMenuItems().stream().filter(m -> m.getId().equals(menuItem2.getId())).findFirst().orElse(new MenuItemView());
                assertEquals(menuItem2.getDefaultPrinter(), menuItemView2.getDefaultPrinter());
                assertEquals(menuItem2.getDescription(), menuItemView2.getDescription());
                assertEquals(menuItem2.getImageURL(), menuItemView2.getImageUrl());
                assertEquals(menuItem2.getName(), menuItemView2.getName());
                assertEquals(service1.getId(), menuItemView2.getServiceId());
                assertEquals(menuItem2.getTaxTypeId(), menuItemView2.getTaxTypeId());
                assertEquals(menuItem2.getType().getName(), menuItemView2.getTypeName());
                assertEquals(0, menuItemView2.getModifierGroups().size());

                MenuItemView menuItemView = groupView1.getMenuItems().stream().filter(m -> m.getId().equals(menuItem3.getId())).findFirst().orElse(new MenuItemView());
                assertEquals(menuItem3.getDefaultPrinter(), menuItemView.getDefaultPrinter());
                assertEquals(menuItem3.getDescription(), menuItemView.getDescription());
                assertEquals(menuItem3.getImageURL(), menuItemView.getImageUrl());
                assertEquals(menuItem3.getName(), menuItemView.getName());
                assertEquals(service1.getId(), menuItemView.getServiceId());
                assertEquals(menuItem3.getTaxTypeId(), menuItemView.getTaxTypeId());
                assertEquals(menuItem3.getType().getName(), menuItemView.getTypeName());
                assertEquals(0, menuItemView.getModifierGroups().size());

            } else {
                assertEquals(course3.getId(), courses.get(0).getId());
                assertEquals(course3.getName(), courses.get(0).getName());
                assertEquals(service1.getId(), courses.get(0).getServiceId());
                assertEquals(course3.getOrdering(), courses.get(0).getOrdering().shortValue());

                //check groups
                assertEquals(0, categoryView.getMenuGroups().size());
                assertEquals(0, categoryView.getMenuGroupsIds().size());
            }
        }
    }


    @SuppressWarnings("Duplicates")
    private Modifier findModifier(String id) {
        if(id.equals(modifier1.getId())) {
            return modifier1;
        } else if(id.equals(modifier2.getId())) {
            return modifier2;
        } else if(id.equals(modifier3.getId())) {
            return modifier3;
        }

        return null;
    }

    @SuppressWarnings("Duplicates")
    private Category findCategory(String id) {
        if(id.equals(category1.getId())) {
            return category1;
        } else if(id.equals(category2.getId())) {
            return category2;
        } else if(id.equals(category3.getId())) {
            return category3;
        }

        return null;
    }

    @Test
    public void testGetMenu() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .get("Menu/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        MenuView menuView = response.getBody().as(MenuView.class, ObjectMapperType.JACKSON_2);
        List<MenuView> menuViewsList = new ArrayList<>();
        menuViewsList.add(menuView);

        testMenuView(menuViewsList);

        menu2.setActive(false);
        menuRepository.save(menu2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .get("Menu/{id}");
        menuView = response.getBody().as(MenuView.class, ObjectMapperType.JACKSON_2);
        menuViewsList = new ArrayList<>();
        menuViewsList.add(menuView);

        testMenuView(menuViewsList);

        menu2.setActive(true);
        menu2.setDeleted(System.currentTimeMillis());
        menuRepository.save(menu2);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .get("Menu/{id}");
        menuView = response.getBody().as(MenuView.class, ObjectMapperType.JACKSON_2);
        //this endpoint deliberately does not check for null
        menuViewsList = new ArrayList<>();
        menuViewsList.add(menuView);

        testMenuView(menuViewsList);
    }

    @Test
    public void testPostMenu() throws Exception {
        MenuView menuView = new MenuView(menu2, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        menuView.setMenuName("a new menu");

        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(menuView)
                .post("Menu");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        String newId = response.getHeader("Location").split("/")[1];

        MenuView menuViewResponse = response.getBody().as(MenuView.class, ObjectMapperType.JACKSON_2);
        Menu created = menuRepository.findOne(newId);
        assertEquals(menuView.getMenuName(), menuViewResponse.getMenuName());
        assertEquals(menuView.getMenuName(), created.getName());
        assertTrue(created.isActive());
        assertTrue(created.getLastUpdate() > (System.currentTimeMillis()-1000));

        menuView.setActive(false);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(menuView)
                .post("Menu");

        newId = response.getHeader("Location").split("/")[1];

        menuViewResponse = response.getBody().as(MenuView.class, ObjectMapperType.JACKSON_2);
        created = menuRepository.findOne(newId);
        assertEquals(menuView.getMenuName(), menuViewResponse.getMenuName());
        assertEquals(menuView.getMenuName(), created.getName());
        assertFalse(created.isActive());
        assertTrue(created.getLastUpdate() > (System.currentTimeMillis()-1000));
    }

    @Test
    public void testDeleteMenu() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .delete("Menu/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(menuRepository.findOne(menu2.getId()).getDeleted());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu1.getId())
                .delete("Menu/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertNull(menuRepository.findOne(menu1.getId()).getDeleted());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .delete("Menu/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testPutMenu() throws Exception {
        String token = getTokenForStaff(staff1);

        MenuView menuView = new MenuView();
        menuView.setMenuName("a changed Name");
        menuView.setActive(true);

        long currentUpdatedTime = menu2.getLastUpdate();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .body(menuView)
                .put("Menu/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Menu modified = menuRepository.findOne(menu2.getId());
        assertEquals(menuView.getMenuName(), modified.getName());
        assertTrue(modified.getLastUpdate() > currentUpdatedTime);

        currentUpdatedTime = modified.getLastUpdate();

        menuView.setActive(false);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .body(menuView)
                .put("Menu/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        modified = menuRepository.findOne(menu2.getId());
        assertEquals(menuView.getMenuName(), modified.getName());
        assertFalse(modified.isActive());
        assertTrue(modified.getLastUpdate() > currentUpdatedTime);

        menuView.setMenuName(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .body(menuView)
                .put("Menu/{id}");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPutMenu1() throws Exception {
        String token = getTokenForStaff(staff1);


        assert(!menu2.getId().equals(restaurant1.getTakeawayMenu()));
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu2.getId())
                .put("Menu/ChangeTakeawayMenu/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Restaurant modified = restaurantRepository.findOne(restaurant1.getId());
        assertEquals(menu2.getId(), modified.getTakeawayMenu());
    }

    @Test
    public void testPostCloneMenu() throws Exception {
        category1.setId(IDAble.generateId(menu1.getId()));
        group1.setId(IDAble.generateId(category1.getId()));
        group1.setName("ketchup");
        menu1.getCategories().clear();
        menu1.getCategories().add(category1);
        category1.getGroups().clear();
        category1.getGroups().add(group1);
        menu1.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);

        String token = getTokenForStaff(staff1);
        IdPojoAndName idPojoAndName = new IdPojoAndName();
        idPojoAndName.setId(menu1.getId());
        idPojoAndName.setName("menu clone");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menu1.getId())
                .body(idPojoAndName)
                .post("Menu/clone/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertNotNull(menuRepository.findOne(menu1.getId()));
        List<Menu> menus = menuRepository.findByRestaurantId(restaurant1.getId());
        Menu test = menus.stream().filter(m -> m.getName().equals("menu clone")).findFirst().orElse(null);
        assertNotNull(test);
        assertNotEquals(menu1.getId(), test.getId());
        assertEquals(1, test.getCategories().size());
        Category testCat = test.getCategories().get(0);
        assertEquals(category1.getName(), testCat.getName());
        assertEquals(category1.getCourseIds(), testCat.getCourseIds());
        assertNotEquals(category1.getId(), testCat.getId());
        assertEquals(category1.getGroups().size(), testCat.getGroups().size());
        Group testGroup = testCat.getGroups().get(0);
        assertEquals(group1.getName(), testGroup.getName());
        assertEquals(group1.getItems(), testGroup.getItems());
        assertNotEquals(group1.getId(), testGroup.getId());
    }
}