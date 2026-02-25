package uk.co.epicuri.serverapi.service.external;

import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.AsyncCommunicationsService;

import static org.junit.Assert.*;

public class EmailServiceTest extends BaseIT {
    @Autowired
    private EmailService emailService;

    @Autowired
    private AsyncCommunicationsService asyncCommunicationsService;

    @Test
    public void sendHtml() {
        assertTrue(emailService.htmlEmail("test", "epicuriblackhole@mailinator.com", "noreply", "noreply@epicuri.email", "<html><strong>strong</strong>body</html>").build());
    }

    @Test
    public void sendSimple() {
        assertTrue(emailService.simpleEmail("test", "epicuriblackhole@mailinator.com", "noreply", "noreply@epicuri.email", "weak body").build());
    }

    /*@Test
    public void temp() {
        Whitebox.setInternalState(emailService,"mailgunApiKey","key-40c450e79f813169a600b6e155e94b5c");
        Whitebox.setInternalState(emailService,"mailgunDomain","epicuri.email");

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Black Horse");
        Address address = new Address();
        address.setCity("West Sussex");
        address.setPostCode("BN18 9NL");
        address.setStreet("High St");
        address.setTown("Amberley");
        restaurant.setAddress(address);
        LatLongPair latLongPair = new LatLongPair(50.908741, -0.533442);
        restaurant.setPosition(latLongPair);
        restaurant.setIANATimezone("Europe/London");
        restaurant.setPhoneNumber1("01798 - 831181");
        restaurant.setPublicEmailAddress("book@amberleyblackhorse.co.uk");

        Booking booking = new Booking();
        booking.setTargetTime(1543696200000L);
        booking.setName("Jo");
        booking.setEmail("joanna.morey@gmail.com");
        booking.setNumberOfPeople(4);

        asyncCommunicationsService.sendEmailToDiner(booking.getName(), booking.getEmail(), booking, restaurant);
    }*/
}