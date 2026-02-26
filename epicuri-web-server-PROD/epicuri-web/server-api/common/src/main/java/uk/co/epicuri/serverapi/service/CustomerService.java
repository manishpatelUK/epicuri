package uk.co.epicuri.serverapi.service;

import com.google.common.collect.Lists;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.customer.SMSRegistrationView;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.Country;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    @Lazy
    private AsyncCommunicationsService asyncCommunicationsService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private PhoneNumberValidationService phoneNumberValidationService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    public boolean exists(String id) {
        return customerRepository.exists(id);
    }

    //customers
    public Customer getCustomer(String id) {
        return customerRepository.findOne(id);
    }

    public List<Customer> getCustomers(List<String> ids) {
        return Lists.newArrayList(customerRepository.findAll(ids));
    }

    public Customer getCustomerByEmail(String email) {
        if(StringUtils.isBlank(email)) {
            return null;
        }
        return customerRepository.findByEmail(email);
    }

    public Customer getCustomerByPhone(String phone) {
        if(StringUtils.isBlank(phone)) {
            return null;
        }
        return customerRepository.findByPhoneNumber(phoneNumberValidationService.trimPhoneNumber(phone));
    }

    public List<Customer> getCustomersByPhone(String phone) {
        if(StringUtils.isBlank(phone)) {
            return new ArrayList<>();
        }
        return customerRepository.searchPhoneNumber(phone);
    }

    public String[] splitNames(String name) {
        if(name == null) {
            return new String[0];
        }

        String[] bits = name.split("\\s");
        String firstName = bits[0];
        String lastName = null;
        if(bits.length > 1) {
            lastName = StringUtils.join(ArrayUtils.remove(bits, 0), ' ');
        }
        return new String[]{firstName, lastName};
    }

    public List<Customer> getCustomerByIds(List<String> ids) {
        return Lists.newArrayList(customerRepository.findAll(ids));
    }

    public Customer upsert(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer insert(Customer customer) {
        return customerRepository.insert(customer);
    }

    public void addBlackMark(String customerId) {
        customerRepository.pushBadSessionBlackMark(customerId);
    }

    public void addBlackMark(List<String> customerIds) {
        customerRepository.pushBadSessionBlackMarks(customerIds);
    }

    public void addBlackMark(String id, BlackMark blackMark) {
        customerRepository.pushBlackMark(id, blackMark);
    }

    public void setAuthKey(String customerId, String key) {
        customerRepository.setAuthKey(customerId, key);
    }

    public void pushConfirmationCode(String customerId, String code) {
        customerRepository.setConfirmationCode(customerId, code);
    }

    public Customer createCustomer(SMSRegistrationView smsRegistrationView, String confirmationCode) {
        String trimmedNumber = phoneNumberValidationService.trimPhoneNumber(smsRegistrationView.getPhoneNumber());
        try {
            Phonenumber.PhoneNumber number = phoneNumberValidationService.concat(smsRegistrationView.getInternationalCode(),smsRegistrationView.getPhoneNumber());
            smsRegistrationView.setInternationalCode(String.valueOf(number.getCountryCode()));
            smsRegistrationView.setPhoneNumber(String.valueOf(number.getNationalNumber()));
            if(smsRegistrationView.getRegionCode() == null) {
                smsRegistrationView.setRegionCode(phoneNumberValidationService.getRegion(number));
            }
        } catch (NumberParseException e) {
            smsRegistrationView.setPhoneNumber(trimmedNumber);
        }

        Customer customer = new Customer(smsRegistrationView, confirmationCode);
        customer.setRegisteredViaApp(true);
        return createCustomer(customer);
    }

    private Customer createCustomer(Customer customer) {
        customer = insert(customer);

        if(!masterDataService.isTestEnvironment()) {
            asyncCommunicationsService.onNewCustomer(customer);
        }

        return customer;
    }

    public Customer createCustomer(String regionCode, String phoneNumber, String emailAddress, String firstName, String lastName) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        if(StringUtils.isNotBlank(phoneNumber)) {
            try {
                Phonenumber.PhoneNumber number = phoneNumberValidationService.parse(regionCode, phoneNumber);
                customer.setInternationalCode(String.valueOf(number.getCountryCode()));
                customer.setPhoneNumber(String.valueOf(number.getNationalNumber()));
            } catch (NumberParseException e) {}
            customer.setRegionCode(regionCode);
        }
        customer.setEmail(emailAddress);
        customer.setRegisteredViaApp(false);

        return createCustomer(customer);
    }

    public String createConfirmationCode() {
        return RandomStringUtils.randomNumeric(6);
    }

    public void acquireAndPushCCData(String customerId, CreditCardData creditCardData) {
        boolean succeeded = sessionPaymentService.processAcquisition(getCustomer(customerId), creditCardData);
        if(succeeded) {
            pushCCData(customerId, creditCardData);
        }
    }

    public void pushCCData(String customerId, CreditCardData creditCardData) {
        customerRepository.pushCCData(customerId, creditCardData);
    }

    public void pushLegalCommunicationsSent(String customerId) {
        customerRepository.pushLegalCommunicationSent(customerId, System.currentTimeMillis());
    }

    public void pushOptedIntoMarketing(String customerId, boolean optedIn) {
        customerRepository.pushOptedIntoMarketing(customerId, optedIn);
    }

    public void sendSMSCode(String code, String internationalCode, String number, String email) {
        asyncCommunicationsService.sendSMSCode(code, internationalCode, number, email);
    }
}
