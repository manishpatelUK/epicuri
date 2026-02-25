package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionArchive;

import java.util.List;

/**
 * Created by manish on 13/01/2018.
 */
@Repository
public class SessionArchiveRepositoryImpl implements CustomSessionArchiveRepository {
    @Autowired
    private MongoOperations operations;

    @Override
    public List<SessionArchive> findByRestaurantIdAndClosedTimeBetween(String restaurantId, long start, long end) {
        Query query = new Query();
        query.addCriteria(Criteria.where("restaurantId").is(restaurantId)
                            .andOperator(Criteria.where("closedTime").gte(start), Criteria.where("closedTime").lte(end)));

        return operations.find(query, SessionArchive.class);
    }
}
