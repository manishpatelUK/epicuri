package uk.co.epicuri.serverapi.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionNumber;

import static org.junit.Assert.*;

/**
 * Created by manish on 23/05/2017.
 */
public class SessionNumberRepositoryImplTest extends BaseIT {
    @Autowired
    private SessionNumberRepository sessionNumberRepository;

    @Test
    public void incrementAndGet() throws Exception {
        assertNull(sessionNumberRepository.findByRestaurantId(restaurant1.getId()));
        SessionNumber sessionNumber = sessionNumberRepository.incrementAndGet(restaurant1.getId());
        assertEquals(1, sessionNumber.getTotalSessionsCreated());
        sessionNumber = sessionNumberRepository.incrementAndGet(restaurant1.getId());
        assertEquals(2, sessionNumberRepository.findByRestaurantId(restaurant1.getId()).getTotalSessionsCreated());
        assertEquals(2, sessionNumber.getTotalSessionsCreated());
        sessionNumber = sessionNumberRepository.incrementAndGet(restaurant1.getId());
        assertEquals(3, sessionNumberRepository.findByRestaurantId(restaurant1.getId()).getTotalSessionsCreated());
        assertEquals(3, sessionNumber.getTotalSessionsCreated());
    }

}