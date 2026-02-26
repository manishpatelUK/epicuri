package uk.co.epicuri.serverapi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterTicketView;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterTicketsCourseView;
import uk.co.epicuri.serverapi.common.pojo.host.PrinterTicketsResponse;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.Batch;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderScheduleService {
    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private BatchService batchService;

    private final RestaurantDefault timeoutDefault;

    public OrderScheduleService() {
        timeoutDefault = new RestaurantDefault();
        timeoutDefault.setValue(300);
    }

    public List<PrinterTicketsResponse> getScheduleList(String restaurantId, String printerId, boolean aggregateOnSession) {
        return getScheduleList(masterDataService.getRestaurant(restaurantId), printerId, aggregateOnSession);
    }

    public List<PrinterTicketsResponse> getScheduleList(Restaurant restaurant, String printerId, boolean aggregateOnSession) {
        String restaurantId = restaurant.getId();
        List<Session> sessions = getRelevantSessions(restaurant);

        // get all orders from each session
        List<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toList());
        Map<String,List<Order>> orders = liveDataService.getOrdersBySessionIds(sessionIds);

        if(orders.size() == 0) {
            return new ArrayList<>();
        }

        Map<String,MenuItem> items = masterDataService.getAllMenuItems(restaurantId).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        // filter each order on printer
        filterForPrinter(printerId, restaurant.getDefaultTakeawayPrinterId(), orders, items, sessions);

        //take out sessions that don't have any orders
        filterForEmptySessions(orders);

        if(orders.size() == 0) {
            return new ArrayList<>();
        }

        //course map
        Map<String,Course> courseMap = new HashMap<>();
        restaurant.getServices().forEach(s -> s.getCourses().forEach(c -> courseMap.put(c.getId(), c)));

        // create payload
        List<PrinterTicketsResponse> responses = new ArrayList<>();
        Set<String> ordersKeys = orders.keySet();

        Map<String,List<Batch>> batchesBySession = batchService.getBatchesBySessionIds(sessionIds).stream().collect(Collectors.groupingBy(Batch::getSessionId));
        sessions.stream().filter(s -> ordersKeys.contains(s.getId())).forEach(s -> {
            if(aggregateOnSession) {
                PrinterTicketsResponse response = getPrinterTicketsAggregatedBySession(restaurant, orders.get(s.getId()), items, courseMap, s);
                responses.add(response);
            } else {
                List<PrinterTicketsResponse> response = getPrinterTicketsAggregatedByBatch(restaurant, orders.get(s.getId()), items, courseMap, s,batchesBySession.get(s.getId()));
                responses.addAll(response);
            }
        });

        Collections.sort(responses);

        return responses;
    }

    public List<PrinterTicketsResponse> getPrinterTicketsAggregatedByBatch(Restaurant restaurant, List<Order> orders, Map<String, MenuItem> items, Map<String, Course> courseMap, Session session, List<Batch> batches) {
        //if there is no batch, revert to aggregate by session
        if(batches == null || batches.size() == 0) {
            return Collections.singletonList(getPrinterTicketsAggregatedBySession(restaurant, orders, items, courseMap, session));
        }

        List<PrinterTicketsResponse> tickets = new ArrayList<>();
        for(Batch batch : batches) {
            List<Order> ordersForBatch = orders.stream().filter(o-> batch.getOrderIds().contains(o.getId())).collect(Collectors.toList());
            if(ordersForBatch.size() == 0) {
                continue;
            }

            PrinterTicketsResponse ticket = getPrinterTicketsAggregatedBySession(restaurant, ordersForBatch, items, courseMap, session);
            if(session.getClosedTime() == null) {
                ticket.amendSessionTime(batch.getIntendedPrintTime(), restaurant.getIANATimezone());
            } else if(session.getClosedTime() != null && session.getSessionType() != SessionType.TAKEAWAY){
                ticket.amendSessionTime(session.getClosedTime(), restaurant.getIANATimezone());
            }
            ticket.setBatchId(batch.getId());
            tickets.add(ticket);
        }

        return tickets;
    }

    public PrinterTicketsResponse getPrinterTicketsAggregatedBySession(Restaurant restaurant, List<Order> orders, Map<String, MenuItem> items, Map<String, Course> courseMap, Session session) {
        orders = createOrderAggregates(orders);

        PrinterTicketsResponse response = new PrinterTicketsResponse(restaurant, session);
        Map<Short,List<Order>> courses = new TreeMap<>();
        orders.forEach(o -> {
            Course course = courseMap.get(o.getCourseId());
            final short courseOrder = course == null ? -1 : course.getOrdering();
            if(!courses.containsKey(courseOrder)) {
                courses.put(courseOrder, new ArrayList<>());
            }
            courses.get(courseOrder).add(o);
        });
        courses.forEach((k,v) -> {
            PrinterTicketsCourseView printerTicketsCourseView = new PrinterTicketsCourseView();
            String courseName = courseMap.get(v.get(0).getCourseId()) == null ? RestaurantConstants.IMMEDIATE_COURSE_NAME : courseMap.get(v.get(0).getCourseId()).getName();
            printerTicketsCourseView.setCourse(courseName);
            v.forEach( o-> {
                printerTicketsCourseView.getItems().add(new PrinterTicketView(o, items.get(o.getMenuItemId())));
            });
            Collections.sort(printerTicketsCourseView.getItems());
            response.getCourses().add(printerTicketsCourseView);
        });
        response.setDone(response.getCourses().stream().allMatch(p -> p.getItems().stream().allMatch(PrinterTicketView::isDone)));
        response.setSessionType(session.getSessionType());
        if(session.getSessionType() == SessionType.TAKEAWAY) {
            response.setTakeawayType(session.getTakeawayType());
        }
        if(session.getClosedTime() != null && session.getSessionType() != SessionType.ADHOC) {
            response.setDone(true);
            traverseAndSetAllDone(response);
            if(session.getSessionType() != SessionType.TAKEAWAY) {
                response.amendSessionTime(session.getClosedTime(), restaurant.getIANATimezone());
            }
        } else if(session.getClosedTime() != null && session.getSessionType() == SessionType.ADHOC) {
            RestaurantDefault restaurantDefault = restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.TICKET_ON_SESSION_CLOSE_TIMEOUT)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.TICKET_ON_SESSION_CLOSE_TIMEOUT, 300));
            long limit = System.currentTimeMillis() - (((Number)restaurantDefault.getValue()).intValue() * 1000);
            if(!response.isDone() && session.getStartTime() < limit) {
                traverseAndSetAllDone(response);
            }

        }

        // in the case of quick order where there is a delivery location, change the title to include table name
        if(session.getSessionType() == SessionType.ADHOC) {
            RestaurantDefault forceLocation = restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.FORCE_LOCATION_ON_QO)).findFirst().orElse(null);
            if(forceLocation != null && forceLocation.getValue() != null && forceLocation.getValue() instanceof Boolean && ((Boolean)forceLocation.getValue())) {
                List<String> deliveryLocations = orders.stream().filter(o -> o.getDeliveryLocation() != null && o.getDeliveryLocation().length() > 0).map(Order::getDeliveryLocation).collect(Collectors.toList());
                String locations = StringUtils.join(deliveryLocations, ", ").trim();
                response.setTableName(locations);
            }
        }

        return response;
    }

    public void traverseAndSetAllDone(PrinterTicketsResponse response) {
        for(PrinterTicketsCourseView printerTicketsCourseView : response.getCourses()) {
            for(PrinterTicketView printerTicketView : printerTicketsCourseView.getItems()) {
                printerTicketView.setDone(true);
            }
        }
    }

    private List<Order> createOrderAggregates(List<Order> orders) {
        //identify identical orders (as far as the kitchen is concerned)
        Map<Integer,List<Order>> identicalOrders = new HashMap<>();
        for(Order order : orders) {
            int code = order.hashCode("id", "time", "modifiers", "publicFacingOrderId", "deliveryLocation");
            if(!identicalOrders.containsKey(code)) {
                identicalOrders.put(code, new ArrayList<>());
            }
            identicalOrders.get(code).add(order);
        }

        // for orders that are still unique, don't do anything. But for aggregated items - plonk them together and give them a special Order Id
        List<Order> aggregatedOrders = new ArrayList<>();
        for(List<Order> identicalOrderSet : identicalOrders.values()) {
            if(identicalOrderSet.size() == 1) {
                aggregatedOrders.addAll(identicalOrderSet);
            } else {
                aggregatedOrders.add(collapseIntoSingular(identicalOrderSet));
            }
        }

        return aggregatedOrders;
    }

    private Order collapseIntoSingular(List<Order> identicalOrderSet) {
        Order order = identicalOrderSet.get(0);
        List<String> ids = new ArrayList<>();
        ids.add(order.getId());
        for(int i = 1; i < identicalOrderSet.size(); i++) {
            Order orderToAdd = identicalOrderSet.get(i);
            ids.add(orderToAdd.getId());
            order.setQuantity(order.getQuantity() + orderToAdd.getQuantity());
        }
        order.setId(StringUtils.join(ids, ","));
        return order;
    }

    private void filterForEmptySessions(Map<String, List<Order>> orders) {
        orders.entrySet().removeIf(s -> orders.get(s.getKey()).size() == 0);
    }

    private void filterForPrinter(String printerId, String takeawayPrinterId, Map<String, List<Order>> orders, Map<String, MenuItem> items, List<Session> sessions) {
        if(StringUtils.isBlank(printerId)) {
            return;
        }

        Map<String,Session> sessionMap = sessions.stream().collect(Collectors.toMap(Session::getId, Function.identity()));

        orders.forEach((k,v) -> filterForPrinter(printerId, takeawayPrinterId, items, v, sessionMap.get(k)));
    }

    private void filterForPrinter(String printerId, String takeawayPrinterId, Map<String, MenuItem> items, List<Order> orders, Session session) {
        if(session.getSessionType() != SessionType.TAKEAWAY) {
            final Iterator<Order> iterator = orders.iterator();
            while (iterator.hasNext()) {
                Order o = iterator.next();
                if (items.containsKey(o.getMenuItemId())) {
                    String designatedPrinterId = items.get(o.getMenuItemId()).getDefaultPrinter();
                    if (designatedPrinterId != null && !designatedPrinterId.equals(printerId)) {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            }
        }

        if(session.getSessionType() == SessionType.TAKEAWAY && !printerId.equals(takeawayPrinterId)) {
            //remove all
            orders.clear();
        }
    }

    public List<Session> getRelevantSessions(Restaurant restaurant) {
        Map<String, RestaurantDefault> defaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));

        // get the live sessions, exclude QuickOrder
        List<Session> sessions = sessionService.getLiveSessions(restaurant.getId());
        sessions.removeIf(s -> s.getSessionType() == SessionType.REFUND);

        // get closed sessions
        //filter for within FixedDefaults.TICKET_ON_SESSION_CLOSE_TIMEOUT
        long now = System.currentTimeMillis();
        long limit = now - (1000*(int)(defaultMap.getOrDefault(FixedDefaults.TICKET_ON_SESSION_CLOSE_TIMEOUT, timeoutDefault).getValue()));
        List<Session> closedSessions = sessionService.getClosedSessions(restaurant.getId(), defaultMap)
                .stream().filter(s -> s.getClosedTime() != null && s.getClosedTime() > limit).collect(Collectors.toList());

        sessions.addAll(closedSessions);
        return sessions;
    }
}
