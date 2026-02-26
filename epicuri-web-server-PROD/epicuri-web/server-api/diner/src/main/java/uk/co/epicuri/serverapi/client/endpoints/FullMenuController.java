package uk.co.epicuri.serverapi.client.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.service.CommonDataViewsConversionService;
import uk.co.epicuri.serverapi.service.MasterDataService;
import uk.co.epicuri.serverapi.auth.CustomerAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/FullMenu", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class FullMenuController {

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private CommonDataViewsConversionService commonDataViewsConversionService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> getFullMenu(@NotNull @PathVariable("id") String id) {
        Menu menu = masterDataService.getMenu(id);
        if(menu == null || menu.getDeleted() != null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringMessage("Menu not found"));
        }

        String restaurantId = menu.getRestaurantId();

        return ResponseEntity.ok(commonDataViewsConversionService.getMenuView(restaurantId, menu));
    }
}
