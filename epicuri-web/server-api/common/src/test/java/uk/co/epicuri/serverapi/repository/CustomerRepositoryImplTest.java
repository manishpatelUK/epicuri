package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.BlackMark;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CustomerRepositoryImplTest extends BaseIT {

    @Test
    public void testPushBlackMark() throws Exception {
        BlackMark mark = new BlackMark();
        mark.setReason("foobar");
        mark.setTime(System.currentTimeMillis());

        customerRepository.pushBlackMark(customer1.getId(),mark);

        Customer customer = customerRepository.findOne(customer1.getId());
        assertNotNull(customer);
        assertEquals(1, customer.getBlackMarks().size());
        assertEquals(mark.getReason(), customer.getBlackMarks().get(0).getReason());
        assertEquals(mark.getTime(), customer.getBlackMarks().get(0).getTime());

        customer = customerRepository.findOne(customer2.getId());
        assertNotNull(customer);
        assertEquals(0, customer.getBlackMarks().size());

        mark.setTime(System.currentTimeMillis());
        customerRepository.pushBlackMark(customer1.getId(),mark);
        customer = customerRepository.findOne(customer1.getId());
        assertEquals(2, customer.getBlackMarks().size());
    }

    @Test
    public void testPushBadSessionBlackMark() throws Exception {
        customerRepository.pushBadSessionBlackMark(customer1.getId());

        Customer customer = customerRepository.findOne(customer1.getId());
        assertNotNull(customer);
        assertEquals(1, customer.getBlackMarks().size());
    }

    @Test
    public void testPushBadSessionBlackMarks() throws Exception {
        List<String> ids = new ArrayList<>();
        ids.add(customer1.getId());
        ids.add(customer3.getId());

        customerRepository.pushBadSessionBlackMarks(ids);

        Customer customer = customerRepository.findOne(customer1.getId());
        assertNotNull(customer);
        assertEquals(1, customer.getBlackMarks().size());

        customer = customerRepository.findOne(customer3.getId());
        assertNotNull(customer);
        assertEquals(1, customer.getBlackMarks().size());

        customer = customerRepository.findOne(customer2.getId());
        assertNotNull(customer);
        assertEquals(0, customer.getBlackMarks().size());

        customerRepository.pushBadSessionBlackMarks(ids);
        customer = customerRepository.findOne(customer1.getId());
        assertEquals(2, customer.getBlackMarks().size());
    }

    @Test
    public void testSetAuthKey() throws Exception {
        String authKey1 = "auth1";
        customerRepository.setAuthKey(customer1.getId(), authKey1);

        Customer customer = customerRepository.findOne(customer1.getId());
        assertEquals(customer.getAuthKey(), authKey1);

        String authKey2 = "auth2";
        customerRepository.setAuthKey(customer1.getId(), authKey2);

        customer = customerRepository.findOne(customer1.getId());
        assertEquals(customer.getAuthKey(), authKey2);

        customer = customerRepository.findOne(customer2.getId());
        assertNull(customer.getAuthKey());
    }
}