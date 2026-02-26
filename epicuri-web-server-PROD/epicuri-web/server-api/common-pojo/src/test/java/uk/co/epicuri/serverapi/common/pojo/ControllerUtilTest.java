package uk.co.epicuri.serverapi.common.pojo;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class ControllerUtilTest {

    @Test
    public void testNextRandom() throws Exception {
        List<String> current = new ArrayList<>();

        for(int i = 0; i < 1000; i++) {
            current.add(ControllerUtil.nextRandom(current, 64));
        }

        assertEquals(current.size(), new HashSet<>(current).size());
        assertTrue(current.stream().noneMatch(StringUtils::isBlank));
    }
}