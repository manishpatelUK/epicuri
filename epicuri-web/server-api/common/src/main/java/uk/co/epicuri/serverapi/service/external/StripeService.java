package uk.co.epicuri.serverapi.service.external;

import com.stripe.exception.*;
import com.stripe.model.Account;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.ChargeSummary;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.CredentialsGrant;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.CredentialsGrantResponse;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.CreditCardData;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.Adjustment;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * See https://stripe.com/docs/api/java for documentation on Stripe library
 * See https://stripe.com/docs/connect for Stripe Connect docs
 */
@Service
public class StripeService {
    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.connection.timeout}")
    private int connectionTimeout;

    @Value("${stripe.connection.read.timeout}")
    private int readTimeout;

    @Value("${stripe.acceptance.ip}")
    private String acceptanceIp;

    public static final Map<String,?> NULL_QUERY_MAP = Collections.unmodifiableMap(new HashMap<>());

    @Autowired
    private MasterDataService masterDataService;

    private RequestOptions requestOptions;

    @PostConstruct
    public void postConstruct() {
        requestOptions = new RequestOptions.RequestOptionsBuilder()
                            .setApiKey(apiKey)
                            .setConnectTimeout(connectionTimeout)
                            .setReadTimeout(readTimeout).build();
    }

    public Customer acquireCustomer(String token,
                                    String emailAddress,
                                    String description) throws StripeException {
        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("source", token);
        if(emailAddress != null) {
            customerParams.put("email", emailAddress);
        }
        if(description != null) {
            customerParams.put("description", description);
        }
        return Customer.create(customerParams, requestOptions);
    }

    public Charge charge(Restaurant recipient, CreditCardData creditCardData, int amount, boolean capture) throws IllegalArgumentException, StripeException {
        return charge(recipient, creditCardData, amount, capture, true);
    }

    public Charge charge(Restaurant recipient, CreditCardData creditCardData, int amount, boolean capture, boolean isCustomer) throws IllegalArgumentException, StripeException {
        return charge(recipient, creditCardData.getExternalId(), amount, capture, isCustomer);
    }

    public Charge charge(Restaurant recipient, String stripeCustomerId, int amount, boolean capture, boolean isCustomerId) throws IllegalArgumentException, StripeException {
        if(!recipient.getIntegrations().containsKey(ExternalIntegration.STRIPE)
                || recipient.getIntegrations().get(ExternalIntegration.STRIPE).getToken() == null
                || recipient.getISOCurrency() == null) {
            throw new IllegalArgumentException("This entity does not have Stripe integration");
        }

        Map<String,Object> chargeMap = new HashMap<>();
        chargeMap.put("amount", amount);
        chargeMap.put("currency", recipient.getISOCurrency().toLowerCase());
        chargeMap.put(isCustomerId ? "customer" : "source", stripeCustomerId);
        chargeMap.put("capture", capture);

        Map<String, Object> destinationParams = new HashMap<>();
        destinationParams.put("account", recipient.getIntegrations().get(ExternalIntegration.STRIPE).getToken()); //account id
        chargeMap.put("destination", destinationParams);

        return Charge.create(chargeMap, requestOptions);
    }

    public Account createAccount(String country, String email, String type) throws StripeException {
        if(masterDataService.isProdEnvironment()) {
            throw new IllegalArgumentException("Cannot create or verify Customer Accounts in production yet");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("country", country);
        params.put("type", type);
        params.put("email", email);

        Account account =  Account.create(params, requestOptions);
        verifyAccount(params, account);

        return account;
    }

    public CredentialsGrantResponse connectAccount(String authenticationCode) {
        RestTemplate restTemplate = new RestTemplate();
        CredentialsGrant credentialsGrant = new CredentialsGrant();
        credentialsGrant.setSecret(apiKey);
        credentialsGrant.setCode(authenticationCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity<>(credentialsGrant, headers);
        return restTemplate.exchange("https://connect.stripe.com/oauth/token", HttpMethod.POST, entity, CredentialsGrantResponse.class, NULL_QUERY_MAP).getBody();
    }

    public Charge capturePayment(Adjustment adjustment) throws StripeException, IllegalArgumentException {
        ChargeSummary chargeSummary = ControllerUtil.OBJECT_MAPPER.convertValue(adjustment.getSpecialAdjustmentData().get(StripeConstants.PAYMENT_KEY), ChargeSummary.class);
        Charge charge = null;
        if(chargeSummary != null) {
            charge = Charge.retrieve(chargeSummary.getChargeId(), requestOptions);
            if(!charge.getCaptured()) {
                charge = charge.capture(requestOptions);
                adjustment.getSpecialAdjustmentData().put(StripeConstants.PAYMENT_KEY, createSummary(charge));
            }
        }

        return charge;
    }

    public boolean hasPreAuth(Adjustment adjustment) throws StripeException {
        ChargeSummary chargeSummary = ControllerUtil.OBJECT_MAPPER.convertValue(adjustment.getSpecialAdjustmentData().get(StripeConstants.PAYMENT_KEY), ChargeSummary.class);
        if(chargeSummary != null) {
            Charge charge = Charge.retrieve(chargeSummary.getChargeId(), requestOptions);
            return charge.getCaptured() != null && !charge.getCaptured();
        }
        return false;
    }

    public static ChargeSummary createSummary(Charge charge) {
        ChargeSummary chargeSummary = new ChargeSummary();
        chargeSummary.setChargeId(charge.getId());
        chargeSummary.setAmount(charge.getAmount());
        chargeSummary.setApplicationFee(charge.getApplicationFee());
        chargeSummary.setCaptured(charge.getCaptured());
        chargeSummary.setDestination(charge.getDestination());
        chargeSummary.setCustomer(charge.getCustomer());
        chargeSummary.setCurrency(charge.getCurrency());
        chargeSummary.setFailureCode(charge.getFailureCode());
        chargeSummary.setFailureMessage(charge.getFailureMessage());
        chargeSummary.setStatus(charge.getStatus());
        chargeSummary.setTransfer(charge.getTransfer());
        chargeSummary.setPaid(charge.getPaid());
        if(charge.getSource() != null && charge.getSource() instanceof Card) {
            Card card = (Card)charge.getSource();
            chargeSummary.setLast4Digits(card.getLast4());
            chargeSummary.setExpMonth(card.getExpMonth());
            chargeSummary.setExpYear(card.getExpYear());
        }

        return chargeSummary;
    }

    private void verifyAccount(Map<String, Object> params, Account account) throws StripeException {
        Map<String, Object> tosAcceptanceParams = new HashMap<>();
        tosAcceptanceParams.put("date", System.currentTimeMillis() / 1000L);
        tosAcceptanceParams.put("ip", acceptanceIp);

        Map<String, Object> acceptanceParams = new HashMap<>();
        params.put("tos_acceptance", tosAcceptanceParams);

        account.update(acceptanceParams, requestOptions);
    }
}
