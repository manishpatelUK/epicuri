package uk.co.epicuri.serverapi.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import uk.co.epicuri.serverapi.common.pojo.authentication.StaffAuthentication;
import uk.co.epicuri.serverapi.common.pojo.customer.KeyValuePair;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.BookingWidgetAuthentications;
import uk.co.epicuri.serverapi.common.pojo.model.authentication.StaffAuthentications;
import uk.co.epicuri.serverapi.common.pojo.model.booking.BookingStatics;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.BookingType;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.repository.*;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class MasterDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MasterDataService.class);
    private final Map<String,String> internationalCodes;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffAuthenticationsRepository staffAuthenticationsRepository;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ModifierRepository modifierRepository;

    @Autowired
    private ModifierGroupRepository modifierGroupRepository;

    @Autowired
    private TaxRateRepository taxRateRepository;

    @Autowired
    private DefaultsRepository defaultsRepository;

    @Autowired
    private AdjustmentTypeRepository adjustmentTypeRepository;

    @Autowired
    private PrinterRepository printerRepository;

    @Autowired
    private OpeningHoursRepository openingHoursRepository;

    @Autowired
    private PreferencesRepository preferencesRepository;

    @Autowired
    private RestaurantImageRepository restaurantImageRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StockLevelRepository stockLevelRepository;

    @Autowired
    private ArchiveDataService archiveDataService;

    @Autowired
    private BookingWidgetAuthenticationsRepository bookingWidgetAuthenticationsRepository;

    @Autowired
    private BookingStaticsRepository bookingStaticsRepository;

    @Autowired
    private Environment environment;

    public MasterDataService() {
        internationalCodes = new HashMap<>();
        readInternationalCodes();
    }

    @PostConstruct
    public void init() {
        if(isProdEnvironment()) {
            LOGGER.info("This is a prod environment; texts will be sent!");
        } else {
            LOGGER.info("This is a testing environment; texts will not be sent");
        }
    }

    public boolean isProdEnvironment() {
        for(String env : environment.getActiveProfiles()) {
            if (env.equals("prod")) {
                return true;
            }
        }
        return false;
    }

    public boolean isTestEnvironment() {
        for(String env : environment.getActiveProfiles()) {
            if (env.equals("test")) {
                return true;
            }
        }
        return false;
    }

    private void readInternationalCodes() {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(MasterDataService.class.getResourceAsStream("/countryDialCodes.csv")))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] bits = line.split(",");
                if(bits.length == 2) {
                    internationalCodes.put(bits[0].trim(), bits[1].trim());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error reading international codes: {}", e.getMessage());
        }
    }

    public String getDialingCode(String countryCode) {
        return internationalCodes.getOrDefault(countryCode, "00");
    }

    public Restaurant upsert(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public Restaurant getRestaurant(String id) {
        return restaurantRepository.findOne(id);
    }

    public Restaurant getRestaurantByStaffFacingId(String staffFacingId) {
        return restaurantRepository.findByStaffFacingId(staffFacingId);
    }

    public void setRestaurantTakeawayMenu(String restaurantId, String menuId) {
        restaurantRepository.setTakeawayMenuId(restaurantId, menuId);
    }

    public List<Restaurant> searchRestaurants(Double trLat, Double trLong,
                                              Double blLat, Double blLong,
                                              String cuisineId,
                                              Boolean hasTakeaway,
                                              String name) { //evil but necessary
        return restaurantRepository.searchByGeography(trLat, trLong, blLat, blLong, cuisineId, hasTakeaway, name);
    }

    public Staff getStaff(String id) {
        return staffRepository.findOne(id);
    }

    public Staff getStaff(String userName, String staffFacingId) {
        Restaurant restaurant = getRestaurantByStaffFacingId(staffFacingId);
        if(restaurant == null) {
            return null;
        }

        List<Staff> staffs = staffRepository.findByUserNameAndRestaurantId(userName, restaurant.getId()).stream().filter(s -> s.getDeleted() == null).collect(Collectors.toList());
        if(staffs.size() == 0) {
            return null;
        }

        if(staffs.size() == 1) {
            Staff staff = staffs.get(0);
            staff.setRestaurant(restaurant);
            return staff;
        } else {
            Staff staff = staffs.get(staffs.size()-1);
            staff.setRestaurant(restaurant);
            return staff;
        }
    }

    public List<Staff> getAllStaff(String restaurantId) {
        return staffRepository.findByRestaurantId(restaurantId);
    }

    public Staff upsert(Staff staff) {
        return staffRepository.save(staff);
    }

    public void softDeleteStaff(String staffId) {
        staffRepository.markDeleted(staffId, Staff.class);
    }

    public StaffAuthentications insertStaffAuthentication(StaffAuthentication staffAuthenticationKey) {
        StaffAuthentications staffAuthentications = new StaffAuthentications();
        staffAuthentications.setCreatedTime(new Date(staffAuthenticationKey.getExpires()));
        staffAuthentications.setStaffId(staffAuthenticationKey.getStaffId());
        staffAuthentications.setAuthenticationKey(staffAuthenticationKey.getKey());
        staffAuthentications.setRestaurantId(staffAuthenticationKey.getRestaurantId());
        return insertStaffAuthentication(staffAuthentications);
    }

    public StaffAuthentications insertStaffAuthentication(StaffAuthentications staffAuthentications) {
        return staffAuthenticationsRepository.insert(staffAuthentications);
    }

    public StaffAuthentications getAuthentication(String staffId) {
        return staffAuthenticationsRepository.findByStaffId(staffId);
    }

    public void deleteAuthentication(String staffId) {
        StaffAuthentications authentication = getAuthentication(staffId);
        if(authentication != null) {
            staffAuthenticationsRepository.delete(authentication.getId());
        }
    }

    public BookingWidgetAuthentications getBookingWidgetAuthentication(String restaurantId) {
        return bookingWidgetAuthenticationsRepository.findByRestaurantId(restaurantId);
    }

    public void extendBookingAuthenticationLife(BookingWidgetAuthentications bookingWidgetAuthentications) {
        bookingWidgetAuthentications.setCreatedTime(new Date());
        bookingWidgetAuthenticationsRepository.save(bookingWidgetAuthentications);
    }

    public BookingWidgetAuthentications insertBookingWidgetAuthentications(BookingWidgetAuthentications auth) {
        return bookingWidgetAuthenticationsRepository.insert(auth);
    }

    public BookingStatics getBookingStaticsByLanguage(String language) {
        return bookingStaticsRepository.findByLanguage(language);
    }

    public StaffRole getStaffRole(String staffId) {
        return staffRepository.getStaffRole(staffId);
    }

    public List<Cuisine> getCuisines() {
        return cuisineRepository.findAll();
    }

    public Cuisine getCuisine(String id) {
        return cuisineRepository.findOne(id);
    }

    public void upsert(Cuisine cuisine) {
        cuisineRepository.save(cuisine);
    }

    public Cuisine upsertCuisine(String newCuisine) {
        Cuisine cuisine = new Cuisine();
        cuisine.setName(StringUtils.capitalize(newCuisine.toLowerCase()));
        try {
            return cuisineRepository.insert(cuisine);
        } catch (DuplicateKeyException ex) {
            return cuisineRepository.findByName(cuisine.getName());
        }
    }

    public List<Course> getCoursesByRestaurantId(String restaurantId) {
        Restaurant restaurant = restaurantRepository.findOne(restaurantId);
        if(restaurant != null) {
            return restaurant.getServices().stream().flatMap(s -> s.getCourses().stream()).distinct().collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public List<Course> getCoursesByServiceId(String serviceId) {
        Service service = getService(serviceId);
        if(service != null) {
            return service.getCourses();
        } else {
            return new ArrayList<>();
        }
    }

    public List<Course> getCourses(Collection<String> ids) {
        List<String> serviceIds = ids.stream().map(IDAble::extractParentId).distinct().collect(Collectors.toList());
        if(serviceIds.size() == 0) {
            return new ArrayList<>();
        }
        List<String> restaurantIds = serviceIds.stream().map(IDAble::extractParentId).distinct().collect(Collectors.toList());
        if(restaurantIds.size() > 1) {
            throw new IllegalArgumentException("Cannot request courses from multiple restaurants");
        }
        String restaurantId = restaurantIds.get(0);
        return getCoursesByRestaurantId(restaurantId).stream().filter(c -> ids.contains(c.getId())).collect(Collectors.toList());
    }

    //menus
    public Menu getMenu(String id) {
        return menuRepository.findOne(id);
    }

    public List<Menu> getMenusByRestaurantId(String id) {
        return menuRepository.findByRestaurantId(id);
    }

    public void addToMenu(String menuId, Category category) {
        if(StringUtils.isBlank(category.getId())) {
            throw new IllegalArgumentException("Category requires id");
        }
        menuRepository.push(menuId, category);
    }

    public Menu addMenu(Menu menu) {
        return menuRepository.insert(menu);
    }

    public void deleteMenu(String id) {
        menuRepository.markDeleted(id, Menu.class);
    }

    public void updateMenuModifiedTime(String id) {
        menuRepository.updateModifiedTime(id, System.currentTimeMillis());
    }

    public Menu upsert(Menu menu) {
        return menuRepository.save(menu);
    }

    public List<MenuItem> getAllMenuItems(String restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public List<MenuItem> getAllMenuItemsNotDeleted(String restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream().filter(m -> m.getDeleted() == null).collect(Collectors.toList());
    }

    public List<MenuItem> getMenuItems(Collection<String> ids) {
        return Lists.newArrayList(menuItemRepository.findAll(ids));
    }

    public List<MenuItem> getMenuItemsByPlu(String restaurantId, List<String> pluList) {
        return menuItemRepository.findByRestaurantIdAndPluIn(restaurantId, pluList);
    }

    public MenuItem getItem(String id) {
        return menuItemRepository.findOne(id);
    }

    public void deleteMenuItem(String id) {
        menuItemRepository.markDeleted(id, MenuItem.class);
    }

    public void deleteMenuItems(List<String> ids) {
        menuItemRepository.markDeleted(ids, MenuItem.class);
    }

    public MenuItem upsert(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public List<MenuItem> upsertMenuItems(List<MenuItem> menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public List<StockLevel> getStockLevels(String restaurantId) {
        return stockLevelRepository.findByRestaurantId(restaurantId);
    }

    public StockLevel getStockLevelByRestaurantIdAndPlu(String restaurantId, String plu) {
        return stockLevelRepository.findByRestaurantIdAndPlu(restaurantId, plu);
    }

    public StockLevel getStockLevel(String id) {
        return stockLevelRepository.findOne(id);
    }

    public void deletePlu(String restaurantId, String id) {
        StockLevel stockLevel = stockLevelRepository.findOne(id);
        if(stockLevel != null) {
            stockLevelRepository.delete(stockLevel);
            List<MenuItem> allItems = getMenuItemsByPlu(restaurantId, Collections.singletonList(stockLevel.getPlu()));
            updateItemsWithPLU(allItems, null);
        }
    }

    public void updateItemsWithPLU(List<MenuItem> allItems, String plu) {
        for(MenuItem item : allItems) {
            item.setPlu(plu);
        }
        menuItemRepository.save(allItems);
    }

    public void upsert(StockLevel stockLevel) {
        stockLevelRepository.save(stockLevel);
    }

    public List<Modifier> getModifiers(Collection<String> ids) {
        return Lists.newArrayList(modifierRepository.findAll(ids));
    }

    public Modifier getModifier(String id) {
        return modifierRepository.findOne(id);
    }

    public List<ModifierGroup> getModifierGroupsByRestaurant(String restaurantId) {
        return modifierGroupRepository.findByRestaurantId(restaurantId).stream().filter(m -> m.getDeleted() == null).collect(Collectors.toList());
    }

    public ModifierGroup getModifierGroup(String id) {
        return modifierGroupRepository.findOne(id);
    }

    public ModifierGroup upsert(ModifierGroup modifierGroup) {
        return modifierGroupRepository.save(modifierGroup);
    }

    public List<ModifierGroup> upsertModifierGroups(List<ModifierGroup> modifierGroups) {
        return modifierGroupRepository.save(modifierGroups);
    }

    public Modifier upsert(Modifier modifier) {
        return modifierRepository.save(modifier);
    }

    public List<Modifier> upsertModifiers(List<Modifier> modifiers) {
        return modifierRepository.save(modifiers);
    }

    public void deleteModifierGroup(String id) {
        modifierGroupRepository.markDeleted(id, ModifierGroup.class);
    }

    public void upsert(TaxRate taxRate) {
        taxRateRepository.save(taxRate);
    }

    public TaxRate getTaxRate(String id) {
        return taxRateRepository.findOne(id);
    }

    public boolean taxRateExists(String id) {
        return taxRateRepository.exists(id);
    }

    public void upsert(Default aDefault) {
        defaultsRepository.save(aDefault);
    }

    public Default getDefaultByName(String aDefault) {
        return defaultsRepository.findByName(aDefault);
    }

    public Default getDefault(String id) {
        return defaultsRepository.findOne(id);
    }

    public List<Default> getDefaults() {
        return defaultsRepository.findAll();
    }

    public RestaurantDefault getRestaurantDefault(String restaurantId, String defaultName) {
        Restaurant restaurant = restaurantRepository.findOne(restaurantId);
        if(restaurant == null) {
            return null;
        }

        RestaurantDefault rd = restaurant.getRestaurantDefaults().stream().filter(r -> r.getName().equals(defaultName)).findFirst().orElse(null);
        if(rd != null) {
            return rd;
        } else {
            Default def = defaultsRepository.findByName(defaultName);
            if(def != null) {
                return new RestaurantDefault(def);
            } else {
                //should never happen really
                return null;
            }
        }
    }

    public void upsert(AdjustmentType adjustmentType) {
        adjustmentTypeRepository.save(adjustmentType);
    }

    public List<AdjustmentType> getAdjustmentTypes() {
        return adjustmentTypeRepository.findAll();
    }

    public List<AdjustmentType> getAdjustmentTypes(Iterable<String> adjustmentTypes) {
        return Lists.newArrayList(adjustmentTypeRepository.findAll(adjustmentTypes));
    }

    public AdjustmentType getAdjustmentType(String id) {
        return adjustmentTypeRepository.findOne(id);
    }

    public List<AdjustmentType> getAdjustmentTypes(List<String> ids) {
        return Lists.newArrayList(adjustmentTypeRepository.findAll(ids));
    }

    public AdjustmentType getAdjustmentTypeByName(String name) {
        return adjustmentTypeRepository.findByName(name);
    }

    public List<Printer> getPrinters(Collection<String> ids) {
        return Lists.newArrayList(printerRepository.findAll(ids));
    }

    public List<Printer> getPrinters(String restaurantId) {
        return printerRepository.findByRestaurantId(restaurantId);
    }

    public Printer upsert(Printer printer) {
        return printerRepository.save(printer);
    }

    public boolean printerExists(String id) {
        return printerRepository.exists(id);
    }

    public List<Service> getServicesByRestaurant(String restaurantId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        if(restaurant == null) {
            return new ArrayList<>();
        } else {
            return restaurant.getServices();
        }
    }

    public Service getService(String id) {
        String restaurantId = IDAble.extractParentId(id);
        List<Service> services = getServicesByRestaurant(restaurantId);
        return services.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public Service getAdhocService(String restaurantId) {
        List<Service> services = getServicesByRestaurant(restaurantId);
        return services.stream().filter(s -> s.getSessionType() == SessionType.ADHOC).findFirst().orElse(null);
    }

    public Service getTakeawayService(String restaurantId) {
        List<Service> services = getServicesByRestaurant(restaurantId);
        return services.stream().filter(s -> s.getSessionType() == SessionType.TAKEAWAY).findFirst().orElse(null);
    }

    public List<Floor> getFloors(String restaurantId) {
        Restaurant restaurant = restaurantRepository.findOne(restaurantId);
        if(restaurant != null) {
            return restaurant.getFloors();
        }

        return new ArrayList<>();
    }

    public void setLayoutSelected(String restaurantId, String floorId, String layoutId) {
        restaurantRepository.setSelectedLayout(restaurantId, floorId, layoutId);
    }

    public List<Table> getTables(String restaurantId, Collection<String> ids) {
        return getTables(restaurantId).stream().filter(t -> ids.contains(t.getId())).collect(Collectors.toList());
    }

    public List<Table> getTables(String restaurantId) {
        Restaurant restaurant = getRestaurant(restaurantId);
        if(restaurant != null) {
            return restaurant.getTables();
        } else {
            return new ArrayList<>();
        }
    }

    public void addLayoutToFloor(String restaurantId, String floorId, Layout layout) {
        restaurantRepository.addLayout(restaurantId, floorId, layout);
    }

    public void updateTables(String restaurantId, List<Table> tables) {
        restaurantRepository.updateTables(restaurantId, tables);
    }

    public void addTable(String restaurantId, Table table) {
        restaurantRepository.pushTable(restaurantId, table);
    }

    public void upsert(String restaurantId, Table table) {
        restaurantRepository.updateTable(restaurantId, table);
    }

    public void deleteTable(String restaurantId, String id) {
        restaurantRepository.pullTable(restaurantId,id);

        Restaurant restaurant = getRestaurant(restaurantId);
        boolean changed = false;
        for(Floor floor : restaurant.getFloors()) {
            for(Layout layout : floor.getLayouts()) {
                if(layout.getTables().contains(id)) {
                    changed = true;
                    layout.getTables().remove(id);
                }
            }
        }
        if(changed) {
            upsert(restaurant);
        }
    }

    public void updateLayout(String restaurantId, String floorId, Layout layout) {
        restaurantRepository.updateLayout(restaurantId, floorId, layout);
    }

    public List<TaxRate> getTaxRatesByCountry(String countryId) {
        return taxRateRepository.findByCountryId(countryId);
    }

    public OpeningHours getOpeningHours(String restaurantId, BookingType bookingType) {
        return openingHoursRepository.findByRestaurantIdAndBookingType(restaurantId, bookingType);
    }

    public List<OpeningHours> getOpeningHours(List<String> restaurantIds, BookingType bookingType) {
        return openingHoursRepository.findByRestaurantIdInAndBookingType(restaurantIds, bookingType);
    }

    public boolean preferenceExists(String id) {
        return preferencesRepository.exists(id);
    }

    public List<Preference> getAllPreferences() {
        return preferencesRepository.findAll();
    }

    public Map<String, List<KeyValuePair>> getAllPreferencesAsMap() {
        List<Preference> allPreferences = getAllPreferences();

        Map<String,List<KeyValuePair>> response = new HashMap<>();
        response.put(PreferenceType.DIETARY.getKey(),new ArrayList<>());
        response.put(PreferenceType.FOOD.getKey(),new ArrayList<>());
        response.put(PreferenceType.ALLERGY.getKey(),new ArrayList<>());

        for(Preference preference : allPreferences) {
            if(preference.getPreferenceType() == PreferenceType.DIETARY) {
                response.get(PreferenceType.DIETARY.getKey()).add(new KeyValuePair(preference.getId(), preference.getName()));
            } else if(preference.getPreferenceType() == PreferenceType.FOOD) {
                response.get(PreferenceType.FOOD.getKey()).add(new KeyValuePair(preference.getId(), preference.getName()));
            } else if(preference.getPreferenceType() == PreferenceType.ALLERGY) {
                response.get(PreferenceType.ALLERGY.getKey()).add(new KeyValuePair(preference.getId(), preference.getName()));
            }
        }
        return response;
    }

    public RestaurantImage getRestaurantImage(String restaurantId, RestaurantImageType type) {
        return restaurantImageRepository.findByRestaurantIdAndImageType(restaurantId, type);
    }

    public RestaurantImage getRestaurantImage(String id) {
        return restaurantImageRepository.findOne(id);
    }

    public RestaurantImage upsert(RestaurantImage restaurantImage) {
        return restaurantImageRepository.save(restaurantImage);
    }

    public void upsert(Country country) {
        countryRepository.save(country);
    }

    public Country getCountry(String id) {
        return countryRepository.findOne(id);
    }

    public void upsert(OpeningHours openingHours) {
        openingHoursRepository.save(openingHours);
    }

    public boolean restaurantExistsByStaffId(String restaurantId) {
        return restaurantRepository.findByStaffFacingId(restaurantId) != null;
    }

    public boolean restaurantExists(String restaurantId) {
        return restaurantRepository.exists(restaurantId);
    }

    // ------ MANAGEMENT STUFF ------

    public List<Printer> getPrinter() {
        return printerRepository.findAll();
    }

    public List<Staff> getStaff() {
        return staffRepository.findAll();
    }

    public void deleteStaff(String id) {
        staffRepository.delete(id);
    }

    public List<Restaurant> getRestaurants() {
        return getRestaurant();
    }

    public List<Restaurant> getRestaurants(List<String> ids) {
        return restaurantRepository.findByIdIn(ids);
    }
    
    public void upsertRestaurants(List<Restaurant> restaurants) {
        restaurantRepository.save(restaurants);
    }

    public long getNumberOfRestaurants() {
        return restaurantRepository.count();
    }

    public List<Restaurant> getRestaurant() {
        return restaurantRepository.findAll();
    }

    public void deleteRestaurant(String id) {
        archiveDataService.deleteRestaurant(id);
    }

    public Restaurant insertRestaurant(Restaurant restaurant) {
        restaurant.setId(null);
        return restaurantRepository.insert(restaurant);
    }

    public String getNextRestaurantId() {
        List<Restaurant> restaurants = getRestaurants();
        String candidate = String.valueOf(restaurants.size()+1);
        return getNextRestaurantId(restaurants.stream().filter(x -> StringUtils.isNotBlank(x.getStaffFacingId())).map(Restaurant::getStaffFacingId).collect(Collectors.toList()), candidate);
    }

    private String getNextRestaurantId(List<String> allIds, String candidate) {
        if(!allIds.contains(candidate)) {
            return candidate;
        } else {
            return getNextRestaurantId(allIds, candidate + "1");
        }
    }

    public void deleteAdjustmentType(String id) {
        adjustmentTypeRepository.delete(id);
    }

    public AdjustmentType insertAdjustmentType(AdjustmentType adjustmentType) {
        adjustmentType.setId(null);
        return adjustmentTypeRepository.insert(adjustmentType);
    }

    public void deleteCountry(String id) {
        countryRepository.delete(id);
    }

    public Country insertCountry(Country country) {
        country.setId(null);
        return countryRepository.insert(country);
    }

    public void deleteCuisine(String id) {
        cuisineRepository.delete(id);
    }

    public Cuisine insertCuisine(Cuisine cuisine) {
        cuisine.setId(null);
        return cuisineRepository.insert(cuisine);
    }

    public void deleteDefault(String id) {
        defaultsRepository.delete(id);
    }

    public Default insertDefault(Default aDefault) {
        aDefault.setId(null);
        return defaultsRepository.insert(aDefault);
    }

    public void deleteTaxRate(String id) {
        taxRateRepository.delete(id);
    }

    public TaxRate insertTaxRate(TaxRate taxRate) {
        taxRate.setId(null);
        return taxRateRepository.insert(taxRate);
    }

    public Printer getPrinter(String id) {
        return printerRepository.findOne(id);
    }

    public void deletePrinter(String id) {
        printerRepository.delete(id);
    }

    public Printer insertPrinter(Printer printer) {
        printer.setId(null);
        return printerRepository.insert(printer);
    }

    public List<Country> getCountry() {
        return countryRepository.findAll();
    }

    public List<Country> getCountries() {
        return countryRepository.findAll();
    }

    public List<Country> getCountrys() {
        return getCountries();
    }

    public List<Cuisine> getCuisine() {
        return cuisineRepository.findAll();
    }

    public List<TaxRate> getTaxRate() {
        return taxRateRepository.findAll();
    }

    public List<AdjustmentType> getAdjustmentType() {
        return adjustmentTypeRepository.findAll();
    }

    public List<Default> getDefault() {
        return defaultsRepository.findAll();
    }

    public Floor getFloor(String id) {
        Restaurant restaurant = getRestaurant(IDAble.extractParentId(id));
        return getFloor(id, restaurant);
    }

    private static Floor getFloor(String id, Restaurant restaurant) {
        return restaurant.getFloors().stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }

    public void upsertFloor(Floor floor) {
        if(floor.getId() == null) {
            return;
        }

        Restaurant restaurant = getRestaurant(IDAble.extractParentId(floor.getId()));
        int found = -1;
        for(int i = 0; i < restaurant.getFloors().size(); i++) {
            if(restaurant.getFloors().get(i).getId().equals(floor.getId())) {
                found = i;
            }
        }

        if(found < 0) {
            return;
        }

        restaurant.getFloors().set(found, floor);
        upsert(restaurant);
    }

    public List<Menu> getMenus(String id) {
        return getMenusByRestaurantId(id);
    }

    public List<Preference> getPreference() {
        return preferencesRepository.findAll();
    }

    public Preference insertPreference(Preference preference) {
        return preferencesRepository.insert(preference);
    }

    public void deletePreference(String id) {
        preferencesRepository.delete(id);
    }

    public Preference upsertPreference(Preference preference) {
        return preferencesRepository.insert(preference);
    }

    public Preference upsert(Preference preference) {
        return preferencesRepository.insert(preference);
    }

    public List<BookingStatics> getBookingStatics() {
        return bookingStaticsRepository.findAll();
    }

    public BookingStatics upsertBookingStatics(BookingStatics bookingStatics) {
        return bookingStaticsRepository.save(bookingStatics);
    }

    public BookingStatics upsert(BookingStatics bookingStatics) {
        return bookingStaticsRepository.save(bookingStatics);
    }
}
