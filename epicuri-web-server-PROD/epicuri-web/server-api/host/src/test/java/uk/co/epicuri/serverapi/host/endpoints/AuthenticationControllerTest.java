package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.host.StaffAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.StaffAuthentications;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static com.jayway.restassured.RestAssured.*;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class AuthenticationControllerTest extends BaseIT {

    @Test
    public void testLogin() throws Exception {
        StaffAuthPayload payload = new StaffAuthPayload();
        payload.setRestaurantId(restaurant2.getStaffFacingId());
        payload.setUsername(staffLogin.getUserName());
        payload.setPassword(AuthenticationUtilTest.PASSWORD);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.HEAD_IP, "localhost")
                .header(Params.HEADER_EPICURI_API, "1")
                .header(Params.HEADER_MAC, "48-2C-6A-1E-59-3D")
                .header(Params.HEADER_SSID, "foobar")
                .body(payload)
                .post("Authentication/Login");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(currentVersion, Integer.valueOf(response.getHeader(Params.HEADER_EPICURI_API)).intValue());
        StaffAuthentications authentications = staffAuthenticationsRepository.findByStaffId(staffLogin.getId());
        assertNotNull(authentications);

        payload.setPassword(AuthenticationUtilTest.PASSWORD + "i");
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.HEAD_IP, "localhost")
                .header(Params.HEADER_EPICURI_API, "1")
                .header(Params.HEADER_MAC, "48-2C-6A-1E-59-3D")
                .header(Params.HEADER_SSID, "foobar")
                .body(payload)
                .post("Authentication/Login");
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());

        payload.setPassword(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.HEAD_IP, "localhost")
                .header(Params.HEADER_EPICURI_API, "1")
                .header(Params.HEADER_MAC, "48-2C-6A-1E-59-3D")
                .header(Params.HEADER_SSID, "foobar")
                .body(payload)
                .post("Authentication/Login");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testHigherThanManagerLogin() throws Exception {
        staffLogin.setRole(StaffRole.EPICURI_ADMIN);
        staffRepository.save(staffLogin);
        StaffAuthPayload payload = new StaffAuthPayload();
        payload.setRestaurantId(restaurant2.getStaffFacingId());
        payload.setUsername(staffLogin.getUserName());
        payload.setPassword(AuthenticationUtilTest.PASSWORD);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.HEAD_IP, "localhost")
                .header(Params.HEADER_EPICURI_API, "1")
                .header(Params.HEADER_MAC, "48-2C-6A-1E-59-3D")
                .header(Params.HEADER_SSID, "foobar")
                .body(payload)
                .post("Authentication/Login");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(currentVersion, Integer.valueOf(response.getHeader(Params.HEADER_EPICURI_API)).intValue());
        StaffAuthentications authentications = staffAuthenticationsRepository.findByStaffId(staffLogin.getId());
        assertNotNull(authentications);
    }
}