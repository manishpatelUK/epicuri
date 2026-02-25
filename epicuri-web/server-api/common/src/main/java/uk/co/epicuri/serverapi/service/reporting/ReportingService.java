package uk.co.epicuri.serverapi.service.reporting;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PACReport;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.CSVWrapper;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HistoricalDataWrapper;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ModifierGroup;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.ReportingConstraints;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.external.PaymentSenseReport;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.CashUpAggregator;
import uk.co.epicuri.serverapi.engines.DateTimeConstants;
import uk.co.epicuri.serverapi.engines.ReportingAggregator;
import uk.co.epicuri.serverapi.engines.reporting.CustomerStat;
import uk.co.epicuri.serverapi.engines.reporting.OrderAggregate;
import uk.co.epicuri.serverapi.engines.reporting.reports.*;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentSenseRestService paymentSenseRestService;

    @Autowired
    private ArchiveDataService archiveDataService;

    public ReportingConstraints createConstraints(String token, String start, String end) {
        String restaurantId = authenticationService.getRestaurantId(token);
        if(restaurantId == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        if(restaurant == null || restaurant.getIANATimezone() == null) {
            throw new IllegalArgumentException("Invalid token: restaurant not found or timezone information missing");
        }

        ZoneId zoneId = ZoneId.of(restaurant.getIANATimezone());

        long startLong = DateTimeConstants.convertToLong(zoneId, start, DateTimeConstants.containsTime(start) ? null : LocalTime.MIN);
        long endLong = DateTimeConstants.convertToLong(zoneId, end, DateTimeConstants.containsTime(end) ? null : LocalTime.MAX);

        return createConstraints(restaurant, zoneId, startLong, endLong);
    }

    public ReportingConstraints createConstraints(Restaurant restaurant, ZoneId zoneId, long startUTC, long endUTC) {
        ReportingConstraints reportingConstraints = new ReportingConstraints();
        reportingConstraints.setStart(startUTC);
        reportingConstraints.setEnd(endUTC);
        reportingConstraints.setRestaurantId(restaurant.getId());
        reportingConstraints.setZoneId(zoneId);
        reportingConstraints.setCurrency(restaurant.getISOCurrency());
        reportingConstraints.setIntegrations(restaurant.getIntegrations().keySet());

        return reportingConstraints;
    }

    public Tuple<List<Session>, List<Order>> getCleanedSessionsAndOrders(String restaurantId, long start, long end) {
        HistoricalDataWrapper historicalDataWrapper = sessionService.getAllSessionsAndOrdersByCloseTime(restaurantId, start, end);
        List<Session> sessions = historicalDataWrapper.allSessions();
        ReportingAggregator.cleanSessions(sessions, true, true);
        Set<String> sessionIds = sessions.stream().map(Session::getId).collect(Collectors.toSet());
        List<Order> orders = new ArrayList<>();
        historicalDataWrapper.allOrders().values().forEach(orders::addAll);
        ReportingAggregator.cleanOrders(orders, true, true);
        orders.removeIf(o -> !sessionIds.contains(o.getSessionId()));

        return new Tuple<>(sessions, orders);
    }

    public List<Order> getOrders(String restaurantId, long start, long end) {
        Tuple<List<Session>, List<Order>> tuple = getCleanedSessionsAndOrders(restaurantId, start, end);
        return tuple.getB();
    }

    public List<CustomerDetailsReportLine> getCustomerDetailsReportLines(ReportingConstraints reportingConstraints) {
        String restaurantId = reportingConstraints.getRestaurantId();
        long startLong = reportingConstraints.getStart();
        long endLong = reportingConstraints.getEnd();
        ZoneId zoneId = reportingConstraints.getZoneId();

        List<Booking> bookings = bookingService.getBookings(restaurantId, startLong, endLong, false);
        List<String> customerIds = bookings.stream().filter(b -> b.getCustomerId() != null).map(Booking::getCustomerId).collect(Collectors.toList());
        Map<String,Customer> customerById;
        Map<String,List<Booking>> bookingsByCustomerId;
        if(customerIds.size() == 0) {
            //saves a trip to the db!
            bookingsByCustomerId = new HashMap<>();
            customerById = new HashMap<>();
        } else {
            bookingsByCustomerId = bookingService.getBookingsByCustomerId(restaurantId, customerIds, startLong, endLong, false);
            customerById = customerService.getCustomerByIds(customerIds).stream().collect(Collectors.toMap(Customer::getId, Function.identity()));
        }
        Map<String,List<Booking>> bookingsByNonCustomerEmailOrPhone = new HashMap<>();
        bookings.forEach(b->
        {
            if(b.getCustomerId() == null && StringUtils.isNotBlank(b.getEmail())) {
                bookingsByNonCustomerEmailOrPhone.computeIfAbsent(b.getEmail(), k -> bookingsByNonCustomerEmailOrPhone.put(k, new ArrayList<>()));
                bookingsByNonCustomerEmailOrPhone.get(b.getEmail()).add(b);
            } else if(b.getCustomerId() == null && StringUtils.isNotBlank(b.getTelephone()) && b.getTelephone().length() > 7) {
                bookingsByNonCustomerEmailOrPhone.computeIfAbsent(b.getTelephone(), k -> bookingsByNonCustomerEmailOrPhone.put(k, new ArrayList<>()));
                bookingsByNonCustomerEmailOrPhone.get(b.getTelephone()).add(b);
            }
        });

        Tuple<List<Session>, List<Order>> tuple = getCleanedSessionsAndOrders(restaurantId, startLong, endLong);
        Map<String,Session> sessionByBookingId = tuple.getA().stream().filter(s -> s.getOriginalBookingId() != null).collect(Collectors.toMap(Session::getOriginalBookingId, Function.identity()));
        Map<String,List<Order>> ordersBySession = tuple.getB().stream().collect(Collectors.groupingBy(Order::getSessionId));

        List<CustomerDetailsReportLine> lines = new ArrayList<>();
        // go through bookingsByCustomerId and check in bookingsByNonCustomerEmailOrPhone in case they exist in there
        bookingsByCustomerId.forEach((k,v) -> {
            Customer customer = customerById.get(k);
            CustomerStat customerStat = new CustomerStat();

            if(StringUtils.isNotBlank(customer.getEmail()) && bookingsByNonCustomerEmailOrPhone.containsKey(customer.getEmail())) {
                v.addAll(bookingsByNonCustomerEmailOrPhone.get(customer.getEmail()));
                bookingsByNonCustomerEmailOrPhone.remove(customer.getEmail());
            } else if (StringUtils.isNotBlank(customer.getPhoneNumber()) && bookingsByNonCustomerEmailOrPhone.containsKey(customer.getPhoneNumber())) {
                v.addAll(bookingsByNonCustomerEmailOrPhone.get(customer.getPhoneNumber()));
                bookingsByNonCustomerEmailOrPhone.remove(customer.getPhoneNumber());
            }

            List<Session> sessionsToAggregate = new ArrayList<>();

            updateStats(sessionByBookingId, v, customerStat, sessionsToAggregate);

            CustomerDetailsReportLine line = new CustomerDetailsReportLine();
            line.setName(Customer.determineName(customer));
            line.setPhone(StringUtils.defaultIfBlank("0" + customer.getPhoneNumber(),""));
            line.setEmail(StringUtils.defaultIfBlank(customer.getEmail(),""));
            line.setReservations(String.valueOf(customerStat.getReservations()));
            line.setTakeaways(String.valueOf(customerStat.getTakeaways()));
            line.setFirstBooking(customerStat.getMinTime() > 0 ? DateTimeConstants.convertToDateTime(zoneId, customerStat.getMinTime()) : "");
            line.setLastBooking(customerStat.getMaxTime() < Long.MAX_VALUE ? DateTimeConstants.convertToDateTime(zoneId, customerStat.getMaxTime()) : "");

            CashUpAggregator aggregator = getCashUpAggregator(ordersBySession, sessionsToAggregate);
            Integer total = aggregator.getReportValues().get(CashUpKeys.TOTAL_SALES);
            line.setTotalValue(total == null ? "0" : String.format("%.2f", MoneyService.toMoneyRoundNearest(total)));
            lines.add(line);
        });

        // go through bookingsByNonCustomerEmailOrPhone, leaving out any customers already covered
        bookingsByNonCustomerEmailOrPhone.forEach((k,v) -> {
            CustomerStat customerStat = new CustomerStat();
            List<Session> sessionsToAggregate = new ArrayList<>();

            updateStats(sessionByBookingId, v, customerStat, sessionsToAggregate);

            CustomerDetailsReportLine line = new CustomerDetailsReportLine();
            line.setName("");
            line.setPhone(k.contains("@") ? "" : k);
            line.setEmail(!k.contains("@") ? "" : k);
            line.setReservations(String.valueOf(customerStat.getReservations()));
            line.setTakeaways(String.valueOf(customerStat.getTakeaways()));
            line.setFirstBooking(customerStat.getMinTime() > 0 ? DateTimeConstants.convertToDateTime(zoneId, customerStat.getMinTime()) : "");
            line.setLastBooking(customerStat.getMaxTime() < Long.MAX_VALUE ? DateTimeConstants.convertToDateTime(zoneId, customerStat.getMaxTime()) : "");

            CashUpAggregator aggregator = getCashUpAggregator(ordersBySession, sessionsToAggregate);
            Integer total = aggregator.getReportValues().get(CashUpKeys.TOTAL_SALES);
            line.setTotalValue(total == null ? "0" : String.format("%.2f", MoneyService.toMoneyRoundNearest(total)));
            lines.add(line);
        });
        return lines;
    }

    private void updateStats(Map<String, Session> sessionByBookingId, List<Booking> v, CustomerStat customerStat, List<Session> sessionsToAggregate) {
        for(Booking booking : v){
            if(!sessionByBookingId.containsKey(booking.getId())) {
                continue;
            }

            if(booking.getBookingType() == BookingType.RESERVATION) {
                customerStat.incrementReservations();
            } else if(booking.getBookingType() == BookingType.TAKEAWAY) {
                customerStat.incrementTakeaways();
            }

            if(booking.getTargetTime() < customerStat.getMinTime()) {
                customerStat.setMinTime(booking.getTargetTime());
            }

            if(booking.getTargetTime() > customerStat.getMaxTime()) {
                customerStat.setMaxTime(booking.getTargetTime());
            }

            sessionsToAggregate.add(sessionByBookingId.get(booking.getId()));
        }
    }

    private CashUpAggregator getCashUpAggregator(Map<String,List<Order>> ordersBySession, List<Session> sessionsToAggregate) {
        CashUpAggregator aggregator = new CashUpAggregator(sessionCalculationService);
        for(Session session : sessionsToAggregate) {
            List<Order> orders = ordersBySession.get(session.getId());
            if(orders.size() > 0) {
                aggregator.addSession(session, orders);
            }
        }
        aggregator.aggregate();
        return aggregator;
    }

    public List<AggregatedItemsReportLine> getAggregatedItemsReportLines(ReportingConstraints reportingConstraints) {
        String restaurantId = reportingConstraints.getRestaurantId();
        long startLong = reportingConstraints.getStart();
        long endLong = reportingConstraints.getEnd();

        Tuple<List<Session>, List<Order>> cleanedSessionsAndOrders = getCleanedSessionsAndOrders(restaurantId, startLong, endLong);
        List<Order> orders = cleanedSessionsAndOrders.getB();
        List<Session> allSessions = cleanedSessionsAndOrders.getA();
        allSessions.removeIf(Session::isLinked);
        Map<String, Session> sessionMap = allSessions.stream().collect(Collectors.toMap(Session::getId, Function.identity()));
        Map<String,OrderAggregate> ordersByMenuItem = ReportingAggregator.aggregateByMenuItem(orders, sessionMap);
        Map<String,OrderAggregate> ordersByMenuItemPLU = reportingConstraints.isAggregateByPLU() ? ReportingAggregator.aggregateByMenuItemPLU(orders, sessionMap) : new HashMap<>();
        List<AggregatedItemsReportLine> lines = new ArrayList<>();
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        ZoneId restaurantZoneId = reportingConstraints.getZoneId();

        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for(Map.Entry<String,OrderAggregate> entry : reportingConstraints.isAggregateByPLU() ? ordersByMenuItemPLU.entrySet() : ordersByMenuItem.entrySet()) {
            OrderAggregate orderAggregate = entry.getValue();
            MenuItem item = orderAggregate.getMenuItem();
            if(item == null) {
                continue;
            }

            AggregatedItemsReportLine line = new AggregatedItemsReportLine();

            line.setItemId(reportingConstraints.isAggregateByPLU() && StringUtils.isNotBlank(item.getPlu()) ? item.getPlu() : item.getId());
            line.setItemName(wrapQuotes(reportingConstraints.isAggregateByPLU() ? getAggregatedMenuItemName(orderAggregate.getOrders()) : item.getName()));
            line.setPrice(reportingConstraints.isAggregateByPLU() ? getAggregatedPrice(orderAggregate.getOrders()) : String.format("%.2f", MoneyService.toMoneyRoundNearest(item.getPrice())));
            if(orderAggregate.lastOrderTime() != Long.MAX_VALUE) {
                ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(orderAggregate.lastOrderTime(), restaurantZoneId);
                line.setLastSold(dateTimeFormat.format(restaurantTime));
            } else {
                line.setLastSold("");
            }
            line.setQuantity(String.valueOf(orderAggregate.getQuantity()));
            List<Order> ordersMinusRefunds = orderAggregate.getOrdersNegated();
            int orderValue = SessionCalculationService.getOrderValue(ordersMinusRefunds);
            line.setValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(orderValue)));
            line.setValueExcludingMods(String.format("%.2f", MoneyService.toMoneyRoundNearest(SessionCalculationService.getOrderValueExcludingModifiers(ordersMinusRefunds))));

            if(orderAggregate.getQuantity() > 0) {
                double averagePrice = MoneyService.toMoneyRoundNearest(orderValue) / orderAggregate.getQuantity();
                averagePrice = MoneyService.toMoneyRoundNearest(MoneyService.toPenniesRoundNearest(averagePrice));
                line.setAverageSalesPrice(String.format("%.2f", (averagePrice)));
            } else {
                line.setAverageSalesPrice("0");
            }
            line.setType(wrapQuotes(reportingConstraints.isAggregateByPLU() ? getAggregatedType(orderAggregate.getOrders()) : item.getType().toString()));
            line.setTaxName(reportingConstraints.isAggregateByPLU() ? getAggregatedTaxName(orderAggregate.getOrders()) : taxRateMap.get(item.getTaxTypeId()).getName());
            line.setTaxRate(reportingConstraints.isAggregateByPLU() ? getAggregatedTaxRate(orderAggregate.getOrders()) : String.format("%.2f",taxRateMap.get(item.getTaxTypeId()).getRate()/10D) + "%");

            lines.add(line);
        }
        return lines;
    }

    public List<AdjustmentReportLine> getPaymentReportLines(ReportingConstraints reportingConstraints) {
        String restaurantId = reportingConstraints.getRestaurantId();
        long startLong = reportingConstraints.getStart();
        long endLong = reportingConstraints.getEnd();
        Map<String,Staff> staffMap = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));

        ZoneId restaurantZoneId = reportingConstraints.getZoneId();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Tuple<List<Session>, List<Order>> tuple = getCleanedSessionsAndOrders(restaurantId, startLong, endLong);
        Map<String,List<Order>> ordersBySession = tuple.getB().stream().collect(Collectors.groupingBy(Order::getSessionId));

        List<AdjustmentReportLine> lines = new ArrayList<>();
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        for(Session session : tuple.getA()) {
            List<Order> orders = ordersBySession.get(session.getId());
            if(orders == null) {
                orders = new ArrayList<>();
            }

            int total = SessionCalculationService.getOrderValue(orders);
            Map<Adjustment,Integer> paymentsAndDiscounts = SessionCalculationService.allAdjustmentBreakdown(session, total).getB();
            List<Adjustment> sortedAdjustments = new ArrayList<>(paymentsAndDiscounts.keySet());
            sortedAdjustments.sort(Comparator.comparingLong(Adjustment::getCreated));
            Map<CalculationKey,Number> calculations = sessionCalculationService.calculateValues(session, orders);
            int change = calculations.get(CalculationKey.CHANGE_DUE).intValue();

            Map<String,Integer> shareOfChange = new HashMap<>();
            if(change > 0) {
                //split the change evenly between each changeable payment
                List<Adjustment> changeableAdjustments = sortedAdjustments.stream().filter(a -> a.getAdjustmentType().isSupportsChange() && a.getAdjustmentType().getType() == AdjustmentTypeType.PAYMENT).collect(Collectors.toList());
                int count = changeableAdjustments.size();
                int[] splits = MoneyService.split(change, count);
                if(splits.length == changeableAdjustments.size()) { //should always be true I think
                    for (int i = 0; i < splits.length; i++) {
                        shareOfChange.put(changeableAdjustments.get(i).getId(), splits[i]);
                    }
                }
            }

            String tableName = "";
            if(session.getSessionType() == SessionType.SEATED) {
                tableName = SessionService.getTableName(restaurant, session).replace(",","|");
            }
            for(Adjustment adjustment : sortedAdjustments) {
                AdjustmentReportLine line = new AdjustmentReportLine();

                ZonedDateTime restaurantTime = TimeUtil.getRestaurantTime(adjustment.getCreated(), restaurantZoneId);
                line.setDate(dateTimeFormat.format(restaurantTime));
                line.setSessionId(session.getReadableId());
                line.setSessionType(session.getSessionType().toString());
                line.setTableNumber(tableName);
                line.setStaffId(adjustment.getStaffId());
                if(staffMap.containsKey(adjustment.getStaffId())) {
                    line.setStaffName(staffMap.get(adjustment.getStaffId()).getUserName());
                } else {
                    line.setStaffName("[NOT AVAILABLE]");
                }
                line.setSessionVoided(!SessionCalculationService.isPaid(calculations) || session.getVoidReason() != null ? "Y" : "N");
                boolean voided = adjustment.isVoided();
                line.setAdjustmentVoided(voided ? "Y" : "N");
                if(voided) {
                    if(StringUtils.isNotBlank(adjustment.getVoidedByStaffId()) && staffMap.containsKey(adjustment.getVoidedByStaffId())) {
                        line.setAdjustmentVoidedByID(adjustment.getVoidedByStaffId());
                        line.setAdjustmentVoidedByUser(staffMap.get(adjustment.getVoidedByStaffId()).getUserName());
                    } else {
                        line.setAdjustmentVoidedByID("");
                        line.setAdjustmentVoidedByUser("");
                    }
                } else {
                    line.setAdjustmentVoidedByID("");
                    line.setAdjustmentVoidedByUser("");
                }
                line.setAdjustmentType(wrapQuotes(adjustment.getAdjustmentType().getType().toString()));
                line.setAdjustmentName(wrapQuotes(adjustment.getAdjustmentType().getName()));
                Integer adjustmentValue = paymentsAndDiscounts.get(adjustment);
                int negate = session.getSessionType() == SessionType.REFUND ? -1 : 1;
                if(adjustment.getAdjustmentType().getType() == AdjustmentTypeType.PAYMENT) {
                    int changeDue = shareOfChange.getOrDefault(adjustment.getId(), 0);
                    line.setAdjustmentValue(String.format("%.2f", negate * MoneyService.toMoneyRoundNearest(adjustmentValue-changeDue)));
                } else if(adjustment.getAdjustmentType().getType() == AdjustmentTypeType.DISCOUNT || adjustment.getAdjustmentType().getType() == AdjustmentTypeType.GRATUITY){
                    line.setAdjustmentValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(adjustmentValue)));
                }
                line.setCurrency(reportingConstraints.getCurrency());
                if(reportingConstraints.getIntegrations().contains(ExternalIntegration.MEWS)) {
                    Object firstName = adjustment.getSpecialAdjustmentData().get(MewsConstants.FIRST_NAME);
                    Object lastName = adjustment.getSpecialAdjustmentData().get(MewsConstants.LAST_NAME);
                    Object room = adjustment.getSpecialAdjustmentData().get(MewsConstants.ROOM_NO);
                    Object chargeId = adjustment.getSpecialAdjustmentData().get(MewsConstants.CHARGE_ID);
                    line.setMewsName((firstName == null ? "" : firstName.toString()) + " " + (lastName == null ? "" : lastName.toString()));
                    line.setMewsRoomNumber(room == null ? "" : room.toString());
                    line.setMewsChargeId(chargeId == null ? "" : chargeId.toString());
                } else {
                    line.setMewsName("");
                    line.setMewsRoomNumber("");
                    line.setMewsChargeId("");
                }

                lines.add(line);
            }
        }
        return lines;
    }

    public List<ItemDetailsReportLine> getItemDetailsReportLines(ReportingConstraints reportingConstraints) {
        String restaurantId = reportingConstraints.getRestaurantId();
        long startLong = reportingConstraints.getStart();
        long endLong = reportingConstraints.getEnd();
        Map<String,Staff> staffMap = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));

        ZoneId restaurantZoneId = reportingConstraints.getZoneId();
        ZoneId utc = ZoneId.of("UTC");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Tuple<List<Session>, List<Order>> tuple = getCleanedSessionsAndOrders(restaurantId, startLong, endLong);
        Map<String,List<Order>> ordersBySession = tuple.getB().stream().collect(Collectors.groupingBy(Order::getSessionId));
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        List<ItemDetailsReportLine> lines = new ArrayList<>();
        List<Session> allSessions = tuple.getA();
        allSessions.removeIf(Session::isLinked);
        allSessions.sort(Comparator.comparingLong(Session::getClosedTime));
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        for(Session session : allSessions) {
            Map<String, String> dinerIdToName = new HashMap<>();
            if(session.getDiners() != null) {
                for(Diner diner : session.getDiners()) {
                    dinerIdToName.put(diner.getId(), getDinerName(diner));
                }
            }
            List<Order> orders = ordersBySession.get(session.getId());
            if(orders == null || orders.size() == 0) {
                continue;
            }

            String tableNames = "";
            if(session.getSessionType() == SessionType.SEATED) {
                tableNames = SessionService.getTableName(restaurant,session).replace(",","|");
            }

            int negate = session.getSessionType() == SessionType.REFUND ? -1 : 1;
            for(Order order : orders) {
                ItemDetailsReportLine line = new ItemDetailsReportLine();
                line.setOrderDateTime(dateTimeFormat.format(getZonedDateTime(restaurantZoneId, utc, order.getTime())));
                line.setDate(dateTimeFormat.format(getZonedDateTime(restaurantZoneId, utc, session.getStartTime())));
                line.setSessionId(session.getReadableId());
                line.setSessionType(session.getSessionType().toString());
                line.setTableNumbers(tableNames);
                line.setStaffId(order.getStaffId());
                if(order.getStaffId() != null && staffMap.containsKey(order.getStaffId())) {
                    line.setStaffId(order.getStaffId());
                    line.setStaffName(wrapQuotes(staffMap.get(order.getStaffId()).getName()));
                } else {
                    line.setStaffId("");
                    line.setStaffName("");
                }
                if(order.getInstantiatedFrom() != null) {
                    line.setOrigin(order.getInstantiatedFrom().name());
                } else {
                    line.setOrigin(ActivityInstantiationConstant.UNKNOWN.name());
                }
                if(StringUtils.isNotBlank(order.getDinerId()) && dinerIdToName.containsKey(order.getDinerId())) {
                    line.setGuestName(dinerIdToName.get(order.getDinerId()));
                } else {
                    line.setGuestName("");
                }
                line.setMenuItemId(order.getMenuItemId());
                line.setSku(StringUtils.isNotBlank(order.getMenuItem().getPlu()) ? order.getMenuItem().getPlu() : "");
                line.setMenuItemName(wrapQuotes(order.getMenuItem().getName()));
                line.setSalesPrice(String.format("%.2f", negate * MoneyService.toMoneyRoundNearest(SessionCalculationService.getOrderValue(order))));
                line.setQuantity(Integer.toString(negate * order.getQuantity()));
                if(order.getAdjustment() != null) {
                    line.setVoidReason(order.getAdjustment().getAdjustmentType().getName());
                } else {
                    line.setVoidReason("");
                }
                line.setItemType(order.getMenuItem().getType().toString());
                line.setTaxName(taxRateMap.get(order.getMenuItem().getTaxTypeId()).getName());
                line.setTaxRate(String.format("%.2f",taxRateMap.get(order.getMenuItem().getTaxTypeId()).getRate()/10D) + "%");

                lines.add(line);
            }

        }
        return lines;
    }

    public List<ModifierDetailsReportLine> getModifierDetailsReportLines(ReportingConstraints reportingConstraints) {
        String restaurantId = reportingConstraints.getRestaurantId();
        long startLong = reportingConstraints.getStart();
        long endLong = reportingConstraints.getEnd();
        Map<String,Staff> staffMap = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));

        ZoneId restaurantZoneId = reportingConstraints.getZoneId();
        ZoneId utc = ZoneId.of("UTC");
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Tuple<List<Session>, List<Order>> tuple = getCleanedSessionsAndOrders(restaurantId, startLong, endLong);
        Map<String,List<Order>> ordersBySession = tuple.getB().stream().collect(Collectors.groupingBy(Order::getSessionId));
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));
        List<ModifierGroup> modifierGroups = masterDataService.getModifierGroupsByRestaurant(restaurantId);
        Map<String,ModifierGroup> modifierIdToGroup = new HashMap<>();
        for(ModifierGroup modifierGroup : modifierGroups) {
            for(Modifier modifier : modifierGroup.getModifiers()) {
                modifierIdToGroup.put(modifier.getId(), modifierGroup);
            }
        }


        List<ModifierDetailsReportLine> lines = new ArrayList<>();
        List<Session> allSessions = tuple.getA();
        allSessions.removeIf(Session::isLinked);
        allSessions.sort(Comparator.comparingLong(Session::getClosedTime));
        for(Session session : allSessions) {
            Map<String, String> dinerIdToName = new HashMap<>();
            if(session.getDiners() != null) {
                for(Diner diner : session.getDiners()) {
                    dinerIdToName.put(diner.getId(), getDinerName(diner));
                }
            }
            List<Order> orders = ordersBySession.get(session.getId());
            if(orders == null || orders.size() == 0) {
                continue;
            }

            for(Order order : orders) {
                for(Modifier modifier : order.getModifiers()) {
                    ModifierDetailsReportLine line = new ModifierDetailsReportLine();
                    line.setModifierName(modifier.getModifierValue());
                    line.setValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(modifier.getPrice())));
                    if(taxRateMap.get(modifier.getTaxTypeId()) == null) {
                        line.setTaxName("");
                        line.setTaxRate("");
                    } else {
                        line.setTaxName(taxRateMap.get(modifier.getTaxTypeId()).getName());
                        line.setTaxRate(String.format("%.2f",taxRateMap.get(order.getMenuItem().getTaxTypeId()).getRate()/10D) + "%");
                    }
                    line.setModifierGroup(modifierIdToGroup.get(modifier.getId()).getName());
                    line.setMenuItemName(order.getMenuItem().getName());
                    line.setMenuItemId(order.getMenuItemId());
                    line.setSessionDateTime(dateTimeFormat.format(getZonedDateTime(restaurantZoneId, utc, order.getTime())));
                    line.setSessionId(session.getId());
                    line.setSessionType(session.getSessionType().toString());
                    if(staffMap.get(order.getStaffId()) == null) {
                        line.setStaffName("");
                    } else {
                        line.setStaffName(staffMap.get(order.getStaffId()).getName());
                    }
                    line.setStaffId(order.getStaffId());
                    line.setOrigin(order.getInstantiatedFrom() == null ? ActivityInstantiationConstant.UNKNOWN.name() : order.getInstantiatedFrom().toString());
                    line.setGuestName(dinerIdToName.get(order.getDinerId()));
                    line.setOrderId(order.getId());

                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private String getDinerName(Diner diner) {
        if(diner.isDefaultDiner()) {
            return "TABLE";
        } else if (diner.getName() == null){
            return "";
        } else {
            return diner.getName();
        }
    }

    private ZonedDateTime getZonedDateTime(ZoneId restaurantZoneId, ZoneId utc, long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZonedDateTime utcDT = ZonedDateTime.ofInstant(instant, utc);
        return utcDT.withZoneSameInstant(restaurantZoneId);
    }

    public List<RevenueReportLine> getRevenueReportLines(ReportingConstraints reportingConstraints) {
        String restaurantId = reportingConstraints.getRestaurantId();
        long startLong = reportingConstraints.getStart();
        long endLong = reportingConstraints.getEnd();

        ZoneId restaurantZoneId = reportingConstraints.getZoneId();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Tuple<List<Session>, List<Order>> tuple = getCleanedSessionsAndOrders(restaurantId, startLong, endLong);
        Map<String,List<Order>> ordersBySession = tuple.getB().stream().collect(Collectors.groupingBy(Order::getSessionId));
        Map<String, Staff> staffMap = masterDataService.getAllStaff(restaurantId).stream().collect(Collectors.toMap(Staff::getId, Function.identity()));

        List<RevenueReportLine> lines = new ArrayList<>();
        List<Session> allSessions = tuple.getA();
        allSessions.sort(Comparator.comparingLong(Session::getClosedTime));
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        for(Session session : allSessions) {
            if(session.getClosedTime() == null) {
                continue;
            }

            List<Order> orders = ordersBySession.get(session.getId());
            if(orders == null) {
                orders = new ArrayList<>();
            }

            String tableNames = "";
            if(session.getSessionType() == SessionType.SEATED) {
                tableNames = SessionService.getTableName(restaurant, session).replace(",","|");
            }

            RevenueReportLine line = new RevenueReportLine();
            Map<CalculationKey,Number> calculations = sessionCalculationService.calculateValues(session, orders);

            ZonedDateTime restaurantTime1 = TimeUtil.getRestaurantTime(session.getStartTime(), restaurantZoneId);
            line.setStartTime(dateTimeFormat.format(restaurantTime1));
            ZonedDateTime restaurantTime2 = TimeUtil.getRestaurantTime(session.getClosedTime(), restaurantZoneId);
            line.setEndTime(dateTimeFormat.format(restaurantTime2));
            line.setSessionId(session.getReadableId());
            line.setNumberOfCovers(String.valueOf(session.getNumberOfRealDiners()));
            line.setCurrencyCode(reportingConstraints.getCurrency());
            line.setSessionType(session.getSessionType().toString());
            line.setTableNumbers(tableNames);
            int negate = session.getSessionType() == SessionType.REFUND ? -1 : 1;
            int subTotal = calculations.get(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS).intValue();
            int total = calculations.get(CalculationKey.TOTAL).intValue();
            line.setSubTotal(String.format("%.2f", negate * MoneyService.toMoneyRoundNearest(subTotal)));
            line.setTotal(String.format("%.2f", negate * MoneyService.toMoneyRoundNearest(total)));
            line.setVatTotal(String.format("%.2f", negate * MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.VAT_TOTAL).intValue())));
            line.setTips(String.format("%.2f",negate * MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.TIP_TOTAL).intValue())));
            line.setDiscounts(String.format("%.2f",negate * MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.DISCOUNT_TOTAL).intValue())));
            int totalPayments = calculations.get(CalculationKey.TOTAL_PAYMENTS).intValue();
            int paymentsExcludingChange = totalPayments - calculations.get(CalculationKey.CHANGE_DUE).intValue();
            if(paymentsExcludingChange < 0) {
                paymentsExcludingChange = 0;
            }
            if(session.getSessionType() == SessionType.TAKEAWAY) {
                line.setDeliveryCost(String.format("%.2f",negate * MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.DELIVERY_TOTAL).intValue())));
            } else {
                line.setDeliveryCost("");
            }
            line.setPayments(String.format("%.2f",negate * MoneyService.toMoneyRoundNearest(paymentsExcludingChange)));
            line.setVoidedPayments(String.format("%.2f", MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.TOTAL_PAYMENTS_VOIDED).intValue())));
            line.setOverpayment(String.format("%.2f",MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.OVER_PAYMENTS).intValue())));
            boolean paid = SessionCalculationService.isPaid(calculations);
            line.setVoided(!paid || session.getVoidReason() != null ? "Y" : "N");
            if(!paid) {
                if(session.getVoidReason() != null) {
                    line.setVoidReason(session.getVoidReason().getDescription());
                } else {
                    line.setVoidReason("FORCE CLOSED");
                }
            } else if(session.getVoidReason() != null){
                line.setVoidReason(session.getVoidReason().getDescription());
            } else {
                line.setVoidReason("");
            }

            if(session.getClosedBy() != null && staffMap.containsKey(session.getClosedBy())) {
                line.setClosedBy(staffMap.get(session.getClosedBy()).getUserName());
                line.setClosedById(session.getClosedBy());
            } else {
                line.setClosedBy("");
                line.setClosedById("");
            }

            if(session.isLinked()) {
                List<String> linkedSession = session.getLinkedSession();
                line.setDeferredSessions(StringUtils.join(linkedSession, "|"));
            }

            lines.add(line);
        }
        return lines;
    }

    public List<ReservationLine> getReservationLines(ReportingConstraints reportingConstraints) {
        BookingType bookingType = BookingType.RESERVATION;
        List<Booking> bookings = getBookings(reportingConstraints, bookingType);

        Map<String, String> staffById = masterDataService.getAllStaff(reportingConstraints.getRestaurantId()).stream().collect(Collectors.toMap(Staff::getId, Staff::getName));
        List<ReservationLine> reservationLines = new ArrayList<>();
        DateTimeFormatter dateTimeFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateTimeFormat2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for(Booking booking : bookings) {
            ReservationLine line = new ReservationLine();
            setCommonBookingElements(reportingConstraints, dateTimeFormat1, dateTimeFormat2, booking, line);
            setBookingState(booking, line);
            setBookingOrigin(staffById, booking, line);

            reservationLines.add(line);
        }
        return reservationLines;
    }

    public List<TakeawayLine> getTakeawayLines(ReportingConstraints reportingConstraints) {
        BookingType bookingType = BookingType.TAKEAWAY;
        List<Booking> bookings = getBookings(reportingConstraints, bookingType);

        Map<String, String> staffById = masterDataService.getAllStaff(reportingConstraints.getRestaurantId()).stream().collect(Collectors.toMap(Staff::getId, Staff::getName));
        Map<String, Session> sessionsByBookingId = sessionService.getSessionsByBookingIds(bookings.stream().map(Booking::getId).collect(Collectors.toList())).stream().collect(Collectors.toMap(Session::getOriginalBookingId, Function.identity()));
        List<TakeawayLine> reservationLines = new ArrayList<>();
        DateTimeFormatter dateTimeFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateTimeFormat2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for(Booking booking : bookings) {
            TakeawayLine line = new TakeawayLine();
            setCommonBookingElements(reportingConstraints, dateTimeFormat1, dateTimeFormat2, booking, line);
            setBookingState(booking, line);
            setBookingOrigin(staffById, booking, line);

            line.setTakeawayType(booking.getTakeawayType() == null ? TakeawayType.NONE.name() : booking.getTakeawayType().name());
            if(booking.getTakeawayType() == TakeawayType.DELIVERY && booking.getDeliveryAddress() != null) {
                line.setAddress("\""+booking.getDeliveryAddress().prettyToString()+"\"");
            } else {
                line.setAddress("");
            }

            Session session = sessionsByBookingId.get(booking.getId());
            if(session != null) {
                line.setSessionId(session.getReadableId());
                Map<CalculationKey, Number> calculations = sessionCalculationService.calculateValues(session);
                line.setTotal(String.format("%.2f", MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.TOTAL).intValue())));
                line.setPaymentStatus(SessionCalculationService.isPaid(calculations) ? "PAID" : "NOT PAID");
                line.setVoided(session.getVoidReason() != null ? "Y" : "N");
            }

            reservationLines.add(line);
        }
        return reservationLines;
    }

    private void setBookingOrigin(Map<String, String> staffById, Booking booking, IBookingLine line) {
        String origin;
        if(booking.getInstantiatedFrom() == null) {
            origin = "UNKNOWN";
        } else {
            if (booking.getInstantiatedFrom() == ActivityInstantiationConstant.WAITER) {
                origin = "EPICURI";
                line.setBookedByStaff(staffById.getOrDefault(booking.getStaffId(), "Unavailable"));
            } else if (booking.getInstantiatedFrom() == ActivityInstantiationConstant.IOS
                    || booking.getInstantiatedFrom() == ActivityInstantiationConstant.ANDROID) {
                origin = "GUEST APP";
                line.setBookedByStaff("");
            } else if (booking.getInstantiatedFrom() == ActivityInstantiationConstant.BOOKING_WIDGET) {
                origin = "ONLINE";
                line.setBookedByStaff("");
            } else {
                origin = "UNKNOWN";
                line.setBookedByStaff("");
            }
        }
        line.setOrigin(origin);
    }

    private void setBookingState(Booking booking, IBookingLine line) {
        String bookingState = "ACCEPTED";
        if(!booking.isAccepted() && booking.isRejected()) {
            bookingState = "REJECTED";
        }
        if(booking.isCancelled()) { //NOT ELSE!
            bookingState = "CANCELLED";
        }
        line.setBookingState(bookingState);
    }

    private void setCommonBookingElements(ReportingConstraints reportingConstraints, DateTimeFormatter dateTimeFormat1, DateTimeFormatter dateTimeFormat2, Booking booking, IBookingLine line) {
        ZonedDateTime restaurantTime1 = TimeUtil.getRestaurantTime(booking.getTargetTime(), reportingConstraints.getZoneId());
        line.setTime(dateTimeFormat1.format(restaurantTime1));
        line.setName(booking.getName());
        line.setCovers(String.valueOf(booking.getNumberOfPeople()));
        line.setTelephone(StringUtils.isBlank(booking.getTelephone()) ? "" : booking.getTelephone());
        line.setEmail(StringUtils.isBlank(booking.getEmail()) ? "" : booking.getEmail());
        line.setNotes(StringUtils.isBlank(booking.getNotes()) ? "" : booking.getNotes());
        ZonedDateTime restaurantTime2 = TimeUtil.getRestaurantTime(booking.getCreatedTime(), reportingConstraints.getZoneId());
        line.setCreationTime(dateTimeFormat2.format(restaurantTime2));
    }

    private List<Booking> getBookings(ReportingConstraints reportingConstraints, BookingType bookingType) {
        List<Booking> bookings = bookingService.getBookings(reportingConstraints.getRestaurantId(), reportingConstraints.getStart(), reportingConstraints.getEnd(), false);
        bookings.removeIf(b -> b.getBookingType() != bookingType);
        bookings.sort(Comparator.comparingLong(Booking::getTargetTime));
        return bookings;
    }

    public List<CashUpReportLine> getCashupLines(ReportingConstraints reportingConstraints) {
        List<CashUp> cashUps = archiveDataService.getCashUpsBetween(reportingConstraints.getRestaurantId(), reportingConstraints.getStart(), reportingConstraints.getEnd());
        cashUps.sort((a,b) -> Long.compare(b.getEndTime(), a.getEndTime()));
        List<CashUpReportLine> lines = new ArrayList<>();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for(CashUp cashUp : cashUps) {
            CashUpReportLine line = new CashUpReportLine();
            ZonedDateTime startTime = TimeUtil.getRestaurantTime(cashUp.getStartTime(), reportingConstraints.getZoneId());
            ZonedDateTime endTime = TimeUtil.getRestaurantTime(cashUp.getEndTime(), reportingConstraints.getZoneId());
            line.setStartDate(dateTimeFormat.format(startTime));
            line.setEndDate(dateTimeFormat.format(endTime));
            line.setOnPremiseCount(String.valueOf(cashUp.getReport().get(CashUpKeys.SEATED_SESSIONS_COUNT)));
            line.setOnPremiseValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.SEATED_SESSIONS_VALUE))));
            line.setTakeawaysCount(String.valueOf(cashUp.getReport().get(CashUpKeys.TAKEAWAY_SESSIONS_COUNT)));
            line.setTakeawaysValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.TAKEAWAY_SESSIONS_VALUE))));
            line.setUnpaidOnPremiseCount(String.valueOf(cashUp.getReport().get(CashUpKeys.VOID_SEATED_SESSION_COUNT)));
            line.setUnpaidOnPremiseValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.VOID_SEATED_SESSION_VALUE))));
            line.setUnpaidTakeawayCount(String.valueOf(cashUp.getReport().get(CashUpKeys.VOID_TAKEAWAY_SESSION_COUNT)));
            line.setUnpaidTakeawayValue(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.VOID_TAKEAWAY_SESSION_VALUE))));
            line.setTotalUnpaid(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.VOID_VALUE))));
            line.setFoodCount(String.valueOf(cashUp.getReport().get(CashUpKeys.FOOD_COUNT)));
            line.setGrossFoodAmount(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.FOOD_VALUE))));
            line.setDrinkCount(String.valueOf(cashUp.getReport().get(CashUpKeys.DRINK_COUNT)));
            line.setGrossDrinkAmount(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.DRINK_VALUE))));
            line.setOtherCount(String.valueOf(cashUp.getReport().get(CashUpKeys.OTHER_COUNT)));
            line.setGrossOtherAmount(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.OTHER_VALUE))));
            line.setDeliveryCharges(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.TOTAL_DELIVERY))));
            Integer totalSales = cashUp.getReport().get(CashUpKeys.TOTAL_SALES);
            line.setTotalSales(String.format("%.2f", MoneyService.toMoneyRoundNearest(totalSales)));
            Integer totalAdjustments = cashUp.getReport().get(CashUpKeys.TOTAL_ADJUSTMENTS);
            line.setTotalAdjustments(String.format("%.2f", MoneyService.toMoneyRoundNearest(totalAdjustments)));
            line.setTotalSalesAfterAdjustments(String.format("%.2f", MoneyService.toMoneyRoundNearest(totalSales-totalAdjustments)));
            Integer vatValue = cashUp.getReport().get(CashUpKeys.VAT_VALUE);
            line.setTotalVATCharged(String.format("%.2f", MoneyService.toMoneyRoundNearest(vatValue)));
            line.setNetSales(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.NET_VALUE))));
            line.setOverpayments(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.OVER_PAYMENTS))));
            line.setTips(String.format("%.2f",MoneyService.toMoneyRoundNearest(cashUp.getReport().getOrDefault(CashUpKeys.TOTAL_TIP, 0))));
            line.setTotalPayments(String.format("%.2f", MoneyService.toMoneyRoundNearest(cashUp.getReport().get(CashUpKeys.PAYMENTS))));
            line.setId(cashUp.getId());

            updateAdjustments(line, "paymentTypes", "PAYMENT: ", cashUp.getPaymentReport());
            updateAdjustments(line, "adjustmentTypes", "DISCOUNT: ", cashUp.getAdjustmentReport());
            updateAdjustments(line, "refunds", "REFUND: ", cashUp.getRefundPaymentReport());

            lines.add(line);
        }

        return lines;
    }

    public <T extends ReportLine> CSVWrapper createCsvWrapper(List<T> lines, Class<T> clazz, String name) {
        try (
                Writer writer = new StringWriter();

                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            FieldDescriptor descriptor = new FieldDescriptor(clazz);

            String[] headerRecord = descriptor.initHeaderRecord(lines);
            csvWriter.writeNext(headerRecord);

            int length = headerRecord.length;
            for(ReportLine reportLine : lines) {
                String[] values = new String[length];
                for(int i = 0; i < length; i++) {
                    try {
                        Field field = descriptor.getFieldFor(i);
                        if(field == null) {
                            values[i] = "";
                            continue;
                        }

                        if(field.getDeclaredAnnotation(DynamicColumn.class) != null) {
                            Map<String, String> columnToValues = reportLine.getColumnToValues();
                            values[i] = columnToValues.getOrDefault(descriptor.getColumnNameForIndex(i), "");
                        } else {
                            field.setAccessible(true);
                            Object object = field.get(reportLine);
                            if (object != null) {
                                values[i] = object.toString();
                            } else {
                                values[i] = "";
                            }
                        }
                    } catch (IllegalAccessException e) {
                        values[i] = "";
                    }
                }
                csvWriter.writeNext(values);
            }

            CSVWrapper csvWrapper = new CSVWrapper();
            csvWrapper.setContent(writer.toString());
            csvWrapper.setFileName(name);

            return csvWrapper;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new CSVWrapper();
    }

    private void updateAdjustments(CashUpReportLine line, String fieldName, String prefix, Map<String, Integer> report) {
        for(Map.Entry<String,Integer> entry : report.entrySet()) {
            try {
                line.updateDynamicField(fieldName, prefix + entry.getKey(), String.format("%.2f", MoneyService.toMoneyRoundNearest(entry.getValue())));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public PACReport getPaymentSensePACReport(String restaurantId, String requestId) {
        List<PaymentSenseReport> paymentSenseReports = paymentSenseRestService.getPaymentSenseReports(restaurantId);
        for(PaymentSenseReport paymentSenseReport : paymentSenseReports) {
            for(PACReport report : paymentSenseReport.getPACReports()) {
                if(report.getRequestId().equals(requestId)) {
                    return report;
                }
            }
        }

        return null;
    }

    private String wrapQuotes(String s) {
        if(s.contains(",")) {
            return "\"" + s + "\"";
        } else {
            return s;
        }
    }

    private String getAggregatedMenuItemName(List<Order> orders) {
        Set<String> names = new HashSet<>();
        for(Order order : orders) {
            names.add(order.getMenuItem().getName());
        }
        if(names.size() == 0) {
            return "";
        } else if(names.size() == 1) {
            return names.iterator().next();
        } else {
            return "MIXED";
        }
    }

    private String getAggregatedPrice(List<Order> orders) {
        Integer price = null;
        for(Order order : orders) {
            if(price == null) {
                price = order.getPriceOverride();
            }
            if(price != order.getPriceOverride()) {
                return "MIXED";
            }
        }
        if(price == null) {
            return "";
        } else {
            return String.format("%.2f", MoneyService.toMoneyRoundNearest(price));
        }
    }

    private String getAggregatedType(List<Order> orders) {
        ItemType type = null;
        for(Order order : orders) {
            if(type == null) {
                type = order.getMenuItem().getType();
            }
            if(type != order.getMenuItem().getType()) {
                return "MIXED";
            }
        }
        if(type == null) {
            return "";
        } else {
            return type.toString();
        }
    }

    private String getAggregatedTaxName(List<Order> orders) {
        Set<String> names = new HashSet<>();
        for(Order order : orders) {
            names.add(order.getTaxRate().getName());
        }
        if(names.size() == 0) {
            return "";
        } else if(names.size() == 1) {
            return names.iterator().next();
        } else {
            return "MIXED";
        }
    }

    private String getAggregatedTaxRate(List<Order> orders) {
        Integer rate = null;
        for(Order order : orders) {
            if(rate == null) {
                rate = order.getTaxRate().getRate();
            }
            if(rate != order.getTaxRate().getRate()) {
                return "MIXED";
            }
        }
        if(rate == null) {
            return "";
        } else {
            return String.format("%.2f",rate/10D) + "%";
        }
    }
}
