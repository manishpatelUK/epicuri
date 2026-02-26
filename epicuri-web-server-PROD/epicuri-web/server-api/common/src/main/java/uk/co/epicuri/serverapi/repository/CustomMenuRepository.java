package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;

/**
 * Created by manish
 */
@Repository
public interface CustomMenuRepository extends DeletableRepository{
    void push(String menuId, Category category);
    void updateModifiedTime(String menuId, long time);
}
