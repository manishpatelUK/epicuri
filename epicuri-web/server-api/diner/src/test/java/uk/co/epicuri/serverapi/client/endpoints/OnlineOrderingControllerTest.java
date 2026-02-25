package uk.co.epicuri.serverapi.client.endpoints;

import com.jayway.restassured.response.Response;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.authentication.OnlineOrderingAuthResponse;
import uk.co.epicuri.serverapi.common.pojo.booking.TimeSlots;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerReservationCheck;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayOrderRequest;
import uk.co.epicuri.serverapi.common.pojo.customer.CustomerTakeawayResponseView;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.HourSpan;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.OpeningHours;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.service.BatchService;
import uk.co.epicuri.serverapi.service.SessionPaymentService;
import uk.co.epicuri.serverapi.service.external.StripeService;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class OnlineOrderingControllerTest extends TakeawayControllersTestsBase {
    @Autowired
    private SessionPaymentService sessionPaymentService;

    @Autowired
    private BatchService batchService;

    private static Set<String> ipAddresses;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupOnlineOrdersIntegration();
    }

    private OpeningHours setupOpeningHours(List<HourSpan> section) {
        OpeningHours openingHours = new OpeningHours();
        openingHours.setRestaurantId(restaurant1.getId());
        openingHours.setBookingType(BookingType.TAKEAWAY);
        openingHours.getHours().put(DayOfWeek.MONDAY, section);
        openingHours.getHours().put(DayOfWeek.TUESDAY, section);
        openingHours.getHours().put(DayOfWeek.WEDNESDAY, section);
        openingHours.getHours().put(DayOfWeek.THURSDAY, section);
        openingHours.getHours().put(DayOfWeek.FRIDAY, section);
        openingHours.getHours().put(DayOfWeek.SATURDAY, section);
        openingHours.getHours().put(DayOfWeek.SUNDAY, section);
        OpeningHours byRestaurantIdAndBookingType = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.TAKEAWAY);
        if (byRestaurantIdAndBookingType != null) openingHoursRepository.delete(byRestaurantIdAndBookingType);
        return openingHoursRepository.insert(openingHours);
    }

    private void setupOpeningHoursOpenAllDay() {
        HourSpan hourSpan = new HourSpan(0, 0, 24, 0);
        setupOpeningHours(Collections.singletonList(hourSpan));
    }

    private void setupOnlineOrdersIntegration() {
        KVData onlineOrderKv = new KVData();
        onlineOrderKv.setToken("abc");
        KVData stripeKv = new KVData();
        stripeKv.setToken("abc");
        restaurant1.getIntegrations().put(ExternalIntegration.EPICURI_ONLINE_ORDERS, onlineOrderKv);
        restaurant1.getIntegrations().put(ExternalIntegration.STRIPE, stripeKv);
        restaurant1.setOnlineOrderingIPAddresses(new ArrayList<>(ipAddresses));
        restaurant1.setIANATimezone("Europe/London");
        restaurant1.setISOCurrency("GBP");
        restaurantRepository.save(restaurant1);
    }

    private Response doAcquireToken() {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("restaurantId",restaurant1.getId())
                .queryParam("publicToken","abc")
                .post("onlineorders/acquireToken");
    }

    @Test
    public void testCors() {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Origin","http://www.someurl.com")
                .queryParam("restaurantId",restaurant1.getId())
                .queryParam("publicToken","abc")
                .post("onlineorders/acquireToken");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    private String getOnlineOrderingToken() {
        return doAcquireToken().as(OnlineOrderingAuthResponse.class).getToken();
    }

    @BeforeClass
    public static void getIps() throws Exception {
        ipAddresses = new HashSet<>();

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                ipAddresses.add(inetAddresses.nextElement().getHostAddress());
            }
        }
    }

    @Test
    public void testAcquireToken_negatives() throws Exception {
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post("onlineorders/acquireToken");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("restaurantId",restaurant1.getId())
                .post("onlineorders/acquireToken");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());

        restaurant1.getOnlineOrderingIPAddresses().clear();
        restaurant1.getOnlineOrderingIPAddresses().add("173.232.232.44");
        restaurantRepository.save(restaurant1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("restaurantId",restaurant1.getId())
                .queryParam("publicToken","abc")
                .post("onlineorders/acquireToken");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode());
    }

    @Test
    public void testAcquireToken() throws Exception {
        Response response = doAcquireToken();

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        OnlineOrderingAuthResponse onlineAuthResponse = response.getBody().as(OnlineOrderingAuthResponse.class);
        String token = onlineAuthResponse.getToken();
        assertTrue(StringUtils.isNotBlank(token));

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("restaurantId",restaurant1.getId())
                .queryParam("publicToken","abc")
                .post("onlineorders/acquireToken");

        String token2 = response.getBody().as(OnlineOrderingAuthResponse.class).getToken();
        assertTrue(StringUtils.isNotBlank(token2));
        assertEquals(token, token2);

        double maxOrder = (Double)restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MAX_TAKEAWAY_VALUE)).findFirst().orElse(null).getValue();
        double minOrder = (Double)restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MIN_TAKEAWAY_VALUE)).findFirst().orElse(null).getValue();
        int minTakeawayTime = (Integer)restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.TAKEAWAY_MINIMUM_TIME)).findFirst().orElse(null).getValue();
        assertEquals(maxOrder, onlineAuthResponse.getMaxOrderValue(), 0.1);
        assertEquals(minOrder, onlineAuthResponse.getMinOrderValue(), 0.1);
        assertEquals(minTakeawayTime, onlineAuthResponse.getTakeawayMinimumTime());
        assertEquals(onlineAuthResponse.getIsoCurrency(), restaurant1.getISOCurrency());
        assertEquals(onlineAuthResponse.getRestaurantName(), restaurant1.getName());
        assertEquals(onlineAuthResponse.getRestaurantImage(), restaurant1.getGuestLogoURL());
    }

    @Test
    public void testTakeawayMenu_negative() {
        restaurant1.setTakeawayMenu(null);
        restaurantRepository.save(restaurant1);

        String token = doAcquireToken().as(OnlineOrderingAuthResponse.class).getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .get("onlineorders/menu");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void testTakeawayMenu() {
        restaurant1.setTakeawayMenu(menu1.getId());
        restaurantRepository.save(restaurant1);
        menu1.setRestaurantId(restaurant1.getId());
        menuRepository.save(menu1);

        String token = doAcquireToken().as(OnlineOrderingAuthResponse.class).getToken();

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .get("onlineorders/menu");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        MenuView menuView = response.as(MenuView.class);
        assertNotNull(menuView);
        assertEquals(menu1.getId(), menuView.getId());
    }

    @Test
    public void testPutTakeawayCheck() throws Exception {
        String token = getOnlineOrderingToken();
        setupOpeningHoursOpenAllDay();
        TimeSlots timeSlots = getTimeSlots();
        assertTrue(timeSlots.getTimes().size() > 0);
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(timeSlots.getTimes().get(0));
        Response response = putTakeawayCheckOnlineOrders(request, token);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        CustomerReservationCheck customerReservationCheck = response.getBody().as(CustomerReservationCheck.class);
        assertEquals(10D, customerReservationCheck.getCost(), 0.01);
        assertEquals(0, customerReservationCheck.getWarning().size());
    }

    @Test
    public void testPutTakeawayCheckThrowsBadRequestOnBlackout() throws Exception {
        String token = getOnlineOrderingToken();

        long now = System.currentTimeMillis();
        addBlackout(now);

        long bookingTime = now + (60 * 1000 * 60 * 5);

        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(bookingTime);

        Response response = putTakeawayCheckOnlineOrders(request, token);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
    }

    @Test
    public void testPostTakeawayThrowsNotAcceptable1() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setTelephone(null);

        String token = getOnlineOrderingToken();

        postTakeawayOnlineOrders(request, token, HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    public void testPostTakeawayThrowsNotAcceptable2() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setAddress(null);
        request.setDelivery(true);

        String token = getOnlineOrderingToken();
        postTakeawayOnlineOrders(request, token, HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    public void testPostTakeawayThrowsBadRequestOnPaymentCheck() throws Exception {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setPayByCC(true);
        request.setChargeId("foobar");
        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC)).findFirst().orElse(null);
        restaurantDefault.setValue(0D);
        restaurantRepository.save(restaurant1);

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(null);
        replay(stripeService);

        String token = getOnlineOrderingToken();
        postTakeawayOnlineOrders(request, token, HttpStatus.BAD_REQUEST);
        verify(stripeService);
    }

    @Test
    public void testTakeawayNotPaidWhenNoChargeId1() throws Exception {
        assertPaymentMadeToTakeaway(null, false,false);
    }

    @Test
    public void testTakeawayNotPaidWhenNoChargeId2() throws Exception {
        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC)).findFirst().orElse(null);
        restaurantDefault.setValue(0D);
        restaurantRepository.save(restaurant1);
        assertPaymentMadeToTakeaway(null, false,false);
    }

    @Test
    public void testTakeawayGetsPaid() throws Exception {
        Charge charge = new Charge();
        charge.setAmount(2000L);
        charge.setId("foobar");
        charge.setPaid(true);
        assertPaymentMadeToTakeaway(charge, true,true);
    }

    private void assertPaymentMadeToTakeaway(Charge charge, boolean expectSuccess, boolean expectPayments) throws StripeException {
        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(System.currentTimeMillis());
        request.setPayByCC(true);
        request.setChargeId("foobar");

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(charge);
        replay(stripeService);

        String token = getOnlineOrderingToken();
        Response response = postTakeawayOnlineOrders(request, token, expectSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        verify(stripeService);

        if(expectSuccess) {
            CustomerTakeawayResponseView customerTakeawayResponseView = response.as(CustomerTakeawayResponseView.class);
            Session session = sessionRepository.findAll().stream().filter(s -> s.getOriginalBookingId() != null && s.getOriginalBookingId().equals(customerTakeawayResponseView.getId())).findFirst().orElse(null);
            assertNotNull(session);
            if (expectPayments) {
                assertEquals(1, session.getAdjustments().size());
                assertNotNull(session.getAdjustments().get(0).getSpecialAdjustmentData().get(StripeConstants.PAYMENT_KEY));
            }
        }
    }

    @Test
    public void testPostTakeaway() throws Exception {
        setupOpeningHoursOpenAllDay();
        TimeSlots timeSlots = getTimeSlots();
        assertTrue(timeSlots.getTimes().size() > 0);

        // ensure the menu item has the right printer
        menuItem1.setDefaultPrinter(masterDataService.getPrinters(restaurant1.getId()).get(0).getId());
        menuItemRepository.save(menuItem1);
        // ensure restaurant has default printer for takeaways
        restaurant1.setDefaultTakeawayPrinterId(menuItem1.getDefaultPrinter());
        restaurantRepository.save(restaurant1);
        // ensure printer has ip address
        Printer printer = printerRepository.findOne(menuItem1.getDefaultPrinter());
        printer.setIp("192.1.1.1");
        printerRepository.save(printer);

        CustomerTakeawayOrderRequest request = getCustomerTakeawayOrderRequest(timeSlots.getTimes().get(0));
        request.setPayByCC(true);

        String token = getOnlineOrderingToken();

        StripeService stripeService = mock(StripeService.class);
        Whitebox.setInternalState(sessionPaymentService, "stripeService", stripeService);
        Charge charge = new Charge();
        charge.setAmount(0L);
        charge.setId("foobar");
        charge.setPaid(true);
        expect(stripeService.charge(anyObject(), anyObject(CreditCardData.class), anyInt(), anyBoolean(), anyBoolean())).andReturn(charge);
        replay(stripeService);

        Response response = postTakeawayOnlineOrders(request, token, HttpStatus.OK);

        CustomerTakeawayResponseView customerTakeawayResponseView = response.as(CustomerTakeawayResponseView.class);
        Session session = sessionRepository.findAll().stream().filter(s -> s.getOriginalBookingId() != null && s.getOriginalBookingId().equals(customerTakeawayResponseView.getId())).findFirst().orElse(null);
        assertNotNull(session);
        assertTrue(session.getOriginalBooking().isAccepted());
        assertEquals(1, session.getAdjustments().size());
        assertNotNull(session.getAdjustments().get(0).getSpecialAdjustmentData().get(StripeConstants.PAYMENT_KEY));
        verify(stripeService);
        List<Order> orders = liveDataService.getAllLiveOrders(session.getId());
        orders.forEach(o -> assertEquals(ActivityInstantiationConstant.ONLINE, o.getInstantiatedFrom()));
        List<Batch> batchesBySessionId = liveDataService.getBatchesBySessionId(session.getId());
        batchesBySessionId.forEach(b -> {
            assertFalse(b.isAwaitingImmediatePrint());
            assertNull(b.getPrintedTime());
        });
        List<HostBatchView> batches = batchService.getHostBatchViews(restaurant1.getId());
        assertEquals(batchesBySessionId.get(0).getId(), batches.get(0).getId());

    }

    @Test
    public void testGetTimeSlots() throws Exception {
        setupOpeningHours(createHourSpans());
        TimeSlots respondedSlots = getTimeSlots();
        ZonedDateTime now = TimeUtil.getRestaurantTime(System.currentTimeMillis(), restaurant1.getIANATimezone());

        assertTrue(respondedSlots.getTimes().size()>0);
        int takeawayMinTime = (Integer)restaurant1.getRestaurantDefaults().stream().filter(r -> r.getName().equals(FixedDefaults.TAKEAWAY_MINIMUM_TIME)).findFirst().orElse(null).getValue();
        ZonedDateTime minTime = now.plusMinutes(takeawayMinTime);
        respondedSlots.getTimes().stream().map(TimeUtil::getHourComponent).forEach(h -> assertFalse(h < minTime.getHour()));

    }

    @Test
    public void testGetTimeSlotsClosed() throws Exception {
        OpeningHours openingHours = setupOpeningHours(createHourSpans());

        ZonedDateTime lastMidnight = TimeUtil.getLastMidnight(restaurant1.getIANATimezone());
        DayOfWeek tomorrow = lastMidnight.plusDays(1).getDayOfWeek();
        // make tomorrow closed all day
        openingHours.getHours().put(tomorrow, new ArrayList<>());
        openingHoursRepository.save(openingHours);

        TimeSlots respondedSlots = getTimeSlots(1);
        assertEquals(0, respondedSlots.getTimes().size());
    }

    private TimeSlots getTimeSlots() {
        return getTimeSlots(0);
    }

    private TimeSlots getTimeSlots(int daysOffset) {
        String token = getOnlineOrderingToken();
        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("day", daysOffset)
                .get("/onlineorders/timeslots");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        return response.getBody().as(TimeSlots.class);
    }
}
