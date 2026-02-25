package uk.co.epicuri.serverapi.service.external;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.BadStateException;
import uk.co.epicuri.serverapi.common.pojo.external.mews.*;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.service.SessionCalculationService;

import java.util.*;

/**
 * Created by manish
 *
 * Mews API documentation: https://mews-systems.gitbook.io/connector-api/guidelines
 */
@Service
public class MewsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MewsService.class);

    @Value("${mews.server.url}")
    private String mewsUrl;

    @Value("${mews.client.token}")
    private String clientToken;

    private static final String MEWS_CUSTOMER_ENDPOINT = "api/connector/v1/customers/search";
    private static final String MEWS_CHARGE_ENDPOINT = "api/connector/v1/orders/add";
    private static final String MEWS_SERVICE_ENDPOINT = "api/connector/v1/services/getAll";
    private static final String MEWS_SERVICE_TAXATION = "api/connector/v1/taxations/getAll";
    private static final HttpHeaders STANDARD_HEADERS = createHeaders();

    private RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

    public MewsService() {
        restTemplate.setInterceptors(Collections.singletonList(new RequestLoggingInterceptor()));
    }

    public Collection<MewsCustomer> getCustomers(String alternateURL, String accessToken, String room, String name) throws BadStateException {
        Set<MewsCustomer> set = new HashSet<>();

        MewsCustomerSearchRequest request = new MewsCustomerSearchRequest();
        request.setAccessToken(accessToken);
        request.setClientToken(clientToken);
        request.setRoomNumber(StringUtils.isBlank(room) ? "" : room);
        request.setName(StringUtils.isBlank(name) ? "" : name);
        HttpEntity<MewsCustomerSearchRequest> entity = new HttpEntity<>(request, STANDARD_HEADERS);

        String url = alternateURL == null ? mewsUrl : alternateURL;

        ResponseEntity<MewsCustomerSearchResponse> responseEntity = restTemplate.exchange(
                String.format("%s/%s", url, MEWS_CUSTOMER_ENDPOINT),
                HttpMethod.POST,entity,MewsCustomerSearchResponse.class);

        if(responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
            throw new BadStateException();
        }

        set.addAll(responseEntity.getBody().getCustomers());
        return set;
    }

    public String charge(String alternateURL, String accessToken, String serviceId, Map<ItemType,String> accountingCategories, String product,
                         String notes, List<Order> orders, Collection<TaxRate> allRates, String customerId, String currency, Restaurant restaurant) throws BadStateException {

        MewsTaxations taxations = getTaxations(alternateURL, accessToken);
        // match tax codes to tax rates
        /*Map<String,String> taxMap = new HashMap<>(); // map of Epicuri tax id to tax code in mews

        for(TaxRate taxRate : allRates) {
            for (MewsTaxRate mewsTaxRate : taxations.getTaxRates()) {
                if (!"Relative".equals(mewsTaxRate.getStrategy().getDiscriminator())) continue;

                if (compareDouble(mewsTaxRate.getStrategy().getValue().getValue(), taxRate.getRateAsDouble(), 0.01)) {
                    taxMap.put(taxRate.getId(), mewsTaxRate.getCode());
                }
            }
        }*/

        Map<String,String> taxMap = new HashMap<>();
        for(String mapping : restaurant.getTaxMappings()) {
            String[] keyValue = mapping.split(",");
            taxMap.put(keyValue[0],keyValue[1]);
        }


        MewsChargeRequest request = new MewsChargeRequest();
        request.setAccessToken(accessToken);
        request.setClientToken(clientToken);
        request.setCustomerId(customerId);
        request.setNotes(notes);
        request.setServiceId(serviceId);

        MewsProductOrders productOrders = new MewsProductOrders();
        productOrders.setProductId(product);
        //request.getProductOrders().add(productOrders);



        orders.forEach(o -> {
            if(!o.isRemoveFromReports() && !o.isVoided()) {
                int price = SessionCalculationService.getUnitValue(o);
                if(o.getAdjustment() != null) {
                    price = 0; //todo this will change when we allow partial discounts on items
                }
                // calculate the net value
                int netValue = SessionCalculationService.calculateNet(price, o.getTaxRate().getRateAsDouble());
                request.getItems().add(new MewsChargeItem(o, price, netValue, taxMap.get(o.getTaxRate().getId()), currency, accountingCategories.get(o.getMenuItem().getType())));
            }
        });


        HttpEntity<MewsChargeRequest> entity = new HttpEntity<>(request, STANDARD_HEADERS);
        String url = alternateURL == null ? mewsUrl : alternateURL;

        ResponseEntity<MewsChargeResponse> responseEntity = restTemplate.exchange(
                String.format("%s/%s", url, MEWS_CHARGE_ENDPOINT),
                HttpMethod.POST,entity,MewsChargeResponse.class);

        if(responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
            throw new BadStateException();
        }

        LOGGER.info("Successfully posted to MEWS: {}", entity);

        return responseEntity.getBody().getOrderId();
    }

    private static boolean compareDouble(double a, double b, double epsilon) {
        return Math.abs(a-b) < epsilon;
    }

    public MewsChargeServiceResponse getServices(String alternateURL, String accessToken) {
        MewsServiceRequest request = new MewsServiceRequest();
        request.setAccessToken(accessToken);
        request.setClientToken(clientToken);

        HttpEntity<MewsServiceRequest> entity = new HttpEntity<>(request, STANDARD_HEADERS);
        String url = alternateURL == null ? mewsUrl : alternateURL;

        ResponseEntity<MewsChargeServiceResponse> responseEntity = restTemplate.exchange(
                String.format("%s/%s", url, MEWS_SERVICE_ENDPOINT),
                HttpMethod.POST,entity,MewsChargeServiceResponse.class);

        if(responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
            throw new BadStateException();
        }

        return responseEntity.getBody();
    }

    public MewsTaxations getTaxations(String alternateURL, String accessToken) {
        MewsServiceRequest request = new MewsServiceRequest();
        request.setAccessToken(accessToken);
        request.setClientToken(clientToken);

        HttpEntity<MewsServiceRequest> entity = new HttpEntity<>(request, STANDARD_HEADERS);
        String url = alternateURL == null ? mewsUrl : alternateURL;

        ResponseEntity<MewsTaxations> responseEntity = restTemplate.exchange(
                String.format("%s/%s", url, MEWS_SERVICE_TAXATION),
                HttpMethod.POST,entity,MewsTaxations.class);

        if(responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
            throw new BadStateException();
        }

        return responseEntity.getBody();
    }

    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
