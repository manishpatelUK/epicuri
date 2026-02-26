package uk.co.epicuri.serverapi.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.OrderRequest;
import uk.co.epicuri.serverapi.repository.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LiveDataService {

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DeviceDetailsRepository deviceDetailsRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private AsyncOrderHandlerService asyncOrderHandlerService;

    @Autowired
    private StockLevelRepository stockLevelRepository;

    private static Comparator<Notification> SORT_BY_TIME = Comparator.comparingLong(Notification::getTime);

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveDataService.class);

    public CheckIn getCheckInByCustomer(String customerId) {
        List<CheckIn> checkIns = getCheckInsForCustomer(customerId);
        if(checkIns.size() > 0) {
            return checkIns.get(checkIns.size()-1);
        }

        return null;
    }

    public List<CheckIn> getCheckInsForCustomer(String customerId) {
        List<CheckIn> checkins = checkInRepository.findByCustomerIdAndDeletedNull(customerId);
        checkins.sort(Comparator.comparingLong(CheckIn::getTime));
        return checkins;
    }

    public CheckIn upsert(CheckIn checkIn) {
        return checkInRepository.save(checkIn);
    }

    public CheckIn getCheckIn(String checkInId) {
        return checkInRepository.findOneNotDeleted(checkInId, CheckIn.class);
    }

    public List<CheckIn> getCheckIns(String restaurantId, long checkInExpiration) {
        return checkInRepository.findByRestaurantIdAndTimeGreaterThanEqual(restaurantId, System.currentTimeMillis()-checkInExpiration)
                .stream().filter(c -> c.getDeleted() == null).collect(Collectors.toList());
    }

    public List<CheckIn> getCheckInsByBookingIds(String restaurantId, List<String> bookingIds) {
        return checkInRepository.findByRestaurantIdAndBookingIdIn(restaurantId, bookingIds)
                .stream().filter(c -> c.getDeleted() == null).collect(Collectors.toList());
    }

    public List<CheckIn> getCheckInsByBookingId(String restaurantId, String bookingId) {
        List<String> bookingIds = new ArrayList<>();
        bookingIds.add(bookingId);
        return getCheckInsByBookingIds(restaurantId, bookingIds);
    }

    public void deleteCheckIn(String checkInId) {
        checkInRepository.delete(checkInId);
    }

    public void softDeleteCheckIn(String checkInId) {
        checkInRepository.markDeleted(checkInId, CheckIn.class);
    }

    public void cancelCheckInAndParty(CheckIn checkIn) {
        if(checkIn.getSessionId() == null) {
            if (checkIn.getPartyId() != null) {
                deleteParty(checkIn.getPartyId());
            }
        }
        softDeleteCheckIn(checkIn.getId());
    }

    public void softDeleteCheckIn(List<String> checkInIds) {
        checkInRepository.markDeleted(checkInIds, CheckIn.class);
    }

    public void tiePartyCheckIn(String customerId, Party party) {
        CheckIn checkIn = getCheckInByCustomer(customerId);
        if(checkIn != null && checkIn.getRestaurantId().equals(party.getRestaurantId())) {
            checkInRepository.updatePartyId(checkIn.getId(), party.getId());
            checkInRepository.updateCustomerId(checkIn.getId(), customerId);
            partyRepository.updateCustomerId(party.getId(), customerId);
        }
    }

    public CheckIn insert(CheckIn checkIn) {
        return checkInRepository.insert(checkIn);
    }

    public void clearCheckIn(String restaurantId, String customerId) {
        List<CheckIn> checkIns = getCheckInsForCustomer(customerId).stream().filter(c -> restaurantId.equals(c.getRestaurantId())).collect(Collectors.toList());
        for(CheckIn checkIn : checkIns) {
            checkInRepository.markDeleted(checkIn.getId(), CheckIn.class);
        }
    }

    public void setSessionDataOnCheckin(String restaurantId, String customerId, String sessionId, String partyId) {
        List<CheckIn> checkIns = getCheckInsForCustomer(customerId).stream().filter(c -> restaurantId.equals(c.getRestaurantId())).collect(Collectors.toList());
        if(checkIns.size() > 0) {
            setSessionDataOnCheckin(checkIns.get(checkIns.size()-1).getId(), sessionId, partyId);
        }
    }

    public void setSessionDataOnCheckin(String checkinId, String sessionId, String partyId) {
        checkInRepository.updateSessionIdAndPartyId(checkinId, sessionId, partyId);
    }

    public void setSessionIdOnCheckin(String restaurantId, String customerId, String sessionId) {
        List<CheckIn> checkIns = getCheckInsForCustomer(customerId).stream().filter(c -> restaurantId.equals(c.getRestaurantId())).collect(Collectors.toList());
        if(checkIns.size() > 0) {
            checkInRepository.updateSessionId(checkIns.get(checkIns.size()-1).getId(), sessionId);
        }
    }

    public void setPartyIdOnCheckin(String restaurantId, String customerId, String partyId) {
        List<CheckIn> checkIns = getCheckInsForCustomer(customerId).stream().filter(c -> restaurantId.equals(c.getRestaurantId())).collect(Collectors.toList());
        if(checkIns.size() > 0) {
            checkInRepository.updatePartyId(checkIns.get(checkIns.size()-1).getId(), partyId);
        }
    }

    public List<CheckIn> getCheckIns(String restaurantId) {
        int minutes = ((Number)masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.CHECKIN_EXPIRATION_TIME).getValue()).intValue();
        return checkInRepository.findByRestaurantIdAndTimeGreaterThanEqual(restaurantId, System.currentTimeMillis() - (minutes * 60 * 1000));
    }

    public void softDeleteCheckIns(List<String> checkInsDelete) {
        checkInRepository.markDeleted(checkInsDelete, CheckIn.class);
    }

    public void deleteCheckIns(List<CheckIn> checkIns) {
        checkInRepository.delete(checkIns);
    }

    public List<Party> getParties(String restaurantId) {
        return partyRepository.findByRestaurantId(restaurantId);
    }

    public Party getParty(String id) {
        return partyRepository.findOne(id);
    }

    public List<Party> getPartyByBookingId(String restaurantId, List<String> bookingIds) {
        return partyRepository.findByRestaurantIdAndBookingIdIn(restaurantId, bookingIds);
    }

    public Party getPartyByBookingId(String restaurantId, String bookingId) {
        return partyRepository.findByRestaurantIdAndBookingId(restaurantId, bookingId);
    }

    public Party upsert(Party party) {
        return partyRepository.save(party);
    }

    public Party insert(Party party) {
        return partyRepository.insert(party);
    }

    public List<Notification> getUnacknowledgedNotifications(String restaurantId, String sessionId) {
        List<Notification> notifications = getAllNotifications(restaurantId, sessionId);
        return notifications.stream().filter(n -> n.getAcknowledged() == null).sorted(SORT_BY_TIME).collect(Collectors.toList());
    }

    public Map<Session,List<Notification>> getAllUnacknowledgedNotificationsBySession(String restaurantId) {
        List<Notification> notifications = getAllNotifications(restaurantId).stream().filter(n -> n.getAcknowledged() == null).collect(Collectors.toList());
        Map<String,Session> sessions = sessionService
                .getSessions(notifications.stream().map(Notification::getSessionId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Session::getId, Function.identity()));

        Map<Session,List<Notification>> map = new HashMap<>();
        notifications.forEach(n -> {
            List<Notification> ns = map.getOrDefault(sessions.get(n.getSessionId()), new ArrayList<>());
            if(!ns.contains(n)) {
                ns.add(n);
            }
            map.put(sessions.get(n.getSessionId()), ns);
        });
        return map;
    }

    public List<Notification> getAllNotifications(String restaurantId, String sessionId) {
        List<Notification> notifications = notificationRepository.findByRestaurantIdAndSessionId(restaurantId, sessionId);
        return notifications.stream().sorted(SORT_BY_TIME).collect(Collectors.toList());
    }

    public List<Notification> getAllNotifications(String restaurantId) {
        List<Notification> notifications = notificationRepository.findByRestaurantId(restaurantId);
        return notifications.stream().sorted(SORT_BY_TIME).collect(Collectors.toList());
    }

    public Notification upsert(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> upsertNotifications(Collection<Notification> notifications) {
        return notificationRepository.save(notifications);
    }

    public Notification getNotification(String id) {
        return notificationRepository.findOne(id);
    }

    public List<Notification> deleteNotifications(String restaurantId, String sessionId) {
        List<Notification> allNotifications = getAllNotifications(restaurantId, sessionId);
        notificationRepository.delete(allNotifications);
        return allNotifications;
    }

    public void deleteNotification(String notificationId) {
        notificationRepository.delete(notificationId);
    }

    public Diner getDiner(String sessionId, String customerId) {
        Session session = sessionService.getSession(sessionId);
        if(session != null) {
            return session.getDiners().stream().filter(d -> d.getCustomerId() != null && d.getCustomerId().equals(customerId)).findFirst().orElse(null);
        } else {
            return null;
        }
    }

    public List<Diner> getDiners(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if(session != null) {
            return session.getDiners();
        } else {
            return new ArrayList<>();
        }
    }

    public Diner getDiner(String dinerId) {
        String sessionId = IDAble.extractParentId(dinerId);
        List<Diner> diners = getDiners(sessionId);
        return diners.stream().filter(d -> d.getId().equals(dinerId)).findFirst().orElse(null);
    }

    public void upsert(String sessionId, Diner diner) {
        sessionRepository.updateDiner(sessionId, diner);
    }

    public List<Order> insertOrders(Collection<Order> orders) {
        return orderRepository.insert(orders);
    }

    public List<Batch> upsert(Collection<Batch> batches) {
        return batchRepository.save(batches);
    }

    public void markBatchesAsPrinted(List<String> ids, long time) {
        batchRepository.setPrintedTime(ids, time);
    }

    public void cancelBatchByOrder(List<String> orderIds) {
        List<Batch> batches = getBatchesForOrders(orderIds);
        batches.forEach(b -> b.getOrderIds().removeAll(orderIds));
        batchRepository.save(batches.stream().filter(b -> b.getOrderIds().size()>0).collect(Collectors.toList()));
        batchRepository.delete(batches.stream().filter(b -> b.getOrderIds().size() == 0).collect(Collectors.toList()));
    }

    public List<Batch> getBatchesForOrders(List<String> orderIds) {
        Iterable<Order> orders = orderRepository.findAll(orderIds);
        return getBatchesForOrders(orders);
    }

    public List<Batch> getBatchesForOrders(Iterable<Order> orders) {
        Iterator<Order> it = orders.iterator();
        Set<String> sessionIds = new HashSet<>();
        Set<String> orderIds = new HashSet<>();
        while(it.hasNext()) {
            Order next = it.next();
            sessionIds.add(next.getSessionId());
            orderIds.add(next.getId());
        }
        List<Batch> batches = batchRepository.findBySessionIdIn(new ArrayList<>(sessionIds));
        batches.removeIf(b -> b.getOrderIds().stream().noneMatch(orderIds::contains));
        return batches;
    }

    public List<Order> getOrders(String sessionId) {
        List<Order> orders = getAllLiveOrders(sessionId);
        return orders.stream().filter(o -> !o.isRemoveFromReports() && !o.isVoided() && o.getDeleted() == null).collect(Collectors.toList());
    }

    public List<Order> getAllLiveOrders(String sessionId) {
        return orderRepository.findBySessionId(sessionId);
    }

    public List<Order> findOrders(Session session) {
        List<Order> orders = null;
        if(session.getCashUpId() == null) {
            orders = getOrders(session.getId());
        } else {
            SessionArchive archive = archiveDataService.getSessionArchive(session.getId());
            if(archive != null) {
                orders = archive.getOrders().stream().filter(o -> !o.isRemoveFromReports() && !o.isVoided() && o.getDeleted() == null).collect(Collectors.toList());
            }
        }

        return orders;
    }

    public List<Order> insertOrders(Session session, Collection<MenuItem> items, Collection<Order> orders, boolean createBatchesImmediately, boolean orderPrintsRequired) {
        List<Order> insertedOrders = insertOrders(orders);
        if(createBatchesImmediately) {
            createPrintBatches(session, items, insertedOrders, !orderPrintsRequired);
        }

        asyncOrderHandlerService.onOrders("", session.getRestaurantId(), session.getId(), new ArrayList<>(orders), getOrders(session.getId()));

        return insertedOrders;
    }

    public void createPrintBatches(Session session, Collection<MenuItem> items, Collection<Order> orders, boolean markAsPrinted) {
        if(session.getSessionType() == SessionType.REFUND) {
            return;
        }

        items = items.stream().filter(m -> m.getDefaultPrinter() != null).collect(Collectors.toList());
        Map<String,String> menuItemToPrintId = items.stream().collect(Collectors.toMap(MenuItem::getId, MenuItem::getDefaultPrinter));
        Map<String,List<Order>> printerIdToOrder = orders.stream().collect(Collectors.groupingBy(o -> menuItemToPrintId.get(o.getMenuItemId())));

        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
        Map<String,RestaurantDefault> defaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        long lockWindowMillis = 60 * 1000 * ((Number)defaultMap.getOrDefault(FixedDefaults.TAKEAWAY_LOCK_WINDOW, RestaurantDefault.newDefault(FixedDefaults.TAKEAWAY_MINIMUM_TIME, 60)).getValue()).intValue();
        long takeawayMinimumTimeMinutes = 60 * 1000 * ((Number)defaultMap.getOrDefault(FixedDefaults.TAKEAWAY_MINIMUM_TIME, RestaurantDefault.newDefault(FixedDefaults.TAKEAWAY_MINIMUM_TIME, 30)).getValue()).intValue();

        //if it is a takeaway, just create 1 batch. Otherwise batch for each printer
        List<Batch> batches = new ArrayList<>();
        if(session.getSessionType() == SessionType.TAKEAWAY) {
            Batch batch = createBatchForTakeaway(session, orders, takeawayMinimumTimeMinutes, lockWindowMillis, restaurant.getDefaultTakeawayPrinterId());
            batches.add(batch);
        } else {
            batches = printerIdToOrder.keySet().stream().map(key -> createBatchForPrinter(session, printerIdToOrder.get(key))).collect(Collectors.toList());
        }

        //if there are any printers that require duplication, duplicate the batch
        List<Printer> printers = masterDataService.getPrinters(restaurant.getId());
        Map<String,Printer> printerMap = printers.stream().collect(Collectors.toMap(Printer::getId, Function.identity()));

        List<Batch> duplicatedBatches = new ArrayList<>();
        for(Batch batch : batches) {
            Printer currentPrinter = printerMap.get(batch.getPrinterId());
            if(currentPrinter != null && StringUtils.isNotBlank(currentPrinter.getDuplicateTo())) {
                String[] ids = currentPrinter.getDuplicateTo().split(",");
                for(String id : ids) {
                    Printer printerToDuplicate = printerMap.getOrDefault(id, null);
                    if(printerToDuplicate != null) {
                        duplicatedBatches.add(Batch.duplicateForPrinter(batch, printerToDuplicate.getId()));
                    }
                }
            }
        }
        batches.addAll(duplicatedBatches);

        if(markAsPrinted) {
            long printedTime = System.currentTimeMillis();
            for(Batch batch : batches) {
                batch.getSpoolTime().add(printedTime);
                batch.setPrintedTime(printedTime);
            }
        }

        batchRepository.insert(batches);
    }

    private Batch createBatchForPrinter(Session session, List<Order> orders) {
        Batch batch = new Batch(session, orders);
        //set for immediate printing, unless it's a self-service
        if(isSelfService(orders)) {
            batch.setAwaitingImmediatePrint(false);
        } else {
            batch.setAwaitingImmediatePrint(true);
        }
        return batch;
    }

    private Batch createBatchForTakeaway(Session session, Collection<Order> orders, long takeawayMinimumTimeMinutes, long lockWindowMillis, String takeawayPrinterId) {
        Batch batch = new Batch(session, orders);
        //set for immediate printing, unless it's a takeaway in the future
        if(isTakeawayInFuture(session, takeawayMinimumTimeMinutes)) {
            batch.setAwaitingImmediatePrint(false);
        } else {
            batch.setAwaitingImmediatePrint(true);
        }
        batch.setIntendedPrintTime(session.getStartTime() - lockWindowMillis);
        batch.setPrinterId(takeawayPrinterId);
        return batch;
    }

    public void setBatchPrinter(List<String> batchIds, String printerId) {
        batchRepository.setPrinterId(batchIds, printerId);
    }

    private boolean isTakeawayInFuture(Session session, long takeawayMinimumTimeMinutes) {
        if(session.getSessionType() != SessionType.TAKEAWAY) {
            return false;
        }

        //is time before the due time for printing?
        return System.currentTimeMillis() < session.getStartTime()-takeawayMinimumTimeMinutes;
    }

    private boolean isSelfService(Collection<Order> orders) {
        return orders.stream().anyMatch(o ->
                o.getInstantiatedFrom() == ActivityInstantiationConstant.ANDROID
                || o.getInstantiatedFrom() == ActivityInstantiationConstant.CUSTOMER
                || o.getInstantiatedFrom() == ActivityInstantiationConstant.IOS);
    }

    public List<Order> insertOrders(String restaurantId, List<OrderRequest> requests, String staffId, boolean orderPrintsRequired) {
        requests = requests.stream().filter(r -> r.getQuantity() > 0).collect(Collectors.toList());

        List<Order> orders = new ArrayList<>();

        //cache modifiers
        List<String> modifierIds = requests.stream().flatMap(i -> i.getModifiers().stream()).distinct().collect(Collectors.toList());
        Map<String,Modifier> modifierMap = masterDataService.getModifiers(modifierIds).stream().collect(Collectors.toMap(Modifier::getId, Function.identity()));
        Map<String,MenuItem> itemsMap = masterDataService.getAllMenuItems(restaurantId).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        Map<String,List<OrderRequest>> groupBySessionId = requests.stream().collect(Collectors.groupingBy(r -> {
            if(StringUtils.isNotBlank(r.getDinerId())) {
                return IDAble.extractParentId(r.getDinerId());
            }
            if(StringUtils.isNotBlank(r.getSessionId())) {
                return r.getSessionId();
            }
            return "";
        }));

        LOGGER.trace("Insert orders for {} sessions", groupBySessionId.size());

        for(Map.Entry<String,List<OrderRequest>> entry : groupBySessionId.entrySet()) {
            LOGGER.trace("Insert {} orders for session {}", entry.getValue().size(), entry.getKey());
            orders.addAll(insertOrdersBySessionId(entry.getKey(), entry.getValue(), itemsMap, modifierMap, taxRateMap, staffId, orderPrintsRequired));
        }

        return orders;
    }

    private List<Order> insertOrdersBySessionId(String sessionId, List<OrderRequest> requests, Map<String,MenuItem> itemsMap, Map<String,Modifier> modifierMap, Map<String,TaxRate> taxRateMap, String staffId, boolean orderPrintsRequired) {
        List<Order> orders = requests.stream().map(orderRequest -> {
            MenuItem item = itemsMap.get(orderRequest.getMenuItemId());
            List<Modifier> modifiers = new ArrayList<>();
            orderRequest.getModifiers().forEach(m -> {
                Modifier modifier = modifierMap.get(m);
                modifier.setTaxRate(taxRateMap.get(modifier.getTaxTypeId()));
                modifiers.add(modifier);
            });
            return new Order(sessionId, orderRequest, modifiers, item, taxRateMap.get(item.getTaxTypeId()), staffId);
        }).collect(Collectors.toList());
        Session session = sessionService.getSession(sessionId);

        return insertOrders(session, itemsMap.values(), orders, true, orderPrintsRequired);
    }

    public Order upsertOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> upsertOrders(List<Order> orders) {
        return orderRepository.save(orders);
    }

    public Order getOrder(String id) {
        return orderRepository.findOne(id);
    }

    public List<Order> getOrdersBySessionId(String id) {
        return orderRepository.findBySessionId(id);
    }

    public Map<String, List<Order>> getOrdersBySessionIds(List<String> sessionIds) {
        List<Order> orders = orderRepository.findBySessionIdIn(sessionIds);
        return orders.stream().collect(Collectors.groupingBy(Order::getSessionId));
    }

    public void voidOrders(List<String> orderIds) {
        orderRepository.updateVoid(orderIds);
    }

    public void upsert(DeviceDetail deviceDetail) {
        deviceDetailsRepository.insert(deviceDetail);
    }

    public List<Batch> getBatches(List<String> ids) {
        return Lists.newArrayList(batchRepository.findAll(ids));
    }

    public List<Batch> getBatchesBySessionId(String id) {
        return batchRepository.findBySessionId(id);
    }

    public Map<String,List<Batch>> getBatchesBySessionId(List<String> ids) {
        return batchRepository.findBySessionIdIn(ids).stream().collect(Collectors.groupingBy(Batch::getSessionId));
    }

    public void updateImmediatePrint(List<String> batchIds, boolean flag) {
        batchRepository.setImmediatePrintFlag(batchIds, flag);
    }

    public void updateBatchTime(String sessionId, long time) {
        List<String> batches = getBatchesBySessionId(sessionId).stream().map(Batch::getId).collect(Collectors.toList());
        batchRepository.pushTimeToPrint(batches, time);
    }

    public boolean tablesInUse(String restaurantId, List<String> tableIds){
        List<Session> sessions = sessionRepository.findCurrentSeatedSessions(restaurantId);
        List<String> currentLiveTables = sessions.stream().flatMap(s -> s.getTables().stream()).distinct().collect(Collectors.toList());
        for(String table : currentLiveTables) {
            if(tableIds.contains(table)) {
                return true;
            }
        }

        return false;
    }

    public boolean tablesInUse(String restaurantId, List<String> tableIds, List<String> except){
        List<Session> sessions = sessionRepository.findCurrentSeatedSessions(restaurantId);
        List<String> currentLiveTables = sessions.stream().flatMap(s -> s.getTables().stream()).distinct().collect(Collectors.toList());
        currentLiveTables.removeAll(except);

        for(String table : currentLiveTables) {
            if(tableIds.contains(table)) {
                return true;
            }
        }

        return false;
    }

    public List<String> tablesInUse(String restaurantId) {
        return sessionService.getLiveSessions(restaurantId).stream().flatMap(s -> s.getTables().stream()).collect(Collectors.toList());
    }

    public void deleteOrders(List<Order> orders) {
        orderRepository.delete(orders);
    }

    public void deleteBatches(List<Batch> batches) {
        batchRepository.delete(batches);
    }

    public void markBatchesDeleted(List<String> ids) {
        batchRepository.markDeleted(ids, Batch.class);
    }

    public void deleteParty(String id) {
        partyRepository.delete(id);
    }

    public void addSpooledTime(List<String> batchIds, long time) {
        batchRepository.pushSpoolTime(batchIds, time);
    }

    public void flagForBatchPrinting(List<String> batchIds, long time) {
        batchRepository.spoolForBatchPrinting(batchIds, time);
    }

    public Map<Booking,Session> getAssociatedSessionsByBookingRecent(String customerId, BookingType bookingType) {
        long from = System.currentTimeMillis() - (1000 * 60 * 120);
        long to = Long.MAX_VALUE;
        Map<String, Booking> bookings = getBookingsBetween(customerId, bookingType, from, to);
        return getAssociatedSessionsByBooking(bookings, true);
    }

    public Map<Booking,Session> getAssociatedSessionsByBookingAll(String customerId, BookingType bookingType) {
        long from = 0;
        long to = Long.MAX_VALUE;
        Map<String, Booking> bookings = getBookingsBetween(customerId, bookingType, from, to);
        return getAssociatedSessionsByBooking(bookings, false);
    }

    public Map<Booking, Session> getAssociatedSessionsByBooking(Map<String, Booking> bookings, boolean filterSessionTimeInFuture) {
        return sessionService.getSessionsByBookingIds(new ArrayList<>(bookings.keySet()))
                                    .stream().filter(s -> {
                                                    if(filterSessionTimeInFuture) {
                                                        return s.getStartTime() > System.currentTimeMillis() - (1000 * 60 * 120)
                                                                && s.getOriginalBookingId() != null
                                                                && bookings.containsKey(s.getOriginalBookingId());
                                                    } else {
                                                        return s.getOriginalBookingId() != null
                                                                && bookings.containsKey(s.getOriginalBookingId());
                                                    }})
                                    .collect(Collectors.toMap(s -> bookings.get(s.getOriginalBookingId()), Function.identity()));
    }

    public Map<String, Booking> getBookingsBetween(String customerId, BookingType bookingType, long from, long to) {
        return bookingService.getBookingsByCustomerId(customerId, from, to, bookingType)
                                            .stream().collect(Collectors.toMap(Booking::getId, Function.identity()));
    }

    public CheckIn createCheckInAndParty(String restaurantId, String reservationId, Customer customer, boolean createParty) {
        return createCheckInAndParty(restaurantId, reservationId, customer, 1, createParty);
    }

    public CheckIn createCheckInAndParty(String restaurantId, String reservationId, Customer customer, int numberOfPeople, boolean createParty) {
        CheckIn newCheckin = new CheckIn();
        newCheckin.setRestaurantId(restaurantId);
        newCheckin.setCustomerId(customer.getId());
        newCheckin.setTime(System.currentTimeMillis());
        newCheckin.setNumberOfPeople(numberOfPeople);
        Booking booking = null;
        if (reservationId != null) {
            booking = bookingService.getBooking(reservationId);
            if(booking != null) {
                newCheckin.setBookingId(reservationId);
            }
        }
        if(createParty) {
            Party party = insert(createParty(booking, restaurantId, customer, numberOfPeople < 1 ? 1 : numberOfPeople));
            newCheckin.setPartyId(party.getId());
        }

        return insert(newCheckin);
    }

    public void cancelOrders(String sessionId) {
        List<Order> orders = getOrders(sessionId);
        cancelOrders(sessionId, orders);
    }

    public void cancelOrders(String sessionId, List<Order> orders) {
        deleteOrders(orders);
        deleteBatches(getBatchesBySessionId(sessionId));
    }

    public String pushSelfServiceParameters(List<Order> orders, String location) {
        String id = RandomStringUtils.randomAlphabetic(3).toUpperCase() + RandomStringUtils.randomNumeric(2);
        orderRepository.pushSelfServiceParameters(orders.stream().map(Order::getId).collect(Collectors.toList()), id, location);
        return id;
    }

    public void updateStockControl(Restaurant restaurant, List<Order> orders, boolean incrementOrDecrement, boolean autoUnavailable) {
        Map<String,Integer> increments = new HashMap<>();
        orders.stream().filter(o -> o.getMenuItem() != null).forEach(o -> {
            MenuItem item = o.getMenuItem();
            if(StringUtils.isNotBlank(item.getPlu())) {
                increments.put(item.getPlu(), o.getQuantity() + increments.getOrDefault(item.getPlu(), 0));
            }
            if(o.getModifiers() != null) {
                for(Modifier modifier : o.getModifiers()) {
                    if(StringUtils.isNotBlank(modifier.getPlu())) {
                        increments.put(modifier.getPlu(), o.getQuantity() + increments.getOrDefault(modifier.getPlu(), 0));
                    }
                }
            }
        });

        List<StockLevel> allStock = stockLevelRepository.findByRestaurantIdAndPluIn(restaurant.getId(), new ArrayList<>(increments.keySet()));
        allStock.forEach(s -> {
            if(!s.isTrackable()) {
                increments.remove(s.getPlu());
            }
        });

        if(increments.size() > 0) {
            for (Map.Entry<String, Integer> entry : increments.entrySet()) {
                stockLevelRepository.increment(restaurant.getId(), entry.getKey(), entry.getValue() * (incrementOrDecrement ? 1 : -1));
            }

            if (autoUnavailable) {
                List<String> pluList = new ArrayList<>(increments.keySet());
                updateUnavailabilityOnStockControl(restaurant.getId(), pluList);
            }
        }
    }

    public void updateUnavailabilityOnStockControl(String restaurantId, List<String> pluList) {
        Map<String, StockLevel> stockLevels = stockLevelRepository.findByRestaurantIdAndPluIn(restaurantId, pluList).stream().filter(StockLevel::isTrackable).collect(Collectors.toMap(StockLevel::getPlu, Function.identity()));
        if(stockLevels.size() > 0) {
            List<MenuItem> menuItemsByPlu = masterDataService.getMenuItemsByPlu(restaurantId, new ArrayList<>(stockLevels.keySet()));
            menuItemsByPlu.forEach(m -> m.setUnavailable(stockLevels.get(m.getPlu()).getLevel()<=0));
            masterDataService.upsertMenuItems(menuItemsByPlu);
        }
    }

    private Party createParty(Booking booking, String restaurantId, Customer customer) {
        return createParty(booking, restaurantId, customer, 1);
    }

    private Party createParty(Booking booking, String restaurantId, Customer customer, int numberOfPeople) {
        if(booking != null) {
            return new Party(booking);
        } else {
            Party party = new Party();
            party.setRestaurantId(restaurantId);
            party.setCustomerId(customer.getId());
            party.setNumberOfPeople(numberOfPeople);
            party.setPartyType(PartyType.WALK_IN);
            String fullName = Customer.determineName(customer);
            if(StringUtils.isBlank(fullName)) {
                fullName = "Anonymous Guest";
            }
            party.setName(fullName);
            return party;
        }
    }
}
