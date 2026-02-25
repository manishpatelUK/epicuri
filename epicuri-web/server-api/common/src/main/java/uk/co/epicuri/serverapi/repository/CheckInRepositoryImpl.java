package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public class CheckInRepositoryImpl implements CustomCheckInRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public void updatePartyId(String id, String partyId) {
        Criteria criteria = Criteria.where("_id").is(id);
        operations.updateFirst(Query.query(criteria), Update.update("partyId", partyId), CheckIn.class);
    }

    @Override
    public void updateSessionId(String id, String sessionId) {
        Criteria criteria = Criteria.where("_id").is(id);
        operations.updateFirst(Query.query(criteria), Update.update("sessionId", sessionId), CheckIn.class);
    }

    @Override
    public void updateSessionIdAndPartyId(String id, String sessionId, String partyId) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("sessionId", sessionId);
        update.set("partyId", partyId);
        operations.updateFirst(Query.query(criteria), update, CheckIn.class);
    }

    @Override
    public void updateCustomerId(String id, String customerId) {
        Criteria criteria = Criteria.where("_id").is(id);
        operations.updateFirst(Query.query(criteria), Update.update("customerId", customerId), CheckIn.class);
    }

    @Override
    public <T extends Deletable> void markDeleted(String id, Class<T> clazz) {
        DeletableRepositoryImpl.markDeleted(operations,id,clazz);
    }

    @Override
    public <T extends Deletable> void markDeleted(List<String> ids, Class<T> clazz) {
        DeletableRepositoryImpl.markDeleted(operations,ids,clazz);
    }

    @Override
    public <T extends Deletable> T findOneNotDeleted(String id, Class<T> clazz) {
        return DeletableRepositoryImpl.findOneNotDeleted(operations,id,clazz);
    }
}
