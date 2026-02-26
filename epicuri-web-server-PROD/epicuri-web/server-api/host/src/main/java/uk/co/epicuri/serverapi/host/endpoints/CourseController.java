package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostCourseView;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.common.pojo.model.Course;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Course", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CourseController {
    @Autowired
    private MasterDataService masterDataDatabase;

    @Autowired
    private AuthenticationService authenticationService;

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<HostCourseView>> getCoursesByServiceId(@RequestHeader(Params.AUTHORIZATION) String token,
                                                   @PathVariable("id") String id) {
        List<Course> courses = masterDataDatabase.getCoursesByServiceId(id);
        return ResponseEntity.ok(courses.stream().map(HostCourseView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<HostCourseView>> getCourses(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Course> courses = masterDataDatabase.getCoursesByRestaurantId(restaurantId);
        return ResponseEntity.ok(courses.stream().map(HostCourseView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity postCourse(@NotNull @RequestBody HostCourseView course) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putCourse(@PathVariable("id")String id,
                                        @RequestHeader(Params.AUTHORIZATION) String token,
                                        @NotNull @RequestBody HostCourseView course) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCourse(@PathVariable("id")String id,
                                          @RequestHeader(Params.AUTHORIZATION) String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


}
