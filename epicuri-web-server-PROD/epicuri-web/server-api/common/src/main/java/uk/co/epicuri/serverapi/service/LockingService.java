package uk.co.epicuri.serverapi.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionLock;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionLockType;
import uk.co.epicuri.serverapi.errors.LockStateException;
import uk.co.epicuri.serverapi.repository.SessionLockRepository;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LockingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockingService.class);

    private final LoadingCache<Tuple<String,SessionLockType>, SessionLock> sessionLocksByExternalIdAndType;

    @Autowired
    private SessionLockRepository sessionLockRepository;

    public LockingService() {
        sessionLocksByExternalIdAndType = CacheBuilder.newBuilder()
                .expireAfterWrite(5100, TimeUnit.MILLISECONDS)
                .maximumSize(1000)
                .build(new SessionLockLoader());
    }

    public synchronized SessionLock lock(String id, SessionLockType type) throws LockStateException{
        try {
            SessionLock sessionLock = sessionLocksByExternalIdAndType.get(Tuple.create(id, type));
            if(sessionLock != null) {
                throw new LockStateException();
            }
        } catch (ExecutionException e) {}

        LOGGER.trace("Lock {} on type {}", id, type);
        SessionLock sessionLock = sessionLockRepository.insert(new SessionLock(id, type));
        sessionLocksByExternalIdAndType.put(Tuple.create(id, type), sessionLock);
        return sessionLock;
    }

    public synchronized void release(SessionLock sessionLock) {
        LOGGER.trace("Unlock {} on type {}", sessionLock.getExternalId(), sessionLock.getSessionLockType());
        sessionLocksByExternalIdAndType.invalidate(Tuple.create(sessionLock.getExternalId(), sessionLock.getSessionLockType()));
        sessionLockRepository.delete(sessionLock.getId());
    }

    private class SessionLockLoader extends CacheLoader<Tuple<String,SessionLockType>, SessionLock> {

        @Override
        public SessionLock load(Tuple<String, SessionLockType> stringSessionLockTypeTuple) throws Exception {
            SessionLock sessionLock = sessionLockRepository.findByExternalIdAndSessionLockType(stringSessionLockTypeTuple.getA(), stringSessionLockTypeTuple.getB());
            if(sessionLock != null) {
                return sessionLock;
            } else {
                throw new LockStateException("entry is null");
            }
        }
    }
}
