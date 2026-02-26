package uk.co.epicuri.serverapi.host.endpoints;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.host.HostClosuresView;
import uk.co.epicuri.serverapi.common.pojo.host.HostOpeningHoursView;
import uk.co.epicuri.serverapi.common.pojo.host.HostRestaurantView;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by manish.
 */
public class RestaurantControllerTest extends BaseIT {

    protected List<RestaurantDefault> restaurantDefaults;

    @Value("${epicuri.url}")
    private String apiURL;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Address address = new Address();
        address.setPostcode("HA6 1AU");
        address.setCity("London");
        address.setStreet("61 Northwood Way");
        restaurant1.setAddress(address);
        restaurant1.setCountryId("UK");
        restaurant1.setCreationTime(System.currentTimeMillis());
        restaurant1.setCuisineId(cuisine2.getId());
        printer1.setRestaurantId(restaurant1.getId());
        printer2.setRestaurantId(restaurant1.getId());
        printerRepository.save(printer1);
        printerRepository.save(printer2);
        restaurant1.setDefaultBillingPrinterId(printer1.getId());
        restaurant1.setDefaultTakeawayPrinterId(printer2.getId());
        restaurant1.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud");
        restaurant1.setEnabledForDiner(true);
        restaurant1.setEnabledForWaiter(false);
        List<Floor> floorList = new ArrayList<>();
        floor1.setId(IDAble.generateId(restaurant1.getId()));
        floor2.setId(IDAble.generateId(restaurant1.getId()));
        floorList.add(floor1);
        floorList.add(floor2);
        restaurant1.setFloors(floorList);
        restaurant1.setIANATimezone("Europe/London");
        restaurant1.setInternalEmailAddress("foo@bar.com");
        restaurant1.setISOCurrency("USD");
        restaurant1.setName("Puky Pizzas");
        restaurant1.setPhoneNumber1("0237983249834");
        restaurant1.setPhoneNumber2("83744332");
        LatLongPair latLongPair = new LatLongPair();
        latLongPair.setLatitude(0.1);
        latLongPair.setLongitude(-0.1);
        restaurant1.setPosition(latLongPair);
        restaurant1.setPublicEmailAddress("bar@foo.com");
        restaurant1.setReceiptFooter("footer");
        restaurant1.setReceiptImageURL("some url");
        restaurant1.setReceiptType(ReceiptType.HOTEL);
        restaurantDefaults = createRestaurantDefaults();
        restaurant1.setRestaurantDefaults(restaurantDefaults);
        List<Service> servicesList = new ArrayList<>();
        service1.setId(IDAble.generateId(restaurant1.getId()));
        servicesList.add(service1);
        restaurant1.setServices(servicesList);
        restaurant1.setStaffFacingId("1");
        List<Table> tablesList = new ArrayList<>();
        table1.setId(IDAble.generateId(restaurant1.getId()));
        table2.setId(IDAble.generateId(restaurant1.getId()));
        tablesList.add(table1);
        tablesList.add(table2);
        restaurant1.setTables(tablesList);
        restaurant1.setTakeawayMenu(menu1.getId());
        restaurant1.setTakeawayOffered(TakeawayOfferingType.DELIVERY_AND_COLLECTION);
        restaurant1.setVatNumber("3984732");
        restaurant1.setWebsite("www.epicuri.co.uk");
        restaurant1.getAdjustmentTypes().add(adjustmentType2.getId());

        OpeningHours openingHours1 = getOpeningHours(BookingType.RESERVATION);
        OpeningHours openingHours2 = getOpeningHours(BookingType.TAKEAWAY);
        openingHoursRepository.deleteAll();
        openingHoursRepository.insert(openingHours1);
        openingHoursRepository.insert(openingHours2);

        restaurantRepository.save(restaurant1);

        staff1.setRestaurantId(restaurant1.getId());
        staffRepository.save(staff1);

