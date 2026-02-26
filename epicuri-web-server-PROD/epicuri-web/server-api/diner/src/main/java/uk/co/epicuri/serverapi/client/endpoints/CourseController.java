package uk.co.epicuri.serverapi.client.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.menu.CourseView;
import uk.co.epicuri.serverapi.service.MasterDataService;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Course", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CourseController {
    @Autowired
    private MasterDataService masterDataService;


    @CustomerAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getCourses(@PathVariable("id") String id) {
        List<CourseView> list = masterDataService.getCoursesByRestaurantId(id).stream().map(CourseView::new)
                                        .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
