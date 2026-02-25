package uk.co.epicuri.serverapi.engines;

import org.junit.Before;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.util.OrderSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish.
 */
public class BasicBIAggregatorTest extends AggregatorTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void aggregate() throws Exception {
        BasicBIAggregator aggregator = new BasicBIAggregator(sessionCalculationService, start, end);
        aggregator.addSession(session1, orderList1);
        aggregator.addSession(session2, orderList2);
        aggregator.addSession(session3, orderList3);
        aggregator.aggregate();

        testAggregatorResults(aggregator);



    }

    private void testAggregatorResults(BasicBIAggregator aggregator) {
        assertEquals(3, aggregator.getSessionIds().size());

        Map<String,OrderSummary> orderSummaryByDay = aggregator.getOrderSummaryByDay();
        assertEquals(7, orderSummaryByDay.size());
        assertEquals(1,orderSummaryByDay.get(BasicBIAggregator.DAYS[0]).getMenuItemCount().get(menuItem1.getId()).intValue());
        assertEquals(1,orderSummaryByDay.get(BasicBIAggregator.DAYS[0]).getMenuItemCount().get(menuItem2.getId()).intValue());
        assertNull(orderSummaryByDay.get(BasicBIAggregator.DAYS[0]).getMenuItemCount().get(menuItem3.getId()));
        assertEquals(2,orderSummaryByDay.get(BasicBIAggregator.DAYS[2]).getMenuItemCount().get(menuItem3.getId()).intValue());

        testDayAndSessionCountResults(aggregator);

        final OrderSummary wholeSummary = aggregator.getWholeSummary();
        final Map<String, Integer> menuItemCountWhole = wholeSummary.getMenuItemCount();
        assertEquals(4, menuItemCountWhole.get(menuItem1.getId()).intValue());
        assertEquals(3, menuItemCountWhole.get(menuItem2.getId()).intValue());
        assertEquals(3, menuItemCountWhole.get(menuItem3.getId()).intValue());
    }

    private void testDayAndSessionCountResults(BasicBIAggregator aggregator) {
        final Map<String, Integer> dayCounts = aggregator.getDayCounts();
        assertEquals(2, dayCounts.get(BasicBIAggregator.DAYS[0]).intValue());
        assertEquals(2, dayCounts.get(BasicBIAggregator.DAYS[1]).intValue());
        assertEquals(2, dayCounts.get(BasicBIAggregator.DAYS[2]).intValue());
        assertEquals(1, dayCounts.get(BasicBIAggregator.DAYS[3]).intValue());
        assertEquals(1, dayCounts.get(BasicBIAggregator.DAYS[4]).intValue());
        assertEquals(1, dayCounts.get(BasicBIAggregator.DAYS[5]).intValue());
        assertEquals(1, dayCounts.get(BasicBIAggregator.DAYS[6]).intValue());

        final Map<String, List<Session>> sessionsByDay = aggregator.getSessionsByDay();
        assertEquals(2, sessionsByDay.get(BasicBIAggregator.DAYS[0]).size());
        assertEquals(0, sessionsByDay.get(BasicBIAggregator.DAYS[1]).size());
        assertEquals(0, sessionsByDay.get(BasicBIAggregator.DAYS[2]).size());
        assertEquals(1, sessionsByDay.get(BasicBIAggregator.DAYS[3]).size());
        assertEquals(0, sessionsByDay.get(BasicBIAggregator.DAYS[4]).size());
        assertEquals(0, sessionsByDay.get(BasicBIAggregator.DAYS[5]).size());
        assertEquals(0, sessionsByDay.get(BasicBIAggregator.DAYS[6]).size());
        assertTrue(sessionsByDay.get(BasicBIAggregator.DAYS[0]).stream().map(Session::getId).collect(Collectors.toList()).contains(session1.getId()));
        assertTrue(sessionsByDay.get(BasicBIAggregator.DAYS[0]).stream().map(Session::getId).collect(Collectors.toList()).contains(session3.getId()));
        assertTrue(sessionsByDay.get(BasicBIAggregator.DAYS[3]).stream().map(Session::getId).collect(Collectors.toList()).contains(session2.getId()));
    }

    @Test
    public void aggregateWithMissingItems1() throws Exception {
        BasicBIAggregator aggregator = new BasicBIAggregator(sessionCalculationService, start, end);
        aggregator.addSession(session1, orderList1);
        aggregator.addSession(session2, orderList2);
        aggregator.addSession(session3, orderList3);

        menuItemRepository.deleteAll();

        aggregator.aggregate();
        testAggregatorResults(aggregator);
    }

    @Test
    public void aggregateWithMissingItems2() throws Exception {
        BasicBIAggregator aggregator = new BasicBIAggregator(sessionCalculationService, start, end);
        aggregator.addSession(session1, orderList1);
        aggregator.addSession(session2, orderList2);
        aggregator.addSession(session3, orderList3);

        aggregator.aggregate();

        Map<String,OrderSummary> orderSummaryByDay = aggregator.getOrderSummaryByDay();
        assertEquals(1, orderSummaryByDay.get(BasicBIAggregator.DAYS[0]).getMenuItemCount().get(menuItem1.getId()).intValue());
        assertEquals(1,orderSummaryByDay.get(BasicBIAggregator.DAYS[0]).getMenuItemCount().get(menuItem2.getId()).intValue());

        testDayAndSessionCountResults(aggregator);

        final OrderSummary wholeSummary = aggregator.getWholeSummary();
        final Map<String, Integer> menuItemCountWhole = wholeSummary.getMenuItemCount();
        assertEquals(4, menuItemCountWhole.get(menuItem1.getId()).intValue());
        assertEquals(3, menuItemCountWhole.get(menuItem2.getId()).intValue());
    }
}