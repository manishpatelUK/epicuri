package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostLevelCheckRequired;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PaymentSenseConstants;
import uk.co.epicuri.serverapi.common.pojo.host.HostAdjustmentView;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HostRefundAdjustmentView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentRequest;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Adjustment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AdjustmentController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteAdjustment(@RequestHeader(Params.AUTHORIZATION) String token,
                                              @PathVariable("id") String id) {
        String sessionId = IDAble.extractParentId(id);
        if(!sessionService.exists(sessionId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        sessionService.removeAdjustment(sessionId, id);
        asyncOrderHandlerService.onReconciliationRequest("epicuriadmin", authenticationService.getRestaurantId(token), sessionId);

        return ResponseEntity.noContent().build();
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postAdjustment(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @NotNull @RequestBody AdjustmentRequest request) {
        AdjustmentType adjustmentType = masterDataService.getAdjustmentType(request.getAdjustmentTypeId());
        if(adjustmentType == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adjustment type does not exist");
        }

        Session session  = sessionService.getSession(request.getSessionId());
        if(session == null || session.getDeleted() != null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        String staffId = authenticationService.getStaffId(token);

        if(session.isLinked()) {
            Adjustment adjustment = null;
            try {
                adjustment = sessionService.settleDeferredSession(session, staffId, request);
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
            }
            return ResponseEntity.ok().body(new HostAdjustmentView(adjustment));
        } else {
            Adjustment adjustment = sessionService.addAdjustment(session, adjustmentType, staffId, request);
            asyncOrderHandlerService.onReconciliationRequest("epicuriadmin", authenticationService.getRestaurantId(token), session.getId());
            return ResponseEntity.ok().body(new HostAdjustmentView(adjustment));
        }
    }

    @HostAuthRequired
    @RequestMapping(value = "/multiple", method = RequestMethod.POST)
    public ResponseEntity<?> postAdjustments(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @NotNull @RequestBody List<AdjustmentRequest> requests) {
        if(requests.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No adjustments");
        }

        List<String> adjustmentIds = requests.stream().filter(r -> StringUtils.isNotBlank(r.getAdjustmentTypeId())).map(AdjustmentRequest::getAdjustmentTypeId).distinct().collect(Collectors.toList());
        List<AdjustmentType> adjustmentTypes = masterDataService.getAdjustmentTypes(adjustmentIds);
        List<String> gotAdjustmentIds = adjustmentTypes.stream().map(AdjustmentType::getId).distinct().collect(Collectors.toList());
        if(adjustmentIds.size() != gotAdjustmentIds.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adjustment type does not exist");
        }

        Session session  = sessionService.getSession(requests.get(0).getSessionId());
        if(session == null || session.getDeleted() != null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        String staffId = authenticationService.getStaffId(token);
        Map<String,AdjustmentType> adjustmentTypeMap = adjustmentTypes.stream().collect(Collectors.toMap(AdjustmentType::getId, Function.identity()));
        List<Adjustment> added = new ArrayList<>();
        for(AdjustmentRequest request : requests) {
            Adjustment adjustment = sessionService.addAdjustment(session, adjustmentTypeMap.get(request.getAdjustmentTypeId()), staffId, request);
            added.add(adjustment);
        }
        asyncOrderHandlerService.onReconciliationRequest("epicuriadmin", authenticationService.getRestaurantId(token), session.getId());

        return ResponseEntity.ok().body(added.stream().map(HostAdjustmentView::new).collect(Collectors.toList()));
    }

    @HostAuthRequired
    @RequestMapping(value = "/refund/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putAdjustmentRefund(@RequestHeader(Params.AUTHORIZATION) String token,
                                                 @PathVariable("id") String id,
                                                 @NotNull @RequestBody HostRefundAdjustmentView view) {
        String sessionId = IDAble.extractParentId(id);
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        Adjustment found = null;
        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.getId().equals(id)) {
                found = adjustment;
                refundAdjustment(token, view, adjustment);
                break;
            }
        }

        if(found != null) {
            for (Adjustment adjustment : session.getAdjustments()) {
                if(hasLink(found, adjustment) || hasSamePaymentSenseReference(found, adjustment)) {
                    refundAdjustment(token, view, adjustment);
                }
            }

            sessionService.upsert(session);
        }

        return ResponseEntity.noContent().build();
    }

    private boolean hasLink(Adjustment adjustment1, Adjustment adjustment2) {
        return adjustment2.getLinkedTo() != null && adjustment2.getLinkedTo().equals(adjustment1.getId());
    }

    private boolean hasSamePaymentSenseReference(Adjustment adjustment1, Adjustment adjustment2) {
        if(!isPaymentSenseAdjustmentType(adjustment1)) {
            return false;
        }
        if(!isPaymentSenseAdjustmentType(adjustment2)) {
            return false;
        }
        return adjustment1.getSpecialAdjustmentData().getOrDefault(Adjustment.REFERENCE, "-1")
                .equals(adjustment2.getSpecialAdjustmentData().getOrDefault(Adjustment.REFERENCE, "-2"));
    }

    private boolean isPaymentSenseAdjustmentType(Adjustment adjustment) {
        if(adjustment.getAdjustmentType() == null) {
            return false;
        }
        return adjustment.getAdjustmentType().getName().equals(PaymentSenseConstants.PS_ADJUSTMENT_TYPE)
            || adjustment.getAdjustmentType().getName().equals(PaymentSenseConstants.PS_ADJUSTMENT_GRATUITY_TYPE);
    }

    public void refundAdjustment(String token, HostRefundAdjustmentView view, Adjustment adjustment) {
        adjustment.setVoided(true);
        adjustment.getSpecialAdjustmentData().put(RestaurantConstants.REFUND_ADJUSTMENT, view);
        adjustment.setVoidedByStaffId(authenticationService.getStaffId(token));
    }
}
