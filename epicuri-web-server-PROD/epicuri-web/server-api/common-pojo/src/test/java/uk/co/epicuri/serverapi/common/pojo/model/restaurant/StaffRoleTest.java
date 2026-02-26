package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by manish on 28/03/2017.
 */
public class StaffRoleTest {
    private Staff staff1, staff2, staff3;

    @Before
    public void setUp() throws Exception {
        staff1 = new Staff();
        staff1.setRole(StaffRole.WAIT_STAFF);

        staff2 = new Staff();
        staff2.setRole(StaffRole.MANAGER);

        staff3 = new Staff();
        staff3.setRole(StaffRole.EPICURI_ADMIN);
    }

    @Test
    public void isHigherSecurityLevelThan() throws Exception {
        assertFalse(staff1.getRole().isHigherOrEqualSecurityLevelThan(staff2.getRole()));
        assertFalse(staff2.getRole().isHigherOrEqualSecurityLevelThan(staff3.getRole()));
        assertTrue(staff3.getRole().isHigherOrEqualSecurityLevelThan(staff2.getRole()));

        staff3.setRole(StaffRole.MANAGER);
        assertTrue(staff3.getRole().isHigherOrEqualSecurityLevelThan(staff2.getRole()));
    }

}