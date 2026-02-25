package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;

import java.util.List;

@Repository
public interface StockLevelRepository extends MongoRepository<StockLevel, String>, CustomStockLevelRepository {
    StockLevel findByRestaurantIdAndPlu(String restaurantId, String plu);
    List<StockLevel> findByRestaurantIdAndPluIn(String restaurantId, List<String> plu);
    List<StockLevel> findByRestaurantId(String restaurantId);
}
