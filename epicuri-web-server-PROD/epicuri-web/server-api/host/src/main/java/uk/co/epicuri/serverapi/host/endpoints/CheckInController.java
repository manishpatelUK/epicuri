package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostCheckInView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.CustomerService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "Checkin", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class CheckInController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private CustomerService customerService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getCheckIns(@RequestHeader(Params.AUTHORIZATION) String token, @RequestParam(value = "includeWithParty", defaultValue = "false") boolean includeCheckInsWithParty) {
        String restaurantId = authenticationService.getRestaurantId(token);
        long expirationTime = 60 * 1000 * masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.CHECKIN_EXPIRATION_TIME)
                                .getAsOrDefault(Integer.class, 15); //should use the proper default, but saves a trip to db
        List<CheckIn> checkIns;
        if(includeCheckInsWithParty) {
            checkIns = liveDataService.getCheckIns(restaurantId, expirationTime).stream().filter(c -> StringUtils.isBlank(c.getSessionId())).collect(Collectors.toList());
        } else {
            checkIns = liveDataService.getCheckIns(restaurantId, expirationTime).stream().filter(c -> StringUtils.isBlank(c.getSessionId()) && StringUtils.isBlank(c.getPartyId())).collect(Collectors.toList());
        }
        LOGGER.trace("Have {} checkIns to consider", checkIns.size());
        List<Customer> customers = customerService.getCustomerByIds(checkIns.stream()
                                                            .filter(x -> StringUtils.isNotBlank(x.getCustomerId()))
                                                            .map(CheckIn::getCustomerId).collect(Collectors.toList()));
        LOGGER.trace("Have {} customers to cross ref", customers.size());
        Map<String,Customer> map = customers.stream().collect(Collectors.toMap(Customer::getId, Function.identity()));
        Map<String,Preference> preferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));

        return ResponseEntity.ok(checkIns.stream().map(x -> new HostCheckInView(x, map.get(x.getCustomerId()), preferences)).collect(Collectors.toList()));
    }
}
