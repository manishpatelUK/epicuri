package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.menu.ModifierView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class ModifierControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);
    }

    @Test
    public void testPostModifier() throws Exception {
        String token = getTokenForStaff(staff1);

        ModifierView payload = new ModifierView();
        payload.setModifierGroupId(modifierGroup1.getId());
        payload.setPrice(1);
        payload.setModifierValue("SomeValue");
        payload.setTaxTypeId(tax2.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Modifier");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        ModifierView modifierView = response.getBody().as(ModifierView.class, ObjectMapperType.JACKSON_2);

        assertNotNull(modifierView.getId());
        Modifier saved = modifierRepository.findOne(modifierView.getId());
        assertNotNull(saved);
        assertEquals(payload.getPrice()*100, saved.getPrice(), 0.001);
        assertEquals(payload.getModifierValue(), saved.getModifierValue());
        assertEquals(payload.getTaxTypeId(), saved.getTaxTypeId());

        ModifierGroup group = modifierGroupRepository.findOne(modifierGroup1.getId());
        assertTrue(group.getModifiers().stream().anyMatch(m -> m.getId().equals(saved.getId())));

        //try non existing modifier group
        payload.setModifierGroupId("foobar");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Modifier");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testPutModifier() throws Exception {
        String token = getTokenForStaff(staff1);

        ModifierView payload = new ModifierView(modifier1, modifierGroup1);
        payload.setModifierValue("new value");
        payload.setPrice(2);
        payload.setTaxTypeId(tax3.getId());

        Response response =given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .pathParam("id", payload.getId())
                .put("Modifier/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Modifier saved = modifierRepository.findOne(payload.getId());
        assertNotNull(saved);
        assertEquals(payload.getPrice()*100, saved.getPrice(), 0.001);
        assertEquals(payload.getModifierValue(), saved.getModifierValue());
        assertEquals(payload.getTaxTypeId(), saved.getTaxTypeId());
    }

    @Test
    public void testDeleteModifier() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response =given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", modifier1.getId())
                .delete("Modifier/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Modifier saved = modifierRepository.findOne(modifier1.getId());
        assertNotNull(saved.getDeleted());

        ModifierGroup group = modifierGroupRepository.findOne(modifierGroup1.getId());
        assertFalse(group.getModifiers().stream().allMatch(m ->m.getId().equals(modifier1.getId())));
    }
}