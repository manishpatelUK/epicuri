package uk.co.epicuri.serverapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;

import java.util.List;

/**
 * Created by manish
 */
@Repository
public interface ModifierGroupRepository extends MongoRepository<ModifierGroup, String>, DeletableRepository {
    List<ModifierGroup> findByRestaurantId(String id);
}
