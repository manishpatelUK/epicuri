package uk.co.epicuri.serverapi.client.endpoints;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.customer.SMSAuthenticationRequest;
import uk.co.epicuri.serverapi.common.pojo.customer.SMSRegistrationView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;

/**
 * Authentication for the client apps, i.e. Guest Apps on iOS, Android etc
 * Includes authentication protocols for social media
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/Authentication", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private PhoneNumberValidationService phoneNumberValidationService;

    @Autowired
    private CustomerBindingService customerBindingService;

    @RequestMapping(value = "/loginSMS", method = RequestMethod.POST)
    public ResponseEntity<?> loginSMS(@NotNull @RequestBody SMSRegistrationView smsRegistrationView) {
        // check for phone number/name
        if(StringUtils.isBlank(smsRegistrationView.getPhoneNumber()) || StringUtils.isBlank(smsRegistrationView.getInternationalCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Phone number and international code is mandatory"));
        }

        String region = "";
        try {
            region = phoneNumberValidationService.getRegion(smsRegistrationView.getInternationalCode());
        } catch (NumberFormatException ex){}

        Customer customerByPhone = null;
        String concatenatedNumber = smsRegistrationView.getInternationalCode() + smsRegistrationView.getPhoneNumber();
        try {
            customerByPhone = customerBindingService.matchCustomer(region, concatenatedNumber);
        } catch (NumberParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Badly formatted phone number. Please use digits only and make sure it is correct."));
        }
        Customer customerByEmail = null;

        if(StringUtils.isNotBlank(smsRegistrationView.getEmail())) {
            customerByEmail = customerService.getCustomerByEmail(smsRegistrationView.getEmail());
        }

        //NB WE DON'T USE CustomerBindingService.match(email) here because that can be gamed - phone number is more secure
        // if phone does not exist, but email exists
            // ignore the email address
        Phonenumber.PhoneNumber number;
        try {
            number = phoneNumberValidationService.parse(region, concatenatedNumber);
        } catch (NumberParseException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Badly formatted phone number. Please use digits only and make sure it is correct."));
        }

        String internationalCode = String.valueOf(number.getCountryCode());
        String actualPhoneNumber = String.valueOf(number.getNationalNumber());
        if(customerByPhone == null && customerByEmail != null) {
            String code = createCodeAndSendSMS(internationalCode, actualPhoneNumber, null);
            customerByEmail.setConfirmationCode(code);
            customerByEmail.setInternationalCode(internationalCode);
            customerByEmail.setPhoneNumber(actualPhoneNumber);
            customerService.upsert(customerByEmail);

            return ResponseEntity.ok(new IdPojo(customerByEmail.getId()));
        }

        // if phone does not exist, and email does not exist
            // onboard both
        if(customerByPhone == null && customerByEmail == null) {
            String code = createCodeAndSendSMS(internationalCode, actualPhoneNumber, smsRegistrationView.getEmail());
            Customer customer = customerService.createCustomer(smsRegistrationView, code);
            return ResponseEntity.ok(new IdPojo(customer.getId()));
        }

        // if code reaches here then customerByPhone is not null and customerByEmail is null
        // if phone exists already, no need to register - just log in
            // update the email address on the account if it is null
        String code = createCodeAndSendSMS(internationalCode, actualPhoneNumber, smsRegistrationView.getEmail());
        customerService.pushConfirmationCode(customerByPhone.getId(), code);
        if(StringUtils.isBlank(customerByPhone.getEmail()) && StringUtils.isNotBlank(smsRegistrationView.getEmail())) {
            customerByPhone.setEmail(smsRegistrationView.getEmail());
            customerByPhone = customerService.upsert(customerByPhone);
        }
        return ResponseEntity.ok(new IdPojo(customerByPhone.getId()));
    }

    private String createCodeAndSendSMS(String internationalCode, String number, String email) {
        if(!masterDataService.isProdEnvironment() || isAppleTest(internationalCode, number)) {
            LOGGER.debug("Testing/Dev environment; setting auth code to 000000");
            return "000000";
        }

        String code = customerService.createConfirmationCode();
        customerService.sendSMSCode(code, internationalCode, number, email);

        return code;
    }

    private boolean isAppleTest(String internationalCode, String number) {
        return internationalCode.equals("1") && number.equals("4089961010");
    }

    @RequestMapping(value = "/checkCode", method = RequestMethod.POST)
    public ResponseEntity<?> checkCode(@NotNull @RequestBody SMSAuthenticationRequest smsAuthenticationRequest) {
        if(StringUtils.isBlank(smsAuthenticationRequest.getId()) || StringUtils.isBlank(smsAuthenticationRequest.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Customer not found"));
        }

        Customer customer = customerService.getCustomer(smsAuthenticationRequest.getId());

        if(customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Customer not found"));
        }

        if(StringUtils.isBlank(customer.getConfirmationCode()))  {
            return ResponseEntity.badRequest().body(new StringMessage("Cannot verify customer"));
        }

        if(!customer.getConfirmationCode().equals(smsAuthenticationRequest.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new StringMessage("Code is not correct"));
        }

        return ResponseEntity.ok(authenticationService.createAuthAndLogin(customer));
    }
}
