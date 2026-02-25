package uk.co.epicuri.serverapi.repository;

import com.mongodb.BasicDBObjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.ChairData;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public class SessionRepositoryImpl implements CustomSessionRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public List<Session> findByStartTime(String restaurantId, long start, long end) {
        Query query = Query.query(
                Criteria.where("restaurantId").is(restaurantId)
                        .andOperator(
                                Criteria.where("startTime").gte(start),
                                Criteria.where("startTime").lte(end)));

        return operations.find(query, Session.class);
    }

    @Override
    public List<Session> findByCloseTime(String restaurantId, long start, long end) {
        Query query = Query.query(
                Criteria.where("restaurantId").is(restaurantId)
                        .andOperator(
                                Criteria.where("closedTime").gte(start),
                                Criteria.where("closedTime").lte(end)));

        return operations.find(query, Session.class);
    }

    @Override
    public List<Session> findTakeawaySessions(String restaurantId, List<String> bookingIds) {
        Query query = Query.query(Criteria.where("restaurantId")
                .is(restaurantId)
                .and("originalBookingId").in(bookingIds)
                .andOperator(
                        Criteria.where("takeawayType").exists(true),
                        Criteria.where("takeawayType").ne(TakeawayType.NONE)
                ));


        return operations.find(query, Session.class);
    }

    @Override
    public List<Session> findCurrentLiveSessions(String restaurantId, long startTimeLimit) {
        Query query = Query.query(Criteria.where("restaurantId")
                .is(restaurantId)
                .and("startTime").lte(startTimeLimit)
                .and("closedTime").exists(false)
                .and("deleted").exists(false));

        return operations.find(query, Session.class);
    }

    @Override
    public List<Session> findCurrentSeatedSessions(String restaurantId) {
        Query query = Query.query(Criteria.where("restaurantId")
                .is(restaurantId)
                .and("sessionType").is(SessionType.SEATED)
                .and("closedTime").exists(false)
                .and("deleted").exists(false));

        return operations.find(query, Session.class);
    }

    @Override
    public void pushDiners(String sessionId, List<Diner> diners) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                new Update().addToSet("diners", BasicDBObjectBuilder.start("$each", diners).get()),Session.class);
    }

    @Override
    public void setService(String sessionId, Service service) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("service", service),Session.class);
    }

    @Override
    public void setName(String sessionId, String name) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("name", name),Session.class);
    }

    @Override
    public void pushTables(String sessionId, List<String> tables) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                new Update().addToSet("tables", BasicDBObjectBuilder.start("$each",tables).get()),Session.class);
    }

    @Override
    public void setTables(String sessionId, List<String> tables) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("tables", tables),Session.class);
    }

    @Override
    public void setBillRequested(String sessionId, boolean billRequested) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("billRequested", billRequested),Session.class);
    }

    @Override
    public void setRemoveFromReports(String sessionId, boolean remove) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("removeFromReports", remove),Session.class);
    }

    @Override
    public void setClosed(String sessionId, Long time) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("closedTime", time),Session.class);
    }

    @Override
    public void setClosedBy(String sessionId, String staffId) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("closedBy", staffId),Session.class);
    }

    @Override
    public void setStartTime(String sessionId, long time) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("startTime", time),Session.class);
    }

    @Override
    public void setVoid(String sessionId, VoidReason reason) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("voidReason", reason),Session.class);
    }

    @Override
    public void setChairData(String sessionId, List<ChairData> data) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("chairData", data),Session.class);
    }

    @Override
    public void setTipPercentage(String sessionId, Double tip) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("tipPercentage", tip),Session.class);
    }

    @Override
    public void setCalculatedDeliveryCost(String sessionId, Integer cost) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("calculatedDeliveryCost", cost),Session.class);
    }

    @Override
    public void setSessionType(String sessionId, SessionType type) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("sessionType", type),Session.class);
    }

    @Override
    public void updateDiner(String sessionId, Diner diner) {
        Query query = Query.query(Criteria.where("_id").is(sessionId).and("diners.id").is(diner.getId()));
        operations.updateFirst(query, Update.update("diners.$", diner), Session.class);
    }

    @Override
    public void pushDiner(String sessionId, Diner diner) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                new Update().addToSet("diners", diner),Session.class);
    }

    @Override
    public void removeDiner(String sessionId, String dinerId) {
        Query query = Query.query(Criteria.where("_id").is(sessionId));
        operations.updateFirst(query, new Update().pull("diners",Query.query(Criteria.where("id").is(dinerId))), Session.class);
    }

    @Override
    public void pushAdjustment(String sessionId, Adjustment adjustment) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                new Update().addToSet("adjustments", adjustment),Session.class);
    }

    @Override
    public void pushAdjustments(String sessionId, List<Adjustment> adjustments) {
        Object[] array = new Object[adjustments.size()];
        for(int i = 0; i < adjustments.size(); i++) {
            array[i] = adjustments.get(i);
        }
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                new Update().pushAll("adjustments", array),Session.class);
    }

    @Override
    public void removeAdjustment(String sessionId, String adjustmentId) {
        Query query = Query.query(Criteria.where("_id").is(sessionId));
        operations.updateFirst(query, new Update().pull("adjustments",Query.query(Criteria.where("id").is(adjustmentId))), Session.class);
    }

    @Override
    public void setDelay(String sessionId, long delay) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(sessionId)),
                Update.update("delay", delay),Session.class);
    }

    @Override
    public void incrementDelay(String sessionId, long increment) {
        Query query = new Query(Criteria.where("_id").is(sessionId));
        Update update = new Update();
        update.inc("delay", increment);
        operations.findAndModify(query, update, Session.class);
    }

    @Override
    public void updateCashUpId(List<String> sessionIds, String cashUpId) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(sessionIds)),
                Update.update("cashUpId", cashUpId),Session.class);
    }

    @Override
    public void setPaid(String sessionId, boolean paid) {
        Query query = new Query(Criteria.where("_id").is(sessionId));
        Update update = new Update();
        update.set("markedAsPaid", paid);
        operations.findAndModify(query, update, Session.class);
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
