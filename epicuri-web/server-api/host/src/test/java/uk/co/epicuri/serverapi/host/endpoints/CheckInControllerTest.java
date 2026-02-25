package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostCheckInView;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 17.6.16..
 */
public class CheckInControllerTest extends SessionSetupBaseIT {

    private long currentTime;

    @Before
    public void setUp() throws Exception {
        super.setUpSession();

        session1.setStartTime(System.currentTimeMillis() - (1000*60*60));
        session1.setRestaurantId(restaurant1.getId());
        session1 = sessionRepository.save(session1);

        checkIn1.setRestaurantId(restaurant1.getId());
        checkIn2.setRestaurantId(restaurant1.getId());
        checkIn3.setRestaurantId(restaurant1.getId());

        checkIn1.setCustomerId(customer1.getId());
        checkIn2.setCustomerId(customer2.getId());
        checkIn3.setCustomerId(customer3.getId());

        currentTime = System.currentTimeMillis();

        checkIn1.setTime(currentTime);
        checkIn2.setTime(currentTime);
        checkIn3.setTime(currentTime);

        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);
    }

    @Test
    public void getCheckIns() throws Exception {
        String token;
        Response response;

        token = getTokenForStaff(staff1);

        //User is unauthorised (staff is not assigned to a restaurant)
        response = getCheckin(token);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        token = getTokenForStaff(staff1);

        //When everything passes
        response = getCheckin(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostCheckInView> entity = Arrays.asList(response.getBody().as(HostCheckInView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(3, entity.size());
        assertEquals(currentTime/1000, entity.stream().filter(e -> e.getCustomer().getId().equals(customer1.getId())).findFirst().get().getTime());
        assertEquals(currentTime/1000, entity.stream().filter(e -> e.getCustomer().getId().equals(customer2.getId())).findFirst().get().getTime());
        assertEquals(currentTime/1000, entity.stream().filter(e -> e.getCustomer().getId().equals(customer3.getId())).findFirst().get().getTime());

        checkIn1.setSessionId(session1.getId());
        checkIn2.setSessionId(session1.getId());
        checkIn3.setSessionId(session1.getId());
        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);

        response = getCheckin(token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        entity = Arrays.asList(response.getBody().as(HostCheckInView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, entity.size());
    }

    @Test
    public void getCheckIns2() throws Exception {
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);

        checkIn1.setSessionId(null);
        checkIn2.setSessionId(session1.getId());
        checkIn3.setSessionId(session1.getId());
        checkIn1.setPartyId(party1.getId());
        checkIn2.setPartyId(party1.getId());
        checkIn3.setPartyId(null);
        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);
        Response response = getCheckin(token, true);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostCheckInView> entity = Arrays.asList(response.getBody().as(HostCheckInView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, entity.size());
    }

    private Response getCheckin(String token) {
        return getCheckin(token, false);
    }

    private Response getCheckin(String token, boolean includeCheckInsWithParty) {
        if(includeCheckInsWithParty) {
            return given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(Params.AUTHORIZATION, token)
                    .param("includeWithParty", true)
                    .get("Checkin");
        } else {
            return given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(Params.AUTHORIZATION, token)
                    .get("Checkin");
        }
    }

    @Test
    public void testGetCheckInWhenPartyIdPresent() {
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);
        checkIn1.setPartyId("foo");
        checkInRepository.save(checkIn1);

        Response response = getCheckin(token);
        List<HostCheckInView> entity = Arrays.asList(response.getBody().as(HostCheckInView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, entity.size());
    }

    @Test
    public void testGetCheckInWhenSessionIdPresent() {
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);
        checkIn1.setSessionId("foo");
        checkInRepository.save(checkIn1);

        Response response = getCheckin(token);
        List<HostCheckInView> entity = Arrays.asList(response.getBody().as(HostCheckInView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, entity.size());
    }

    @Test
    public void testGetCheckInWhenDeleted() {
        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
        String token = getTokenForStaff(staff1);
        checkIn1.setDeleted(System.currentTimeMillis());
        checkInRepository.save(checkIn1);

        Response response = getCheckin(token);
        List<HostCheckInView> entity = Arrays.asList(response.getBody().as(HostCheckInView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, entity.size());
    }
}