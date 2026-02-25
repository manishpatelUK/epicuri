package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import uk.co.epicuri.serverapi.common.pojo.model.session.Booking;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class DeletableRepositoryImplTest extends BaseIT {

    @Autowired
    MongoOperations operations;

    @Test
    public void testMarkDeleted1() throws Exception {
        List<String> list = new ArrayList<>();
        list.add(booking1.getId());
        list.add(booking3.getId());

        DeletableRepositoryImpl.markDeleted(operations,list, Booking.class);

        assertNotNull(bookingRepository.findOne(booking1.getId()).getDeleted());
        assertNotNull(bookingRepository.findOne(booking3.getId()).getDeleted());
        assertNull(bookingRepository.findOne(booking2.getId()).getDeleted());
    }

    @Test
    public void testMarkDeleted2() throws Exception {
        DeletableRepositoryImpl.markDeleted(operations,booking1.getId(), Booking.class);
        DeletableRepositoryImpl.markDeleted(operations,booking3.getId(), Booking.class);

        assertNotNull(bookingRepository.findOne(booking1.getId()).getDeleted());
        assertNotNull(bookingRepository.findOne(booking3.getId()).getDeleted());
        assertNull(bookingRepository.findOne(booking2.getId()).getDeleted());
    }

    @Test
    public void testFindOneNotDeleted() throws Exception {
        booking1.setDeleted(0L);
        bookingRepository.save(booking1);

        assertNull(DeletableRepositoryImpl.findOneNotDeleted(operations,booking1.getId(),Booking.class));
        assertNotNull(DeletableRepositoryImpl.findOneNotDeleted(operations,booking2.getId(),Booking.class));
    }
}