package uk.co.epicuri.serverapi.service.external;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.model.Address;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.List;

import static org.junit.Assert.*;

public class PostcodeLookupServiceTest extends BaseIT{

    @Autowired
    private PostcodeLookupService postcodeLookupService;

    @Ignore
    @Test
    public void testLookup() throws Exception {
        List<Address> addressList = postcodeLookupService.lookup("le7 9ud");
        assertTrue(addressList.size() > 0);
    }

    @Test
    public void testParseAddress() throws Exception {
        Address address = postcodeLookupService.parseAddress("le7 9ud", "10 Watkin Terrace, , , , , Northampton, Northamptonshire");
        assertEquals("10 Watkin Terrace", address.getStreet());

        address = postcodeLookupService.parseAddress("le7 9ud", "Flat 1, 6 Watkin Terrace, , , , Northampton, Northamptonshire");
        assertEquals("Flat 1, 6 Watkin Terrace", address.getStreet());

        address = postcodeLookupService.parseAddress("LE7 9UD", "Flat 1, Watkin Court, Watkin Terrace, foo, bar, Northampton, Northamptonshire");
        assertEquals("Flat 1, Watkin Court, Watkin Terrace, foo", address.getStreet());
        assertEquals("bar", address.getTown());
        assertEquals("Northampton", address.getCity());
        assertEquals("LE7 9UD", address.getPostcode());
    }
}