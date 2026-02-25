package uk.co.epicuri.serverapi.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsChargeService;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsChargeServiceResponse;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsCustomer;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsTaxations;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by manish
 */
@Ignore
public class MewsServiceTest extends BaseIT {

    //private String accessToken = "C66EF7B239D24632943D115EDE9CB810-EA00F8FD8294692C940F6B5A8F9453D";
    private String accessToken = "CC150C355D6A4048A220AD20015483AB-B6D09C0C84B09538077CB8FFBB907B4";
    //private String accessToken = "5F56B9903A834F199E28AD20015E58CA-5C6A1A00550634911534AD6A098E8B7";
    //private String accessToken = "39E301DD5A1C4A569087AD20015F60DD-50DC28896E9090CCA0995C9BBD90351";

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
        assertTrue(collection2.size() > 0);

        MewsCustomer captured = collection2.iterator().next();
        assertNotNull(captured.getId());
    }

    @Test
    public void testCharge() throws Exception {
        Collection<MewsCustomer> collection1 = mewsService.getCustomers(null, accessToken, "", "");
        String customerId = collection1.iterator().next().getId();

        MewsChargeServiceResponse services = mewsService.getServices(null, accessToken);
        String serviceId = services.getServices().stream().filter(MewsChargeService::isActive).findFirst().orElse(null).getId();

        //String serviceId = "bc69c610-f0f8-4645-8bb3-ab3a00c97c1e";

        String currency = "GBP";
        setUpOrders();

        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        Map<ItemType,String> accountingCategories = new HashMap<>();
        accountingCategories.put(ItemType.FOOD, "136487be-df51-410c-b2f1-ad750164f1c0");
        accountingCategories.put(ItemType.DRINK, "fa2a164a-4974-484e-9536-ad75016472c7");
        accountingCategories.put(ItemType.OTHER, "bdca04a3-067b-4357-ab43-ac9a0109667b");

        String product = "9d3f62ab-49d4-44d0-8395-ae7200ad4abb";

        restaurant1.getTaxMappings().add(tax1.getId()+",foobar1");
        restaurant1.getTaxMappings().add(tax2.getId()+",foobar2");
        restaurant1.getTaxMappings().add(tax3.getId()+",foobar3");
        restaurantRepository.save(restaurant1);

        String charge = mewsService.charge(null, accessToken, serviceId, accountingCategories,product,"some notes", orders, taxRateRepository.findAll(), customerId, currency, restaurant1);

        assertNotNull(charge);
    }

    @Test
    public void testGetServices() throws Exception {
        MewsChargeServiceResponse services = mewsService.getServices(null, accessToken);
        assertTrue(services.getServices().size() > 0);
    }

    @Test
    public void testGetTaxations() throws Exception {
        MewsTaxations taxations = mewsService.getTaxations(null, accessToken);
        assertTrue(taxations.getTaxations().size() > 0);
        assertTrue(taxations.getTaxRates().size() > 0);
    }
}