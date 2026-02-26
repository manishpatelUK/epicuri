package uk.co.epicuri.serverapi.repository;

import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionNumber;

/**
 * Created by manish on 23/05/2017.
 */
@Repository
public class SessionNumberRepositoryImpl implements CustomSessionNumberRepository {
    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public SessionNumber incrementAndGet(String restaurantId) {
        Query query = new Query(Criteria.where("restaurantId").is(restaurantId));
        Update update = new Update().inc("totalSessionsCreated", 1);
        SessionNumber sessionNumber = mongoOperations.findAndModify(query, update, SessionNumber.class);

        if(sessionNumber == null) {
            SessionNumber newSessionNumber = new SessionNumber();
            newSessionNumber.setRestaurantId(restaurantId);
            newSessionNumber.setTotalSessionsCreated(1);
            mongoOperations.insert(newSessionNumber);
            return newSessionNumber;
        }

        sessionNumber.setTotalSessionsCreated(sessionNumber.getTotalSessionsCreated()+1);
        return sessionNumber;
    }
}
