package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.host.*;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayType;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class KitchenControllerTest extends SessionSetupBaseIT {
    @Before
    public void setUp() throws Exception{
        super.setUp();

        setUpMixedSessions();
        // 1st June 2017 12:00 in London == GMT+1, 11:00 UTC
        long time = 1496314800000L;
        session1.setStartTime(time);
        session1.setName("session1");
        session2.setStartTime(time);
        session2.setName("session2");
        session3.setStartTime(time);
        session3.setName("session3");
        session4.setStartTime(time);
        session4.setName("session4");

        table1.setId(IDAble.generateId(restaurant1.getId()));
        table2.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getTables().clear();
        restaurant1.getTables().add(table1);
        restaurant1.getTables().add(table2);
        List<String> tables = new ArrayList<>();
        tables.add(table1.getId());
        tables.add(table2.getId());
        session3.setTables(tables);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);
        sessionRepository.save(session4);

        setUpPrinters(restaurant1);
        setUpMenuItems(restaurant1);

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        restaurant1.setIANATimezone("Europe/London");
        restaurant1.getServices().clear();
        service1.getCourses().clear();
        restaurant1.getServices().add(service1);
        service1.getCourses().add(course1);
        course1.setName("course1");
        restaurantRepository.save(restaurant1);

        printer1.setRestaurantId(restaurant1.getId());
        printer2.setRestaurantId(restaurant1.getId());
        printerRepository.save(printer1);
        printerRepository.save(printer2);
    }

    @Test
    public void getOrdersAllPrintersAggregateBySession() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "true")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        assertEqual(ticketsResponses.get(0), session1, true);
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWhenNoBatches() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        assertEqual(ticketsResponses.get(0), session1, true); //behaves like a session aggregation
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWithBatches() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);
        batch1.setSessionId(session1.getId());
        batch1.setIntendedPrintTime(session1.getStartTime() + 60000);
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch2.setSessionId(session1.getId());
        batch2.setIntendedPrintTime(session1.getStartTime() + 60000);
        batch2.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(2, ticketsResponses.size());
        assertEqual(ticketsResponses.get(0), session1, false);
        assertEqual(ticketsResponses.get(1), session1, false);
        assertNotNull(ticketsResponses.get(0).getBatchId());
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWithBatchesAndAggregateIdenticalOrders1() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);

        // 2 orders identical, same batch
        amendOrder(order1, menuItem1, menuItem1.getPrice(), 1, tax1, new ArrayList<>());
        amendOrder(order2, menuItem1, menuItem1.getPrice(), 1, tax1, new ArrayList<>());
        amendOrder(order3, menuItem2, menuItem2.getPrice(), 1, tax1, new ArrayList<>());
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        PrinterTicketsResponse ticketsResponse = ticketsResponses.get(0);
        assertNotNull(ticketsResponse.getBatchId());
        assertEquals(1, ticketsResponse.getCourses().size());
        PrinterTicketsCourseView printerTicketsCourseView = ticketsResponse.getCourses().get(0);
        assertEquals(2, printerTicketsCourseView.getItems().size());
        PrinterTicketView printerTicketView1 = printerTicketsCourseView.getItems().stream().filter(p -> p.getMenuItemId().equals(menuItem1.getId())).findFirst().orElse(null);
        PrinterTicketView printerTicketView2 = printerTicketsCourseView.getItems().stream().filter(p -> p.getMenuItemId().equals(menuItem2.getId())).findFirst().orElse(null);
        assertEquals(splitSortJoin(order1.getId() + "," + order2.getId()), splitSortJoin(printerTicketView1.getOrderId()));
        assertEquals(2, printerTicketView1.getQuantity());
        assertEquals(order3.getId(), printerTicketView2.getOrderId());
        assertEquals(1, printerTicketView2.getQuantity());
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWithBatchesAndAggregateIdenticalOrders2() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);

        // 2 orders identical (with modifiers), same batch
        List<Modifier> modifiers = new ArrayList<>();
        modifier1.setPriceOverride(3);
        modifier1.setPrice(3);
        modifier1.setTaxRate(tax1);
        modifier1.setModifierValue("foo");
        modifiers.add(modifier1);
        amendOrder(order1, menuItem1, menuItem1.getPrice(), 1, tax1, modifiers);
        amendOrder(order2, menuItem1, menuItem1.getPrice(), 1, tax1, modifiers);
        amendOrder(order3, menuItem2, menuItem2.getPrice(), 1, tax1, new ArrayList<>());
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        PrinterTicketsResponse ticketsResponse = ticketsResponses.get(0);
        assertNotNull(ticketsResponse.getBatchId());
        assertEquals(1, ticketsResponse.getCourses().size());
        PrinterTicketsCourseView printerTicketsCourseView = ticketsResponse.getCourses().get(0);
        assertEquals(2, printerTicketsCourseView.getItems().size());
        PrinterTicketView printerTicketView1 = printerTicketsCourseView.getItems().stream().filter(p -> p.getMenuItemId().equals(menuItem1.getId())).findFirst().orElse(null);
        PrinterTicketView printerTicketView2 = printerTicketsCourseView.getItems().stream().filter(p -> p.getMenuItemId().equals(menuItem2.getId())).findFirst().orElse(null);
        assertEquals(splitSortJoin(order1.getId() + "," + order2.getId()), splitSortJoin(printerTicketView1.getOrderId()));
        assertEquals(2, printerTicketView1.getQuantity());
        assertEquals(order3.getId(), printerTicketView2.getOrderId());
        assertEquals(1, printerTicketView2.getQuantity());
    }

    private String splitSortJoin(String id) {
        String[] bits = id.split(",");
        Arrays.sort(bits);
        return StringUtils.join(bits, ",");
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWithBatchesAndAggregateIdenticalOrders3() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);

        // 3 orders (with different modifiers), same batch
        List<Modifier> modifiers1 = new ArrayList<>();
        modifier1.setPriceOverride(3);
        modifier1.setPrice(3);
        modifier1.setTaxRate(tax1);
        modifier1.setModifierValue("foo");
        modifiers1.add(modifier1);
        List<Modifier> modifiers2 = new ArrayList<>();
        modifier2.setPriceOverride(4);
        modifier2.setPrice(4);
        modifier2.setTaxRate(tax1);
        modifier2.setModifierValue("bar");
        modifiers2.add(modifier2);
        amendOrder(order1, menuItem1, menuItem1.getPrice(), 1, tax1, modifiers1);
        amendOrder(order2, menuItem1, menuItem1.getPrice(), 1, tax1, modifiers2);
        amendOrder(order3, menuItem2, menuItem2.getPrice(), 1, tax1, new ArrayList<>());
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        PrinterTicketsResponse ticketsResponse = ticketsResponses.get(0);
        assertNotNull(ticketsResponse.getBatchId());
        assertEquals(1, ticketsResponse.getCourses().size());
        PrinterTicketsCourseView printerTicketsCourseView = ticketsResponse.getCourses().get(0);
        assertEquals(3, printerTicketsCourseView.getItems().size());
        assertTrue(printerTicketsCourseView.getItems().stream().allMatch(p -> p.getQuantity() == 1));
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWithBatchesAndAggregateIdenticalOrders4() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);

        // 2 orders identical (with null modifiers), same batch
        amendOrder(order1, menuItem1, menuItem1.getPrice(), 1, tax1, null);
        amendOrder(order2, menuItem1, menuItem1.getPrice(), 1, tax1, null);
        amendOrder(order3, menuItem2, menuItem2.getPrice(), 1, tax1, new ArrayList<>());
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        PrinterTicketsResponse ticketsResponse = ticketsResponses.get(0);
        assertNotNull(ticketsResponse.getBatchId());
        assertEquals(1, ticketsResponse.getCourses().size());
        PrinterTicketsCourseView printerTicketsCourseView = ticketsResponse.getCourses().get(0);
        assertEquals(2, printerTicketsCourseView.getItems().size());
        PrinterTicketView printerTicketView1 = printerTicketsCourseView.getItems().stream().filter(p -> p.getMenuItemId().equals(menuItem1.getId())).findFirst().orElse(null);
        PrinterTicketView printerTicketView2 = printerTicketsCourseView.getItems().stream().filter(p -> p.getMenuItemId().equals(menuItem2.getId())).findFirst().orElse(null);
        assertEquals(splitSortJoin(order1.getId() + "," + order2.getId()), splitSortJoin(printerTicketView1.getOrderId()));
        assertEquals(2, printerTicketView1.getQuantity());
        assertEquals(order3.getId(), printerTicketView2.getOrderId());
        assertEquals(1, printerTicketView2.getQuantity());
    }

    @Test
    public void getOrdersAllPrintersAggregateByBatchWithBatchesAndAggregateIdenticalOrders5() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);

        // 0 orders identical (with null modifiers), same batch, different menu items
        amendOrder(order1, menuItem1, menuItem1.getPrice(), 1, tax1, null);
        amendOrder(order2, menuItem2, menuItem1.getPrice(), 1, tax1, null);
        amendOrder(order3, menuItem3, menuItem2.getPrice(), 1, tax1, null);
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batch1.setIntendedPrintTime(session1.getStartTime() + 60000);
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("aggregateBySession", "false")
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        PrinterTicketsResponse printerTicketsResponse = ticketsResponses.get(0);
        assertEqual(printerTicketsResponse, session1, false);
        PrinterTicketsCourseView printerTicketsCourseView = printerTicketsResponse.getCourses().get(0);
        assertEquals(3, printerTicketsCourseView.getItems().size());
    }

    @Test
    public void getOrdersAllPrinters3() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);
        setUpOrders(session2, restaurant1, false, course1);
        setUpOrders(session3, restaurant1, false, course1);
        setUpOrders(session4, restaurant1, false, course1);
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(4, ticketsResponses.size());
        assertEqual(ticketsResponses.stream().filter(p -> p.getSessionId().equals(session1.getId())).findFirst().orElse(null), session1, true); //behave like session aggregation, because there's no batch
        assertEqual(ticketsResponses.stream().filter(p -> p.getSessionId().equals(session2.getId())).findFirst().orElse(null), session2, true); //behave like session aggregation, because there's no batch
        assertEqual(ticketsResponses.stream().filter(p -> p.getSessionId().equals(session3.getId())).findFirst().orElse(null), session3, true); //behave like session aggregation, because there's no batch
        assertEqual(ticketsResponses.stream().filter(p -> p.getSessionId().equals(session4.getId())).findFirst().orElse(null), session4, true); //behave like session aggregation, because there's no batch
    }

    @Test
    public void getOrdersAllPrintersWhenDone() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);
        List<Order> allOrders = orderRepository.findAll();
        allOrders.forEach(o -> o.setDoneTime(System.currentTimeMillis()));
        orderRepository.save(allOrders);

        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batch1.setIntendedPrintTime(session1.getStartTime() + 60000);
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        assertEqual(ticketsResponses.get(0), session1, false);
    }

    @Test
    public void getOrdersOnePrinter() throws Exception {
        String token = getTokenForStaff(staff1);

        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem2.setDefaultPrinter(printer2.getId());
        menuItem3.setDefaultPrinter(printer2.getId());

        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        setUpOrders(session1, restaurant1, true, course1);
        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("printerId", printer1.getId())
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        assertTrue(ticketsResponses.stream().allMatch(p -> p.getCourses().stream().allMatch(c -> c.getItems().stream().allMatch(i -> i.getMenuItemId().equals(menuItem1.getId())))));
    }

    @Test
    public void getOrdersOnePrinterTakeaway() throws Exception {
        String token = getTokenForStaff(staff1);
        restaurant1.setDefaultTakeawayPrinterId(printer1.getId());
        restaurantRepository.save(restaurant1);

        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem2.setDefaultPrinter(printer2.getId());
        menuItem3.setDefaultPrinter(printer2.getId());

        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);

        setUpOrders(session1, restaurant1, true, course1);
        session1.setSessionType(SessionType.TAKEAWAY);
        sessionRepository.save(session1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("printerId", printer1.getId())
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("printerId", printer2.getId())
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(0, ticketsResponses.size());
    }

    @Test
    public void getOrdersTwoCourses() throws Exception {
        String token = getTokenForStaff(staff1);

        setUpOrders(session1, restaurant1, true, course1);
        order1.setCourseId(null);
        orderRepository.save(order1);

        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);

        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch1.getOrderIds().add(order3.getId());
        batch1.setIntendedPrintTime(session1.getStartTime() + 60000);
        batchRepository.save(batch1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("printerId", printer1.getId())
                .get("Kitchen/Orders");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        List<PrinterTicketsResponse> ticketsResponses = Arrays.asList(response.getBody().as(PrinterTicketsResponse[].class, ObjectMapperType.JACKSON_2));
        assertEquals(1, ticketsResponses.size());
        assertEqual(ticketsResponses.get(0), session1, false);
    }

    @Test
    public void putMarkDone() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        MarkItemDoneRequest request = new MarkItemDoneRequest();
        request.setOrderId("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/Done");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());

        request.setOrderId(order1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/Done");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
    }

    @Test
    public void putMarkDoneMultiple() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        MarkItemDoneRequest request = new MarkItemDoneRequest();
        request.setOrderId(order1.getId() + "," + order2.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/Done");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order2.getId()).getDoneTime());
    }

    @Test
    public void putUnMarkDone() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        order1.setDoneTime(System.currentTimeMillis());
        orderRepository.save(order1);

        MarkItemDoneRequest request = new MarkItemDoneRequest();
        request.setOrderId("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/Undone");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());

        request.setOrderId(order1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/Undone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNull(orderRepository.findOne(order1.getId()).getDoneTime());
    }

    @Test
    public void putUnMarkDoneMultiple() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        order1.setDoneTime(System.currentTimeMillis());
        order2.setDoneTime(System.currentTimeMillis());
        orderRepository.save(order1);
        orderRepository.save(order2);

        MarkItemDoneRequest request = new MarkItemDoneRequest();
        request.setOrderId(order1.getId() + "," + order2.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/Undone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNull(orderRepository.findOne(order2.getId()).getDoneTime());
    }

    @Test
    public void putAllMarkDone() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        MarkAllItemsDoneRequest request = new MarkAllItemsDoneRequest();
        request.setSessionId("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllDone");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());

        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);
        request.setSessionId(session1.getId());
        request.setPrinterId(printer1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllDone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order2.getId()).getDoneTime());
        assertNull(orderRepository.findOne(order3.getId()).getDoneTime());

        request.setPrinterId(printer2.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllDone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order2.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order3.getId()).getDoneTime());
    }

    @Test
    public void putAllMarkDone2() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        MarkAllItemsDoneRequest request = new MarkAllItemsDoneRequest();
        request.setSessionId(session1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllDone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order2.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order3.getId()).getDoneTime());
    }

    @Test
    public void putAllMarkDoneWithBatchId() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);
        session1.setSessionType(SessionType.SEATED);
        sessionRepository.save(session1);
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch2.setSessionId(session1.getId());
        batch2.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);

        MarkAllItemsDoneRequest request = new MarkAllItemsDoneRequest();
        request.setSessionId(session1.getId());
        request.setBatchId(batch1.getId());
        request.setPrinterId(printer1.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllDone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order2.getId()).getDoneTime());
        assertNull(orderRepository.findOne(order3.getId()).getDoneTime());
    }

    @Test
    public void putAllMarkDoneWithBatchIdTakeaway() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);
        session1.setSessionType(SessionType.TAKEAWAY);
        batch1.setSessionId(session1.getId());
        batch1.getOrderIds().add(order1.getId());
        batch1.getOrderIds().add(order2.getId());
        batch2.setSessionId(session1.getId());
        batch2.getOrderIds().add(order3.getId());
        batchRepository.save(batch1);
        batchRepository.save(batch2);
        batchRepository.save(batch3);

        MarkAllItemsDoneRequest request = new MarkAllItemsDoneRequest();
        request.setSessionId(session1.getId());
        request.setBatchId(batch1.getId());
        request.setPrinterId(printer2.getId());

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllDone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNotNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order2.getId()).getDoneTime());
        assertNotNull(orderRepository.findOne(order3.getId()).getDoneTime());
    }

    @Test
    public void putAllUnMarkDone() throws Exception {
        String token = getTokenForStaff(staff1);
        setUpOrders(session1, restaurant1, true, course1);

        order1.setDoneTime(System.currentTimeMillis());
        orderRepository.save(order1);
        order2.setDoneTime(System.currentTimeMillis());
        orderRepository.save(order2);
        order3.setDoneTime(System.currentTimeMillis());
        orderRepository.save(order3);

        MarkAllItemsDoneRequest request = new MarkAllItemsDoneRequest();
        request.setSessionId("foo");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllUndone");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());

        request.setSessionId(session1.getId());
        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .body(request)
                .put("Kitchen/AllUndone");

        assertEquals(HttpStatus.OK.value(), response.statusCode());
        assertNull(orderRepository.findOne(order1.getId()).getDoneTime());
        assertNull(orderRepository.findOne(order2.getId()).getDoneTime());
        assertNull(orderRepository.findOne(order3.getId()).getDoneTime());
    }

    private void assertEqual(PrinterTicketsResponse response, Session session) {
        assertEqual(response, session, true);
    }

    private void assertEqual(PrinterTicketsResponse response, Session session, boolean isAggregatedSession) {
        Map<String,Table> tableMap = restaurantRepository.findOne(session.getRestaurantId()).getTables().stream().collect(Collectors.toMap(Table::getId, Function.identity()));

        assertEquals(session.getId(), response.getSessionId());
        if(isAggregatedSession) {
            assertEquals("12:00, 01/06/2017", response.getSessionTime());
        } else {
            assertEquals("12:01, 01/06/2017", response.getSessionTime());
        }
        assertEquals(areOrdersDone(), response.isDone());
        if(session.getSessionType() == SessionType.SEATED) {
            assertTrue(response.getTitle().startsWith("Dine in"));
            for(String tableId : session.getTables()) {
                assertTrue(response.getTitle().contains(tableMap.get(tableId).getName()));
            }
            assertNull(response.getPartyName());
            if(session.getTables().size() > 0) {
                assertEquals(table1.getName() + ", " + table2.getName(), response.getTableName());
            }
            assertEquals(session.getDiners().size()-1, response.getCovers().intValue());
        } else if(session.getSessionType() == SessionType.TAKEAWAY) {
            assertTrue(response.getTitle().startsWith("Takeaway"));
            if(session.getTakeawayType() == TakeawayType.COLLECTION) {
                assertTrue(response.getTitle().contains(TakeawayType.COLLECTION.toString()));
            } else if(session.getTakeawayType() == TakeawayType.DELIVERY) {
                assertTrue(response.getTitle().contains(TakeawayType.DELIVERY.toString()));
            } else {
                fail();
            }
            //assertEquals(session.getName(), response.getPartyName());
            assertNull(response.getTableName());
            assertNull(response.getCovers());
        } else if(session.getSessionType() == SessionType.TAB) {
            assertEquals("Tab", response.getTitle());
            assertEquals(session.getName(), response.getPartyName());
            assertNull(response.getTableName());
            assertEquals(session.getDiners().size()-1, response.getCovers().intValue());
        }

        assertTrue(response.getCourses().size() > 0);
        for(PrinterTicketsCourseView courseView : response.getCourses()) {
            Map<String,Order> allOrders = orderRepository.findAll().stream().collect(Collectors.toMap(Order::getId, Function.identity()));
            Map<String,MenuItem> allMenuItems = menuItemRepository.findAll().stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
            for(PrinterTicketView printerTicketView : courseView.getItems()) {
                Order correspondingOrder = allOrders.get(printerTicketView.getOrderId());
                if(correspondingOrder.getCourseId() == null) {
                    assertEquals(RestaurantConstants.IMMEDIATE_COURSE_NAME, courseView.getCourse());
                } else {
                    assertEquals(course1.getName(), courseView.getCourse());
                }
                assertEquals(correspondingOrder.getMenuItemId(), printerTicketView.getMenuItemId());
                assertEquals(allMenuItems.get(correspondingOrder.getMenuItemId()).getName(), printerTicketView.getMenuItemName());
                assertEquals(correspondingOrder.getQuantity(), printerTicketView.getQuantity());
                assertEquals(correspondingOrder.getDoneTime() != null, printerTicketView.isDone());
                if(correspondingOrder.getDoneTime() != null) {
                    assertEquals(correspondingOrder.getDoneTime(), printerTicketView.getDoneTime());
                }
                if(correspondingOrder.getNote() != null) {
                    assertEquals(correspondingOrder.getNote(), printerTicketView.getNote());
                }
                if(correspondingOrder.getModifiers().size() > 0) {
                    assertEquals(correspondingOrder.getModifiers().size(), printerTicketView.getModifiers().size());
                    for(int i = 0; i < correspondingOrder.getModifiers().size(); i++) {
                        assertEquals(correspondingOrder.getModifiers().get(i).getModifierValue(), printerTicketView.getModifiers().get(i));
                    }
                }
            }
        }
    }

    private boolean areOrdersDone() {
        return orderRepository.findAll().stream().allMatch(o -> o.getDoneTime() != null);
    }
}