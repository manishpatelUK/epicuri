package uk.co.epicuri.serverapi.common.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by manish
 */
public class ControllerUtil {
    public static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String nextRandom(List<String> current) {
        return nextRandom(current, 4);
    }

    public static String nextRandom(List<String> current, int var) {
        while(true) {
            String random = nextRandom(var);
            if(!current.contains(random)) {
                return random;
            }
        }
    }

    public static String nextRandom(int var) {
        return RandomStringUtils.randomAlphanumeric(var);
    }
}
