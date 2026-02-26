package uk.co.epicuri.serverapi.repository;

import com.jayway.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.epicuri.serverapi.auth.AuthenticationUtil;
import uk.co.epicuri.serverapi.auth.AuthenticationUtilTest;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;
import uk.co.epicuri.serverapi.common.pojo.host.HostCourseView;
import uk.co.epicuri.serverapi.common.pojo.host.StaffView;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.menu.*;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.config.TestConfig;
import uk.co.epicuri.serverapi.config.TestMongoConfig;
import uk.co.epicuri.serverapi.service.*;
import uk.co.epicuri.serverapi.service.external.EmailService;
import uk.co.epicuri.serverapi.service.external.MewsService;
import uk.co.epicuri.serverapi.service.external.SMSService;

import java.io.File;
import java.io.FileInputStream;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by manish
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, TestMongoConfig.class}, webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, value = "{$server.port}")
@Component
@ActiveProfiles("test")
public abstract class BaseIT {

    @Value("${epicuri.version.current}")
    protected int currentVersion = 0;

    @Value("${epicuri.paymentsense.pull.enabled}")
    private boolean paymentSensePullEnabled;

    @Autowired
    protected AdjustmentTypeRepository adjustmentTypeRepository;

    @Autowired
    protected BatchRepository batchRepository;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected CashUpRepository cashUpRepository;

    @Autowired
    protected CheckInRepository checkInRepository;

    @Autowired
    protected CustomerRepository customerRepository;

    @Autowired
    protected MenuItemRepository menuItemRepository;

    @Autowired
    protected MenuRepository menuRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected RestaurantRepository restaurantRepository;

    @Autowired
    protected SessionRepository sessionRepository;

    @Autowired
    protected PartyRepository partyRepository;

    @Autowired
    protected StaffRepository staffRepository;

    @Autowired
    protected StaffAuthenticationsRepository staffAuthenticationsRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected PrinterRepository printerRepository;

    @Autowired
    protected ModifierRepository modifierRepository;

    @Autowired
    protected ModifierGroupRepository modifierGroupRepository;

    @Autowired
    protected DefaultsRepository defaultsRepository;

    @Autowired
    protected CuisineRepository cuisineRepository;

    @Autowired
    protected TaxRateRepository taxRateRepository;

    @Autowired
    protected OpeningHoursRepository openingHoursRepository;

    @Autowired
    protected RestaurantImageRepository restaurantImageRepository;

    @Autowired
    protected DeviceDetailsRepository deviceDetailsRepository;

    @Autowired
    protected CountryRepository countryRepository;

    @Autowired
    protected SessionArchiveRepository sessionArchiveRepository;

    @Autowired
    protected PreferencesRepository preferencesRepository;

    @Autowired
    protected StockLevelRepository stockLevelRepository;

    // ------- SERVICES ------------

    @Autowired
    protected ArchiveDataService archiveDataService;

    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected MasterDataService masterDataService;

    @Autowired
    protected BookingService bookingService;

    @Autowired
    protected CustomerService customerService;

    @Autowired
    protected LiveDataService liveDataService;

    @Autowired
    protected MewsService mewsService;

    @Autowired
    protected SessionCalculationService sessionCalculationService;

    @Autowired
    protected SessionService sessionService;

    @Autowired
    protected SessionTimingService sessionTimingService;

    @Autowired
    protected MasterDataCreationService masterDataCreationService;

    @Autowired
    protected SMSService smsService;

    @Autowired
    protected EmailService emailService;

    @Autowired
    protected PhoneNumberValidationService phoneNumberValidationService;

    protected AdjustmentType adjustmentType1, adjustmentType2, adjustmentType3;
    protected Batch batch1, batch2, batch3;
    protected Booking booking1, booking2, booking3;
    protected CashUp cashUp1, cashUp2, cashUp3;
    protected CheckIn checkIn1, checkIn2, checkIn3;
    protected Customer customer1, customer2, customer3, customerLogin;
    protected MenuItem menuItem1, menuItem2, menuItem3;
    protected Menu menu1, menu2, menu3;
    protected Category category1, category2, category3;
    protected Order order1, order2, order3;
    protected Restaurant restaurant1, restaurant2, restaurant3;
    protected Cuisine cuisine1, cuisine2, cuisine3;
    protected Session session1, session2, session3, session4;
    protected Party party1, party2, party3;
    protected Diner diner1, diner2, diner3;
    protected Service service1, service2, service3;
    protected Table table1, table2, table3;
    protected Adjustment adjustment1, adjustment2, adjustment3;
    protected Staff staff1, staff2, staff3, staffLogin;
    protected Notification notification1, notification2, notification3;
    protected Printer printer1, printer2, printer3;
    protected ModifierGroup modifierGroup1;
    protected Modifier modifier1, modifier2, modifier3;
    protected Floor floor1, floor2, floor3;
    protected Course course1, course2, course3;
    protected HostCourseView hostCourseView1, hostCourseView2, hostCourseView3;
    protected TaxRate tax1, tax2, tax3;
    protected Schedule schedule1;
    protected OpeningHours openingHours1, openingHours2, openingHours3;
    protected RestaurantImage restaurantImage1, restaurantImage2;
    protected Group group1, group2, group3;
    protected Country country1, country2, country3;

