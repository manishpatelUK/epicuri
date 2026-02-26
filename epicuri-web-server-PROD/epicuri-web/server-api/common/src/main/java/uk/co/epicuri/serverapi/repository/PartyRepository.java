package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface PartyRepository extends MongoRepository<Party, String>, CustomPartyRepository {
    List<Party> findByRestaurantId(String restaurantId);
    List<Party> findByRestaurantIdAndBookingIdIn(String restaurantId, List<String> bookingIds);
    Party findByRestaurantIdAndBookingId(String restaurantId, String bookingId);
}
