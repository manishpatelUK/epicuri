package uk.co.epicuri.serverapi.service.util;

import java.util.Map;

/**
 * Created by manish on 26/06/2017.
 */
public class MapUtil {
    public static <T> void update(Map<T,Integer> map, T type, int amount) {
        if(map.containsKey(type)) {
            map.compute(type, (k, v) -> v+amount);
        } else {
            map.put(type, amount);
        }
    }
}
