package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class MenuRepositoryImplTest extends BaseIT {

    @Test
    public void testPush() throws Exception {
        assert menu1.getCategories().size() == 0;

        menuRepository.push(menu1.getId(), category1);
        Menu menu = menuRepository.findOne(menu1.getId());

        assertTrue(menu.getCategories().size() == 1);
        assertEquals(menu.getCategories().get(0).getName(), category1.getName());

        menu = menuRepository.findOne(menu2.getId());
        assertTrue(menu.getCategories().size() == 0);
    }

    @Test
    public void testUpdateModifiedTime() throws Exception {
        long modifiedTime = menu1.getLastUpdate();

        menuRepository.updateModifiedTime(menu1.getId(),System.currentTimeMillis());
        Menu menu1 = menuRepository.findOne(this.menu1.getId());
        assertTrue(modifiedTime < menu1.getLastUpdate());

        Menu menu2 = menuRepository.findOne(this.menu2.getId());
        assertNotEquals(menu1.getLastUpdate(), menu2.getLastUpdate());
    }
}