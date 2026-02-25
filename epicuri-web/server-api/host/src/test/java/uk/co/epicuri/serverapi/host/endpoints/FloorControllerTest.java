package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.host.HostFloorView;
import uk.co.epicuri.serverapi.common.pojo.host.HostLayoutView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Layout;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import java.util.ArrayList;
import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class FloorControllerTest extends FloorBaseIT {

    @Test
    public void testGetFloors() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Floor");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        HostFloorView[] hostFloorViews = response.getBody().as(HostFloorView[].class, ObjectMapperType.JACKSON_2);
        assertEquals(2, hostFloorViews.length);
        assertTrue(hostFloorViews[0].getId().equals(floor1.getId()) || hostFloorViews[1].getId().equals(floor1.getId()));
        assertTrue(hostFloorViews[0].getId().equals(floor2.getId()) || hostFloorViews[1].getId().equals(floor2.getId()));
        assertNotNull(hostFloorViews[0].getLayout());
        assertNotNull(hostFloorViews[1].getLayout());

    }

    @Test
    public void testGetFloors1() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Floor/foobar");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Floor/"+floor2.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        HostFloorView hostFloorView = response.getBody().as(HostFloorView.class, ObjectMapperType.JACKSON_2);

        assertEquals(floor2.getId(), hostFloorView.getId());
        assertNotNull(hostFloorView.getLayout());
        assertEquals(1, hostFloorView.getLayouts().size());

        HostLayoutView layoutView = hostFloorView.getLayouts().get(0);
        assertEquals(floor2.getId(), layoutView.getFloor());
        assertEquals(floor2.getLayouts().get(0).getId(), layoutView.getId());
        assertEquals(floor2.getLayouts().get(0).getName(), layoutView.getName());
        assertTrue(layoutView.getTables().stream().anyMatch(t -> t.getId().equals(table1.getId())));
        assertTrue(layoutView.getTables().stream().anyMatch(t -> t.getId().equals(table2.getId())));
        assertTrue(layoutView.getTables().stream().anyMatch(t -> t.getId().equals(table3.getId())));
    }

    @Test
    public void testPutLayout() throws Exception {
        String token = getTokenForStaff(staff1);

        HostLayoutView hostLayoutView = new HostLayoutView();
        hostLayoutView.setId(floor2.getLayouts().get(0).getId());

        floor2.setActiveLayout(null);
        restaurantRepository.save(restaurant1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .put("Floor/"+floor2.getId());

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertEquals(floor2.getLayouts().get(0).getId(),
                restaurantRepository.findOne(restaurant1.getId()).getFloors()
                            .stream().filter(f -> f.getId().equals(floor2.getId())).findFirst()
                            .get().getActiveLayout());
    }

    @Test
    public void testPutLayoutActiveTables() throws Exception {
        Layout extra = new Layout(floor2);
        floor2.getLayouts().add(extra);
        Layout layout = floor2.getLayouts().get(0);
        layout.getTables().add(table1.getId());
        floor2.setActiveLayout(layout.getId());
        session1.setSessionType(SessionType.SEATED);
        session1.setRestaurantId(staff1.getRestaurantId());
        session1.setStartTime(System.currentTimeMillis());
        restaurantRepository.save(restaurant1);
        session1.getTables().add(table1.getId());
        sessionRepository.save(session1);

        HostLayoutView hostLayoutView = new HostLayoutView();
        hostLayoutView.setId(extra.getId());

        String token = getTokenForStaff(staff1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(hostLayoutView)
                .put("Floor/"+floor2.getId());

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testGetFloorImage() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", restaurantImage1.getId())
                .when()
                .get("Floor/Image/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        byte[] array = response.asByteArray();
        assertTrue(Arrays.equals(restaurantImage1.getImage(), array));

        response = given()
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", restaurantImage1.getId())
                .when()
                .get("Floor/Image/{id}.jpg");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        array = response.asByteArray();
        assertTrue(Arrays.equals(restaurantImage1.getImage(), array));
    }

    @Test
    public void testActiveLayout() throws Exception {
        floor1.setActiveLayout(null);
        restaurantRepository.save(restaurant1);
        HostFloorView view = new HostFloorView(floor1, true, new ArrayList<>());
        assertNull(view.getLayout());
        FloorController.ensureActiveLayout(view, restaurant1.getId(), masterDataService);
        assertNotNull(view.getLayout());
        assertNotNull(restaurantRepository.findOne(restaurant1.getId()).getFloors().get(0).getActiveLayout());
    }
}
