package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.host.HostFloorView;
import uk.co.epicuri.serverapi.common.pojo.host.HostLayoutView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.SessionService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Floor")
public class FloorController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    @Value("${epicuri.url}")
    private String epicuriBaseURL;

    private static final Logger LOGGER = LoggerFactory.getLogger(FloorController.class);

    public FloorController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HostFloorView>> getFloors(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Floor> floors = masterDataService.getFloors(restaurantId);
        List<Table> tables = masterDataService.getTables(restaurantId);

        List<HostFloorView> response = floors.stream().map(f -> new HostFloorView(f, true, tables)).collect(Collectors.toList());
        response.forEach(f -> ensureActiveLayout(f, restaurantId, masterDataService));
        //take out the layouts list - not required in this call
        response.forEach(f -> f.setLayouts(null));
        updateURL(response);

        LOGGER.trace("Floors returned for restaurant {}, size={}", restaurantId, response.size());

        return ResponseEntity.ok(response);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HostFloorView> getFloors(@PathVariable("id") String id,
                                                   @RequestHeader(Params.AUTHORIZATION) String token,
                                                   HttpServletRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Floor> floors = masterDataService.getFloors(restaurantId);
        List<Table> tables = masterDataService.getTables(restaurantId);

        for(Floor floor : floors) {
            if(floor.getId().equals(id)) {
                HostFloorView response = new HostFloorView(floor, true, tables);
                removeTemporaryLayouts(response);
                ensureActiveLayout(response, restaurantId, masterDataService);
                String url = request.getRequestURL().toString();
                updateURL(response, url);
                LOGGER.trace("Floors returned for restaurant {}, response={}", restaurantId, response);
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putLayout(@PathVariable("id") String id,
                                       @RequestHeader(Params.AUTHORIZATION) String token,
                                       @NotNull @RequestBody IdPojo layoutId) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        Floor floor = restaurant.getFloors().stream().filter(f -> f.getId().equals(id)).findFirst().orElse(new Floor());
        Layout layout = floor.getLayouts().stream().filter(l -> l.getId().equals(floor.getActiveLayout())).findFirst().orElse(new Layout());
        List<Session> allSessions = sessionService.getLiveSessions(restaurantId);
        for(Session session : allSessions) {
            if(session.getSessionType() == SessionType.SEATED) {
                for(String tableId : session.getTables()) {
                    if(layout.getTables().contains(tableId)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot change layout whilst tables are in use");
                    }
                }
            }
        }


        masterDataService.setLayoutSelected(restaurantId, id, layoutId.getId());

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/Image/{id:.+}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getFloorImage(@PathVariable("id")String id,
                                HttpServletResponse response) {
        if(id.contains(".")) {
            id = id.substring(0, id.lastIndexOf('.'));
        }

        RestaurantImage image = masterDataService.getRestaurantImage(id);
        if(image != null) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            return ResponseEntity.ok(image.getImage());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
    }

    public static void ensureActiveLayout(HostFloorView hostFloorView, String restaurantId, MasterDataService masterDataService) {
        if(StringUtils.isBlank(hostFloorView.getLayout())
                && hostFloorView.getLayouts() != null
                && hostFloorView.getLayouts().size() > 0) {
            for(HostLayoutView hostLayoutView : hostFloorView.getLayouts()) {
                if(!hostLayoutView.isTemporary()) {
                    hostFloorView.setLayout(hostLayoutView.getId());
                    masterDataService.setLayoutSelected(restaurantId, hostFloorView.getId(), hostLayoutView.getId());
                    return;
                }
            }
        }
    }
    private void updateURL(List<HostFloorView> views) {
        for(HostFloorView view : views) {
            updateURL(view, epicuriBaseURL);
        }
    }

    private void updateURL(HostFloorView view, String baseUrl) {
        if(StringUtils.isNotBlank(view.getImageURL())) {
            view.setImageURL(baseUrl + "/Floor/Image/" + view.getImageURL() + ".jpg");
        }
    }

    private void removeTemporaryLayouts(HostFloorView floor) {
        if(floor.getLayouts() == null || floor.getLayouts().size() == 0) {
            return;
        }

        floor.getLayouts().removeIf(HostLayoutView::isTemporary);
    }
}
