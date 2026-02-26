package uk.co.epicuri.serverapi.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.model.TakeawayOfferingType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Floor;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Layout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by manish.
 */
@Repository
public class RestaurantRepositoryImpl implements CustomRestaurantRepository {

    @Autowired
    private MongoOperations operations;

    @Override
    public List<Restaurant> searchByGeography(Double trLat, Double trLong, Double blLat, Double blLong, String cuisineId, Boolean hasTakeaway, String name) {
        Criteria criteria = new Criteria();

        if(trLat != null && trLong != null && blLat != null && blLong != null) {
            Point bottomLeft = new Point(blLong, blLat);
            Point topLeft = new Point(blLong, trLat);
            Point topRight = new Point(trLong, trLat);
            Point bottomRight = new Point(trLong, blLat);
            Polygon polygon = new Polygon(bottomLeft, topLeft, topRight, bottomRight);

            criteria = criteria.and("position.position").within(polygon);
        }
        if(cuisineId != null) {
            criteria = criteria.and("cuisineId").is(cuisineId);
        }
        if(hasTakeaway != null && hasTakeaway) {
            criteria = criteria.and("takeawayOffered").ne(TakeawayOfferingType.NOT_OFFERED);
        }
        if(StringUtils.isNotBlank(name)) {
            criteria = criteria.and("name").regex(".*" + name + ".*", "i");
        }
        return operations.find(new Query(criteria), Restaurant.class);
    }

    @Override
    public void addLayout(String restaurantId, String floorId, Layout layout) {
        Criteria criteria = Criteria.where("_id").is(restaurantId)
                .and("floors.id").is(floorId);
        operations.updateFirst(Query.query(criteria),
                new Update().addToSet("floors.$.layouts", layout), Restaurant.class);
    }

    @Override
    public void updateLayout(String restaurantId, String floorId, Layout layout) {
        // cannot update document nested in 2 arrays without knowing position... easier to pull the whole thing out
        Criteria criteria = Criteria.where("_id").is(restaurantId);
        Restaurant restaurant = operations.findOne(Query.query(criteria), Restaurant.class);
        Floor floor = restaurant.getFloors().stream().filter(f -> f.getId().equals(floorId)).findFirst().orElse(null);
        if(floor != null) {
            Layout current = floor.getLayouts().stream().filter(l -> l.getId().equals(layout.getId())).findFirst().orElse(null);
            if(current != null) {
                floor.getLayouts().set(floor.getLayouts().indexOf(current), layout);
                operations.save(restaurant);
            }
        }
    }

    @Override
    public void setSelectedLayout(String restaurantId, String floorId, String layoutId) {
        operations.updateFirst(Query.query
                (Criteria.where("_id").is(restaurantId)
                .and("floors.id").is(floorId)),
                Update.update("floors.$.activeLayout", layoutId), Restaurant.class);
    }

    @Override
    public void setTakeawayMenuId(String restaurantId, String menuId) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(restaurantId)),
                Update.update("takeawayMenu", menuId), Restaurant.class);
    }

    @Override
    public void updateTables(String restaurantId, List<Table> tables) {
        Restaurant restaurant = operations.findOne(Query.query(Criteria.where("_id").is(restaurantId)), Restaurant.class);
        List<Table> all = new ArrayList<>();
        Map<String,Table> ids = tables.stream().collect(Collectors.toMap(Table::getId, Function.identity()));
        for(Table table : restaurant.getTables()) {
            if(ids.containsKey(table.getId())) {
                all.add(ids.get(table.getId()));
            } else {
                all.add(table);
            }
        }
        restaurant.setTables(all);
        operations.save(restaurant);
    }

    @Override
    public void updateTable(String restaurantId, Table table) {
        operations.updateFirst(Query.query
                        (Criteria.where("_id").is(restaurantId)
                                .and("tables.id").is(table.getId())),
                Update.update("tables.$", table), Restaurant.class);
    }

    @Override
    public void pushTable(String restaurantId, Table table) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(restaurantId)),
                new Update().addToSet("tables", table), Restaurant.class);
    }

    @Override
    public void pullTable(String restaurantId, String tableId) {
        Criteria find = Criteria.where("_id").is(restaurantId);

        operations.updateFirst(Query.query(find),
                new Update().pull("tables", Query.query(Criteria.where("id").is(tableId))), Restaurant.class);
    }

    @Override
    public void updateIntegrations(String restaurantId, Map<ExternalIntegration, KVData> integrations) {
        operations.updateFirst(Query.query(Criteria.where("_id").is(restaurantId)),
                Update.update("integrations", integrations), Restaurant.class);
    }
}
