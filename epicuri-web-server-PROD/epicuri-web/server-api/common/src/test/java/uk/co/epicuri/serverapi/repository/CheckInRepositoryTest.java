package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CheckInRepositoryTest extends BaseIT {

    @Test
    public void testFindByCustomerId() throws Exception {
        checkIn1.setCustomerId(customer1.getId());
        checkIn2.setCustomerId(customer2.getId());
        checkIn3.setCustomerId(null);

        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);

        assertNotNull(checkInRepository.findByCustomerIdAndDeletedNull(customer1.getId()).stream().filter(c -> c.getCustomerId().equals(customer1.getId())).findFirst().orElse(null));
        assertNotNull(checkInRepository.findByCustomerIdAndDeletedNull(customer2.getId()).stream().filter(c -> c.getCustomerId().equals(customer2.getId())).findFirst().orElse(null));
        assertEquals(0, checkInRepository.findByCustomerIdAndDeletedNull("foobar").size());
        assertNull(checkInRepository.findByCustomerIdAndDeletedNull(customer3.getId()).stream().filter(c -> c.getCustomerId().equals(customer3.getId())).findFirst().orElse(null));

        checkIn1.setDeleted(0L);
        checkInRepository.save(checkIn1);
        assertEquals(0, checkInRepository.findByCustomerIdAndDeletedNull(customer1.getId()).size());
    }

    @Test
    public void testFindByRestaurantIdAndTimeGreaterThanEqual() throws Exception {
        checkInRepository.deleteAll();
        addCheckIns();

        checkIn1.setTime(10);
        checkIn2.setTime(10);
        checkIn3.setRestaurantId("foobar");
        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);

        List<CheckIn> checkIns = checkInRepository.findByRestaurantIdAndTimeGreaterThanEqual(restaurant1.getId(),0);
        assertTrue(checkIns.size() == 2);
        assertTrue(checkIns.stream().anyMatch(c -> c.getId().equals(checkIn1.getId())));
        assertTrue(checkIns.stream().anyMatch(c -> c.getId().equals(checkIn2.getId())));

        checkIns = checkInRepository.findByRestaurantIdAndTimeGreaterThanEqual(restaurant1.getId(),10);
        assertTrue(checkIns.size() == 2);
        assertTrue(checkIns.stream().anyMatch(c -> c.getId().equals(checkIn1.getId())));
        assertTrue(checkIns.stream().anyMatch(c -> c.getId().equals(checkIn2.getId())));

        checkIns = checkInRepository.findByRestaurantIdAndTimeGreaterThanEqual(restaurant1.getId(),100);
        assertTrue(checkIns.size() == 0);
    }

    @Test
    public void testFindByRestaurantIdAndBookingIdIn() throws Exception {
        checkIn1.setBookingId(booking1.getId());
        checkIn2.setBookingId(booking2.getId());
        checkIn3.setRestaurantId("foobar");
        checkIn3.setBookingId(booking1.getId());
        checkInRepository.save(checkIn1);
        checkInRepository.save(checkIn2);
        checkInRepository.save(checkIn3);

        List<String> ids = new ArrayList<>();
        ids.add(booking1.getId());
        ids.add(booking2.getId());

        List<CheckIn> checkIns = checkInRepository.findByRestaurantIdAndBookingIdIn(restaurant1.getId(),ids);
        assertTrue(checkIns.size() == 2);
        assertTrue(checkIns.stream().anyMatch(c -> c.getId().equals(checkIn1.getId())));
        assertTrue(checkIns.stream().anyMatch(c -> c.getId().equals(checkIn2.getId())));
    }
}

