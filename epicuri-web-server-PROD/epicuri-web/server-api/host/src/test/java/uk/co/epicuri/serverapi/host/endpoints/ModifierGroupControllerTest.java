package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierGroupView;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class ModifierGroupControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        modifierGroup1.getModifiers().add(modifier1);
        modifierGroupRepository.save(modifierGroup1);
    }

    @Test
    public void testGetModifierGroups() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("ModifierGroup");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<ModifierGroupView> views = Arrays.asList(response.getBody().as(ModifierGroupView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(1, views.size());
        testEqual(modifierGroup1, views.get(0));

        modifierGroup1.setDeleted(0L);
        modifierGroupRepository.save(modifierGroup1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("ModifierGroup");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        views = Arrays.asList(response.getBody().as(ModifierGroupView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(0, views.size());

    }

    private void testEqual(ModifierGroup group, ModifierGroupView view) {
        assertEquals(group.getId(), view.getId());
        assertEquals(group.getName(), view.getName());
        assertEquals(group.getLowerLimit(), view.getLowerLimit());
        assertEquals(group.getUpperLimit(), view.getUpperLimit());
        assertEquals(group.getModifiers().size(), view.getModifiers().size());
        for(Modifier modifier : group.getModifiers()) {
            testEqual(modifier, view.getModifiers().stream().filter(m -> m.getId().equals(modifier.getId())).findFirst().orElse(null));
        }
    }

    private void testEqual(Modifier modifier, ModifierView modifierView) {
        assertEquals(modifier.getModifierValue(), modifierView.getModifierValue());
        assertEquals(modifier.getPrice(), modifierView.getPrice()*100D, 0.001);
        assertEquals(modifier.getTaxTypeId(), modifierView.getTaxTypeId());
    }

    @Test
    public void testGetModifierGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .get("ModifierGroup/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", modifierGroup1.getId())
                .get("ModifierGroup/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        ModifierGroupView view = response.getBody().as(ModifierGroupView.class, ObjectMapperType.JACKSON_2);

        testEqual(modifierGroup1, view);
    }

    @Test
    public void testPostModifierGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        ModifierGroupView groupView = new ModifierGroupView();
        groupView.setId("foobar");
        groupView.setLowerLimit(10);
        groupView.setUpperLimit(30);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .post("ModifierGroup");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        ModifierGroupView view = response.getBody().as(ModifierGroupView.class, ObjectMapperType.JACKSON_2);

        groupView.setId(view.getId());
        ModifierGroup modifierGroup = modifierGroupRepository.findOne(view.getId());
        testEqual(modifierGroup, groupView);
        testEqual(modifierGroup, view);
        assertEquals(staff1.getRestaurantId(), modifierGroup.getRestaurantId());
    }

    @Test
    public void testPutModifierGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        ModifierGroupView groupView = new ModifierGroupView(modifierGroup1);
        groupView.setName("foobar");
        groupView.setLowerLimit(10);
        groupView.setUpperLimit(30);

        int count = modifierGroupRepository.findAll().size();
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(groupView)
                .pathParam("id", groupView.getId())
                .put("ModifierGroup/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        ModifierGroupView view = response.getBody().as(ModifierGroupView.class, ObjectMapperType.JACKSON_2);

        testEqual(modifierGroupRepository.findOne(modifierGroup1.getId()), groupView);
        testEqual(modifierGroupRepository.findOne(modifierGroup1.getId()), view);
        assertEquals(count, modifierGroupRepository.findAll().size());
    }

    @Test
    public void testDeleteModifierGroup() throws Exception {
        String token = getTokenForStaff(staff1);

        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.getModifierGroupIds().add(modifierGroup1.getId());
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem2.getModifierGroupIds().add(modifierGroup1.getId());
        menuItem3.setRestaurantId(restaurant1.getId());
        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);


        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", modifierGroup1.getId())
                .delete("ModifierGroup/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        modifierGroup1 = modifierGroupRepository.findOne(modifierGroup1.getId());
        assertNotNull(modifierGroup1.getDeleted());

        assertEquals(0, menuItemRepository.findOne(menuItem1.getId()).getModifierGroupIds().size());
        assertEquals(0, menuItemRepository.findOne(menuItem2.getId()).getModifierGroupIds().size());
        assertEquals(0, menuItemRepository.findOne(menuItem3.getId()).getModifierGroupIds().size());
    }
}