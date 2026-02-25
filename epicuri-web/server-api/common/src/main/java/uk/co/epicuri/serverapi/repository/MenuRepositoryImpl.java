package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public class MenuRepositoryImpl implements CustomMenuRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public void push(String menuId, Category category) {
        operations.updateFirst(Query.query(Criteria.where("_id").in(menuId)),
                new Update().addToSet("categories", category), Menu.class);
    }

    @Override
    public void updateModifiedTime(String menuId, long time) {
        operations.updateFirst(Query.query(Criteria.where("_id").in(menuId)),
                Update.update("lastUpdate", time), Menu.class);
    }

    @Override
    public <T extends Deletable> void markDeleted(String id, Class<T> clazz) {
        DeletableRepositoryImpl.markDeleted(operations, id, clazz);
    }

    @Override
    public <T extends Deletable> void markDeleted(List<String> ids, Class<T> clazz) {
        DeletableRepositoryImpl.markDeleted(operations, ids, clazz);
    }

    @Override
    public <T extends Deletable> T findOneNotDeleted(String id, Class<T> clazz) {
        return DeletableRepositoryImpl.findOneNotDeleted(operations, id, clazz);
    }
}
