package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostLayoutView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTablePositionView;
import uk.co.epicuri.serverapi.common.pojo.host.HostTableView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Floor;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Layout;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class LayoutControllerTest extends FloorBaseIT {
    @Test
    public void testGetLayout() throws Exception {
        String token = getTokenForStaff(staff1);

        Layout layout = floor2.getLayouts().get(0);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", layout.getId())
                .get("Layout/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostLayoutView hostLayoutView = response.getBody().as(HostLayoutView.class, ObjectMapperType.JACKSON_2);
        assertEquals(layout.getId(), hostLayoutView.getId());
        assertEquals(layout.getFloor(), hostLayoutView.getFloor());
        assertEquals(layout.getName(), hostLayoutView.getName());
        assertEquals(layout.getUpdated() / 1000, hostLayoutView.getUpdated());

        assertTrue(hostLayoutView.getTables().stream().anyMatch(t -> t.getId().equals(table1.getId())));
        assertTrue(hostLayoutView.getTables().stream().anyMatch(t -> t.getId().equals(table2.getId())));
        assertTrue(hostLayoutView.getTables().stream().anyMatch(t -> t.getId().equals(table3.getId())));

        HostTableView tableView2 = hostLayoutView.getTables().stream().filter(t -> t.getId().equals(table2.getId())).findFirst().get();
        assertEquals(table2.getName(), tableView2.getName());
        assertEquals(table2.getShape().getOrdinal(), tableView2.getShape());
        HostTablePositionView positionView2 = tableView2.getPosition();
        assertEquals(tableView2.getPosition().getRotation(), positionView2.getRotation(), 0.01D);
        assertEquals(tableView2.getPosition().getScaleX(), positionView2.getScaleX(), 0.01D);
        assertEquals(tableView2.getPosition().getScaleY(), positionView2.getScaleY(), 0.01D);
        assertEquals(tableView2.getPosition().getX(), positionView2.getX(), 0.01D);
        assertEquals(tableView2.getPosition().getY(), positionView2.getY(), 0.01D);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .get("Layout/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testPostLayout() throws Exception {
        String token = getTokenForStaff(staff1);

        Layout layout = floor2.getLayouts().get(0);
        HostLayoutView hostLayoutView = getLayoutView(layout);
        hostLayoutView.setName("New nambo");
        hostLayoutView.getTables().remove(2);
        hostLayoutView.setId(null);
        hostLayoutView.setFloor("a bad id");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .post("Layout");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        hostLayoutView.setFloor(floor2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .post("Layout");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        String[] bits = response.getHeader("Location").split("/");
        String newId = bits[bits.length-1];
        Floor floor = restaurantRepository.findOne(restaurant1.getId()).getFloors().stream().filter(f -> f.getId().equals(floor2.getId())).findFirst().get();
        Layout layout2 = floor.getLayouts().stream().filter(l -> l.getId().equals(newId)).findFirst().get();
        assertEquals(hostLayoutView.getFloor(), layout2.getFloor());
        assertEquals(hostLayoutView.getName(), layout2.getName());
        assertTrue(layout2.getUpdated() >= System.currentTimeMillis()/1000);

        layout2.getTables().stream().anyMatch(t -> t.equals(table1.getId()));
        layout2.getTables().stream().anyMatch(t -> t.equals(table2.getId()));
        assertEquals(hostLayoutView.getTables().size(), layout2.getTables().size());
    }

    @Test
    public void testPutLayout() throws Exception {
        String token = getTokenForStaff(staff1);

        Layout layout = floor2.getLayouts().get(0);
        HostLayoutView hostLayoutView = getLayoutView(layout);

        // bad floor id
        hostLayoutView.setFloor("a bad id");
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .pathParam("id", hostLayoutView.getId())
                .put("Layout/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        hostLayoutView.setFloor(floor2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .pathParam("id", "foobar")
                .put("Layout/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        hostLayoutView.setFloor(null);
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .pathParam("id", hostLayoutView.getId())
                .put("Layout/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        hostLayoutView.setFloor(floor2.getId());
        Layout layout2 = getUpdatedLayout(token, hostLayoutView);
        assertEquals(hostLayoutView.getFloor(), layout2.getFloor());
        assertEquals(hostLayoutView.getName(), layout2.getName());
        assertTrue(layout2.getUpdated() >= System.currentTimeMillis()/1000);

        //change name
        hostLayoutView.setName("New nambo");
        layout2 = getUpdatedLayout(token, hostLayoutView);
        assertEquals(hostLayoutView.getName(), layout2.getName());

        //remove a table
        HostTableView removedTable = hostLayoutView.getTables().remove(2);
        layout2 = getUpdatedLayout(token, hostLayoutView);
        assertEquals(hostLayoutView.getTables().size(), layout2.getTables().size());
        assertFalse(layout2.getTables().contains(removedTable.getId()));

        //add a table
        hostLayoutView.getTables().add(removedTable);
        layout2 = getUpdatedLayout(token, hostLayoutView);
        assertEquals(hostLayoutView.getTables().size(), layout2.getTables().size());
        assertTrue(layout2.getTables().contains(removedTable.getId()));

        //modify table2
        HostTableView hostTableView = hostLayoutView.getTables().stream().filter(t -> t.getId().equals(table2.getId())).findFirst().get();
        hostTableView.setName("t2arse");
        hostTableView.getPosition().setRotation(hostTableView.getPosition().getRotation()*2);
        hostTableView.getPosition().setScaleX(hostTableView.getPosition().getScaleX()*2);
        hostTableView.getPosition().setScaleY(hostTableView.getPosition().getScaleY()*2);
        hostTableView.getPosition().setX(hostTableView.getPosition().getX()*2);
        hostTableView.getPosition().setY(hostTableView.getPosition().getY()*2);
        layout2 = getUpdatedLayout(token, hostLayoutView);
        Table table = restaurantRepository.findOne(restaurant1.getId()).getTables().stream().filter(t -> t.getId().equals(hostTableView.getId())).findFirst().get();
        assertEquals(hostTableView.getName(), table.getName());
        assertEquals(hostTableView.getPosition().getRotation(), table.getPosition().getRotation(), 0.01);
        assertEquals(hostTableView.getPosition().getScaleX(), table.getPosition().getScaleX(), 0.01);
        assertEquals(hostTableView.getPosition().getScaleY(), table.getPosition().getScaleY(), 0.01);
        assertEquals(hostTableView.getPosition().getX(), table.getPosition().getX(), 0.01);
        assertEquals(hostTableView.getPosition().getY(), table.getPosition().getY(), 0.01);
    }

    private Layout getUpdatedLayout(String token, HostLayoutView hostLayoutView) {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .pathParam("id", hostLayoutView.getId())
                .put("Layout/{id}");

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        Floor floor = restaurantRepository.findOne(restaurant1.getId()).getFloors().stream().filter(f -> f.getId().equals(floor2.getId())).findFirst().get();
        return floor.getLayouts().stream().filter(l -> l.getId().equals(hostLayoutView.getId())).findFirst().get();
    }

    private HostLayoutView getLayoutView(Layout layout) {
        List<Table> tables = new ArrayList<>();
        tables.add(table1);
        tables.add(table2);
        tables.add(table3);
        return new HostLayoutView(floor2, layout, tables);
    }
}