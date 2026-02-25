package uk.co.epicuri.serverapi.repository;

import org.springframework.data.repository.NoRepositoryBean;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

import java.util.List;

/**
 * Created by manish
 */
@NoRepositoryBean
//@Repository
public interface DeletableRepository {
    <T extends Deletable> void markDeleted(String id, Class<T> clazz);
    <T extends Deletable> void markDeleted(List<String> ids, Class<T> clazz);
    <T extends Deletable> T findOneNotDeleted(String id, Class<T> clazz);
}
