package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class BookingRepositoryImplTest extends BaseIT {

    @Test
    public void testPushAccept() throws Exception {
        assert !(booking1.isAccepted());
        assert !(booking2.isAccepted());
        assert !(booking3.isAccepted());

        bookingRepository.pushAccept(booking1.getId(), true);
        bookingRepository.pushAccept(booking2.getId(), true);
        bookingRepository.pushAccept(booking3.getId(), true);

        assertTrue(bookingRepository.findOne(booking1.getId()).isAccepted());
        assertTrue(bookingRepository.findOne(booking2.getId()).isAccepted());
        assertTrue(bookingRepository.findOne(booking3.getId()).isAccepted());

        bookingRepository.pushAccept(booking2.getId(), false);
        assertFalse(bookingRepository.findOne(booking2.getId()).isAccepted());
    }

    @Test
    public void testPushReject() throws Exception {
        assert !(booking1.isRejected());
        assert !(booking2.isRejected());
        assert !(booking3.isRejected());

        bookingRepository.pushReject(booking1.getId(), true, null);
        bookingRepository.pushReject(booking2.getId(), true, "foo");
        bookingRepository.pushReject(booking3.getId(), true, "bar");

        Booking updated1 = bookingRepository.findOne(booking1.getId());
        Booking updated2 = bookingRepository.findOne(booking2.getId());
        Booking updated3 = bookingRepository.findOne(booking3.getId());
        assertTrue(updated1.isRejected());
        assertNull(updated1.getRejectionNotice());
        assertTrue(updated2.isRejected());
        assertEquals("foo", updated2.getRejectionNotice());
        assertTrue(updated3.isRejected());
        assertEquals("bar", updated3.getRejectionNotice());

        bookingRepository.pushReject(booking2.getId(), false, null);
        updated2 = bookingRepository.findOne(booking2.getId());
        assertFalse(updated2.isRejected());
        assertNull(updated2.getRejectionNotice());
    }

    @Test
    public void testPushCancel() throws Exception {
        assert !(booking1.isCancelled());
        assert !(booking2.isCancelled());
        assert !(booking3.isCancelled());

        bookingRepository.pushCancel(booking1.getId(), true);
        bookingRepository.pushCancel(booking2.getId(), true);
        bookingRepository.pushCancel(booking3.getId(), true);

        assertTrue(bookingRepository.findOne(booking1.getId()).isCancelled());
        assertTrue(bookingRepository.findOne(booking2.getId()).isCancelled());
        assertTrue(bookingRepository.findOne(booking3.getId()).isCancelled());

        bookingRepository.pushCancel(booking2.getId(), false);
        assertFalse(bookingRepository.findOne(booking2.getId()).isCancelled());
    }

    @Test
    public void testFind1() throws Exception {
        booking1.setTargetTime(5);
        booking2.setTargetTime(10);
        booking3.setTargetTime(10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> bookings = bookingRepository.find(restaurant1.getId(),4,11, BookingType.RESERVATION,true);
        assertEquals(1, bookings.size());
        bookings = bookingRepository.find(restaurant1.getId(),5,10, BookingType.RESERVATION,true);
        assertEquals(1, bookings.size());
        bookings = bookingRepository.find(restaurant1.getId(),6,10, BookingType.RESERVATION,true);
        assertEquals(0, bookings.size());
        bookings = bookingRepository.find(restaurant1.getId(),4,11, BookingType.TAKEAWAY,true);
        assertEquals(2, bookings.size());

        booking1.setRejected(true);
        booking2.setRejected(true);
        booking3.setRejected(true);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        bookings = bookingRepository.find(restaurant1.getId(),4,11, BookingType.RESERVATION,true);
        assertEquals(1, bookings.size());
        bookings = bookingRepository.find(restaurant1.getId(),4,11, BookingType.RESERVATION,false);
        assertEquals(0, bookings.size());
    }

    @Test
    public void testFind2() throws Exception {
        booking1.setTargetTime(5);
        booking1.setCustomerId(customer1.getId());
        booking2.setBookingType(BookingType.RESERVATION);
        booking2.setTargetTime(10);
        booking2.setCustomerId(customer1.getId());
        booking3.setTargetTime(10);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> bookings = bookingRepository.find(customer1.getId(),4,11,BookingType.RESERVATION);
        assertEquals(2, bookings.size());
        bookings = bookingRepository.find("foobar",4,11,BookingType.RESERVATION);
        assertEquals(0, bookings.size());
        bookings = bookingRepository.find(customer1.getId(),100,200,BookingType.RESERVATION);
        assertEquals(0, bookings.size());
        bookings = bookingRepository.find(customer1.getId(),4,11,BookingType.TAKEAWAY);
        assertEquals(0, bookings.size());
    }

    @Test
    public void testFind2NoBookingType() throws Exception {
        booking1.setTargetTime(5);
        booking1.setCustomerId(customer1.getId());
        booking1.setBookingType(BookingType.TAKEAWAY);
        booking2.setTargetTime(10);
        booking2.setCustomerId(customer1.getId());
        booking2.setBookingType(BookingType.RESERVATION);
        booking3.setTargetTime(10);
        booking3.setCustomerId(customer1.getId());
        booking3.setBookingType(BookingType.RESERVATION);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
        bookingRepository.save(booking3);

        List<Booking> bookings = bookingRepository.find(customer1.getId(),4,11,null);
        assertEquals(3, bookings.size());
    }

    @Test
    public void testFindOne() throws Exception {
        booking1.setCancelled(true);
        booking1.setDeleted(0L);
        booking2.setCancelled(true);

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);

        Booking test1 = bookingRepository.findOne(booking1.getId(),true,true);
        assertNotNull(test1);
        Booking test2 = bookingRepository.findOne(booking1.getId(),false,true);
        assertNull(test2);
        Booking test3 = bookingRepository.findOne(booking1.getId(),true,false);
        assertNull(test3);
        Booking test4 = bookingRepository.findOne(booking2.getId(),true,true);
        assertNotNull(test4);
        Booking test5 = bookingRepository.findOne(booking2.getId(),true,false);
        assertNull(test5);
    }
}