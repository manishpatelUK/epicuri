package uk.co.epicuri.serverapi.host.workflows;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by manish on 26/07/2017.
 */
@Ignore
public class OrderingWorkflow extends WorkflowsBaseIT{

    @Before
    public void setUp() throws Exception{
        super.setUp();

        printer1.setRestaurantId(testRestaurant.getId());
        printer1.setIp("192.168.1.100");
        printerRepository.save(printer1);
        printer3.setRestaurantId(testRestaurant.getId());
        printer3.setIp("192.168.1.100");
        printerRepository.save(printer3);

        setUpItem(testRestaurant, printer1, menuItem1);
        setUpItem(testRestaurant, printer3, menuItem2);
        setUpItem(testRestaurant, printer1, menuItem3);

        service1.setId(IDAble.generateId(testRestaurant.getId()));
        testRestaurant.getServices().add(service1);
        restaurantRepository.save(testRestaurant);
    }

    protected void setUpItem(Restaurant restaurant, Printer printer, MenuItem menuItem) {
        menuItem.setRestaurantId(restaurant.getId());
        menuItem.setDefaultPrinter(printer.getId());
        menuItem.setTaxTypeId(tax1.getId());
        menuItemRepository.save(menuItem);
    }

    protected OrderRequest getOrderRequest(Session session, Course course, MenuItem menuItem) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCourseId(course.getId());
        orderRequest.setDinerId(session.getDiners().get(0).getId());
        orderRequest.setInstantiatedFromId(ActivityInstantiationConstant.WAITER.getId());
        orderRequest.setMenuItemId(menuItem.getId());
        orderRequest.setNote("foobar");
        orderRequest.setQuantity(1);
        orderRequest.setSessionId(session.getId());
        return orderRequest;
    }

    protected List<HostBatchView> createOrder(String token, Session session, Course course, MenuItem... menuItems) {
        List<OrderRequest> requestList = new ArrayList<>();
        for(MenuItem menuItem : menuItems) {
            requestList.add(getOrderRequest(session, course, menuItem));
        }

        Response response = given().accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(requestList)
                .queryParam("willAttemptImmediatePrint", true)
                .post("Order");

        OrderResponse orderResponse = response.as(OrderResponse.class, ObjectMapperType.JACKSON_2);
        return orderResponse.getBatches();
    }

    protected void resetImmediatePrintingToFalse(Session session) {
        List<Batch> batchesBySessionId = liveDataService.getBatchesBySessionId(session.getId());
        batchesBySessionId.forEach(b -> b.setAwaitingImmediatePrint(false));
        batchRepository.save(batchesBySessionId);
    }
}
