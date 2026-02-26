package uk.co.epicuri.serverapi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BatchRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BatchService {
    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SessionService sessionService;

    @Value("${epicuri.waiter.print.window}")
    private long printSpoolWindow;


    public List<Batch> getBatches(List<String> batchIds) {
        return liveDataService.getBatches(batchIds);
    }

    public List<Batch> getBatchesBySessionIds(List<String> sessionIds) {
        return batchRepository.findBySessionIdIn(sessionIds);
    }

    public List<HostBatchView> getHostBatchViews(String restaurantId) {
        List<Session> liveSessionsIncludeClosed = sessionService.getLiveSessionsIncludeClosedWithinLockWindow(restaurantId);
        Tuple<List<Session>, Map<String,Booking>> tuple = filterValidSessions(liveSessionsIncludeClosed, restaurantId);
        List<Session> allSessions = tuple.getA();
        // bookings by id
        Map<String, Booking> bookingMap = tuple.getB();
        Map<String,List<Order>> orders = liveDataService.getOrdersBySessionIds(allSessions.stream().map(Session::getId).collect(Collectors.toList()));

        return getHostBatchViews(restaurantId, allSessions, bookingMap, orders, false);
    }

    public List<HostBatchView> getHostBatchViews(String restaurantId, List<Session> allSessions, Map<String, Booking> bookingMap, Map<String, List<Order>> orders, boolean includeAwaitingImmediatePrint) {
        // all orders
        final List<Order> allOrders = orders.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList());
        // all order ids
        Set<String> allOrderIds = allOrders.stream().map(Order::getId).collect(Collectors.toSet());
        List<Batch> batches = getBatchesToPrintBySessionId(allSessions.stream().map(Session::getId).distinct().collect(Collectors.toList()))
                                .stream().filter(b -> allOrderIds.containsAll(b.getOrderIds())).collect(Collectors.toList());

        List<HostBatchView> list = new ArrayList<>();
        long now = System.currentTimeMillis();

        // Only return those which haven't recently been spooled and not printed at all
        batches = filterBatches(batches, now, printSpoolWindow);
        batches = getBatchesWherePrintingRequired(batches, includeAwaitingImmediatePrint);

        // if all printers are logical, batches will be size 0 & return an empty list
        if (batches.size() == 0) {
            return list;
        }

        // orders by session id
        Map<String,List<Order>> sessionIdToOrders = new HashMap<>();

        // filter out orders that pertain to said batches
        List<String> orderIds = batches.stream().flatMap(b -> b.getOrderIds().stream()).distinct().collect(Collectors.toList());
        List<Order> temp = allOrders.stream().filter(o -> orderIds.contains(o.getId())).collect(Collectors.toList());
        allOrders.clear();
        allOrders.addAll(temp);

        // populate sessionIdToOrders
        allSessions.forEach(s -> sessionIdToOrders.put(s.getId(), allOrders.stream().filter(o -> o.getSessionId().equals(s.getId())).collect(Collectors.toList())));

        // sessions by id
        Map<String,Session> sessionMap = allSessions.stream().collect(Collectors.toMap(Session::getId, Function.identity()));

        // all courses
        Map<String,Course> courseMap = getCourses(restaurantId);

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        Map<String,Staff> staffMap = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));
        batches.forEach(b -> addBatch(list, sessionIdToOrders, sessionMap, courseMap, bookingMap, b, restaurant, staffMap));

        return list;
    }

    public void markAsPrinted(List<String> batchIds) {
        List<Batch> batches = getBatches(batchIds);
        List<String> ids = batches.stream()
                .filter(b -> b.getPrintedTime() == null)
                .map(Batch::getId).collect(Collectors.toList());

        if(ids.size() > 0) {
            liveDataService.markBatchesAsPrinted(ids, System.currentTimeMillis());
        }
    }

    public List<Batch> getBatchesToPrintBySessionId(List<String> ids) {
        return batchRepository.findBySessionIdInAndIntendedPrintTimeLessThanEqual(ids, System.currentTimeMillis()+1);
    }

    public Tuple<List<Session>,Map<String, Booking>> filterValidSessions(List<Session> allSessions, String restaurantId) {
        // filter all sessions for takeaways, seated & not closed, adhoc & paid
        allSessions = allSessions.stream()
                .filter(s ->
                        (s.getSessionType() == SessionType.TAKEAWAY && isNotClosedOrClosedWithinBound(s))
                        || (s.getSessionType() == SessionType.SEATED && isNotClosedOrClosedWithinBound(s))
                        || (s.getSessionType() == SessionType.ADHOC)
                        || (s.getSessionType() == SessionType.TAB && isNotClosedOrClosedWithinBound(s))).collect(Collectors.toList());

        RestaurantDefault lockWindow = masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.TAKEAWAY_LOCK_WINDOW);
        long now = System.currentTimeMillis();
        long msCutOff = (((Number)lockWindow.getValue()).intValue() * 60 * 1000);
        Map<String, Booking> bookingMap = getBookingsById(allSessions);
        allSessions.removeIf(s -> s.getSessionType() == SessionType.TAKEAWAY && s.getOriginalBookingId() != null && bookingMap.containsKey(s.getOriginalBookingId())
                && now < (bookingMap.get(s.getOriginalBookingId()).getTargetTime()-msCutOff));

        return new Tuple<>(allSessions, bookingMap);
    }

    private boolean isNotClosedOrClosedWithinBound(Session session) {
        return session.getClosedTime() == null; //may in the future need to include closed time within a bound... e.g. session closes quickly, but still needs printing
    }

    public Map<String, Booking> getBookingsById(List<Session> allSessions) {
        List<Booking> bookings = getBookings(allSessions);
        return bookings.stream().collect(Collectors.toMap(Booking::getId, Function.identity()));
    }

    public List<Booking> getBookings(List<Session> allSessions) {
        return bookingService.getBookings(allSessions.stream()
                .filter(s -> StringUtils.isNotBlank(s.getOriginalBookingId()))
                .map(Session::getOriginalBookingId).collect(Collectors.toList()));
    }

    public List<Batch> filterBatches(List<Batch> batches, long now, long printSpoolWindow) {
        return batches.stream().filter(b ->
                b.getDeleted() == null
                        && b.getPrintedTime() == null
                        && (b.getSpoolTime().size() == 0
                        || b.getSpoolTime().get(b.getSpoolTime().size()-1) < now-printSpoolWindow)).collect(Collectors.toList());
    }

    private boolean addBatch(List<HostBatchView> list, Map<String, List<Order>> orderMap, Map<String, Session> sessionMap, Map<String, Course> courseMap, Map<String, Booking> bookingMap, Batch b, Restaurant restaurant, Map<String, Staff> staffMap) {
        List<Order> sessionOrders = orderMap.getOrDefault(b.getSessionId(), new ArrayList<>());
        sessionOrders = sessionOrders.stream().filter(o -> b.getOrderIds().contains(o.getId())).collect(Collectors.toList());

        List<String> staffIds = new ArrayList<>();
        for(Order order : sessionOrders) {
            if(order.getStaffId() != null && !staffIds.contains(order.getStaffId())) {
                staffIds.add(order.getStaffId());
            }
        }

        String staff = getStaffUserNames(staffMap, staffIds, sessionOrders);
        Session session = sessionMap.get(b.getSessionId());

        return list.add(
                new HostBatchView(b,
                        session,
                        sessionOrders,
                        bookingMap.getOrDefault(session.getOriginalBookingId(), null),
                        courseMap,
                        restaurant,
                        staff));
    }

    private String getStaffUserNames(Map<String, Staff> staffMap, List<String> staffIds, List<Order> orders) {
        String staff = RestaurantConstants.STAFF_PRINT_LABEL;
        if(orders != null) {
            if(orders.stream().allMatch(o -> o.getInstantiatedFrom() != ActivityInstantiationConstant.WAITER)) {
                staff = RestaurantConstants.CUSTOMER_PRINT_LABEL;
            }
        }
        if(staffIds.size() >= 1) {
            for(String staffId : staffIds) {
                if(staffMap.containsKey(staffId)) {
                    staff = staffMap.get(staffId).getUserName();
                    if(staffIds.size() > 1) {
                        staff += " (+" + (staffIds.size()-1) + " more)";
                        return staff;
                    }
                }
            }
        }
        return staff;
    }

    private Map<String, Course> getCourses(String restaurantId) {
        return masterDataService.getCoursesByRestaurantId(restaurantId).stream().collect(Collectors.toMap(Course::getId, Function.identity()));
    }

    public List<Batch> getBatchesWherePrintingRequired(List<Batch> batches, boolean includeAwaitingImmediatePrint) {
        List<Printer> printers = masterDataService.getPrinters(batches.stream().map(Batch::getPrinterId).collect(Collectors.toList()));
        Map<String,Printer> printerMap = printers.stream().collect(Collectors.toMap(Printer::getId, Function.identity()));
        return batches.stream()
                .filter(batch -> printerMap.containsKey(batch.getPrinterId())
                        && StringUtils.isNotBlank(printerMap.get(batch.getPrinterId()).getIp()))
                .filter(batch -> includeAwaitingImmediatePrint || !batch.isAwaitingImmediatePrint()).collect(Collectors.toList());
    }
}
