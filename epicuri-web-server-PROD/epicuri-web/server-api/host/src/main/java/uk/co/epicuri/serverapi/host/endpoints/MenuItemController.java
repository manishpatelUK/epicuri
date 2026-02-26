package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.auth.HostLevelCheckRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdList;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/MenuItem", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MenuItemController {

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private AuthenticationService authenticationService;

    public MenuItemController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MenuItemView>> getMenuItems(@RequestHeader(Params.AUTHORIZATION) String token,
                                                           @RequestParam(value = "orphaned", defaultValue = "false") boolean orphaned) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<MenuItem> items = masterDataService.getAllMenuItemsNotDeleted(restaurantId);

        if(orphaned) {
            List<Menu> menus = masterDataService.getMenusByRestaurantId(restaurantId);
            Set<String> nonOrphanedItems = new HashSet<>();
            for(Menu menu : menus) {
                for(Category  category : menu.getCategories()) {
                    for(Group group : category.getGroups()) {
                        nonOrphanedItems.addAll(group.getItems());
                    }
                }
            }
            items = items.stream().filter(m -> !nonOrphanedItems.contains(m.getId())).collect(Collectors.toList());
        }

        return ResponseEntity.ok(items.stream().map(m -> new MenuItemView(m, 0)).collect(Collectors.toList()));
    }


    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postMenuItem(@RequestHeader(Params.AUTHORIZATION) String token,
                                          @NotNull @RequestBody MenuItemView itemView) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Menu> menu = masterDataService.getMenusByRestaurantId(restaurantId);

        if(!masterDataService.printerExists(itemView.getDefaultPrinter())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Printer not found");
        }

        if(!masterDataService.taxRateExists(itemView.getTaxTypeId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tax type not found");
        }

        //ensure uniqueness of short code
        MenuItem duplicate = isShortCodeDuplicated(null, itemView, masterDataService.getAllMenuItems(restaurantId));
        if(duplicate != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Short code is already in use (" + duplicate.getName() + ")");
        }

        List<String> preferenceIds = masterDataService.getAllPreferences().stream().map(Preference::getId).collect(Collectors.toList());
        itemView.getAllergyIds().removeIf(x -> !preferenceIds.contains(x));
        itemView.getDietaryIds().removeIf(x -> !preferenceIds.contains(x));

        MenuItem menuItem = new MenuItem(itemView);
        menuItem.setRestaurantId(restaurantId);
        MenuItem item = masterDataService.upsert(menuItem);

        menu.forEach(m -> {
            final boolean[] changed = {false};
            m.getCategories().forEach(c -> c.getGroups().forEach(g -> {
                if(itemView.getMenuGroups() != null && itemView.getMenuGroups().contains(g.getId()) && !g.getItems().contains(item.getId())) {
                    changed[0] = true;
                    g.getItems().add(item.getId());
                }}));

            if(changed[0]) {
                masterDataService.upsert(m);
            }
        });

        itemView.setId(item.getId());
        return ResponseEntity.created(URI.create("/MenuItem/"+item.getId())).body(itemView);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putMenuItem(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @PathVariable("id") String id,
                                         @NotNull @RequestBody MenuItemView itemView) {
        if(StringUtils.isBlank(itemView.getDefaultPrinter()) || !masterDataService.printerExists(itemView.getDefaultPrinter())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Printer not found");
        }

        if(StringUtils.isBlank(itemView.getTaxTypeId()) || !masterDataService.taxRateExists(itemView.getTaxTypeId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tax type not found");
        }

        List<MenuItem> items = masterDataService.getAllMenuItems(authenticationService.getRestaurantId(token));
        return saveMenuItem(items, itemView, id);
    }

    @HostAuthRequired
    @RequestMapping(value = "/multiple", method = RequestMethod.PUT)
    public ResponseEntity<?> putMenuItem(@RequestHeader(Params.AUTHORIZATION) String token,
                                         @NotNull @RequestBody List<MenuItemView> itemViews) {

        for(MenuItemView itemView : itemViews) {
            if (StringUtils.isBlank(itemView.getDefaultPrinter()) || !masterDataService.printerExists(itemView.getDefaultPrinter())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Printer not found");
            }

            if (StringUtils.isBlank(itemView.getTaxTypeId()) || !masterDataService.taxRateExists(itemView.getTaxTypeId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tax type not found");
            }
        }

        List<MenuItem> items = masterDataService.getAllMenuItems(authenticationService.getRestaurantId(token));

        for(MenuItemView itemView : itemViews) {
            if(itemView.getId() != null) {
                saveMenuItem(items, itemView, itemView.getId());
            }
        }

        return ResponseEntity.accepted().build();
    }

    private ResponseEntity<?> saveMenuItem(List<MenuItem> items, MenuItemView itemView, String id) {
        MenuItem item = items.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
        if(item == null || item.getDeleted() != null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        }

        //ensure uniqueness of short code
        MenuItem duplicate = isShortCodeDuplicated(id, itemView, items);
        if(duplicate != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Short code is already in use (" + duplicate.getName() + ")");
        }

        List<String> preferenceIds = masterDataService.getAllPreferences().stream().map(Preference::getId).collect(Collectors.toList());
        itemView.getAllergyIds().removeIf(x -> !preferenceIds.contains(x));
        itemView.getDietaryIds().removeIf(x -> !preferenceIds.contains(x));

        MenuItem copy = new MenuItem(itemView);
        copy.setId(id);
        copy.setRestaurantId(item.getRestaurantId());
        masterDataService.upsert(copy);

        return ResponseEntity.accepted().build(); //changed this from created to accepted because we didn't create
    }

    public MenuItem isShortCodeDuplicated(String id, MenuItemView itemView, List<MenuItem> items) {
        if(StringUtils.isNotBlank(itemView.getShortCode())) {
            for(MenuItem menuItem : items) {
                if(menuItem.getDeleted() != null || menuItem.getId().equals(id)) {
                    continue;
                }

                if(itemView.getShortCode().equalsIgnoreCase(menuItem.getShortCode())) {
                    return menuItem;
                }
            }
        }
        return null;
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMenuItem(@RequestHeader(Params.AUTHORIZATION) String token,
                                            @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        MenuItem item = masterDataService.getItem(id);
        if(item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        }

        for(Menu menu : masterDataService.getMenusByRestaurantId(restaurantId)) {
            final boolean[] changed = {false};
            for(Category category : menu.getCategories()) {
                category.getGroups().stream().filter(group -> group.getItems().contains(id)).forEach(group -> {
                    changed[0] = true;
                    group.getItems().remove(id);
                });

            }
            if(changed[0]) {
                masterDataService.upsert(menu);
            }
        }

        masterDataService.deleteMenuItem(id);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/multiple", method = RequestMethod.POST) //has to be post to avoid cross origin issue
    public ResponseEntity<?> deleteMultipleMenuItem(@RequestHeader(Params.AUTHORIZATION) String token,
                                                    @RequestBody IdList idList) {
        if(idList.getIds() == null || idList.getIds().size() == 0) {
            return ResponseEntity.ok().build();
        }

        String restaurantId = authenticationService.getRestaurantId(token);

        for(Menu menu : masterDataService.getMenusByRestaurantId(restaurantId)) {
            boolean changed = false;
            for(Category category : menu.getCategories()) {
                for(Group group : category.getGroups()) {
                    if(group.getItems().stream().anyMatch(s -> idList.getIds().contains(s))) {
                        changed = true;
                        group.getItems().removeAll(idList.getIds());
                    }
                }

            }
            if(changed) {
                masterDataService.upsert(menu);
            }
        }

        masterDataService.deleteMenuItems(idList.getIds());

        return ResponseEntity.ok().build();
    }
}
