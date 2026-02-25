package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantImage;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantImageType;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface RestaurantImageRepository extends MongoRepository<RestaurantImage, String> {
    RestaurantImage findByRestaurantIdAndImageType(String restaurantId, RestaurantImageType imageType);
    List<RestaurantImage> findByRestaurantId(String restaurantId);
}
