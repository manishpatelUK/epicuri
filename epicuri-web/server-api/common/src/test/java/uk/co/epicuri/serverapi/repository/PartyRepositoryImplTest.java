package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.model.session.Party;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class PartyRepositoryImplTest extends BaseIT {

    @Test
    public void testUpdateCustomerId() throws Exception {
        assert partyRepository.findOne(party1.getId()).getCustomerId() == null;
        assert partyRepository.findOne(party2.getId()).getCustomerId() == null;

        partyRepository.updateCustomerId(party2.getId(), customer2.getId());
        Party p1 = partyRepository.findOne(party2.getId());
        Party p2 = partyRepository.findOne(party1.getId());

        assertEquals(customer2.getId(), p1.getCustomerId());
        assertNull(p2.getCustomerId());
    }
}