    @Value("${server.port}")
    protected int port;

    @Before
    public void setUp() throws Exception{
        RestAssured.port = port;

        defaultsRepository.deleteAll();
        adjustmentTypeRepository.deleteAll();
        batchRepository.deleteAll();
        bookingRepository.deleteAll();
        cashUpRepository.deleteAll();
        customerRepository.deleteAll();
        checkInRepository.deleteAll();
        menuItemRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantRepository.deleteAll();
        sessionRepository.deleteAll();
        partyRepository.deleteAll();
        staffRepository.deleteAll();
        staffAuthenticationsRepository.deleteAll();
        notificationRepository.deleteAll();
        printerRepository.deleteAll();
        modifierRepository.deleteAll();
        modifierGroupRepository.deleteAll();
        cuisineRepository.deleteAll();
        taxRateRepository.deleteAll();
        openingHoursRepository.deleteAll();
        restaurantImageRepository.deleteAll();
        deviceDetailsRepository.deleteAll();
        countryRepository.deleteAll();
        stockLevelRepository.deleteAll();

        createDefaults();
        addRestaurants();
        addCuisines();
        addAdjustmentTypes();
        addBatches();
        addCheckIns();
        addBookings();
        addCashUps();
        addCustomers();
        addMenuItems();
        addMenus();
        createCategories();
        addOrders();
        addSessions();
        addParties();
        createDiners();
        createServices();
        createTables();
        createAdjustments();
        addStaffs();
        addNotifications();
        addPrinters();
        addModifierGroups();
        addModifiers();
        createFloors();
        createCourses();
        createHostCourseViews();
        createTaxes();
        createSchedules();
        createOpeningHours();
        createRestaurantFloorplanImages();
        createRestaurantBillLogos();
        createMenuGroups();
        createCountries();

        masterDataCreationService.recreateBookingStatics();
    }

    private void createDefaults() {
        defaultsRepository.save(new Default(FixedDefaults.CHECKIN_EXPIRATION_TIME,15,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.DEFAULT_TIP_PERCENTAGE,10D,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.COVERS_BEFORE_AUTOTIP,4,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.BIRTHDAY_TIMESPAN,10,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.WALKIN_EXPIRATION_TIME,90,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.CLOSED_SESSION_MESSAGE,"Bye!",null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.SOCIAL_MEDIA_MESSAGE,"Hi!",null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MIN_TIME_BETWEEN_SERVICE_REQUESTS,5,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.QUICK_ORDER_SESSION_TIMEOUT,15,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAX_COVERS_PER_RESERVATION,6,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAX_ACTIVE_RESERVATIONS,5,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.RESERVATION_FILTERTIME,12,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.RESERVATION_MINIMUM_TIME,120,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.RESERVATION_LOCK_WINDOW,120,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.RESERVATION_TIMESLOT,120,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAX_ACTIVE_RESERVATIONS_COVERS,30,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAX_TAKEAWAYS_PER_HOUR,8,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAX_TAKEAWAY_VALUE,80D,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MIN_TAKEAWAY_VALUE,10D,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.DELIVERY_SURCHARGE,1.5D,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAX_DELIVERY_RADIUS,1.5D,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.TAKEAWAY_MINIMUM_TIME,30,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.TAKEAWAY_LOCK_WINDOW,120,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.TAX_LABEL,"VAT",null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.TAX_REFERENCE_LABEL,"Our VAT Number",null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.BILL_PREFIX,"A",null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.TICKET_ON_SESSION_CLOSE_TIMEOUT,300,null,null,0));
        defaultsRepository.save(new Default(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC,15D,"Currency","Max allowable amount of a takeaway value before Credit Card payment is forced on guest app",0));
    }

