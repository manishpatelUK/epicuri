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
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryCloneView;
import uk.co.epicuri.serverapi.common.pojo.menu.CategoryView;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.StaffRole;
import uk.co.epicuri.serverapi.service.AuthenticationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "MenuCategory", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MenuCategoryController { 

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MasterDataService masterDataService;

    private static Comparator<Category> SORT_BY_ORDER_FIELD = Comparator.comparingInt(Category::getOrder);

    public MenuCategoryController() {

    }

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postMenuCategory(@RequestHeader(Params.AUTHORIZATION) String token,
                                              @NotNull @RequestBody CategoryView categoryView) {
        if(StringUtils.isBlank(categoryView.getMenuId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        Menu menu = masterDataService.getMenu(categoryView.getMenuId());
        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        String restaurantId = authenticationService.getRestaurantId(token);
        List<String> courses = masterDataService.getCoursesByRestaurantId(restaurantId).stream().map(Course::getId).collect(Collectors.toList());
        List<String> validIds = categoryView.getDefaultCourseIds().stream().filter(courses::contains).distinct().collect(Collectors.toList());
        categoryView.setDefaultCourseIds(validIds);
        Category category = new Category(menu, categoryView);

        saveCategory(categoryView, menu, category);

        return ResponseEntity.created(URI.create("MenuCategory/"+category.getId())).body(categoryView);
    }

    private void saveCategory(@NotNull @RequestBody CategoryView categoryView, Menu menu, Category category) {
        String menuId = menu.getId();
        masterDataService.addToMenu(menuId, category);
        masterDataService.updateMenuModifiedTime(menuId);
        categoryView.setId(category.getId());
    }

    @HostAuthRequired
    @RequestMapping(value = "/clone/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postCloneMenuCategory(@PathVariable("id") String categoryIdToClone,
                                                   @NotNull @RequestBody CategoryCloneView categoryCloneView) {
        Tuple<Menu,Category> menuCategoryTuple = getMenuAndCategory(categoryCloneView, categoryIdToClone);

        if(menuCategoryTuple == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu or category not found");
        }

        Category copy = copyAndUpsert(categoryCloneView, menuCategoryTuple.getA(), menuCategoryTuple.getB());
        return ResponseEntity.created(URI.create("MenuCategory/" + copy.getId())).build();
    }

    private Tuple<Menu,Category> getMenuAndCategory(CategoryCloneView categoryCloneView, String categoryId) {
        String menuIdCopyTo = categoryCloneView.getMenuId() == null ? IDAble.extractParentId(categoryId) : categoryCloneView.getMenuId();
        String menuIdCopyFrom = IDAble.extractParentId(categoryId);
        Menu copyTo = masterDataService.getMenu(menuIdCopyTo);
        Menu copyFrom = null;
        if(menuIdCopyFrom.equals(menuIdCopyTo)) {
            copyFrom = copyTo;
        } else {
            copyFrom = masterDataService.getMenu(menuIdCopyFrom);
        }

        if(copyFrom == null || copyTo == null) {
            return null;
        }

        Category currentCategory = copyFrom.getCategories().stream().filter(c -> c.getId().equals(categoryId)).findFirst().orElse(null);
        if(currentCategory == null) {
            return null;
        }

        return new Tuple<>(copyTo, currentCategory);
    }

    private Category copyAndUpsert(CategoryCloneView categoryCloneView, Menu menu, Category currentCategory) {
        //make a copy
        Category copy = currentCategory.copyExcludingId();
        copy.setName(categoryCloneView.getName());
        copy.setId(IDAble.generateId(menu.getId()));
        for(Group group : copy.getGroups()) {
            group.setId(IDAble.generateId(copy.getId()));
        }
        copy.setOrder(menu.getCategories().size());
        menu.getCategories().add(copy);
        menu.setLastUpdate(System.currentTimeMillis());
        masterDataService.upsert(menu);
        return copy;
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putMenuCategory(@PathVariable("id") String id,
                                             @NotNull @RequestBody CategoryView categoryView) {
        Menu menu = masterDataService.getMenu(categoryView.getMenuId());
        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        Category currentCategory = menu.getCategories().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        if(currentCategory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }

        menu.getCategories().removeIf(c -> c.getId().equals(id));

        Category category = new Category();
        category.setName(categoryView.getCategoryName());
        category.setOrder(categoryView.getOrder());
        category.setId(id);
        //category.setGroups(new ArrayList<>(findGroups(menu, categoryView.getMenuGroupsIds()))); //current cpe does not do this!
        category.setGroups(currentCategory.getGroups());
        category.setCourseIds(categoryView.getDefaultCourseIds().stream().distinct().collect(Collectors.toList()));
        menu.getCategories().add(category);
        menu.setLastUpdate(System.currentTimeMillis());

        menu.getCategories().sort(SORT_BY_ORDER_FIELD);

        masterDataService.upsert(menu);
        categoryView.setId(id);

        return ResponseEntity.ok(categoryView);
    }

    private Set<Group> findGroups(Menu menu, List<String> groupIds) {
        Set<Group> groups = new HashSet<>();
        for(Category category : menu.getCategories()) {
            groups.addAll(category.getGroups().stream().filter(group -> groupIds.contains(group.getId())).collect(Collectors.toList()));
        }

        return groups;
    }

    @HostAuthRequired
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMenuCategory(@PathVariable("id") String id,
                                                @RequestHeader(Params.AUTHORIZATION) String token) {
        String restaurantId = authenticationService.getRestaurantId(token);
        List<Menu> menus = masterDataService.getMenusByRestaurantId(restaurantId);
        for(Menu menu : menus) {
            Category category = menu.getCategories().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if(category != null) {
                menu.getCategories().removeIf(c -> c.getId().equals(id));
                masterDataService.upsert(menu);
                masterDataService.updateMenuModifiedTime(menu.getId());
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.ok().build();
    }
}
