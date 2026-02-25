package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.internal.AuditLog;

import java.util.List;

/**
 * Created by manish.
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog,String> {
    List<AuditLog> findByRestaurantId(String restaurantId);
}