    private void addAdjustmentTypes() {
        adjustmentType1 = new AdjustmentType();
        adjustmentType1.setName("Adj type 1");
        adjustmentType1.setSupportsChange(false);
        adjustmentType1.setType(AdjustmentTypeType.DISCOUNT);

        adjustmentType2 = new AdjustmentType();
        adjustmentType2.setName("Adj type 2");
        adjustmentType2.setSupportsChange(true);
        adjustmentType2.setType(AdjustmentTypeType.PAYMENT);

        adjustmentType3 = new AdjustmentType();
        adjustmentType3.setName("Adj type 3");
        adjustmentType3.setSupportsChange(false);
        adjustmentType3.setType(AdjustmentTypeType.PAYMENT);

        adjustmentType1 = adjustmentTypeRepository.save(adjustmentType1);
        adjustmentType2 = adjustmentTypeRepository.save(adjustmentType2);
        adjustmentType3 = adjustmentTypeRepository.save(adjustmentType3);
    }

    private void addSessions() {
        session1 = new Session();
        session1.setName("session1");

        session2 = new Session();
        session2.setName("session2");

        session3 = new Session();
        session3.setName("session3");

        session4 = new Session();
        session4.setName("session4");

        session1 = sessionRepository.save(session1);
        session2 = sessionRepository.save(session2);
        session3 = sessionRepository.save(session3);
        session4 = sessionRepository.save(session4);
    }

    private void addBatches() {
        batch1 = new Batch();
        batch1.setSessionId("session1");
        batch1.setIntendedPrintTime(1);
        batch1 = batchRepository.save(batch1);

        batch2 = new Batch();
        batch2.setSessionId("session3");
        batch2.setIntendedPrintTime(2);
        batch2 = batchRepository.save(batch2);

        batch3 = new Batch();
        batch3.setSessionId("session3");
        batch3.setIntendedPrintTime(2);
        batch3 = batchRepository.save(batch3);
    }

    private void addBookings() {
        booking1 = new Booking();
        booking1.setBookingType(BookingType.RESERVATION);
        booking1.setRestaurantId(restaurant1.getId());

        booking2 = new Booking();
        booking2.setBookingType(BookingType.TAKEAWAY);
        booking2.setTakeawayType(TakeawayType.COLLECTION);
        booking2.setRestaurantId(restaurant1.getId());

        booking3 = new Booking();
        booking3.setBookingType(BookingType.TAKEAWAY);
        booking3.setTakeawayType(TakeawayType.DELIVERY);
        booking3.setRestaurantId(restaurant1.getId());

        booking1 = bookingRepository.save(booking1);
        booking2 = bookingRepository.save(booking2);
        booking3 = bookingRepository.save(booking3);
    }

    private void addCashUps() {
        cashUp1 = new CashUp();
        cashUp1.setStartTime(0);
        cashUp1.setEndTime(10);
        cashUp1.setRestaurantId(restaurant1.getId());

        cashUp2 = new CashUp();
        cashUp2.setStartTime(11);
        cashUp2.setEndTime(20);
        cashUp2.setRestaurantId(restaurant1.getId());

        cashUp3 = new CashUp();
        cashUp3.setStartTime(0);
        cashUp3.setEndTime(10);
        cashUp3.setRestaurantId(restaurant1.getId());

        cashUp1 = cashUpRepository.save(cashUp1);
        cashUp2 = cashUpRepository.save(cashUp2);
        cashUp3 = cashUpRepository.save(cashUp3);
    }

    private void addCustomers() {
        customer1 = new Customer();
        customer1.setEmail("c1@c1.com");
        customer1.setPhoneNumber("1234");
        customer1.setInternationalCode("44");
        customer1.setRegisteredViaApp(true);
        customer2 = new Customer();
        customer2.setEmail("c2@c2.com");
        customer2.setPhoneNumber("5678");
        customer2.setInternationalCode("44");
        customer2.setRegisteredViaApp(true);
        customer3 = new Customer();
        customer3.setEmail("c3@c3.com");
        customer3.setPhoneNumber("0987");
        customer3.setInternationalCode("44");
        customer3.setRegisteredViaApp(true);

        customer1 = customerRepository.save(customer1);
        customer2 = customerRepository.save(customer2);
        customer3 = customerRepository.save(customer3);

        customerLogin = new Customer();
        customerLogin.setEmail("foo@foo.com");
        Address address = new Address();
        address.setCity("London");
        address.setPostcode("HA6 1AU");
        address.setStreet("61 Northwood Way");
        customerLogin.setAddress(address);
        List<String> allergies = new ArrayList<>();
        allergies.add("fish");
        allergies.add("eggs");
        customerLogin.setAllergies(allergies);
        customerLogin = customerRepository.save(customerLogin);
    }

