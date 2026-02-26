package uk.co.epicuri.serverapi.engines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.CalculationKey;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.service.SessionCalculationService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by manish on 23/06/2017.
 */
public abstract class SessionAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAggregator.class);

    protected final SessionCalculationService sessionCalculationService;
    protected final Map<String,Session> allSessions = new HashMap<>();
    protected final Map<String,List<Order>> allOrders = new HashMap<>();
    protected final Map<Session,Map<CalculationKey,Number>> calculatedValues = new HashMap<>();
    protected final List<Order> allOrdersList = new ArrayList<>();


    protected final List<Order> emptyOrderList = Collections.emptyList();

    public SessionAggregator(SessionCalculationService sessionCalculationService) {
        this.sessionCalculationService = sessionCalculationService;
    }

    public void addSession(Session session, List<Order> orders) {
        LOGGER.trace("Add session {} with {} orders", session.getId(), orders == null ? "0[null]" : orders.size());
        allSessions.put(session.getId(), session);
        if(orders == null) {
            orders = emptyOrderList;
        }
        prepareOrders(orders);
        allOrders.put(session.getId(), orders);
        allOrdersList.addAll(orders);
        calculatedValues.put(session, sessionCalculationService.calculateValues(session, orders));
    }

    public void prepareOrders(List<Order> orders) {
        if(orders == null) {
            return;
        }
        orders.removeIf(Order::isRemoveFromReports);
    }

    public List<String> getSessionIds() {
        return new ArrayList<>(allSessions.keySet());
    }

    public Map<String, List<Order>> getAllOrders() {
        return allOrders;
    }

    public abstract void aggregate();
}
