package uk.co.epicuri.serverapi.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

import java.util.List;

/**
 * Created by Manish Patel
 */
@Repository
public class ModifierGroupRepositoryImpl implements CustomModifierGroupRepository {

    @Autowired
    private MongoOperations operations;

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
