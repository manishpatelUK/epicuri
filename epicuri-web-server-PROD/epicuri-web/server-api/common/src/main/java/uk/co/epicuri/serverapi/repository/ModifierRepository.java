package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;

/**
 * Created by manish
 */
@Repository
public interface ModifierRepository extends MongoRepository<Modifier, String> {

}
