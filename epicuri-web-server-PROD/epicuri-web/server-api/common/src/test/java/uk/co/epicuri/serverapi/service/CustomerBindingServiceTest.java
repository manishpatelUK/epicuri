package uk.co.epicuri.serverapi.service;

import org.junit.Before;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class CustomerBindingServiceTest extends BaseIT {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        restaurant1.setCountryId(country1.getId());
        restaurantRepository.save(restaurant1);

        country1.setAcronym("GB");
        countryRepository.save(country1);
    }

    @Test
    public void onBookingCreation1() throws Exception {
        CustomerBindingService customerBindingService = getCustomerBindingService();
        CompletableFuture<Booking> bookingCompletableFuture = customerBindingService.onBookingCreation("foo");
        Booking booking = bookingCompletableFuture.get();
        assertNull(booking);
    }

    @Test
    public void onBookingCreation2() throws Exception  {
        CustomerBindingService customerBindingService = getCustomerBindingService();
        booking1.setRestaurantId("foo");
        CompletableFuture<Booking> bookingCompletableFuture = customerBindingService.onBookingCreation(booking1);
        Booking booking = bookingCompletableFuture.get();
        assertNull(booking);
    }

    @Test
    public void onBookingCreationExistingCustomer() throws Exception  {
        CustomerBindingService customerBindingService = getCustomerBindingService();
        booking1.setRestaurantId(restaurant1.getId());

        customer1.setEmail("foo@bar.com");
        customer1.setInternationalCode("44");
        customer1.setPhoneNumber("7393663938");
        customerRepository.save(customer1);
        int originalNumberOfCustomers = customerRepository.findAll().size();

        //an existing customer with email address
        booking1.setEmail("foo@bar.com");
        customerBindingService.onBookingCreation(booking1, restaurant1);
        assertEquals(originalNumberOfCustomers, customerRepository.findAll().size());

        //an existing customer with email address AND phone
        booking1.setTelephone("7393663938");
        customerBindingService.onBookingCreation(booking1, restaurant1);
        assertEquals(originalNumberOfCustomers, customerRepository.findAll().size());

        //with a padded number
        booking1.setTelephone("07393663938");
        customerBindingService.onBookingCreation(booking1, restaurant1);
        assertEquals(originalNumberOfCustomers, customerRepository.findAll().size());

        //an existing customer with phone
        booking1.setEmail(null);
        customerBindingService.onBookingCreation(booking1, restaurant1);
        assertEquals(originalNumberOfCustomers, customerRepository.findAll().size());
    }

    @Test
    public void onBookingCreationNonExistingCustomer1() throws Exception  {
        CustomerBindingService customerBindingService = getCustomerBindingService();
        booking1.setRestaurantId(restaurant1.getId());

        int originalNumberOfCustomers = customerRepository.findAll().size();

        booking1.setEmail("foo@barla.com");
        booking1.setName("Goo la manchu");
        customerBindingService.onBookingCreation(booking1, restaurant1);
        assertEquals(originalNumberOfCustomers+1, customerRepository.findAll().size());

        Customer customer = customerRepository.findByEmail(booking1.getEmail());
        assertEquals(booking1.getEmail(), customer.getEmail());
        assertNull(customer.getPhoneNumber());
        assertNull(customer.getInternationalCode());
        assertEquals("Goo", customer.getFirstName());
        assertEquals("la manchu", customer.getLastName());
    }

    @Test
    public void onBookingCreationNonExistingCustomer2() throws Exception  {
        CustomerBindingService customerBindingService = getCustomerBindingService();
        booking1.setRestaurantId(restaurant1.getId());

        int originalNumberOfCustomers = customerRepository.findAll().size();

        booking1.setEmail(null);
        booking1.setTelephone("07222929222");
        booking1.setName("Goo la manchu");
        customerBindingService.onBookingCreation(booking1, restaurant1);
        assertEquals(originalNumberOfCustomers+1, customerRepository.findAll().size());

        Customer customer = customerRepository.findByPhoneNumber("7222929222");
        assertEquals(booking1.getEmail(), customer.getEmail());
        assertEquals("7222929222", customer.getPhoneNumber());
        assertEquals("44", customer.getInternationalCode());
        assertEquals("Goo", customer.getFirstName());
        assertEquals("la manchu", customer.getLastName());
    }

    @Test
    public void matchCustomer1() throws Exception  {
        CustomerBindingService customerBindingService = getCustomerBindingService();
        Customer customer = customerBindingService.matchCustomer(null, null, null);
        assertNull(customer);

        customer1.setEmail("goo@bar.com");
        customerRepository.save(customer1);
        customer = customerBindingService.matchCustomer(null, null, customer1.getEmail());
        assertEquals(customer1, customer);

        customer = customerBindingService.matchCustomer(null, "1234", customer1.getEmail());
        assertEquals(customer1, customer);

        customer = customerBindingService.matchCustomer(Collections.singletonList("GB"), "1234", customer1.getEmail());
        assertEquals(customer1, customer);
    }

    @Test
    public void matchCustomer2() throws Exception  {
        customer1.setRegionCode("GB");
        customer1.setInternationalCode("44");
        customer1.setPhoneNumber("7383773764");
        customerRepository.save(customer1);

        customer2.setRegionCode("GR");
        customer2.setInternationalCode("30");
        customer2.setPhoneNumber("7383773764");
        customerRepository.save(customer2);

        customer3.setRegionCode("NL");
        customer3.setInternationalCode("31");
        customer3.setPhoneNumber("7383773764");
        customerRepository.save(customer3);

        CustomerBindingService customerBindingService = getCustomerBindingService();

        Customer customer = customerBindingService.matchCustomer("GB", "7383773764");
        assertEquals(customer1, customer);
        customer = customerBindingService.matchCustomer("GB", "07383773764");
        assertEquals(customer1, customer);
        customer = customerBindingService.matchCustomer("GB", "004407383773764");
        assertEquals(customer1, customer);
        customer = customerBindingService.matchCustomer("GB", "00447383773764");
        assertEquals(customer1, customer);
        customer = customerBindingService.matchCustomer("GB", "447383773764");
        assertEquals(customer1, customer);
        customer = customerBindingService.matchCustomer("GB", "+447383773764");
        assertEquals(customer1, customer);
        customer = customerBindingService.matchCustomer("GR", "7383773764");
        assertEquals(customer2, customer);
        customer = customerBindingService.matchCustomer("NL", "7383773764");
        assertEquals(customer3, customer);
    }
}