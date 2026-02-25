package uk.co.epicuri.serverapi.engines;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerBlackMarkUtil;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerOrderItemView;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.host.HostReservationRequest;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.TakeawayPayload;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FuseBoxAggregationProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuseBoxAggregationProxy.class);

    private Map<String,Object> restaurantDefaults;
    private String restaurantId;
    private Session session;

    //stuff that is lazy loaded
    private Customer customer;
    private String customerId;
    private Booking booking;
    private List<Booking> currentBookings;
    private List<Party> currentParties;
    private long bookingTimeSeconds;
    private OpeningHours openingHours;
    private Double calculatedDistance;
    private List<Order> orders;
    private Integer deliveryCost;
    private Map<CalculationKey,Number> calculations;
    private Restaurant restaurant;
    private Map<String,MenuItem> menuItemMap;
    private Map<String,TaxRate> taxRateMap;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private GoogleMapsService googleMapsService;

    @Autowired
    private SessionCalculationService sessionCalculationService;

    private static FuseBoxAggregationProxy getFuseBoxAggregationProxy(AutowireCapableBeanFactory factory) {
        FuseBoxAggregationProxy proxy = new FuseBoxAggregationProxy();
        factory.autowireBean(proxy);
        return proxy;
    }

    public static FuseBoxAggregationProxy createTakeawayProxy(AutowireCapableBeanFactory factory, Booking booking, Session session) {
        if(factory == null || booking == null || session == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(booking, session);
        return takeawayPrep(proxy);
    }

    public static FuseBoxAggregationProxy createTakeawayProxy(AutowireCapableBeanFactory factory, Booking booking, Session session, Restaurant restaurant) {
        if(factory == null || booking == null || session == null || restaurant == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(booking, session, restaurant);
        return takeawayPrep(proxy);
    }

    public static FuseBoxAggregationProxy createReservationsProxy(AutowireCapableBeanFactory factory, String restaurantId, HostReservationRequest request) {
        if(factory == null || restaurantId == null || request == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(restaurantId, request);
        return reservationsPrep(proxy);
    }

    public static FuseBoxAggregationProxy createReservationsProxy(AutowireCapableBeanFactory factory, Booking booking) {
        if(factory == null || booking == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(booking);
        return reservationsPrep(proxy);
    }

    private static FuseBoxAggregationProxy createCustomerReservationsProxy(AutowireCapableBeanFactory factory, CustomerReservationView request, String customerId) {
        if(factory == null || request == null || customerId == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(request, customerId);
        return reservationsPrep(proxy);
    }

    public static FuseBoxAggregationProxy createCustomerReservationsProxy(AutowireCapableBeanFactory factory, CustomerReservationView request, Customer customer) {
        if(factory == null || request == null || customer == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = createCustomerReservationsProxy(factory, request, customer.getId());
        proxy.wire(customer);
        return reservationsPrep(proxy);
    }

    public static FuseBoxAggregationProxy createCustomerReservationsProxy(AutowireCapableBeanFactory factory, CustomerReservationView request) {
        if(factory == null || request == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(request, null);
        return reservationsPrep(proxy);
    }

    public static FuseBoxAggregationProxy createTakeawayProxy(AutowireCapableBeanFactory factory, CustomerTakeawayOrderRequest request, String customerId) {
        if(factory == null || request == null || customerId == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(request, customerId);
        return takeawayPrep(proxy);
    }

    public static FuseBoxAggregationProxy createTakeawayProxy(AutowireCapableBeanFactory factory, CustomerTakeawayOrderRequest request) {
        if(factory == null || request == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(request);
        return takeawayPrep(proxy);
    }

    public static FuseBoxAggregationProxy createTakeawayProxy(AutowireCapableBeanFactory factory, TakeawayPayload request) {
        if(factory == null || request == null) {
            throw new IllegalArgumentException("nulls not allowed");
        }
        FuseBoxAggregationProxy proxy = getFuseBoxAggregationProxy(factory);
        proxy.wire(request);
        return takeawayPrep(proxy);
    }

    private FuseBoxAggregationProxy() {}

    private void wire(Booking booking) {
        this.booking = booking;
        this.bookingTimeSeconds = booking.getTargetTime() / 1000;

        if(StringUtils.isNotBlank(booking.getCustomerId())) {
            this.customerId = booking.getCustomerId();
        }

        if(StringUtils.isNotBlank(booking.getRestaurantId())) {
            this.restaurantId = booking.getRestaurantId();
            if(restaurant == null) {
                this.restaurant = masterDataService.getRestaurant(restaurantId);
            }
            wireRestaurantDefaults(restaurantId);
        }
    }

    private void wire(Booking booking, Session session) {
        this.restaurantId = session.getRestaurantId();
        this.session = session;
        this.restaurant = masterDataService.getRestaurant(restaurantId);
        wireRestaurantDefaults(restaurantId);
        wire(booking);
    }

    public void wire(Booking booking, Session session, Restaurant restaurant) {
        this.restaurantId = session.getRestaurantId();
        this.session = session;
        this.restaurant = restaurant;
        this.restaurantDefaults = RestaurantDefault.asMap(restaurant.getRestaurantDefaults());
        wire(booking);
    }

    private void wire(String restaurantId, HostReservationRequest request) {
        this.restaurantId = restaurantId;
        this.restaurant = masterDataService.getRestaurant(restaurantId);
        wireRestaurantDefaults(restaurantId);
        this.customerId = request.getLeadCustomerId();

        //create a fake booking
        Booking aBooking = new Booking();
        aBooking.setTelephone(request.getPhoneNumber());
        aBooking.setEmail(request.getEmail());
        aBooking.setCustomerId(customerId);
        aBooking.setNumberOfPeople(request.getNumberInParty());
        aBooking.setRestaurantId(restaurantId);
        aBooking.setCreatedTime(System.currentTimeMillis());
        aBooking.setTargetTime(request.getReservationTime() * 1000);
        aBooking.setBookingType(BookingType.RESERVATION);

        wire(aBooking);
    }

    private void wireRestaurantDefaults(String restaurantId) {
        if(this.restaurant == null) {
            this.restaurant = masterDataService.getRestaurant(restaurantId);
        }
        this.restaurantDefaults = RestaurantDefault.asMap(restaurant.getRestaurantDefaults());
    }

    private void wire(CustomerReservationView request, String customerId) {
        this.restaurantId = request.getRestaurantId();
        this.restaurant = masterDataService.getRestaurant(restaurantId);
        wireRestaurantDefaults(restaurantId);

        //create a fake booking
        Booking aBooking = new Booking();
        aBooking.setTelephone(request.getTelephone());
        aBooking.setEmail(request.getEmail());
        aBooking.setCustomerId(customerId);
        aBooking.setNumberOfPeople(request.getNumberOfPeople());
        aBooking.setRestaurantId(restaurantId);
        aBooking.setCreatedTime(System.currentTimeMillis());
        if(request.getReservationTime() != null) {
            aBooking.setTargetTime(request.getReservationTime() * 1000);
        }
        aBooking.setBookingType(BookingType.RESERVATION);

        wire(aBooking);
    }

    private void wire(Customer customer) {
        this.customer = customer;
        this.customerId = customer.getId();
    }

    private void wire(String customerId) {
        this.customerId = customerId;
    }

    private void wire(CustomerTakeawayOrderRequest request, String customerId) {
        wire(customerId);
        wire(request);
    }

    private void wire(CustomerTakeawayOrderRequest request) {
        this.restaurantId = request.getRestaurantId();
        this.restaurant = masterDataService.getRestaurant(restaurantId);
        wireRestaurantDefaults(restaurantId);

        //create a fake booking
        Booking aBooking = new Booking();
        aBooking.setDeliveryAddress(request.getAddress());
        aBooking.setTelephone(request.getTelephone());
        aBooking.setCreatedTime(System.currentTimeMillis());
        aBooking.setRestaurantId(restaurantId);
        aBooking.setTargetTime(request.getRequestedTime() * 1000);
        aBooking.setBookingType(BookingType.TAKEAWAY);
        aBooking.setTakeawayType(request.isDelivery() ? TakeawayType.DELIVERY : TakeawayType.COLLECTION);
        if(request.isDelivery()) {
            aBooking.setDeliveryAddress(request.getAddress());
        }
        wire(aBooking);

        //create a fake session
        this.session = new Session();
        this.session.setSessionType(SessionType.TAKEAWAY);
        this.session.setRestaurantId(restaurantId);

        //create fake orders
        if(request.getItems() != null) {
            menuItemMap = masterDataService.getAllMenuItems(restaurantId).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
            Map<String,Modifier> modifierMap = masterDataService
                    .getModifiers(request.getItems().stream().flatMap(c -> c.getModifiers().stream()).distinct().collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(Modifier::getId, Function.identity()));
            taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));
            orders = new ArrayList<>();
            for(CustomerOrderItemView orderItemView : request.getItems()) {
                List<Modifier> modifiers = new ArrayList<>();
                for(String modifierId : orderItemView.getModifiers()) {
                    if(modifierMap.containsKey(modifierId)) {
                        modifiers.add(modifierMap.get(modifierId));
                    }
                }
                MenuItem item = menuItemMap.get(orderItemView.getMenuItemId());
                Order order = new Order(item, taxRateMap.get(item.getTaxTypeId()), orderItemView.getQuantity(), modifiers);
                orders.add(order);
            }
        }
    }

    private void wire(TakeawayPayload request) {
        this.restaurantId = request.getRestaurantId();
        this.restaurant = masterDataService.getRestaurant(restaurantId);
        wireRestaurantDefaults(restaurantId);
        this.customerId = request.getLeadCustomerId();

        //create a fake booking
        Booking aBooking = new Booking();
        aBooking.setDeliveryAddress(request.getAddress());
        aBooking.setTelephone(request.getTelephone());
        aBooking.setCreatedTime(System.currentTimeMillis());
        aBooking.setRestaurantId(restaurantId);
        aBooking.setTargetTime(request.getRequestedTime() * 1000);
        aBooking.setBookingType(BookingType.TAKEAWAY);
        aBooking.setTakeawayType(request.isDelivery() ? TakeawayType.DELIVERY : TakeawayType.COLLECTION);
        aBooking.setCustomerId(request.getLeadCustomerId());
        wire(aBooking);

        //create a fake session
        this.session = new Session();
        this.session.setSessionType(SessionType.TAKEAWAY);

        orders = new ArrayList<>();
    }

    private void reservationsPrep() {
        this.currentBookings = getReservations();
        prep();
    }

    private static FuseBoxAggregationProxy reservationsPrep(FuseBoxAggregationProxy proxy) {
        proxy.reservationsPrep();
        return proxy;
    }

    private void takeawayPrep() {
        this.currentBookings = getTakeaways();
        prep();
    }

    private static FuseBoxAggregationProxy takeawayPrep(FuseBoxAggregationProxy proxy) {
        proxy.takeawayPrep();
        return proxy;
    }

    private void prep() {
        if(booking != null && customerId == null) {
            customerId = booking.getCustomerId();
        }
        if(customer == null && customerId != null) {
            customer = customerService.getCustomer(customerId);
        }
        if(session != null && StringUtils.isNotBlank(session.getOriginalBookingId())) {
            booking = bookingService.getBooking(session.getOriginalBookingId());
            if(booking != null) {
                bookingTimeSeconds = booking.getTargetTime() / 1000;
            }
        }
    }

    private List<Booking> getReservations() {
        long plusMinusResTimeSlot = 1000 * 60
                * (restaurantDefaults == null ? 120 : ((Number)restaurantDefaults.getOrDefault(FixedDefaults.RESERVATION_TIMESLOT, 120)).intValue());
        long bookingTime = bookingTimeSeconds * 1000;
        List<Booking> reservations = bookingService.getReservations(restaurantId,
                (bookingTime - plusMinusResTimeSlot),
                (bookingTime + plusMinusResTimeSlot));
        reservations.removeIf(Booking::isOmitFromChecks);
        reservations.removeIf(Booking::isCancelled);
        return reservations;
    }

    private List<Booking> getTakeaways() {
        long plusMinusMinTime = 1000 * 60
                * (restaurantDefaults == null ? 2 : ((Number)restaurantDefaults.getOrDefault(FixedDefaults.TAKEAWAY_LOCK_WINDOW, 30)).intValue());
        long bookingTime = bookingTimeSeconds * 1000;
        List<Booking> takeaways = bookingService.getTakeaways(restaurantId,
                (bookingTime - plusMinusMinTime),
                (bookingTime + plusMinusMinTime));
        takeaways.removeIf(Booking::isOmitFromChecks);
        takeaways.removeIf(Booking::isCancelled);
        return takeaways;
    }

    private List<Party> getWalkIns() {
        long expirationInSeconds = 60 * ((Number)restaurantDefaults.getOrDefault(FixedDefaults.WALKIN_EXPIRATION_TIME, 90)).intValue();
        long limit = (System.currentTimeMillis()/1000) - expirationInSeconds;
        List<Party> all = liveDataService.getParties(restaurantId);
        return all.stream().filter(p -> p.getArrivedTime() != null && (p.getArrivedTime()/1000) > limit).collect(Collectors.toList());
    }

    public long getBookingTimeSeconds() {
        return bookingTimeSeconds;
    }

    public Map<String, Object> getRestaurantDefaults() {
        return restaurantDefaults;
    }

    public int getRequestedNumberOfPeople() {
        if(session != null) {
            if(session.getSessionType() == SessionType.TAKEAWAY) {
                return 1;
            } else {
                return session.getNumberOfRealDiners();
            }
        } else if(booking != null){
            if(booking.getBookingType() == BookingType.TAKEAWAY) {
                return 1;
            } else {
                return booking.getNumberOfPeople();
            }
        }

        return 1;
    }

    public int getNumberOfActiveReservationsInTimeSlot() {
        return currentBookings.size();
    }

    public int getNumberOfPeopleInTimeSlot() {
        return currentBookings.stream().mapToInt(Booking::getNumberOfPeople).sum();
    }

    public int getNumberOfWalkInsWithinExpirationTime() {
        if(currentParties == null) {
            currentParties = getWalkIns();
        }

        return currentParties.size();
    }

    public boolean isClosedForReservations() {
        if(booking == null) {
            return false;
        }

        if(openingHours == null) {
            openingHours = masterDataService.getOpeningHours(restaurantId, BookingType.RESERVATION);
        }

        if(openingHours == null) {
            return false;
        }

        return isClosed();
    }

    private boolean isClosed() {
        LOGGER.trace("Compare booking time {} with opening hours {}", booking.getTargetTime(), openingHours);
        if(openingHours.getAbsoluteBlackouts().stream().anyMatch(b ->
                booking.getTargetTime() >= b.getStart()
                        && booking.getTargetTime() <= b.getEnd())) {
            return true;
        }

        Instant instant = Instant.ofEpochMilli(booking.getTargetTime());
        ZonedDateTime utcDT = ZonedDateTime.ofInstant(instant, TimeUtil.UTC);
        ZoneId restaurantTz = ZoneId.of(restaurant.getIANATimezone());
        ZonedDateTime localDT = utcDT.withZoneSameInstant(restaurantTz);
        DayOfWeek dayOfWeek = localDT.getDayOfWeek();

        LOGGER.trace("Get hours for {}", dayOfWeek);

        if(openingHours.getHours().containsKey(dayOfWeek)) {
            List<HourSpan> hourSpans = openingHours.getHours().get(dayOfWeek);
            if(hourSpans.size() == 0) {
                LOGGER.trace("Closed because hour span size == 0");
                return true;
            }
            if(hourSpans.size() == 1 && isOpenAllDay(hourSpans.get(0))) {
                LOGGER.trace("Open because hour span set to all day open");
                return false;
            }

            for(HourSpan hourSpan : hourSpans) {
                ZonedDateTime open = ZonedDateTime.of(localDT.getYear(), localDT.getMonth().getValue(), localDT.getDayOfMonth(), hourSpan.getHourOpen(), hourSpan.getMinuteOpen(), 0, 0, restaurantTz);
                ZonedDateTime close;
                if(hourSpan.getHourClose() == 24) {
                    close = ZonedDateTime.of(localDT.getYear(), localDT.getMonth().getValue(), localDT.getDayOfMonth(), 23, 59, 59, 999999999, restaurantTz).plusNanos(1);
                } else {
                    close = ZonedDateTime.of(localDT.getYear(), localDT.getMonth().getValue(), localDT.getDayOfMonth(), hourSpan.getHourClose(), hourSpan.getMinuteClose(), 0, 0, restaurantTz);
                }

                long openUTC = open.withZoneSameInstant(TimeUtil.UTC).toInstant().toEpochMilli();
                long closeUTC = close.withZoneSameInstant(TimeUtil.UTC).toInstant().toEpochMilli();
                LOGGER.trace("Compare {} with open {} and close {}", booking.getTargetTime(), openUTC, closeUTC);
                if(booking.getTargetTime() >= openUTC
                    && booking.getTargetTime() <= closeUTC) {
                    return false;
                }
            }

        }
        return true;
    }

    public boolean isOpenAllDay(HourSpan hourSpan) {
        return hourSpan.getHourOpen() == 0
                && hourSpan.getMinuteOpen() == 0
                && hourSpan.getHourClose() == 24; //ignore minute when closed hour == 24
    }

    public int getDeliverySurcharge() {
        if(booking == null || booking.getDeliveryAddress() == null) {
            return 0;
        }

        if(calculatedDistance == null) {
            calculatedDistance = calculateDistance();
        }

        //get surcharge based on distance (FreeDeliveryRadius & DeliverySurcharge)
        double cutOff = ((Number)restaurantDefaults.getOrDefault(FixedDefaults.FREE_DELIVERY_RADIUS, 2D)).doubleValue();
        deliveryCost = getDeliveryCost(calculatedDistance, cutOff);
        return deliveryCost;
    }

    public int getDeliveryCost(double calculatedDistance, double cutOff) {
        if(calculatedDistance > cutOff) {
            double charge = ((Number)restaurantDefaults.getOrDefault(FixedDefaults.DELIVERY_SURCHARGE, 1.5)).doubleValue();
            return MoneyService.toPenniesRoundNearest(charge);
        } else {
            return 0;
        }
    }

    public double calculateDistance() {
        if(restaurant == null) {
            restaurant = masterDataService.getRestaurant(restaurantId);
        }
        if(calculatedDistance == null) {
            calculatedDistance = calculateDistance(restaurant.getAddress(), booking.getDeliveryAddress());
        }
        return calculatedDistance < 0 ? 0 : calculatedDistance;
    }

    public double calculateDistance(Address from, Address to) {
        return googleMapsService.getDistanceMiles(from,to);
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public boolean exceedsBlackMarks() {
        if(customerId == null && booking != null) {
            customerId = booking.getCustomerId();
        }
        if(customer == null && customerId != null) {
            customer = customerService.getCustomer(customerId);
        }
        return exceedsBlackMarks(customer);
    }

    public static boolean exceedsBlackMarks(Customer aCustomer) {
        return CustomerBlackMarkUtil.exceedsBlackMarks(aCustomer);
    }

    public static boolean isBirthday(Customer customer, RestaurantDefault birthdayTimeSpan) {
        return TimeUtil.isBirthday(customer.getBirthday(), ((Number) birthdayTimeSpan.getValue()).intValue());
    }

    public List<Booking> getCurrentBookingsAroundRequest() {
        if(booking == null || booking.getId() == null) {
            return currentBookings;
        }

        return currentBookings.stream().filter(b -> !b.getId().equals(booking.getId())).collect(Collectors.toList());
    }

    public List<Booking> getCurrentBookingsAroundRequestForCustomer() {
        //if there is a customer id then filter on that, else return none
        if(StringUtils.isNotBlank(customerId)) {
            return currentBookings.stream().filter(b -> b.getCustomerId() != null && b.getCustomerId().equals(customerId)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public boolean isClosedForTakeaways() {
        if(booking == null) {
            return false;
        }

        if(openingHours == null) {
            openingHours = masterDataService.getOpeningHours(restaurantId, BookingType.TAKEAWAY);
        }

        if(openingHours == null) {
            return false;
        }

        return isClosed();
    }

    public int getCalculatedSessionTotal() {
        if(calculations == null) {
            if (orders == null) {
                orders = liveDataService.getOrders(session.getId());
            }
            calculations = sessionCalculationService.calculateValues(session, orders);
        }

        return calculations.getOrDefault(CalculationKey.SESSION_TOTAL, 0).intValue();
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Booking getBooking() {
        return booking;
    }

    public boolean checkIsAddressFormatValidAndExists() {
        if (booking == null
                || booking.getBookingType() != BookingType.TAKEAWAY
                || booking.getTakeawayType() != TakeawayType.DELIVERY) {
            return true;
        }

        return booking.getDeliveryAddress() != null && googleMapsService.isAddressValid(booking.getDeliveryAddress());
    }

    public Session getSession() {
        return session;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getCustomerId() {
        return customerId;
    }
}
