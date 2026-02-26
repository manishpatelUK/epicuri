package uk.co.epicuri.serverapi.host.endpoints;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.auth.HostAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupCloneView;
import uk.co.epicuri.serverapi.common.pojo.menu.GroupView;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Category;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Group;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Comparator;
import java.util.Iterator;

@CrossOrigin
@RestController
@RequestMapping(value = "/MenuGroup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class MenuGroupController {

    @Autowired
    private MasterDataService masterDataService;

    private static final Comparator<Group> SORT_BY_ORDER_FIELD = Comparator.comparingInt(Group::getOrder);

    @HostAuthRequired
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> postMenuGroup(@NotNull @RequestBody GroupView groupView) {
        if(StringUtils.isBlank(groupView.getMenuCategoryId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid model state");
        }

        Menu menu = masterDataService.getMenu(IDAble.extractParentId(groupView.getMenuCategoryId()));
        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        Category category = menu.getCategories().stream().filter(c -> c.getId().equals(groupView.getMenuCategoryId())).findFirst().orElse(null);
        if(category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }

        Group group = new Group(category, groupView);
        category.getGroups().add(group);
        menu.setLastUpdate(System.currentTimeMillis());

        masterDataService.upsert(menu);

        groupView.setId(group.getId());
        return ResponseEntity.created(URI.create("/MenuGroup/"+groupView.getId())).body(groupView);
    }

    @HostAuthRequired
    @RequestMapping(value = "/clone/{id}", method = RequestMethod.POST)
    public ResponseEntity<?> postCloneMenuGroup(@PathVariable("id") String id,
                                                @NotNull @RequestBody GroupCloneView groupCloneView) {
        String currentCategoryId = IDAble.extractParentId(id);
        String copyToCategoryId = groupCloneView.getCategoryId() == null ? currentCategoryId : groupCloneView.getCategoryId();
        String copyToMenuId = IDAble.extractParentId(copyToCategoryId);
        String currentMenuId = IDAble.extractParentId(currentCategoryId);
        Menu currentMenu = masterDataService.getMenu(currentMenuId);
        if(currentMenu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        Menu copyToMenu = copyToMenuId.equals(currentMenu.getId()) ? currentMenu : masterDataService.getMenu(copyToMenuId);
        Category copyToCategory = filterCategory(copyToCategoryId, copyToMenu);
        Category copyFromCategory = filterCategory(currentCategoryId, currentMenu);

        if (copyFromCategory == null || copyToCategory == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }

        Group group = null;
        for(Group inGroup: copyFromCategory.getGroups()) {
            if(inGroup.getId().equals(id)) {
                group = inGroup;
                break;
            }
        }
        if(group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        Group copy = group.copyExcludingId();
        copy.setId(IDAble.generateId(copyToCategoryId));
        copy.setName(groupCloneView.getName());
        copy.setOrder(copyToCategory.getGroups().size());
        copyToCategory.getGroups().add(copy);

        copyToMenu.setLastUpdate(System.currentTimeMillis());
        masterDataService.upsert(copyToMenu);

        return ResponseEntity.created(URI.create("/MenuGroup/"+copy.getId())).build();
    }

    private Category filterCategory(String categoryId, Menu menu) {
        Category copyToCategory = menu.getCategories().stream().filter(c -> c.getId().equals(categoryId)).findFirst().orElse(null);
        if(copyToCategory == null) {
            return null;
        }
        return copyToCategory;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putMenuGroup(@PathVariable("id") String id,
                                          @NotNull @RequestBody GroupView groupView) {
        String menuId = IDAble.extractParentId(IDAble.extractParentId(id));
        Menu menu = masterDataService.getMenu(menuId);
        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        if(StringUtils.isBlank(groupView.getMenuCategoryId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid model state");
        }

        Category category = menu.getCategories().stream().filter(c -> c.getId().equals(groupView.getMenuCategoryId())).findFirst().orElse(null);
        if(category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }

        Group group = category.getGroups().stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null);
        if(group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        group.setName(groupView.getGroupName());
        group.setOrder(groupView.getOrder());
        group.getItems().clear();
        group.getItems().addAll(groupView.getMenuItemIds());
        menu.setLastUpdate(System.currentTimeMillis());

        category.getGroups().sort(SORT_BY_ORDER_FIELD);

        masterDataService.upsert(menu);
        groupView.setId(group.getId());
        return ResponseEntity.created(URI.create("/MenuGroup/"+groupView.getId())).body(groupView);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMenuGroup(@PathVariable("id") String id) {
        String categoryId = IDAble.extractParentId(id);
        String menuId = IDAble.extractParentId(categoryId);

        Menu menu = masterDataService.getMenu(menuId);
        if(menu == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Menu not found");
        }

        Category category = menu.getCategories().stream().filter(c -> c.getId().equals(categoryId)).findFirst().orElse(null);
        if(category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }

        Iterator<Group> groupIterator = category.getGroups().iterator();
        while (groupIterator.hasNext()) {
            Group group = groupIterator.next();
            if(group.getId().equals(id)) {
                groupIterator.remove();
                menu.setLastUpdate(System.currentTimeMillis());
                masterDataService.upsert(menu);
                break;
            }
        }

        return ResponseEntity.ok().build();
    }
}
