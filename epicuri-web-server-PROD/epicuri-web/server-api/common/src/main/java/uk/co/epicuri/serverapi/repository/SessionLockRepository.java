package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionLock;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionLockType;

@Repository
public interface SessionLockRepository extends MongoRepository<SessionLock, String> {
    SessionLock findByExternalIdAndSessionLockType(String externalId, SessionLockType sessionLockType);
}
