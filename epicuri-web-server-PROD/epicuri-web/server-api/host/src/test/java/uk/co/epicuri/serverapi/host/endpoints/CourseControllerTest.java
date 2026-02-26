package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostCourseView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by lazarpantovic on 17.6.16..
 */
public class CourseControllerTest extends SessionSetupBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        setUpSession();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }

    @Test
    public void getCoursesByServiceId() throws Exception {
        String token;
        Response response;
        String serviceId = "serviceId";

        token = "";

        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Course/"+serviceId);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);

        //OK if staff member exists in that restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Course/"+serviceId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostCourseView> courses = Arrays.asList(response.getBody().as(HostCourseView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, courses.size());

        setUpCoursesAndServices();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Course/"+service1.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        courses = Arrays.asList(response.getBody().as(HostCourseView[].class, ObjectMapperType.JACKSON_2));
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

    private void assertEqual(Course course, Service service, HostCourseView view) {
        assertEquals(course.getId(), view.getId());
        assertEquals(course.getName(), view.getName());
        assertEquals(service.getId(), view.getServiceId());
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
                .get("/Course");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);
        setUpCoursesAndServices();
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("/Course");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<HostCourseView> courses = Arrays.asList(response.getBody().as(HostCourseView[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, courses.size());
        assertEqual(course1, service1, courses.stream().filter(c -> c.getId().equals(course1.getId())).findFirst().orElse(null));
        assertEqual(course3, service1, courses.stream().filter(c -> c.getId().equals(course3.getId())).findFirst().orElse(null));

    }

    @Test
    public void postCourse() throws Exception {
        Response response;
        String token = "";

        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostCourseView1)
                .post("/Course");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);

        //BAD_REQUEST if there's no body of request (HostCourseView)
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .post("/Course");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        //NOT_IMPLEMENTED if everything with the request is fine
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostCourseView1)
                .post("/Course");

        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), response.getStatusCode());
    }

    @Test
    public void putCourse() throws Exception {
        Response response;
        String token = "";

        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostCourseView1)
                .put("/Course/"+hostCourseView1.getId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);

        //BAD_REQUEST if there's no body of request (HostCourseView)
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .put("/Course/"+hostCourseView1.getId());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        //NOT_IMPLEMENTED if everything with the request is fine
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostCourseView1)
                .put("/Course/"+hostCourseView1.getId());

        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), response.getStatusCode());
    }

    @Test
    public void deleteCourse() throws Exception {
        Response response;
        String token = "";

        //UNAUTHORIZED if the staff member not existing in the restaurant
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("/Course/"+course1.getId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());

        token = getTokenForStaff(staff1);

        //NOT_IMPLEMENTED if everything with the request is fine
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .delete("/Course/"+course1.getId());

        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), response.getStatusCode());
    }

}