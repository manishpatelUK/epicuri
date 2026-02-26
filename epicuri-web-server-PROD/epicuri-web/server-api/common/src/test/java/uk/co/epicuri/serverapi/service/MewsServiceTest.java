package uk.co.epicuri.serverapi.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsCustomer;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
@Ignore
public class MewsServiceTest extends BaseIT {

    private String accessToken = "C66EF7B239D24632943D115EDE9CB810-EA00F8FD8294692C940F6B5A8F9453D";

    @Before
    public void setUp() throws Exception{
        super.setUp();

        tax3.setRate(200);
        taxRateRepository.save(tax3);
    }

    @Test
    public void testGetCustomers() throws Exception {
        Collection<MewsCustomer> collection1 = mewsService.getCustomers(null, accessToken, "", "");
        assertTrue(collection1.size() > 0);

        MewsCustomer mewsCustomer = collection1.iterator().next();

        Collection<MewsCustomer> collection2 = mewsService.getCustomers(null, accessToken, mewsCustomer.getRoomNumber(), mewsCustomer.getLastName());
        assertEquals(1, collection2.size());

        MewsCustomer captured = collection2.iterator().next();
        assertNotNull(captured.getId());
        assertNotNull(captured.getFirstName());
        assertNotNull(captured.getLastName());
    }

    @Test
    public void testCharge() throws Exception {
        Collection<MewsCustomer> collection1 = mewsService.getCustomers(null, accessToken, "", "");
        String customerId = collection1.iterator().next().getId();

        String currency = "GBP";
        setUpOrders();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        String charge = mewsService.charge(null, accessToken, "some notes", orders, taxRateRepository.findAll(), customerId, currency);

        assertNotNull(charge);
    }
}