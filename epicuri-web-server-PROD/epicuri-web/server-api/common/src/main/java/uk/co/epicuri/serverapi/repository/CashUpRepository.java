package uk.co.epicuri.serverapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface CashUpRepository extends MongoRepository<CashUp,String> {
    @Query("{'restaurantId' : '?0'}")
    Page<CashUp> findLastCashUp(String restaurantId, Pageable pageable); //http://stackoverflow.com/questions/10067169/query-with-sort-and-limit-in-spring-repository-interface

    List<CashUp> findByRestaurantIdAndEndTimeGreaterThanEqual(String restaurantId, long endTimeMin);
    List<CashUp> findByRestaurantIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(String restaurantId, long startTime, long endTime);
    List<CashUp> findByRestaurantId(String restaurantId);
}
