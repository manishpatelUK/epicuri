package uk.co.epicuri.serverapi.host.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.Params;
import uk.co.epicuri.serverapi.common.pojo.management.MenuStructure;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.repository.BookingStaticsRepository;
import uk.co.epicuri.serverapi.service.MasterDataCreationService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.*;

/**
 * Created by Manish Patel
 */
public class ManagementControllerTest extends BaseIT {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Autowired
    private BookingStaticsRepository bookingStaticsRepository;

    private ManagementController managementController;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        managementController = new ManagementController();
        autowireCapableBeanFactory.autowireBean(managementController);

        staff1.setRestaurantId(restaurant1.getId());
        staff1.setRole(StaffRole.EPICURI_ADMIN);
        staff1 = staffRepository.save(staff1);
    }

    @Test
    public void testGet() throws Exception {
        assertEquals(restaurant2, managementController.get(Restaurant.class.getCanonicalName(), restaurant2.getId()));
        assertEquals(adjustmentType2, managementController.get(AdjustmentType.class.getCanonicalName(), adjustmentType2.getId()));
        assertEquals(country2, managementController.get(Country.class.getCanonicalName(), country2.getId()));
        assertEquals(cuisine2, managementController.get(Cuisine.class.getCanonicalName(), cuisine2.getId()));
        Default aDefault = defaultsRepository.findAll().get(2);
        assertEquals(aDefault, managementController.get(Default.class.getCanonicalName(), aDefault.getId()));
        assertEquals(tax2, managementController.get(TaxRate.class.getCanonicalName(), tax2.getId()));
    }

    @Test
    public void testSave() throws Exception {
        restaurant2.setName("foo");
        managementController.save(Restaurant.class.getCanonicalName(), restaurant2);
        assertEquals(restaurant2, restaurantRepository.findOne(restaurant2.getId()));

        adjustmentType2.setName("foo");
        managementController.save(AdjustmentType.class.getCanonicalName(), adjustmentType2);
        assertEquals(adjustmentType2, adjustmentTypeRepository.findOne(adjustmentType2.getId()));

        country2.setName("foo");
        managementController.save(Country.class.getCanonicalName(), country2);
        assertEquals(country2, countryRepository.findOne(country2.getId()));

        cuisine2.setName("foo");
        managementController.save(Cuisine.class.getCanonicalName(), cuisine2);
        assertEquals(cuisine2, cuisineRepository.findOne(cuisine2.getId()));

        Default aDefault = defaultsRepository.findAll().get(2);
        aDefault.setName("foo");
        managementController.save(Default.class.getCanonicalName(), aDefault);
        assertEquals(aDefault, defaultsRepository.findOne(aDefault.getId()));

        tax2.setName("foo");
        managementController.save(TaxRate.class.getCanonicalName(), tax2);
        assertEquals(tax2, taxRateRepository.findOne(tax2.getId()));
    }

    @Test
    public void testUpdateField() throws Exception {
        ManagementController.updateField("name", "foo", restaurant2);
        assertEquals("foo", restaurant2.getName());

        ManagementController.updateField("type", AdjustmentTypeType.DISCOUNT.toString(), adjustmentType1);
        assertEquals(AdjustmentTypeType.DISCOUNT, adjustmentType1.getType());

        ManagementController.updateField("type", AdjustmentTypeType.PAYMENT.toString(), adjustmentType1);
        assertEquals(AdjustmentTypeType.PAYMENT, adjustmentType1.getType());
    }

    @Test
    public void testDelete() throws Exception {
        managementController.delete(Restaurant.class.getCanonicalName(), restaurant2.getId());
        assertNull(restaurantRepository.findOne(restaurant2.getId()));

        managementController.delete(AdjustmentType.class.getCanonicalName(), adjustmentType2.getId());
        assertNull(adjustmentTypeRepository.findOne(adjustmentType2.getId()));

        managementController.delete(Country.class.getCanonicalName(), country2.getId());
        assertNull(countryRepository.findOne(country2.getId()));

        managementController.delete(Cuisine.class.getCanonicalName(), cuisine2.getId());
        assertNull(cuisineRepository.findOne(cuisine2.getId()));

        Default aDefault = defaultsRepository.findAll().get(2);
        managementController.delete(Default.class.getCanonicalName(), aDefault.getId());
        assertNull(defaultsRepository.findOne(aDefault.getId()));

        managementController.delete(TaxRate.class.getCanonicalName(), tax2.getId());
        assertNull(taxRateRepository.findOne(tax2.getId()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInsert() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Object object = new Restaurant();
        String id = managementController.insert(Restaurant.class.getCanonicalName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(restaurantRepository.findOne(id));

        object = new AdjustmentType();
        id = managementController.insert(AdjustmentType.class.getCanonicalName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(adjustmentTypeRepository.findOne(id));

        object = new Country();
        id = managementController.insert(Country.class.getCanonicalName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(countryRepository.findOne(id));

        object = new Cuisine();
        id = managementController.insert(Cuisine.class.getCanonicalName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(cuisineRepository.findOne(id));

        object = new Default();
        id = managementController.insert(Default.class.getCanonicalName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(defaultsRepository.findOne(id));

        object = new TaxRate();
        id = managementController.insert(TaxRate.class.getCanonicalName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(taxRateRepository.findOne(id));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSave2() throws Exception {
        ObjectMapper mapper = new ObjectMapper();


        Object object = restaurant1;
        restaurant1.setName("fop");
        managementController.save(Restaurant.class.getCanonicalName(), mapper.convertValue(object, Map.class));
        assertEquals("fop",restaurantRepository.findOne(restaurant1.getId()).getName());

        /*object = new AdjustmentType();
        id = managementController.insert(ManagedPojoEnum.ADJUSTMENT_TYPE.getName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(adjustmentTypeRepository.findOne(id));

        object = new Country();
        id = managementController.insert(ManagedPojoEnum.COUNTRY.getName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(countryRepository.findOne(id));

        object = new Cuisine();
        id = managementController.insert(ManagedPojoEnum.CUISINE.getName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(cuisineRepository.findOne(id));

        object = new Default();
        id = managementController.insert(ManagedPojoEnum.DEFAULT.getName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(defaultsRepository.findOne(id));

        object = new TaxRate();
        id = managementController.insert(ManagedPojoEnum.TAX.getName(), mapper.convertValue(object, Map.class)).getId();
        assertNotNull(taxRateRepository.findOne(id));*/
    }

    @Test
    public void testClearOrders() throws Exception {
        menuItem1.setRestaurantId(restaurant1.getId());
        order1.setSessionId(session1.getId());
        session1.setRestaurantId(restaurant1.getId());
        batch1.setSessionId(session1.getId());

        menuItemRepository.save(menuItem1);
        orderRepository.save(order1);
        sessionRepository.save(session1);
        batchRepository.save(batch1);

        managementController.deleteOrders(restaurant2.getId());

        assertNotNull(menuItemRepository.findOne(menuItem1.getId()));
        assertNotNull(orderRepository.findOne(order1.getId()));
        assertNotNull(sessionRepository.findOne(session1.getId()));
        assertNotNull(batchRepository.findOne(batch1.getId()));

        managementController.deleteOrders(restaurant1.getId());

        assertNotNull(menuItemRepository.findOne(menuItem1.getId()));
        assertNull(orderRepository.findOne(order1.getId()));
        assertNull(sessionRepository.findOne(session1.getId()));
        assertNull(batchRepository.findOne(batch1.getId()));
    }

    @Test
    public void testResetDefaults() throws Exception {
        Default aDefault = masterDataService.getDefaultByName(FixedDefaults.DEFAULT_TIP_PERCENTAGE);
        aDefault.setValue(10000D);
        aDefault.setDescription("foobar");
        defaultsRepository.save(aDefault);

        managementController.postResetDefaults();

        aDefault = masterDataService.getDefaultByName(FixedDefaults.DEFAULT_TIP_PERCENTAGE);
        assertNotEquals(10000D, aDefault.getValue());
        assertNotEquals("foobar", aDefault.getDescription());
    }

    @Test
    public void testPostMaintenanceDefaults1() throws Exception {
        List<Default> all = MasterDataCreationService.allDefaults();
        restaurant1.getRestaurantDefaults().clear();
        restaurant2.getRestaurantDefaults().clear();
        restaurant3.getRestaurantDefaults().clear();
        for(Default def : all) {
            restaurant1.getRestaurantDefaults().add(new RestaurantDefault(def));
            restaurant2.getRestaurantDefaults().add(new RestaurantDefault(def));
            restaurant3.getRestaurantDefaults().add(new RestaurantDefault(def));
        }
        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);


        RestaurantDefault restaurantDefault = restaurant1.getRestaurantDefaults().remove(7);
        restaurantRepository.save(restaurant1);
        assertNull(restaurantRepository.findOne(restaurant1.getId()).getRestaurantDefaults().stream().filter(r -> r.getName().equals(restaurantDefault.getName())).findFirst().orElse(null));

        managementController.postMaintenanceDefaults();
        List<RestaurantDefault> list1 = restaurant2.getRestaurantDefaults();
        List<RestaurantDefault> list2 = restaurantRepository.findOne(restaurant2.getId()).getRestaurantDefaults();
        list1.sort(Comparator.comparing(RestaurantDefault::getName));
        list2.sort(Comparator.comparing(RestaurantDefault::getName));
        assertEquals(list1, list2);
        List<RestaurantDefault> list3 = restaurant3.getRestaurantDefaults();
        List<RestaurantDefault> list4 = restaurantRepository.findOne(restaurant3.getId()).getRestaurantDefaults();
        list3.sort(Comparator.comparing(RestaurantDefault::getName));
        list4.sort(Comparator.comparing(RestaurantDefault::getName));
        assertEquals(list3, list4);
        assertNotNull(restaurantRepository.findOne(restaurant1.getId()).getRestaurantDefaults().stream().filter(r -> r.getName().equals(restaurantDefault.getName())).findFirst().orElse(null));
    }

    @Test
    public void testPostMaintenanceDefaults2() throws Exception {
        Default def = masterDataService.getDefault().get(7);
        masterDataService.deleteDefault(def.getId());

        managementController.postMaintenanceDefaults();

        assertNotNull(masterDataService.getDefault().stream().filter(d -> d.getName().equals(def.getName())).findFirst().orElse(null));
    }

    @Test
    public void testPostMaintenanceUpdatePermissions1() throws Exception {
        restaurant1.setStaffPermissions(MasterDataCreationService.createDefaultPermissions(restaurant1.getId()));
        restaurant1.getStaffPermissions().getPermissions().clear();
        restaurant2.setStaffPermissions(MasterDataCreationService.createDefaultPermissions(restaurant2.getId()));
        restaurant2.getStaffPermissions().getPermissions().removeIf(p -> p.getRole() == StaffRole.WAIT_STAFF);
        restaurant3.setStaffPermissions(MasterDataCreationService.createDefaultPermissions(restaurant3.getId()));
        IndividualStaffPermission individualStaffPermission = restaurant3.getStaffPermissions().getPermissions().stream().filter(p -> p.getRole() == StaffRole.MANAGER).findFirst().orElse(null);
        individualStaffPermission.getPermissions().remove(WaiterAppFeature.CASH_UP_SIMULATION);
        restaurantRepository.save(restaurant1);
        restaurantRepository.save(restaurant2);
        restaurantRepository.save(restaurant3);

        managementController.postUpdatePermissions();

        StaffPermissions defaultPermissions = MasterDataCreationService.createDefaultPermissions("");
        Restaurant r1 = restaurantRepository.findOne(restaurant1.getId());
        Restaurant r2 = restaurantRepository.findOne(restaurant2.getId());
        Restaurant r3 = restaurantRepository.findOne(restaurant3.getId());

        assertPermissions(r1.getStaffPermissions(), defaultPermissions);
        assertPermissions(r2.getStaffPermissions(), defaultPermissions);
        assertPermissions(r3.getStaffPermissions(), defaultPermissions);
    }

    @Test
    public void testPostMaintenanceUpdatePermissions2() throws Exception {
        restaurant1.setStaffPermissions(MasterDataCreationService.createDefaultPermissions(restaurant1.getId()));
        IndividualStaffPermission individualStaffPermission = restaurant1.getStaffPermissions().getPermissions().stream().filter(p -> p.getRole() == StaffRole.THIRD_PARTY).findFirst().orElse(null);
        individualStaffPermission.getPermissions().put(WaiterAppFeature.CASH_UP_SIMULATION, true);
        individualStaffPermission.getPermissions().put(WaiterAppFeature.DRAWER_KICK_NO_SALE, true);
        restaurantRepository.save(restaurant1);
        managementController.postUpdatePermissions();

        Restaurant r1 = restaurantRepository.findOne(restaurant1.getId());
        assertPermissions(r1.getStaffPermissions(), restaurant1.getStaffPermissions());
    }

    private void assertPermissions(StaffPermissions currentRestaurantPermissions, StaffPermissions defaultPermissions) {
        assertEquals(currentRestaurantPermissions.getPermissions().size(), defaultPermissions.getPermissions().size());
        for(IndividualStaffPermission individualStaffPermission : defaultPermissions.getPermissions()) {
            IndividualStaffPermission rolePermission = currentRestaurantPermissions.getPermissions().stream().filter(p -> p.getRole() == individualStaffPermission.getRole()).findFirst().orElse(null);
            assertEquals(individualStaffPermission.getPermissions().size(), rolePermission.getPermissions().size());
            for(Map.Entry<WaiterAppFeature,Boolean> entry : individualStaffPermission.getPermissions().entrySet()) {
                assertEquals(rolePermission.getPermissions().get(entry.getKey()), entry.getValue());
            }
        }
    }

    @Test
    public void testResetBookingStatics() throws Exception {
        bookingStaticsRepository.deleteAll();
        managementController.postResetBookingStatics();
        assertEquals(2, bookingStaticsRepository.findAll().size());
    }

    @Test
    public void testGetMenuStructure() throws Exception {
        setUpDefaultMenuStructure();

        MenuStructure menuStructure = managementController.getMenuStructure(restaurant1);

        assertEquals(3, menuStructure.getItems().size());
        assertNotNull(menuStructure.getItems().stream().filter(m -> m.equals(menuItem1)).findFirst().orElse(null));
        assertNotNull(menuStructure.getItems().stream().filter(m -> m.equals(menuItem2)).findFirst().orElse(null));
        assertNotNull(menuStructure.getItems().stream().filter(m -> m.equals(menuItem3)).findFirst().orElse(null));
        assertEquals(1, menuStructure.getMenus().size());
        assertEquals(menu1, menuStructure.getMenus().get(0));
        assertEquals(1, menuStructure.getCourseNames().size());
        assertEquals(course1.getName(), menuStructure.getCourseNames().get(0));
        assertEquals(1, menuStructure.getMenuAndCategoryToCourseName().size());
        assertEquals(course1.getName(), menuStructure.getMenuAndCategoryToCourseName().get(menu1.getName() + ">" + category1.getName()));
        assertEquals(1, menuStructure.getModifierGroups().size());
        assertEquals(modifierGroup1, menuStructure.getModifierGroups().get(0));
        assertEquals(1, menuStructure.getModifiers().size());
        assertEquals(modifier1, menuStructure.getModifiers().get(0));
    }

    private void callPutMenuStructure(MenuStructure menuStructure) {
        String token = getTokenForStaff(staff1);
        Whitebox.setInternalState(authenticationService, "currentAdminToken", token);
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(Params.AUTHORIZATION, token)
                .pathParam("id", restaurant1.getId())
                .body(menuStructure)
                .put("Management/Static/MenuUpload/{id}");
    }

    @Test
    public void putMenuStructureItemsOnly() throws Exception {
        setUpDefaultMenuStructure();

        MenuStructure menuStructure = new MenuStructure();
        menuItem2.setId("-1");
        menuItem2.getModifierGroupIds().clear();
        menuItem2.setName("item 2");
        menuItem3.getModifierGroupIds().clear();
        menuItem3.setName("renamed");
        menuStructure.getItems().add(menuItem2);
        menuStructure.getItems().add(menuItem3);
        group1.setName("Drinks");
        group1.getItems().add("-1");
        group1.getItems().add(menuItem3.getId());
        group1.getItems().add("foobar");
        menuStructure.getMenus().add(menu1);
        menuStructure.getMenuAndCategoryToCourseName().put(menu1.getName() + ">" + category1.getName(), course1.getName());

        callPutMenuStructure(menuStructure);

        Menu menu = menuRepository.findOne(menu1.getId());
        assertEquals(1, menu.getCategories().size());
        Category category = menu.getCategories().get(0);
        assertEquals(category1.getName(), category.getName());
        assertEquals(1, category.getGroups().size());
        Group group = category.getGroups().get(0);
        assertEquals(group1.getName(), group.getName());
        assertEquals(3, group.getItems().size());
        List<MenuItem> allMenuItems = masterDataService.getAllMenuItems(restaurant1.getId());
        assertTrue(allMenuItems.stream().anyMatch(m ->m.getName().equals(menuItem1.getName())));
        assertTrue(allMenuItems.stream().anyMatch(m ->m.getName().equals(menuItem2.getName())));
        assertTrue(allMenuItems.stream().anyMatch(m ->m.getName().equals(menuItem3.getName())));
    }

    @Test
    public void putMenuStructureMenuCategoryGroup1() throws Exception {
        setUpDefaultMenuStructure();

        MenuStructure currentStructure = managementController.getMenuStructure(restaurant1);
        MenuStructure menuStructure = new MenuStructure();
        menuStructure.getMenus().add(menu1);
        managementController.putMenuStructure(restaurant1.getId(), menuStructure);
        assertEquals(currentStructure, managementController.getMenuStructure(restaurant1));
    }

    @Test
    public void putMenuStructureMenuCategoryGroup2() throws Exception {
        setUpDefaultMenuStructure();

        String courseName = "Pre-dinner drinks";
        Menu menu = new Menu();
        Category category = new Category();
        Group group = new Group();
        MenuItem menuItem = new MenuItem();
        MenuStructure menuStructure = createMenuStructureWithItems(courseName, menu, category, group, menuItem);

        callPutMenuStructure(menuStructure);

        assertEquality(courseName, menu, category, group, menuItem);
        assertEquals(4, menuItemRepository.findByRestaurantId(restaurant1.getId()).size());
    }

    public void assertEquality(String courseName, Menu menu, Category category, Group group, MenuItem menuItem) {
        List<Menu> menus = menuRepository.findByRestaurantId(restaurant1.getId());
        assertEquals(menu1, menus.stream().filter(m -> m.getId().equals(menu1.getId())).findFirst().orElse(null));
        assertEquals(2, menus.size());
        Menu newMenu = menus.stream().filter(m -> !m.getId().equals(menu1.getId())).findFirst().orElse(null);
        assertNotNull(newMenu);
        assertEquals(menu.getName(), newMenu.getName());
        assertEquals(1, newMenu.getCategories().size());
        Category newCategory = newMenu.getCategories().get(0);
        assertEquals(category.getName(), newCategory.getName());
        assertEquals(1, newCategory.getCourseIds().size());
        Course newCourse = masterDataService.getCoursesByRestaurantId(restaurant1.getId()).stream().filter(c -> c.getId().equals(newCategory.getCourseIds().get(0))).findFirst().orElse(null);
        assertNotNull(newCourse);
        assertEquals(courseName, newCourse.getName());
        assertEquals(1, newCategory.getGroups().size());
        Group newGroup = newCategory.getGroups().get(0);
        assertEquals(group.getName(), newGroup.getName());
        assertEquals(1, newGroup.getItems().size());
        MenuItem newMenuItem = menuItemRepository.findOne(newGroup.getItems().get(0));
        menuItem.setId(newMenuItem.getId());
        menuItem.setRestaurantId(restaurant1.getId());
        //ignore modifier groups when comparing menu items
        List<String> requestedModifierIds = menuItem.getModifierGroupIds();
        List<String> savedModifierIds = newMenuItem.getModifierGroupIds();
        menuItem.setModifierGroupIds(null);
        newMenuItem.setModifierGroupIds(null);
        assertEquals(menuItem, newMenuItem);
        assertEquals(requestedModifierIds.size(), savedModifierIds.size());
    }

    private MenuStructure createMenuStructureWithItems(String courseName, Menu menu, Category category, Group group, MenuItem menuItem) {
        MenuStructure menuStructure = new MenuStructure();
        menuStructure.getMenus().add(menu);
        menu.setName("Drinks");
        category.setName("Alcohol");
        menuStructure.getCourseNames().add(courseName);
        menuStructure.getMenuAndCategoryToCourseName().put(menu.getName() + ">" + category.getName(), courseName);
        menu.getCategories().add(category);
        group.setName("Pre-dinner sakis");
        group.getItems().add("-1");
        category.getGroups().add(group);
        menuItem.setId("-1");
        menuItem.setName("Saki");
        menuItem.setDefaultPrinter(printer1.getId());
        menuItem.setTaxTypeId(tax1.getId());
        menuItem.setPrice(100);
        menuItem.setDescription("foobar");
        menuItem.setType(ItemType.DRINK);
        menuItem.setShortCode("abc");
        menuStructure.getItems().add(menuItem);
        return menuStructure;
    }

    @Test
    public void putMenuStructureExistingModifierGroupAndModifiers() throws Exception {
        setUpDefaultMenuStructure();
        modifierGroup1.setRestaurantId(restaurant1.getId());
        modifierGroup1.getModifiers().clear();
        modifierGroup1.getModifiers().add(modifier1);
        modifierGroupRepository.save(modifierGroup1);

        String courseName = "Pre-dinner drinks";
        Menu menu = new Menu();
        Category category = new Category();
        Group group = new Group();
        MenuItem menuItem = new MenuItem();
        MenuStructure menuStructure = createMenuStructureWithItems(courseName, menu, category, group, menuItem);
        menuItem.getModifierGroupIds().add(modifierGroup1.getId());

        callPutMenuStructure(menuStructure);

        assertEquality(courseName, menu, category, group, menuItem);
        assertModifierGroupIsOnMenuItem(menuItem);
    }

    @Test
    public void putMenuStructureExistingModifierGroupAndNewModifiers() throws Exception {
        setUpDefaultMenuStructure();
        modifierGroup1.setRestaurantId(restaurant1.getId());
        modifierGroup1.getModifiers().clear();
        modifierGroupRepository.save(modifierGroup1);

        testInsertionWithModifierGroup(modifierGroup1);
    }

    private void testInsertionWithModifierGroup(ModifierGroup modifierGroup) {
        String courseName = "Pre-dinner drinks";
        Menu menu = new Menu();
        Category category = new Category();
        Group group = new Group();
        MenuItem menuItem = new MenuItem();
        MenuStructure menuStructure = createMenuStructureWithItems(courseName, menu, category, group, menuItem);
        menuItem.getModifierGroupIds().add(modifierGroup.getId());
        Modifier modifier = createModifier("Double shots", "-1");
        menuStructure.getModifiers().add(modifier);
        menuStructure.getModifierGroups().add(modifierGroup);
        modifierGroup.getModifiers().add(modifier);

        callPutMenuStructure(menuStructure);

        assertEquality(courseName, menu, category, group, menuItem);
        ModifierGroup updatedGroup = modifierGroupRepository.findOne(modifierGroup.getId());
        assertEquals(1, updatedGroup.getModifiers().size());
        Modifier newModifier = modifierRepository.findOne(updatedGroup.getModifiers().get(0).getId());
        assertNotNull(newModifier);
        modifier.setId(newModifier.getId());
        assertEquals(modifier, newModifier);
        assertModifierGroupIsOnMenuItem(menuItem);
    }

    private Modifier createModifier(String name, String id) {
        Modifier modifier = new Modifier();
        modifier.setModifierValue(name);
        modifier.setPrice(100);
        modifier.setTaxTypeId(tax1.getId());
        modifier.setId(id);
        return modifier;
    }

    @Test
    public void putMenuStructureNewModifierGroupAndNewModifiers() throws Exception {
        setUpDefaultMenuStructure();

        Modifier newMod1 = createModifier("Double shots", "-1");
        Modifier newMod2 = createModifier("Triple shots", "-2");
        ModifierGroup mg1 = createModifierGroup("mod group 1", "-4");
        mg1.getModifiers().addAll(Lists.newArrayList(newMod1, modifier1));
        ModifierGroup mg2 = createModifierGroup("mod group 2", "-2");
        mg2.getModifiers().add(newMod2);

        String courseName = "Pre-dinner drinks";
        Menu menu = new Menu();
        Category category = new Category();
        Group group = new Group();
        MenuItem menuItem = new MenuItem();
        menuItem.getModifierGroupIds().add("-4");
        menuItem.getModifierGroupIds().add("-2");
        MenuStructure menuStructure = createMenuStructureWithItems(courseName, menu, category, group, menuItem);
        menuStructure.getModifierGroups().add(mg1);
        menuStructure.getModifierGroups().add(mg2);
        menuStructure.getModifiers().add(newMod1);
        menuStructure.getModifiers().add(newMod2);

        callPutMenuStructure(menuStructure);

        assertEquality(courseName, menu, category, group, menuItem);
        MenuItem savedItem = menuItemRepository.findAll().stream().filter(m -> m.getName().equals(menuItem.getName())).findFirst().orElse(null);
        assertNotNull(savedItem);
        assertEquals(2, savedItem.getModifierGroupIds().size());
        ModifierGroup savedModGroup1 = modifierGroupRepository.findOne(savedItem.getModifierGroupIds().get(0));
        assertEquals(mg1.getName(), savedModGroup1.getName());
        ModifierGroup savedModGroup2 = modifierGroupRepository.findOne(savedItem.getModifierGroupIds().get(1));
        assertEquals(mg2.getName(), savedModGroup2.getName());
        List<Modifier> modifiers = mg1.getModifiers();
        assertEquals(2, modifiers.size());
        assertEquals("Double shots", modifiers.get(0).getModifierValue());
        assertEquals(modifier1, modifiers.get(1));
    }

    private ModifierGroup createModifierGroup(String name, String id) {
        ModifierGroup modifierGroup = new ModifierGroup();
        modifierGroup.setName(name);
        modifierGroup.setRestaurantId(restaurant1.getId());
        modifierGroup.setId(id);
        return modifierGroup;
    }

    private void assertModifierGroupIsOnMenuItem(MenuItem menuItem) {
        MenuItem newMenuItem = menuItemRepository.findByRestaurantId(restaurant1.getId()).stream().filter(m -> m.getName() != null && m.getName().equals(menuItem.getName())).findFirst().orElse(null);
        assertNotNull(newMenuItem);
        assertEquals(1, newMenuItem.getModifierGroupIds().size());
        assertEquals(modifierGroup1.getId(), newMenuItem.getModifierGroupIds().get(0));
    }

    public void setUpDefaultMenuStructure() {
        service1.setDefaultService(true);
        service1.setId(IDAble.generateId(restaurant1.getId()));
        restaurant1.getServices().clear();
        restaurant1.getServices().add(service1);
        service1.getCourses().clear();
        course1.setName("ASAP");
        course1.setId(IDAble.generateId(service1.getId()));
        service1.getCourses().add(course1);
        restaurantRepository.save(restaurant1);

        menu1.setRestaurantId(restaurant1.getId());
        menu1.getCategories().clear();
        category1.setId(IDAble.generateId(menu1.getId()));
        category1.getCourseIds().add(course1.getId());
        menu1.getCategories().add(category1);

        group1.setId(IDAble.generateId(category1.getId()));
        category1.getGroups().clear();
        category1.getGroups().add(group1);

        group1.getItems().clear();

        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem2.setRestaurantId(restaurant1.getId());
        menuItem3.setRestaurantId(restaurant1.getId());
        menuItemRepository.save(Lists.newArrayList(menuItem1, menuItem2, menuItem3));
        group1.getItems().add(menuItem1.getId());
        menuRepository.save(menu1);

        modifierGroup1.setRestaurantId(restaurant1.getId());
        modifierGroup1.getModifiers().clear();
        modifierGroup1.getModifiers().add(modifier1);
        modifierGroupRepository.save(modifierGroup1);
    }
}