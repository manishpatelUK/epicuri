package uk.co.epicuri.serverapi.repository;

import de.flapdoodle.embed.process.collections.Collections;
import org.junit.Before;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class MenuRepositoryTest extends BaseIT {
    @Before
    public void setUp() throws Exception {
        super.setUp();

        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem3.setRestaurantId(restaurant3.getId());

        menuItemRepository.save(Collections.newArrayList(menuItem1, menuItem2, menuItem1));
    }

    @Test
    public void testGetByRestaurantId() throws Exception {
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurant1.getId());
        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(m -> m.getId().equals(menuItem1.getId())));
        assertTrue(items.stream().anyMatch(m -> m.getId().equals(menuItem2.getId())));
        assertFalse(items.stream().anyMatch(m -> m.getId().equals(menuItem3.getId())));
    }
}
