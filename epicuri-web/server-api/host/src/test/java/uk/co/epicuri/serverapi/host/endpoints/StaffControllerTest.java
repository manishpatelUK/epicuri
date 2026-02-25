package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtil;
import uk.co.epicuri.serverapi.common.pojo.host.StaffPermissionView;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.host.WaiterAppFeatureView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class StaffControllerTest extends BaseIT {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.WAIT_STAFF);
        staff1.setPin("1234");
        staff1.setUserName("username1");
        staff2.setRestaurantId(restaurant3.getId());
        staff2.setRole(StaffRole.MANAGER);
        staff2.setPin("1234");
        staff2.setUserName("username2");
        staff3.setRestaurantId(restaurant3.getId());
        staff3.setUserName("username3");
        staff3.setRole(StaffRole.SITE_OWNER);
        staffRepository.save(staff1);
        staffRepository.save(staff2);
        staffRepository.save(staff3);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetStaff() throws Exception {
        String token = getTokenForStaff(staff1);
        StaffView staffView;

        staffView = authenticationService.staffLogin(staff2);
        token = staffView.getAuthKey();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Staff");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        StaffView[] entity  = response.getBody().as(StaffView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(2, entity.length);
        for(StaffView view : entity) {
            assertTrue(view.getId().equals(staff2.getId()) || view.getId().equals(staff3.getId()));
            assertFalse(view.getId().equals(staff1.getId()));
        }
    }

    @Test
    public void testPostStaff() throws Exception {
        String token = getTokenForStaff(staff2);

        StaffView newStaff = createRandomStaffView();

        Response response = addStaff(token, newStaff);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        String newId = response.getHeader("Location").split("/")[2];

        Staff staff = staffRepository.findOne(newId);
        assertEquals(newStaff.getName(), staff.getName());
        assertEquals(newStaff.isManager(), staff.getRole().equals(StaffRole.MANAGER));
        String expectedMash = AuthenticationUtil.getPasswordMash(staff, newStaff.getPassword());
        assertEquals(expectedMash, staff.getMash());
        assertEquals(newStaff.getPin(), staff.getPin());
        assertEquals(newStaff.getUsername(), staff.getUserName());

        response = addStaff(token, newStaff);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        newStaff.setUsername(null);

        response = addStaff(token, newStaff);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    private Response addStaff(String token, StaffView newStaff) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(newStaff)
                .post("Staff");
    }

    private Response putStaff(String token, StaffView newStaff) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(newStaff)
                .put("Staff/" + newStaff.getId());
    }

    private StaffView createRandomStaffView() {
        StaffView newStaff = new StaffView();
        newStaff.setName("Foo Man");
        newStaff.setManager(false);
        newStaff.setPassword("foobar");
        newStaff.setPin("1234");
        newStaff.setUsername("fooman");
        return newStaff;
    }

    @Test
    public void testPutStaff() throws Exception {
        String token = getTokenForStaff(staff2);
        StaffView newStaff = createRandomStaffView();
        Response response = addStaff(token, newStaff);
        String newId = response.getHeader("Location").split("/")[2];

        StaffView newStaffView = new StaffView(staffRepository.findOne(newId));
        assertTrue(StringUtils.isNotBlank(newStaffView.getId()));
        assertEquals(newId, newStaffView.getId());

        testEditStaff(token, newId, newStaffView, false);

        //try above on SELF
        setUp();
        token = getTokenForStaff(staff2);
        newStaffView = new StaffView(staffRepository.findOne(staff2.getId()));
        testEditStaff(token, staff2.getId(), newStaffView, true);
    }

    private void testEditStaff(String token, String staffId, StaffView staffView, boolean expectManagerUpgradeError) throws Exception {
        //change name
        String name = "a new name";
        staffView.setName(name);
        putStaff(token, staffView);
        assertEquals(name, staffRepository.findOne(staffId).getName());

        //change username
        String username = "a new user name";
        staffView.setUsername(username);
        putStaff(token, staffView);
        assertEquals(username, staffRepository.findOne(staffId).getUserName());
        //expect an error if changes to existing
        String existingUserName = staff3.getUserName();
        staffView.setUsername(existingUserName);
        Response response = putStaff(token, staffView);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        //reset
        staffView.setUsername(username);

        //change to manager
        if(expectManagerUpgradeError) {
            staffView.setRole(StaffRole.SITE_OWNER.toString());
            response = putStaff(token, staffView);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            assertEquals(StaffRole.MANAGER, staffRepository.findOne(staffId).getRole());
        } else {
            staffView.setManager(true);
            staffView.setRole(StaffRole.MANAGER.toString());
            putStaff(token, staffView);
            assertEquals(StaffRole.MANAGER, staffRepository.findOne(staffId).getRole());
        }

        //reset
        staffView.setRole(StaffRole.MANAGER.toString());

        //change pin
        String pin = "5432";
        staffView.setPin(pin);
        putStaff(token, staffView);
        assertEquals(pin, staffRepository.findOne(staffId).getPin());

        //change password
        String expectedMash = staffRepository.findOne(staffId).getMash();
        staffView.setPassword(null);
        putStaff(token, staffView);
        assertEquals(expectedMash, staffRepository.findOne(staffId).getMash());
        staffView.setPassword("abc123");
        putStaff(token, staffView);
        assertNotEquals(expectedMash, staffRepository.findOne(staffId).getMash());
    }

    @Test
    public void testDeleteStaff() throws Exception {
        String token = getTokenForStaff(staff2);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Staff/"+staff3.getId());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertNull(staffRepository.findOne(staff3.getId()).getDeleted());

        staff3.setRole(StaffRole.WAIT_STAFF);
        staffRepository.save(staff3);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Staff/"+staff3.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(staffRepository.findOne(staff3.getId()).getDeleted());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Staff/"+staff2.getId());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertNull(staffRepository.findOne(staff2.getId()).getDeleted());

        staff2.setRole(StaffRole.MANAGER);
        staff2.setDeleted(null);
        staff3.setRole(StaffRole.MANAGER);
        staff3.setDeleted(null);
        staff3.setRestaurantId(staff2.getRestaurantId());
        staffRepository.save(staff2);
        staffRepository.save(staff3);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("Staff/"+staff3.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(staffRepository.findOne(staff3.getId()).getDeleted());

    }

    @Test
    public void testGetStaffPermissions_noPermissionsYet() {
        String token = getTokenForStaff(staff2);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Staff/permissions");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        StaffPermissionView[] views = response.getBody().as(StaffPermissionView[].class);
        assertEquals(StaffRole.values().length-2, views.length); //don't include UNKNOWN and EPICURI_ADMIN
        for(StaffPermissionView staffPermissionView : views) {
            assertEquals(WaiterAppFeature.values().length, staffPermissionView.getBooleanCapabilities().size());
        }

        StaffPermissions staffPermissions = masterDataService.getRestaurant(staff2.getRestaurantId()).getStaffPermissions();
        assertNotNull(staffPermissions);
        assertEquals(StaffRole.values().length, staffPermissions.getPermissions().size());
    }

    @Test
    public void testPutStaffPermissions_noPermissionsYet() {
        //force creation of permissions
        String token = getTokenForStaff(staff2);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Staff/permissions");

        StaffPermissionView[] views = response.getBody().as(StaffPermissionView[].class);
        StaffPermissionView hostStaff = null;
        for(StaffPermissionView view : views) {
            if(view.getRole() == StaffRole.HOST_STAFF) {
                hostStaff = view;
                break;
            }
        }

        assertNotNull(hostStaff);
        WaiterAppFeatureView cashupAbility = hostStaff.getBooleanCapabilities().stream().filter(b -> b.getCapability() == WaiterAppFeature.CASH_UP).findFirst().orElse(null);
        assertFalse(cashupAbility.isEnabled());
        cashupAbility.setEnabled(true);

        Response response2 = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(views)
                .put("Staff/permissions");
        assertEquals(HttpStatus.OK.value(), response2.getStatusCode());

        StaffPermissions staffPermissions = masterDataService.getRestaurant(staff2.getRestaurantId()).getStaffPermissions();
        IndividualStaffPermission individualStaffPermission = staffPermissions.getPermissions().stream().filter(p -> p.getRole() == StaffRole.HOST_STAFF).findFirst().orElse(null);
        assertTrue(individualStaffPermission.getPermissions().get(WaiterAppFeature.CASH_UP));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Staff/permissions");

        StaffPermissionView[] views2 = response.getBody().as(StaffPermissionView[].class);
        assertEquals(views.length, views2.length);
    }
}