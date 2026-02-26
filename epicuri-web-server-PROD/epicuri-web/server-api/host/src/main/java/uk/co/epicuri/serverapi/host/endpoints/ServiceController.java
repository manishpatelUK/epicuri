package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostServiceView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Service", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ServiceController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    //todo temporary for those who don't update waiter app to 1.8
    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getServices(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestParam(required = false, value = "all", defaultValue = "false") boolean all) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Service> services = masterDataService.getServicesByRestaurant(restaurantId);

        //temporary - to avoid service drop-downs in waiter app
        if(!all) {
            services = services.stream().filter(Service::isDefaultService).collect(Collectors.toList());
        }

        return ResponseEntity.ok(services.stream().filter(s -> s.isActive() && s.getSessionType() != SessionType.ADHOC)
                .map(HostServiceView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<?> getAllServices(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Service> services = masterDataService.getServicesByRestaurant(restaurantId);

        int defaultServiceIndex = 0;
        for(int i = 0; i < services.size(); i++) {
            if(services.get(i).isDefaultService()) {
                defaultServiceIndex = i;
                break;
            }
        }
        if(defaultServiceIndex > 0) {
            Service defaultService = services.remove(defaultServiceIndex);
            services.add(0, defaultService);
        }

        return ResponseEntity.ok(services.stream().filter(Service::isActive).map(HostServiceView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getService(@PathVariable("id") String id) {
        Service service = masterDataService.getService(id);
        if(service == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }
        return ResponseEntity.ok(new HostServiceView(service));
    }
}
