package uk.co.epicuri.serverapi.common.pojo;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.customer.OrderAggregation;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish on 03/11/2017.
 */
public class OrderUtilTest {
    private List<Order> orders;

    @Before
    public void setUp() throws Exception {
        orders = new ArrayList<>();

        orders.add(createOrder("1", "m1", 10, 1));
        orders.add(createOrder("2", "m1", 10, 2));
        orders.add(createOrder("3", "m2", 10, 2));
        orders.add(createOrder("4", "m1", 20, 1));

        Order orderWithMod1 = createOrder("5", "m1", 20, 1);
        orderWithMod1.getModifiers().add(createModifier("mod1", 5));
        orders.add(orderWithMod1);

        Order orderWithMod2 = createOrder("6", "m1", 20, 1);
        orderWithMod2.getModifiers().add(createModifier("mod1", 5));
        orders.add(orderWithMod2);

        Order orderWithMod3 = createOrder("7", "m2", 20, 1);
        orderWithMod3.getModifiers().add(createModifier("mod1", 5));
        orders.add(orderWithMod3);

        Order orderWithMod4 = createOrder("8", "m1", 20, 1);
        orderWithMod4.getModifiers().add(createModifier("mod2", 5));
        orders.add(orderWithMod4);
    }

    private Order createOrder(String id, String menuItemId, int price, int quantity) {
        Order order = new Order();
        order.setId(id);
        order.setMenuItemId(menuItemId);
        order.setPriceOverride(price);
        order.setQuantity(quantity);
        return order;
    }

    private Modifier createModifier(String id, int price) {
        Modifier modifier = new Modifier();
        modifier.setId(id);
        modifier.setPriceOverride(price);
        return modifier;
    }

    @Test
    public void aggregateIdenticalOrders() throws Exception {
        List<OrderAggregation> aggregations = OrderUtil.aggregateIdenticalOrders(orders);

        assertEquals(6, aggregations.size());

        OrderAggregation agg1 = aggregations.stream().filter(o -> o.getOrderIds().contains("1")).findFirst().orElse(null);
        assertNotNull(agg1);
        assertEquals(2, agg1.getOrderIds().size());
        assertEquals(3, agg1.getTotalQuantity());

    }

}