package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.common.IdList;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupView;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.PreferenceType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class MenuItemControllerTest extends MenuBaseIT {

    private List<MenuItem> items = new ArrayList<>();
    private Group group4;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        //add existing items to other category
        GroupView groupView = new GroupView();
        groupView.setGroupName("group 4");
        groupView.setMenuCategoryId(category2.getId());
        group4 = new Group(category2, groupView);
        group4.getItems().add(menuItem1.getId());

        category2.getGroups().add(group4);
        menuRepository.save(menu2);

        items.clear();
        items.add(menuItem1);
        items.add(menuItem2);
        items.add(menuItem3);
    }

    @Test
    public void testGetMenuItems() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("MenuItem");

        List<MenuItemView> menuItemViews = Arrays.asList(response.getBody().as(MenuItemView[].class, ObjectMapperType.JACKSON_2));
        for(MenuItemView menuItemView : menuItemViews) {
            testEquality(items.stream().filter(m -> m.getId().equals(menuItemView.getId())).findFirst().get(), menuItemView);
        }

        //test orphaned
        group1.getItems().remove(menuItem2.getId());
        group2.getItems().remove(menuItem2.getId());
        group3.getItems().remove(menuItem2.getId());
        group4.getItems().remove(menuItem2.getId());
        menuRepository.save(menu2);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("orphaned", true)
                .get("MenuItem");

        menuItemViews = Arrays.asList(response.getBody().as(MenuItemView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, menuItemViews.size());
        testEquality(menuItem2, menuItemViews.get(0));
    }

    private void testEquality(MenuItem item, MenuItemView view) {
        assertEquals(item.getId(), view.getId());
        assertEquals(item.getName(), view.getName());
        assertEquals(item.getType().getId(), view.getMenuItemTypeId());
        assertEquals(item.getPrice()/100D, view.getPrice(), 0.001);
        assertEquals(item.getImageURL(), view.getImageUrl());
        assertEquals(item.getTaxTypeId(), view.getTaxTypeId());
        assertEquals(item.getModifierGroupIds().stream().sorted().collect(Collectors.toList()), view.getModifierGroups().stream().sorted().collect(Collectors.toList()));
        assertEquals(item.getDefaultPrinter(), view.getDefaultPrinter());
        assertEquals(item.isUnavailable(), view.isUnavailable());
        assertEquals(item.getDescription(), view.getDescription());
        if(item.getAllergyIds() != null && view.getAllergyIds() != null) {
            Collections.sort(item.getAllergyIds());
            Collections.sort(view.getAllergyIds());
            assertEquals(item.getAllergyIds(), view.getAllergyIds());
        }
        if(item.getDietaryIds() != null && view.getDietaryIds() != null) {
            Collections.sort(item.getDietaryIds());
            Collections.sort(view.getDietaryIds());
            assertEquals(item.getDietaryIds(), view.getDietaryIds());
        }
    }

    @Test
    public void testPostMenuItem() throws Exception {
        String token = getTokenForStaff(staff1);

        MenuItemView view = new MenuItemView(menuItem3, 0);
        view.setId(null);
        view.setName("Some new name");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .post("MenuItem");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        MenuItemView menuItemView = response.getBody().as(MenuItemView.class, ObjectMapperType.JACKSON_2);
        MenuItem newMenuItem = menuItemRepository.findOne(menuItemView.getId());
        testEquality(newMenuItem, menuItemView);
        view.setId(menuItemView.getId());
        assertEquals(view, menuItemView);

        view.setId(null);
        view.setMenuGroups(new ArrayList<>());
        view.getMenuGroups().add(group1.getId());
        view.getMenuGroups().add(group2.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .post("MenuItem");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        menuItemView = response.getBody().as(MenuItemView.class, ObjectMapperType.JACKSON_2);
        newMenuItem = menuItemRepository.findOne(menuItemView.getId());
        testEquality(newMenuItem, menuItemView);
        view.setId(menuItemView.getId());
        assertEquals(view, menuItemView);
        menu2 = menuRepository.findOne(this.menu2.getId());
        assertTrue(menu2.getCategories().get(0).getGroups().get(0).getItems().contains(menuItemView.getId()));
        assertEquals(4, menu2.getCategories().get(0).getGroups().get(0).getItems().size());

        view.setId(null);
        view.getMenuGroups().add(group1.getId());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .post("MenuItem");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        menuItemView = response.getBody().as(MenuItemView.class, ObjectMapperType.JACKSON_2);
        newMenuItem = menuItemRepository.findOne(menuItemView.getId());
        testEquality(newMenuItem, menuItemView);
        view.setId(menuItemView.getId());
        assertEquals(view, menuItemView);
        menu2 = menuRepository.findOne(this.menu2.getId());
        assertTrue(menu2.getCategories().get(0).getGroups().get(0).getItems().contains(menuItemView.getId()));
        assertEquals(5, menu2.getCategories().get(0).getGroups().get(0).getItems().size());
    }

    @Test
    public void testPostMenuItemDuplicateShortCode() throws Exception {
        String token = getTokenForStaff(staff1);

        String shortCode = "foobar";
        menuItem3.setShortCode(shortCode);
        menuItemRepository.save(menuItem3);

        MenuItemView view = new MenuItemView(menuItem3, 0);
        view.setId(null);
        view.setName("Some new name");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .post("MenuItem");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPutMenuItem() throws Exception {
        String token = getTokenForStaff(staff1);

        MenuItemView view = new MenuItemView(menuItem3, 0);
        setUpView(view, menuItem3);
        updateItemsOnView(view);


        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .pathParam("id", menuItem3.getId())
                .put("MenuItem/{id}");

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode());

        MenuItem newMenuItem = menuItemRepository.findOne(menuItem3.getId());
        testEquality(newMenuItem, view);

        view.setDefaultPrinter("non existent printer");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .pathParam("id", menuItem3.getId())
                .put("MenuItem/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        view.setDefaultPrinter(printer2.getId());
        view.setTaxTypeId("super tax");

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .pathParam("id", menuItem3.getId())
                .put("MenuItem/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .pathParam("id", "a bad id")
                .put("MenuItem/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    private void updateItemsOnView(MenuItemView view) {
        Preference preference1 = new Preference();
        preference1.setName("foo");
        preference1.setPreferenceType(PreferenceType.ALLERGY);
        view.getAllergyIds().add(preferencesRepository.insert(preference1).getId());
        Preference preference2 = new Preference();
        preference2.setName("foo");
        preference2.setPreferenceType(PreferenceType.DIETARY);
        view.getDietaryIds().add(preferencesRepository.insert(preference2).getId());
    }

    private void setUpView(MenuItemView view, MenuItem menuItem) {
        view.setId(menuItem.getId());
        view.setName("Some new name");
        view.setDefaultPrinter(printer2.getId());
        view.setTaxTypeId(tax3.getId());
        view.setImageUrl("foobar");
        List<String> intendedMenuGroups = new ArrayList<>();
        intendedMenuGroups.add(group4.getId());
        intendedMenuGroups.add(group2.getId());
        view.setMenuGroups(intendedMenuGroups);
        List<String> intendedModifierGroups = new ArrayList<>();
        intendedModifierGroups.add(modifierGroup1.getId());
        view.setModifierGroups(intendedModifierGroups);
        view.setPrice(4000D);
        view.setTypeName(ItemType.DRINK.getName());
        view.setMenuItemTypeId(ItemType.DRINK.getId());
    }

    @Test
    public void testPutMultipleMenuItem() throws Exception {
        String token = getTokenForStaff(staff1);

        menuItem2.setRestaurantId(staff1.getRestaurantId());
        menuItem3.setRestaurantId(staff1.getRestaurantId());
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        MenuItemView view2 = new MenuItemView(menuItem2, 0);
        setUpView(view2, menuItem2);
        MenuItemView view3 = new MenuItemView(menuItem3, 0);
        setUpView(view3, menuItem3);

        updateItemsOnView(view2);
        updateItemsOnView(view3);

        List<MenuItemView> list = new ArrayList<>();
        list.add(view2);
        list.add(view3);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(list)
                .put("MenuItem/multiple");

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode());

        testEquality(menuItemRepository.findOne(menuItem3.getId()), view3);
        testEquality(menuItemRepository.findOne(menuItem2.getId()), view2);
        assertEquals(menuItemRepository.findOne(menuItem1.getId()), menuItem1);
    }

    @Test
    public void testPutMenuItemDuplicateShortCode() throws Exception {
        String token = getTokenForStaff(staff1);

        String shortCode = "foobar";
        menuItem2.setShortCode(shortCode);
        menuItemRepository.save(menuItem2);

        MenuItemView view = new MenuItemView(menuItem3, 0);
        setUpView(view, menuItem3);
        view.setShortCode(shortCode);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(view)
                .pathParam("id", menuItem3.getId())
                .put("MenuItem/{id}");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testDeleteMenuItem() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menuItem1.getId())
                .delete("MenuItem/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<Menu> menus = menuRepository.findByRestaurantId(restaurant1.getId());
        for(Menu menu : menus) {
            for(Category category : menu.getCategories()) {
                for(Group group : category.getGroups()) {
                    assertFalse(group.getItems().contains(menuItem1.getId()));
                }
            }
        }

        assertNotNull(menuItemRepository.findOne(menuItem1.getId()).getDeleted());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", menuItem1.getId())
                .delete("MenuItem/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "a bad id")
                .delete("MenuItem/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testDeleteMultipleMenuItem() throws Exception {
        group4.getItems().add(menuItem2.getId());
        menuRepository.save(menu2);

        String token = getTokenForStaff(staff1);
        List<String> ids = new ArrayList<>();
        ids.add(menuItem1.getId());
        ids.add(menuItem2.getId());
        IdList idList = new IdList();
        idList.setIds(ids);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(idList)
                .post("MenuItem/multiple");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<Menu> menus = menuRepository.findByRestaurantId(restaurant1.getId());
        for(Menu menu : menus) {
            for(Category category : menu.getCategories()) {
                for(Group group : category.getGroups()) {
                    assertFalse(group.getItems().contains(menuItem1.getId()));
                    assertFalse(group.getItems().contains(menuItem2.getId()));
                }
            }
        }

        assertNotNull(menuItemRepository.findOne(menuItem1.getId()).getDeleted());
        assertNotNull(menuItemRepository.findOne(menuItem2.getId()).getDeleted());
        assertNull(menuItemRepository.findOne(menuItem3.getId()).getDeleted());
    }
}