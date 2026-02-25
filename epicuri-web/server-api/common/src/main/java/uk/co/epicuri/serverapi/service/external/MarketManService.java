package uk.co.epicuri.serverapi.service.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.*;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.CashUpKeys;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.CashUpAggregator;
import uk.co.epicuri.serverapi.engines.DateTimeConstants;
import uk.co.epicuri.serverapi.repository.RestaurantRepository;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.SessionCalculationService;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MarketManService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketManService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MasterDataService masterDataService;

    private static final String ENDPOINT = "https://api.marketman.com/v1/buyers/";
    private static final HttpHeaders STANDARD_HEADERS = createHeaders();

    private final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public KVData updateOrAcquireToken(Restaurant restaurant) {
        Map<ExternalIntegration, KVData> integrations = restaurant.getIntegrations();
        if(!integrations.containsKey(ExternalIntegration.MARKET_MAN)) {
            return null;
        }

        KVData kvData = integrations.get(ExternalIntegration.MARKET_MAN);
        if(kvData.getTokenExpiration() < System.currentTimeMillis() || kvData.getToken() == null) {
            AuthorizationResponse response = acquireToken(kvData.getKey(), kvData.getPassword());
            if(response == null) {
                return null;
            }

            kvData.setToken(response.getToken());
            kvData.setTokenExpiration(DateTimeConstants.convertToLong(FORMATTER, ZoneId.of("UTC"), response.getExpireDate(), null));
            restaurantRepository.updateIntegrations(restaurant.getId(), integrations);
        }

        return kvData;
    }

    public AuthorizationResponse acquireToken(String apiKey, String password) {
        AuthorizationRequest request = new AuthorizationRequest();
        request.setApiKey(apiKey);
        request.setApiPassword(password);
        HttpEntity<AuthorizationRequest> entity = new HttpEntity<>(request, STANDARD_HEADERS);

        ResponseEntity<AuthorizationResponse> responseEntity = restTemplate.exchange(
                ENDPOINT + "auth/GetToken",
                HttpMethod.POST,entity,AuthorizationResponse.class);

        if(responseEntity.getBody() != null && responseEntity.getBody().isSuccess()) {
            return responseEntity.getBody();
        }

        return null;
    }

    public boolean updateSales(String token, String uniqueId, long startTime, long endTime, CashUpAggregator cashUpAggregator) {
        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));
        List<Order> allOrders = cashUpAggregator.getAllOrders().values().stream().flatMap(List::stream).collect(Collectors.toList());
        Map<String, Integer> reportValues = cashUpAggregator.getReportValues();
        int grossValue = reportValues.getOrDefault(CashUpKeys.GROSS_VALUE, 0);
        int netValue = reportValues.getOrDefault(CashUpKeys.NET_VALUE, 0);

        return updateSales(token, taxRateMap, allOrders, uniqueId, startTime, endTime, grossValue, netValue);
    }

    public boolean updateSales(String token, Map<String, TaxRate> taxRateMap, List<Order> allOrders, String uniqueId, long startTime, long endTime, int grossValue, int netValue) {
        SetSalesRequest request = new SetSalesBuilder()
                .withToken(token)
                .withId(uniqueId)
                .withLimits(startTime, endTime)
                .withTaxRates(taxRateMap)
                .withOrders(allOrders)
                .withTotalGross(grossValue)
                .withTotalNet(netValue)
                .build();

        HttpEntity<SetSalesRequest> entity = new HttpEntity<>(request, STANDARD_HEADERS);

        ResponseEntity<MarketManResponse> responseEntity = restTemplate.exchange(
                ENDPOINT + "sales/SetSales",
                HttpMethod.POST,entity,MarketManResponse.class);

        MarketManResponse marketManResponse = responseEntity.getBody();
        if(!marketManResponse.isSuccess()) {
            LOGGER.error("Could not send data to MarketMan: " + marketManResponse.getErrorMessage());
            return false;
        }

        return true;
    }


    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
