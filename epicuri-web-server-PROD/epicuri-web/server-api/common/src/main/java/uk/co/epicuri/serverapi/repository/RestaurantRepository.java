package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface RestaurantRepository extends MongoRepository<Restaurant, String>, CustomRestaurantRepository{
    Restaurant findByStaffFacingId(String staffFacingId);
    List<Restaurant> findByIdIn(List<String> ids);
}
