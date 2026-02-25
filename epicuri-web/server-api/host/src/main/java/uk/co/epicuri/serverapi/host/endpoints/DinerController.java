package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostDinerRequest;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.common.pojo.model.session.Diner;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.SessionService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Diner", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class DinerController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postDiner(@NotNull HostDinerRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putDiner(@PathVariable("id") String id,
                                      @RequestHeader(Params.AUTHORIZATION) String token,
                                      @NotNull @RequestBody HostDinerRequest request) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Diner diner = liveDataService.getDiner(id);
        if(diner == null || diner.isDefaultDiner()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Session session = sessionService.getSession(IDAble.extractParentId(diner.getId()));
        if(session == null || !session.getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Session belongs to a different restaurant");
        }
        if(session.getClosedTime() != null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session is closed");
        }

        if(request.getEpicuriUser() != null) {
            RestaurantDefault restaurantDefault = masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.CHECKIN_EXPIRATION_TIME);
            long min = ((Number) restaurantDefault.getValue()).intValue() * 60 * 1000;
            List<CheckIn> checkIns = liveDataService.getCheckIns(restaurantId, min);
            checkIns = checkIns.stream().filter(x -> x.getCustomerId() != null && x.getCustomerId().equals(request.getEpicuriUser().getId())).collect(Collectors.toList());

            if(checkIns.size() != 1) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found or has not checked in recently");
            }

            CheckIn checkIn = checkIns.get(0);
            diner.setCustomerId(checkIn.getCustomerId());
            liveDataService.upsert(session.getId(), diner);
            Party party = session.getOriginalParty();
            if(party != null) {
                liveDataService.setSessionDataOnCheckin(session.getRestaurantId(), request.getEpicuriUser().getId(), session.getId(), party.getId());
                party.setCustomerId(request.getEpicuriUser().getId());
                liveDataService.upsert(party);
            }
            if(party != null && checkIn.getPartyId() != null && !party.getId().equals(checkIn.getPartyId())) {
                liveDataService.deleteParty(checkIn.getPartyId());
            }
            return ResponseEntity.noContent().build();
        }

        if(StringUtils.isNotBlank(request.getGuestName())) {
            diner.setName(request.getGuestName());
            liveDataService.upsert(session.getId(), diner);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteDiner(@PathVariable("id") String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); //no evidence of this in waiter app code
    }

    @HostAuthRequired
    @RequestMapping(value = "/DisassociateCheckIn/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteDinerAssociation(@PathVariable("id") String id) {
        Diner diner = liveDataService.getDiner(id);

        if(diner == null || diner.isDefaultDiner()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Diner not found");
        }

        if(StringUtils.isBlank(diner.getCustomerId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Diner is not associated with a check-in");
        }

        CheckIn checkIn = liveDataService.getCheckInByCustomer(diner.getCustomerId());
        diner.setCustomer(null);
        diner.setCustomerId(null);
        liveDataService.upsert(IDAble.extractParentId(diner.getId()), diner);
        if(checkIn != null) {
            liveDataService.softDeleteCheckIn(checkIn.getId());
        }

        return ResponseEntity.noContent().build();
    }
}