        restaurantImage2.setRestaurantId(restaurant1.getId());
        restaurantImageRepository.save(restaurantImage2);
    }

    private OpeningHours getOpeningHours(BookingType type) {
        OpeningHours openingHours = new OpeningHours();
        AbsoluteBlackout blackout1 = new AbsoluteBlackout();
        blackout1.setStart(1498608000000L); //28th June 2017 midnight UTC
        blackout1.setEnd(1498608000000L + (1000*60*60*24));
        AbsoluteBlackout blackout2 = new AbsoluteBlackout();
        blackout2.setStart(1488326400000L); //March 1st 2017 midnight UTC
        blackout2.setEnd(1488326400000L + (1000*60*60*24));
        openingHours.getAbsoluteBlackouts().add(blackout2);
        openingHours.getAbsoluteBlackouts().add(blackout1);
        openingHours.getHours().put(DayOfWeek.MONDAY, getHours());
        openingHours.getHours().put(DayOfWeek.TUESDAY, getHours());
        openingHours.getHours().put(DayOfWeek.WEDNESDAY, getHours());
        openingHours.getHours().put(DayOfWeek.THURSDAY, getHours());
        openingHours.getHours().put(DayOfWeek.FRIDAY, getHours());
        openingHours.getHours().put(DayOfWeek.SATURDAY, getHours());
        openingHours.getHours().put(DayOfWeek.SUNDAY, getHours());
        openingHours.setRestaurantId(restaurant1.getId());
        openingHours.setBookingType(type);
        return openingHours;
    }

    private List<HourSpan> getHours() {
        List<HourSpan> list = new ArrayList<>();
        HourSpan hourSpan = new HourSpan();
        hourSpan.setHourOpen(9); //this is in UTC!
        hourSpan.setHourClose(21); //this is in UTC!
        list.add(hourSpan);
        return list;
    }

    private List<RestaurantDefault> createRestaurantDefaults() {
        List<RestaurantDefault> list = new ArrayList<>();
        for(Default def : defaultsRepository.findAll()) {
            RestaurantDefault restaurantDefault = new RestaurantDefault(def);
            if(def.getName().equals(FixedDefaults.COVERS_BEFORE_AUTOTIP)) {
                restaurantDefault.setValue(45);
            }
        }
        return list;
    }

    @Test
    public void testGetRestaurant() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Restaurant");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        HostRestaurantView restaurantView = response.getBody().as(HostRestaurantView.class, ObjectMapperType.JACKSON_2);
        testRestaurantEquals(restaurant1, restaurantView, false);

        KVData kvData = new KVData();
        kvData.setToken("a token");
        restaurant1.getIntegrations().put(ExternalIntegration.MEWS, kvData);
        restaurantRepository.save(restaurant1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Restaurant");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        restaurantView = response.getBody().as(HostRestaurantView.class, ObjectMapperType.JACKSON_2);
        testRestaurantEquals(restaurant1, restaurantView, false);

        AdjustmentType mews = new AdjustmentType();
        mews.setType(AdjustmentTypeType.PAYMENT);
        mews.setName(MewsConstants.MEWS_ADJUSTMENT_TYPE);
        mews.setSupportsChange(false);
        adjustmentTypeRepository.insert(mews);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Restaurant");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        restaurantView = response.getBody().as(HostRestaurantView.class, ObjectMapperType.JACKSON_2);
        testRestaurantEquals(restaurant1, restaurantView, true);

        KVData kvData2 = new KVData();
        kvData2.setHost("foo");
        kvData2.setKey("bar");
        restaurant1.getIntegrations().put(ExternalIntegration.PAYMENT_SENSE, kvData2);
        restaurantRepository.save(restaurant1);

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .get("Restaurant");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        restaurantView = response.getBody().as(HostRestaurantView.class, ObjectMapperType.JACKSON_2);
        assertEquals(kvData2.getHost(), restaurantView.getPaymentSense().get("host"));
        assertEquals(kvData2.getKey(), restaurantView.getPaymentSense().get("key"));
    }

    private void testRestaurantEquals(Restaurant restaurant, HostRestaurantView restaurantView, boolean mewsAdjustmentAdded) {
        assertEquals(restaurant.getAddress(), restaurantView.getAddress());
        assertEquals(restaurant.getDefaultBillingPrinterId(), restaurantView.getBillingPrinterId());
        assertEquals(restaurant.getISOCurrency(), restaurantView.getCurrency());
        assertEquals(restaurant.getPublicEmailAddress(), restaurantView.getEmail());
        assertEquals(restaurant.getName(), restaurantView.getName());
        assertEquals(restaurant.getPhoneNumber1(), restaurantView.getPhoneNumber1());
        assertEquals(restaurant.getPhoneNumber2(), restaurantView.getPhoneNumber2());
        assertEquals(restaurant.getReceiptFooter(), restaurantView.getRecieptFooter());
        assertEquals(apiURL + "/Restaurant/BillLogo/" + restaurant.getReceiptImageURL(), restaurantView.getRecieptImageURL());
        assertEquals(restaurant.getTakeawayMenu(), restaurantView.getTakeawayMenuId());
        assertEquals(restaurant.getIANATimezone(), restaurantView.getTimezone());
        assertEquals(restaurant.getVatNumber(), restaurantView.getVatNumber());
        assertEquals(restaurant.getWebsite(), restaurantView.getWebsite());
        assertEquals(restaurant.getDefaultTakeawayPrinterId(), restaurantView.getTakeawayPrinterId());
        assertEquals(restaurant.getReceiptType().getApiExpose(), restaurantView.getRecieptType());
        assertEquals(restaurantDefaults.stream().collect(Collectors.toMap(RestaurantDefault::getName, RestaurantDefault::getValue)), restaurantView.getRestaurantDefaults());
        assertEquals(restaurant.getIntegrations().containsKey(ExternalIntegration.MEWS) && mewsAdjustmentAdded, restaurantView.isMewsEnabled());
        assertTrue(restaurantView.getAdjustmentTypes().stream().anyMatch(a -> a.getId().equals(adjustmentType2.getId())));
    }

    @Test
    public void testGetLogoImage() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", "foobar")
                .get("Restaurant/BillLogo/{id}");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());

        response = given()
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", restaurantImage2.getId())
                .get("Restaurant/BillLogo/{id}");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        byte[] array = response.asByteArray();
        assertTrue(Arrays.equals(restaurantImage2.getImage(), array));

        response = given()
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", restaurantImage2.getId())
                .get("Restaurant/BillLogo/{id}.jpg");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        array = response.asByteArray();
        assertTrue(Arrays.equals(restaurantImage2.getImage(), array));
    }

    @Test
    public void testGetOpeningHours() throws Exception {
        String token = getTokenForStaff(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.TAKEAWAY.toString())
                .get("Restaurant/OpeningHours");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostOpeningHoursView openingHoursView = response.getBody().as(HostOpeningHoursView.class, ObjectMapperType.JACKSON_2);

        assertHoursEqual(9, 21, DayOfWeek.MONDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.TUESDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.WEDNESDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.THURSDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.FRIDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.SATURDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.SUNDAY, openingHoursView);
        assertHoursEqual(9, 21, DayOfWeek.SUNDAY, openingHoursView);
    }

    private void assertHoursEqual(int open, int close, DayOfWeek dayOfWeek, HostOpeningHoursView openingHoursView) {
        assertEquals(open, openingHoursView.getHours().get(dayOfWeek).get(0).getHourOpen());
        assertEquals(close, openingHoursView.getHours().get(dayOfWeek).get(0).getHourClose());
    }

    @Test
    public void testPutOpeningHours() throws Exception {
        String token = getTokenForStaff(staff1);
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        HostOpeningHoursView hostOpeningHoursView = new HostOpeningHoursView(openingHours);
        hostOpeningHoursView.getHours().get(DayOfWeek.WEDNESDAY).get(0).setHourOpen(12);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostOpeningHoursView)
                .put("Restaurant/OpeningHours");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        assertEquals(9, openingHours.getHours().get(DayOfWeek.MONDAY).get(0).getHourOpen());
        assertEquals(12, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(0).getHourOpen());
        OpeningHours openingHours2 = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.TAKEAWAY);
        assertEquals(9, openingHours2.getHours().get(DayOfWeek.MONDAY).get(0).getHourOpen());
        assertEquals(9, openingHours2.getHours().get(DayOfWeek.WEDNESDAY).get(0).getHourOpen());

        //sunday all day is closed
        hostOpeningHoursView.getHours().put(DayOfWeek.SUNDAY, new ArrayList<>());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostOpeningHoursView)
                .put("Restaurant/OpeningHours");

        assertSundayClosed(response);

        //test it works when there is no opening hours to begin with
        openingHoursRepository.deleteAll();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostOpeningHoursView)
                .put("Restaurant/OpeningHours");

        assertSundayClosed(response);
        assertEquals(1, openingHoursRepository.findAll().size());
    }

    @Test
    public void testPutOpeningHoursClosedAllDayIsCleaned1() throws Exception {
        String token = getTokenForStaff(staff1);
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        HostOpeningHoursView hostOpeningHoursView = new HostOpeningHoursView(openingHours);
        hostOpeningHoursView.getHours().get(DayOfWeek.WEDNESDAY).add(new HourSpan(0,0,24,0));
        hostOpeningHoursView.getHours().get(DayOfWeek.WEDNESDAY).add(new HourSpan(22,0,23,0));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostOpeningHoursView)
                .put("Restaurant/OpeningHours");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        assertEquals(0, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(0).getHourOpen());
        assertEquals(24, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(0).getHourClose());
        assertEquals(1,openingHours.getHours().get(DayOfWeek.WEDNESDAY).size());

        //monday is unaffected
        assertEquals(9, openingHours.getHours().get(DayOfWeek.MONDAY).get(0).getHourOpen());
    }

    @Test
    public void testPutOpeningHoursClosedAllDayIsCleaned2() throws Exception {
        String token = getTokenForStaff(staff1);
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        HostOpeningHoursView hostOpeningHoursView = new HostOpeningHoursView(openingHours);
        hostOpeningHoursView.getHours().get(DayOfWeek.WEDNESDAY).clear();
        hostOpeningHoursView.getHours().get(DayOfWeek.WEDNESDAY).add(new HourSpan(9,0,12,0));
        hostOpeningHoursView.getHours().get(DayOfWeek.WEDNESDAY).add(new HourSpan(18,0,24,0));

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostOpeningHoursView)
                .put("Restaurant/OpeningHours");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        assertEquals(9, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(0).getHourOpen());
        assertEquals(12, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(0).getHourClose());
        assertEquals(18, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(1).getHourOpen());
        assertEquals(24, openingHours.getHours().get(DayOfWeek.WEDNESDAY).get(1).getHourClose());
        assertEquals(2,openingHours.getHours().get(DayOfWeek.WEDNESDAY).size());

        //monday is unaffected
        assertEquals(9, openingHours.getHours().get(DayOfWeek.MONDAY).get(0).getHourOpen());
    }

    private void assertSundayClosed(Response response) {
        OpeningHours openingHours;
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        assertEquals(0, openingHours.getHours().get(DayOfWeek.SUNDAY).size());
    }

    @Test
    public void testGetClosures() throws Exception {
        String token = getTokenForStaff(staff1);
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.TAKEAWAY.toString())
                .get("Restaurant/AbsoluteClosures");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        HostClosuresView hostClosuresView = response.getBody().as(HostClosuresView.class, ObjectMapperType.JACKSON_2);
        assertEquals(0, hostClosuresView.getClosures().size());

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.TAKEAWAY.toString())
                .queryParam("archived", true)
                .get("Restaurant/AbsoluteClosures");


        hostClosuresView = response.getBody().as(HostClosuresView.class, ObjectMapperType.JACKSON_2);
        assertEquals(2, hostClosuresView.getClosures().size());
        assertEquals("2017-06-28 01:00",hostClosuresView.getClosures().get(0).getStart());
        assertEquals("2017-06-29 01:00",hostClosuresView.getClosures().get(0).getEnd());
        assertEquals("2017-03-01 00:00",hostClosuresView.getClosures().get(1).getStart());
        assertEquals("2017-03-02 00:00",hostClosuresView.getClosures().get(1).getEnd());
    }

    @Test
    public void testPutClosures() throws Exception {
        String token = getTokenForStaff(staff1);
        staff1.setRole(StaffRole.MANAGER);
        staffRepository.save(staff1);

        OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
        HostClosuresView hostClosuresView = new HostClosuresView(restaurant1.getIANATimezone(), openingHours.getAbsoluteBlackouts());

        hostClosuresView.getClosures().get(0).setStart("2017-06-28 00:00");
        hostClosuresView.getClosures().get(0).setEnd("2017-06-29 00:00");

        Response response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostClosuresView)
                .put("Restaurant/AbsoluteClosures");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertClosuresEqual(true,true);

        //test it works when there is no opening hours to begin with
        openingHoursRepository.deleteAll();

        response = given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .queryParam("type", BookingType.RESERVATION.toString())
                .body(hostClosuresView)
                .put("Restaurant/AbsoluteClosures");

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        assertClosuresEqual(true,false);
        assertEquals(1, openingHoursRepository.findAll().size());
    }

    private void assertClosuresEqual(boolean checkReservations, boolean checkTakeaways) {
        if(checkReservations) {
            OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.RESERVATION);
            assertEquals(1498608000000L - (1000 * 60 * 60), openingHours.getAbsoluteBlackouts().get(0).getStart());
            assertEquals((1498608000000L + (1000 * 60 * 60 * 24)) - (1000 * 60 * 60), openingHours.getAbsoluteBlackouts().get(0).getEnd());
        }

        if(checkTakeaways) {
            OpeningHours openingHours = openingHoursRepository.findByRestaurantIdAndBookingType(restaurant1.getId(), BookingType.TAKEAWAY);
            assertEquals(1498608000000L, openingHours.getAbsoluteBlackouts().get(1).getStart());
            assertEquals((1498608000000L + (1000 * 60 * 60 * 24)), openingHours.getAbsoluteBlackouts().get(1).getEnd());
        }
    }
}