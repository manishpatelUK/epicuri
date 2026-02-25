package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.HostTaxView;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "TaxType", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class TaxTypeController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    public TaxTypeController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getTaxTypes(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);

        List<TaxRate> rates = masterDataService.getTaxRatesByCountry(restaurant.getCountryId());

        return ResponseEntity.ok(rates.stream().map(HostTaxView::new).collect(Collectors.toList()));
    }
}
