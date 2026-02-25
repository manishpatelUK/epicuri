package uk.co.epicuri.serverapi.client.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.customer.*;
import uk.co.epicuri.serverapi.common.pojo.host.HostCustomerView;
import uk.co.epicuri.serverapi.common.pojo.host.WaitingPartyPayload;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Table;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.errors.LockStateException;
import uk.co.epicuri.serverapi.service.*;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Session", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class SessionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionController.class);

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Autowired
    private LockingService lockingService;

    @CustomerAuthRequired
    @RequestMapping(value = "/ServiceRequest/{id}", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> postServiceRequest(@RequestHeader(Params.AUTHORIZATION) String token,
                                                @NotNull @PathVariable("id") String sessionId) {
        return checkAndInsertNotification(token, sessionId, NotificationConstant.TEXT_SERVICE_CALL, NotificationConstant.TARGET_WAITER_ACTION);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/BillRequest/{id}", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> postBillRequest(@RequestHeader(Params.AUTHORIZATION) String token,
                                    @NotNull @PathVariable("id") String sessionId) {
        return checkAndInsertNotification(token, sessionId, NotificationConstant.TEXT_BILL_REQUEST, NotificationConstant.TARGET_WAITER_ACTION);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/Order/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postOrders(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String sessionId,
                                        @NotNull @RequestBody SelfServiceRequest request) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
        }

        Map<String,Course> courseMap = masterDataService.getCoursesByRestaurantId(session.getRestaurantId()).stream().collect(Collectors.toMap(Course::getId, Function.identity()));

        if(session.getSessionType() == SessionType.SEATED && request.getCcData() == null) {
            Tuple<List<Order>, Tuple<HttpStatus, String>> response = createSelfServiceOrders(request.getOrders(), session, token, true);
            if (response.getA() == null && response.getB() != null) {
                Tuple<HttpStatus, String> error = response.getB();
                return ResponseEntity.status(error.getA()).body(new StringMessage(error.getB()));
            }
            return ResponseEntity.created(URI.create("/Order/"+sessionId)).body(new SelfServiceResponse(response.getA(), courseMap, session.getService(), 0, null));
        }

        if((session.getSessionType() == SessionType.SEATED || session.getSessionType() == SessionType.TAB) && request.getCcData() != null) {
            Tuple<List<Order>, Tuple<HttpStatus, String>> response = createSelfServiceOrders(request.getOrders(), session, token, false);
            List<Order> orderList = response.getA();
            if (orderList == null && response.getB() != null) {
                Tuple<HttpStatus, String> error = response.getB();
                return ResponseEntity.status(error.getA()).body(new StringMessage(error.getB()));
            }
            if(orderList == null) {
                //should never happen
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new StringMessage("Could not create orders"));
            }

            Customer customer = customerService.getCustomer(authenticationService.getCustomerId(token));
            if(customer.getCcData() == null) {
                LOGGER.trace("Could not process card payment for {}, ccData: {}, request:", customer.getId(), customer.getCcData(), request.getCcData());
                liveDataService.cancelOrders(sessionId, orderList);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Please add a credit card to your profile!"));
            }

            Map<CalculationKey, Number> calculations = sessionCalculationService.calculateValues(session, orderList);
            int total = calculations.get(CalculationKey.TOTAL).intValue();
            int gratuity = request.getTipAmount();

            List<Adjustment> adjustments = sessionPaymentService.processPayment(session, customer.getCcData(), total, gratuity, true);
            if(adjustments.size() == 0) {
                liveDataService.cancelOrders(sessionId, orderList);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Could not process credit card data"));
            }
            sessionService.addAdjustments(session.getId(), adjustments);
            String publicOrderId = updateOrderAndBatchParameters(request, orderList);
            //create the print batches if all was a success
            liveDataService.createPrintBatches(session, masterDataService.getAllMenuItems(session.getRestaurantId()), orderList, false);
            return ResponseEntity.created(URI.create("/Order/"+sessionId)).body(new SelfServiceResponse(orderList, courseMap, session.getService(), total, publicOrderId));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Incorrect data sent to server")); //should never get here
    }

    private String updateOrderAndBatchParameters(@NotNull @RequestBody SelfServiceRequest request, List<Order> orderList) {
        final String deliveryLocation = request.getDeliveryLocation();
        String publicOrderId = liveDataService.pushSelfServiceParameters(orderList, deliveryLocation);
        orderList.forEach(o -> {
            o.setPublicFacingOrderId(publicOrderId);
            o.setDeliveryLocation(deliveryLocation);
        });
        return publicOrderId;
    }

    private Tuple<List<Order>,Tuple<HttpStatus,String>> createSelfServiceOrders(List<CustomerOrderItemView> orders, Session session, String token, boolean createBatchesImmediately) {
        orders = orders.stream().filter(o -> o.getQuantity() > 0).collect(Collectors.toList());
        if(orders.size() == 0) {
            return new Tuple<>(null, new Tuple<>(HttpStatus.BAD_REQUEST, "There are no order items"));
        }

        if(session.isBillRequested()) {
            return new Tuple<>(null, new Tuple<>(HttpStatus.BAD_REQUEST, "Your bill has already been requested"));
        }

        String customerId = authenticationService.getCustomerId(token);
        Diner diner = liveDataService.getDiner(session.getId(), customerId);
        if(diner == null) {
            return new Tuple<>(null, new Tuple<>(HttpStatus.FORBIDDEN, "Customer is not attached to this session"));
        }

        Map<String,MenuItem> items = masterDataService.getAllMenuItems(session.getRestaurantId()).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        //cache the modifiers so don't have to keep going back to the database
        Set<String> allModifierIds = new HashSet<>();
        orders.forEach(m -> allModifierIds.addAll(m.getModifiers()));
        List<Modifier> modifiers = masterDataService.getModifiers(allModifierIds);
        Map<String,Modifier> modifierMap = modifiers.stream().collect(Collectors.toMap(Modifier::getId, Function.identity()));

        List<Order> ordersToInsert = new ArrayList<>();
        for(CustomerOrderItemView orderView : orders) {
            if(!items.containsKey(orderView.getMenuItemId())) {
                continue; // should not throw an error in this case
            }
            MenuItem item = items.get(orderView.getMenuItemId());
            if(item.isUnavailable()) {
                continue; // should not throw an error in this case
            }

            List<Modifier> modifiersWithTaxes = new ArrayList<>();
            orderView.getModifiers().forEach(m -> modifiersWithTaxes.add(new Modifier(modifierMap.get(m), taxRateMap.get(modifierMap.get(m).getTaxTypeId()))));
            Order newOrder = new Order(session,orderView,modifiersWithTaxes,taxRateMap.get(item.getTaxTypeId()), item, diner, null);
            if(orderView.getInstantiatedFromId() == ActivityInstantiationConstant.UNKNOWN.getId()) {
                newOrder.setInstantiatedFrom(ActivityInstantiationConstant.CUSTOMER);
            }
            ordersToInsert.add(newOrder);
        }

        if(ordersToInsert.size() == 0) {
            return new Tuple<>(null, new Tuple<>(HttpStatus.NOT_FOUND, "Menu items not found"));
        }

        List<Order> inserted = liveDataService.insertOrders(session, items.values(), ordersToInsert, createBatchesImmediately, true);

        return new Tuple<>(inserted,null);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getSession(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String sessionId) {
        Session session = sessionService.getSession(sessionId);

        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Session not found"));
        }

        String customerId = authenticationService.getCustomerId(token);
        Diner diner = liveDataService.getDiner(sessionId, customerId);
        List<Diner> diners = liveDataService.getDiners(sessionId);
        diners = diners.stream().filter(d -> !d.isDefaultDiner()).collect(Collectors.toList());

        if(diner == null || diners.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Diner not found"));
        }

        Service service = session.getService();
        if(service == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Service not found"));
        }

        List<RestaurantDefault> restaurantDefaults = masterDataService.getRestaurant(session.getRestaurantId()).getRestaurantDefaults();

        List<Order> orders = liveDataService.getOrders(sessionId);
        List<String> courseIds = orders.stream().filter(o -> o.getCourseId() != null).map(Order::getCourseId).distinct().collect(Collectors.toList());
        List<Course> courses = masterDataService.getCourses(courseIds);

        if(session.getOriginalBookingId() != null && session.getSessionType().equals(SessionType.TAKEAWAY)) {
            Booking takeawayBooking = bookingService.getBookingIncludeCancelled(session.getOriginalBookingId());
            if(takeawayBooking != null) {
                session.setOriginalBooking(takeawayBooking);
            }
        }

        double tipPercentage = 0;
        if(session.getTipPercentage() != null) {
            tipPercentage = session.getTipPercentage();
        }

        List<Customer> customers = customerService.getCustomerByIds(diners.stream().filter(d -> StringUtils.isNotBlank(d.getCustomerId())).map(Diner::getCustomerId).collect(Collectors.toList()));
        Map<String,Customer> idToCustomer = customers.stream().collect(Collectors.toMap(Customer::getId, Function.identity()));
        BillSplit billSplit = sessionCalculationService.calculateDinerSplits(session, orders);
        Map<String, Table> tableIdToTable = masterDataService.getTables(session.getRestaurantId()).stream().collect(Collectors.toMap(Table::getId, Function.identity()));
        StringBuilder tableName = new StringBuilder();
        for(int i = 0; i < session.getTables().size(); i++) {
            Table table = tableIdToTable.get(session.getTables().get(0));
            if(table == null) continue;
            tableName.append(table.getName());
            if(i != (session.getTables().size()-1)) tableName.append(",");
        }

        CustomerSessionView customerSessionView = new CustomerSessionView(
                session,
                service,
                masterDataService.getRestaurant(session.getRestaurantId()),
                tipPercentage,
                sessionCalculationService.calculateValues(session, orders),
                restaurantDefaults.stream().filter(r -> r.getName().equals(FixedDefaults.CLOSED_SESSION_MESSAGE)).findFirst().orElse(null).getValue().toString(),
                restaurantDefaults.stream().filter(r -> r.getName().equals(FixedDefaults.SOCIAL_MEDIA_MESSAGE)).findFirst().orElse(null).getValue().toString(),
                orders,
                courses,
                idToCustomer,
                billSplit,
                tableName.toString());

        return ResponseEntity.ok(customerSessionView);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/tab/{id}", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> postTab(@RequestHeader(Params.AUTHORIZATION) String token,
                                     @PathVariable("id") String id) {
        String customerId = authenticationService.getCustomerId(token);
        return getPostTab(customerId, id);
    }

    private ResponseEntity<?> getPostTab(String customerId, String restaurantId) {
        //get checkin if it exists, otherwise create one
        if(!masterDataService.restaurantExists(restaurantId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not find restaurant");
        }
        List<Service> services = masterDataService.getServicesByRestaurant(restaurantId);
        Service service = services.stream().filter(s -> s.getSessionType() == SessionType.TAB || s.getSessionType() == SessionType.SEATED).findFirst().orElse(null);
        if(service == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Sorry, we don't support tabs on guest accounts yet :("));
        }

        CheckIn checkIn = liveDataService.getCheckInByCustomer(customerId);
        if(checkIn == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Please hit the check-in button first"));
        }

        Party party = liveDataService.getParty(checkIn.getPartyId());
        if(party == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Party couldn't be created - please see the wait staff to create your party"));
        }

        //check if a session already belongs to this party - if so, bug out
        Session session = sessionService.getSessionByPartyId(party.getId());
        if(session != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("You're already checked in & have a session going!"));
        }

        SessionLock sessionLock = null;
        try {
            sessionLock = lockingService.lock(customerId, SessionLockType.SESSION_CREATION);
        } catch (LockStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Check In progressing, please wait");
        }
        try {
            SessionPayload sessionPayload = new SessionPayload();
            sessionPayload.setServiceId(service.getId());
            session = sessionService.createFromParty(party, sessionPayload);
            liveDataService.setSessionDataOnCheckin(restaurantId, customerId, session.getId(), session.getOriginalPartyId());
        } finally {
            lockingService.release(sessionLock);
        }

        CustomerTabCreationView view = new CustomerTabCreationView();
        view.setCheckInId(checkIn.getId());
        view.setSessionId(session.getId());

        return ResponseEntity.ok(view);
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/tab/close/{id}", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> postTabClose(@PathVariable("id") String id) {
        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Could not find your tab"));
        }

        if(session.getSessionType() != SessionType.TAB) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Cannot close at this time"));
        }

        if(!sessionService.isFullyPaid(session)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Session is not paid - please settle up."));
        }

        sessionService.closeSession(session);

        return ResponseEntity.ok().build();
    }

    @CustomerAuthRequired
    @RequestMapping(value = "/Payment/{id}/cc", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> paySession(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String id,
                                        @RequestBody PaymentRequestView paymentRequestView) {
        Customer customer = customerService.getCustomer(authenticationService.getCustomerId(token));
        if(customer.getCcData() == null
                || customer.getCcData().getCcToken() == null
                || !customer.getCcData().getCcToken().equals(paymentRequestView.getCcToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Credit card information is missing or out of date"));
        }

        Session session = sessionService.getSession(id);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Session not found"));
        }

        //calculate the overpayment, and overflow it into the tip amount
        Map<CalculationKey,Number> calculations = sessionCalculationService.calculateValues(session);
        int total = calculations.get(CalculationKey.REMAINING_TOTAL).intValue();
        int gratuity = paymentRequestView.getTipAmount();
        int toPay = paymentRequestView.getAmount();
        if(toPay > total) {
            int extra = toPay - total;
            gratuity += extra;
            toPay -= extra;
        }

        List<Adjustment> adjustments = sessionPaymentService.processPayment(session, customer.getCcData(), toPay, gratuity, true);
        if(adjustments.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Could not complete payment"));
        }

        sessionService.addAdjustments(session.getId(), adjustments);

        return ResponseEntity.ok(paymentRequestView);
    }

    private boolean checkInExpired(CheckIn checkIn) {
        long expiration = CheckInController.getExpirationTime(masterDataService, checkIn);
        return CheckInController.isCheckInExpired(checkIn, expiration);
    }


    private ResponseEntity<?> checkAndInsertNotification(String token, String sessionId, NotificationConstant text, NotificationConstant action) {
        Session session = sessionService.getSession(sessionId);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Session not found"));
        }

        if(session.isBillRequested()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new StringMessage("Your bill has already been requested"));
        }

        String customerId = authenticationService.getCustomerId(token);
        CheckIn checkIn = liveDataService.getCheckInByCustomer(customerId);
        Diner diner = liveDataService.getDiner(sessionId, customerId);

        if(checkIn != null && !checkIn.getSessionId().equals(sessionId) || diner == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new StringMessage("Customer not attached to this party"));
        }

        RestaurantDefault restaurantDefault = masterDataService.getRestaurantDefault(session.getRestaurantId(), FixedDefaults.MIN_TIME_BETWEEN_SERVICE_REQUESTS);
        long minTime = restaurantDefault == null ? 1000*60*5 : 1000*60*Long.valueOf(restaurantDefault.getValue().toString());

        List<Notification> notifications = liveDataService.getUnacknowledgedNotifications(session.getRestaurantId(), sessionId);

        Notification last = notifications.stream().filter(n -> n.getText().equals(text.getConstant())).reduce((a, b) -> b).orElse(null);
        if(last == null || notifications.size() == 0 || last.getTime() < System.currentTimeMillis()-minTime) {
            // allow request to proceed
            Notification notification = new Notification();
            notification.setRestaurantId(session.getRestaurantId());
            notification.setSessionId(sessionId);
            notification.setTarget(action.getConstant());
            notification.setText(text.getConstant());
            notification.setTime(System.currentTimeMillis());
            notification.setNotificationType(NotificationType.ADHOC);
            liveDataService.upsert(notification);
            return ResponseEntity.created(URI.create("/ServiceRequest/"+notification.getId())).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new StringMessage("Only one service call allowed every " + Math.round(minTime/(1000*60)) + " minutes"));
        }
    }
}
