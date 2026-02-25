package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;

import java.util.List;

/**
 * Created by manish.
 */
@Repository
public class BatchRepositoryImpl implements CustomBatchRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public void pushSpoolTime(List<String> batchIds, long time) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(batchIds)),
                new Update().addToSet("spoolTime", time), Batch.class);
    }

    @Override
    public void spoolForBatchPrinting(List<String> batchIds, long time) {
        Update update = new Update();
        update.addToSet("spoolTime", time);
        update.unset("printedTime");
        update.set("awaitingImmediatePrint", false);
        operations.updateMulti(Query.query(Criteria.where("_id").in(batchIds)),
                update, Batch.class);
    }

    @Override
    public void setPrintedTime(List<String> batchIds, long time) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(batchIds)),
                Update.update("printedTime", time), Batch.class);
    }

    @Override
    public void pushTimeToPrint(List<String> batchIds, long time) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(batchIds).and("printedTime").exists(false)),
                Update.update("intendedPrintTime", time), Batch.class);
    }

    @Override
    public void setImmediatePrintFlag(List<String> batchIds, boolean flag) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(batchIds)),
                Update.update("awaitingImmediatePrint", flag), Batch.class);
    }

    @Override
    public void setPrinterId(List<String> batchIds, String printerId) {
        operations.updateMulti(Query.query(Criteria.where("_id").in(batchIds)),
                Update.update("printerId", printerId), Batch.class);
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
