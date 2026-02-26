package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Default;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;

import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class DefaultsRepositoryTest extends BaseIT {

    @Test
    public void testFindByName() throws Exception {
        Default aDefault = defaultsRepository.findByName(FixedDefaults.DEFAULT_TIP_PERCENTAGE);
        assertNotNull(aDefault);
    }

    @Test
    public void testTypes() throws Exception {
        Default aDefault = new Default();
        aDefault.setDescription("foo");
        aDefault.setMeasure("bar");
        aDefault.setName("man");
        aDefault.setValue("chu");

        aDefault = defaultsRepository.save(aDefault);
        assertTrue(defaultsRepository.findOne(aDefault.getId()).getValue().getClass() == String.class);

        defaultsRepository.deleteAll();

        aDefault = new Default();
        aDefault.setDescription("foo");
        aDefault.setMeasure("bar");
        aDefault.setName("man");
        aDefault.setValue(2d);

        aDefault = defaultsRepository.save(aDefault);
        assertTrue(defaultsRepository.findOne(aDefault.getId()).getValue().getClass() == Double.class);

        defaultsRepository.deleteAll();

        aDefault = new Default();
        aDefault.setDescription("foo");
        aDefault.setMeasure("bar");
        aDefault.setName("man");
        aDefault.setValue(2);

        aDefault = defaultsRepository.save(aDefault);
        assertTrue(defaultsRepository.findOne(aDefault.getId()).getValue().getClass() == Integer.class);
    }
}