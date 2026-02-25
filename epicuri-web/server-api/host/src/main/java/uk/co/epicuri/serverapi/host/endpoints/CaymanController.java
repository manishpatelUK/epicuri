package uk.co.epicuri.serverapi.host.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.common.pojo.external.cayman.CaymanGatewayPayload;
import uk.co.epicuri.serverapi.common.pojo.model.session.Adjustment;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.BookingService;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.SessionService;

import java.util.List;

@RestController
@RequestMapping(value = "/External/CaymanGateway", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class CaymanController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaymanController.class);

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postPayment(@RequestParam(value = "bookingId", defaultValue = "0") String bookingId, @RequestBody CaymanGatewayPayload payload) {
        LOGGER.debug("Payment confirmation {}", payload);

        Session session = sessionService.getByBookingId(bookingId);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }
        List<Adjustment> adjustments = sessionPaymentService.processCaymanPayment(session, payload);
        if(adjustments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not update payment");
        } else {
            sessionService.addAdjustments(session.getId(), adjustments);
        }
        return ResponseEntity.ok().build();
    }
}
