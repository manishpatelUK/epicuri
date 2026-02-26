package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostEventView;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.SessionTimingService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Event", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class EventController {

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionTimingService sessionTimingService;

    private static Comparator<HostEventView> EVENT_COMPARATOR = (n1, n2) -> Long.compare(n1.getDue(), n2.getDue());

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<HostEventView>> getEvents(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Map<Session,List<Notification>> all = liveDataService.getAllUnacknowledgedNotificationsBySession(restaurantId);

        List<HostEventView> events = new ArrayList<>();
        for(Map.Entry<Session,List<Notification>> entry : all.entrySet()) {
            events.addAll(entry.getValue()
                    .stream()
                    .map(n -> new HostEventView(n, sessionTimingService.calculateDelay(entry.getKey(),System.currentTimeMillis(),n)))
                    .collect(Collectors.toList()));
        }

        return ResponseEntity.ok(events.stream().sorted(EVENT_COMPARATOR).collect(Collectors.toList()));
    }
}
