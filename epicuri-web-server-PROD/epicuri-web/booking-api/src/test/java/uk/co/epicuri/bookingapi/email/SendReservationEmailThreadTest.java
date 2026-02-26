package uk.co.epicuri.bookingapi.email;

import com.exxeleron.qjava.QBasicConnection;
import com.exxeleron.qjava.QConnection;
import org.joda.time.DateTime;
import org.junit.Test;
import uk.co.epicuri.api.core.pojo.ReservationRequest;
import uk.co.epicuri.api.core.pojo.Restaurant;
import uk.co.epicuri.bookingapi.email.SendReservationEmailThread;

public class SendReservationEmailThreadTest {

    @Test
    public void testRun() throws Exception {

        QConnection tickerplantdb = new QBasicConnection("localhost",7000,"emailstp","13ghunda~Varad");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"statics","13ghunda~Akoti");
        tickerplantdb.open();
        staticsdb.open();

        sendMail(tickerplantdb, staticsdb);
    }

    private static void sendMail(QConnection tickerplantdb, QConnection staticsdb) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Silly Pizza");
        restaurant.setEmail("manni.patel@gmail.com");
        restaurant.setDescription("desc");
        Restaurant.Address address = new Restaurant.Address();
        address.setCity("Leicester");
        address.setPostCode("LE7 9UD");
        address.setStreet("Pulford Drive");
        address.setTown("Scraptoft");
        restaurant.setAddress(address);
        restaurant.setId(-1);
        restaurant.setPhoneNumber("01923450466");
        restaurant.setTimezone("Europe/London");

        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setName("Manish Patel");
        reservationRequest.setNotes("notes");
        reservationRequest.setNumberOfPeople(1);
        reservationRequest.setTelephone("123");
        reservationRequest.setReservationTime(0);
        reservationRequest.setEmail("manni.patel@gmail.com");
        SendReservationEmailThread thread1 = new SendReservationEmailThread(new DateTime(),reservationRequest,restaurant,tickerplantdb,staticsdb,"en");
        thread1.run();
        SendReservationEmailThread thread2 = new SendReservationEmailThread(new DateTime(),reservationRequest,restaurant,tickerplantdb,staticsdb,"nl");
        thread2.run();
    }

    @Test
    public void stressTest() throws Exception {
        QConnection tickerplantdb = new QBasicConnection("localhost",7000,"emailstp","13ghunda~Varad");
        QConnection staticsdb = new QBasicConnection("localhost",12100,"statics","13ghunda~Akoti");
        tickerplantdb.open();
        staticsdb.open();

        long start = System.currentTimeMillis();
        for(int i = 0; i < 50; i++) {
            sendMail(tickerplantdb,staticsdb);
        }

        System.out.print("Took " + (System.currentTimeMillis()-start) + "ms");
    }
}