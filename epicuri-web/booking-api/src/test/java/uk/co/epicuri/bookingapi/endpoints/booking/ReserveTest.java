package uk.co.epicuri.bookingapi.endpoints.booking;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.api.core.pojo.ReservationRequest;
import uk.co.epicuri.api.core.pojo.Restaurant;
import uk.co.epicuri.bookingapi.endpoints.booking.Reserve;
import uk.co.epicuri.bookingapi.endpoints.booking.Statics;
import uk.co.epicuri.bookingapi.endpoints.booking.TimeSlots;
import uk.co.epicuri.bookingapi.pojo.BookingRequest;
import uk.co.epicuri.bookingapi.pojo.BookingStatics;
import uk.co.epicuri.bookingapi.pojo.EmailConfirmRequest;
import uk.co.epicuri.bookingapi.pojo.TimeSlotsRequest;

import javax.ws.rs.WebApplicationException;
import java.util.concurrent.TimeUnit;

public class ReserveTest {

    @Test
    public void testReserve() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        QConnection tickerplantdb = new QBasicConnection("localhost",7000,"","");
        securitydb.open();
        staticsdb.open();
        tickerplantdb.open();

        //QConnection tickdb = new QBasicConnection("localhost",7000,null,null);
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Statics statics = new Statics(api,securitydb,staticsdb);

        BookingStatics bookingStaticsRequest = new BookingStatics(){
            {
                setLanguage("en");
            }
        };

        final BookingStatics bookingStatics = statics.statics("5", bookingStaticsRequest);

        TimeSlots timeSlots = new TimeSlots(api,securitydb,staticsdb);

        final DateTime dateTime = new DateTime(DateTimeZone.forID("Europe/London"));
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy.MM.dd");
        TimeSlotsRequest timeSlotsRequest = new TimeSlotsRequest(){
            {
                setDate(dtf.print(dateTime));
                setToken(bookingStatics.getToken());
            }
        };
        timeSlots.timeSlots("5",timeSlotsRequest, "Europe/London");

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setTime("19:30");
        bookingRequest.setTelephone("1234");
        bookingRequest.setEmail("manni.patel@gmail.com");
        bookingRequest.setName("manish");
        bookingRequest.setToken(bookingStaticsRequest.getToken());
        bookingRequest.setNumberOfPeople(1);
        bookingRequest.setDate(dtf.print(dateTime));

        Reserve reserve = new Reserve(api,securitydb,tickerplantdb,staticsdb);
        reserve.reserve("5",bookingRequest,null);
    }

    @Test
    public void testReserveNoCommit() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        QConnection tickerplantdb = new QBasicConnection("localhost",7000,"","");
        securitydb.open();
        staticsdb.open();
        tickerplantdb.open();

        //QConnection tickdb = new QBasicConnection("localhost",7000,null,null);
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Statics statics = new Statics(api,securitydb,staticsdb);

        BookingStatics bookingStaticsRequest = new BookingStatics(){
            {
                setLanguage("en");
            }
        };

        final BookingStatics bookingStatics = statics.statics("5", bookingStaticsRequest);

        TimeSlots timeSlots = new TimeSlots(api,securitydb,staticsdb);

        final DateTime dateTime = new DateTime(DateTimeZone.forID("Europe/London")).plusDays(1);
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy.MM.dd");
        TimeSlotsRequest timeSlotsRequest = new TimeSlotsRequest(){
            {
                setDate(dtf.print(dateTime));
                setToken(bookingStatics.getToken());
            }
        };
        timeSlots.timeSlots("5",timeSlotsRequest, "Europe/London");

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setTime("19:30");
        bookingRequest.setToken(bookingStaticsRequest.getToken());
        bookingRequest.setDate(dtf.print(dateTime));
        Reserve reserve = new Reserve(api,securitydb,tickerplantdb,staticsdb);
        reserve.reservecheck("5", bookingRequest, null);
        //reserve.reserve("5", bookingRequest, null);
    }

    @Test(expected = WebApplicationException.class)
    public void testReservePast() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        QConnection tickerplantdb = new QBasicConnection("localhost",7000,"","");
        securitydb.open();
        staticsdb.open();
        tickerplantdb.open();

        //QConnection tickdb = new QBasicConnection("localhost",7000,null,null);
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.PROD);
        Statics statics = new Statics(api,securitydb,staticsdb);

        BookingStatics bookingStaticsRequest = new BookingStatics(){
            {
                setLanguage("en");
            }
        };

        final BookingStatics bookingStatics = statics.statics("5", bookingStaticsRequest);

        TimeSlots timeSlots = new TimeSlots(api,securitydb,staticsdb);

        final DateTime dateTime = new DateTime(DateTimeZone.forID("Europe/London")).minusDays(2);
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy.MM.dd");
        TimeSlotsRequest timeSlotsRequest = new TimeSlotsRequest(){
            {
                setDate(dtf.print(dateTime));
                setToken(bookingStatics.getToken());
            }
        };
        timeSlots.timeSlots("5",timeSlotsRequest, "Europe/London");

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setTime(timeSlotsRequest.getTimes().get(timeSlotsRequest.getTimes().size()-1));
        bookingRequest.setTelephone("1234");
        bookingRequest.setEmail("manni.patel@gmail.com");
        bookingRequest.setName("manish");
        bookingRequest.setToken(bookingStaticsRequest.getToken());
        bookingRequest.setNumberOfPeople(1);
        bookingRequest.setDate(dtf.print(dateTime));

        Reserve reserve = new Reserve(api,securitydb,tickerplantdb,staticsdb);
        reserve.reserve("5", bookingRequest, null);
    }

    @Test
    public void testReserveConfirmation() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        QConnection tickerplantdb = new QBasicConnection("localhost",7000,"","");
        securitydb.open();
        staticsdb.open();
        tickerplantdb.open();
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);

        Restaurant restaurant = new Restaurant() {
            {
                setAddress(new Restaurant.Address() {
                               {
                                   setStreet("Test Street");
                                   setTown("Test Town");
                                   setCity("Test City");
                                   setPostCode("TEST POSTCODE");
                               }
                           }
                );
                setTimezone("Europe/London");
                setPhoneNumber("0123456789");
                setEmail("info@epicuri.co.uk");
                setDescription("Test Description");
                setName("Test Restaurant");
                setId(5);
            }
        };
        ReservationRequest request = new ReservationRequest(){
            {
                setName("Test Diner Name");
                setTelephone("0798999999999");
                setNotes("Test Note");
                setReservationTime((System.currentTimeMillis() + (1000*60*4)));
                setNumberOfPeople(2);
                setEmail("manni.patel@gmail.com");
            }
        };

        EmailConfirmRequest confirmRequest = new EmailConfirmRequest();
        confirmRequest.setLanguage("en");
        confirmRequest.setReservationRequest(request);
        confirmRequest.setRestaurant(restaurant);

        Reserve reserve = new Reserve(api,securitydb,tickerplantdb,staticsdb);
        reserve.emailConfirmation("5",confirmRequest);

        confirmRequest.setLanguage("nl");
        reserve.emailConfirmation("5",confirmRequest);


    }
}