package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CustomerRepositoryTest extends BaseIT {

    @Test
    public void testFindByEmail() throws Exception {
        assert customer1.getEmail() != null;

        Customer customer = customerRepository.findByEmail(customer1.getEmail());
        assertNotNull(customer);
        assertEquals(customer1.getId(), customer.getId());

        customer = customerRepository.findByEmail("foomanchu");
        assertNull(customer);
    }

    @Test
    public void testFindByPhoneNumber() throws Exception {
        customer1.setPhoneNumber("0123456");
        customerRepository.save(customer1);

        Customer customer = customerRepository.findByPhoneNumber(customer1.getPhoneNumber());
        assertNotNull(customer);
        assertEquals(customer1.getId(), customer.getId());

        customer = customerRepository.findByPhoneNumber("99999");
        assertNull(customer);
    }
}