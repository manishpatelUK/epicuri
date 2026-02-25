package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface BookingRepository extends MongoRepository<Booking, String>, CustomBookingRepository {
    List<Booking> findByRestaurantId(String id);
}
