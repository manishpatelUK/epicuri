package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class StockControlControllerTest extends BaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        stockLevelRepository.insert(createStockLevel("m1",1,restaurant1.getId()));
        stockLevelRepository.insert(createStockLevel("m2",1,restaurant1.getId()));
        stockLevelRepository.insert(createStockLevel("m3",1,restaurant2.getId()));
    }

    private StockLevel createStockLevel(String plu, int level, String id) {
        StockLevel stockLevel = new StockLevel();
        stockLevel.setPlu(plu);
        stockLevel.setLevel(level);
        stockLevel.setRestaurantId(id);
        return stockLevel;
    }

    @Test
    public void getStockLevels() {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("StockControl");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        StockLevel[] stockLevels = response.getBody().as(StockLevel[].class, ObjectMapperType.JACKSON_2);
        assertEquals(2, stockLevels.length);
        assertTrue(stockLevels[0].getPlu().equals("m1") || stockLevels[0].getPlu().equals("m2"));
        assertTrue(stockLevels[1].getPlu().equals("m1") || stockLevels[1].getPlu().equals("m2"));
    }

    @Test
    public void putStockLevel() {
        String token = getTokenForStaff(staff1);

        StockLevel stockLevel = masterDataService.getStockLevels(restaurant1.getId()).get(0);
        stockLevel.setLevel(5);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .pathParam("id", stockLevel.getId())
                .header(Params.AUTHORIZATION, token)
                .put("StockControl/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(5, stockLevelRepository.findOne(stockLevel.getId()).getLevel());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .pathParam("id", "foobar")
                .header(Params.AUTHORIZATION, token)
                .put("StockControl/{id}");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void putStockLevelUpdatesItems() {
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setPlu("m1");
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem2.setPlu("m2");
        menuItem3.setRestaurantId(restaurant1.getId());
        menuItem3.setPlu(null);
        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        StockLevel stockLevel = masterDataService.getStockLevelByRestaurantIdAndPlu(restaurant1.getId(), "m2");
        stockLevel.setPlu("foobar");

        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .pathParam("id", stockLevel.getId())
                .header(Params.AUTHORIZATION, token)
                .put("StockControl/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals("m1", menuItemRepository.findOne(menuItem1.getId()).getPlu());
        assertEquals("foobar", menuItemRepository.findOne(menuItem2.getId()).getPlu());
        assertNull(menuItemRepository.findOne(menuItem3.getId()).getPlu());
    }

    @Test
    public void putStockLevelAvailability() {
        String token = getTokenForStaff(staff1);

        StockLevel stockLevel = masterDataService.getStockLevels(restaurant1.getId()).get(0);
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setPlu(stockLevel.getPlu());
        menuItemRepository.save(menuItem1);
        stockLevel.setLevel(0);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .pathParam("id", stockLevel.getId())
                .header(Params.AUTHORIZATION, token)
                .put("StockControl/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertTrue(menuItemRepository.findOne(menuItem1.getId()).isUnavailable());

        stockLevel.setLevel(1);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .pathParam("id", stockLevel.getId())
                .header(Params.AUTHORIZATION, token)
                .put("StockControl/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertFalse(menuItemRepository.findOne(menuItem1.getId()).isUnavailable());
    }

    @Test
    public void putStockLevelAvailabilityWhenNotTrackable() {
        String token = getTokenForStaff(staff1);

        StockLevel stockLevel = masterDataService.getStockLevels(restaurant1.getId()).get(0);
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setPlu(stockLevel.getPlu());
        menuItemRepository.save(menuItem1);
        stockLevel.setLevel(0);
        stockLevel.setTrackable(false);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .pathParam("id", stockLevel.getId())
                .header(Params.AUTHORIZATION, token)
                .put("StockControl/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertFalse(menuItemRepository.findOne(menuItem1.getId()).isUnavailable());
    }

    @Test
    public void postStockLevel() {
        String token = getTokenForStaff(staff1);

        StockLevel stockLevel = createStockLevel("m5", 1, restaurant1.getId());
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .header(Params.AUTHORIZATION, token)
                .post("StockControl");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNotNull(stockLevelRepository.findByRestaurantId(restaurant1.getId()).stream().filter(s -> s.getPlu().equals("m5")).findFirst().orElse(null));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .header(Params.AUTHORIZATION, token)
                .post("StockControl");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void deleteStockLevel() {
        String token = getTokenForStaff(staff1);

        StockLevel stockLevel = createStockLevel("m5", 1, restaurant1.getId());
        stockLevel = stockLevelRepository.save(stockLevel);
        menuItem1.setPlu("m5");
        menuItem1.setRestaurantId(restaurant1.getId());
        menuItemRepository.save(menuItem1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(stockLevel)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", stockLevel.getId())
                .delete("StockControl/{id}");
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertNull(stockLevelRepository.findOne(stockLevel.getId()));
        assertNull(menuItemRepository.findOne(menuItem1.getId()).getPlu());
    }
}