package uk.co.epicuri.serverapi.engines;

import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.engines.reporting.OrderAggregate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by manish on 03/10/2017.
 */
public class ReportingAggregator {
    public static void cleanSessions(List<Session> sessions, boolean sessionsRemoveFromReports, boolean sessionDeleted) {
        sessions.removeIf(s -> (sessionsRemoveFromReports && s.isRemoveFromReports()) || (sessionDeleted && s.getDeleted() != null));
    }

    public static void cleanOrders(List<Order> orders, boolean orderRemoveFromReports, boolean orderDeleted) {
        orders.removeIf(o -> (orderRemoveFromReports && o.isRemoveFromReports()) || (orderDeleted && o.getDeleted() != null));
    }

    public static Map<String,OrderAggregate> aggregateByMenuItem(List<Order> orders, Map<String,Session> sessionMap) {
        return aggregateByMenuItemId(orders, sessionMap);
    }

    public static Map<String,OrderAggregate> aggregateByMenuItemId(List<Order> orders, Map<String, Session> sessionMap) {
        Map<String, List<Order>> ordersByMenuItem = orders.stream().collect(Collectors.groupingBy(Order::getMenuItemId));
        return getOrderAggregateMap(ordersByMenuItem, sessionMap);
    }

    public static Map<String,OrderAggregate> aggregateByMenuItemPLU(List<Order> orders, Map<String, Session> sessionMap) {
        Map<String, List<Order>> ordersByMenuItem = orders.stream().collect(Collectors.groupingBy(o -> {
            if(o.getMenuItem() == null || StringUtils.isBlank(o.getMenuItem().getPlu())) {
                return o.getMenuItemId();
            } else {
                return o.getMenuItem().getPlu();
            }
        }));
        return getOrderAggregateMap(ordersByMenuItem, sessionMap);
    }

    private static Map<String, OrderAggregate> getOrderAggregateMap(Map<String, List<Order>> ordersByMenuItem, Map<String, Session> sessionMap) {
        Map<String,OrderAggregate> map = new HashMap<>();
        for (Map.Entry<String, List<Order>> entry : ordersByMenuItem.entrySet()) {
            OrderAggregate orderAggregate = new OrderAggregate();
            orderAggregate.add(entry.getValue(), sessionMap);
            map.put(entry.getKey(), orderAggregate);
        }
        return map;
    }
}
