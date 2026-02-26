package uk.co.epicuri.serverapi.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import uk.co.epicuri.serverapi.BadStateException;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;

public class AuthenticationUtil {

    public static String encrypt(String baseString, String keyString)
    {
        return encrypt(baseString + ":" + keyString + ":" + baseString);
    }

    public static String encrypt(String string) {
        return DigestUtils.md5DigestAsHex(string.getBytes());
    }

    public static String getPasswordMash(String identifier, String password) throws BadStateException {
        return encrypt(identifier,password);
    }

    public static String getPasswordMash(Customer customer, String password) throws BadStateException {
        if(StringUtils.isBlank(customer.getEmail())) {
            throw new BadStateException("Email address is null or invalid");
        }
        return encrypt(customer.getEmail(),password);
    }

    public static String getPasswordMash(Staff staff, String password) throws BadStateException {
        return getPasswordMash(staff.getUserName(), password);
    }
}
