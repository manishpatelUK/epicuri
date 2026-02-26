package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/Menu", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MenuController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    @HostAuthRequired
    @RequestMapping(value = "/All", method = RequestMethod.GET)
    public ResponseEntity<List<MenuView>> getAllMenus(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Menu> menus = masterDataService.getMenusByRestaurantId(restaurantId).stream().filter(m -> m.getDeleted() == null).collect(Collectors.toList());
        return generateMenuViews(restaurantId, menus);
    }

    private ResponseEntity<List<MenuView>> generateMenuViews(String restaurantId, List<Menu> menus) {
        List<Course> courses = masterDataService.getCoursesByRestaurantId(restaurantId);
        List<ModifierGroup> modifierGroups = masterDataService.getModifierGroupsByRestaurant(restaurantId);

        List<MenuItem> items = masterDataService.getAllMenuItems(restaurantId);
        List<MenuView> menuViews = new ArrayList<>();
        for(Menu menu : menus) {
            MenuView menuView = getMenuView(items, courses, modifierGroups, menu);
            menuViews.add(menuView);
        }

        menuViews.sort(Comparator.comparingInt(MenuView::getOrder));

        return ResponseEntity.ok(menuViews);
    }

    private MenuView getMenuView(List<MenuItem> allItems, List<Course> courses, List<ModifierGroup> modifierGroups, Menu menu) {
        List<String> ids = menu.getCategories().stream()
                .map(Category::getGroups)
                .flatMap(List::stream)
                .map(Group::getItems)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<MenuItem> items = allItems.stream().filter(m -> ids.contains(m.getId())).collect(Collectors.toList());
        return new MenuView(menu, modifierGroups, courses, items);
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MenuView>> getMenus(@RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Menu> menus = masterDataService.getMenusByRestaurantId(restaurantId);
        menus = menus.stream().filter(m -> m.isActive() && m.getDeleted() == null).collect(Collectors.toList());
        return generateMenuViews(restaurantId, menus);
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMenu(@RequestHeader(Params.AUTHORIZATION) String token,
                                     @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Menu menu = masterDataService.getMenu(id);
        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        List<Course> courses = masterDataService.getCoursesByRestaurantId(restaurantId);
        List<ModifierGroup> modifierGroups = masterDataService.getModifierGroupsByRestaurant(restaurantId);
        List<MenuItem> items = masterDataService.getAllMenuItems(restaurantId);
        MenuView menuView = getMenuView(items, courses, modifierGroups, menu);

        return ResponseEntity.ok(menuView);
    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postMenu(@RequestHeader(Params.AUTHORIZATION) String token,
                                      @NotNull @RequestBody MenuView menuView) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Menu menu = createMenu(restaurantId, menuView.getMenuName(), menuView.isActive());
        return ResponseEntity.created(URI.create("Menu/"+menu.getId())).body(getMenuView(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), menu));
    }

    private Menu createMenu(String restaurantId, String menuName, boolean isActive) {
        Menu menu = new Menu();
        menu.setLastUpdate(System.currentTimeMillis());
        menu.setRestaurantId(restaurantId);
        menu.setName(menuName);
        menu.setActive(isActive);
        menu = masterDataService.addMenu(menu);
        menu.setOrder(masterDataService.getMenus(restaurantId).size());

        return menu;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMenu(@RequestHeader(Params.AUTHORIZATION) String token,
                                        @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Menu menu = masterDataService.getMenu(id);
        if(menu == null || !restaurantId.equals(menu.getRestaurantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Restaurant restaurant = masterDataService.getRestaurant(restaurantId);
        List<Service> services = restaurant.getServices();

        if(services.stream().anyMatch(s -> id.equals(s.getDefaultMenuId()) || id.equals(s.getSelfServiceMenuId()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to delete the menu (menu currently in use).");
        }

        masterDataService.deleteMenu(id);

        return ResponseEntity.ok().build();
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putMenu(@PathVariable("id") String id,
                                     @RequestBody MenuView menuView ) {
        Menu menu = masterDataService.getMenu(id);

        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        if(StringUtils.isBlank(menuView.getMenuName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Menu name is mandatory");
        }

        menu.setActive(menuView.isActive());
        menu.setLastUpdate(System.currentTimeMillis());
        menu.setName(menuView.getMenuName());
        menu.setOrder(menuView.getOrder());

        masterDataService.upsert(menu);

        return ResponseEntity.ok(menuView);
    }

    @RequestMapping(value = "/ChangeTakeawayMenu/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putMenu(@RequestHeader(Params.AUTHORIZATION) String token,
                                     @PathVariable("id") String id) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Menu menu = masterDataService.getMenu(id);

        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        masterDataService.setRestaurantTakeawayMenu(restaurantId, id);

        return ResponseEntity.ok().build();

    }

    @HostAuthRequired
    @RequestMapping(value = "/clone/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postCloneMenu(@RequestHeader(Params.AUTHORIZATION) String token,
                                           @PathVariable("id") String id,
                                           @RequestBody IdPojoAndName idPojoAndName) {
        String restaurantId = authenticationService.getRestaurantId(token);
        Menu menu = masterDataService.getMenu(id);
        Menu clone = createMenu(restaurantId, idPojoAndName.getName(), menu.isActive());

        //recurse into menu and copy category/group/items
        for(Category category : menu.getCategories()) {
            Category newCategory = category.copyExcludingId();
            newCategory.setId(IDAble.generateId(clone.getId()));

            for(Group group : newCategory.getGroups()) {
                group.setId(IDAble.generateId(newCategory.getId()));
            }

            clone.getCategories().add(newCategory);
        }
        masterDataService.upsert(clone);

        return ResponseEntity.created(URI.create("Menu/"+clone.getId())).body(getMenuView(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), clone));
    }
}
