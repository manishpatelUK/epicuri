package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.comms.EmailRequest;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.ArchiveDataService;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;
import uk.co.epicuri.serverapi.service.SessionService;

import java.util.List;

@RestController
@RequestMapping(value = "comms", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CommsController {
    @Autowired
    private SessionService sessionService;

    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @HostAuthRequired
    @RequestMapping(path = "/email/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postEmailBillComms(@PathVariable("id") String id,
                                                @RequestBody List<EmailRequest> emailRequests) {
        Session session = sessionService.findSession(id);
        if(session == null) {
            return ResponseEntity.ok().build();
        }

        asyncCommunicationsService.sendReceiptToCustomer(session, emailRequests);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(path = "/email/cashup/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postEmailCashupComms(@PathVariable("id") String cashupId,
                                                  @RequestParam(value = "start", defaultValue = "-1") long start,
                                                  @RequestParam(value = "end", defaultValue = "-1") long end) {
        CashUp cashUp = archiveDataService.getCashUp(cashupId);
        if(cashUp != null) {
            asyncCommunicationsService.sendCashup(cashUp);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
