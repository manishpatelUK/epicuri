package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;

/**
 * Created by manish
 */
@Repository
public class PartyRepositoryImpl implements CustomPartyRepository {

    @Autowired
    MongoOperations operations;

    @Override
    public void updateCustomerId(String id, String customerId) {
        Criteria criteria = Criteria.where("_id").is(id);
        operations.updateFirst(Query.query(criteria), Update.update("customerId", customerId), Party.class);
    }
}
