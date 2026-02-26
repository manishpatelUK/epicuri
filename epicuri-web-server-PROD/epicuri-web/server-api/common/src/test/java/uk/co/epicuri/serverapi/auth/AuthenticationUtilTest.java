package uk.co.epicuri.serverapi.auth;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class AuthenticationUtilTest {

    public static final String PASSWORD = "foomanchu";
    public static final String SHORT_PASSWORD = "fooma";

    @Test
    public void testEncrypt() throws Exception {
        String baseString = "abc123";
        long time1 = System.currentTimeMillis();
        String mashed1 = AuthenticationUtil.encrypt(baseString, PASSWORD);
        long time2 = System.currentTimeMillis();
        String mashed2 = AuthenticationUtil.encrypt(baseString, PASSWORD);

        assertEquals(mashed1, mashed2);
        assertTrue(time2-time1 < 800);
    }

    @Test
    public void testGetPasswordMash1() throws Exception {
        Staff staff = new Staff();
        staff.setUserName("mp");
        String p = AuthenticationUtil.getPasswordMash(staff, "password");
        assertNotNull(p);
    }

    @Test
    public void testGetPasswordMash2() throws Exception {
        Customer customer = new Customer();
        boolean exception = false;
        try {
            String p = AuthenticationUtil.getPasswordMash(customer, "password");
        } catch (Exception ex) {
            exception = true;
        }
        assertTrue(exception);

        customer.setEmail("foo@bar.com");
        String p = AuthenticationUtil.getPasswordMash(customer, "password");
        assertNotNull(p);
    }
}