package uk.co.epicuri.bookingapi.endpoints.booking;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import org.junit.Assert;
import org.junit.Test;
import uk.co.epicuri.api.core.EpicuriAPI;
import uk.co.epicuri.bookingapi.endpoints.booking.Statics;
import uk.co.epicuri.bookingapi.pojo.BookingStatics;

public class StaticsTest {

    @Test
    public void testStatics() throws Exception {
        QConnection securitydb = new QBasicConnection("localhost",12000,"","");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"","");
        securitydb.open();
        staticsdb.open();

        //QConnection tickdb = new QBasicConnection("localhost",7000,null,null);
        EpicuriAPI api = new EpicuriAPI(EpicuriAPI.Environment.STAGING);
        Statics statics = new Statics(api,securitydb,staticsdb);

        BookingStatics request = new BookingStatics(){
            {
                setLanguage("en");
            }
        };
        BookingStatics bookingStatics = statics.statics("5", request);

        Assert.assertTrue(1 == (long)securitydb.sync("count select from passes where time > .z.p-0D00:00:05"));
        System.out.println(bookingStatics);
    }

}