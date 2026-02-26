package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.service.CustomerService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.PhoneNumberValidationService;
import uk.co.epicuri.serverapi.service.external.PostcodeLookupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/Customer", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private PhoneNumberValidationService phoneNumberValidationService;

    @Autowired
    private PostcodeLookupService postcodeLookupService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HostCustomerView>> getCustomer(@RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                                              @RequestParam(value = "email", required = false) String email) {
        if(email != null && ControllerUtil.EMAIL_REGEX.matcher(email).matches()) {
            email = email.toLowerCase().trim();
        } else {
            email = null;
        }

        Map<String,Preference> allPreferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));
        List<HostCustomerView> list = new ArrayList<>();
        if(StringUtils.isNotBlank(email)) {
            Customer customer = customerService.getCustomerByEmail(email.trim());
            if(customer != null) {
                list.add(new HostCustomerView(customer, allPreferences));
            }
        } else if(StringUtils.isNotBlank(phoneNumber)) {
            String phone = phoneNumberValidationService.trimPhoneNumber(phoneNumber);
            if(phone.length() > 6) {
                List<Customer> customers = customerService.getCustomersByPhone(phone);
                list.addAll(customers.stream().map(c -> new HostCustomerView(c, allPreferences)).collect(Collectors.toList()));
            }
        }

        return ResponseEntity.ok(list);
    }

    @HostAuthRequired
    @RequestMapping(path = "addressLookup", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAddress(@RequestParam(value = "postcode") String query) {
        try {
            List<Address> lookup = postcodeLookupService.lookup(query);
            return ResponseEntity.ok(lookup);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not find addresses");
        }
    }
}
