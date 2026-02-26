package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

import java.util.List;

/**
 * Created by manish
 */
public class DeletableRepositoryImpl {

    public static <T extends Deletable> void markDeleted(MongoOperations operations, String id, Class<T> clazz) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(id)),
                Update.update("deleted", System.currentTimeMillis()),clazz);
    }

    public static <T extends Deletable> void markDeleted(MongoOperations operations, List<String> ids, Class<T> clazz) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(ids)),
                Update.update("deleted", System.currentTimeMillis()),clazz);
    }

    public static <T extends Deletable> T findOneNotDeleted(MongoOperations operations, String id, Class<T> clazz) {
        return operations.findOne(Query.query(Criteria.where("_id").is(id)
                                    .and("deleted").exists(false)), clazz);
    }
}
