package uk.co.epicuri.serverapi.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.repository.BaseIT;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
public class GoogleMapsServiceTest extends BaseIT {

    @Autowired
    GoogleMapsService service;

    private Address validAddress1;
    private Address validAddress2;
    private Address invalidAddress1;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        validAddress1 = new Address();
        validAddress1.setCity("Novi Sad");
        validAddress1.setTown("Novi Sad");
        validAddress1.setStreet("Mise Dimitrijevica 2");
        validAddress1.setPostcode("21000");

        validAddress2 = new Address();
        validAddress2.setCity("Beograd");
        validAddress2.setTown("Beograd");
        validAddress2.setStreet("Jurija Gagarina 20");
        validAddress2.setPostcode("11000");

        invalidAddress1 = new Address();
        invalidAddress1.setCity("ABC");
        invalidAddress1.setTown("DEF");
        invalidAddress1.setStreet("GHI");
    }


    @Ignore
    @Test
    public void testGetDistanceMiles() throws Exception {
        Double distance = service.getDistanceMiles(validAddress1, validAddress2);
        assertEquals(57.27797878, distance, .1);
    }

    @Ignore
    @Test
    public void testIsAddressValid() throws Exception {
        assertTrue(service.isAddressValid(validAddress1));
        assertTrue(service.isAddressValid(validAddress2));
        assertFalse(service.isAddressValid(invalidAddress1));
    }
}