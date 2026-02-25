package uk.co.epicuri.serverapi.engines.reporting;

import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;

import java.util.*;

/**
 * Created by manish on 03/10/2017.
 */
public class OrderAggregate {

    private List<Order> orders = new LinkedList<>();
    private Map<String,Session> orderToSessionMap = new HashMap<>();
    private MenuItem menuItem;
    private int quantity;

    public void add(List<Order> orders, Map<String, Session> sessionMap) {
        orders.forEach(o -> add(o, sessionMap));
    }

    public void add(Order order, Map<String, Session> sessionMap) {
        orderToSessionMap.put(order.getId(), sessionMap.getOrDefault(order.getSessionId(), new Session()));
        int negate = sessionMap.getOrDefault(order.getSessionId(), new Session()).getSessionType() == SessionType.REFUND ? -1 : 1;
        quantity += negate * order.getQuantity();
        if(orders.size() == 0) {
            orders.add(order);
        } else {
            for (int i = 1; i < orders.size(); i++) {
                if(order.getTime() < orders.get(i).getTime()) {
                    orders.add(i-1, order);
                    return;
                }
            }
            orders.add(order);
        }
        if(menuItem == null) {
            menuItem = order.getMenuItem();
        }
    }

    public long lastOrderTime() {
        return orders.size() == 0 ? Long.MAX_VALUE : orders.get(orders.size()-1).getTime();
    }

    public long firstOrderTime() {
        return orders.size() == 0 ? Long.MIN_VALUE : orders.get(0).getTime();
    }

    public int size() {
        return orders.size();
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public List<Order> getOrdersNegated() {
        List<Order> negatedOrders = new ArrayList<>();
        for(Order order : orders) {
            Order newOrder = new Order(order);
            if(orderToSessionMap.get(order.getId()).getSessionType() == SessionType.REFUND) {
                newOrder.setQuantity(-1 * order.getQuantity());
            }
            negatedOrders.add(newOrder);
        }
        return negatedOrders;
    }

    public int getQuantity() {
        return quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }
}
