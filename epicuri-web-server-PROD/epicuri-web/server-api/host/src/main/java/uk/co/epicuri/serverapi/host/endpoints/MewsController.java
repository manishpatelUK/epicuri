package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsCustomer;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsRequest;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.service.external.MewsService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * See http://mewssystems.github.io/public/content/developers/api/charging.html
 */
@RestController
@RequestMapping(value = "/Mews", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MewsController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MewsService mewsService;

    @Autowired
    private SessionCalculationService calculationService;

    @HostAuthRequired
    @RequestMapping(value = "/Customers", method = RequestMethod.GET)
    public ResponseEntity<?> getCustomers(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @RequestParam(value = "name", defaultValue = "") String name,
                                          @RequestParam(value = "room", defaultValue = "") String room) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        KVData kvData = restaurant.getIntegrations().get(ExternalIntegration.MEWS);
        if(!restaurant.getIntegrations().containsKey(ExternalIntegration.MEWS)
                || kvData == null
                || StringUtils.isBlank(kvData.getToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mews is not configured for this restaurant");
        }

        String mewsToken = kvData.getToken();
        try {
            Collection<MewsCustomer> customers = mewsService.getCustomers(kvData.getHost(), mewsToken, room, name);
            return ResponseEntity.ok(customers);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error communicating with Mews: " + ex.getMessage());
        }
    }

    @HostAuthRequired
    @RequestMapping(value = "/Adjustment", method = RequestMethod.POST)
    public ResponseEntity<?> postAdjustment(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @RequestBody MewsRequest request) {
        AdjustmentType adjustmentType = masterDataService.getAdjustmentTypeByName(MewsConstants.MEWS_ADJUSTMENT_TYPE);
        if(adjustmentType == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Mews adjustment type not found");
        }

        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        KVData kvData = restaurant.getIntegrations().get(ExternalIntegration.MEWS);
        if(!restaurant.getIntegrations().containsKey(ExternalIntegration.MEWS)
                || kvData == null
                || StringUtils.isBlank(kvData.getToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mews is not configured for this restaurant");
        }

        if(request.getCustomer() == null || StringUtils.isBlank(request.getCustomer().getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mews customer information missing");
        }

        Session session = sessionService.getSession(request.getSessionId());
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        if(session.getAdjustments().size() > 0 && sessionService.hasValidPayments(session)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mews charge cannot be made as monetary adjustments have already been made against the session");
        }

        List<Order> orders = liveDataService.getOrdersBySessionId(request.getSessionId());
        try {
            String chargeId = mewsService.charge(kvData.getHost(),
                                    kvData.getToken(),
                                    session.getReadableId(),
                                    orders,
                                    masterDataService.getTaxRate(),
                                    request.getCustomer().getId(),
                                    restaurant.getISOCurrency());

            Map<CalculationKey,Number> calculationMap = calculationService.calculateValues(session,orders);

            Adjustment adjustment = new Adjustment(session.getId());
            adjustment.setAdjustmentType(adjustmentType);
            adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
            adjustment.setValue(calculationMap.get(CalculationKey.REMAINING_TOTAL).intValue());
            adjustment.setCreated(System.currentTimeMillis());
            adjustment.getSpecialAdjustmentData().put(MewsConstants.FIRST_NAME, request.getCustomer().getFirstName());
            adjustment.getSpecialAdjustmentData().put(MewsConstants.LAST_NAME, request.getCustomer().getLastName());
            adjustment.getSpecialAdjustmentData().put(MewsConstants.ROOM_NO, request.getCustomer().getRoomNumber());
            adjustment.getSpecialAdjustmentData().put(MewsConstants.CHARGE_ID, chargeId);
            adjustment.setStaffId(authenticationService.getStaffId(token));
            sessionService.addAdjustment(session.getId(), adjustment);

            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error communicating with Mews: " + ex.getMessage());
        }
    }
}
