package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public class OrderRepositoryImpl implements CustomOrderRepository {
    @Autowired
    private MongoOperations operations;

    @Override
    public void updateVoid(List<String> ids) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(ids)),
                Update.update("voided",true), Order.class);
    }

    @Override
    public void pushSelfServiceParameters(List<String> ids, String publicFacingId, String location) {
        Update update = new Update();
        update.set("publicFacingOrderId", publicFacingId);
        update.set("deliveryLocation", location);
        operations.updateMulti(Query.query(Criteria.where("_id").in(ids)),update, Order.class);
    }
}
