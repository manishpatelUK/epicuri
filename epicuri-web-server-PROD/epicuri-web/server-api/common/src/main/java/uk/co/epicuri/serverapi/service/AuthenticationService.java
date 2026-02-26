package uk.co.epicuri.serverapi.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.BadStateException;
import uk.co.epicuri.serverapi.auth.AuthenticationUtil;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerCustomerView;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.BookingWidgetAuthentications;
import uk.co.epicuri.serverapi.common.pojo.model.internal.AuditLog;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.authentication.StaffAuthentication;
import uk.co.epicuri.serverapi.common.pojo.host.StaffAuthPayload;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.StaffAuthentications;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.repository.AuditLogRepository;
import uk.co.epicuri.serverapi.repository.StaffAuthenticationsRepository;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationService implements InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String BOOKING_WIDGET_BASE_STRING = "bookingwidget";
    private static final String ONLINE_ORDERING_BASE_STRING = "onlineordering";

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private StaffAuthenticationsRepository staffAuthenticationsRepository;

    private LoadingCache<String,StaffAuthentications> staffAuthentications;
    private LoadingCache<String,String> customerAuthentications;
    private String currentAdminToken;

    @Value("${epicuri.auth.cachesize.max}")
    private int maxCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        staffAuthentications = CacheBuilder.newBuilder()
                .maximumSize(maxCache)
                .expireAfterWrite(32, TimeUnit.DAYS)
                .build(new StaffKeyLoader());

        customerAuthentications = CacheBuilder.newBuilder()
                .maximumSize(maxCache)
                .expireAfterWrite(365, TimeUnit.DAYS)
                .build(new CustomerKeyLoader());
    }

    //only used in testing for now, but will be useful for releases too
    public synchronized void invalidateStaffLogins() {
        staffAuthentications.invalidateAll();
        staffAuthenticationsRepository.deleteAll();
    }

    public synchronized void invalidateStaffLogin(String staffId) {
        masterDataService.deleteAuthentication(staffId);
        staffAuthentications.invalidate(staffId);
    }

    public synchronized IdPojo adminLogin(String userName, String password) {
        String passwordMash;
        String userNameMash;
        try {
            passwordMash = AuthenticationUtil.encrypt(password);
            userNameMash = AuthenticationUtil.encrypt(userName);
        } catch (BadStateException e) {
            return null;
        }
        if(passwordMash.equalsIgnoreCase("daa789198fc6d70c74dd4f80de7861a8")
                && userNameMash.equalsIgnoreCase("d143ee6687065bd9ee604ec4012adaab")) {
            currentAdminToken = createAdminToken();
            return new IdPojo(currentAdminToken);
        }

        return null;
    }

    public StaffView staffLogin(StaffAuthPayload payload) {
        Staff staff = masterDataService.getStaff(payload.getUsername(), payload.getRestaurantId());
        if(staff == null || staff.getDeleted() != null || staff.getRestaurantId() == null) {
            LOGGER.trace("Did not pass {} because staff is {}", payload.getUsername(), staff);
            return null;
        }

        String mash;
        try {
            mash = AuthenticationUtil.getPasswordMash(staff, payload.getPassword());
        } catch (BadStateException e) {
            LOGGER.trace("Could not log {} in because of encryption failure: {}", payload.getUsername(), e.getMessage());
            return null;
        }
        if(!mash.equals(staff.getMash())) {
            LOGGER.trace("Password mismatch {}", payload.getUsername());
            return null;
        }

        Restaurant restaurant = staff.getRestaurant();

        if(!restaurant.getId().equals(staff.getRestaurantId())) {
            LOGGER.trace("Staff/Restaurant ID mismatch {}", payload.getUsername());
            return null;
        }

        if(!restaurant.isEnabledForWaiter()) {
            LOGGER.trace("Restaurant not enabled for waiter - deny login {}", restaurant.getId());
            return null;
        }

        //return existing login if it exists
        try {
            StaffAuthentications existingLogin = this.staffAuthentications.get(staff.getId());
            if(existingLogin != null) {
                return new StaffView(staff, existingLogin.getAuthenticationKey());
            }
        } catch (ExecutionException e) {
            LOGGER.error("Could not get token from cache; create a new login (this will make other log-ins obsolete)", e.getMessage());
            return staffLogin(staff);
        }

        return staffLogin(staff);
    }

    public StaffView staffLogin(Staff staff) {
        StaffAuthentication staffAuthentication = new StaffAuthentication();
        staffAuthentication.setExpires(System.currentTimeMillis() + StaffAuthentications.AUTHORIZATION_TIME);
        staffAuthentication.setKey(createStaffToken(staff));
        staffAuthentication.setUsername(staff.getUserName());
        staffAuthentication.setStaffId(staff.getId());
        staffAuthentication.setRestaurantId(staff.getRestaurantId());

        StaffAuthentications auth = masterDataService.insertStaffAuthentication(staffAuthentication);
        staffAuthentications.put(staff.getId(), auth);

        return new StaffView(staff, staffAuthentication.getKey());
    }

    public String bookingWidgetLogin(String restaurantId) {
        if(!masterDataService.restaurantExistsByStaffId(restaurantId)) {
            return null;
        }

        BookingWidgetAuthentications auth = masterDataService.getBookingWidgetAuthentication(restaurantId);
        if(auth == null) {
            return createBookingWidgetLogin(restaurantId);
        } else {
            //extend life of auth
            masterDataService.extendBookingAuthenticationLife(auth);
            return auth.getToken();
        }
    }

    private String createBookingWidgetLogin(String restaurantId) {
        BookingWidgetAuthentications authentication = new BookingWidgetAuthentications();
        authentication.setCreatedTime(new Date());
        authentication.setRestaurantId(restaurantId);
        String mash = AuthenticationUtil.encrypt(BOOKING_WIDGET_BASE_STRING+System.currentTimeMillis(),restaurantId);
        authentication.setToken(mash);
        masterDataService.insertBookingWidgetAuthentications(authentication);

        return mash;
    }

    public boolean verifyBookingWidgetToken(String restaurantId, String token) {
        if(!masterDataService.restaurantExistsByStaffId(restaurantId)) {
            return false;
        }

        BookingWidgetAuthentications auth = masterDataService.getBookingWidgetAuthentication(restaurantId);
        return auth != null && token != null && token.equals(auth.getToken());
    }

    public String onlineOrderingLogin(String restaurantId, String publicToken, String[] ipAddresses) {
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        if(restaurant == null
                || restaurant.getIntegrations().get(ExternalIntegration.EPICURI_ONLINE_ORDERS) == null
                || !publicToken.equals(restaurant.getIntegrations().get(ExternalIntegration.EPICURI_ONLINE_ORDERS).getToken())) {
            LOGGER.trace("Restaurant not found or ExternalIntegration.EPICURI_ONLINE_ORDERS token doesn't match");
            return null;
        }

        if(restaurant.getOnlineOrderingIPAddresses().size() > 0) {
            LOGGER.trace("Checking IP Addresses");
            boolean found = false;
            OUTER:
            for (String ipAddress : ipAddresses) {
                for (String authorizedIP : restaurant.getOnlineOrderingIPAddresses()) {
                    if (ipAddress != null && authorizedIP != null && ipAddress.contains(authorizedIP)) {
                        found = true;
                        break OUTER;
                    }
                }
            }

            if (!found) {
                LOGGER.trace("IP Address check failed - returning null");
                return null;
            }
        }

        return createOnlineOrderAuthentication(restaurantId, publicToken);
    }

    public boolean verifyOnlineOrderingToken(String token) {
        if(token == null) {
            return false;
        }
        String[] bits = token.split(IDAble.SEPARATOR);
        return token.equals(createOnlineOrderAuthentication(bits[1], bits[2]));
    }

    private String createOnlineOrderAuthentication(String restaurantId, String publicToken) {
        return AuthenticationUtil.encrypt(ONLINE_ORDERING_BASE_STRING + publicToken,restaurantId) + IDAble.SEPARATOR + restaurantId + IDAble.SEPARATOR + publicToken ;
    }

    public CustomerCustomerView customerLogin(CustomerAuthPayload payload) {
        Customer customer = customerService.getCustomerByEmail(payload.getEmail());
        if(customer == null) {
            return null;
        }

        String passwordMash;
        try {
            String password = payload.getPassword();
            passwordMash = getCustomerPasswordMash(customer, password);
        } catch (BadStateException e) {
            return null;
        }

        if(!customer.getPasswordMash().equals(passwordMash)) {
            return null;
        }

        return createAuthAndLogin(customer);
    }

    public CustomerCustomerView createAuthAndLogin(Customer customer) {
        if(StringUtils.isBlank(customer.getAuthKey())) {
            String token = createCustomerToken(customer);
            customer.setAuthKey(token);
            customerService.setAuthKey(customer.getId(), token);
        }

        customerAuthentications.put(customer.getId(), customer.getAuthKey());

        return new CustomerCustomerView(customer);
    }

    public String getCustomerPasswordMash(Customer customer, String password) {
        return AuthenticationUtil.getPasswordMash(customer, password);
    }

    public String createCustomerToken(Customer customer) {
        return customer.getId() + IDAble.SEPARATOR + RandomStringUtils.randomAlphanumeric(8);
    }

    public String createStaffToken(Staff staff) {
        return staff.getId() + IDAble.SEPARATOR + staff.getRestaurantId() + IDAble.SEPARATOR + RandomStringUtils.randomAlphanumeric(64);
    }

    public String createAdminToken() {
        return "A" + IDAble.SEPARATOR + "_" + IDAble.SEPARATOR + RandomStringUtils.randomAlphanumeric(64);
    }

    public String getRestaurantId(String token) {
        if(StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Cannot process token");
        }
        String[] bits = token.split(IDAble.SEPARATOR);
        if(bits.length != 3) {
            throw new IllegalArgumentException("Cannot process token");
        }
        return bits[1];
    }

    public String getCustomerId(String token) {
        if(StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("Cannot process token");
        }
        token = stripOffBasic(token);
        String[] bits = token.split(IDAble.SEPARATOR);
        if(bits.length != 2) {
            throw new IllegalArgumentException("Cannot process token");
        }
        return bits[0];
    }

    public String getStaffId(String token) {
        if(StringUtils.isBlank(token)) {
            LOGGER.trace("Token is empty");
            throw new IllegalArgumentException("Cannot process token");
        }
        token = stripOffBasic(token);
        String[] bits = token.split(IDAble.SEPARATOR);
        if(bits.length != 3) {
            LOGGER.trace("Token is not length 3");
            throw new IllegalArgumentException("Cannot process token");
        }
        return bits[0];
    }

    public static String stripOffBasic(String token) {
        if(!token.startsWith("Basic ")) {
            return token;
        } else {
            return token.substring(6);
        }
    }

    public boolean verifyAdminToken(String token) throws Exception {
        return currentAdminToken != null && token != null && token.equalsIgnoreCase(currentAdminToken);
    }

    public boolean verifyStaffToken(String token) throws Exception {
        String staffId = null;
        try {
            staffId = getStaffId(token);
        } catch (IllegalArgumentException ex) {
            LOGGER.trace("No staff ID associated with token {}", token);
            return false;
        }
        String restaurantId = null;

        try {
            restaurantId = getRestaurantId(token);
        } catch (IllegalArgumentException ex) {
            LOGGER.trace("Token {} does not contain restaurant ID", token);
            return false;
        }

        StaffAuthentications authentication = null;

        try {
            authentication = staffAuthentications.get(staffId);
        } catch (CacheLoader.InvalidCacheLoadException ex) {
            LOGGER.error("Could not get data from cache", ex);
            return false;
        } catch (Exception ex) {
            LOGGER.error("Staff authentication not found in database", ex);
            return false;
        }

        if(authentication != null
            && authentication.getStaffId().equalsIgnoreCase(staffId)
            && authentication.getRestaurantId() != null
            && authentication.getRestaurantId().equalsIgnoreCase(restaurantId)
            && authentication.getAuthenticationKey().equals(token)) {
            return true;
        }
        return false;
    }

    public boolean verifyCustomerToken(String token) throws Exception {
        String customerId;
        try {
            customerId = getCustomerId(token);
        }catch (IllegalArgumentException e){
            return false;
        }
        String savedToken = null;

        try {
            savedToken = customerAuthentications.get(customerId);
        } catch (CacheLoader.InvalidCacheLoadException ex) {
            //squelch
            LOGGER.trace("Captured error: " + ex.getMessage());
        }catch (Exception ex) {
            LOGGER.error("Customer authentication not found in database", ex);
        }

        if(StringUtils.isNotBlank(savedToken)
                && savedToken.equals(token)) {
            return true;
        }

        return false;
    }

    public StaffRole getStaffSecurityLevel(String token) throws Exception {
        String staffId;
        try {
            staffId = getStaffId(token);
        } catch (IllegalArgumentException ex) {
            return StaffRole.UNKNOWN;
        }
        StaffAuthentications authentication = staffAuthentications.get(staffId);
        return masterDataService.getStaffRole(authentication.getStaffId());
    }

    public void updateAuditLog(String ip,
                               String api,
                               String mac,
                               String ssid,
                               String restaurantId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setIp(ip);
        auditLog.setApiVersion(api);
        auditLog.setMac(mac);
        auditLog.setSsid(ssid);
        auditLog.setRestaurantId(restaurantId);
        auditLog.setTime(System.currentTimeMillis());

        auditLogRepository.save(auditLog);
    }

    private class StaffKeyLoader extends CacheLoader<String,StaffAuthentications> {
        @Override
        public StaffAuthentications load(String staffId) throws Exception {
            StaffAuthentications authentication = masterDataService.getAuthentication(staffId);
            if(authentication == null) {
                throw new Exception("Authentication not found for staff ID " + staffId);
            }
            return authentication;
        }
    }

    private class CustomerKeyLoader extends CacheLoader<String,String> {
        @Override
        public String load(String customerId) throws Exception {
            Customer customer = customerService.getCustomer(customerId);
            if(customer == null) {
                throw new Exception("Authentication not found for customer ID " + customerId);
            }
            return customer.getAuthKey();
        }
    }
}
