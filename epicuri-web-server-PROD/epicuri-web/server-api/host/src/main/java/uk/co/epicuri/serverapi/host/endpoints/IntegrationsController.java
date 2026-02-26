package uk.co.epicuri.serverapi.host.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.HostIntegrationsView;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "integrations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class IntegrationsController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getIntegrations(@RequestHeader(Params.AUTHORIZATION) String token) {
        Restaurant restaurant = masterDataService.getRestaurant(authenticationService.getRestaurantId(token));
        if(restaurant == null) {
            return ResponseEntity.notFound().build();
        }

        List<HostIntegrationsView> views = new ArrayList<>();
        restaurant.getIntegrations().forEach((k,v) -> views.add(new HostIntegrationsView(k,v)));
        return ResponseEntity.ok(views);
    }
}