    protected void addCheckIns() {
        checkIn1 = new CheckIn();
        checkIn1.setRestaurantId(restaurant1.getId());

        checkIn2 = new CheckIn();
        checkIn2.setRestaurantId(restaurant1.getId());

        checkIn3 = new CheckIn();
        checkIn3.setRestaurantId(restaurant1.getId());

        checkIn1 = checkInRepository.save(checkIn1);
        checkIn2 = checkInRepository.save(checkIn2);
        checkIn3 = checkInRepository.save(checkIn3);
    }

    private void addMenuItems() {
        menuItem1 = new MenuItem();
        menuItem1.setName("chips");
        menuItem1.setPlu("m1");

        menuItem2 = new MenuItem();
        menuItem2.setName("beans");
        menuItem2.setPlu("m2");

        menuItem3 = new MenuItem();
        menuItem3.setName("salad");
        menuItem3.setPlu("m3");

        menuItem1 = menuItemRepository.save(menuItem1);
        menuItem2 = menuItemRepository.save(menuItem2);
        menuItem3 = menuItemRepository.save(menuItem3);
    }

    private void addMenus() {
        menu1 = new Menu();
        menu1.setName("chips");

        menu2 = new Menu();
        menu2.setName("beans");

        menu3 = new Menu();
        menu3.setName("salad");

        menu1 = menuRepository.save(menu1);
        menu2 = menuRepository.save(menu2);
        menu3 = menuRepository.save(menu3);
    }

    private void createCategories() {
        category1 = new Category();
        category1.setName("cat1");
        category1.setOrder(0);

        category2 = new Category();
        category2.setName("cat2");
        category2.setOrder(1);

        category3 = new Category();
        category3.setName("cat3");
        category3.setOrder(2);
    }

    private void addOrders() {
        order1 = new Order();
        order2 = new Order();
        order3 = new Order();

        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
        order3 = orderRepository.save(order3);
    }

    private void addRestaurants() {
        restaurant1 = new Restaurant();
        restaurant1.setName("r1");
        restaurant1.setStaffFacingId("1");
        restaurant1.setEnabledForWaiter(true);
        restaurant1.setEnabledForDiner(true);
        restaurant2 = new Restaurant();
        restaurant2.setName("r2");
        restaurant2.setStaffFacingId("2");
        restaurant2.setEnabledForWaiter(true);
        restaurant2.setEnabledForDiner(true);
        restaurant3 = new Restaurant();
        restaurant3.setName("r3");
        restaurant3.setStaffFacingId("3");
        restaurant3.setEnabledForWaiter(true);
        restaurant3.setEnabledForDiner(true);

        restaurant1.getRestaurantDefaults().clear();
        restaurant2.getRestaurantDefaults().clear();
        restaurant3.getRestaurantDefaults().clear();

        for(Default def : defaultsRepository.findAll()) {
            restaurant1.getRestaurantDefaults().add(new RestaurantDefault(def));
            restaurant2.getRestaurantDefaults().add(new RestaurantDefault(def));
            restaurant3.getRestaurantDefaults().add(new RestaurantDefault(def));
        }

        restaurant1 = restaurantRepository.save(restaurant1);
        restaurant2 = restaurantRepository.save(restaurant2);
        restaurant3 = restaurantRepository.save(restaurant3);
    }

    private void addCuisines() {
        cuisine1 = new Cuisine();
        cuisine1.setName("Bananas");
        cuisineRepository.insert(cuisine1);

        cuisine2 = new Cuisine();
        cuisine2.setName("Apples");
        cuisineRepository.insert(cuisine2);

        cuisine3 = new Cuisine();
        cuisine3.setName("Oranges");
        cuisineRepository.insert(cuisine3);
    }

    private void addParties() {
        party1 = new Party();
        party1.setName("party1");
        party1.setRestaurantId(restaurant1.getId());
        party1.setNumberOfPeople(3);
        party2 = new Party();
        party2.setName("party2");
        party3 = new Party();
        party3.setName("party3");

        party1 = partyRepository.save(party1);
        party2 = partyRepository.save(party2);
        party3 = partyRepository.save(party3);
    }

    private void createDiners() {
        diner1 = new Diner(session1);
        diner2 = new Diner(session2);
        diner3 = new Diner(session3);
    }

