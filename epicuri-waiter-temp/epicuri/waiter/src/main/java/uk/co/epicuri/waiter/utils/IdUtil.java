package uk.co.epicuri.waiter.utils;

/**
 * Created by manish on 10/01/2018.
 */

public class IdUtil {
    public static int getIntId(String id) {
        if(id == null) {
            return 0;
        } else {
            return Math.abs(id.hashCode());
        }
    }
}
