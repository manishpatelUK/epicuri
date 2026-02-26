package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.StockLevel;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.LiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/StockControl", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class StockControlController {

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private LiveDataService liveDataService;

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getStockLevels(@RequestHeader(Params.AUTHORIZATION) String token){
        return ResponseEntity.ok(masterDataService.getStockLevels(authenticationService.getRestaurantId(token)));
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<?> putStockLevel(@RequestHeader(Params.AUTHORIZATION) String token, @PathVariable("id") String id, @RequestBody StockLevel stockLevel) {
        stockLevel.setRestaurantId(authenticationService.getRestaurantId(token));

        if (isModelInvalid(token, stockLevel)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Invalid model"));
        }

        if(!stockLevel.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("ID mismatch"));
        }

        StockLevel inDb = masterDataService.getStockLevel(id);
        if(inDb == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("SKU not found"));
        }
        String restaurantId = authenticationService.getRestaurantId(token);
        if(inDb.getPlu() != null && !inDb.getPlu().equals(stockLevel.getPlu())) {
            List<MenuItem> menuItemsByPlu = masterDataService.getMenuItemsByPlu(restaurantId, Collections.singletonList(inDb.getPlu()));
            masterDataService.updateItemsWithPLU(menuItemsByPlu, stockLevel.getPlu());
        }
        masterDataService.upsert(stockLevel);
        updateUnavailability(stockLevel, restaurantId);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postStockLevel(@RequestHeader(Params.AUTHORIZATION) String token, @RequestBody StockLevel stockLevel) {
        String restaurantId = authenticationService.getRestaurantId(token);
        stockLevel.setRestaurantId(restaurantId);
        if (isModelInvalid(token, stockLevel)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("Invalid model"));
        }

        StockLevel inDb = masterDataService.getStockLevelByRestaurantIdAndPlu(restaurantId, stockLevel.getPlu());
        if(inDb != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringMessage("PLU already exists: " + stockLevel.getPlu()));
        }

        masterDataService.upsert(stockLevel);
        updateUnavailability(stockLevel, restaurantId);

        return ResponseEntity.ok().build();
    }

    private void updateUnavailability(StockLevel stockLevel, String restaurantId) {
        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        boolean autoUnavailable = (Boolean) restaurant.getRestaurantDefaults().stream().filter(d -> d.getName().equals(FixedDefaults.AUTO_STOCK_UNAVAILABLE)).findFirst().orElse(RestaurantDefault.newDefault(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true)).getValue();
        if(autoUnavailable) {
            liveDataService.updateUnavailabilityOnStockControl(restaurantId, Collections.singletonList(stockLevel.getPlu()));
        }
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<?> deleteStockLevel(@RequestHeader(Params.AUTHORIZATION) String token, @PathVariable("id") String id) {
        masterDataService.deletePlu(authenticationService.getRestaurantId(token), id);
        return ResponseEntity.ok().build();
    }


    private boolean isModelInvalid(String token, StockLevel stockLevel) {
        if(StringUtils.isBlank(stockLevel.getRestaurantId()) || !stockLevel.getRestaurantId().equals(authenticationService.getRestaurantId(token))) {
            return true;
        }

        if(StringUtils.isBlank(stockLevel.getPlu())) {
            return true;
        }

        return false;
    }
}
