package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface MenuRepository extends MongoRepository<Menu, String>, CustomMenuRepository {
    List<Menu> findByRestaurantId(String id);
}
