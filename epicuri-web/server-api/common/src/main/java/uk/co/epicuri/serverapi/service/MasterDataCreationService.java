package uk.co.epicuri.serverapi.service;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PaymentSenseConstants;
import uk.co.epicuri.serverapi.common.pojo.external.stripe.StripeConstants;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.PreferenceType;
import uk.co.epicuri.serverapi.common.pojo.model.booking.BookingStatics;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentTypeType;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.repository.BookingStaticsRepository;
import uk.co.epicuri.serverapi.repository.DefaultsRepository;
import uk.co.epicuri.serverapi.repository.PreferencesRepository;
import uk.co.epicuri.serverapi.service.external.AWSService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@org.springframework.stereotype.Service
public class MasterDataCreationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MasterDataCreationService.class);

    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private DefaultsRepository defaultsRepository;

    @Autowired
    private PreferencesRepository preferencesRepository;

    @Autowired
    private AWSService awsService;

    @Autowired
    private BookingStaticsRepository bookingStaticsRepository;

    public void addCourse(Service defaultService, String name, short order) {
        Course course = new Course(defaultService);
        course.setName(name);
        course.setOrdering(order);
        defaultService.getCourses().add(course);
    }

    public Menu createDefaultMenu(Restaurant restaurant, String name, int order) {
        Menu menu = new Menu();
        menu.setRestaurantId(restaurant.getId());
        menu.setActive(true);
        menu.setName(name);
        menu.setOrder(order);
        return menu;
    }

    public Restaurant createRestaurantWithDefaultMasterData(Restaurant restaurant) {
        if(restaurant.getId() != null) {
            throw new IllegalArgumentException("Restaurant must not already be in database (id is not null)");
        }

        restaurant.setRestaurantDefaults(masterDataService.getDefault().stream().map(RestaurantDefault::new).collect(Collectors.toList()));
        restaurant = masterDataService.insertRestaurant(restaurant);

        Menu defaultMenu = masterDataService.upsert(createDefaultMenu(restaurant, "Main Menu", 0));
        Menu defaultTakeawayMenu = masterDataService.upsert(createDefaultMenu(restaurant, "Takeaway Menu", 1));
        Menu defaultSelfServiceMenu = masterDataService.upsert(createDefaultMenu(restaurant, "Self Service Menu", 2));

        restaurant.setTakeawayMenu(defaultTakeawayMenu.getId());

        // create a default service
        Service defaultService = new Service(restaurant);
        defaultService.setName("Default Service");
        defaultService.setActive(true);
        defaultService.setDefaultMenuId(defaultMenu.getId());
        defaultService.setSelfServiceMenuId(defaultSelfServiceMenu.getId());
        defaultService.setSessionType(SessionType.SEATED);
        defaultService.setDefaultService(true);

        addCourse(defaultService, RestaurantConstants.IMMEDIATE_COURSE_NAME, (short)0);
        addCourse(defaultService, "Starters", (short)1);
        addCourse(defaultService, "Mains", (short)2);
        addCourse(defaultService, "Dessert", (short)3);

        Service adhocService = new Service(restaurant);
        adhocService.setName("Adhoc Service");
        adhocService.setActive(true);
        adhocService.setDefaultMenuId(defaultMenu.getId());
        adhocService.setSessionType(SessionType.ADHOC);
        addCourse(adhocService, RestaurantConstants.IMMEDIATE_COURSE_NAME, (short)0);

        Service takeawayService = new Service(restaurant);
        takeawayService.setName("Takeaway Service");
        takeawayService.setActive(true);
        takeawayService.setDefaultMenuId(defaultMenu.getId());
        takeawayService.setSessionType(SessionType.TAKEAWAY);
        addCourse(takeawayService, RestaurantConstants.IMMEDIATE_COURSE_NAME, (short)0);

        restaurant.getServices().add(defaultService);
        restaurant.getServices().add(adhocService);
        restaurant.getServices().add(takeawayService);

        if(restaurant.getStaffFacingId() == null) {
            long number = masterDataService.getNumberOfRestaurants();
            while(true) {
                Restaurant test = masterDataService.getRestaurantByStaffFacingId(String.valueOf(number));
                if(test == null) {
                    break;
                } else {
                    number++;
                }
            }
            restaurant.setStaffFacingId(String.valueOf(number));
        }

        restaurant.setIANATimezone("Europe/London");

        //set a default profile image
        try {
            List<String> urls = awsService.getGenericRestaurantImages();
            if(urls.size() > 0) {
                restaurant.getImageURLs().add(urls.get(RandomUtils.nextInt(0, urls.size()-1)));
            }
        } catch (Exception ex) {
            LOGGER.warn("Could not get data from AWS: {}", ex.getMessage());
        }

        //add default payment and gratuities
        Map<String,AdjustmentType> currentAdjustments = masterDataService.getAdjustmentType().stream().collect(Collectors.toMap(AdjustmentType::getName, Function.identity()));
        addIfAvailable(restaurant, currentAdjustments, "Cash");
        addIfAvailable(restaurant, currentAdjustments, "Visa");
        addIfAvailable(restaurant, currentAdjustments, "Mastercard");
        addIfAvailable(restaurant, currentAdjustments, "Gratuity");
        addIfAvailable(restaurant, currentAdjustments, "Staff Discount");
        addIfAvailable(restaurant, currentAdjustments, "Discount (Good Will)");
        addIfAvailable(restaurant, currentAdjustments, "Discount (Promotion)");

        addStaff(restaurant);
        addDefaultPermissions(restaurant);

        return masterDataService.upsert(restaurant);
    }

    private void addDefaultPermissions(Restaurant restaurant) {
        StaffPermissions permissions = createDefaultPermissions(restaurant.getId());
        restaurant.setStaffPermissions(permissions);
    }

    public static StaffPermissions createDefaultPermissions(String restaurantId) {
        StaffPermissions permissions = createBlanketPermissions(restaurantId, true, StaffRole.EPICURI_ADMIN, StaffRole.SUPER_ADMIN, StaffRole.SITE_OWNER, StaffRole.MANAGER);
        createPartialPermissions(permissions, StaffRole.ASSISTANT_MANAGER, StaffRole.HOST_STAFF, StaffRole.WAIT_STAFF);
        createBlanketPermissions(permissions, false, StaffRole.THIRD_PARTY, StaffRole.UNKNOWN);
        return permissions;
    }

    private static StaffPermissions createBlanketPermissions(String restaurantId, boolean perms, StaffRole... roles) {
        StaffPermissions permissions = new StaffPermissions();
        permissions.setRestaurantId(restaurantId);

        createBlanketPermissions(permissions, perms, roles);

        return permissions;
    }

    private static void createBlanketPermissions(StaffPermissions permissions, boolean perms, StaffRole... roles) {
        for(StaffRole role : roles) {
            IndividualStaffPermission individualStaffPermission = new IndividualStaffPermission();
            individualStaffPermission.setRole(role);
            for(WaiterAppFeature feature : WaiterAppFeature.values()) {
                individualStaffPermission.getPermissions().put(feature, perms);
            }
            permissions.getPermissions().add(individualStaffPermission);
        }
    }

    private static void createPartialPermissions(StaffPermissions permissions, StaffRole... staffRoles) {
        for(StaffRole role : staffRoles) {
            IndividualStaffPermission individualStaffPermission = new IndividualStaffPermission();
            individualStaffPermission.setRole(role);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.LOGIN_MANAGER, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.FLOOR_PLAN_MANAGER, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.MENU_MANAGER, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.CASH_UP, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.CASH_UP_SIMULATION, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.PORTAL, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.ORDER_VOID, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.DRAWER_KICK_NO_SALE, true);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.MANUAL_DRAWER_KICK, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.FORCE_CLOSE, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.ADD_DELETE_PAYMENT, true);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.ADD_DELETE_DISCOUNT, true);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.SESSION_HISTORY, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.GENERIC_REFUND, false);
            individualStaffPermission.getPermissions().put(WaiterAppFeature.PRICE_OVERRIDE, false);

            permissions.getPermissions().add(individualStaffPermission);
        }
    }

    private void addStaff(Restaurant restaurant) {
        Staff staff = new Staff();
        staff.setRestaurantId(restaurant.getId());
        staff.setName("Epicuri Admin - DO NOT DELETE");
        staff.setRole(StaffRole.EPICURI_ADMIN);
        staff.setUserName("epicuriadmin");
        staff.setPin("1234");
        staff.setMash("0ecdd6c0c433bf82c80c4115b8efd0a3");
        masterDataService.upsert(staff);
    }

    private void addIfAvailable(Restaurant restaurant, Map<String, AdjustmentType> currentAdjustments, String name) {
        if(currentAdjustments.containsKey(name)) {
            restaurant.getAdjustmentTypes().add(currentAdjustments.get(name).getId());
        }
    }


    //// ----- STUFF THAT IS ONLY RUN THE FIRST TIME AN ENVIRONMENT GOES LIVE

    public void createDefaultAdjustments() {
        Map<String,AdjustmentType> current = masterDataService.getAdjustmentType().stream().collect(Collectors.toMap(AdjustmentType::getName, Function.identity()));

        //create external adjustment types
        checkAndCreateAdjustmentType(current, MewsConstants.MEWS_ADJUSTMENT_TYPE, AdjustmentTypeType.PAYMENT, false);
        checkAndCreateAdjustmentType(current, PaymentSenseConstants.PS_ADJUSTMENT_TYPE, AdjustmentTypeType.PAYMENT, false);
        checkAndCreateAdjustmentType(current, PaymentSenseConstants.PS_ADJUSTMENT_OTHER_TYPE, AdjustmentTypeType.PAYMENT, false);
        checkAndCreateAdjustmentType(current, PaymentSenseConstants.PS_ADJUSTMENT_GRATUITY_TYPE, AdjustmentTypeType.GRATUITY, false);
        //Stripe
        checkAndCreateAdjustmentType(current, StripeConstants.STRIPE_PAYMENT_TYPE, AdjustmentTypeType.PAYMENT, false);
        checkAndCreateAdjustmentType(current, StripeConstants.STRIPE_GRATUITY_TYPE, AdjustmentTypeType.GRATUITY, false);

        //create CASH
        checkAndCreateAdjustmentType(current, "Cash", AdjustmentTypeType.PAYMENT, true);

        //cards
        checkAndCreateAdjustmentType(current, "Visa", AdjustmentTypeType.PAYMENT, false);
        checkAndCreateAdjustmentType(current, "Mastercard", AdjustmentTypeType.PAYMENT, false);

        //gratuity
        checkAndCreateAdjustmentType(current, "Gratuity", AdjustmentTypeType.GRATUITY, false);

        //discount
        checkAndCreateAdjustmentType(current, "Staff Discount", AdjustmentTypeType.GRATUITY, false);
        checkAndCreateAdjustmentType(current, "Discount (Good Will)", AdjustmentTypeType.GRATUITY, false);
        checkAndCreateAdjustmentType(current, "Discount (Promotion)", AdjustmentTypeType.GRATUITY, false);
    }

    protected void checkAndCreateAdjustmentType(Map<String,AdjustmentType> current, String name, AdjustmentTypeType adjustmentTypeType, boolean supportsChange) {
        if(current.containsKey(name)) {
            return;
        }

        AdjustmentType adjustmentType = masterDataService.getAdjustmentTypeByName(name);
        if(adjustmentType != null) {
            return;
        }

        adjustmentType = new AdjustmentType();
        adjustmentType.setType(adjustmentTypeType);
        adjustmentType.setName(name);
        adjustmentType.setSupportsChange(supportsChange);

        adjustmentType = masterDataService.insertAdjustmentType(adjustmentType);
        current.put(name, adjustmentType);
    }

    public void recreateDefaults() {
        defaultsRepository.deleteAll();
        defaultsRepository.save(allDefaults());
    }

    public static List<Default> allDefaults() {
        List<Default> defaults = new ArrayList<>();
        defaults.add(new Default(FixedDefaults.CHECKIN_EXPIRATION_TIME,15,"Minutes","Minutes after checking in before a CheckIn expires",0));
        defaults.add(new Default(FixedDefaults.DEFAULT_TIP_PERCENTAGE,10D,"Percentage","Default % value for automatic tips",0));
        defaults.add(new Default(FixedDefaults.COVERS_BEFORE_AUTOTIP,4,"Number","Number of diners at a table before a tip is automatically added",0));
        defaults.add(new Default(FixedDefaults.BIRTHDAY_TIMESPAN,10,"Days","Show a diners birthday indicator if it falls in +/- these number of days",0));
        defaults.add(new Default(FixedDefaults.WALKIN_EXPIRATION_TIME,90,"Minutes","Time before a Walkin expires",0));
        defaults.add(new Default(FixedDefaults.CLOSED_SESSION_MESSAGE,"Thank you for dining with us!","-","Message to be displayed upon closing a session",0));
        defaults.add(new Default(FixedDefaults.SOCIAL_MEDIA_MESSAGE,"@EpicuriUK helped me fill my belly. Highly recommended! I am #friendswithepicuri","-","Message to be displayed for social media",0));
        defaults.add(new Default(FixedDefaults.MIN_TIME_BETWEEN_SERVICE_REQUESTS,5,"Minutes","The minimum amount of time between service calls",0));
        defaults.add(new Default(FixedDefaults.QUICK_ORDER_SESSION_TIMEOUT,15,"Minutes","Minutes before a quick order times out and is removed from the kitchen screen",0));
        defaults.add(new Default(FixedDefaults.MAX_COVERS_PER_RESERVATION,6,"Number","The maximum number of covers on a particular reservation",0));
        defaults.add(new Default(FixedDefaults.MAX_ACTIVE_RESERVATIONS,5,"Number","The maximum number of active reservations within ReservationTimeslot",0));
        defaults.add(new Default(FixedDefaults.RESERVATION_FILTERTIME,12,"Hours","Reservations to be displayed from now till X hours in the future",0));
        defaults.add(new Default(FixedDefaults.RESERVATION_MINIMUM_TIME,120,"Minutes","Minimum amount of time required before a reservation can be booked",0));
        defaults.add(new Default(FixedDefaults.RESERVATION_LOCK_WINDOW,120,"Minutes","The time before a reservation is due where changes are locked",0));
        defaults.add(new Default(FixedDefaults.RESERVATION_TIMESLOT,120,"Minutes","Average session duration, used for capacity checking",0));
        defaults.add(new Default(FixedDefaults.MAX_ACTIVE_RESERVATIONS_COVERS,30,"Number","Maximum number of covers in reservations within ReservationTimeslot",0));
        defaults.add(new Default(FixedDefaults.MAX_TAKEAWAYS_PER_HOUR,8,"Number","Maximum number of Takeaways in a 60 min slot",0));
        defaults.add(new Default(FixedDefaults.MAX_TAKEAWAY_VALUE,80D,"Currency","Maximum value of a Takeaway request",0));
        defaults.add(new Default(FixedDefaults.MIN_TAKEAWAY_VALUE,10D,"Currency","Minimum value of a Takeaway request",0));
        defaults.add(new Default(FixedDefaults.DELIVERY_SURCHARGE,1.5D,"Currency","Surcharge for a delivered Takeaway",0));
        defaults.add(new Default(FixedDefaults.MAX_DELIVERY_RADIUS,1.5D,"Miles","Radius for free Takeaway Delivery",0));
        defaults.add(new Default(FixedDefaults.TAKEAWAY_LOCK_WINDOW,60,"Minutes","The time before a takeaway is due where changes are locked",0));
        defaults.add(new Default(FixedDefaults.TAX_LABEL,"VAT","-","Localised string for tax reference in country (on receipt)",0));
        defaults.add(new Default(FixedDefaults.TAX_REFERENCE_LABEL,"Our VAT Number",null,null,0));
        defaults.add(new Default(FixedDefaults.TAKEAWAY_MINIMUM_TIME,30,"Minutes","Minimum amount of time required before a takeaway can be booked",0));
        defaults.add(new Default(FixedDefaults.BILL_PREFIX,"","-","Prefix on billing IDs (receipts)",0));
        defaults.add(new Default(FixedDefaults.TICKET_ON_SESSION_CLOSE_TIMEOUT,300,null,null,0));
        defaults.add(new Default(FixedDefaults.MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC,15D,"Currency","Max allowable amount of a takeaway value before Credit Card payment is forced on guest app",0));
        defaults.add(new Default(FixedDefaults.ALLOW_SELF_SERVICE_DELIVERY_TO_LOCATION,false,"Boolean","If true, guest app users can have items delivered to their specified location",0));
        defaults.add(new Default(FixedDefaults.FREE_DELIVERY_RADIUS,2D,"Miles","Delivery radius where it is free to deliver",0));
        defaults.add(new Default(FixedDefaults.EMAIL_RECEIPTS_ENABLED,true,"Boolean","Whether to give the waiter the option to email receipts (and to automatically send them if email address is known)",0));
        defaults.add(new Default(FixedDefaults.PRINT_SHORT_CODE, true, "Boolean", "Whether to print short code in kitchen or long name", 0));
        defaults.add(new Default(FixedDefaults.DOUBLE_HEIGHT_ORDER_PRINTS, true, "Boolean", "Use double height setting on kitchen/bar printouts", 0));
        defaults.add(new Default(FixedDefaults.DOUBLE_WIDTH_ORDER_PRINTS, true, "Boolean", "Use double height setting on kitchen/bar printouts", 0));
        defaults.add(new Default(FixedDefaults.BILL_PRINT_FONT_SIZE, 12D, "Double", "Font size of text on bill prints", 0));
        defaults.add(new Default(FixedDefaults.CONDITIONAL_SERVICE_CHARGE_TEXT, "", "", "If service charge is 0%, show this text, e.g. Service charge not included. If you don't want this behaviour, leave empty", 0));
        defaults.add(new Default(FixedDefaults.RESERVATIONS_ENABLED, true, "", "If true, will allow reservations from guest app (reveal button) and booking widget", 0));
        defaults.add(new Default(FixedDefaults.ITEM_PRINT_BOTTOM, false, "", "If true, order prints will print the session info at the bottom of the printout", 0));
        defaults.add(new Default(FixedDefaults.ITEM_PRINT_TOP, true, "", "If true, order prints will print the session info at the top of the printout", 0));
        defaults.add(new Default(FixedDefaults.PS_ALLOW_PAY_AT_TABLE, false, "", "If true, do calls to PaymentSense servers for Pay At Table (set to false for counter only services)", 0));
        defaults.add(new Default(FixedDefaults.PRINT_LINES_BETWEEN_COURSES, false, "", "If true, will print a line between each course", 0));
        defaults.add(new Default(FixedDefaults.REPRINT_BILL_AT_CLOSE, false, "", "If true, will force a reprint of the customer bill at session close", 0));
        defaults.add(new Default(FixedDefaults.FORCE_LOCATION_ON_QO, false, "", "If true, will force Quick Order screen to ask for a table/location when an order is created", 0));
        defaults.add(new Default(FixedDefaults.SHOW_TABLE_ON_QO, true, "", "If true, will show the table option on the Quick Order screen", 0));
        defaults.add(new Default(FixedDefaults.SHOW_TAB_ON_QO, true, "", "If true, will show the tab option on the Quick Order screen", 0));
        defaults.add(new Default(FixedDefaults.SHOW_REFUND_ON_QO, true, "", "If true, will show the refund option on the Quick Order screen", 0));
        defaults.add(new Default(FixedDefaults.ENABLE_STOCK_COUNTDOWN, false, "", "If true, will decrement stock number on every order", 0));
        defaults.add(new Default(FixedDefaults.AUTO_STOCK_UNAVAILABLE, true, "", "If true and ENABLE_STOCK_COUNTDOWN is true, will automatically set any item with the PLU to ", 0));
        defaults.add(new Default(FixedDefaults.QO_SCREENSIZE_THRESHOLD, 10D, "Inches", "Inches across screen; used to compare against to determine whether to change font size in QO", 0));
        defaults.add(new Default(FixedDefaults.QO_FONT_SCALE_UP, 1D, "", "Multiplier to apply to font size, e.g. 2 means double the font size", 0));
        defaults.add(new Default(FixedDefaults.SHOW_ON_ACCOUNT_QO, false, "", "If true, will show the 'On Account' option in QO", 0));
        defaults.add(new Default(FixedDefaults.ENABLE_DEFER_SESSIONS, false, "", "If true, user can defer sessions", 0));
        defaults.add(new Default(FixedDefaults.APPLY_AUTOTIP_TO_QO, false, "", "If true, applies tip automatically to QO", 0));
        defaults.add(new Default(FixedDefaults.FORCE_PAYMENT_ON_SELF_SERVICE, false, "", "If true, forces payment at the end of a self-service", 0));

        return defaults;
    }

    public void recreateDefaultPreferences() {
        preferencesRepository.deleteAll();

        preferencesRepository.insert(new Preference(PreferenceType.DIETARY, "Kosher"));
        preferencesRepository.insert(new Preference(PreferenceType.DIETARY, "Halal"));
        preferencesRepository.insert(new Preference(PreferenceType.DIETARY, "Vegetarian"));
        preferencesRepository.insert(new Preference(PreferenceType.DIETARY, "Vegan"));

        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Celery"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Gluten"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Crustaceans"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Eggs"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Fish"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Lupin"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Milk"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Molluscs"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Mustard"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Nuts"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Peanuts"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Sesame Seeds"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Soya"));
        preferencesRepository.insert(new Preference(PreferenceType.ALLERGY, "Sulphites"));

        preferencesRepository.insert(new Preference(PreferenceType.FOOD, "Bitter"));
        preferencesRepository.insert(new Preference(PreferenceType.FOOD, "Salty"));
        preferencesRepository.insert(new Preference(PreferenceType.FOOD, "Savoury"));
        preferencesRepository.insert(new Preference(PreferenceType.FOOD, "Spicy"));
        preferencesRepository.insert(new Preference(PreferenceType.FOOD, "Sweet"));
        preferencesRepository.insert(new Preference(PreferenceType.FOOD, "Umami"));
    }

    public void recreateBookingStatics() {
        bookingStaticsRepository.deleteAll();

        BookingStatics english = new BookingStatics();
        english.setLanguage("en");
        english.setBackLabel("Back");
        english.setDateStepMessage("Date of booking and number of guests?");
        english.setDateStepButtonLabel("Next");
        english.setTimeStepMessage("Time of booking?");
        english.setTimeStepButtonLabel("Next");
        english.setDetailsStepMessage("Almost there! Just a few details...");
        english.setDetailsStepButtonLabel("Book!");
        english.setNameLabel("Name");
        english.setNumberLabel("Phone");
        english.setEmailLabel("Email");
        english.setNotesLabel("Notes");
        english.setConfirmationMessage("Thanks! See you soon!");
        english.setTelephoneValidationError("Valid phone number is required");
        english.setNameValidationError("Name is required");
        english.setTitle("Book a table");
        english.setMarketingMessage("Please keep me up to date with news and offers");
        english.setConfirmationStepMessage("Confirmed");
        english.setBiggerPartiesLabel("Please call us for larger parties");
        english.setToleranceBreachMessage("Sorry, we have no availability at this time. Please pick another date/time or call us.");
        english.setTimeInPastMessage("Cannot create a booking in the past!");
        english.setNoTimeSlotsMessage("No availability");
        english.setNumberGuestsLabel("Guests");

        BookingStatics dutch = new BookingStatics();
        dutch.setLanguage("nl");
        dutch.setBackLabel("Terug");
        dutch.setDateStepMessage("Datum van reservatie en aantal gasten?");
        dutch.setDateStepButtonLabel("Volgende");
        dutch.setTimeStepMessage("Tijdstip van reservatie?");
        dutch.setTimeStepButtonLabel("Volgende");
        dutch.setDetailsStepMessage("We hebben nog een aantal details nodig...");
        dutch.setDetailsStepButtonLabel("Reserveren!");
        dutch.setNameLabel("Naam");
        dutch.setNumberLabel("Telefoonnummer");
        dutch.setEmailLabel("Email");
        dutch.setNotesLabel("Opmerkingen");
        dutch.setConfirmationMessage("Bedankt! Tot zo!");
        dutch.setTelephoneValidationError("Geldig telefoonnummer is vereist");
        dutch.setNameValidationError("Naam is vereist");
        dutch.setTitle("Reserveer een Tafel");
        dutch.setMarketingMessage("Houdt me op de hoogte van nieuws & aanbiedingen");
        dutch.setConfirmationStepMessage("Bevestigd");
        dutch.setBiggerPartiesLabel("Gelieve ons te bellen voor grotere groepen");
        dutch.setToleranceBreachMessage("Sorry, wij zijn op dat moment niet beschikbaar/open. Gelieve een reservatie te plaatsen op een andere datum/tijdstip.");
        dutch.setTimeInPastMessage("Kan geen reservatie maken in het verleden!");
        dutch.setNoTimeSlotsMessage("Niet beschikbaar");
        dutch.setNumberGuestsLabel("Gasten");

        bookingStaticsRepository.insert(english);
        bookingStaticsRepository.insert(dutch);
    }
}
