package uk.co.epicuri.serverapi.common.pojo.model.session;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.db.TableNames;

import java.util.Date;

@Document(collection = TableNames.SESSION_LOCK)
public class SessionLock extends IDAble {
    @Transient
    private transient static final int EXPIRATION_TIME_SECONDS = 5;

    @Indexed(expireAfterSeconds = EXPIRATION_TIME_SECONDS)
    private Date createdTime = new Date();

    @Indexed
    private String externalId;

    private SessionLockType sessionLockType;

    public SessionLock() {}
    public SessionLock(String externalId, SessionLockType sessionLockType) {
        this.externalId = externalId;
        this.sessionLockType = sessionLockType;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public SessionLockType getSessionLockType() {
        return sessionLockType;
    }

    public void setSessionLockType(SessionLockType sessionLockType) {
        this.sessionLockType = sessionLockType;
    }
}
