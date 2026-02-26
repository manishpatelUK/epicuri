package uk.co.epicuri.serverapi.repository;

import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Layout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;

import java.util.List;
import java.util.Map;

/**
 * Created by manish.
 */
@Repository
public interface CustomRestaurantRepository {
    List<Restaurant> searchByGeography(Double trLat, Double trLong,
                                       Double blLat, Double blLong,
                                       String cuisineId,
                                       Boolean hasTakeaway,
                                       String name);

    void addLayout(String restaurantId, String floorId, Layout layout);
    void updateLayout(String restaurantId, String floorId, Layout layout);
    void setSelectedLayout(String restaurantId, String floorId, String layoutId);
    void setTakeawayMenuId(String restaurantId, String menuId);
    void updateTables(String restaurantId, List<Table> tables);
    void updateTable(String restaurantId, Table table);
    void pushTable(String restaurantId, Table table);
    void pullTable(String restaurantId, String tableId);
    void updateIntegrations(String restaurantId, Map<ExternalIntegration, KVData> integrations);
}
