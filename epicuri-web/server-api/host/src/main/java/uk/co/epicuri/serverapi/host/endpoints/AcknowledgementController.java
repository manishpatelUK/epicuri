package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostAcknowledgementView;
import uk.co.epicuri.serverapi.common.pojo.host.NotifySessionId;

import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping(value = "/Acknowledgement", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AcknowledgementController {
    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionTimingService sessionTimingService;

    @Autowired
    private AuthenticationService authenticationService;

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postAcknowledgement(@PathVariable("id") String id,
                                                 @RequestHeader(Params.AUTHORIZATION) String token,
                                                 @NotNull @RequestBody NotifySessionId sessionId) {
        if(!sessionService.exists(sessionId.getSessionId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        long ackTime = sessionTimingService.postAcknowledge(authenticationService.getRestaurantId(token), sessionId.getSessionId(), id);

        HostAcknowledgementView view = new HostAcknowledgementView();
        view.setNotificationId(id);
        view.setSessionId(sessionId.getSessionId());
        view.setTime(ackTime/1000);

        return ResponseEntity.created(URI.create("/Acknowledgement/"+id)).body(view);
    }


}
