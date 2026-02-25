package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteraction;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;

import java.util.List;

public class CustomerInteractionRepositoryImpl implements CustomCustomerInteractionRepository {
    @Autowired
    private MongoOperations operations;

    @Override
    public void setPaid(String id, String settlementSessionId, boolean paid) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update();
        update.set("paid", paid);
        update.set("settlementSessionId", settlementSessionId);
        operations.updateFirst(Query.query(criteria), update, CustomerInteraction.class);
    }

    @Override
    public List<CustomerInteractionDeferredSession> findByRestaurantIdAndPaid(String restaurantId, boolean paid) {
        Query query = Query.query(Criteria.where("restaurantId").is(restaurantId).and("paid").is(paid));
        return operations.find(query, CustomerInteractionDeferredSession.class);
    }

    @Override
    public CustomerInteractionDeferredSession findByRestaurantIdAndSessionId(String restaurantId, String sessionId) {
        Query query = Query.query(Criteria.where("restaurantId").is(restaurantId).and("sessionId").is(sessionId));
        return operations.findOne(query, CustomerInteractionDeferredSession.class);
    }
}
