package uk.co.epicuri.serverapi.host.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.epicuri.serverapi.auth.AuthenticationUtil;
import uk.co.epicuri.serverapi.auth.EpicuriAuthRequired;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.management.FieldEdit;
import uk.co.epicuri.serverapi.common.pojo.management.MenuStructure;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.service.ArchiveDataService;
import uk.co.epicuri.serverapi.service.MasterDataCreationService;
import uk.co.epicuri.serverapi.service.MasterDataService;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "Management", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementController.class);

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private MasterDataCreationService masterDataCreationService;

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity handleOptions() {
        return new ResponseEntity(HttpStatus.OK);
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getEntity(@PathVariable("type") String type,
                                       @PathVariable("id") String id) {
        Object instance = get(type, id);
        if(instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(instance);
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/{type:.+}", method = RequestMethod.GET)
    public ResponseEntity<?> getEntities(@PathVariable("type") String type) {
        return ResponseEntity.ok(get(type));
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Restaurants", method = RequestMethod.GET)
    public ResponseEntity<?> getRestaurants() {
        return ResponseEntity.ok(masterDataService.getRestaurants());
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Printers/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getPrinters(@PathVariable("id") String id) {
        return ResponseEntity.ok(masterDataService.getPrinters(id));
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Staffs/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getStaff(@PathVariable("id") String id) {
        return ResponseEntity.ok(masterDataService.getAllStaff(id));
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/RestaurantId", method = RequestMethod.GET)
    public ResponseEntity<?> getNextRestaurantId() {
        return ResponseEntity.ok(new IdPojo(masterDataService.getNextRestaurantId()));
    }

    @EpicuriAuthRequired
    @RequestMapping(value = Restaurant.RECEIPT_IMAGE_ENDPOINT + "/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postReceiptImage(@PathVariable("id") String id,
                                              @RequestParam(value = "receiptImageURL", required = false) String imageId,
                                              @NotNull @RequestBody byte[] fileBytes) {
        LOGGER.debug("Attempt to add/update image ({} bytes) for restaurant {} and image id {}", fileBytes.length, id, imageId);

        Restaurant restaurant = masterDataService.getRestaurant(id);
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        RestaurantImage image = getRestaurantImage(id, imageId, RestaurantImageType.BILL_LOGO);
        if(image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        image.setImage(fileBytes);
        imageId = masterDataService.upsert(image).getId();

        restaurant.setReceiptImageURL(imageId);
        masterDataService.upsert(restaurant);

        return ResponseEntity.ok(new IdPojo(imageId));
    }

    @EpicuriAuthRequired
    @RequestMapping(value = Floor.FLOOR_IMAGE_ENDPOINT + "/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postFloorImage(@PathVariable("id") String id,
                                            @RequestParam(value = "imageURL", required = false) String imageId,
                                            @NotNull @RequestBody byte[] fileBytes) {
        LOGGER.debug("Attempt to add/update image ({} bytes) for restaurant {} and floor image id {}", fileBytes.length, id, imageId);

        Restaurant restaurant = masterDataService.getRestaurant(IDAble.extractParentId(id));
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        RestaurantImage image = getRestaurantImage(restaurant.getId(), imageId, RestaurantImageType.FLOOR_PLAN);
        if(image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        image.setImage(fileBytes);
        imageId = masterDataService.upsert(image).getId();

        for(Floor floor : restaurant.getFloors()) {
            if(floor.getId().equals(id)) {
                LOGGER.debug("Change floor id {}-{}", floor.getImageURL(), imageId);
                floor.setImageURL(imageId);
            }
        }

        masterDataService.upsert(restaurant);

        return ResponseEntity.ok(new IdPojo(imageId));
    }

    private RestaurantImage getRestaurantImage(String restaurantId,
                                               String imageId,
                                               RestaurantImageType type) {
        RestaurantImage image = null;
        if(StringUtils.isBlank(imageId)) {
            image = new RestaurantImage();
            image.setRestaurantId(restaurantId);
            image.setImageType(type);
        } else {
            image = masterDataService.getRestaurantImage(imageId);
        }
        return image;
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putEntity(@PathVariable("type") String type,
                                       @PathVariable("id") String id,
                                       @RequestBody Map<String,Object> map) {
        Object instance = get(type, id);
        if(instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        LOGGER.debug("Put for object {} id:{} -> {}", type, id, map.getClass());

        save(type, map);
        return ResponseEntity.ok().build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteEntity(@PathVariable("type") String type,
                                          @PathVariable("id") String id) {
        Object instance = get(type, id);
        if(instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        LOGGER.debug("Delete for object {} + {} -> {}", type, id, instance);

        try {
            delete(type, id);
        } catch(IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Orders/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteOrders(@PathVariable("id") String id) {
        archiveDataService.clearOrders(id);

        return ResponseEntity.ok().build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff", method = RequestMethod.POST)
    public ResponseEntity<?> postStaffEntity(@RequestBody Staff staff){
        LOGGER.debug("Post for staff {} ", staff);

        staff.setMash(AuthenticationUtil.getPasswordMash(staff, staff.getMash()));
        if(StringUtils.isEmpty(staff.getId())) {
            staff.setId(null); //just in case it is ""
        }

        masterDataService.upsert(staff);

        return ResponseEntity.ok().build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff", method = RequestMethod.PUT)
    public ResponseEntity<?> putStaffEntity(@RequestBody Staff staff){
        LOGGER.debug("Put for staff {} ", staff);

        staff.setMash(AuthenticationUtil.getPasswordMash(staff, staff.getMash()));
        if(StringUtils.isEmpty(staff.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        masterDataService.upsert(staff);

        return ResponseEntity.ok().build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/{type:.+}", method = RequestMethod.POST)
    public ResponseEntity<?> postEntity(@PathVariable("type") String type, @RequestBody Map<String,Object> map){
        LOGGER.debug("Post for object {} -> ({}) {}", type, map.getClass(), map);

        try {
            return ResponseEntity.ok(insert(type, map));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/SingleField/{type}/{id}", method = RequestMethod.PUT)
    public ResponseEntity<?> putEntity(@PathVariable("type") String type,
                                       @PathVariable("id") String id,
                                       @RequestBody FieldEdit fieldEdit) {
        if(!validate(fieldEdit)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is not valid");
        }

        Object instance = get(type, id);
        if(instance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        LOGGER.debug("Put field for object {} + {} + {} -> {}", type, id, fieldEdit, instance);

        updateField(fieldEdit.getFieldName(), fieldEdit.getEditedObject(), instance);
        save(type, instance);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Reset/Defaults", method = RequestMethod.POST)
    public ResponseEntity<?> postResetDefaults() {
        LOGGER.warn("About to reset global defaults!!");
        masterDataCreationService.recreateDefaults();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Reset/Adjustments", method = RequestMethod.POST)
    public ResponseEntity<?> postResetDefaultsAdjustments() {
        LOGGER.warn("About to reset global defaults!!");
        masterDataCreationService.createDefaultAdjustments();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Reset/Preferences", method = RequestMethod.POST)
    public ResponseEntity<?> postResetDefaultPreferences() {
        LOGGER.warn("About to reset preferences!!");
        masterDataCreationService.recreateDefaultPreferences();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Reset/BookingStatics", method = RequestMethod.POST)
    public ResponseEntity<?> postResetBookingStatics() {
        LOGGER.warn("About to reset booking statics!!");
        masterDataCreationService.recreateBookingStatics();

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Maintenance/Defaults", method = RequestMethod.POST)
    public ResponseEntity<?> postMaintenanceDefaults() {
        List<Restaurant> restaurants = masterDataService.getRestaurants();
        Map<String,Default> defaults = masterDataService.getDefaults().stream().collect(Collectors.toMap(Default::getName, Function.identity()));

        //recreate any required defaults
        boolean defaultsAdded = false;
        for(Default def : MasterDataCreationService.allDefaults()) {
            if(!defaults.containsKey(def.getName())) {
                defaultsAdded = true;
                masterDataService.upsert(def);
            }
        }
        if(defaultsAdded) {
            defaults = masterDataService.getDefaults().stream().collect(Collectors.toMap(Default::getName, Function.identity()));
        }

        List<Restaurant> updatedRestaurants = new ArrayList<>();
        for(Restaurant restaurant : restaurants) {
            Map<String,RestaurantDefault> restaurantDefaultMap = restaurant.getRestaurantDefaults().stream().collect(Collectors.toMap(RestaurantDefault::getName, Function.identity()));
            boolean changed = false;
            for(String masterDefaultKey : defaults.keySet()) {
                if(!restaurantDefaultMap.containsKey(masterDefaultKey)) {
                    changed = true;
                    restaurant.getRestaurantDefaults().add(new RestaurantDefault(defaults.get(masterDefaultKey)));
                }
            }
            if(changed) {
                updatedRestaurants.add(restaurant);
            }
        }
        if(updatedRestaurants.size() > 0) {
            masterDataService.upsertRestaurants(updatedRestaurants);
        }
        return ResponseEntity.ok(new StringMessage("Updated " + updatedRestaurants.size() + " restaurant objects"));
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Maintenance/UpdateMenuOrdering", method = RequestMethod.POST)
    public ResponseEntity<?> postUpdateMenuOrdering() {
        LOGGER.warn("About to reset menu orders!!");
        List<Restaurant> restaurants = masterDataService.getRestaurants();
        for(Restaurant restaurant : restaurants) {
            List<Menu> menus = masterDataService.getMenus(restaurant.getId());
            for(int i = 0; i < menus.size(); i++) {
                if(menus.get(i).getOrder() != i) {
                    menus.get(i).setOrder(i);
                    masterDataService.upsert(menus.get(i));
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Maintenance/UpdatePermissions", method = RequestMethod.POST)
    public ResponseEntity<?> postUpdatePermissions() {
        LOGGER.warn("About to update permissions!!");
        List<Restaurant> restaurants = masterDataService.getRestaurants();
        for(Restaurant restaurant : restaurants) {
            StaffPermissions defaultPermissions = MasterDataCreationService.createDefaultPermissions(restaurant.getId());
            if(restaurant.getStaffPermissions() == null) {
                restaurant.setStaffPermissions(defaultPermissions);
                masterDataService.upsert(restaurant);
                continue;
            }
            List<IndividualStaffPermission> currentRestaurantPermissions = restaurant.getStaffPermissions().getPermissions();
            boolean doSave = false;
            for(IndividualStaffPermission individualStaffPermission : defaultPermissions.getPermissions()) {
                IndividualStaffPermission currentPermissionForRole = currentRestaurantPermissions.stream().filter(p -> p.getRole() == individualStaffPermission.getRole()).findFirst().orElse(null);
                if(currentPermissionForRole == null) {
                    currentRestaurantPermissions.add(individualStaffPermission);
                    doSave = true;
                } else {
                    Set<WaiterAppFeature> defaultFeatures = new HashSet<>(individualStaffPermission.getPermissions().keySet());
                    defaultFeatures.removeIf(k -> currentPermissionForRole.getPermissions().containsKey(k));
                    if(defaultFeatures.size() > 0) {
                        defaultFeatures.forEach(key -> {
                            boolean value = individualStaffPermission.getPermissions().get(key);
                            currentPermissionForRole.getPermissions().put(key,value);
                        });
                        doSave = true;
                    }
                }
            }
            if(doSave) {
                masterDataService.upsert(restaurant);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Static/MenuStructure/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMenuStructure(@PathVariable("id") String id) {
        Restaurant restaurant = masterDataService.getRestaurant(id);
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        MenuStructure menuStructure = getMenuStructure(restaurant);

        return ResponseEntity.ok(menuStructure);
    }

    public MenuStructure getMenuStructure(Restaurant restaurant) {
        List<Menu> menus = masterDataService.getMenus(restaurant.getId());
        List<ModifierGroup> groups = masterDataService.getModifierGroupsByRestaurant(restaurant.getId());
        List<Modifier> modifiers = new ArrayList<>();
        groups.forEach(g -> modifiers.addAll(g.getModifiers()));
        List<MenuItem> items = masterDataService.getAllMenuItemsNotDeleted(restaurant.getId());

        Service defaultService = restaurant.getServices().stream().filter(Service::isDefaultService).findFirst().orElse(null);
        if(defaultService == null) {
            return null;
        }
        Map<String,String> courseIdToName = defaultService.getCourses().stream().collect(Collectors.toMap(Course::getId, Course::getName));

        MenuStructure menuStructure = new MenuStructure();
        menuStructure.setMenus(menus);
        for(Menu menu : menus) {
            for(Category category : menu.getCategories()) {
                if(category.getCourseIds().size() > 0) {
                    menuStructure.getMenuAndCategoryToCourseName().put(menu.getName() + ">" + category.getName(), courseIdToName.get(category.getCourseIds().get(0)));
                }
            }
        }
        Set<String> courseNames = new HashSet<>();
        restaurant.getServices().forEach(s -> s.getCourses().forEach(c->courseNames.add(c.getName())));
        menuStructure.setCourseNames(new ArrayList<>(courseNames));
        menuStructure.setModifierGroups(groups);
        menuStructure.setModifiers(modifiers);
        menuStructure.setItems(items);
        return menuStructure;
    }

    @EpicuriAuthRequired
    @RequestMapping(value = "/Static/MenuUpload/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putMenuStructure(@PathVariable("id") String id,
                                              @RequestBody MenuStructure newMenuStructure) {
        Restaurant restaurant = masterDataService.getRestaurant(id);
        if(restaurant == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //check courses
        Service defaultService = restaurant.getServices().stream().filter(Service::isDefaultService).findFirst().orElse(null);
        if(defaultService == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Default service not found");
        }
        Map<String, Course> courseByName = defaultService.getCourses().stream().collect(Collectors.toMap(Course::getName, Function.identity()));
        boolean saveService = false;
        for(String courseName : newMenuStructure.getCourseNames()) {
            if(!courseByName.containsKey(courseName)) {
                Course course = new Course(defaultService);
                course.setName(courseName);
                course.setOrdering((short)courseByName.size());
                courseByName.put(courseName, course);
                defaultService.getCourses().add(course);
                saveService = true;
            }
        }
        if(saveService) {
            masterDataService.upsert(restaurant);
        }

        //upsert any new/updated modifiers
        List<Modifier> newModifiers = newMenuStructure.getModifiers().stream().filter(m -> m.getId() != null && m.getId().startsWith("-")).collect(Collectors.toList());
        Map<String,Modifier> pseudoIdToModifier = new HashMap<>();
        addNewModifiers(newModifiers, pseudoIdToModifier);

        //update the other modifiers, even if same
        List<Modifier> oldModifiers = newMenuStructure.getModifiers().stream().filter(m -> m.getId() != null && !newModifiers.contains(m)).collect(Collectors.toList());
        masterDataService.upsertModifiers(oldModifiers);
        Map<String,Modifier> existingIdToModifier = oldModifiers.stream().collect(Collectors.toMap(Modifier::getId, Function.identity()));

        //upsert any new groups
        newMenuStructure.getModifierGroups().forEach(m -> m.setRestaurantId(restaurant.getId()));
        List<ModifierGroup> newModifiersGroups = newMenuStructure.getModifierGroups().stream().filter(m -> m.getId() != null && m.getId().startsWith("-")).collect(Collectors.toList());
        Map<String,ModifierGroup> pseudoIdToModifierGroup = new HashMap<>();
        addNewModifierGroups(pseudoIdToModifierGroup, newModifiersGroups, pseudoIdToModifier, existingIdToModifier);

        //update the other modifier groups, even if same
        List<ModifierGroup> oldModifierGroups = newMenuStructure.getModifierGroups().stream().filter(m -> m.getId() != null && !newModifiersGroups.contains(m)).collect(Collectors.toList());
        updateExistingGroupWithModifiers(pseudoIdToModifier, existingIdToModifier, oldModifierGroups);
        masterDataService.upsertModifierGroups(oldModifierGroups);

        //upsert any new menu items
        newMenuStructure.getItems().forEach(m -> m.setRestaurantId(restaurant.getId()));
        List<MenuItem> newMenuItems = newMenuStructure.getItems().stream().filter(m -> m.getId() != null && m.getId().startsWith("-")).collect(Collectors.toList());
        updateModifierGroupIds(newMenuItems, pseudoIdToModifierGroup);
        Map<String,MenuItem> pseudoIdToItem = new HashMap<>();
        addNewItems(newMenuItems, pseudoIdToItem);

        //upsert old menu items
        List<MenuItem> oldMenuItems = newMenuStructure.getItems().stream().filter(m -> m.getId() != null && !newMenuItems.contains(m)).collect(Collectors.toList());
        updateModifierGroupIds(oldMenuItems, pseudoIdToModifierGroup);
        masterDataService.upsertMenuItems(oldMenuItems);

        //upsert menus - replace group's menu ids with the right ones
        Map<String,MenuItem> currentMenuItems = masterDataService.getAllMenuItemsNotDeleted(restaurant.getId()).stream().collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        List<Menu> menusToAdd = new ArrayList<>();
        for(Menu menu : newMenuStructure.getMenus()) {
            if(menu.getId() == null) {
                List<Category> categories = menu.getCategories();
                menu.setCategories(new ArrayList<>());
                menu.setLastUpdate(System.currentTimeMillis());
                menu.setRestaurantId(restaurant.getId());
                menu = masterDataService.upsert(menu);
                //now add back categories
                menu.setCategories(categories);
            }
            menusToAdd.add(menu);
        }

        for(Menu menu : menusToAdd) {
            for(Category category : menu.getCategories()) {
                if(category.getId() == null) {
                    category.setId(IDAble.generateId(menu.getId()));
                }

                String key = menu.getName() + ">" + category.getName();
                String courseName = newMenuStructure.getMenuAndCategoryToCourseName().get(key);
                if(courseByName.containsKey(courseName)) {
                    category.getCourseIds().add(courseByName.get(courseName).getId());
                }

                for(Group group : category.getGroups()) {
                    if(group.getId() == null) {
                        group.setId(IDAble.generateId(category.getId()));
                    }
                    List<String> menuItemsIds = new ArrayList<>();
                    for(String itemId : group.getItems()) {
                        if(itemId.startsWith("-") && pseudoIdToItem.containsKey(itemId)) {
                            menuItemsIds.add(pseudoIdToItem.get(itemId).getId());
                        } else if(currentMenuItems.containsKey(itemId)){
                            menuItemsIds.add(itemId);
                        }
                    }
                    group.setItems(menuItemsIds);
                }
            }
            masterDataService.upsert(menu);
        }

        return ResponseEntity.ok().build();
    }

    public void updateExistingGroupWithModifiers(Map<String, Modifier> pseudoIdToModifier, Map<String, Modifier> existingIdToModifier, List<ModifierGroup> oldModifierGroups) {
        for(ModifierGroup modifierGroup : oldModifierGroups) {
            for(Modifier modifier : modifierGroup.getModifiers()) {
                List<Modifier> newModifierList = new ArrayList<>();
                if(modifier.getId().startsWith("-") && pseudoIdToModifier.containsKey(modifier.getId())) {
                    newModifierList.add(pseudoIdToModifier.get(modifier.getId()));
                } else if(!modifier.getId().startsWith("-") && existingIdToModifier.containsKey(modifier.getId())) {
                    newModifierList.add(existingIdToModifier.get(modifier.getId()));
                }

                modifierGroup.setModifiers(newModifierList);
            }
        }
    }

    private void updateModifierGroupIds(List<MenuItem> menuItems, Map<String, ModifierGroup> pseudoIdToModifier) {
        for(MenuItem item : menuItems) {
            if(item.getModifierGroupIds() != null && item.getModifierGroupIds().size() > 0) {
                List<String> actualIds = new ArrayList<>();
                for(String id : item.getModifierGroupIds()) {
                    if(id.startsWith("-") && pseudoIdToModifier.containsKey(id)) {
                        actualIds.add(pseudoIdToModifier.get(id).getId());
                    } else if(!id.startsWith("-")) {
                        actualIds.add(id);
                    }
                }
                item.setModifierGroupIds(actualIds);
            }
        }
    }

    private void addNewModifierGroups(Map<String, ModifierGroup> pseudoIdToModifierGroup, List<ModifierGroup> newModifiersGroups, Map<String, Modifier> pseudoIdToModifier, Map<String, Modifier> idToModifier) {
        for(ModifierGroup modifierGroup : newModifiersGroups) {
            String id = modifierGroup.getId();
            modifierGroup.setId(null);
            List<Modifier> modifiers = modifierGroup.getModifiers();
            modifierGroup.setModifiers(null);
            ModifierGroup newModifierGroup = masterDataService.upsert(modifierGroup);
            //for each modifier, find the actual modifier and put it in
            List<Modifier> actualModifiers = new ArrayList<>();
            for(Modifier modifier : modifiers) {
                if(modifier.getId() == null) {
                    continue;
                }

                if(modifier.getId().startsWith("-") && pseudoIdToModifier.containsKey(modifier.getId())) {
                    actualModifiers.add(pseudoIdToModifier.get(modifier.getId()));
                } else if(!modifier.getId().startsWith("-")){
                    //existing
                    actualModifiers.add(modifier);
                }
                newModifierGroup.setModifiers(actualModifiers);
            }

            masterDataService.upsert(newModifierGroup);
            pseudoIdToModifierGroup.put(id, newModifierGroup);
        }
    }

    private void addNewModifiers(List<Modifier> newModifiers, Map<String, Modifier> pseudoIdToModifier) {
        newModifiers.forEach(m -> {
            String pid = m.getId();
            m.setId(null);
            Modifier newMod = masterDataService.upsert(m);
            pseudoIdToModifier.put(pid, newMod);
        });
    }

    private void addNewItems(List<MenuItem> newItems, Map<String, MenuItem> pseudoIdToModifier) {
        newItems.forEach(m -> {
            String pid = m.getId();
            m.setId(null);
            MenuItem newItem = masterDataService.upsert(m);
            pseudoIdToModifier.put(pid, newItem);
        });
    }

    @SuppressWarnings("unchecked")
    public static void updateField(String fieldName, Object newValue, Object instance) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            if(field.getType().isEnum()) {
                field.set(instance, Enum.valueOf((Class<Enum>) field.getType(), newValue.toString()));
            } else {
                field.set(instance, newValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.warn("Could not modify field {} due to error: {}", fieldName, e.getMessage());
        }
    }

    private boolean validate(FieldEdit fieldEdit) {
        if(fieldEdit.isNullify()) {
            return fieldEdit.getFieldName() != null;
        } else {
            return fieldEdit.getEditedObject() != null && fieldEdit.getFieldName() != null;
        }
    }

    public Object get(String type, String id) {
        LOGGER.trace("Get {} for id {}", type, id);
        try {
            Method method = MasterDataService.class.getMethod("get"+getTypeMethod(type), String.class);
            Object returnedObject = method.invoke(masterDataService, id);
            LOGGER.trace("Got {}", returnedObject);
            return returnedObject;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Could not get by reflection", e);
            return null;
        }
    }

    public Object get(String type) {
        LOGGER.trace("Get {}s", type);
        try {
            Method method = MasterDataService.class.getMethod("get"+getTypeMethod(type));
            Object returnedObject = method.invoke(masterDataService);
            LOGGER.trace("Got {}", returnedObject);
            return returnedObject;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Could not get by reflection", e);
            return null;
        }
    }

    public void save(String type, Object object) {
        LOGGER.trace("Upsert {} for object: {}", type, object);
        try {
            Method method = MasterDataService.class.getMethod("upsert", object.getClass());
            method.invoke(masterDataService, object);
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Could not get by reflection", e);
        }
    }

    public void save(String type, Map<String,Object> map) {
        LOGGER.trace("Upsert {} for object: {}", type, map);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object object = mapper.convertValue(map, Class.forName(type));

            Method method = MasterDataService.class.getMethod("upsert", object.getClass());
            method.invoke(masterDataService, object);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException | ClassCastException | ClassNotFoundException e) {
            LOGGER.error("Could not get by reflection", e);
        }
    }

    public void delete(String type, String id) throws IllegalArgumentException{
        LOGGER.trace("Delete {} for id {}", type, id);
        try {
            Method method = MasterDataService.class.getMethod("delete"+getTypeMethod(type), String.class);
            method.invoke(masterDataService, id);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Could not get by reflection", e);
            throw new IllegalArgumentException("Cannot delete " + id + " of type " + type);
        }
    }

    public IDAble insert(String type, Map<String,Object> map) throws IllegalArgumentException{
        try {
            ObjectMapper mapper = new ObjectMapper();
            final Class<?> toValueType = Class.forName(type);
            Object object = mapper.convertValue(map, toValueType);
            //if object is IDAble and is not assigned an ID from the database, it should be rejected
            if(object instanceof IDAble && StringUtils.isBlank(((IDAble)object).getId()) && !toValueType.isAnnotationPresent(Document.class)) {
                throw new IllegalArgumentException("Cannot add IDAble without an ID when it is not auto-assigned");
            }

            if(object instanceof Restaurant) {
                return insert((Restaurant)object);
            }

            Method method = MasterDataService.class.getMethod("insert"+getTypeMethod(type), toValueType);
            return (IDAble)method.invoke(masterDataService, object);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NullPointerException | ClassCastException | ClassNotFoundException e) {
            LOGGER.error("Could not get by reflection", e);
            throw new IllegalArgumentException("Cannot insert data");
        }
    }

    private Restaurant insert(Restaurant restaurant) {
        masterDataCreationService.createRestaurantWithDefaultMasterData(restaurant);
        return restaurant;
    }

    private String getTypeMethod(String type) {
        String[] bits = type.split("\\.");
        return bits[bits.length-1];
    }
}
