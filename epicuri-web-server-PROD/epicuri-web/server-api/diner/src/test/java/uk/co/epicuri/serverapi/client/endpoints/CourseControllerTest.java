package uk.co.epicuri.serverapi.client.endpoints;

import org.junit.Before;
import org.junit.Test;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.menu.CourseView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 1.8.16..
 */
public class CourseControllerTest extends SessionSetupBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        setUpSession();
    }

    @Test
    public void getCourses() throws Exception {
        String token;
        Response response;

        token = "";

        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Course/"+restaurant1.getId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForCustomer(customerLogin);

        setUpCoursesAndServices();
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Course/"+restaurant1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<CourseView> courses = Arrays.asList(response.getBody().as(CourseView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, courses.size());
        assertEqual(course1, service1, courses.stream().filter(c -> c.getId().equals(course1.getId())).findFirst().orElse(null));
        assertEqual(course3, service1, courses.stream().filter(c -> c.getId().equals(course3.getId())).findFirst().orElse(null));
    }

    private void setUpCoursesAndServices() {
        service1.setId(IDAble.generateId(restaurant1.getId()));
        course1.setId(IDAble.generateId(service1.getId()));
        course3.setId(IDAble.generateId(service1.getId()));
        service1.getCourses().clear();
        service1.getCourses().add(course1);
        service1.getCourses().add(course3);
        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        restaurantRepository.save(restaurant1);

        service2.setId(IDAble.generateId(restaurant2.getId()));
        service3.setId(IDAble.generateId(restaurant2.getId()));
        restaurantRepository.save(restaurant2);
    }

    private void assertEqual(Course course, Service service, CourseView view) {
        assertEquals(course.getId(), view.getId());
        assertEquals(course.getName(), view.getName());
        assertEquals(service.getId(), view.getServiceId());
    }

}