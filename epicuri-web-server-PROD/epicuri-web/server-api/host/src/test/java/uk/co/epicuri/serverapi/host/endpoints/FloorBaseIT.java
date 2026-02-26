package uk.co.epicuri.serverapi.host.endpoints;

import org.junit.Before;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Position;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.TableShape;
import uk.co.epicuri.serverapi.repository.BaseIT;

/**
 * Created by manish
 */
public abstract class FloorBaseIT extends BaseIT{
    @Before
    public void setUp() throws Exception {
        super.setUp();

        floor1.getLayouts().get(0).getTables().add(table1.getId());
        floor1.getLayouts().get(0).getTables().add(table2.getId());

        floor2.getLayouts().get(0).setName("layout 1 in floor2");
        floor2.getLayouts().get(0).setFloor(floor2.getId());
        floor2.getLayouts().get(0).setUpdated(System.currentTimeMillis());
        floor2.getLayouts().get(0).getTables().add(table1.getId());
        floor2.getLayouts().get(0).getTables().add(table2.getId());
        floor2.getLayouts().get(0).getTables().add(table3.getId());

        table1.setShape(TableShape.CIRCLE);
        Position position1 = new Position();
        position1.setRotation(4D);
        position1.setScaleX(2D);
        position1.setScaleY(2D);
        position1.setX(4D);
        position1.setY(4D);
        table1.setPosition(position1);
        table2.setShape(TableShape.SQUARE);
        Position position2 = new Position();
        position2.setRotation(8D);
        position2.setScaleX(4D);
        position2.setScaleY(4D);
        position2.setX(8D);
        position2.setY(8D);
        table2.setPosition(position2);
        Position position3 = new Position();
        position3.setRotation(8D);
        position3.setScaleX(4D);
        position3.setScaleY(4D);
        position3.setX(8D);
        position3.setY(8D);
        table3.setPosition(position3);

        restaurant1.getFloors().add(floor1);
        restaurant1.getFloors().add(floor2);

        restaurant1.getTables().clear();
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        restaurant1.getTables().add(table3);

        restaurantRepository.save(restaurant1);

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);
    }
}
