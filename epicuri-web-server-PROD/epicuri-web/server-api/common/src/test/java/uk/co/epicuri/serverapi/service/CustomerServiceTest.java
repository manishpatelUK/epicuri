package uk.co.epicuri.serverapi.service;

import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCustomerView;
import uk.co.epicuri.serverapi.common.pojo.customer.SMSRegistrationView;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CustomerServiceTest extends BaseIT {

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testExists() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetCustomer() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetCustomers() throws Exception {

    }

    @Test
    public void testGetCustomerByEmail() throws Exception {
        Customer c1 = customerService.getCustomerByEmail(customer1.getEmail());

        assertEquals(customer1.getId(), c1.getId());

        Customer c2 = customerService.getCustomerByEmail("foo");
        assertNull(c2);

        Customer c3 = customerService.getCustomerByEmail(null);
        assertNull(c3);
    }

    @Test
    public void testGetCustomerByPhone() throws Exception {
        Customer c1 = customerService.getCustomerByPhone(customer1.getPhoneNumber());

        assertEquals(customer1.getId(), c1.getId());

        Customer c2 = customerService.getCustomerByPhone("foo");
        assertNull(c2);

        Customer c3 = customerService.getCustomerByPhone(null);
        assertNull(c3);
    }


    @Test
    public void testGetCustomersByPhoneSearch() throws Exception {
        customer1.setPhoneNumber("7893233");
        customer2.setPhoneNumber("32542354");
        customer3.setPhoneNumber("42542354");
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);

        List<Customer> list = customerService.getCustomersByPhone("2542354");
        assertFalse(list.stream().anyMatch(c -> c.getId().equals(customer1.getId())));
        assertTrue(list.stream().anyMatch(c -> c.getId().equals(customer2.getId())));
        assertTrue(list.stream().anyMatch(c -> c.getId().equals(customer3.getId())));
    }

    @Test
    public void testCreateCustomer1() throws Exception {
        SMSRegistrationView smsRegistrationView = new SMSRegistrationView();
        smsRegistrationView.setInternationalCode("44");
        smsRegistrationView.setPhoneNumber("07292722922");
        testCustomerCreation(smsRegistrationView, "7292722922", "44", "GB");

        smsRegistrationView.setRegionCode(null);
        smsRegistrationView.setInternationalCode("30");
        smsRegistrationView.setPhoneNumber("2289072038");
        testCustomerCreation(smsRegistrationView, "2289072038", "30", "GR");

        smsRegistrationView.setRegionCode(null);
        smsRegistrationView.setInternationalCode("31");
        smsRegistrationView.setPhoneNumber("07292722922");
        testCustomerCreation(smsRegistrationView, "7292722922", "31", "NL");
    }

    private void testCustomerCreation(SMSRegistrationView smsRegistrationView, String expectedNumber, String expectedCountryCode, String expectedRegion) {
        Customer customer = customerService.createCustomer(smsRegistrationView, "");
        assertNotNull(customer.getId());
        assertEquals(customer, customerRepository.findOne(customer.getId()));
        assertEquals(expectedNumber, customer.getPhoneNumber());
        assertEquals(expectedCountryCode, customer.getInternationalCode());
        assertEquals(expectedRegion, customer.getRegionCode());
    }

    @Test
    public void testCreateCustomer2() throws Exception {
        Customer customer = customerService.createCustomer("GB", "07292722922", "foo@bars.com", "foo", "bar");
        assertNotNull(customer.getId());
        assertEquals("44", customer.getInternationalCode());
        assertEquals("GB", customer.getRegionCode());
        assertEquals("7292722922", customer.getPhoneNumber());
        assertEquals("foo@bars.com", customer.getEmail());
        assertEquals("foo", customer.getFirstName());
        assertEquals("bar", customer.getLastName());

        customer = customerService.createCustomer("GB", "7292722922", "foo@bars.com", "foo", "bar");
        assertEquals("44", customer.getInternationalCode());
        assertEquals("GB", customer.getRegionCode());
        assertEquals("7292722922", customer.getPhoneNumber());

        customer = customerService.createCustomer("GB", "+447292722922", "foo@bars.com", "foo", "bar");
        assertEquals("44", customer.getInternationalCode());
        assertEquals("GB", customer.getRegionCode());
        assertEquals("7292722922", customer.getPhoneNumber());

        customer = customerService.createCustomer("GB", "00447292722922", "foo@bars.com", "foo", "bar");
        assertEquals("44", customer.getInternationalCode());
        assertEquals("GB", customer.getRegionCode());
        assertEquals("7292722922", customer.getPhoneNumber());
    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testGetCustomerByIds() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testUpsert() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testInsert() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testAddBlackMark() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testAddBlackMark1() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testAddBlackMark2() throws Exception {

    }

    @Ignore(value = "Thin layer to repository")
    @Test
    public void testSetAuthKey() throws Exception {

    }

}
