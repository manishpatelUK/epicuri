package uk.co.epicuri.serverapi.engines;

import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.service.SessionCalculationService;
import uk.co.epicuri.serverapi.service.util.OrderSummary;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by manish
 */
public class BasicBIAggregator extends SessionAggregator {

    public final static String[] DAYS = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    public final static String SEATED = "seated";
    public final static String TAKEAWAY_COLLECTION = "collection";
    public final static String TAKEAWAY_DELIVERY = "delivery";
    private final long start;
    private final long end;
    private Map<String, OrderSummary> orderSummaryByDay = new HashMap<>();
    private Map<String, List<Session>> sessionsByDay = new HashMap<>();
    private Map<String,Integer> dayCounts = new HashMap<>();
    private OrderSummary wholeSummary;

    public BasicBIAggregator(SessionCalculationService sessionCalculationService, long start, long end) {
        super(sessionCalculationService);
        this.start = start;
        this.end = end;
    }

    @Override
    public void addSession(Session session, List<Order> orders) {
        if(session.getSessionType() != SessionType.REFUND) {
            super.addSession(session, orders);
        }
    }

    @Override
    public void aggregate() {
        Map<String,List<Order>> orderByDay = new HashMap<>();

        for(String day : DAYS) {
            orderByDay.put(day, new ArrayList<>());
            sessionsByDay.put(day, new ArrayList<>());
            dayCounts.put(day, 0);
        }

        LocalDateTime localDateTime1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(start), TimeUtil.UTC);
        LocalDateTime localDateTime2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(end), TimeUtil.UTC);
        LocalDate startDate = localDateTime1.toLocalDate();
        LocalDate endDate = localDateTime2.toLocalDate();

        for(long i = 0; i <= (endDate.getLong(ChronoField.EPOCH_DAY)-startDate.getLong(ChronoField.EPOCH_DAY)); i++) {
            String key = DAYS[startDate.plusDays(i).getDayOfWeek().getValue()-1];
            dayCounts.put(key, dayCounts.get(key)+1);
        }

        for(Session session : allSessions.values()) {
            ZonedDateTime sessionTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(session.getStartTime()), ZoneId.of("UTC"));
            String key = DAYS[sessionTime.getDayOfWeek().getValue()-1];
            sessionsByDay.get(key).add(session);
        }

        // allocate orders to days of week
        for(Order order : allOrdersList) {
            ZonedDateTime orderTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(order.getTime()), ZoneId.of("UTC"));
            String key = DAYS[orderTime.getDayOfWeek().getValue()-1];
            orderByDay.get(key).add(order);
        }

        //totals by day
        for(String key : orderByDay.keySet()) {
            orderSummaryByDay.put(key,SessionCalculationService.summarise(orderByDay.get(key)));
        }

        // whole period summary
        wholeSummary = SessionCalculationService.summarise(allOrdersList);
    }

    public Map<String, OrderSummary> getOrderSummaryByDay() {
        return orderSummaryByDay;
    }

    public OrderSummary getWholeSummary() {
        return wholeSummary;
    }

    public Map<String, List<Session>> getSessionsByDay() {
        return sessionsByDay;
    }

    public Map<String, Integer> getDayCounts() {
        return dayCounts;
    }
}
