package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface SessionRepository extends MongoRepository<Session,String>, CustomSessionRepository{
    List<Session> findByRestaurantIdAndOriginalBookingIdIn(String restaurantId, List<String> bookingIds);
    List<Session> findByRestaurantId(String restaurantId);
    List<Session> findByOriginalBookingIdIn(List<String> bookingIds);
    Session findByOriginalBookingId(String bookingId);
    Session findByOriginalPartyId(String partyId);
    List<Session> findByOriginalPartyIdIn(List<String> partyId);
}
