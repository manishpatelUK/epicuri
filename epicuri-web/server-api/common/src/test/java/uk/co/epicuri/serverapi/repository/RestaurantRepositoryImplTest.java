package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.TakeawayOfferingType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class RestaurantRepositoryImplTest extends BaseIT {

    @Test
    public void testSearchByGeography() throws Exception {
        LatLongPair position1 = new LatLongPair();
        position1.setLatitude(50);
        position1.setLongitude(-1);

        LatLongPair position2 = new LatLongPair();
        position2.setLatitude(100);
        position2.setLongitude(-10);

        LatLongPair position3 = new LatLongPair();
        position3.setLatitude(150);
        position3.setLongitude(-20);

        restaurant1.setPosition(position1);
        restaurant1.setCuisineId("c1");
        restaurant1.setName("Food Stuff");
        restaurant2.setPosition(position2);
        restaurant2.setCuisineId("c2");
        restaurant2.setName("Stuff Food");
        restaurant2.setTakeawayOffered(TakeawayOfferingType.DELIVERY_AND_COLLECTION);
        restaurant3.setPosition(position3);
        restaurant3.setName("Stuffed Nuts");

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);

        List<Restaurant> restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,null,null,null);
        assertEquals(3, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(60D,0D,40D,-5D,null,null,null);
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(120D,0D,40D,-19D,null,null,null);
        assertEquals(2, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,"c1",null,null);
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(null,null,null,null,"c1",null,null);
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,"c2",true,null);
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,"c2",false,null);
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,null,null,"Fo");
        assertEquals(2, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,null,null,"fo");
        assertEquals(2, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,null,null,"Stuff");
        assertEquals(3, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(null,null,null,null,null,null,"Fo");
        assertEquals(2, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(null,null,null,null,"c2",null,"Fo");
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(null,null,null,null,"c1",null,"Fo");
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,null,true,"Stuff");
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(999D,999D,-999D,-999D,null,false,"Stuff");
        assertEquals(3, restaurants.size());
    }

    @Test
    public void testSearchByGeographyNullParams() throws Exception {
        double latitude = 45.27;
        double longitude = 19.8;
        LatLongPair position1 = new LatLongPair();
        position1.setLatitude(latitude);
        position1.setLongitude(longitude);

        restaurant1.setPosition(position1);
        restaurantRepository.save(restaurant1);

        double trLat = latitude + 0.025;
        double trLong = longitude + 0.025;
        double blLat = latitude - 0.025;
        double blLong = longitude - 0.025;

        List<Restaurant> restaurants = restaurantRepository.searchByGeography(trLat, trLong, blLat, blLong, null, null, null);
        assertEquals(1, restaurants.size());

        restaurants = restaurantRepository.searchByGeography(trLat, trLong, blLat, blLong, null, null, "");
        assertEquals(1, restaurants.size());
    }

    @Test
    public void testAddLayout() throws Exception {
        Floor floor1 = new Floor(restaurant1.getId());
        Floor floor2 = new Floor(restaurant1.getId());
        restaurant1.getFloors().add(floor1);
        restaurant1.getFloors().add(floor2);

        restaurantRepository.save(restaurant1);

        Layout layout = new Layout(floor2);
        restaurantRepository.addLayout(restaurant1.getId(), floor2.getId(), layout);

        final Restaurant savedRestaurant = restaurantRepository.findOne(restaurant1.getId());
        Floor savedFloor1 = savedRestaurant.getFloors().get(1);
        Floor savedFloor0 = savedRestaurant.getFloors().get(0);
        assertTrue(savedFloor1.getLayouts().size() == 1);
        assertTrue(savedFloor0.getLayouts().size() == 0);
        assertEquals(savedFloor1.getLayouts().get(0).getId(), layout.getId());

        assertFalse(restaurantRepository.findOne(restaurant2.getId()).getFloors().size() > 0);
    }

    @Test
    public void testUpdateLayout() throws Exception {
        Floor floor1 = new Floor(restaurant1.getId());
        Layout layout1 = new Layout(floor1);
        Layout layout2 = new Layout(floor1);
        assertNotEquals(layout1.getId(), layout2.getId());
        floor1.getLayouts().add(layout1);
        floor1.getLayouts().add(layout2);
        restaurant1.getFloors().add(floor1);

        Floor floor2 = new Floor(restaurant2.getId());
        Layout layout3 = new Layout(floor2);
        floor2.getLayouts().add(layout3);
        restaurant2.getFloors().add(floor2);

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);

        final String tableName = "a table";
        layout1.getTables().add(tableName);

        restaurantRepository.updateLayout(restaurant1.getId(), floor1.getId(), layout1);

        final Restaurant r1 = restaurantRepository.findOne(restaurant1.getId());
        Floor savedFloor = r1.getFloors().get(0);
        assertTrue(savedFloor.getLayouts().size() == 2);
        assertEquals(savedFloor.getLayouts().get(0).getId(), layout1.getId());
        assertEquals(savedFloor.getLayouts().get(0).getTables().get(0), tableName);

        restaurantRepository.updateLayout(restaurant1.getId(), floor1.getId(), layout1);

        savedFloor = restaurantRepository.findOne(restaurant1.getId()).getFloors().get(0);
        assertTrue(savedFloor.getLayouts().size() == 2);
        assertEquals(savedFloor.getLayouts().get(0).getId(), layout1.getId());
        assertEquals(savedFloor.getLayouts().get(0).getTables().get(0), tableName);
        assertEquals(0, savedFloor.getLayouts().get(1).getTables().size());

        assertTrue(restaurantRepository.findOne(restaurant2.getId()).getFloors().get(0).getLayouts().size() == 1);
        assertFalse(restaurantRepository.findOne(restaurant2.getId()).getFloors().get(0).getLayouts().get(0).getTables().size() > 0);
    }

    @Test
    public void testSetSelectedLayout() throws Exception {
        Floor floor1 = new Floor(restaurant1.getId());
        Floor floor2 = new Floor(restaurant1.getId());
        assertNotEquals(floor1.getId(), floor2.getId());
        restaurant1.getFloors().add(floor1);
        restaurant1.getFloors().add(floor2);

        Floor floor3 = new Floor(restaurant2.getId());
        restaurant2.getFloors().add(floor3);

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);

        String dummyLayoutId1 = "L1";
        restaurantRepository.setSelectedLayout(restaurant1.getId(), floor1.getId(), dummyLayoutId1);

        Restaurant savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        assertEquals(2, savedRestaurant1.getFloors().size());
        assertEquals(dummyLayoutId1, savedRestaurant1.getFloors().get(0).getActiveLayout());
        assertNull(savedRestaurant1.getFloors().get(1).getActiveLayout());
        restaurantRepository.setSelectedLayout(restaurant1.getId(), floor2.getId(), dummyLayoutId1);
        restaurantRepository.setSelectedLayout(restaurant1.getId(), floor1.getId(), null);
        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        assertEquals(dummyLayoutId1, savedRestaurant1.getFloors().get(1).getActiveLayout());
        assertNull(savedRestaurant1.getFloors().get(0).getActiveLayout());

        Restaurant savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());
        assertEquals(1, savedRestaurant2.getFloors().size());
        assertNull(savedRestaurant2.getFloors().get(0).getActiveLayout());
    }

    @Test
    public void testSetTakeawayMenuId() throws Exception {
        String menu1 = "M1";
        String menu2 = "M2";
        restaurant1.setTakeawayMenu(menu1);

        restaurantRepository.save(restaurant1);

        restaurantRepository.setTakeawayMenuId(restaurant1.getId(), null);
        Restaurant savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());

        assertNull(savedRestaurant1.getTakeawayMenu());
        assertNull(restaurantRepository.findOne(restaurant2.getId()).getTakeawayMenu());

        restaurantRepository.setTakeawayMenuId(restaurant2.getId(), menu2);
        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        Restaurant savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());

        assertNull(savedRestaurant1.getTakeawayMenu());
        assertEquals(menu2, savedRestaurant2.getTakeawayMenu());
    }

    @Test
    public void testUpdateTables() throws Exception {
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        restaurant2.getTables().add(table3);

        table1.setName("foo");
        List<Table> list1 = new ArrayList<>();
        list1.add(table1);

        restaurantRepository.updateTables(restaurant1.getId(), list1);
        Restaurant savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());

        assertEquals(1, savedRestaurant1.getTables().size());
        assertEquals(table1.getName(), savedRestaurant1.getTables().get(0).getName());

        Restaurant savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());
        assertEquals(1, savedRestaurant2.getTables().size());

        list1.clear();
        restaurantRepository.updateTables(restaurant1.getId(), list1);

        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());

        assertEquals(1, savedRestaurant1.getTables().size());
        assertEquals(table1.getName(), savedRestaurant1.getTables().get(0).getName());

        savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());
        assertEquals(1, savedRestaurant2.getTables().size());
    }

    @Test
    public void testUpdateTable() throws Exception {
        table1.setName("foo");
        restaurantRepository.updateTable(restaurant1.getId(), table1);
        Restaurant savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());

        assertNotNull(savedRestaurant1.getTables().get(0).getName());
        assertTrue(savedRestaurant1.getTables().stream().map(Table::getName).collect(Collectors.toList()).contains(table1.getName()));
    }

    @Test
    public void testPushTable() throws Exception {
        Table table4 = new Table();
        table4.setName("t4");
        table4.setId("t4");
        table4.setShape(TableShape.SQUARE);
        restaurant1.getTables().add(table4);

        restaurantRepository.pushTable(restaurant1.getId(), table4);
        Restaurant savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());

        assertEquals(2, savedRestaurant1.getTables().size());
        assertEquals(table1.getId(), savedRestaurant1.getTables().get(0).getId());
        assertEquals(table4.getId(), savedRestaurant1.getTables().get(1).getId());

        Restaurant savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());
        assertEquals(1, savedRestaurant2.getTables().size());
        assertNotEquals(table1.getId(), savedRestaurant2.getTables().get(0).getId());
        assertNotEquals(table4.getId(), savedRestaurant2.getTables().get(0).getId());
        assertEquals(table2.getId(), savedRestaurant2.getTables().get(0).getId());

    }

    @Test
    public void testPullTable() throws Exception {
        Table table4 = new Table();
        table4.setName("t4");
        table4.setId("t4");
        table4.setShape(TableShape.SQUARE);
        restaurant2.getTables().add(table4);

        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);

        restaurantRepository.pullTable(restaurant1.getId(), table1.getId());
        Restaurant savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        Restaurant savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());

        assertEquals(0, savedRestaurant1.getTables().size());
        assertEquals(2, savedRestaurant2.getTables().size());

        restaurantRepository.pullTable(restaurant1.getId(), table1.getId());
        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());

        assertEquals(0, savedRestaurant1.getTables().size());
        assertEquals(2, savedRestaurant2.getTables().size());

        restaurantRepository.pullTable(restaurant2.getId(), table1.getId());
        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());

        assertEquals(0, savedRestaurant1.getTables().size());
        assertEquals(2, savedRestaurant2.getTables().size());

        restaurantRepository.pullTable(restaurant2.getId(), null);
        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());

        assertEquals(0, savedRestaurant1.getTables().size());
        assertEquals(2, savedRestaurant2.getTables().size());

        restaurantRepository.pullTable(restaurant2.getId(), table4.getId());
        savedRestaurant1 = restaurantRepository.findOne(restaurant1.getId());
        savedRestaurant2 = restaurantRepository.findOne(restaurant2.getId());

        assertEquals(0, savedRestaurant1.getTables().size());
        assertEquals(1, savedRestaurant2.getTables().size());
        assertEquals(table2.getId(), savedRestaurant2.getTables().get(0).getId());
    }
}