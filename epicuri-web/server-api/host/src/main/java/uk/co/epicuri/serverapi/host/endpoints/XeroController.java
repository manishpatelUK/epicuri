package uk.co.epicuri.serverapi.host.endpoints;

import com.xero.api.OAuthAuthorizeToken;
import com.xero.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.BooleanMessage;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroAccountView;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroMappingRule;
import uk.co.epicuri.serverapi.common.pojo.external.xero.XeroMappingsResponse;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.service.external.XeroService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "xero")
public class XeroController {
    private static final Logger LOGGER = LoggerFactory.getLogger(XeroController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private XeroService xeroService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> preAuthentication(@RequestHeader(Params.AUTHORIZATION) String token) {
        Tuple<Restaurant,ResponseEntity<?>> tuple = checkAndGetRestaurant(token);
        if(tuple.getB() != null) {
            return tuple.getB();
        }

        try {
            OAuthAuthorizeToken oAuthAuthorizeToken = xeroService.getPreAuthentication(tuple.getA());
            return ResponseEntity.ok(new StringMessage(oAuthAuthorizeToken.getAuthUrl()));
        } catch (IOException e) {
            LOGGER.error("Exception whilst trying getPreAuthentication", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE, value = "/auth/{id}")
    public RedirectView verify(@PathVariable("id") String id,
                               @RequestParam(value = "oauth_verifier") String verifier) {
        Restaurant restaurant = masterDataService.getRestaurant(id);
        if(restaurant == null || restaurant.getIntegrations() == null || !restaurant.getIntegrations().keySet().contains(ExternalIntegration.XERO)) {
            LOGGER.error("Restaurant {} is not set up correctly for Xero", id);
            return new RedirectView("https://epicuri.co.uk/xero/error.html");
        }

        try {
            xeroService.verify(restaurant, verifier);
        } catch(IllegalStateException e) {
            LOGGER.error("Could not verify the token", e);
            return new RedirectView("https://epicuri.co.uk/xero/error.html");
        } catch (IOException e) {
            LOGGER.error("Xero verification procedure failed", e);
            return new RedirectView("https://epicuri.co.uk/xero/error.html");
        }
        return new RedirectView("https://epicuri.co.uk/xero");
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, value = "/connection", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTokenValidity(@RequestHeader(Params.AUTHORIZATION) String token) {
        Restaurant restaurant = masterDataService.getRestaurant(authenticationService.getRestaurantId(token));
        if(restaurant == null || restaurant.getIntegrations() == null || !restaurant.getIntegrations().keySet().contains(ExternalIntegration.XERO)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean stale = xeroService.isTokenStale(restaurant);
        if(!xeroService.isPublicApp() && stale) {
            try {
                stale = xeroService.checkAndRefreshToken(restaurant);
            } catch (IOException e) {
                LOGGER.error("Xero verification procedure failed", e);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        LOGGER.debug("Xero token stale={}, returning {}", stale, !stale);
        return ResponseEntity.ok(new BooleanMessage(!stale));
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET, value = "/mappings", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMappings(@RequestHeader(Params.AUTHORIZATION) String token) {
        Tuple<Restaurant,ResponseEntity<?>> tuple = checkAndGetRestaurant(token);
        if(tuple.getB() != null) {
            return tuple.getB();
        }

        try {
            Restaurant restaurant = tuple.getA();
            List<Account> accounts = xeroService.getAccounts(restaurant);
            List<XeroAccountView> xeroAccounts = accounts.stream().map(a -> new XeroAccountView(a.getName(), a.getAccountID(), a.getCode(), a.getType() != null ? a.getType().value() : null)).collect(Collectors.toList());
            if(xeroAccounts.size() > 0) {
                xeroAccounts.add(0, XeroAccountView.DEFAULT_NONE);
            } else {
                xeroAccounts.add(XeroAccountView.DEFAULT_NONE);
            }
            List<AdjustmentType> adjustmentTypes = masterDataService.getAdjustmentTypes(restaurant.getAdjustmentTypes());
            Map<String, String> data = restaurant.getIntegrations().get(ExternalIntegration.XERO).getData();
            List<TaxRate> taxRatesByCountry = masterDataService.getTaxRatesByCountry(restaurant.getCountryId());

            return ResponseEntity.ok(new XeroMappingsResponse(xeroAccounts, adjustmentTypes, xeroService.convertToRules(data, adjustmentTypes, taxRatesByCountry), taxRatesByCountry));
        } catch (IOException e) {
            LOGGER.error("Xero API error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.PUT, value = "/mappings", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putMappings(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @RequestBody List<XeroMappingRule> rules) {
        Tuple<Restaurant,ResponseEntity<?>> tuple = checkAndGetRestaurant(token);
        if(tuple.getB() != null) {
            return tuple.getB();
        }

        Restaurant restaurant = tuple.getA();
        xeroService.updateRules(rules, restaurant);
        return ResponseEntity.ok().build();
    }

    private Tuple<Restaurant,ResponseEntity<?>> checkAndGetRestaurant(String token) {
        Restaurant restaurant = masterDataService.getRestaurant(authenticationService.getRestaurantId(token));
        if(restaurant == null) {
            return new Tuple<>(null,ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        if(restaurant.getIntegrations() == null || !restaurant.getIntegrations().keySet().contains(ExternalIntegration.XERO)) {
            return new Tuple<>(null, ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        return new Tuple<>(restaurant,null);
    }
}
