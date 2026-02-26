package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class RestaurantRepositoryTest extends BaseIT {

    @Test
    public void testFindByStaffFacingId() throws Exception {
        assert restaurant2.getStaffFacingId() != null;

        Restaurant restaurant = restaurantRepository.findByStaffFacingId(restaurant2.getStaffFacingId());

        assertNotNull(restaurant);
        assertEquals(restaurant2.getId(), restaurant.getId());
    }
}