package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.CheckIn;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class CheckInRepositoryImplTest extends BaseIT {

    @Test
    public void testUpdatePartyId() throws Exception {
        assert checkInRepository.findOne(checkIn1.getId()).getPartyId() == null;
        assert checkInRepository.findOne(checkIn2.getId()).getPartyId() == null;

        checkInRepository.updatePartyId(checkIn2.getId(), party1.getId());
        CheckIn c1 = checkInRepository.findOne(checkIn2.getId());
        CheckIn c2 = checkInRepository.findOne(checkIn1.getId());

        assertEquals(party1.getId(), c1.getPartyId());
        assertNull(c2.getPartyId());
    }

    @Test
    public void testUpdateCustomerId() throws Exception {
        assert checkInRepository.findOne(checkIn1.getId()).getCustomerId() == null;
        assert checkInRepository.findOne(checkIn2.getId()).getCustomerId() == null;

        checkInRepository.updateCustomerId(checkIn2.getId(), customer2.getId());
        CheckIn c1 = checkInRepository.findOne(checkIn2.getId());
        CheckIn c2 = checkInRepository.findOne(checkIn1.getId());

        assertEquals(customer2.getId(), c1.getCustomerId());
        assertNull(c2.getCustomerId());
    }

    @Test
    public void testUpdateSessionIdAndPartyId() {
        checkInRepository.updateSessionIdAndPartyId(checkIn1.getId(), "session", "party");

        CheckIn test = checkInRepository.findOne(checkIn1.getId());
        assertEquals("party", test.getPartyId());
        assertEquals("session", test.getSessionId());
    }
}