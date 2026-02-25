package uk.co.epicuri.serverapi.engines;

import org.junit.Before;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish.
 */
public abstract class AggregatorTestBase extends BaseIT{
    protected List<Order> orderList1 = new ArrayList<>();
    protected List<Order> orderList2 = new ArrayList<>();
    protected List<Order> orderList3 = new ArrayList<>();

    protected long start = 1497870000000L-1;
    protected long end = 1498647600000L+1;

    public void setUp() throws Exception {
        super.setUp();

        restaurant1.setIANATimezone("Europe/London");
        restaurantRepository.save(restaurant1);

        menuItem1 = prepMenuItem(menuItem1, ItemType.FOOD, 10);
        menuItem2 = prepMenuItem(menuItem2, ItemType.DRINK, 20);
        menuItem3 = prepMenuItem(menuItem3, ItemType.OTHER, 30);

        session1 = prepSession(session1, 1497870000000L);
        session2 = prepSession(session2, 1498129200000L);
        session3 = prepSession(session3, 1498474800000L);

        orderList1.clear();
        orderList2.clear();
        orderList3.clear();
        orderList1.add(createOrder(1497870000000L, session1.getId(), menuItem1)); //monday
        orderList1.add(createOrder(1497956400000L, session1.getId(), menuItem2)); //tuesday
        orderList1.add(createOrder(1498042800000L, session1.getId(), menuItem3)); //wednesday
        orderList2.add(createOrder(1498129200000L, session2.getId(), menuItem1)); //thursday
        orderList2.add(createOrder(1498215600000L, session2.getId(), menuItem1)); //friday
        orderList2.add(createOrder(1498302000000L, session2.getId(), menuItem2)); //saturday
        orderList2.add(createOrder(1498388400000L, session2.getId(), menuItem3)); //sunday
        orderList3.add(createOrder(1498474800000L, session3.getId(), menuItem2)); //monday
        orderList3.add(createOrder(1498561200000L, session3.getId(), menuItem1)); //tuesday
        orderList3.add(createOrder(1498647600000L, session3.getId(), menuItem3)); //wednesday
    }

    private Order createOrder(long time, String sessionId, MenuItem item) {
        Order order = new Order();
        order.setTime(time);
        order.setMenuItem(item);
        order.setMenuItemId(item.getId());
        order.setSessionId(sessionId);
        order.setItemPrice(item.getPrice());
        order.setPriceOverride(item.getPrice());
        return orderRepository.insert(order);
    }

    private MenuItem prepMenuItem(MenuItem item, ItemType type, int price) {
        item.setPrice(price);
        item.setRestaurantId(restaurant1.getId());
        item.setType(type);
        item.setTaxTypeId(tax1.getId());
        return menuItemRepository.save(item);
    }

    private Session prepSession(Session session, long startTime) {
        session.setRestaurantId(restaurant1.getId());
        session.setStartTime(startTime);
        return sessionRepository.save(session);
    }
}
