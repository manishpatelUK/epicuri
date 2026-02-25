package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostServiceView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Schedule;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class ServiceControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        restaurant1.getServices().add(service2);
        setUpService(service1, "default service", SessionType.SEATED, true);
        setUpService(service2, "takeaway", SessionType.TAKEAWAY, false);

        restaurantRepository.save(restaurant1);
    }

    private void setUpService(Service service, String name, SessionType sessionType, boolean isDefault) {
        service.setId(IDAble.generateId(restaurant1.getId()));
        service.setActive(true);
        menu1.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);
        service.setDefaultMenuId(menu1.getId());
        service.setSelfServiceMenuId(menu1.getId());
        service.setName(name);
        service.setSessionType(sessionType);
        service.setDefaultService(isDefault);

        if(sessionType == SessionType.SEATED) {
            Schedule schedule = new Schedule(service);
            service.setSchedule(schedule);

            ScheduledItem item1 = new ScheduledItem();
            item1.setTimeAfterStart(1000);
            item1.setText("do something 1");
            schedule.getScheduledItems().add(item1);

            ScheduledItem item2 = new ScheduledItem();
            item2.setTimeAfterStart(2000);
            item2.setText("do something 2");
            schedule.getScheduledItems().add(item2);
        }

    }

    @Test
    public void testGetServices() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("all", "true")
                .get("Service");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostServiceView> services = Arrays.asList(response.getBody().as(HostServiceView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, services.size());
        testEqual(service1, services.stream().filter(s -> s.getId().equals(service1.getId())).findFirst().get());
        testEqual(service2, services.stream().filter(s -> s.getId().equals(service2.getId())).findFirst().get());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("all", "false")
                .get("Service");
        services = Arrays.asList(response.getBody().as(HostServiceView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(1, services.size());
        testEqual(service1, services.get(0));

        service1.setSessionType(SessionType.ADHOC);
        restaurantRepository.save(restaurant1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("all", "true")
                .get("Service");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        services = Arrays.asList(response.getBody().as(HostServiceView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, services.size());
        assertEquals(service2.getId(), services.get(0).getId());
    }

    @Test
    public void testGetAllServices() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Service/all");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostServiceView> services = Arrays.asList(response.getBody().as(HostServiceView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(2, services.size());
        testEqual(service1, services.get(0));
        testEqual(service2, services.get(1));
    }

    private void testEqual(Service service, HostServiceView view) {
        assertEquals(service.getId(), view.getId());
        assertEquals(service.getDefaultMenuId(), view.getMenuId());
        assertEquals(service.getName(), view.getName());
    }

    @Test
    public void testGetService() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .get("Service/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", service2.getId())
                .get("Service/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostServiceView service = response.getBody().as(HostServiceView.class, ObjectMapperType.JACKSON_2);
        testEqual(service2, service);
    }
}