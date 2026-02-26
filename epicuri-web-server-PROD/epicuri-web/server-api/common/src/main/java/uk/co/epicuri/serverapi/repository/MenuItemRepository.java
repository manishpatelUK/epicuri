package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface MenuItemRepository extends MongoRepository<MenuItem, String>, CustomMenuItemRepository {
    List<MenuItem> findByRestaurantId(String restaurantId);
    List<MenuItem> findByRestaurantIdAndPlu(String restaurantId, String plu);
    List<MenuItem> findByRestaurantIdAndPluIn(String restaurantId, List<String> plu);
}
