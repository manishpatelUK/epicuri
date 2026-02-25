package uk.co.epicuri.serverapi.host.endpoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.BasicBusinessIntelligenceReport;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HistoricalDataWrapper;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayType;
import uk.co.epicuri.serverapi.engines.BasicBIAggregator;
import uk.co.epicuri.serverapi.engines.DateTimeConstants;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.SessionCalculationService;
import uk.co.epicuri.serverapi.service.SessionService;
import uk.co.epicuri.serverapi.service.util.OrderSummary;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/BusinessIntelligence")
public class BusinessIntelligenceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessIntelligenceController.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Value("${epicuri.portal}")
    private String portalURL;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public RedirectView getIndex(RedirectAttributes attributes,
                                      @RequestParam("Auth") String token,
                                      @RequestParam(value = "Period", defaultValue = "-1") int period,
                                      @RequestParam(value = "webView", defaultValue = "false") boolean webView) {
        String url = portalURL + "?Auth=" + token + "&Period=" + period + "&webView=" + webView;
        LOGGER.trace("Forward to URL: {}", url);

        attributes.addAttribute("Auth", token);
        attributes.addAttribute("Period", period);
        attributes.addAttribute("webView", webView);
        return new RedirectView(portalURL);
    }

    @HostAuthRequired
    @RequestMapping(value = "/Basic", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getBasicMetrics(@RequestHeader(Params.AUTHORIZATION) String token,
                                             @RequestParam("start") String startDate,
                                             @RequestParam("end") String endDate) {

        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);


        ZoneId zoneId = ZoneId.of(restaurant.getIANATimezone());
        long start = DateTimeConstants.convertToLong(zoneId, startDate, LocalTime.MIN);
        long end = DateTimeConstants.convertToLong(zoneId, endDate, LocalTime.MAX);
        if(start < 0 || end < 0) {
            return ResponseEntity.badRequest().body("times not specified");
        }

        Map<String,MenuItem> allItems = masterDataService.getAllMenuItems(restaurantId).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        BasicBIAggregator biAggregator = new BasicBIAggregator(sessionCalculationService, start, end);

        HistoricalDataWrapper historicalDataWrapper = sessionService.getAllSessionsAndOrdersByOpenTime(restaurantId, start, end);
        List<Session> liveSessions = historicalDataWrapper.getLiveData();
        Map<String,List<Order>> liveOrders = historicalDataWrapper.getLiveOrders();
        List<Session> oldSessions = historicalDataWrapper.getOldData();
        Map<String,List<Order>> oldOrders = historicalDataWrapper.getOldOrders();

        liveSessions.forEach(s -> biAggregator.addSession(s, liveOrders.get(s.getId())));
        oldSessions.forEach(s -> biAggregator.addSession(s, oldOrders.get(s.getId())));

        biAggregator.aggregate();

        return ResponseEntity.ok(buildReport(biAggregator, allItems));
    }

    private BasicBusinessIntelligenceReport buildReport(BasicBIAggregator aggregator, Map<String,MenuItem> menuItemMap) {
        BasicBusinessIntelligenceReport report = new BasicBusinessIntelligenceReport();
        Map<String,Integer> popularItems = aggregator.getWholeSummary().getMenuItemCount();

        if(popularItems.size() > 0) {
            // sort values ascending order
            getPopularItems(menuItemMap, report, popularItems);
        }

        Map<String, OrderSummary> orderSummaryByDay = aggregator.getOrderSummaryByDay();

        if(orderSummaryByDay.size() > 0) {
            applyDailySessionsReport(report, aggregator);
            applyDailyItemsReport(report, aggregator);
        }

        return report;
    }

    private void getPopularItems(Map<String, MenuItem> menuItemMap, BasicBusinessIntelligenceReport report, Map<String, Integer> popularItems) {
        popularItems = popularItems.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        for(ItemType type : ItemType.values()) {
            List<BasicBusinessIntelligenceReport.PopularItem> list =
                    getPopularItems(menuItemMap,
                            popularItems.entrySet().stream().filter(e -> menuItemMap.get(e.getKey()) != null && menuItemMap.get(e.getKey()).getType() == type)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));
            report.getPopularItemsReport().put(type.getName(), list);
        }
    }

    private List<BasicBusinessIntelligenceReport.PopularItem> getPopularItems(Map<String, MenuItem> menuItemMap, Map<String, Integer> popularItems) {
        List<BasicBusinessIntelligenceReport.PopularItem> popularItemList = new ArrayList<>();
        Iterator<String> iterator = popularItems.keySet().iterator();
        for (int i = 0; iterator.hasNext() && i <= 5; i++) {
            String key = iterator.next();
            BasicBusinessIntelligenceReport.PopularItem popularItem = new BasicBusinessIntelligenceReport.PopularItem();
            popularItem.setName(menuItemMap.get(key) != null ? menuItemMap.get(key).getName() : "Unknown");
            popularItem.setNumber(popularItems.get(key));
            popularItemList.add(popularItem);
            i++;
        }
        return popularItemList;
    }

    private void applyDailySessionsReport(BasicBusinessIntelligenceReport report, BasicBIAggregator aggregator) {
        Map<String, List<Session>> sessionsByDay = aggregator.getSessionsByDay();
        Map<String,Map<SessionType,List<Session>>> sessionsByDayAndType = new HashMap<>();
        sessionsByDay.forEach((key, value) -> value.forEach(s -> {
            sessionsByDayAndType.computeIfAbsent(key, k -> new HashMap<>());
            sessionsByDayAndType.get(key).computeIfAbsent(s.getSessionType(), k -> new ArrayList<>());

            sessionsByDayAndType.get(key).get(s.getSessionType()).add(s);
        }));

        Map<String,Integer> dayCounts = aggregator.getDayCounts();

        for(String key : sessionsByDay.keySet()) {
            Map<String,Double> averageSessions = new HashMap<>();
            Map<SessionType, List<Session>> map = sessionsByDayAndType.computeIfAbsent(key, k -> new HashMap<>());
            List<Session> seatedSessions = map.computeIfAbsent(SessionType.SEATED, k->new ArrayList<>());
            List<Session> adhocSessions = map.computeIfAbsent(SessionType.ADHOC, k->new ArrayList<>());
            List<Session> tabSessions = map.computeIfAbsent(SessionType.TAB, k->new ArrayList<>());
            List<Session> takeaway = map.computeIfAbsent(SessionType.TAKEAWAY, k->new ArrayList<>());

            List<Session> inDining = new ArrayList<>();
            inDining.addAll(seatedSessions);
            inDining.addAll(adhocSessions);
            inDining.addAll(tabSessions);
            averageSessions.put(BasicBIAggregator.SEATED, dayCounts.get(key) == 0 ? 0D : inDining.size()/(double)dayCounts.get(key));
            averageSessions.put(BasicBIAggregator.TAKEAWAY_COLLECTION, dayCounts.get(key) == 0 ? 0D : takeaway.stream().filter(s -> s.getTakeawayType() == TakeawayType.COLLECTION).collect(Collectors.toList()).size()/(double)dayCounts.get(key));
            averageSessions.put(BasicBIAggregator.TAKEAWAY_DELIVERY, dayCounts.get(key) == 0 ? 0D : takeaway.stream().filter(s -> s.getTakeawayType() == TakeawayType.DELIVERY).collect(Collectors.toList()).size()/(double)dayCounts.get(key));

            report.getAverageSessionsReport().put(key, averageSessions);
        }
    }

    private void applyDailyItemsReport(BasicBusinessIntelligenceReport report, BasicBIAggregator aggregator) {
        Map<String,OrderSummary> orderSummaryByDay = aggregator.getOrderSummaryByDay();

        Map<String,Integer> dayCounts = aggregator.getDayCounts();

        for(String key : orderSummaryByDay.keySet()) {
            Map<String,Double> averageCounts = new HashMap<>();
            for(ItemType itemType : ItemType.values()) {
                Integer count = orderSummaryByDay.get(key).getItemTypeCount().get(itemType);
                averageCounts.put(itemType.getName(), dayCounts.get(key) == null || dayCounts.get(key) == 0 ? 0D : count.doubleValue()/dayCounts.get(key));
            }

            report.getAverageItemsReport().put(key, averageCounts);
        }
    }
}
