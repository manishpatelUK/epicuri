package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;

import java.util.List;

@Repository
public class StockLevelRepositoryImpl implements CustomStockLevelRepository {
    @Autowired
    private MongoOperations operations;

    @Override
    public void increment(String restaurantId, String plu, int increment) {
        Query query = new Query();
        query.addCriteria(Criteria.where("restaurantId").is(restaurantId));
        query.addCriteria(Criteria.where("plu").is(plu));
        StockLevel stockLevel = operations.findOne(query, StockLevel.class);
        if(stockLevel == null) {
            stockLevel = new StockLevel();
            stockLevel.setRestaurantId(restaurantId);
            stockLevel.setPlu(plu);
        }
        stockLevel.setLevel(stockLevel.getLevel() + increment);
        operations.save(stockLevel);
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

    private void updateStockLevels(int increment, Query query) {
        Update update = new Update();
        update.inc("level", increment);
        operations.findAndModify(query, update, StockLevel.class);
    }
}
