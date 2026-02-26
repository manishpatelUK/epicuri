package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;

import java.util.List;

@Repository
public interface CustomStockLevelRepository extends DeletableRepository {
    void increment(String restaurantId, String plu, int increment);
}
