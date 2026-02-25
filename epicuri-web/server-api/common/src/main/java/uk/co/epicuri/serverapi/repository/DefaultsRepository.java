package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Default;

/**
 * Created by manish
 */
@Repository
public interface DefaultsRepository extends MongoRepository<Default, String>{
    Default findByName(String name);
}
