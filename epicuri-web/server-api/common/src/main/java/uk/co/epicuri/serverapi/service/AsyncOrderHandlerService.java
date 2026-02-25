package uk.co.epicuri.serverapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.engines.CashUpAggregator;
import uk.co.epicuri.serverapi.service.external.MarketManService;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by manish on 31/07/2017.
 */
@Service
public class AsyncOrderHandlerService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncOrderHandlerService.class);

    @Value("${epicuri.paymentsense.pull.enabled}")
    private boolean paymentSensePullEnabled;

    @Autowired
    private PaymentSenseRestService paymentSenseRestService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private MarketManService marketManService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public CompletableFuture<Void> onOrders(String staffId, String restaurantId, String sessionId, List<Order> orders, List<Order> allOrders) {
        LOGGER.trace("Process {} orders for {}", orders.size(), restaurantId);
        try {
            return onOrders(staffId, masterDataService.getRestaurant(restaurantId), sessionService.getSession(sessionId), orders, allOrders);
        } catch (Exception ex) {
            LOGGER.error("Exception in processing orders", ex);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async
    public CompletableFuture<Void> onOrders(String staffId, Restaurant restaurant, Session session, List<Order> orders, List<Order> allOrders) {
        if(orders == null || orders.size() == 0) {
            return CompletableFuture.completedFuture(null);
        }

        if(restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            boolean allowPayAtTable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.PS_ALLOW_PAY_AT_TABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.PS_ALLOW_PAY_AT_TABLE, true)).getValue();
            if(allowPayAtTable && session.getSessionType() == SessionType.SEATED) {
                onOrderPaymentSense(staffId, restaurant, session, orders, allOrders);
            }
        }
        boolean stockControlEnabled = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.ENABLE_STOCK_COUNTDOWN)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, false)).getValue();
        if(stockControlEnabled) {
            boolean autoUnavailable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.AUTO_STOCK_UNAVAILABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true)).getValue();
            liveDataService.updateStockControl(restaurant, allOrders, false, autoUnavailable);
        }

        //add others

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> onOrderRemoved(String staffId, String restaurantId, Order order, List<Order> allOrders) {
        return onOrderRemoved(staffId, masterDataService.getRestaurant(restaurantId), sessionService.getSession(order.getSessionId()), order, allOrders);
    }

    @Async
    public CompletableFuture<Void> onOrderRemoved(String staffId, Restaurant restaurant, Session session, Order order, List<Order> allOrders) {
        if(order == null || !(order.getAdjustment() != null || order.isVoided())) {
            CompletableFuture.completedFuture(null);
        }

        if(restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            boolean allowPayAtTable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.PS_ALLOW_PAY_AT_TABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.PS_ALLOW_PAY_AT_TABLE, true)).getValue();
            if(allowPayAtTable && session.getSessionType() == SessionType.SEATED) {
                onOrderRemovedPaymentSense(staffId, restaurant, session, order, allOrders);
            }
        }
        /*boolean stockControlEnabled = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.ENABLE_STOCK_COUNTDOWN)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, false)).getValue();
        if(stockControlEnabled) {
            boolean autoUnavailable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.AUTO_STOCK_UNAVAILABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true)).getValue();
            liveDataService.updateStockControl(restaurant, Collections.singletonList(order), true, autoUnavailable);
        }*/

        //add others

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> onAllOrdersRemoved(String staffId, String restaurantId, String sessionId, List<Order> orders) {
        return onAllOrdersRemoved(staffId,masterDataService.getRestaurant(restaurantId), sessionService.getSession(sessionId), orders);
    }

    @Async
    public CompletableFuture<Void> onAllOrdersRemoved(String staffId, Restaurant restaurant, Session session, List<Order> orders) {
        boolean stockControlEnabled = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.ENABLE_STOCK_COUNTDOWN)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, false)).getValue();
        if(stockControlEnabled) {
            boolean autoUnavailable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.AUTO_STOCK_UNAVAILABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true)).getValue();
            liveDataService.updateStockControl(restaurant, orders, true, autoUnavailable);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> onSessionClose(String staffId, Restaurant restaurant, Session session, boolean forceClosed) {
        if(restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            boolean allowPayAtTable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.PS_ALLOW_PAY_AT_TABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.PS_ALLOW_PAY_AT_TABLE, true)).getValue();
            if(allowPayAtTable && session.getSessionType() == SessionType.SEATED) {
                tabledClosePaymentSense(staffId, restaurant, session);
            }
        }

        if(forceClosed && session.getSessionType() == SessionType.ADHOC) {
            boolean stockControlEnabled = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.ENABLE_STOCK_COUNTDOWN)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, false)).getValue();
            if(stockControlEnabled) {
                boolean autoUnavailable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.AUTO_STOCK_UNAVAILABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true)).getValue();
                liveDataService.updateStockControl(restaurant, liveDataService.getOrders(session.getId()), true, autoUnavailable);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> onCashUp(Restaurant restaurant, CashUp cashUp, CashUpAggregator cashUpAggregator) {
        if(restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE)) {
            cashUpPaymentSense(restaurant, cashUp.getEndTime());
        }

        if(restaurant.getIntegrations().containsKey(ExternalIntegration.MARKET_MAN)) {
            cashupMarketMan(restaurant, cashUp, cashUpAggregator);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> onReconciliationRequest(String staffId, String restaurantId, String sessionId) {
        return onReconciliationRequest(staffId, masterDataService.getRestaurant(restaurantId), sessionService.getSession(sessionId));
    }

    @Async
    public CompletableFuture<Void> onReconciliationRequest(String staffId, Restaurant restaurant, Session session) {
        if(restaurant.getIntegrations().containsKey(ExternalIntegration.PAYMENT_SENSE) && paymentSensePullEnabled) {
            boolean allowPayAtTable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.PS_ALLOW_PAY_AT_TABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.PS_ALLOW_PAY_AT_TABLE, true)).getValue();
            if(allowPayAtTable) {
                reconcilePaymentSense(staffId, restaurant, session);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    private void cashUpPaymentSense(Restaurant restaurant, long endTime) {
        paymentSenseRestService.cleanAllTables(restaurant, endTime);
        try {
            /*List<PACReport> reports = paymentSenseRestService.getPACReport("1", restaurant);
            if(reports != null && reports != null && reports.size() > 0) {
                archiveDataService.pushReport(reports, restaurant.getId());
            }*/
        } catch (Exception ex) {
            LOGGER.warn("Error occurred whilst getting PS reports: {}",ex.getMessage());
        }
    }

    private void tabledClosePaymentSense(String staffId, Restaurant restaurant, Session session) {
        if (!checkApplicableToHospitalityPS(session)) return;

        onReconciliationRequest(staffId,restaurant,session);
        session = sessionService.getSession(session.getId());

        String tableName = SessionService.getTableName(restaurant, session);
        int total = 0;
        int paid = 0;
        //calculate total
        TableResponse tableResponse = paymentSenseRestService.getTable(restaurant, tableName);
        if(!tableName.equals(tableResponse.getTableName())) {
            //calculate manually
            Map<CalculationKey,Number> calculatedValues = sessionCalculationService.calculateValues(session);
            if(calculatedValues.get(CalculationKey.TOTAL) == null || calculatedValues.get(CalculationKey.TOTAL).intValue() == 0) {
                return;
            }
            total = calculatedValues.get(CalculationKey.TOTAL).intValue();
            //paid amounts
            paid = getAmountPaid(session);
        } else {
            total = tableResponse.getAmount();
            paid = tableResponse.getAmountPaid();
        }

        paymentSenseRestService.deleteTable("1", restaurant, tableName, total, paid);
    }

    public void onOrderRemovedPaymentSense(String staffId, Restaurant restaurant, Session session, Order order, List<Order> allOrders) {
        if (!checkApplicableToHospitalityPS(session)) return;

        //calculate total
        Map<CalculationKey,Number> calculatedValues = sessionCalculationService.calculateValues(session);
        if(calculatedValues.get(CalculationKey.TOTAL) == null) {
            return;
        }

        int total = calculatedValues.get(CalculationKey.TOTAL).intValue();
        int lastKnownAmount = total+order.getAdjustment().getValue(); //because it's already been applied
        //paid amounts
        int paid = getAmountPaid(session);
        String tableName = SessionService.getTableName(restaurant, session);
        if(!paymentSenseRestService.updateAmount("1",restaurant,tableName,lastKnownAmount,paid,total)) {
            LOGGER.warn("Could not post update for order: {}", order.getId());
            return;
        }
        paymentSenseRestService.updateReceipt("1", restaurant, tableName, lastKnownAmount, paid, allOrders);
    }

    public void onOrderPaymentSense(String staffId, Restaurant restaurant, Session session, List<Order> orders, List<Order> allOrders) {
        if (!checkApplicableToHospitalityPS(session)) return;

        LOGGER.trace("Process {} orders for session {}", orders.size(), session.getId());

        //calculate total
        Map<CalculationKey,Number> calculatedValues = sessionCalculationService.calculateValues(session);
        if(calculatedValues.get(CalculationKey.TOTAL) == null || calculatedValues.get(CalculationKey.TOTAL).intValue() == 0) {
            LOGGER.trace("Null or 0 value for session, won't call PaymentSense");
            return;
        }

        int total = calculatedValues.get(CalculationKey.TOTAL).intValue();
        String tableName = SessionService.getTableName(restaurant, session);
        LOGGER.trace("Total {} for session {} on table {}", total, session.getId(), tableName);

        //create a table, even if it doesn't exist (no harm)
        boolean created = paymentSenseRestService.postTable("1", restaurant, tableName, total);
        if(!created) {
            //total just from these orders
            Map<CalculationKey,Number> newValues = sessionCalculationService.calculateValues(session,orders);
            int subOrderTotal = newValues.get(CalculationKey.TOTAL).intValue();
            int lastKnownAmount = total-subOrderTotal;

            //paid amounts
            int paid = getAmountPaid(session);

            //update receipt
            paymentSenseRestService.updateReceipt("1", restaurant, tableName, lastKnownAmount, paid, allOrders);

            //post an edit
            boolean edited = paymentSenseRestService.updateAmount("1", restaurant, tableName, lastKnownAmount, paid, total);
            if(!edited) {
                LOGGER.warn("Could not update amount to payment sense; session id={}, restaurant=", session.getId(), restaurant.getId());
            }
        } else {
            int lastKnownPaidAmount = calculatedValues.get(CalculationKey.TOTAL_PAYMENTS).intValue();
            //update receipt
            paymentSenseRestService.updateReceipt("1", restaurant, tableName, total, lastKnownPaidAmount, allOrders);
        }
    }

    public void reconcilePaymentSense(String staffId, Restaurant restaurant, Session session) {
        if(session.getSessionType() != SessionType.SEATED) {
            return;
        }

        String tableName = SessionService.getTableName(restaurant, session);
        TableResponse tableResponse = paymentSenseRestService.getTable(restaurant, tableName);
        if(tableResponse.getPayments().size() == 0) {
            return;
        }

        List<Adjustment> adjustments = session.getAdjustments().stream()
                .filter(a -> (a.getAdjustmentType().getType() == AdjustmentTypeType.PAYMENT || a.getAdjustmentType().getType() == AdjustmentTypeType.GRATUITY)
                        && a.getSpecialAdjustmentData().containsKey(PaymentSenseConstants.PAYMENT_KEY)).collect(Collectors.toList());


        Map<String,Payment> paymentMap = tableResponse.getPayments().stream().collect(Collectors.toMap(Payment::getPaymentId, Function.identity()));
        for(Adjustment adjustment : adjustments) {
            Object paymentObject = adjustment.getSpecialAdjustmentData().get(PaymentSenseConstants.PAYMENT_KEY);
            Payment payment = objectMapper.convertValue(paymentObject, Payment.class);
            paymentMap.remove(payment.getPaymentId());
        }

        if(paymentMap.size() == 0) {
            return;
        }

        AdjustmentType adjustmentType = masterDataService.getAdjustmentTypeByName(PaymentSenseConstants.PS_ADJUSTMENT_TYPE);
        AdjustmentType otherAdjustmentType = masterDataService.getAdjustmentTypeByName(PaymentSenseConstants.PS_ADJUSTMENT_OTHER_TYPE);
        AdjustmentType gratuityType = masterDataService.getAdjustmentTypeByName(PaymentSenseConstants.PS_ADJUSTMENT_GRATUITY_TYPE);
        if(adjustmentType == null || otherAdjustmentType == null || gratuityType == null) {
            LOGGER.warn("Cannot reconcile with PaymentSense - payment={}, other={}, gratuity={}", adjustmentType, otherAdjustmentType, gratuityType);
            return;
        }

        final boolean[] added = {false};
        paymentMap.forEach((k,v) -> {
            int value = v.getAmountPaid() - v.getAmountCashback();
            if(value > 0) {
                addAdjustment(staffId, session, v.getPaymentMethod() == PaymentMethod.UNKNOWN ? otherAdjustmentType : adjustmentType, v, value);
                added[0] = true;
            }
            int gratuity = v.getAmountGratuity();
            if(gratuity > 0) {
                addAdjustment(staffId, session, gratuityType, v, gratuity);
                added[0] = true;
            }
        });

        if(added[0]) {
            sessionService.upsert(session);
        }

        paymentSenseRestService.updateReceipt(staffId, restaurant, tableName, tableResponse.getAmount(), tableResponse.getAmountPaid(), liveDataService.getOrders(session.getId()));
    }

    private void addAdjustment(String staffId, Session session, AdjustmentType adjustmentType, Payment v, int value) {
        Adjustment adjustment = new Adjustment(session.getId());
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setCreated(System.currentTimeMillis());
        adjustment.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        adjustment.setStaffId(staffId);
        adjustment.setValue(value);
        adjustment.getSpecialAdjustmentData().put(PaymentSenseConstants.PAYMENT_KEY, v);
        session.getAdjustments().add(adjustment);
    }

    private int getAmountPaid(Session session) {
        int paid = 0;
        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.isVoided() || adjustment.getAdjustmentType().getType() != AdjustmentTypeType.PAYMENT) {
                continue;
            }
            if(adjustment.getSpecialAdjustmentData().containsKey(PaymentSenseConstants.PAYMENT_KEY)) {
                paid += adjustment.getValue();
            }
        }
        return paid;
    }

    private boolean checkApplicableToHospitalityPS(Session session) {
        return session.getSessionType() == SessionType.TAB || session.getSessionType() == SessionType.SEATED;
    }

    private void cashupMarketMan(Restaurant restaurant, CashUp cashUp, CashUpAggregator cashUpAggregator) {
        KVData kvData = marketManService.updateOrAcquireToken(restaurant);
        if(kvData == null || kvData.getToken() ==  null) {
            LOGGER.warn("Cannot update MarketMan (could not acquire token): restaurant ID {}", restaurant.getId());
            return;
        }

        marketManService.updateSales(kvData.getToken(), cashUp.getId(), cashUp.getStartTime(), cashUp.getEndTime(), cashUpAggregator);
    }
}
