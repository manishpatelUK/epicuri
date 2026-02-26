package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionArchive;

import java.util.List;

@Repository
public interface SessionArchiveRepository extends MongoRepository<SessionArchive,String>, CustomSessionArchiveRepository {
    SessionArchive findBySessionId(String sessionId);
    List<SessionArchive> findBySessionIdIn(Iterable<String> sessionIds);
    List<SessionArchive> findByRestaurantId(String restaurantId);
}