    private void createServices() {
        service1 = new Service(restaurant1);
        service2 = new Service(restaurant2);
        service3 = new Service(restaurant3);

        restaurant1.getServices().clear();
        restaurant2.getServices().clear();
        restaurant3.getServices().clear();

        restaurant1.getServices().add(service1);
        restaurant2.getServices().add(service2);
        restaurant3.getServices().add(service3);

        restaurant1 = restaurantRepository.save(restaurant1);
        restaurant2 = restaurantRepository.save(restaurant2);
        restaurant3 = restaurantRepository.save(restaurant3);
    }

    private void createTables() {

        table1 = new Table();
        table1.setName("t1");
        table1.setId("t1");
        table1.setShape(TableShape.CIRCLE);

        table2 = new Table();
        table2.setName("t2");
        table2.setId("t2");
        table2.setShape(TableShape.CIRCLE);

        table3 = new Table();
        table3.setName("t3");
        table3.setId("t3");
        table3.setShape(TableShape.SQUARE);

        restaurant1.getTables().clear();
        restaurant2.getTables().clear();
        restaurant3.getTables().clear();

        restaurant1.getTables().add(table1);
        restaurant2.getTables().add(table2);
        restaurant3.getTables().add(table3);

        restaurant1 = restaurantRepository.save(restaurant1);
        restaurant2 = restaurantRepository.save(restaurant2);
        restaurant3 = restaurantRepository.save(restaurant3);
    }

    private void createAdjustments() {
        adjustment1 = new Adjustment();
        adjustment1.setId("adj1");
        adjustment1.setAdjustmentType(adjustmentType1);

        adjustment2 = new Adjustment();
        adjustment2.setId("adj2");
        adjustment2.setAdjustmentType(adjustmentType2);

        adjustment3 = new Adjustment();
        adjustment3.setId("adj3");
        adjustment3.setAdjustmentType(adjustmentType3);
    }

    private void addStaffs() {
        staff1 = new Staff();
        staff1.setName("foo");
        staff2 = new Staff();
        staff2.setName("man");
        staff3 = new Staff();
        staff3.setName("chu");

        staff1 = staffRepository.save(staff1);
        staff2 = staffRepository.save(staff2);
        staff3 = staffRepository.save(staff3);

        staffLogin = new Staff();
        staffLogin.setName("test");
        staffLogin.setUserName("test");
        staffLogin.setPin("1234");
        staffLogin.setRestaurantId(restaurant2.getId());
        staffLogin.setRole(StaffRole.MANAGER);
        String mash = null;
        mash = AuthenticationUtil.getPasswordMash(staffLogin, AuthenticationUtilTest.PASSWORD); //have to use a testable class. ugly
        staffLogin.setMash(mash);

        staffLogin = staffRepository.save(staffLogin);
    }

