package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostTablePositionView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTableView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class TableControllerTest extends BaseIT {

    private static Random random = new Random();

    @Before
    public void setUp() throws Exception {
        super.setUp();

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        table1.setId(IDAble.generateId(restaurant1.getId()));
        table2.setId(IDAble.generateId(restaurant1.getId()));
        table3.setId(IDAble.generateId(restaurant1.getId()));

        setParams(table1);
        setParams(table2);
        setParams(table3);

        restaurant1.getTables().clear();
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        restaurant1.getTables().add(table3);

        restaurantRepository.save(restaurant1);
    }

    private void setParams(Table table) {
        Position position = new Position();
        position.setRotation(random.nextDouble());
        position.setX(random.nextDouble());
        position.setY(random.nextDouble());
        position.setScaleX(random.nextDouble());
        position.setScaleY(random.nextDouble());
        table.setPosition(position);

        table.setShape(random.nextBoolean() ? TableShape.CIRCLE : TableShape.SQUARE);
    }

    @Test
    public void testGetTables() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Table");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        List<HostTableView> tables = Arrays.asList(response.getBody().as(HostTableView[].class, ObjectMapperType.JACKSON_2));

        assertEquals(3, tables.size());
        testEqual(table1, tables.stream().filter(t -> t.getId().equals(table1.getId())).findFirst().get(), false);
        testEqual(table2, tables.stream().filter(t -> t.getId().equals(table2.getId())).findFirst().get(), false);
        testEqual(table3, tables.stream().filter(t -> t.getId().equals(table3.getId())).findFirst().get(), false);
    }

    public void testEqual(Table table, HostTableView view, boolean testForPosition) {
        assertEquals(table.getId(), view.getId());
        assertEquals(table.getName(), view.getName());
        assertEquals(table.getShape().getOrdinal(), view.getShape());
        if(testForPosition) {
            assertEquals(table.getPosition().getX(), view.getPosition().getX(), 0.01);
            assertEquals(table.getPosition().getY(), view.getPosition().getY(), 0.01);
            assertEquals(table.getPosition().getRotation(), view.getPosition().getRotation(), 0.01);
            assertEquals(table.getPosition().getScaleX(), view.getPosition().getScaleX(), 0.01);
            assertEquals(table.getPosition().getScaleY(), view.getPosition().getScaleY(), 0.01);
        } else {
            assertNotNull(table.getPosition());
        }
    }

    @Test
    public void testPostTable() throws Exception {
        String token = getTokenForStaff(staff1);

        Table table4 = new Table();
        //table4.setId(IDAble.generateId(restaurant1.getId()));
        table4.setName("table4");
        setParams(table4);

        HostTableView payload = new HostTableView(table4, true);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Table");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostTableView created = response.getBody().as(HostTableView.class, ObjectMapperType.JACKSON_2);
        assertNotNull(created.getId());
        table4.setId(created.getId());

        testEqual(table4, created, true);
        assertNotNull(restaurantRepository.findOne(restaurant1.getId()).getTables().stream().filter(t -> t.getId().equals(created.getId())).findFirst().orElse(null));
    }

    @Test
    public void testPostTableWhenTablesDisabled() throws Exception {
        String token = getTokenForStaff(staff1);
        Restaurant restaurant = restaurantRepository.findOne(staff1.getRestaurantId());
        restaurant.setTablesEnabled(false);
        restaurantRepository.save(restaurant);

        Table table4 = new Table();
        //table4.setId(IDAble.generateId(restaurant1.getId()));
        table4.setName("table4");
        setParams(table4);

        HostTableView payload = new HostTableView(table4, true);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Table");

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());

        staff1.setRole(StaffRole.EPICURI_ADMIN);
        staffRepository.save(staff1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Table");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testPostTableCheckDuplication() throws Exception {
        String token = getTokenForStaff(staff1);

        Table table4 = new Table();
        table4.setName(table1.getName());
        setParams(table4);

        HostTableView payload = new HostTableView(table4, true);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Table");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPostTableCheckEmptyName() throws Exception {
        String token = getTokenForStaff(staff1);

        Table table4 = new Table();
        table4.setName("");
        setParams(table4);

        HostTableView payload = new HostTableView(table4, true);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .post("Table");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPutTable1() throws Exception {
        String token = getTokenForStaff(staff1);
        HostTableView payload = new HostTableView(table2, true);
        putTable(token, payload, true);
    }

    @Test
    public void testPutTable2() throws Exception {
        String token = getTokenForStaff(staff1);
        HostTableView payload = new HostTableView(table2, false);
        table2.setPosition(new Position(new HostTablePositionView(random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble())));
        restaurantRepository.save(restaurant1);

        putTable(token, payload, false);
    }

    private void putTable(String token, HostTableView payload, boolean setPosition) {
        payload.setShape((table2.getShape() == TableShape.CIRCLE ? TableShape.SQUARE : TableShape.CIRCLE).getOrdinal());
        payload.setName("foobarmanchu");
        if(setPosition) {
            payload.setPosition(new HostTablePositionView(random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble()));
        } else {
            payload.setPosition(null);
        }

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .pathParam("id", table2.getId())
                .put("Table/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        Table test = restaurantRepository.findOne(restaurant1.getId()).getTables().stream().filter(t -> t.getId().equals(table2.getId())).findFirst().get();
        testEqual(test, payload, setPosition);
    }

    @Test
    public void testPutTableWhenTablesDisabled() throws Exception {
        String token = getTokenForStaff(staff1);
        Restaurant restaurant = restaurantRepository.findOne(staff1.getRestaurantId());
        restaurant.setTablesEnabled(false);
        restaurantRepository.save(restaurant);

        HostTableView payload = new HostTableView(table2, true);

        payload.setShape((table2.getShape() == TableShape.CIRCLE ? TableShape.SQUARE : TableShape.CIRCLE).getOrdinal());
        payload.setName("foobarmanchu");
        payload.setPosition(new HostTablePositionView(random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble()));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .pathParam("id", table2.getId())
                .put("Table/{id}");

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode());

        staff1.setRole(StaffRole.EPICURI_ADMIN);
        staffRepository.save(staff1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(payload)
                .pathParam("id", table2.getId())
                .put("Table/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    public void testDeleteTable() throws Exception {
        String token = getTokenForStaff(staff1);

        session1.setRestaurantId(restaurant1.getId());
        session1.setStartTime(System.currentTimeMillis());
        session1.getTables().add(table1.getId());
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", table1.getId())
                .delete("Table/{id}");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        session1.getTables().clear();
        sessionRepository.save(session1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", table1.getId())
                .delete("Table/{id}");

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());

        assertNull(restaurantRepository.findOne(restaurant1.getId()).getTables().stream().filter(t -> t.getId().equals(table1.getId())).findFirst().orElse(null));
    }
}