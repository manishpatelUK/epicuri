package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification,String> {
    List<Notification> findByRestaurantId(String restaurantId);
    List<Notification> findByRestaurantIdAndSessionId(String restaurantId, String sessionId);
}
