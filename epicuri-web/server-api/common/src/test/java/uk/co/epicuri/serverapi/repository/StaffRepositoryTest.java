package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class StaffRepositoryTest extends BaseIT {

    @Test
    public void testFindByUserNameAndRestaurantId() throws Exception {
        staff1.setUserName("user1");
        staff1.setRestaurantId(restaurant2.getId());
        staffRepository.save(staff1);

        Staff staff = staffRepository.findByUserNameAndRestaurantId(staff1.getUserName(), staff1.getRestaurantId()).get(0);

        assertNotNull(staff);
        assertEquals(staff1.getId(), staff.getId());
    }

    @Test
    public void testFindByRestaurantId() throws Exception {
        staff1.setRestaurantId(restaurant2.getId());
        staff2.setRestaurantId(restaurant2.getId());
        staff3.setRestaurantId(restaurant1.getId());

        staffRepository.deleteAll();
        staffRepository.save(staff1);
        staffRepository.save(staff2);
        staffRepository.save(staff3);

        List<Staff> r1 = staffRepository.findByRestaurantId(restaurant1.getId());
        List<Staff> r2 = staffRepository.findByRestaurantId(restaurant2.getId());
        List<Staff> r3 = staffRepository.findByRestaurantId(restaurant3.getId());

        assertEquals(1, r1.size());
        assertEquals(staff3.getId(), r1.get(0).getId());

        List<String> r2list = r2.stream().map(Staff::getId).collect(Collectors.toList());
        assertEquals(2, r2list.size());
        assertTrue(r2list.contains(staff1.getId()));
        assertTrue(r2list.contains(staff2.getId()));

        assertEquals(0, r3.size());
    }
}