    private void addNotifications() {
        notification1 = new Notification();
        notification2 = new Notification();
        notification3 = new Notification();

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);
        notificationRepository.save(notification3);
    }

    private void addPrinters() {
        printer1 = new Printer();
        printer2 = new Printer();
        printer3 = new Printer();

        printerRepository.save(printer1);
        printerRepository.save(printer2);
        printerRepository.save(printer3);
    }

    private void addModifierGroups() {
        modifierGroup1 = new ModifierGroup();
        modifierGroup1.setName("ModifierGroup 1");
        modifierGroup1.setRestaurantId(restaurant1.getId());

        modifierGroup1 = modifierGroupRepository.save(modifierGroup1);
    }

    private void addModifiers() {
        modifier1 = new Modifier();
        modifier1.setPlu("mod1");
        modifier2 = new Modifier();
        modifier2.setPlu("mod2");
        modifier3 = new Modifier();
        modifier3.setPlu("mod3");

        modifierRepository.save(modifier1);
        modifierRepository.save(modifier2);
        modifierRepository.save(modifier3);

        modifierGroup1.getModifiers().add(modifier1);
        modifierGroup1.getModifiers().add(modifier2);
        modifierGroup1.getModifiers().add(modifier3);
        modifierGroupRepository.save(modifierGroup1);
    }

    private void createFloors() {
        floor1 = createFloor(1);
        floor2 = createFloor(2);
        floor3 = createFloor(3);
    }

    private Floor createFloor(int id) {
        Floor floor = new Floor();
        floor.setId(ControllerUtil.nextRandom(5));
        String layoutId = floor.getId() + IDAble.SEPARATOR + id;
        floor.setActiveLayout(layoutId);
        Layout layout = new Layout();
        layout.setFloor(floor.getId());
        layout.setName("layout"+id);
        layout.setId(layoutId);
        floor.getLayouts().add(layout);
        return floor;
    }

    private void createCourses() {
        course1 = new Course();
        course1.setId(IDAble.generateId(service1.getId()));
        course1.setName("course1");
        course1.setOrdering((short)0);

        course2 = new Course();
        course2.setId(IDAble.generateId(service1.getId()));
        course2.setName("course2");
        course2.setOrdering((short)1);

        course3 = new Course();
        course3.setId(IDAble.generateId(service1.getId()));
        course3.setName("course3");
        course3.setOrdering((short)0);
    }

    private void createHostCourseViews() {
        hostCourseView1 = new HostCourseView();
        hostCourseView1.setId(IDAble.generateId(service1.getId()));
        hostCourseView1.setName("hostCourseView1");
        hostCourseView1.setOrdering((short)0);
        hostCourseView1.setServiceId(service1.getId());

        hostCourseView2 = new HostCourseView();
        hostCourseView2.setId(IDAble.generateId(service1.getId()));
        hostCourseView2.setName("hostCourseView2");
        hostCourseView2.setOrdering((short)1);
        hostCourseView2.setServiceId(service2.getId());

        hostCourseView3 = new HostCourseView();
        hostCourseView3.setId(IDAble.generateId(service1.getId()));
        hostCourseView3.setName("hostCourseView3");
        hostCourseView3.setOrdering((short)0);
        hostCourseView3.setServiceId(service3.getId());
    }

    private void createTaxes() {
        tax1 = new TaxRate(0);
        tax2 = new TaxRate(200);
        tax3 = new TaxRate(175);

        taxRateRepository.save(tax1);
        taxRateRepository.save(tax2);
        taxRateRepository.save(tax3);
    }

    protected void setUpOrders() {
        setUpPrinters();

        setUpMenuItems();

        setUpOrders(session2, restaurant2, true, null);
    }

    protected void setUpOrders(Session session, Restaurant restaurant, boolean clearBatchesAndOrders, Course course) {
        order1.setMenuItemId(menuItem1.getId());
        order1.setMenuItem(menuItem1);
        order1.setItemPrice(menuItem1.getPrice());
        order1.setPriceOverride(menuItem1.getPrice());
        order1.setQuantity(1);
        order1.setTaxRate(tax1);
        order2.setMenuItemId(menuItem2.getId());
        order2.setMenuItem(menuItem2);
        order2.setItemPrice(menuItem2.getPrice());
        order2.setPriceOverride(menuItem2.getPrice());
        order2.setQuantity(1);
        order2.getModifiers().clear();
        order2.getModifiers().add(modifier1);
        order2.setTaxRate(tax2);
        order3.setMenuItemId(menuItem3.getId());
        order3.setMenuItem(menuItem3);
        order3.setItemPrice(menuItem3.getPrice());
        order3.setPriceOverride(menuItem3.getPrice());
        order3.setQuantity(1);
        order3.setTaxRate(tax3);
        //order1.setId(RandomStringUtils.randomAlphabetic(3));
        //order2.setId(RandomStringUtils.randomAlphabetic(3));
        //order3.setId(RandomStringUtils.randomAlphabetic(3));
        order1.setSessionId(session.getId());
        order2.setSessionId(session.getId());
        order3.setSessionId(session.getId());
        if(course != null) {
            order1.setCourseId(course.getId());
            order2.setCourseId(course.getId());
            order3.setCourseId(course.getId());
        }

        if(clearBatchesAndOrders) {
            orderRepository.deleteAll();
            batchRepository.deleteAll();
        }

        session.setRestaurantId(restaurant.getId());
        sessionRepository.save(session);
        order1.setId(null);
        order2.setId(null);
        order3.setId(null);
        order1 = orderRepository.insert(order1);
        order2 = orderRepository.insert(order2);
        order3 = orderRepository.insert(order3);
    }

    protected void amendOrder(Order order, MenuItem item, int priceOverride, int quantity, TaxRate tax1, List<Modifier> modifiers) {
        order.setMenuItem(item);
        order.setMenuItemId(item.getId());
        order.setItemPrice(item.getPrice());
        order.setPriceOverride(priceOverride);
        order.setQuantity(quantity);
        order.setTaxRate(tax1);
        order.setModifiers(modifiers);
        orderRepository.save(order);
    }

    protected void setUpMenuItems() {
        setUpMenuItems(restaurant2);
    }

    protected void setUpMenuItems(Restaurant restaurant) {
        menuItem1.setRestaurantId(restaurant.getId());
        menuItem2.setRestaurantId(restaurant.getId());
        menuItem3.setRestaurantId(restaurant.getId());
        menuItem1.setDefaultPrinter(printer1.getId());
        menuItem2.setDefaultPrinter(printer1.getId());
        menuItem3.setDefaultPrinter(printer2.getId());
        menuItem1.setTaxTypeId(tax1.getId());
        menuItem2.setTaxTypeId(tax2.getId());
        menuItem3.setTaxTypeId(tax3.getId());
        menuItem1.setType(ItemType.DRINK);
        menuItem2.setType(ItemType.FOOD);
        menuItem3.setType(ItemType.OTHER);
        menuItem1.setPrice(10);
        menuItem2.setPrice(20);
        menuItem3.setPrice(30);
        menuItemRepository.save(menuItem1);
        menuItemRepository.save(menuItem2);
        menuItemRepository.save(menuItem3);
    }

    protected void setUpPrinters() {
        setUpPrinters(restaurant2);
    }

    protected void setUpPrinters(Restaurant restaurant) {
        printer1.setRestaurantId(restaurant.getId());
        printer2.setRestaurantId(restaurant.getId());
        printer1.setIp("192.168.0.1");
        printer2.setIp("192.168.0.2");
        printerRepository.save(printer1);
        printerRepository.save(printer2);
    }

    protected void createSchedules() {
        schedule1 = new Schedule();
        ScheduledItem scheduledItem1 = new ScheduledItem();
        scheduledItem1.setId("s1");
        scheduledItem1.setTimeAfterStart(60);
        scheduledItem1.setNotificationType(NotificationType.SCHEDULED);
        scheduledItem1.setText("Slap out the menus");

        ScheduledItem scheduledItem2 = new ScheduledItem();
        scheduledItem2.setId("s2");
        scheduledItem2.setTimeAfterStart(120);
        scheduledItem2.setNotificationType(NotificationType.SCHEDULED);
        scheduledItem2.setText("Take an order");

        ScheduledItem scheduledItem3 = new ScheduledItem();
        scheduledItem3.setId("s3");
        scheduledItem3.setTimeAfterStart(240);
        scheduledItem3.setNotificationType(NotificationType.SCHEDULED);
        scheduledItem3.setText("Kick them out");

        schedule1.getScheduledItems().add(scheduledItem1);
        schedule1.getScheduledItems().add(scheduledItem2);
        schedule1.getScheduledItems().add(scheduledItem3);
    }

    protected void createOpeningHours() {
        LocalDateTime date1 = LocalDateTime.of(2025, Month.DECEMBER,25,0,0,0);
        LocalDateTime date2 = LocalDateTime.of(2025, Month.DECEMBER,26,0,0,0);

        AbsoluteBlackout blackout1 = new AbsoluteBlackout();
        blackout1.setStart(date1.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
        blackout1.setEnd(date1.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli() + (1000*60*60*24)-1);
        AbsoluteBlackout blackout2 = new AbsoluteBlackout();
        blackout2.setStart(date2.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
        blackout2.setEnd(date2.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli() + (1000*60*60*24)-1);
        List<AbsoluteBlackout> blackouts = new ArrayList<>();
        blackouts.add(blackout1);
        blackouts.add(blackout2);

        Map<DayOfWeek, List<HourSpan>> hours = new HashMap<>();
        HourSpan hourSpan1 = new HourSpan();
        hourSpan1.setHourOpen(7);
        hourSpan1.setHourClose(23);
        List<HourSpan> list1 = new ArrayList<>();
        list1.add(hourSpan1);
        HourSpan hourSpan2 = new HourSpan();
        hourSpan2.setHourOpen(10);
        hourSpan2.setHourClose(14);
        List<HourSpan> list2 = new ArrayList<>();
        list2.add(hourSpan2);
        hours.put(DayOfWeek.MONDAY, list1);
        hours.put(DayOfWeek.TUESDAY, list1);
        hours.put(DayOfWeek.WEDNESDAY, list1);
        hours.put(DayOfWeek.THURSDAY, list1);
        hours.put(DayOfWeek.FRIDAY, list1);
        hours.put(DayOfWeek.SATURDAY, list1);
        hours.put(DayOfWeek.SUNDAY, list2);

        openingHours1 = new OpeningHours();
        openingHours1.setAbsoluteBlackouts(blackouts);
        openingHours1.setBookingType(BookingType.RESERVATION);
        openingHours1.setHours(hours);
        openingHours1.setRestaurantId(restaurant1.getId());
        openingHours1 = openingHoursRepository.insert(openingHours1);

        openingHours2 = new OpeningHours();
        openingHours2.setAbsoluteBlackouts(blackouts);
        openingHours2.setBookingType(BookingType.RESERVATION);
        openingHours2.setHours(hours);
        openingHours2.setRestaurantId(restaurant2.getId());
        openingHours2 = openingHoursRepository.insert(openingHours2);

        openingHours3 = new OpeningHours();
        openingHours3.setAbsoluteBlackouts(blackouts);
        openingHours3.setBookingType(BookingType.RESERVATION);
        openingHours3.setHours(hours);
        openingHours3.setRestaurantId(restaurant3.getId());
        openingHours3 = openingHoursRepository.insert(openingHours3);
    }

    protected void createRestaurantFloorplanImages() throws Exception{
        File file = new File("src/test/resources/floor.jpg");
        restaurantImage1 = new RestaurantImage();
        restaurantImage1.setImage(IOUtils.toByteArray(new FileInputStream(file)));
        restaurantImage1.setImageType(RestaurantImageType.FLOOR_PLAN);
        restaurantImage1 = restaurantImageRepository.save(restaurantImage1);
    }

    protected void createRestaurantBillLogos() throws Exception {
        File file = new File("src/test/resources/floor.jpg");
        restaurantImage2 = new RestaurantImage();
        restaurantImage2.setImage(IOUtils.toByteArray(new FileInputStream(file)));
        restaurantImage2.setImageType(RestaurantImageType.BILL_LOGO);
        restaurantImage2 = restaurantImageRepository.save(restaurantImage2);
    }

    protected void createMenuGroups() {
        group1 = new Group();
        group1.setOrder(0);
        group2 = new Group();
        group2.setOrder(1);
        group3 = new Group();
        group3.setOrder(2);
    }

    protected String getTokenForStaff(Staff staff) {
        StaffView staffView = authenticationService.staffLogin(staff);
        return staffView.getAuthKey();
    }

    protected String getTokenForCustomer(Customer customer) {
        customer.setAuthKey(customer.getId() + IDAble.SEPARATOR + "123");
        customerRepository.save(customer);
        return customer.getAuthKey();
    }

    protected void createCountries() {
        country1 = new Country();
        country1.setName("United Kingdom of Great Britain");
        country1.setAcronym("GB");
        countryRepository.insert(country1);

        country2 = new Country();
        country2.setName("United States of America");
        country2.setAcronym("US");
        countryRepository.insert(country2);

        country3 = new Country();
        country3.setName("Germany");
        country3.setAcronym("DE");
        countryRepository.insert(country3);
    }

    protected AsyncOrderHandlerService getAsycHandlerService() throws Exception {
        AsyncOrderHandlerService asyncOrderHandlerService = new AsyncOrderHandlerService();
        Whitebox.setInternalState(asyncOrderHandlerService,"masterDataService",masterDataService);
        Whitebox.setInternalState(asyncOrderHandlerService,"sessionCalculationService",sessionCalculationService);
        Whitebox.setInternalState(asyncOrderHandlerService,"sessionService",sessionService);
        Whitebox.setInternalState(asyncOrderHandlerService, "paymentSensePullEnabled", paymentSensePullEnabled);

        return asyncOrderHandlerService;
    }

    protected AsyncCommunicationsService getAsyncCommunicationsService() throws Exception {
        AsyncCommunicationsService asyncCommunicationsService = new AsyncCommunicationsService();
        Whitebox.setInternalState(asyncCommunicationsService,"smsService",smsService);
        Whitebox.setInternalState(asyncCommunicationsService,"emailService",emailService);
        Whitebox.setInternalState(asyncCommunicationsService,"bookingService",bookingService);
        Whitebox.setInternalState(asyncCommunicationsService,"masterDataService",masterDataService);
        Whitebox.setInternalState(asyncCommunicationsService,"customerService",customerService);
        return asyncCommunicationsService;
    }

    protected CustomerBindingService getCustomerBindingService() throws Exception {
        CustomerBindingService customerBindingService = new CustomerBindingService();
        Whitebox.setInternalState(customerBindingService,"bookingService",bookingService);
        Whitebox.setInternalState(customerBindingService,"masterDataService",masterDataService);
        Whitebox.setInternalState(customerBindingService,"customerService",customerService);
        Whitebox.setInternalState(customerBindingService,"phoneNumberValidationService",phoneNumberValidationService);
        return customerBindingService;
    }

    protected List<HourSpan> createHourSpans() {
        HourSpan span = new HourSpan();
        span.setHourOpen(20);
        span.setMinuteOpen(0);
        span.setHourClose(24);
        span.setMinuteClose(0);

        return Collections.singletonList(span);
    }
}
