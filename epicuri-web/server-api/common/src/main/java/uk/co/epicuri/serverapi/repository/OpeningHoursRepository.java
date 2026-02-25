package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;

import java.util.List;

/**
 * Created by manish.
 */
@Repository
public interface OpeningHoursRepository extends MongoRepository<OpeningHours, String>{
    OpeningHours findByRestaurantIdAndBookingType(String restaurantId, BookingType bookingType);
    List<OpeningHours> findByRestaurantIdInAndBookingType(List<String> restaurantIds, BookingType bookingType);
    List<OpeningHours> findByRestaurantId(String restaurantId);
}
