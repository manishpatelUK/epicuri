package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostAcknowledgementView;
import uk.co.epicuri.serverapi.common.pojo.host.NotifySessionId;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;
import uk.co.epicuri.serverapi.service.SessionService;

import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping(value = "/AdhocAcknowledgement", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AdhocAcknowledgementController {

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postAcknowledgement(@PathVariable("id") String id,
                                                 @NotNull @RequestBody NotifySessionId sessionId) {
        if(!sessionService.exists(sessionId.getSessionId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        Notification notification = liveDataService.getNotification(id);
        if(notification==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }

        long ackTime = System.currentTimeMillis();
        notification.setAcknowledged(ackTime);
        liveDataService.upsert(notification);

        HostAcknowledgementView view = new HostAcknowledgementView();
        view.setNotificationId(id);
        view.setTime(ackTime/1000);
        view.setSessionId(sessionId.getSessionId());

        return ResponseEntity.created(URI.create(String.valueOf(id))).body(view);
    }
}
