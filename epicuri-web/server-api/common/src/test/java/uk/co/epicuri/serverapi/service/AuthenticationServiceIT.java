package uk.co.epicuri.serverapi.service;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCustomerView;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.host.StaffAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.repository.BookingWidgetAuthenticationsRepository;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class AuthenticationServiceIT extends BaseIT{

    @Rule
    public ExpectedException exception = ExpectedException.none();

    //@Autowired
    //private BookingWidgetAuthenticationsRepository bookingWidgetAuthenticationsRepository;

    @Test
    public void testStaffLogin() throws Exception {
        StaffAuthPayload payload = new StaffAuthPayload();
        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername(staffLogin.getUserName());
        payload.setRestaurantId(restaurant2.getStaffFacingId());

        StaffView view = authenticationService.staffLogin(payload);

        assertNotNull(view);
        assertEquals(staffLogin.getId(), view.getId());
        assertEquals(staffLogin.getName(), view.getName());
        assertEquals(staffLogin.getUserName(), view.getUsername());
        assertEquals(staffLogin.getPin(), view.getPin());
        assertTrue(view.isManager());

        String key = view.getAuthKey();
        assertNotNull(key);
        view = authenticationService.staffLogin(payload);
        assertEquals(key, view.getAuthKey());


        payload.setPassword("bad password");
        view = authenticationService.staffLogin(payload);

        assertNull(view);

        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername("other user");

        view = authenticationService.staffLogin(payload);

        assertNull(view);

        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername(staffLogin.getUserName());
        payload.setRestaurantId(null);

        view = authenticationService.staffLogin(payload);
        assertNull(view);

        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername(staffLogin.getUserName());
        payload.setRestaurantId("not valid id");

        view = authenticationService.staffLogin(payload);
        assertNull(view);
    }

    @Test
    public void testMultipleLogin() throws Exception {
        StaffAuthPayload payload = new StaffAuthPayload();
        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername(staffLogin.getUserName());
        payload.setRestaurantId(restaurant2.getStaffFacingId());

        StaffView view1 = authenticationService.staffLogin(payload);
        assertTrue(authenticationService.verifyStaffToken(view1.getAuthKey()));

        StaffView view2 = authenticationService.staffLogin(payload);
        assertTrue(authenticationService.verifyStaffToken(view2.getAuthKey()));
        assertTrue(authenticationService.verifyStaffToken(view1.getAuthKey()));
    }

    @Ignore
    @Test
    public void testCustomerLogin() throws Exception {
        CustomerAuthPayload payload = new CustomerAuthPayload();
        payload.setEmail(customerLogin.getEmail());
        payload.setPassword(AuthenticationUtilTest.PASSWORD);

        CustomerCustomerView view = authenticationService.customerLogin(payload);

        assertNotNull(view);
        assertNotNull(view.getAuthKey());

        assertEquals(view.getEmail(), customerLogin.getEmail());
        assertEquals(view.getAddress(), customerLogin.getAddress());

        payload.setPassword("bad password");
        view = authenticationService.customerLogin(payload);

        assertNull(view);

        payload.setEmail("bad email");
        payload.setPassword(AuthenticationUtilTest.PASSWORD);

        view = authenticationService.customerLogin(payload);

        assertNull(view);
    }

    @Ignore
    @Test
    public void testGetCustomerPasswordMash() throws Exception {

    }

    @Ignore
    @Test
    public void testCreateCustomerToken() throws Exception {

    }

    @Ignore
    @Test
    public void testCreateStaffToken() throws Exception {

    }

    @Test
    public void testGetRestaurantId() throws Exception {
        String restaurantId = staffLogin.getRestaurantId();
        String token = authenticationService.createStaffToken(staffLogin);

        assertEquals(restaurantId, authenticationService.getRestaurantId(token));
        exception.expect(IllegalArgumentException.class);

        authenticationService.getRestaurantId(null);
        authenticationService.getRestaurantId("foo");
        authenticationService.getRestaurantId("foo.bar");
    }

    @Test
    public void testGetCustomerId() throws Exception {
        String customerId = customerLogin.getId();
        String token = authenticationService.createCustomerToken(customerLogin);

        assertEquals(customerId, authenticationService.getCustomerId(token));
        exception.expect(IllegalArgumentException.class);

        authenticationService.getCustomerId(null);
        authenticationService.getCustomerId("foo");
    }

    @Test
    public void testGetStaffId() throws Exception {
        String staffId = staffLogin.getId();
        String token = authenticationService.createStaffToken(staffLogin);

        assertEquals(staffId, authenticationService.getStaffId(token));
        exception.expect(IllegalArgumentException.class);

        authenticationService.getStaffId(null);
        authenticationService.getStaffId("foo");
        authenticationService.getStaffId("foo.bar");
    }

    @Test
    public void testVerifyStaffToken() throws Exception {
        StaffAuthPayload payload = new StaffAuthPayload();
        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername(staffLogin.getUserName());
        payload.setRestaurantId(restaurant2.getStaffFacingId());

        StaffView view = authenticationService.staffLogin(payload);

        String token = view.getAuthKey();
        assertTrue(authenticationService.verifyStaffToken(token));
        assertTrue(authenticationService.verifyStaffToken(token));

        authenticationService.invalidateStaffLogins();

        assertFalse(authenticationService.verifyStaffToken(token));

        restaurant2.setEnabledForWaiter(false);
        restaurantRepository.save(restaurant2);

        view = authenticationService.staffLogin(payload);
        assertNull(view);
    }

    @Ignore
    @Test
    public void testVerifyCustomerToken() throws Exception {
        CustomerAuthPayload payload = new CustomerAuthPayload();
        payload.setEmail(customerLogin.getEmail());
        payload.setPassword(AuthenticationUtilTest.PASSWORD);

        CustomerCustomerView view = authenticationService.customerLogin(payload);
        String token = view.getAuthKey();

        assertTrue(authenticationService.verifyCustomerToken(token));
    }

    @Test
    public void testGetStaffSecurityLevel() throws Exception {
        StaffAuthPayload payload = new StaffAuthPayload();
        payload.setPassword(AuthenticationUtilTest.PASSWORD);
        payload.setUsername(staffLogin.getUserName());
        payload.setRestaurantId(restaurant2.getStaffFacingId());

        StaffView view = authenticationService.staffLogin(payload);

        String token = view.getAuthKey();

        int level = authenticationService.getStaffSecurityLevel(token).getSecurityLevel();

        assertEquals(staffLogin.getRole().getSecurityLevel(), level);
    }

    @Ignore
    @Test
    public void testUpdateAuditLog() throws Exception {

    }

    @Test
    public void testStripOffBasic() throws Exception {
        String s1 = "foo-bar Basic";
        String s2 = "Basicfoo-bar";
        String s3 = "Basic foo-bar";

        assertEquals(s1, AuthenticationService.stripOffBasic(s1));
        assertEquals(s2, AuthenticationService.stripOffBasic(s2));
        assertEquals("foo-bar", AuthenticationService.stripOffBasic(s3));
    }

    @Test
    public void testBookingWidgetLogin() {
        assertNull(authenticationService.bookingWidgetLogin("foobar"));

        String token = authenticationService.bookingWidgetLogin(restaurant1.getId());
        assertNull(token);
        token = authenticationService.bookingWidgetLogin(restaurant1.getStaffFacingId());
        assertNotNull(token);

        assertEquals(token, authenticationService.bookingWidgetLogin(restaurant1.getStaffFacingId()));
    }

    @Test
    public void testOnlineOrderingLogin_publicAPIToken() {
        setupOnlineOrdersIntegration();

        assertNull(authenticationService.onlineOrderingLogin(restaurant1.getId(), "loo", new String[]{}));
        String token = authenticationService.onlineOrderingLogin(restaurant1.getId(), "abc", new String[]{});
        assertNotNull(token);
    }

    private void setupOnlineOrdersIntegration() {
        KVData value = new KVData();
        value.setToken("abc");
        restaurant1.getIntegrations().put(ExternalIntegration.EPICURI_ONLINE_ORDERS, value);
        restaurantRepository.save(restaurant1);
    }

    @Test
    public void testOnlineOrderingLogin_IPs() {
        setupOnlineOrdersIntegration();
        restaurant1.getOnlineOrderingIPAddresses().add("172.100.22.11");
        restaurantRepository.save(restaurant1);

        assertNull(authenticationService.onlineOrderingLogin(restaurant1.getId(), "abc", new String[]{}));
        assertNotNull(authenticationService.onlineOrderingLogin(restaurant1.getId(), "abc", new String[]{"172.100.22.11"}));
        assertNotNull(authenticationService.onlineOrderingLogin(restaurant1.getId(), "abc", new String[]{"324234324,343q,32432,172.100.22.11,32432,23423,4","32423","q34"}));
    }
}