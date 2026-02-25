package uk.co.epicuri.serverapi.client.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.menu.CustomerSummaryMenuView;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.service.MasterDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Menu", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MenuController {

    @Autowired
    private MasterDataService masterDataService;

    @RequestMapping(value = "/RestaurantMenus/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getRestaurantMenus(@PathVariable("id") String id) {
        List<Menu> list = masterDataService.getMenusByRestaurantId(id);
        list = list.stream().filter(m -> m.isActive() && m.getDeleted() == null).collect(Collectors.toList());

        if(list.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("No menus found"));
        }

        Restaurant restaurant = masterDataService.getRestaurant(id);
        List<CustomerSummaryMenuView> result = list.stream().map(menu -> new CustomerSummaryMenuView(restaurant, menu)).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
