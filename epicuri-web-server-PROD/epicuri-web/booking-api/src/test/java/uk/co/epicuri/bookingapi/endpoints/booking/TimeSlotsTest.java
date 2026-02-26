package uk.co.epicuri.bookingapi.endpoints.booking;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.bookingapi.endpoints.booking.Statics;
import uk.co.epicuri.bookingapi.endpoints.booking.TimeSlots;
import uk.co.epicuri.bookingapi.pojo.BookingStatics;
import uk.co.epicuri.bookingapi.pojo.TimeSlotsRequest;

import javax.ws.rs.WebApplicationException;

public class TimeSlotsTest {

    @Test
    public void testTimeSlots() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        securitydb.open();
        staticsdb.open();

        //QConnection tickdb = new QBasicConnection("localhost",7000,null,null);
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);
        TimeSlots timeSlots = new TimeSlots(api,securitydb,staticsdb);
        Statics statics = new Statics(api,securitydb,staticsdb);

        final BookingStatics staticsRequest = new BookingStatics(){
            {
                setLanguage("en");
            }
        };
        final BookingStatics bookingStatics = statics.statics("5", staticsRequest);


        final DateTime dateTime1 = new DateTime(DateTimeZone.forID("Europe/London")).plusDays(1);
        TimeSlotsRequest request = getTimeSlotsRequest(timeSlots, bookingStatics, dateTime1);

        Assert.assertTrue(request.getTimes().get(0).equals("10:00"));
        Assert.assertTrue(request.getTimes().get(request.getTimes().size()-1).equals("22:30"));

        final DateTime dateTime2 = new DateTime(DateTimeZone.forID("Europe/London"));
        TimeSlotsRequest request2 = getTimeSlotsRequest(timeSlots, bookingStatics, dateTime2);
        Assert.assertTrue(request2.getTimes().size()<25);
    }

    @Test(expected = WebApplicationException.class)
    public void testTimeSlotsPast() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        securitydb.open();
        staticsdb.open();

        //QConnection tickdb = new QBasicConnection("localhost",7000,null,null);
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);
        TimeSlots timeSlots = new TimeSlots(api,securitydb,staticsdb);
        Statics statics = new Statics(api,securitydb,staticsdb);

        final BookingStatics staticsRequest = new BookingStatics(){
            {
                setLanguage("en");
            }
        };
        final BookingStatics bookingStatics = statics.statics("5", staticsRequest);


        final DateTime dateTime1 = new DateTime(DateTimeZone.forID("Europe/London")).minusDays(1);
        TimeSlotsRequest request = getTimeSlotsRequest(timeSlots, bookingStatics, dateTime1);
    }

    private static TimeSlotsRequest getTimeSlotsRequest(TimeSlots timeSlots, final BookingStatics bookingStatics, final DateTime dateTime) {
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy.MM.dd");
        TimeSlotsRequest request = new TimeSlotsRequest(){
            {
                setDate(dtf.print(dateTime));
                setToken(bookingStatics.getToken());
            }
        };
        timeSlots.timeSlots("5",request, "Europe/London");
        return request;
    }
}