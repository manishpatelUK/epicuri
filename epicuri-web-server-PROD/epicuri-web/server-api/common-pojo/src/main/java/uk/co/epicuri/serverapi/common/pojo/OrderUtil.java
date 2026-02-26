package uk.co.epicuri.serverapi.common.pojo;

import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderView;
import uk.co.epicuri.serverapi.common.pojo.customer.OrderAggregation;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by manish on 03/11/2017.
 */
public class OrderUtil {
    public static boolean isSameOrderContent(Order order1, Order order2) {
        if(order1.getId().equals(order2.getId())) {
            return true;
        }

        if(!order1.getMenuItemId().equals(order2.getMenuItemId())) {
            return false;
        }

        if(order1.getPriceOverride() != order2.getPriceOverride()) {
            return false;
        }

        if(order1.getModifiers().size() > 0 && order2.getModifiers().size() > 0) {
            if(order1.getModifiers().size() != order2.getModifiers().size()) {
                return false;
            }

            Map<String, Modifier> modifierMap = order1.getModifiers().stream().collect(Collectors.toMap(Modifier::getId, Function.identity()));
            for(Modifier modifier : order2.getModifiers()) {
                if(!modifierMap.containsKey(modifier.getId())) {
                    return false;
                }

                if(!isSameModifierContent(modifierMap.get(modifier.getId()), modifier)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isSameModifierContent(Modifier modifier1, Modifier modifier2) {
        return modifier1.equals(modifier2);
    }

    public static int orderHash(Order order) {
        StringBuilder builder = new StringBuilder(order.getMenuItemId());
        builder.append(":")
                .append(order.getPriceOverride())
                .append(":");
        for(Modifier modifier : order.getModifiers()) {
            builder.append(modifier.getId())
                    .append(":")
                    .append(modifier.getPriceOverride());
        }
        return builder.toString().hashCode();
    }

    public static int orderHashView(CustomerOrderView order) {
        StringBuilder builder = new StringBuilder(order.getMenuItemId());
        builder.append(":")
                .append(order.getPriceOverride())
                .append(":");
        for(String modifier : order.getModifiers()) {
            builder.append(modifier);
        }
        return builder.toString().hashCode();
    }

    public static List<OrderAggregation> aggregateIdenticalOrders(List<Order> orders) {
        Map<Integer,OrderAggregation> ordersMap = new HashMap<>();
        for(Order order : orders) {
            int orderHash = orderHash(order);
            if(!ordersMap.containsKey(orderHash)) {
                OrderAggregation aggregation = new OrderAggregation();
                aggregation.setMenuItemId(order.getMenuItemId());
                ordersMap.put(orderHash, aggregation);
            }

            ordersMap.get(orderHash).getOrderIds().add(order.getId());
            ordersMap.get(orderHash).increment(order.getQuantity());
        }

        return new ArrayList<>(ordersMap.values());
    }

    public static List<OrderAggregation> aggregateIdenticalOrderViews(List<CustomerOrderView> orders) {
        Map<Integer,OrderAggregation> ordersMap = new HashMap<>();
        for(CustomerOrderView order : orders) {
            int orderHash = orderHashView(order);
            if(!ordersMap.containsKey(orderHash)) {
                OrderAggregation aggregation = new OrderAggregation();
                aggregation.setMenuItemId(order.getMenuItemId());
                ordersMap.put(orderHash, aggregation);
            }

            ordersMap.get(orderHash).getOrderIds().add(order.getId());
            ordersMap.get(orderHash).increment(order.getQuantity());
        }

        return new ArrayList<>(ordersMap.values());
    }
}
