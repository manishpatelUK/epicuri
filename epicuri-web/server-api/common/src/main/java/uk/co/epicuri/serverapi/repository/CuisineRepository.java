package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Cuisine;

/**
 * Created by manish
 */
@Repository
public interface CuisineRepository extends MongoRepository<Cuisine, String>{
    Cuisine findByName(String name);
}
