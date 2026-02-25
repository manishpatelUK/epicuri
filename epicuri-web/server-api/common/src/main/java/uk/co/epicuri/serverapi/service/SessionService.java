package uk.co.epicuri.serverapi.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.BadStateException;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerInteractionDeferredSession;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerViewBasic;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HistoricalDataWrapper;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderItemView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.session.ChairData;
import uk.co.epicuri.serverapi.common.pojo.model.session.HostSessionView;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionPayload;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;
import uk.co.epicuri.serverapi.repository.CustomerInteractionRepository;
import uk.co.epicuri.serverapi.repository.SessionNumberRepository;
import uk.co.epicuri.serverapi.repository.SessionRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by manish
 */
@org.springframework.stereotype.Service
public class SessionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionTimingService sessionTimingService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionCalculationService calculationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private SessionNumberRepository sessionNumberRepository;

    @Autowired
    private CustomerInteractionRepository customerInteractionRepository;

    private static Comparator<HostSessionView> HOST_SESSION_VIEW_COMPARATOR_DESCENDING = (s1, s2) -> Long.compare(s2.getClosedTime(), s1.getClosedTime());

    public Session getSession(String id) {
        return sessionRepository.findOne(id);
    }

    public Session findSession(String id) {
        Session session = getSession(id);
        if(session == null) {
            return findSessionInArchive(id);
        }

        return session;
    }

    private Session findSessionInArchive(String id) {
        SessionArchive archive = archiveDataService.getSessionArchive(id);
        if(archive != null) {
            return archive.getSession();
        }

        return null;
    }

    public List<Session> getSessions(Iterable<String> ids) {
        return Lists.newArrayList(sessionRepository.findAll(ids));
    }

    public Session addAdjustments(Session session, String staffId, List<AdjustmentRequest> requests) {
        List<AdjustmentType> adjustmentTypes = masterDataService.getAdjustmentTypes(requests.stream().map(AdjustmentRequest::getAdjustmentTypeId).distinct().collect(Collectors.toList()));
        Map<String,AdjustmentType> adjustmentTypesMap = adjustmentTypes.stream().collect(Collectors.toMap(AdjustmentType::getId, Function.identity()));
        List<Order> orders = liveDataService.getOrdersBySessionId(session.getId());
        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session,orders);
        for(AdjustmentRequest request : requests) {
            Adjustment adjustment = createAdjustment(session.getId(), adjustmentTypesMap.get(request.getAdjustmentTypeId()), staffId, request, values);
            session.getAdjustments().add(adjustment);
        }
        return sessionRepository.save(session);
    }

    public void addAdjustments(String sessionId, List<Adjustment> adjustments) {
        Session session = getSession(sessionId);
        if(session != null) {
            session.getAdjustments().addAll(adjustments);
            sessionRepository.save(session);
        }
    }

    public Adjustment addAdjustment(Session session, AdjustmentType adjustmentType, String staffId, AdjustmentRequest request) {
        List<Order> orders = liveDataService.getOrdersBySessionId(session.getId());
        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session,orders);
        Adjustment adjustment = createAdjustment(session.getId(), adjustmentType, staffId, request, values);
        addAdjustment(session.getId(), adjustment);
        return adjustment;
    }

    public Session voidAllPayments(Session session, String staffId) {
        boolean changed = false;
        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.getAdjustmentType().getType() == AdjustmentTypeType.PAYMENT) {
                adjustment.setVoided(true);
                adjustment.setVoidedByStaffId(staffId);
                changed = true;
            }
        }

        return changed ? upsert(session) : session;
    }

    private Adjustment createAdjustment(String sessionId, AdjustmentType adjustmentType, String staffId, AdjustmentRequest request, Map<CalculationKey, Number> values) {
        Adjustment adjustment = createAdjustment(sessionId, adjustmentType, staffId, request);
        if(StringUtils.isNotBlank(request.getLinkedTo())) {
            adjustment.setLinkedTo(request.getLinkedTo());
        }
        if(StringUtils.isNotBlank(request.getReference())) {
            adjustment.getSpecialAdjustmentData().put(Adjustment.REFERENCE, request.getReference());
        }

        //If the adjustment is a discount, prevent the value of the adjustment from making the session balance negative
        if(adjustmentType.getType() == AdjustmentTypeType.DISCOUNT) {
            if(NumericalAdjustmentType.fromClientId(request.getNumericalTypeId()) == NumericalAdjustmentType.PERCENTAGE) {
                if(request.getValue() < 0) {
                    adjustment.setValue(0);
                }
                else if(request.getValue() > 100) {
                    adjustment.setValue(100);
                } else {
                    adjustment.setValue(MoneyService.percentageDiscountToInt(request.getValue()));
                }
            }
            else if(NumericalAdjustmentType.fromClientId(request.getNumericalTypeId()) == NumericalAdjustmentType.ABSOLUTE) {
                int remainingTotal = values.get(CalculationKey.REMAINING_TOTAL).intValue();
                int absolute = MoneyService.toPenniesRoundNearest(Math.abs(request.getValue()));
                if(absolute > remainingTotal) {
                    adjustment.setValue(remainingTotal);
                } else {
                    adjustment.setValue(absolute);
                }
            }
        } else if(adjustmentType.getType() == AdjustmentTypeType.PAYMENT || adjustmentType.getType() == AdjustmentTypeType.GRATUITY) {
            adjustment.setValue(MoneyService.toPenniesRoundNearest(Math.abs(request.getValue())));
        }
        return adjustment;
    }

    private Adjustment createAdjustment(String sessionId, AdjustmentType adjustmentType, String staffId, AdjustmentRequest request) {
        return createAdjustment(sessionId, adjustmentType, staffId, NumericalAdjustmentType.fromClientId(request.getNumericalTypeId()), extractItemType(request));
    }

    private ItemType extractItemType(AdjustmentRequest request) {
        String itemType = request.getItemType();
        if(StringUtils.isBlank(itemType)) {
            return ItemType.ALL;
        }
        return ItemType.valueOfRequestName(itemType);
    }

    private Adjustment createAdjustment(String sessionId, AdjustmentType adjustmentType, String staffId, NumericalAdjustmentType numericalAdjustmentType, ItemType itemType) {
        Adjustment adjustment = new Adjustment(sessionId);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setStaffId(staffId);
        adjustment.setNumericalType(numericalAdjustmentType);
        adjustment.setApplicableToItems(itemType);
        return adjustment;
    }

    public HistoricalDataWrapper getAllSessionsAndOrdersByOpenTime(String restaurantId, long start, long end) {
        List<Session> liveSessions = getSessionsBetweenStartTimes(restaurantId, start, end);
        return getHistoricalDataWrapper(restaurantId, start, end, liveSessions);
    }

    private HistoricalDataWrapper getHistoricalDataWrapper(String restaurantId, long start, long end, List<Session> liveSessions) {
        List<String> liveSessionIds = liveSessions.stream().map(Session::getId).collect(Collectors.toList());
        Map<String,List<Order>> liveOrders = liveDataService.getOrdersBySessionIds(liveSessionIds);
        List<SessionArchive> sessionArchives = archiveDataService.getSessionArchivesByClosedTime(restaurantId, start, end)
                .stream().filter(s -> s.getSession() != null && s.getSessionId() != null && s.getOrders() != null && !liveSessionIds.contains(s.getSessionId())).collect(Collectors.toList());
        List<Session> oldSessions = sessionArchives.stream().map(SessionArchive::getSession).collect(Collectors.toList());
        Map<String,List<Order>> oldOrders = sessionArchives.stream().collect(Collectors.toMap(SessionArchive::getSessionId, SessionArchive::getOrders));

        HistoricalDataWrapper wrapper = new HistoricalDataWrapper();
        wrapper.setLiveData(liveSessions);
        wrapper.setLiveOrders(liveOrders);
        wrapper.setOldData(oldSessions);
        wrapper.setOldOrders(oldOrders);

        return wrapper;
    }

    public HistoricalDataWrapper getAllSessionsAndOrdersByCloseTime(String restaurantId, long start, long end) {
        List<Session> liveSessions = getSessionsBetweenClosingTimes(restaurantId, start, end);
        return getHistoricalDataWrapper(restaurantId, start, end, liveSessions);
    }

    public Session upsert(Session session) {
        return sessionRepository.save(session);
    }

    public HostSessionView getSessionView(Session session) {
        Customer customer = null;
        if(StringUtils.isNotBlank(session.getOriginalBookingId())) {
            Booking booking = bookingService.getBookingIncludeCancelled(session.getOriginalBookingId());
            if(booking != null && StringUtils.isNotBlank(booking.getCustomerId())) {
                customer = customerService.getCustomer(booking.getCustomerId());
            }
        }
        List<Order> orders = liveDataService.getOrdersBySessionId(session.getId()).stream().filter(o -> o.getDeleted() == null).collect(Collectors.toList());
        Map<String,RestaurantDefault> defaultMap = masterDataService.getRestaurant(session.getRestaurantId()).getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        Map<String,Staff> allStaff = new HashMap<>();
        if(session.getVoidReason() != null){
            allStaff = masterDataService.getAllStaff(session.getRestaurantId()).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));
        }
        Map<String,Preference> allPreferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));
        List<Customer> allCustomers = customerService.getCustomers(session.getDiners().stream().filter(d -> StringUtils.isNotBlank(d.getCustomerId())).map(Diner::getCustomerId).collect(Collectors.toList()));

        Map<CalculationKey, Number> calculations = sessionCalculationService.calculateValues(session, orders);
        BillSplit billSplit = sessionCalculationService.calculateDinerSplits(session, orders, calculations);

        return new HostSessionView(
                session,
                orders,
                masterDataService.getTables(session.getRestaurantId(), session.getTables()),
                liveDataService.getAllNotifications(session.getRestaurantId(), session.getId()),
                session.getDelay(),
                defaultMap,
                calculations,
                allCustomers,
                customer,
                allStaff,
                allPreferences,
                billSplit,
                SessionCalculationService.isPaid(calculations));
    }

    public List<HostSessionView> getAllTakeawaySessions(String restaurantId, long from, long to) {
        List<Booking> bookings = bookingService.getTakeaways(restaurantId, from, to);
        List<String> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toList());
        List<Session> sessions = getSessionsByBookingIds(restaurantId, bookingIds);
        List<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toList());
        Map<String,List<Order>> orders = liveDataService.getOrdersBySessionIds(sessionIds);

        Map<String,String> customerIds = bookings.stream().filter(b -> StringUtils.isNotBlank(b.getCustomerId()))
                                        .collect(Collectors.toMap(Booking::getId, Booking::getCustomerId));
        Map<String,Customer> customers = customerService.getCustomerByIds(new ArrayList<>(customerIds.values()))
                                        .stream().collect(Collectors.toMap(Customer::getId, Function.identity()));
        Map<String,RestaurantDefault> defaultMap = masterDataService.getRestaurant(restaurantId).getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));

        List<HostSessionView> all = new ArrayList<>();

        Map<String,Staff> allStaff = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));
        Map<String,Preference> allPreferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));

        sessions.forEach(s -> {
            Map<CalculationKey, Number> calculatedValues = sessionCalculationService.calculateValues(s, orders.getOrDefault(s.getId(), new ArrayList<>()));
            all.add(new HostSessionView(
                    s,
                    orders.getOrDefault(s.getId(), new ArrayList<>()),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0,
                    defaultMap,
                    calculatedValues,
                    new ArrayList<>(), //no need for customer list here
                    customers.get(s.getOriginalBookingId()),
                    allStaff,
                    allPreferences,
                    new BillSplit(),
                    SessionCalculationService.isPaid(calculatedValues)));
        });

        return all.stream().sorted(HOST_SESSION_VIEW_COMPARATOR_DESCENDING).collect(Collectors.toList());
    }

    public List<HostSessionView> getAllSessions(String restaurantId) {
        // restaurant defaults
        Map<String, RestaurantDefault> defaultMap = masterDataService.getRestaurant(restaurantId).getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));

        long upperLimit = 1000*60*(10+((Number)defaultMap.get(FixedDefaults.TAKEAWAY_MINIMUM_TIME).getValue()).intValue());
        // all sessions
        List<Session> all = getLiveSessions(restaurantId, System.currentTimeMillis()+upperLimit);

        return getHostSessionViews(restaurantId, defaultMap, all);
    }

    private List<HostSessionView> getHostSessionViews(String restaurantId, Map<String, RestaurantDefault> defaultMap, List<Session> all) {
        // all tables
        Set<String> tableIds = new HashSet<>();
        all.forEach(s -> tableIds.addAll(s.getTables()));
        List<Table> tableList = masterDataService.getTables(restaurantId, tableIds);
        Map<String,List<Table>> tableMap = new HashMap<>();
        for(Session session : all) {
            tableMap.put(session.getId(),
                    tableList.stream().filter(t -> session.getTables().contains(t.getId())).collect(Collectors.toList()));
        }

        // all orders
        Map<String,List<Order>> orderMap = liveDataService.getOrdersBySessionIds(all.stream().map(Session::getId).collect(Collectors.toList()));

        // all notifications
        Map<String,List<Notification>> notificationsMap = liveDataService.getAllNotifications(restaurantId)
                .stream()
                .collect(Collectors.groupingBy(Notification::getSessionId));

        // customers and diners
        List<String> customerIds = all.stream()
                .flatMap(s -> s.getDiners().stream())
                .filter(d -> StringUtils.isNotBlank(d.getCustomerId()))
                .map(Diner::getCustomerId)
                .collect(Collectors.toList());
        customerIds.addAll(all.stream()
                .filter(s -> s.getSessionType() == SessionType.TAKEAWAY && s.getOriginalBooking() != null && s.getOriginalBooking().getCustomerId() != null)
                .map(s -> s.getOriginalBooking().getCustomerId())
                .collect(Collectors.toList()));
        List<Customer> customers = customerService.getCustomers(customerIds.stream().distinct().collect(Collectors.toList()));
        Map<String,Customer> customerMap = customers.stream().collect(Collectors.toMap(Customer::getId, Function.identity()));

        for(Session session : all) {
            List<Diner> diners = session.getDiners().stream().filter(d -> StringUtils.isNotBlank(d.getCustomerId())).collect(Collectors.toList());
            diners.forEach(d -> d.setCustomer(customerMap.get(d.getCustomerId())));
        }

        Map<String,Staff> allStaff = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));
        Map<String,Preference> allPreferences = masterDataService.getAllPreferences().stream().collect(Collectors.toMap(Preference::getId, Function.identity()));

        List<HostSessionView> sessions = all.stream().map(session -> {
            List<Order> orders = orderMap.getOrDefault(session.getId(), new ArrayList<>());
            Map<CalculationKey, Number> calculations = calculationService.calculateValues(session, orders);
            BillSplit billSplit = calculationService.calculateDinerSplits(session, orders, calculations);

            return new HostSessionView(
                    session,
                    orderMap.getOrDefault(session.getId(), new ArrayList<>()),
                    tableMap.get(session.getId()),
                    notificationsMap.getOrDefault(session.getId(), new ArrayList<>()),
                    session.getDelay(),
                    defaultMap,
                    calculations,
                    customers,
                    session.getSessionType() == SessionType.TAKEAWAY ? customerMap.get(session.getOriginalBooking().getCustomerId()) : null,
                    allStaff,
                    allPreferences,
                    billSplit,
                    SessionCalculationService.isPaid(calculations)
            );
        }).collect(Collectors.toList());

        return sessions.stream().sorted(HOST_SESSION_VIEW_COMPARATOR_DESCENDING).collect(Collectors.toList());
    }

    public List<HostSessionView> getClosedSessions(String restaurantId) {
        // restaurant defaults
        Map<String, RestaurantDefault> defaultMap = masterDataService.getRestaurant(restaurantId).getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        List<Session> all = getClosedSessions(restaurantId, defaultMap);
        return getHostSessionViews(restaurantId, defaultMap, all);
    }

    public List<Session> getClosedSessions(String restaurantId, Map<String, RestaurantDefault> defaultMap) {
        long endTime1 = 0;
        CashUp cashUp = archiveDataService.getLastCashUp(restaurantId);
        if(cashUp != null) {
            endTime1 = cashUp.getEndTime() + 1;
        }
        long takeawayMinTime = 1000*60*((Number)defaultMap.get(FixedDefaults.TAKEAWAY_MINIMUM_TIME).getValue()).intValue();
        return getSessionsBetweenClosingTimes(restaurantId, endTime1, System.currentTimeMillis() + takeawayMinTime);
    }

    public void addDiners(String sessionId, List<Diner> diners) {
        sessionRepository.pushDiners(sessionId, diners.stream().filter(d -> StringUtils.isNotBlank(d.getId())).collect(Collectors.toList()));
    }

    public void addDinerToSession(String sessionId, Diner diner) {
        if(StringUtils.isNotBlank(diner.getId())) {
            sessionRepository.pushDiner(sessionId, diner);
        }
    }

    public void removeDinerFromSession(String dinerId) {
        String sessionId = IDAble.extractParentId(dinerId);
        if(StringUtils.isNotBlank(sessionId)) {
            removeDinerFromSession(sessionId, dinerId);
        }
    }

    public void removeDinerFromSession(String sessionId, String dinerId) {
        sessionRepository.removeDiner(sessionId, dinerId);
    }

    public void updateService(String sessionId, Service service) {
        sessionRepository.setService(sessionId, service);
    }

    public void updateName(String sessionId, String name) {
        sessionRepository.setName(sessionId, name);
    }

    public void updateType(String sessionId, SessionType type) {
        sessionRepository.setSessionType(sessionId, type);
    }

    public void updateTables(String id, List<String> tables) {
        sessionRepository.setTables(id, tables);
    }

    public void requestBill(String id) {
        sessionRepository.setBillRequested(id, true);
    }

    public void unRequestBill(String id) {
        sessionRepository.setBillRequested(id, false);
    }

    public void removeFromReports(String id) {
        sessionRepository.setRemoveFromReports(id, true);
    }

    public void includeOnReports(String id) {
        sessionRepository.setRemoveFromReports(id, false);
    }

    public void markPaid(String id, boolean paid, String staffId) {
        Session session = sessionRepository.findOne(id);

        if(session == null) {
            return;
        }

        LOGGER.trace("Set closed on session {}", session.getId());
        if(paid) {
            sessionRepository.setClosed(id, System.currentTimeMillis());
            sessionRepository.setClosedBy(id, staffId);
        } else {
            sessionRepository.setClosed(id, null);
            sessionRepository.setClosedBy(id, null);
        }
        sessionRepository.setPaid(id, paid);
        archiveDataService.updateSession(session);
    }

    public void markDeferredPaid(String id, String settlementSessionId, boolean paid) {
        customerInteractionRepository.setPaid(id, settlementSessionId, paid);
    }

    public void markClosed(String id, String staffId) {
        updateClosed(id, System.currentTimeMillis(), staffId);
    }

    public void updateClosed(String id, Long closedTime, String staffId) {
        sessionRepository.setClosed(id, closedTime);
        sessionRepository.setClosedBy(id, staffId);
        SessionArchive archive = archiveDataService.getSessionArchive(id);
        if(archive != null) {
            archiveDataService.updateSession(sessionRepository.findOne(id));
        }
    }

    public void updateVoidReason(String sessionId, VoidReason reason) {
        sessionRepository.setVoid(sessionId, reason);
    }

    public void updateUnVoid(String sessionId) {
        sessionRepository.setVoid(sessionId, null);
    }

    public boolean exists(String sessionId) {
        return sessionRepository.exists(sessionId);
    }

    public Tuple<Session,Map<CalculationKey,Number>> createTakeaway(CustomerTakeawayOrderRequest takeawayPayload, String customerId, List<String> warnings, String staffId) {
        //create takeaway session, setting Accepted to true if customer is ok to order (based on black marks)
        Customer customer = null;
        if(customerId != null) {
            customer = customerService.getCustomer(customerId);
            if(customer == null) {
                return null;
            }
        }

        //set expected time to now + TakeawayMinimumTime OR the requested time, whichever is greater
        Restaurant restaurant = masterDataService.getRestaurant(takeawayPayload.getRestaurantId());
        Map<String,RestaurantDefault> defaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        int takeawayMinimumTimeMinutes = ((Number)defaultMap.get(FixedDefaults.TAKEAWAY_MINIMUM_TIME).getValue()).intValue();

        Session session = createSessionNoSave();
        session.setSessionType(SessionType.TAKEAWAY);
        session.setRestaurantId(takeawayPayload.getRestaurantId());
        if(customer != null) {
            session.setName(Customer.determineName(customer));
        } else if(StringUtils.isNotBlank(takeawayPayload.getName())) {
            session.setName(takeawayPayload.getName());
        } else {
            session.setName("UNKNOWN");
        }

        Booking booking = new Booking();
        booking.setName(session.getName());
        booking.setDeliveryAddress(takeawayPayload.getAddress());
        booking.setBookingType(BookingType.TAKEAWAY);
        if(takeawayPayload.isDelivery()) {
            session.setTakeawayType(TakeawayType.DELIVERY);
            booking.setTakeawayType(TakeawayType.DELIVERY);
        } else {
            session.setTakeawayType(TakeawayType.COLLECTION);
            booking.setTakeawayType(TakeawayType.COLLECTION);
        }
        booking.setNotes(takeawayPayload.getNotes());
        booking.setTargetTime(takeawayPayload.getRequestedTime() * 1000);
        setStartTime(session, booking, takeawayMinimumTimeMinutes);
        booking.setTelephone(takeawayPayload.getTelephone());
        booking.setCustomerId(customerId);
        booking.setRestaurantId(takeawayPayload.getRestaurantId());
        if((customer != null && FuseBoxAggregationProxy.exceedsBlackMarks(customer)) || warnings.size() > 0) {
            booking.setRejectionNotice(StringUtils.join(warnings,", "));
            booking.setAccepted(false);
        } else {
            booking.setAccepted(true);
        }

        booking = bookingService.insert(booking);

        session.setOriginalBooking(booking);
        Service service = masterDataService.getTakeawayService(takeawayPayload.getRestaurantId());
        if(service != null) {
            session.setService(service);
        }

        session = sessionRepository.save(session);
        applyReadableId(session, restaurant);
        session = sessionRepository.save(session);

        Diner diner = new Diner(session);
        diner.setDefaultDiner(true);
        diner.setCustomerId(customerId);
        diner.setName(RestaurantConstants.DEFAULT_DINER_NAME);
        addDinerToSession(session.getId(), diner);

        takeawayPayload.setItems(takeawayPayload.getItems().stream().filter(c -> c.getQuantity() > 0).collect(Collectors.toList()));
        List<String> modifierIds = takeawayPayload.getItems().stream().flatMap(i -> i .getModifiers() != null ? i.getModifiers().stream() : Stream.empty()).distinct().collect(Collectors.toList());
        Map<String,Modifier> modifierMap = masterDataService.getModifiers(modifierIds).stream().collect(Collectors.toMap(Modifier::getId, Function.identity()));
        Map<String,MenuItem> itemsMap = masterDataService.getAllMenuItems(takeawayPayload.getRestaurantId()).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        // set up the course manually because it's not on the payload
        Map<String, String> menuItemIdToCourseIdMap = new HashMap<>();
        if(restaurant.getTakeawayMenu() != null) {// should never happen
            Menu takeawayMenu = masterDataService.getMenu(restaurant.getTakeawayMenu());
            menuItemIdToCourseIdMap = getMenuItemToCourseMap(itemsMap, takeawayMenu);
        }

        List<Order> orders = new ArrayList<>();
        for(CustomerOrderItemView orderItemView : takeawayPayload.getItems()) {
            MenuItem item = itemsMap.get(orderItemView.getMenuItemId());
            List<Modifier> modifiersWithTaxes = new ArrayList<>();
            orderItemView.getModifiers().forEach(m -> modifiersWithTaxes.add(new Modifier(modifierMap.get(m), taxRateMap.get(modifierMap.get(m).getTaxTypeId()))));
            Order order = new Order(session, orderItemView, modifiersWithTaxes, taxRateMap.get(item.getTaxTypeId()), item, diner, staffId);
            if(menuItemIdToCourseIdMap.containsKey(item.getId())) {
                order.setCourseId(menuItemIdToCourseIdMap.get(item.getId()));
            }
            orders.add(order);
        }
        liveDataService.insertOrders(session, itemsMap.values(), orders, false, true);

        Map<CalculationKey,Number> calculations = sessionCalculationService.calculateValues(session, orders);

        if(takeawayPayload.isDelivery()) {
            int cost = calculations.get(CalculationKey.DELIVERY_TOTAL).intValue();
            sessionRepository.setCalculatedDeliveryCost(session.getId(), cost);
            session.setCalculatedDeliveryCost(cost);
        }

        return new Tuple<>(session, calculations);
    }

    private Map<String, String> getMenuItemToCourseMap(Map<String, MenuItem> itemsMap, Menu takeawayMenu) {
        Map<String,String> menuItemIdToCourseIdMap = new HashMap<>();
        for(Category category : takeawayMenu.getCategories()) {
            List<String> courseIds = category.getCourseIds();
            if(courseIds == null || courseIds.size() == 0) continue;

            for(Group group : category.getGroups()) {
                for(String menuItem : group.getItems()) {
                    if(itemsMap.containsKey(menuItem)) {
                        menuItemIdToCourseIdMap.put(menuItem, courseIds.get(0));
                    }
                }
            }
        }
        return menuItemIdToCourseIdMap;
    }

    public Session createTakeaway(TakeawayPayload takeawayPayload, String rejectionMessage, boolean isPendingWaiterAction, String staffId) {
        Restaurant restaurant = masterDataService.getRestaurant(takeawayPayload.getRestaurantId());
        Map<String,RestaurantDefault> defaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        Session session = createSessionNoSave();
        session.setSessionType(SessionType.TAKEAWAY);
        if(takeawayPayload.isDelivery()) {
            session.setTakeawayType(TakeawayType.DELIVERY);
        } else {
            session.setTakeawayType(TakeawayType.COLLECTION);
        }

        session.setRestaurantId(takeawayPayload.getRestaurantId());
        session.setBillRequested(takeawayPayload.isRequestedBill());
        session.setName(takeawayPayload.getName());

        Booking booking = new Booking();
        booking.setDeliveryAddress(takeawayPayload.getAddress());
        booking.setBookingType(BookingType.TAKEAWAY);
        booking.setNotes(takeawayPayload.getMessage());
        booking.setTargetTime(takeawayPayload.getRequestedTime() * 1000);
        booking.setRejectionNotice(rejectionMessage);
        booking.setTelephone(takeawayPayload.getTelephone());
        booking.setStaffId(staffId);
        if(isPendingWaiterAction) {
            booking.setAccepted(false);
            booking.setRejected(false);
        } else {
            booking.setAccepted(true);
            booking.setRejected(false);
        }
        booking.setRestaurantId(takeawayPayload.getRestaurantId());
        booking.setName(session.getName());
        if(StringUtils.isNotBlank(takeawayPayload.getLeadCustomerId())) {
            booking.setCustomerId(takeawayPayload.getLeadCustomerId());
        }
        if(takeawayPayload.isDelivery()) {
            booking.setTakeawayType(TakeawayType.DELIVERY);
        } else {
            booking.setTakeawayType(TakeawayType.COLLECTION);
        }

        //set expected time to now + TakeawayMinimumTime OR the requested time, whichever is greater
        int takeawayMinimumTimeMinutes = ((Number)defaultMap.get(FixedDefaults.TAKEAWAY_MINIMUM_TIME).getValue()).intValue();
        setStartTime(session, booking, takeawayMinimumTimeMinutes);

        booking = bookingService.insert(booking);

        session.setOriginalBooking(booking);
        Service service = masterDataService.getTakeawayService(takeawayPayload.getRestaurantId());
        if(service != null) {
            session.setService(service);
        }

        session = sessionRepository.save(session);
        applyReadableId(session, restaurant);
        session = sessionRepository.save(session);
        if(takeawayPayload.isDelivery()) {
            int cost = sessionCalculationService.calculateDeliveryCost(session);
            sessionRepository.setCalculatedDeliveryCost(session.getId(), cost);
            session.setCalculatedDeliveryCost(cost);
        }

        Diner diner = new Diner(session);
        diner.setDefaultDiner(false);
        diner.setName(RestaurantConstants.DEFAULT_DINER_NAME);
        diner.setCustomerId(takeawayPayload.getLeadCustomerId());
        addDinerToSession(session.getId(), diner);
        session.getDiners().add(diner);

        return session;
    }

    public static void setStartTime(Session session, Booking booking, int takeawayMinimumTimeMinutes) {
        if(booking.getTargetTime() < System.currentTimeMillis() + (takeawayMinimumTimeMinutes * 60 * 1000)) {
            session.setStartTime(System.currentTimeMillis());
        } else {
            session.setStartTime(booking.getTargetTime());
        }
    }

    public Session createSession(WaitingPartyPayload payload, String restaurantId) {
        Service service = null;

        if(payload.getAdHoc() != null && payload.getAdHoc()) {
            service = masterDataService.getAdhocService(restaurantId);
        } else {
            if(StringUtils.isNotBlank(payload.getServiceId())) {
                service = masterDataService.getService(payload.getServiceId());
            }
        }
        if(payload.getTables() != null && liveDataService.tablesInUse(restaurantId, payload.getTables())) {
            throw new BadStateException("Tables already occupied");
        }

        // 1.7 and previous can put 0 when created from Ad Hoc
        int numberOfDiners = payload.getNumberOfPeople() > 0 ? payload.getNumberOfPeople() : 1;
        payload.setNumberOfPeople(numberOfDiners);
        Party party = createPartyFromPayload(payload, restaurantId);

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        Session session = createSession(party, service, restaurant);

        Diner defaultDiner = new Diner(session);
        defaultDiner.setDefaultDiner(true);
        defaultDiner.setName(RestaurantConstants.DEFAULT_DINER_NAME);
        session.getDiners().add(defaultDiner);

        String customerId = payload.getCustomer() != null ? payload.getCustomer().getId() : null;

        if(payload.getAdHoc() != null && payload.getAdHoc()) {
            session.setSessionType(payload.isRefund() ? SessionType.REFUND : SessionType.ADHOC);
            // add one more diner
            Diner diner = new Diner(session);
            diner.setName(RestaurantConstants.ACTUAL_DINER_PREPEND + " 1");
            session.getDiners().add(diner);
        } else if (payload.getTables() == null || payload.getTables().size() == 0){
            session.setSessionType(SessionType.TAB);
            addDiners(numberOfDiners, customerId, session);
        } else {
            session.setSessionType(SessionType.SEATED);
            if(payload.getTables() != null) {
                session.setTables(payload.getTables());
            }

            addDiners(numberOfDiners, customerId, session);
        }
        session.setStartTime(System.currentTimeMillis());

        //set autotip
        Map<String,RestaurantDefault> defaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        checkTipPercentage(session, defaultMap);

        return upsert(session);
    }

    private void addDiners(int numberOfPeople, String customerId, Session session) {
        for(int i = 0; i < numberOfPeople; i++) {
            Diner diner = new Diner(session);
            if(i == 0 && StringUtils.isNotBlank(customerId)) {
                diner.setCustomerId(customerId);
            }
            diner.setName(RestaurantConstants.ACTUAL_DINER_PREPEND + " " + (i+1));
            session.getDiners().add(diner);
        }
    }

    public Session getByBookingId(String bookingId) {
        return sessionRepository.findByOriginalBookingId(bookingId);
    }

    public Party createPartyFromPayload(WaitingPartyPayload payload, String restaurantId) {
        Party party = new Party(payload, restaurantId);
        party = liveDataService.insert(party);
        if(StringUtils.isNotBlank(party.getCustomerId())) {
            liveDataService.tiePartyCheckIn(party.getCustomerId(), party);
        }
        return party;
    }

    public Session createFromParty(Party party, SessionPayload sessionPayload) {
        Service service = masterDataService.getService(sessionPayload.getServiceId());
        if(service == null) {
            throw new BadStateException("Service Not Found");
        }

        List<Table> tables = masterDataService.getTables(party.getRestaurantId(),sessionPayload.getTables());

        if(tables.size() > 0) {
            if(liveDataService.tablesInUse(party.getRestaurantId(), sessionPayload.getTables())) {
                throw new BadStateException("Tables already in use");
            }
        }

        Restaurant restaurant = masterDataService.getRestaurant(party.getRestaurantId());
        Session session = createSession(party, service, restaurant);

        if(StringUtils.isNotBlank(party.getName())) {
            session.setName(party.getName());
        } else if(StringUtils.isBlank(party.getName()) && tables.size() > 0) {
            session.setName(tables.get(0).getName());
        }

        if(StringUtils.isNotBlank(party.getBookingId())) {
            Booking booking = bookingService.getBookingIncludeCancelled(party.getBookingId());
            if(booking != null && !bookingService.isBookingTiedToSession(booking.getId())) {
                session.setOriginalBooking(booking);
                if(!booking.isAccepted()) {
                    bookingService.acceptBooking(booking.getId());
                }
            }
        }

        addAllDiners(party, session);

        if(StringUtils.isNotBlank(party.getCustomerId())) {
            Diner firstNonDefaultDiner = session.getFirstNonDefaultDiner();
            if(firstNonDefaultDiner != null) {
                firstNonDefaultDiner.setCustomerId(party.getCustomerId());
                liveDataService.setSessionIdOnCheckin(party.getRestaurantId(), party.getCustomerId(), session.getId());
            }
        }

        LOGGER.trace("Added {} diners to session {}", session.getDiners().size(), session.getId());

        if(tables.size() > 0) {
            LOGGER.trace("Set session {} to SEATED", session.getId());
            session.setSessionType(SessionType.SEATED);
            for(Table table : tables) {
                session.getTables().add(table.getId());
            }
            clear(session, false, false, false, true, false);
        } else {
            LOGGER.trace("Set session {} to TAB", session.getId());
            session.setSessionType(SessionType.TAB);
        }

        //set autotip
        Map<String,RestaurantDefault> defaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
        checkTipPercentage(session, defaultMap);

        return sessionRepository.save(session);
    }

    public void checkTipPercentage(Session session, Map<String, RestaurantDefault> defaultMap) {
        if(session.getSessionType() == SessionType.TAB || session.getSessionType() == SessionType.TAKEAWAY || session.getSessionType() == SessionType.REFUND) {
            return;
        }

        if (isAdhocAndRequiresTip(session, defaultMap)
                || (session.getSessionType() == SessionType.SEATED && session.getNumberOfRealDiners() >= ((Number) defaultMap.get(FixedDefaults.COVERS_BEFORE_AUTOTIP).getValue()).intValue())) {
            double tip = ((Number) defaultMap.get(FixedDefaults.DEFAULT_TIP_PERCENTAGE).getValue()).doubleValue();
            session.setTipPercentage(tip);
        }
    }

    private boolean isAdhocAndRequiresTip(Session session, Map<String, RestaurantDefault> defaultMap) {
        if(session.getSessionType() != SessionType.ADHOC) {
            return false;
        }

        return (Boolean) defaultMap.getOrDefault(FixedDefaults.APPLY_AUTOTIP_TO_QO, RestaurantDefault.newDefault(FixedDefaults.APPLY_AUTOTIP_TO_QO, false)).getValue();
    }

    public void addAllDiners(Party party, Session session) {
        Diner defaultDiner = new Diner(session);
        defaultDiner.setDefaultDiner(true);
        defaultDiner.setName(RestaurantConstants.DEFAULT_DINER_NAME);
        session.getDiners().add(defaultDiner);
        //add each diner
        for(int i = 0; i < party.getNumberOfPeople(); i++) {
            Diner diner = new Diner(session);
            diner.setName(RestaurantConstants.ACTUAL_DINER_PREPEND + " " + (i+1));
            session.getDiners().add(diner);
        }
    }

    public Session changeNumberOfDiners(String sessionId, int numberOfDiners) {
        Session session = getSession(sessionId);
        if(numberOfDiners > session.getNumberOfRealDiners()) {
            return addDiners(session, numberOfDiners-session.getNumberOfRealDiners());
        } else if(numberOfDiners < session.getNumberOfRealDiners()) {
            return removeDiners(session, session.getNumberOfRealDiners() - numberOfDiners);
        }

        return session;
    }

    public void incrementCourseAway(String sessionId, String courseId) {
        Session session = findSession(sessionId);
        int count = session.getCourseAwayMessagesSent().getOrDefault(courseId, 0);
        session.getCourseAwayMessagesSent().put(courseId, count+1);
        upsert(session);
    }

    public Session addDiners(Session session, int numberOfDiners) {
        int current = session.getNumberOfRealDiners();
        for(int i = 0; i < numberOfDiners; i++) {
            Diner diner = new Diner(session);
            diner.setName(RestaurantConstants.ACTUAL_DINER_PREPEND + " " + (current + i + 1));
            session.getDiners().add(diner);
        }

        return upsert(session);
    }

    public Session removeDiners(Session session, int numberOfDiners) {
        Iterator<Diner> iterator = session.getDiners().iterator();
        Map<String, List<Order>> ordersByDiner = liveDataService.getOrders(session.getId()).stream().collect(Collectors.groupingBy(Order::getDinerId));
        int removed = 0;
        final ArrayList<Order> defaultValue = new ArrayList<>();
        while(iterator.hasNext() && removed < numberOfDiners) {
            Diner diner = iterator.next();
            if(diner.isDefaultDiner()) {
                continue;
            }
            if(ordersByDiner.getOrDefault(diner.getId(), defaultValue).size() == 0) {
                iterator.remove();
                removed++;
            }
        }
        return upsert(session);
    }

    public void updateStart(String id, long time) {
        sessionRepository.setStartTime(id, time);
    }

    public void updateChairData(String id, List<ChairData> chairData) {
        sessionRepository.setChairData(id, chairData);
    }

    public void updateTip(String id, double tip) {
        sessionRepository.setTipPercentage(id, tip);
    }

    public void updateDeliveryCost(String id, int deliveryCost) {
        sessionRepository.setCalculatedDeliveryCost(id, deliveryCost);
    }

    public void updateDelete(String id) {
        sessionRepository.markDeleted(id, Session.class);
    }

    public void updateDelay(String id, long delay) {
        sessionRepository.setDelay(id, delay);
    }

    public void incrementDelay(String id, long delay) {
        sessionRepository.incrementDelay(id, delay);
    }

    public boolean hasValidPayments(Session session) {
        return session.getAdjustments() != null && session.getAdjustments().stream().anyMatch(a -> !a.isVoided() && a.getValue() > 0);
    }

    public void removeAdjustment(String sessionId, String adjustmentId) {
        sessionRepository.removeAdjustment(sessionId, adjustmentId);
    }

    public void addAdjustment(String sessionId, Adjustment adjustment) {
        sessionRepository.pushAdjustment(sessionId, adjustment);
    }

    public List<Session> getSessionsByBookingIds(String restaurantId, List<String> bookingIds){
        return sessionRepository.findByRestaurantIdAndOriginalBookingIdIn(restaurantId, bookingIds);
    }

    public List<Session> getSessionsByBookingIds(List<String> bookingIds) {
        return sessionRepository.findByOriginalBookingIdIn(bookingIds);
    }

    public List<Session> getLiveSessions(String restaurantId) {
        long limit = 1000*60*((Number)masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.TAKEAWAY_LOCK_WINDOW).getValue()).intValue();
        return getLiveSessions(restaurantId, System.currentTimeMillis()+limit);
    }

    public List<Session> getLiveSessions(String restaurantId, long limit) {
        return sessionRepository.findCurrentLiveSessions(restaurantId, limit);
    }

    public List<Session> getLiveSessionsIncludeClosedWithinLockWindow(String restaurantId) {
        long limit = 1000*60*((Number)masterDataService.getRestaurantDefault(restaurantId, FixedDefaults.TAKEAWAY_LOCK_WINDOW).getValue()).intValue();
        long now = System.currentTimeMillis();
        //do a start from a month ago, just in case restaurant has been rubbish with cash ups
        List<Session> sessions = sessionRepository.findByStartTime(restaurantId, now - 2419200000L, now + limit);
        long closedTimeCutOff = now - limit;
        sessions.removeIf(s -> s.getClosedTime() != null && s.getClosedTime() < closedTimeCutOff);
        return sessions;
    }

    public Session getSessionByPartyId(String id) {
        return sessionRepository.findByOriginalPartyId(id);
    }

    public List<Session> getSessionByPartyIds(List<String> ids) {
        return sessionRepository.findByOriginalPartyIdIn(ids);
    }

    public List<Session> getSessionsBetweenStartTimes(String restaurantId, long startTime1, long startTime2) {
        List<Session> sessions = sessionRepository.findByStartTime(restaurantId, startTime1, startTime2);
        return sessions.stream().filter(s -> s.getDeleted() == null).collect(Collectors.toList());
    }

    public List<Session> getSessionsBetweenClosingTimes(String restaurantId, long endTime1, long endTime2) {
        List<Session> sessions = sessionRepository.findByCloseTime(restaurantId, endTime1, endTime2);
        return sessions.stream().filter(s -> s.getDeleted() == null).collect(Collectors.toList());
    }

    public void delete(String id) {
        sessionRepository.delete(id);
    }

    public void cancelSession(Session session) {
        liveDataService.cancelOrders(session.getId());
        liveDataService.deleteNotifications(session.getRestaurantId(), session.getId());

        Party party = session.getOriginalParty();
        if(party != null) {
            liveDataService.deleteParty(party.getId());
        }

        List<CheckIn> checkIns = liveDataService.getCheckIns(session.getRestaurantId()).stream().filter(c -> session.getId().equals(c.getSessionId())).collect(Collectors.toList());
        liveDataService.deleteCheckIns(checkIns);

        Booking originalBooking = session.getOriginalBooking();
        if(originalBooking != null) {
            bookingService.delete(originalBooking.getId());
        }

        delete(session.getId());
    }

    public void clearWithSession(Session session, boolean deleteOrders, boolean deleteBatches, boolean deleteNotifications, boolean deleteParty, boolean deleteCheckIns) {
        clear(session, deleteOrders, deleteBatches, deleteNotifications, deleteParty, deleteCheckIns);
        sessionRepository.delete(session);
    }

    public void clear(Session session, boolean deleteOrders, boolean deleteBatches, boolean deleteNotifications, boolean deleteParty, boolean deleteCheckIns) {
        if(deleteOrders) {
            List<Order> orders = liveDataService.getAllLiveOrders(session.getId());
            if(orders.size() > 0) {
                liveDataService.deleteOrders(orders);
                archiveDataService.archiveSessionOrders(session, orders);
            }
        }
        if(deleteBatches) {
            List<Batch> batches = liveDataService.getBatchesBySessionId(session.getId());
            if(batches.size() > 0) {
                liveDataService.deleteBatches(batches);
                archiveDataService.archiveSessionBatches(session, batches);
            }
        }
        if(deleteNotifications) {
            List<Notification> notifications = liveDataService.deleteNotifications(session.getRestaurantId(), session.getId());
            if(notifications.size() > 0) {
                archiveDataService.archiveSessionNotifications(session, notifications);
            }
        }
        if(deleteParty && StringUtils.isNotBlank(session.getOriginalPartyId())) {
            String originalPartyId = session.getOriginalPartyId();
            Party party = liveDataService.getParty(originalPartyId);
            liveDataService.deleteParty(originalPartyId);
            archiveDataService.archiveParty(session, party);
        }
        if(deleteCheckIns) {
            List<CheckIn> checkIns = liveDataService.getCheckIns(session.getRestaurantId()).stream().filter(c -> session.getId().equals(c.getSessionId())).collect(Collectors.toList());
            liveDataService.deleteCheckIns(checkIns);
            archiveDataService.archiveCheckIns(session, checkIns);
        }
    }

    public Party unclearParty(String sessionId) {
        SessionArchive archive = archiveDataService.getSessionArchive(sessionId);
        if(archive != null && archive.getParty() != null) {
            Party party = archive.getParty();
            LOGGER.trace("Put party {} back into parties repo", party.getId());
            liveDataService.upsert(party);
            return party;
        }
        return null;
    }

    public void updateCashUpId(List<String> sessionIds, String cashUpId) {
        sessionRepository.updateCashUpId(sessionIds, cashUpId);
        //update the ids in sessionArchive too
        List<SessionArchive> sessionArchives = archiveDataService.getSessionArchives(sessionIds);
        for(SessionArchive archive : sessionArchives) {
            if(archive.getSession() != null && archive.getSession().getCashUpId() == null) {
                archive.getSession().setCashUpId(cashUpId);
            }
        }
        archiveDataService.updateArchives(sessionArchives);
    }

    public Session createDefaultSession(Restaurant restaurant, String name) {
        Party party = new Party();
        party.setRestaurantId(restaurant.getId());
        party.setInstantiatedFrom(ActivityInstantiationConstant.WAITER);
        party.setNumberOfPeople(1);
        party.setName(name == null ? "Session" : name);
        party.setPartyType(PartyType.WALK_IN);
        long now = System.currentTimeMillis();
        party.setTime(now);
        party.setArrivedTime(now);

        Service service = restaurant.getServices().stream().filter(Service::isDefaultService).findFirst().orElse(null);
        if(service == null) {
            throw new IllegalStateException("Default service not found for this restaurant");
        }

        party = liveDataService.upsert(party);
        Session session = createSession(party, service, restaurant);
        addAllDiners(party, session);
        return session;
    }

    private Session createSession(Party party, Service service, Restaurant restaurant) {
        Session session = createSessionNoSave(party, service);

        //save the session to get an id
        session = sessionRepository.save(session);
        applyReadableId(session, restaurant);
        session = sessionRepository.save(session);

        //create notifications
        sessionTimingService.createNotifications(session);

        return session;
    }

    private Session createSessionNoSave(Party party, Service service) {
        Session session = createSessionNoSave();
        session.setOriginalParty(party);
        session.setRestaurantId(party.getRestaurantId());
        session.setStartTime(System.currentTimeMillis());
        session.setName(party.getName());

        if(service != null) {
            session.setService(service);
        }

        return session;
    }

    private Session createSessionNoSave() {
        return new Session();
    }

    private void applyReadableId(Session session, Restaurant restaurant) {
        if(restaurant.getSessionIdStrategy() == SessionIdStrategy.HASH && session.getId() != null) {
            session.setReadableId(String.valueOf(Math.abs(session.getId().hashCode())));
        } else if(restaurant.getSessionIdStrategy() == SessionIdStrategy.NUMERIC_ASCENDING) {
            SessionNumber sessionNumber = sessionNumberRepository.incrementAndGet(restaurant.getId());
            session.setReadableId(String.valueOf(sessionNumber.getTotalSessionsCreated()));
        }
    }

    public boolean isFullyPaid(Session session) {
        return sessionCalculationService.isPaid(session);
    }

    public void closeSession(Session session) {
        long closedTime = System.currentTimeMillis();
        session.setClosedTime(closedTime);
        sessionRepository.setClosed(session.getId(), closedTime);
        clear(session,false,false,true,true, true);
    }

    public void closeSession(Session session, String staffId) {
        sessionRepository.setClosedBy(session.getId(), staffId);
        closeSession(session);
    }

    public boolean defermentExists(String restaurantId, String sessionId) {
        return liveDataService.getDeferredSession(restaurantId, sessionId) != null;
    }

    public void deferPayment(String sessionId, AdjustmentType discountType, String staffId, HostCustomerViewBasic customerView) {
        deferPayment(sessionId, discountType, staffId, customerView.getId());
    }

    public void deferPayment(String sessionId, AdjustmentType discountType, String staffId, String customerId) {
        Session session = deferPayment(sessionId, discountType, staffId, customerService.getCustomer(customerId));
        CustomerInteractionDeferredSession deferredSession = new CustomerInteractionDeferredSession(customerId, session.getRestaurantId(), sessionId, staffId);
        liveDataService.saveInteraction(deferredSession);
    }

    public Adjustment settleDeferredSession(String sessionId, String staffId, AdjustmentRequest request) throws IllegalStateException {
        Session session = findSession(sessionId);
        if(session == null || !session.isLinked()) {
            throw new IllegalStateException("Session not found");
        }
        return settleDeferredSession(session, staffId, request);
    }

    public Adjustment settleDeferredSession(Session session, String staffId, AdjustmentRequest request) {
        List<Order> orders = liveDataService.findOrders(session);
        Map<CalculationKey, Number> calculatedValues = sessionCalculationService.calculateValues(session, orders);
        //add the adjustments onto the session and ensure it is fully paid
        AdjustmentType adjustmentType = masterDataService.getAdjustmentType(request.getAdjustmentTypeId());
        Adjustment adjustment = createAdjustment(session.getId(), adjustmentType, staffId, request, calculatedValues);
        session.getAdjustments().add(adjustment);
        calculatedValues = sessionCalculationService.calculateValues(session, orders);

        if(!SessionCalculationService.isPaid(calculatedValues)) {
            throw new IllegalStateException("Insufficient payment supplied - please execute a Pay & Close");
        }

        session.setClosedBy(staffId);
        session.setClosedTime(System.currentTimeMillis());
        session.setMarkedAsPaid(true);
        session = sessionRepository.save(session);
        clear(session,false,true,true,true, true);
        for (String deferredSessionId : session.getLinkedSession()) {
            CustomerInteractionDeferredSession customerInteraction = liveDataService.getDeferredSession(session.getRestaurantId(), deferredSessionId);
            markDeferredPaid(customerInteraction.getId(), session.getId(), true);
        }

        return adjustment;
    }

    public Session voidDeferredSessionAndOrders(String sessionId, String staffId) throws IllegalStateException {
        Triple<Session,Session,CustomerInteractionDeferredSession> triple = copyDeferredSessionAndOrders(sessionId);
        Session copy = triple.getMiddle();
        CustomerInteractionDeferredSession deferred = triple.getRight();
        Customer customer = customerService.getCustomer(deferred.getCustomerId());

        // void the session and then delete deferred
        copy = voidAllPayments(copy, staffId);
        VoidReason voidReason = new VoidReason();
        voidReason.setStaffId(staffId);
        voidReason.setTime(System.currentTimeMillis());
        voidReason.setDescription("Voided On Account: " + (customer != null ? Customer.determineName(customer) : "Unknown"));
        copy.setVoidReason(voidReason);
        closeSession(copy, staffId);
        copy.setClosedBy(staffId);

        liveDataService.deleteCustomerInteraction(deferred.getId());
        return copy;
    }

    public Triple<Session,Session,CustomerInteractionDeferredSession> copyDeferredSessionAndOrders(String sessionId) throws IllegalStateException {
        Tuple<Session,CustomerInteractionDeferredSession> tuple = findSessionAndDeferred(sessionId);
        Session original = tuple.getA();

        Session copy = sessionRepository.findByLinkedSessionContains(sessionId);
        if(copy != null) {
            return Triple.of(original, copy, tuple.getB());
        }
        copy = copyDeferredSession(original);
        List<Order> orders = liveDataService.findOrders(original);
        copyOrders(copy, orders);

        return Triple.of(original, copy, tuple.getB());
    }

    private Tuple<Session,CustomerInteractionDeferredSession> findSessionAndDeferred(String sessionId) throws IllegalStateException {
        Session original = findSession(sessionId);
        if(original == null) {
            throw new IllegalStateException("Cannot find original session");
        }

        CustomerInteractionDeferredSession deferredSession = liveDataService.getDeferredSession(original.getRestaurantId(), sessionId);
        if(deferredSession == null || deferredSession.isPaid()) {
            throw new IllegalStateException("Session is already paid or cannot be found");
        }
        return new Tuple<>(original, deferredSession);
    }

    private void copyOrders(Session copy, List<Order> originalOrders) {
        List<Order> newOrders = new ArrayList<>();
        String dinerId = (copy.getDiners() != null && copy.getDiners().size() > 0) ? copy.getDiners().get(0).getId() : null;
        for(Order order : originalOrders) {
            Order orderCopy = order.copy();
            orderCopy.setSessionId(copy.getId());
            orderCopy.setDoneTime(System.currentTimeMillis());
            orderCopy.setCompleted(System.currentTimeMillis());
            orderCopy.setDinerId(dinerId);
            newOrders.add(orderCopy);
        }
        liveDataService.insertOrders(newOrders);
    }

    public Session copyDeferredSession(Session original) {
        Session copy = createSessionCopy(original, "SETTLEMENT OF ");
        copy.setTables(new ArrayList<>());
        copy.setSessionType(SessionType.TAB);
        copy.setBillRequested(true);
        copy = sessionRepository.save(copy);

        // can only do diners after a save
        for(Diner diner : original.getDiners()) {
            Diner dinerCopy = createDinerCopy(copy, diner);
            copy.getDiners().add(dinerCopy);
        }

        // can only do adjustments after a save
        for(Adjustment adjustment : original.getAdjustments()) {
            if(isDeferredAdjustment(adjustment)) {
                continue;
            }
            Adjustment adjustmentCopy = createAdjustmentCopy(copy, adjustment);
            copy.getAdjustments().add(adjustmentCopy);
        }

        copy = sessionRepository.save(copy);
        applyReadableId(copy, masterDataService.getRestaurant(copy.getRestaurantId()));
        return sessionRepository.save(copy);
    }

    public static boolean isDeferredAdjustment(Adjustment adjustment) {
        return adjustment.getAdjustmentType().getName().equals(RestaurantConstants.DEFER_ADJUSTMENT)
                && adjustment.getAdjustmentType().getType() == AdjustmentTypeType.DISCOUNT;
    }

    private Adjustment createAdjustmentCopy(Session session, Adjustment original) {
        Adjustment copy = new Adjustment(session.getId());
        copy.setAdjustmentType(original.getAdjustmentType());
        copy.setNumericalType(original.getNumericalType());
        copy.setValue(original.getValue());
        copy.setCreated(System.currentTimeMillis());
        copy.setStaffId(original.getStaffId());
        copy.setSpecialAdjustmentData(copy.getSpecialAdjustmentData());
        copy.setVoided(copy.isVoided());
        copy.setVoidedByStaffId(copy.getVoidedByStaffId());
        return copy;
    }

    private Diner createDinerCopy(Session session, Diner original) {
        Diner copy = new Diner(session);
        copy.setCustomerId(copy.getCustomerId());
        copy.setDefaultDiner(original.isDefaultDiner());
        copy.setName(original.getName());
        return copy;
    }

    private Session createSessionCopy(Session original, String prepend) {
        Session copy = createSessionNoSave();
        copy.setRestaurantId(original.getRestaurantId());
        copy.setStartTime(System.currentTimeMillis());
        copy.setName(original.getName() + " (" + prepend + original.getReadableId() + ")");
        copy.setBillRequested(original.isBillRequested());
        copy.setRemoveFromReports(original.isRemoveFromReports());
        copy.setVoidReason(original.getVoidReason());
        copy.setService(original.getService());
        copy.setSessionType(original.getSessionType());
        copy.setChairData(original.getChairData());
        copy.setTipPercentage(original.getTipPercentage());
        copy.setTables(original.getTables());
        copy.setCalculatedDeliveryCost(original.getCalculatedDeliveryCost());
        copy.setTakeawayType(original.getTakeawayType());
        copy.setCourseAwayMessagesSent(original.getCourseAwayMessagesSent());
        copy.setReadableId(original.getReadableId() + " [COPY]");
        if(copy.getLinkedSession() == null) {
            copy.setLinkedSession(new ArrayList<>());
        }
        copy.getLinkedSession().add(original.getId());
        return copy;
    }

    private Session deferPayment(String sessionId, AdjustmentType discountType, String staffId, Customer customer) {
        Session session = getSession(sessionId);
        boolean adjustmentExists = false;
        if(session.getAdjustments() != null && session.getAdjustments().size() > 0) {
            for(Adjustment adjustment : session.getAdjustments()) {
                if(adjustment.getAdjustmentType().getId().equals(discountType.getId())) {
                    adjustmentExists = true;
                    break;
                }
            }
        }
        if(!adjustmentExists) {
            Adjustment adjustment = createAdjustment(sessionId, discountType, staffId, NumericalAdjustmentType.PERCENTAGE, ItemType.ALL);
            adjustment.getSpecialAdjustmentData().put(RestaurantConstants.ADJUSTMENT_DATA_KEY_DEFERMENT_NOTE, summarizeDeferredSessionForCustomer(customer));
            adjustment.setValue(1000);
            session.getAdjustments().add(adjustment);
        }

        if(session.getClosedTime() == null) {
            session.setClosedBy(staffId);
            session.setClosedTime(System.currentTimeMillis());
            session.setMarkedAsPaid(true);
            // get the session state again
            session = sessionRepository.save(session);
            clear(session,false,false,true,true, true);
        }
        return session;
    }

    public static String getTableName(Restaurant restaurant, Session session) {
        if(session.getSessionType() == SessionType.SEATED) {
            return getTableName(restaurant,session.getTables());
        } else {
            return session.getName() == null ? String.valueOf(session.getId().hashCode()) : session.getName();
        }
    }

    private static String getTableName(Restaurant restaurant, List<String> tables) {
        List<String> actualNames = new ArrayList<>();
        Map<String,String> tableIdToName = restaurant.getTables().stream().filter(t -> t.getName() != null).collect(Collectors.toMap(Table::getId, Table::getName));
        tables.forEach(t -> actualNames.add(tableIdToName.getOrDefault(t, "")));
        return String.join(",",actualNames);
    }

    private String summarizeDeferredSessionForCustomer(Customer customer) {
        String text = "";
        if(customer == null) {
            return text + "(Unknown customer)";
        }
        return text + "[" + Customer.determineName(customer) + " " + HostCustomerView.formatPhoneNumber(customer.getInternationalCode(), customer.getPhoneNumber()) + "]";
    }
}
