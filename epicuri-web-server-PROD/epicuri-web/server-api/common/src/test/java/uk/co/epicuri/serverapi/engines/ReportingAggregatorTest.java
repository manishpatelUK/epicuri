package uk.co.epicuri.serverapi.engines;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.engines.reporting.OrderAggregate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ReportingAggregatorTest {

    @Test
    public void cleanSessionsNoFilter() throws Exception {
        List<Session> sessions = getMixedSessions();
        ReportingAggregator.cleanSessions(sessions, false, false);
        assertEquals(3, sessions.size());
    }

    @Test
    public void cleanSessionsRemoveReports() throws Exception {
        List<Session> sessions = getMixedSessions();
        ReportingAggregator.cleanSessions(sessions, true, false);
        assertEquals(2, sessions.size());
        assertFalse(sessions.stream().anyMatch(s -> s.getName().equals("3")));
    }

    @Test
    public void cleanSessionsDeleted() throws Exception {
        List<Session> sessions = getMixedSessions();
        ReportingAggregator.cleanSessions(sessions, false, true);
        assertEquals(2, sessions.size());
        assertFalse(sessions.stream().anyMatch(s -> s.getName().equals("2")));
    }

    @Test
    public void cleanSessionsFullClean() throws Exception {
        List<Session> sessions = getMixedSessions();
        ReportingAggregator.cleanSessions(sessions, true, true);
        assertEquals(1, sessions.size());
        assertFalse(sessions.stream().anyMatch(s -> s.getName().equals("2")));
        assertFalse(sessions.stream().anyMatch(s -> s.getName().equals("3")));
    }

    @Test
    public void cleanOrdersNoFilter() throws Exception {
        List<Order> orders = getMixedOrders();
        ReportingAggregator.cleanOrders(orders, false, false);
        assertEquals(3, orders.size());
    }

    @Test
    public void cleanOrdersRemoveReports() throws Exception {
        List<Order> orders = getMixedOrders();
        ReportingAggregator.cleanOrders(orders, true, false);
        assertEquals(2, orders.size());
        assertFalse(orders.stream().anyMatch(s -> s.getNote().equals("3")));
    }

    @Test
    public void cleanOrdersDeleted() throws Exception {
        List<Order> orders = getMixedOrders();
        ReportingAggregator.cleanOrders(orders, false, true);
        assertEquals(2, orders.size());
        assertFalse(orders.stream().anyMatch(s -> s.getNote().equals("2")));
    }

    @Test
    public void cleanOrdersFullClean() throws Exception {
        List<Order> orders = getMixedOrders();
        ReportingAggregator.cleanOrders(orders, true, true);
        assertEquals(1, orders.size());
        assertFalse(orders.stream().anyMatch(s -> s.getNote().equals("2")));
        assertFalse(orders.stream().anyMatch(s -> s.getNote().equals("3")));
    }

    @Test
    public void aggregateByMenuItem() throws Exception {
        List<Order> orders = getMixedOrders();
        Map<String,OrderAggregate> aggregateMap = ReportingAggregator.aggregateByMenuItem(orders, getSessionMap());
        assertEquals(2, aggregateMap.size());
        assertEquals(2, aggregateMap.get("1").size());
        assertEquals(1, aggregateMap.get("2").size());
    }

    @Test
    public void aggregateByMenuItemRefund() throws Exception {
        List<Order> orders = getMixedOrders();
        orders.get(1).setDeleted(null);
        orders.get(1).setQuantity(2);
        Map<String,Session> sessionMap = getSessionMap();
        sessionMap.get("s1").setSessionType(SessionType.REFUND);

        Map<String,OrderAggregate> aggregateMap = ReportingAggregator.aggregateByMenuItem(orders, sessionMap);
        assertEquals(2, aggregateMap.size());
        assertEquals(2, aggregateMap.get("1").size());
        assertEquals(1, aggregateMap.get("2").size());

        assertEquals(1, aggregateMap.values().stream().filter(o -> o.getMenuItem().getId().equals("1")).mapToInt(OrderAggregate::getQuantity).sum());
    }

    private Map<String, Session> getSessionMap() {
        Map<String,Session> sessionMap = new HashMap<>();
        sessionMap.put("s1", new Session());
        sessionMap.get("s1").setSessionType(SessionType.SEATED);
        sessionMap.put("s2", new Session());
        sessionMap.get("s2").setSessionType(SessionType.TAKEAWAY);
        sessionMap.put("s3", new Session());
        sessionMap.get("s3").setSessionType(SessionType.ADHOC);
        return sessionMap;
    }

    @Test
    public void aggregateByMenuItemPLU() throws Exception {
        List<Order> orders = getMixedOrders();
        Map<String,OrderAggregate> aggregateMap = ReportingAggregator.aggregateByMenuItemPLU(orders, getSessionMap());
        assertEquals(2, aggregateMap.size());
        assertEquals(2, aggregateMap.get("1").size());
    }

    private List<Session> getMixedSessions() {
        Session session1 = new Session();
        session1.setName("1");

        Session session2 = new Session();
        session2.setName("2");
        session2.setDeleted(0L);

        Session session3 = new Session();
        session3.setName("3");
        session3.setRemoveFromReports(true);

        return Lists.newArrayList(session1, session2, session3);
    }

    private List<Order> getMixedOrders() {
        MenuItem m1 = new MenuItem();
        m1.setId("1");
        m1.setPlu("1");
        Order order1 = new Order();
        order1.setMenuItemId("1");
        order1.setNote("1");
        order1.setMenuItem(m1);
        order1.setSessionId("s1");

        MenuItem m2 = new MenuItem();
        m2.setId("1");
        m2.setPlu("1"); // left as 1 on purpose
        Order order2 = new Order();
        order2.setMenuItemId("1");
        order2.setNote("2");
        order2.setDeleted(0L);
        order2.setMenuItem(m2);
        order2.setSessionId("s2");

        MenuItem m3 = new MenuItem();
        m3.setId("3");
        m3.setPlu("3");
        Order order3 = new Order();
        order3.setMenuItemId("2");
        order3.setNote("3");
        order3.setRemoveFromReports(true);
        order3.setMenuItem(m3);
        order3.setSessionId("s3");

        return Lists.newArrayList(order1, order2, order3);
    }
}