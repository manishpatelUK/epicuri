package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CustomStaffRepositoryTest extends BaseIT {

    @Test
    public void testGetStaffRole() throws Exception {
        staff1.setRole(StaffRole.ASSISTANT_MANAGER);
        staff2.setRole(StaffRole.MANAGER);

        staffRepository.save(staff1);
        staffRepository.save(staff2);

        assertEquals(StaffRole.ASSISTANT_MANAGER, staffRepository.getStaffRole(staff1.getId()));
        assertEquals(StaffRole.MANAGER, staffRepository.getStaffRole(staff2.getId()));
        assertEquals(StaffRole.UNKNOWN, staffRepository.getStaffRole("foo"));
    }
}