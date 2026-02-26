package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostPartyChangeRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.*;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Party", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class PartyController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    public PartyController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getParties(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Party> parties = liveDataService.getParties(restaurantId).stream().filter(p -> p.getDeleted() == null).collect(Collectors.toList());
        Map<String,Session> partyToSession = sessionService.getSessionByPartyIds(parties.stream().map(Party::getId).collect(Collectors.toList()))
                                                .stream().collect(Collectors.toMap(Session::getOriginalPartyId, Function.identity()));

        // take out the parties that are seated or closed
        parties.removeIf(p -> partyToSession.containsKey(p.getId()) && (partyToSession.get(p.getId()).getSessionType() == SessionType.SEATED
                || partyToSession.get(p.getId()).getClosedTime() != null)
                || (partyToSession.get(p.getId()) != null && partyToSession.get(p.getId()).getSessionType() == SessionType.REFUND));

        //get customers in one go
        List<Customer> customers = customerService.getCustomerByIds(parties.stream().filter(p -> StringUtils.isNotBlank(p.getCustomerId())).map(Party::getCustomerId).collect(Collectors.toList()));
        List<HostPartyView> response = new ArrayList<>();
        Integer walkInExp = ((Number)masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.WALKIN_EXPIRATION_TIME).getValue()).intValue();
        Map<String,Preference> preferenceMap = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));

        parties.forEach(p -> {
            HostPartyView view = null;

            if(partyToSession.containsKey(p.getId())) {
                view = createHostPartyView(partyToSession, customers, preferenceMap, p);
            }
            else if(p.getPartyType() == PartyType.WALK_IN && (p.getTime() + (walkInExp * 60 * 1000)) > System.currentTimeMillis()) {
                view = createHostPartyView(partyToSession, customers, preferenceMap, p);
            } else if(p.getPartyType() == PartyType.RESERVATION && StringUtils.isNotBlank(p.getBookingId())
                    && p.getArrivedTime() != null
                    && (p.getArrivedTime() + (walkInExp * 60 * 1000)) > System.currentTimeMillis()) {
                view = createHostPartyView(partyToSession, customers, preferenceMap, p);
            }

            if(view != null) {
                response.add(view);
            }

        });

        return ResponseEntity.ok(response);
    }

    @HostAuthRequired
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putParty(@RequestBody HostPartyChangeRequest request,
                                      @PathVariable("id") String id) {
        Party party = liveDataService.getParty(id);
        if(party == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Party not found");
        }
        Session session = sessionService.getSessionByPartyId(id);
        if(session != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Party is already ");
        }

        if(request.getNumberOfDiners() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid number of diners");
        }
        party.setNumberOfPeople(request.getNumberOfDiners());
        if(StringUtils.isNotBlank(request.getName()) && !request.getName().equals(party.getName())) {
            party.setName(request.getName());
        }
        liveDataService.upsert(party);
        return ResponseEntity.ok().build();
    }

    private HostPartyView createHostPartyView(Map<String, Session> partyToSession, List<Customer> customers, Map<String, Preference> preferenceMap, Party p) {
        HostPartyView view = null;
        if(p.getBookingId() != null) {
            Booking booking = bookingService.getBooking(p.getBookingId());
            if(booking != null && !booking.isRejected()) {
                view = createPartyViewWithCustomerAndBooking(partyToSession, customers, preferenceMap, p, booking);
            }
        }
        else if(StringUtils.isBlank(p.getCustomerId())) {
            view = new HostPartyView(p, partyToSession.get(p.getId()));
        } else {
            view = createPartyWithCustomer(partyToSession, customers, preferenceMap, p);
        }
        return view;
    }

    private HostPartyView createPartyWithCustomer(Map<String, Session> partyToSession, List<Customer> customers, Map<String, Preference> preferenceMap, Party p) {
        HostPartyView view;
        Customer customer = customers.stream().filter(c -> c.getId().equals(p.getCustomerId())).findFirst().orElse(null);
        if(customer != null && customer.isRegisteredViaApp()) {
            view = new HostPartyView(p, customer, preferenceMap, partyToSession.get(p.getId()));
        } else {
            view = new HostPartyView(p, partyToSession.get(p.getId()));
        }
        return view;
    }

    private HostPartyView createPartyViewWithCustomerAndBooking(Map<String, Session> partyToSession, List<Customer> customers, Map<String, Preference> preferenceMap, Party p, Booking booking) {
        HostPartyView view;
        Customer customer = customers.stream().filter(c -> c.getId().equals(p.getCustomerId())).findFirst().orElse(null);
        if(customer != null && customer.isRegisteredViaApp()) {
            view = new HostPartyView(p, partyToSession.get(p.getId()), booking, preferenceMap, customer);
        } else {
            view = new HostPartyView(p, partyToSession.get(p.getId()), booking);
        }
        return view;
    }

}
