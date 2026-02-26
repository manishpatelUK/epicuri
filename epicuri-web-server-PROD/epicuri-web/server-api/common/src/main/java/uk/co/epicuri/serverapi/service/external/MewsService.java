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

    public String charge(String alternateURL, String accessToken, String notes, List<Order> orders, Collection<TaxRate> allRates, String customerId, String currency) throws BadStateException {
        MewsChargeRequest request = new MewsChargeRequest();
        request.setAccessToken(accessToken);
        request.setClientToken(clientToken);
        request.setCustomerId(customerId);
        request.setNotes(notes);

        orders.forEach(o -> {
            if(!o.isRemoveFromReports() && !o.isVoided()) {
                int price = SessionCalculationService.getUnitValue(o);
                if(o.getAdjustment() != null) {
                    price = 0; //todo this will change when we allow partial discounts on items
                }
                request.getItems().add(new MewsChargeItem(o, price, allRates, currency));
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

        return responseEntity.getBody().getChargeId();
    }

    private static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
