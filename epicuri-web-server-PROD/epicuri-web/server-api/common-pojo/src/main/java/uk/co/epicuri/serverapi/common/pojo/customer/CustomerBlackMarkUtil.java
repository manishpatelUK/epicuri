package uk.co.epicuri.serverapi.common.pojo.customer;

import uk.co.epicuri.serverapi.common.pojo.model.Customer;

import java.util.stream.Collectors;

/**
 * Created by manish
 */
public class CustomerBlackMarkUtil {
    public static final long BLACK_MARK_EXPIRY = 1000L * 60 * 60 * 24 * 30;

    public static boolean exceedsBlackMarks(Customer customer) {
        return customer != null
                && customer.getBlackMarks().size() != 0
                && customer.getBlackMarks().stream().filter(b -> (b.getTime() + BLACK_MARK_EXPIRY) > System.currentTimeMillis()).collect(Collectors.toList()).size() >= 3;
    }
}
