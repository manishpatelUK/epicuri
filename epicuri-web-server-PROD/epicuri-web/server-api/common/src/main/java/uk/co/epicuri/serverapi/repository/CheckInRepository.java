package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface CheckInRepository extends MongoRepository<CheckIn, String>, CustomCheckInRepository {
    List<CheckIn> findByCustomerIdAndDeletedNull(String customerId);
    List<CheckIn> findByRestaurantIdAndTimeGreaterThanEqual(String restaurantId, long time);
    List<CheckIn> findByRestaurantIdAndBookingIdIn(String restaurantId, List<String> bookingId);
    List<CheckIn> findByRestaurantId(String restaurantId);
    List<CheckIn> findByTimeBefore(long time);
}
