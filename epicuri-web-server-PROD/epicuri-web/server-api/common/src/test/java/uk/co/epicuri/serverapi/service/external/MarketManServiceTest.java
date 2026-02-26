package uk.co.epicuri.serverapi.service.external;

import de.flapdoodle.embed.process.collections.Collections;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.AuthorizationResponse;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.engines.CashUpAggregator;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import static org.junit.Assert.*;

@Ignore
public class MarketManServiceTest extends SessionSetupBaseIT {
    @Autowired
    private MarketManService marketManService;

    @Before
    public void setUp() throws Exception{
        super.setUp();

        KVData data = new KVData();
        data.setKey("267a3f1528f0a87501ec259003808b14");
        data.setPassword("89a0ae3d11d14fa5b22f73ce92d4ebd1");
        restaurant1.getIntegrations().put(ExternalIntegration.MARKET_MAN, data);
        restaurant1 = restaurantRepository.save(restaurant1);
    }

    @Test
    public void testUpdateOrAcquireToken() {
        Restaurant restaurant = restaurantRepository.findOne(restaurant1.getId());
        assertTrue(restaurant.getIntegrations().get(ExternalIntegration.MARKET_MAN).getTokenExpiration() < 0);
        assertNull(restaurant.getIntegrations().get(ExternalIntegration.MARKET_MAN).getToken());

        marketManService.updateOrAcquireToken(restaurant1);

        restaurant = restaurantRepository.findOne(restaurant1.getId());
        assertFalse(restaurant.getIntegrations().get(ExternalIntegration.MARKET_MAN).getTokenExpiration() < 0);
        assertNotNull(restaurant.getIntegrations().get(ExternalIntegration.MARKET_MAN).getToken());
    }

    @Test
    public void testAcquireToken() {
        String key = "267a3f1528f0a87501ec259003808b14";
        String password = "89a0ae3d11d14fa5b22f73ce92d4ebd1";

        AuthorizationResponse response = marketManService.acquireToken(key, password);
        assertTrue(response.isSuccess());
        assertNull(response.getErrorMessage());
        assertNotNull(response.getToken());
    }

    @Test
    public void testUpdateSales() throws Exception {
        setUpSession();

        String key = "267a3f1528f0a87501ec259003808b14";
        String password = "89a0ae3d11d14fa5b22f73ce92d4ebd1";
        AuthorizationResponse response = marketManService.acquireToken(key,password);

        CashUpAggregator aggregator = new CashUpAggregator(sessionCalculationService);
        aggregator.addSession(session1, Collections.newArrayList(order1, order2, order3));
        aggregator.aggregate();

        marketManService.updateSales(response.getToken(), String.valueOf(System.currentTimeMillis()), System.currentTimeMillis()-1, System.currentTimeMillis(), aggregator);
    }
}