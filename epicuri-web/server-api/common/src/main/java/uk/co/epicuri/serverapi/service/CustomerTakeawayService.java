package uk.co.epicuri.serverapi.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationCheck;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayResponseView;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.FuseBox;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.engines.NoticeAggregator;
import uk.co.epicuri.serverapi.errors.IllegalStateResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerTakeawayService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerTakeawayService.class);

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    private final FuseBox customerTakeawayCreationFuseBox;
    private final FuseBox customerTakeawayCreditCardCheck;
    private final FuseBox deliveryFuseBox;
    private final FuseBox customerTakeawayCreationBlackoutsFuseBox;
    private final FuseBox customerTakeawayTimesFuseBox;

    public CustomerTakeawayService() {
        customerTakeawayCreationFuseBox = new FuseBox();
        customerTakeawayCreationFuseBox.add(FuseBox::checkBlackMarksAnonymous);
        customerTakeawayCreationFuseBox.add(FuseBox::checkTakeawayMinimumTime);
        customerTakeawayCreationFuseBox.add(FuseBox::checkTakeawaysBlackouts);
        customerTakeawayCreationFuseBox.add(FuseBox::checkDuplicateTakeaway);
        customerTakeawayCreationFuseBox.add(FuseBox::checkMaxTakeawaysPerHour);
        customerTakeawayCreationFuseBox.add(FuseBox::checkMaxOrder);
        customerTakeawayCreationFuseBox.add(FuseBox::checkMinOrder);
        customerTakeawayCreationFuseBox.finalise();

        customerTakeawayCreditCardCheck = new FuseBox();
        customerTakeawayCreditCardCheck.add(FuseBox::checkCCPresent);
        customerTakeawayCreditCardCheck.finalise();

        deliveryFuseBox = new FuseBox();
        deliveryFuseBox.add(FuseBox::checkAddressExistence);
        deliveryFuseBox.add(FuseBox::checkMaxDeliveryRadius);
        deliveryFuseBox.finalise();

        customerTakeawayCreationBlackoutsFuseBox = new FuseBox();
        customerTakeawayCreationBlackoutsFuseBox.add(FuseBox::checkTakeawaysBlackouts);

        customerTakeawayTimesFuseBox = new FuseBox();
        customerTakeawayTimesFuseBox.add(FuseBox::checkTakeawaysBlackouts);
        customerTakeawayTimesFuseBox.add(FuseBox::checkTakeawayMinimumTime);
        customerTakeawayTimesFuseBox.add(FuseBox::checkMaxTakeawaysPerHour);
        customerTakeawayTimesFuseBox.finalise();
    }

    public CustomerReservationCheck checkTakeaway(String customerId, CustomerTakeawayOrderRequest order) throws IllegalStateResponseException {
        String payloadCheck = checkPayload(order);
        if(payloadCheck != null) {
            throw new IllegalStateResponseException(payloadCheck, HttpStatus.NOT_ACCEPTABLE);
        }

        if (hitsBlackout(order, customerId)) {
            throw new IllegalStateResponseException(NoticeAggregator.BOOKING_IMMEDIATE_REJECT_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        return checkTakeawayRequest(order, customerId);
    }

    public String checkPayload(CustomerTakeawayOrderRequest order) {
        if(StringUtils.isBlank(order.getRestaurantId())) {
            return "Restaurant not selected";
        }

        if(order.getItems() == null) { //no need to check for size==0, just avoiding NPE here
            return "There are no orders on this request";
        }

        if(order.isDelivery() && order.getAddress() == null) {
            return "There is no address on this on this delivery";
        }

        if(StringUtils.isBlank(order.getTelephone()) || order.getTelephone().equals("null")) {
            return "Telephone number is invalid";
        }

        return null;
    }

    public CustomerReservationCheck checkTakeawayRequest(CustomerTakeawayOrderRequest order, String customerId) {
        CustomerReservationCheck customerReservationCheck = new CustomerReservationCheck();

        Restaurant restaurant = masterDataService.getRestaurant(order.getRestaurantId());
        if(StringUtils.isNotBlank(order.getTimeSlot()) && order.getRequestedTime() == 0) {
            order.setRequestedTime(TimeUtil.toEpochMillisDayOffsetFromToday(order.getTimeSlot(), restaurant.getIANATimezone(), order.getDateSlot()) / 1000);
        }

        FuseBoxAggregationProxy fuseBoxAggregationProxy =
                customerId == null ? FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order)
                                    : FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order, customerId);
        List<String> warnings = checkAndGetWarnings(order, fuseBoxAggregationProxy);

        //todo - Cost is used as delivery cost on waiter api. Should separate these out
        if(order.isDelivery()) {
            Map<String, Object> restaurantDefaults = RestaurantDefault.asMap(restaurant.getRestaurantDefaults());
            double calculatedDistance = fuseBoxAggregationProxy.calculateDistance(restaurant.getAddress(), order.getAddress());
            double cutOff = (Double) restaurantDefaults.getOrDefault(FixedDefaults.FREE_DELIVERY_RADIUS, 2D);
            int calculatedCost = fuseBoxAggregationProxy.getDeliveryCost(calculatedDistance, cutOff);
            double cost = MoneyService.toMoneyRoundNearest(calculatedCost);
            if(cost > 0) {
                customerReservationCheck.setExtraCosts(cost);
                customerReservationCheck.setExtraCostsReason(CustomerReservationCheck.REASON_DELIVERY);
            }
        } else {
            customerReservationCheck.setCost(0);
        }

        int calculatedSessionTotal = fuseBoxAggregationProxy.getCalculatedSessionTotal();
        double cost = MoneyService.toMoneyRoundNearest(calculatedSessionTotal);
        customerReservationCheck.setCost(cost);
        customerReservationCheck.setWarning(warnings);

        return customerReservationCheck;
    }

    public boolean checkTimesFuseBox(CustomerTakeawayOrderRequest request) {
        FuseBoxAggregationProxy fuseBoxAggregationProxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, request);
        NoticeAggregator output = customerTakeawayTimesFuseBox.check(fuseBoxAggregationProxy);
        return output.getIndividualNotices().size() > 0;
    }

    public boolean hitsBlackout(CustomerTakeawayOrderRequest order, String customerId) {
        if(StringUtils.isNotBlank(order.getTimeSlot()) && order.getRequestedTime() == 0) {
            Restaurant restaurant = masterDataService.getRestaurant(order.getRestaurantId());
            order.setRequestedTime(TimeUtil.toEpochMillisDayOffsetFromToday(order.getTimeSlot(), restaurant.getIANATimezone(), order.getDateSlot()) / 1000);
        }

        // some basic checks before fusebox
        FuseBoxAggregationProxy fuseBoxAggregationProxy
                = customerId == null ?  FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order)
                    : FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order, customerId);

        NoticeAggregator output = customerTakeawayCreationBlackoutsFuseBox.check(false, fuseBoxAggregationProxy);
        return output.getIndividualNotices().size() > 0;
    }

    public List<String> checkAndGetWarnings(CustomerTakeawayOrderRequest order, FuseBoxAggregationProxy proxy) {
        if(order.isDelivery()) {
            return new ArrayList<>(deliveryFuseBox.check(proxy).getIndividualNotices());
        }

        return checkAndGetWarnings(proxy, customerTakeawayCreationFuseBox);
    }

    public static List<String> checkAndGetWarnings(FuseBoxAggregationProxy proxy, FuseBox fuseBox) {
        return new ArrayList<>(fuseBox.check(false, proxy).getIndividualNotices());
    }

    public CustomerTakeawayResponseView createTakeaway(String customerId, CustomerTakeawayOrderRequest order) throws IllegalStateResponseException {
        // if the time slot is populated instead of RequestedTime, convert the slot to requested time for today (online ordering)
        Restaurant restaurant = masterDataService.getRestaurant(order.getRestaurantId());
        if(StringUtils.isNotBlank(order.getTimeSlot()) && order.getRequestedTime() == 0) {
            order.setRequestedTime(TimeUtil.toEpochMillisDayOffsetFromToday(order.getTimeSlot(), restaurant.getIANATimezone(), order.getDateSlot()) / 1000);
        }

        String payloadCheck = checkPayload(order);
        if(payloadCheck != null) {
            LOGGER.debug("Payload check fail: {}", payloadCheck);
            throw new IllegalStateResponseException(payloadCheck, HttpStatus.NOT_ACCEPTABLE);
        }

        FuseBoxAggregationProxy fuseBoxAggregationProxy =
                customerId == null ? FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order)
                        : FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order, customerId);

        List<String> messages = checkAndGetWarnings(order, fuseBoxAggregationProxy);
        if(order.isDelivery() && messages.size() > 0) {
            LOGGER.debug("Cannot deliver to this location");
            throw new IllegalStateResponseException("Cannot deliver to this location", HttpStatus.BAD_REQUEST);
        }

        int deliverySurcharge = 0;
        if(order.isDelivery()) {
            deliverySurcharge = fuseBoxAggregationProxy.getDeliverySurcharge();
        }

        Tuple<Session, Map<CalculationKey, Number>> takeaway = sessionService.createTakeaway(order, customerId, messages, null);
        Session session = takeaway.getA();
        if(session == null) {
            LOGGER.error("Could not create a takeaway: from order {}", order);
            throw new IllegalStateResponseException("Oops. Something went wrong... Please try again later", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<CalculationKey,Number> calculations = takeaway.getB();

        List<String> hardStop = new ArrayList<>();
        boolean paymentRequired = false;
        boolean isOnlineOrder = false;
        if(customerId != null) {
            //customer order
            FuseBoxAggregationProxy ccFuseBoxAggregationProxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, order, customerId);
            hardStop = CustomerTakeawayService.checkAndGetWarnings(ccFuseBoxAggregationProxy, customerTakeawayCreditCardCheck);
            paymentRequired = order.isPayByCC() || paymentRequired(calculations, ((Number) ccFuseBoxAggregationProxy.getRestaurantDefaults().getOrDefault(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC, 15)).doubleValue());
        } else {
            //online order
            //paymentRequired = paymentRequired(calculations, ((Number) fuseBoxAggregationProxy.getRestaurantDefaults().getOrDefault(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC, 15)).doubleValue());
            isOnlineOrder = true;
            if(order.isPayByCC()) {
                List<Adjustment> onlineOrderAdjustments = processPayment(session, calculations.get(CalculationKey.TOTAL).intValue(), order.getChargeId());
                if (onlineOrderAdjustments == null || (onlineOrderAdjustments.size() == 0)) {
                    hardStop.add("Could not verify payment");
                } else {
                    //payment was successful, update the adjustments
                    updateAdjustments(session, calculations, onlineOrderAdjustments);
                }
            }
        }

        if((hardStop.size() > 0 && paymentRequired) || (hardStop.size() > 0 && isOnlineOrder)) {
            sessionService.cancelSession(session);
            throw new IllegalStateResponseException("Cannot take payment with credit card - please check your card details", HttpStatus.BAD_REQUEST);
        }

        if(paymentRequired && !isOnlineOrder) {
            List<Adjustment> adjustments = sessionPaymentService.processPayment(session, customerService.getCustomer(customerId).getCcData(), false);
            updateAdjustments(session, calculations, adjustments);
        }

        Booking originalBooking = session.getOriginalBooking();
        if(messages.size() > 0) {
            originalBooking.setRejectionNotice(StringUtils.join(messages,", "));
            bookingService.upsert(originalBooking);
        }

        if(deliverySurcharge > 0) {
            session.setCalculatedDeliveryCost(deliverySurcharge);
            sessionService.updateDeliveryCost(session.getId(), deliverySurcharge);
        }

        //create the print batches if all was a success
        liveDataService.createPrintBatches(session, masterDataService.getAllMenuItems(order.getRestaurantId()), liveDataService.getOrdersBySessionId(session.getId()), false);

        return getCustomerTakeawayResponseView(originalBooking, sessionService.getSession(session.getId()));
    }

    protected void updateAdjustments(Session session, Map<CalculationKey, Number> calculations, List<Adjustment> adjustments) throws IllegalStateResponseException {
        if(adjustments.size() == 0 && calculations.get(CalculationKey.REMAINING_TOTAL).intValue() > 0) {
            //cancel the session
            sessionService.cancelSession(session);
            LOGGER.debug("Failed on adjustment size {} and remaining total {}", adjustments.size(), calculations.get(CalculationKey.REMAINING_TOTAL).intValue());
            throw new IllegalStateResponseException("Could not complete credit card payment", HttpStatus.BAD_REQUEST);
        }
        sessionService.addAdjustments(session.getId(), adjustments);
    }

    private List<Adjustment> processPayment(Session session, int total, String chargeId) {
        if(StringUtils.isBlank(chargeId)) {
            return null;
        }

        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setExternalId(chargeId);
        return sessionPaymentService.processPayment(session, creditCardData ,total, 0, true, false);
    }

    private boolean verifyPayment(int minimumAmount, ChargeSummary payment) {
        return payment != null && payment.getAmount() != null && payment.getAmount() >= minimumAmount;
    }

    private static boolean paymentRequired(Map<CalculationKey, Number> order, double maxValue) {
        int maxTotal = MoneyService.toPenniesRoundNearest(maxValue);
        return order.get(CalculationKey.TOTAL).intValue() >= maxTotal;
    }

    public CustomerTakeawayResponseView getCustomerTakeawayResponseView(Booking booking, Session session) {
        List<Order> orders = liveDataService.getOrders(session.getId());
        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());

        Map<CalculationKey,Number> values = sessionCalculationService.calculateValues(session, orders);
        return new CustomerTakeawayResponseView(session, booking, orders, restaurant, values, SessionCalculationService.isPaid(values));
    }
}
