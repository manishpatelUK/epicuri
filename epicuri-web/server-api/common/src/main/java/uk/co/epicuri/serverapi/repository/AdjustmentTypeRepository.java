package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;

/**
 * Created by manish
 */
@Repository
public interface AdjustmentTypeRepository extends MongoRepository<AdjustmentType, String>{
    AdjustmentType findByName(String name);
}
