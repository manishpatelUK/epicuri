package uk.co.epicuri.serverapi.client.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCustomerView;
import uk.co.epicuri.serverapi.common.pojo.customer.KeyValuePair;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.PreferenceType;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.CustomerService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping(value = "/User", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @CustomerAuthRequired
    @RequestMapping(path = "/options", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE) //this is what diner app seems to call!
    public ResponseEntity<?> getOptions() {
        Map<String, List<KeyValuePair>> response = masterDataService.getAllPreferencesAsMap();

        return ResponseEntity.ok(response);
    }

    @CustomerAuthRequired
    @RequestMapping(path = "/1", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE) //this is what diner app seems to call!
    public ResponseEntity<?> getOptionsOld() {
        Map<String, List<KeyValuePair>> response = masterDataService.getAllPreferencesAsMap();

        return ResponseEntity.ok(response);
    }

    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> putCustomer(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @NotNull @RequestBody CustomerCustomerView customer) {
        LOGGER.trace("Customer payload: {}", customer);
        String id = authenticationService.getCustomerId(token);
        LOGGER.trace("Try to get customer: {}", id);
        Customer dbCustomer = customerService.getCustomer(id);
        LOGGER.trace("Got customer: {}", dbCustomer);

        customer.getAllergies().removeIf(this::checkPreferenceDoesNotExist);
        customer.getDietaryRequirements().removeIf(this::checkPreferenceDoesNotExist);
        customer.getFoodPreferences().removeIf(this::checkPreferenceDoesNotExist);

        Customer newCustomer = new Customer(customer, dbCustomer);

        if(dbCustomer.getEmail() == null || !dbCustomer.getEmail().equals(customer.getEmail())) {
            if(StringUtils.isBlank(customer.getEmail())) {
                newCustomer.setEmail(null);
            } else {
                //email address has changed, make sure it doesn't exist elsewhere
                Customer other = customerService.getCustomerByEmail(customer.getEmail());
                if (other != null && !other.getId().equals(id)) {
                    LOGGER.trace("Cannot change email address for dbCustomer {}, email address matches with {}", id, other.getId());
                    return ResponseEntity.badRequest().body(new StringMessage("Email address already in use"));
                }
            }
        }

        customerService.upsert(newCustomer);
        return ResponseEntity.ok(new CustomerCustomerView(newCustomer));
    }

    @CustomerAuthRequired
    @RequestMapping(path = "/cc", method = RequestMethod.POST)
    public ResponseEntity<?> postCC(@RequestHeader(Params.AUTHORIZATION) String token,
                                    @NotNull @RequestBody CreditCardData ccData) {
        String customerId = authenticationService.getCustomerId(token);
        if(customerId == null || !customerService.exists(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Profile not found"));
        }

        customerService.acquireAndPushCCData(customerId, ccData);
        return ResponseEntity.ok(ccData);
    }

    @CustomerAuthRequired
    @RequestMapping(path = "/cc", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCC(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @NotNull @RequestBody CreditCardData ccData) {
        String customerId = authenticationService.getCustomerId(token);
        if(customerId == null || !customerService.exists(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Profile not found"));
        }

        customerService.pushCCData(customerId, null);
        return ResponseEntity.ok(ccData);
    }

    @CustomerAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getCustomer(@RequestHeader(Params.AUTHORIZATION) String token) {
        String customerId = authenticationService.getCustomerId(token);
        if(customerId == null || !customerService.exists(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Profile not found"));
        }

        return ResponseEntity.ok(new CustomerCustomerView(customerService.getCustomer(customerId)));
    }

    private boolean checkPreferenceDoesNotExist(String id) {
        return StringUtils.isBlank(id) || !masterDataService.preferenceExists(id);
    }
}
