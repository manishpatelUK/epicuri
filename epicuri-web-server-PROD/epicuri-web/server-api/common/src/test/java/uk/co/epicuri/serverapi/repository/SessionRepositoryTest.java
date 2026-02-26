package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class SessionRepositoryTest extends BaseIT {

    @Test
    public void testFindByRestaurantIdAndOriginalBookingIdIn() throws Exception {
        session1.setOriginalBooking(booking1);
        session1.setRestaurantId(restaurant1.getId());
        session2.setOriginalBooking(booking2);
        session2.setRestaurantId(restaurant1.getId());
        session3.setOriginalBooking(booking3);
        session3.setRestaurantId(restaurant2.getId());

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);


        List<String> ids = new ArrayList<>();
        ids.add(booking1.getId());
        ids.add(booking2.getId());
        ids.add(booking3.getId());
        ids.add("foo");

        List<Session> list1 = sessionRepository.findByRestaurantIdAndOriginalBookingIdIn(restaurant1.getId(), ids);
        List<Session> list2 = sessionRepository.findByRestaurantIdAndOriginalBookingIdIn(restaurant2.getId(), ids);
        ids.remove(booking2.getId());
        List<Session> list3 = sessionRepository.findByRestaurantIdAndOriginalBookingIdIn(restaurant1.getId(), ids);
        List<Session> list4 = sessionRepository.findByRestaurantIdAndOriginalBookingIdIn(restaurant1.getId(), new ArrayList<>());

        assertEquals(2, list1.size());
        assertTrue(list1.stream().map(Session::getId).collect(Collectors.toList()).contains(session1.getId()));
        assertTrue(list1.stream().map(Session::getId).collect(Collectors.toList()).contains(session2.getId()));

        assertEquals(1, list2.size());
        assertEquals(session3.getId(), list2.get(0).getId());

        assertEquals(1, list3.size());
        assertEquals(session1.getId(), list3.get(0).getId());

        assertEquals(0, list4.size());
    }

    @Test
    public void testFindByRestaurantId() throws Exception {
        session1.setRestaurantId(restaurant1.getId());
        session2.setRestaurantId(restaurant1.getId());
        session3.setRestaurantId(restaurant2.getId());

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);

        List<Session> list1 = sessionRepository.findByRestaurantId(restaurant1.getId());
        assertEquals(2, list1.size());
        assertTrue(list1.stream().map(Session::getId).collect(Collectors.toList()).contains(session1.getId()));
        assertTrue(list1.stream().map(Session::getId).collect(Collectors.toList()).contains(session2.getId()));
    }

    @Test
    public void testFindByOriginalBookingIdIn() throws Exception {
        session1.setOriginalBooking(booking1);
        session1.setRestaurantId(restaurant1.getId());
        session2.setOriginalBooking(booking2);
        session2.setRestaurantId(restaurant1.getId());
        session3.setOriginalBooking(booking3);
        session3.setRestaurantId(restaurant2.getId());

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);


        List<String> ids = new ArrayList<>();
        ids.add(booking1.getId());
        ids.add(booking2.getId());
        ids.add(booking3.getId());
        ids.add("foo");

        List<Session> list1 = sessionRepository.findByOriginalBookingIdIn(ids);
        ids.remove(booking2.getId());
        List<Session> list2 = sessionRepository.findByOriginalBookingIdIn(ids);
        List<Session> list3 = sessionRepository.findByOriginalBookingIdIn(new ArrayList<>());

        assertEquals(3, list1.size());
        assertEquals(session1.getId(), list1.get(0).getId());

        assertEquals(2, list2.size());
        assertEquals(session1.getId(), list2.get(0).getId());
        assertEquals(session3.getId(), list2.get(1).getId());

        assertEquals(0, list3.size());
    }

    @Test
    public void testFindByOriginalPartyId() throws Exception {
        session1.setOriginalParty(party1);
        session2.setOriginalParty(party2);

        sessionRepository.save(session1);
        sessionRepository.save(session2);

        Session s1 = sessionRepository.findByOriginalPartyId(party1.getId());
        Session s2 = sessionRepository.findByOriginalPartyId(party2.getId());
        Session s3 = sessionRepository.findByOriginalPartyId("foo");

        assertEquals(session1.getId(), s1.getId());
        assertEquals(session2.getId(), s2.getId());
        assertNull(s3);
    }

    @Test
    public void testFindByOriginalPartyIdIn() throws Exception {
        session1.setOriginalParty(party1);
        session2.setOriginalParty(party2);
        session3.setOriginalParty(party3);

        sessionRepository.save(session1);
        sessionRepository.save(session2);
        sessionRepository.save(session3);


        List<String> ids = new ArrayList<>();
        ids.add(party1.getId());
        ids.add(party2.getId());
        ids.add(party3.getId());
        ids.add("foo");

        List<Session> list1 = sessionRepository.findByOriginalPartyIdIn(ids);
        ids.remove(party2.getId());
        List<Session> list2 = sessionRepository.findByOriginalPartyIdIn(ids);
        List<Session> list3 = sessionRepository.findByOriginalPartyIdIn(new ArrayList<>());

        assertEquals(3, list1.size());
        assertEquals(session1.getId(), list1.get(0).getId());
        assertEquals(session2.getId(), list1.get(1).getId());
        assertEquals(session3.getId(), list1.get(2).getId());

        assertEquals(2, list2.size());
        assertEquals(session1.getId(), list2.get(0).getId());
        assertEquals(session3.getId(), list2.get(1).getId());

        assertEquals(0, list3.size());
    }
}