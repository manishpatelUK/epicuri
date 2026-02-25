package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface BatchRepository extends MongoRepository<Batch,String>, CustomBatchRepository {
    List<Batch> findBySessionIdIn(List<String> ids);
    List<Batch> findBySessionIdInAndIntendedPrintTimeLessThanEqual(List<String> ids, long time);
    List<Batch> findBySessionId(String id);
}
