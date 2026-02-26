package uk.co.epicuri.serverapi.service;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.model.Country;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Lazy
public class CustomerBindingService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CustomerBindingService.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PhoneNumberValidationService phoneNumberValidationService;

    @Async
    public CompletableFuture<Booking> onBookingCreation(String bookingId) {
        if(bookingId == null) {
            return CompletableFuture.completedFuture(null);
        }

        Booking booking = bookingService.getBooking(bookingId);
        if(booking == null) {
            return CompletableFuture.completedFuture(null);
        }

        return onBookingCreation(booking);
    }

    @Async
    public CompletableFuture<Booking> onBookingCreation(Booking booking) {
        if(booking.getRestaurantId() == null || booking.getCustomerId() != null) {
            return CompletableFuture.completedFuture(null);
        }

        Restaurant restaurant = masterDataService.getRestaurant(booking.getRestaurantId());
        if(restaurant == null) {
            return CompletableFuture.completedFuture(null);
        }

        return onBookingCreation(booking, restaurant);
    }

    @Async
    public CompletableFuture<Booking> onBookingCreation(Booking booking, Restaurant restaurant) {
        Customer customer = null;
        if(booking.getCustomerId() != null) {
            customer = customerService.getCustomer(booking.getCustomerId());
        }

        if(customer != null) {
            return CompletableFuture.completedFuture(booking);
        }

        //if customer is still null, try and get customer from phone number
        List<String> countryCodes = masterDataService.getCountries().stream().map(Country::getAcronym).collect(Collectors.toList());
        customer = matchCustomer(countryCodes, booking.getTelephone(), booking.getEmail());

        //if still null, create the customer
        if(customer == null) {
            String[] names = customerService.splitNames(booking.getName());
            String firstName = null;
            String lastName = null;
            if(names.length > 0) {
                firstName = names[0];
                lastName = names[1];
            }
            Country country = masterDataService.getCountry(restaurant.getCountryId());
            customer = customerService.createCustomer(country.getAcronym(), booking.getTelephone(), booking.getEmail(), firstName, lastName);
            if(booking.getOptedIntoMarketing() != null) {
                customerService.pushOptedIntoMarketing(customer.getId(), booking.getOptedIntoMarketing());
            }
        }

        bookingService.pushCustomerId(booking.getId(), customer.getId());
        return CompletableFuture.completedFuture(booking);
    }

    public Customer matchCustomer(List<String> suspectedCountryOrigins, String phone, String email) {
        if(StringUtils.isBlank(phone) && StringUtils.isBlank(email)) {
            return null;
        }

        if(StringUtils.isNotBlank(email)) {
            Customer customer = customerService.getCustomerByEmail(email);
            if(customer != null) {
                return customer;
            }
        }

        if(StringUtils.isNotBlank(phone)) {
            return matchCustomer(suspectedCountryOrigins, phone);
        } else {
            return null;
        }
    }

    public Customer matchCustomer(List<String> suspectedCountryOrigins, String phone) {
        for(String suspectedCountryOrigin : suspectedCountryOrigins) {
            try {
                Customer customer = matchCustomer(suspectedCountryOrigin, phone);
                if(customer != null) {
                    return customer;
                }
            } catch (Exception e) {}
        }

        return null;
    }

    public Customer matchCustomer(String suspectedCountryOrigin, String phone) throws NumberParseException {
        Phonenumber.PhoneNumber number = phoneNumberValidationService.parse(suspectedCountryOrigin, phone);
        if(StringUtils.isBlank(phone) || !phoneNumberValidationService.isPossibleNumber(number)) {
            return null;
        }

        List<Customer> customers = customerService.getCustomersByPhone(String.valueOf(number.getNationalNumber()));
        String countryCode = String.valueOf(number.getCountryCode());
        for(Customer customer : customers) {
            if(customer.getInternationalCode().equals(countryCode)) {
                return customer;
            }
        }

        return null;
    }
}
