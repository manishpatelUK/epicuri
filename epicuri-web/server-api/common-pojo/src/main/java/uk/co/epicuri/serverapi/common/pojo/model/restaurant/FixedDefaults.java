package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class FixedDefaults {

    public static String CHECKIN_EXPIRATION_TIME = "CheckInExpirationTime";
    public static String DEFAULT_TIP_PERCENTAGE = "DefaultTipPercentage";
    public static String COVERS_BEFORE_AUTOTIP = "CoversBeforeAutoTip";
    public static String BIRTHDAY_TIMESPAN = "BirthdayTimespan";
    public static String WALKIN_EXPIRATION_TIME = "WalkinExpirationTime";
    public static String CLOSED_SESSION_MESSAGE = "ClosedSessionMessage";
    public static String SOCIAL_MEDIA_MESSAGE = "SocialMediaMessage";
    public static String MIN_TIME_BETWEEN_SERVICE_REQUESTS = "MinTimeBetweenServiceRequests"; //could expand
    public static String QUICK_ORDER_SESSION_TIMEOUT = "QuickOrderSessionTimeout";
    public static String MAX_COVERS_PER_RESERVATION = "MaxCoversPerReservation";
    public static String MAX_ACTIVE_RESERVATIONS = "MaxActiveReservations";
    public static String RESERVATION_FILTERTIME = "ReservationFilterTime(hours)";
    public static String RESERVATION_MINIMUM_TIME = "ReservationMinimumTime";
    public static String RESERVATION_LOCK_WINDOW = "ReservationLockWindow";
    public static String RESERVATION_TIMESLOT = "ReservationTimeSlot";
    public static String MAX_ACTIVE_RESERVATIONS_COVERS = "MaxActiveReservationsCovers";
    public static String MAX_TAKEAWAYS_PER_HOUR = "MaxTakeawaysPerHour";
    public static String MAX_TAKEAWAY_VALUE = "MaxTakeawayValue";
    public static String MIN_TAKEAWAY_VALUE = "MinTakeawayValue";
    public static String DELIVERY_SURCHARGE = "DeliverySurcharge";
    public static String MAX_DELIVERY_RADIUS = "MaxDeliveryRadius";
    public static String TAKEAWAY_MINIMUM_TIME = "TakeawayMinimumTime";
    public static String TAKEAWAY_LOCK_WINDOW = "TakeawayLockWindow";
    public static String TAX_LABEL = "TaxLabel";
    public static String TAX_REFERENCE_LABEL = "TaxReferenceLabel";
    public static String BILL_PREFIX = "BillPrefix";
    public static String TICKET_ON_SESSION_CLOSE_TIMEOUT = "TicketSessionCloseTimeout"; //measured in seconds
    public static String MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC = "MinimumTakeawayValueWithoutCC";
    public static String ALLOW_SELF_SERVICE_DELIVERY_TO_LOCATION = "AllowSelfServiceDeliveryToLocation";
    public static String FREE_DELIVERY_RADIUS = "FreeDeliveryRadius";
    public static String EMAIL_RECEIPTS_ENABLED = "EmailReceiptsEnabled";
    public static String PRINT_SHORT_CODE = "PrintShortCode";
    public static String DOUBLE_HEIGHT_ORDER_PRINTS = "DoubleHeightOrderPrints";
    public static String DOUBLE_WIDTH_ORDER_PRINTS = "DoubleWidthOrderPrints";
    public static String BILL_PRINT_FONT_SIZE = "BillPrintFontSize";
    public static String CONDITIONAL_SERVICE_CHARGE_TEXT = "ConditionalServiceChargeText";
    public static String RESERVATIONS_ENABLED = "ReservationsEnabled";
    public static String ITEM_PRINT_BOTTOM = "ItemPrintBottom";
    public static String ITEM_PRINT_TOP = "ItemPrintTop";
    public static String PS_ALLOW_PAY_AT_TABLE = "AllowPayAtTablePaymentSense";
    public static String PRINT_LINES_BETWEEN_COURSES = "PrintLinesBetweenCourses";
    public static String REPRINT_BILL_AT_CLOSE = "ReprintBillAtClose";
    public static String FORCE_LOCATION_ON_QO = "ForceLocationOnQO";
    public static String SHOW_TABLE_ON_QO = "ShowTableOnQO";
    public static String SHOW_TAB_ON_QO = "ShowTabOnQO";
    public static String SHOW_REFUND_ON_QO = "ShowRefundOnQO";
    public static String ENABLE_STOCK_COUNTDOWN = "EnableStockCountdown";
    public static String AUTO_STOCK_UNAVAILABLE = "AutoStockUnavailable";
    public static String QO_SCREENSIZE_THRESHOLD = "QOScreenSizeThreshold";
    public static String QO_FONT_SCALE_UP = "QOFontScaleUp";
    public static String SHOW_ON_ACCOUNT_QO = "QOOnAccountButton";
    public static String ENABLE_DEFER_SESSIONS = "EnableDeferredSessions";
    public static String APPLY_AUTOTIP_TO_QO = "ApplyAutoTipToQO";
    public static String FORCE_PAYMENT_ON_SELF_SERVICE = "ForcePaymentOnSelfService";

    public static final Map<String,Class> TYPE_MAP = createTypeMap();

    private static Map<String, Class> createTypeMap() {
        //create types
        Map<String,Class> types = new HashMap<>();
        types.put(CHECKIN_EXPIRATION_TIME, Integer.class);
        types.put(DEFAULT_TIP_PERCENTAGE, Double.class);
        types.put(COVERS_BEFORE_AUTOTIP, Integer.class);
        types.put(BIRTHDAY_TIMESPAN, Integer.class);
        types.put(WALKIN_EXPIRATION_TIME, Integer.class);
        types.put(CLOSED_SESSION_MESSAGE, String.class);
        types.put(SOCIAL_MEDIA_MESSAGE, String.class);
        types.put(MIN_TIME_BETWEEN_SERVICE_REQUESTS, Integer.class);
        types.put(QUICK_ORDER_SESSION_TIMEOUT, Integer.class);
        types.put(MAX_COVERS_PER_RESERVATION, Integer.class);
        types.put(MAX_ACTIVE_RESERVATIONS, Integer.class);
        types.put(RESERVATION_FILTERTIME, Integer.class);
        types.put(RESERVATION_MINIMUM_TIME, Integer.class);
        types.put(RESERVATION_LOCK_WINDOW, Integer.class);
        types.put(RESERVATION_TIMESLOT, Integer.class);
        types.put(MAX_ACTIVE_RESERVATIONS_COVERS, Integer.class);
        types.put(MAX_TAKEAWAYS_PER_HOUR, Integer.class);
        types.put(MAX_TAKEAWAY_VALUE, Double.class);
        types.put(MIN_TAKEAWAY_VALUE, Double.class);
        types.put(DELIVERY_SURCHARGE, Double.class);
        types.put(MAX_DELIVERY_RADIUS, Double.class);
        types.put(TAKEAWAY_MINIMUM_TIME, Integer.class);
        types.put(TAKEAWAY_LOCK_WINDOW, Integer.class);
        types.put(TAX_LABEL, String.class);
        types.put(TAX_REFERENCE_LABEL, String.class);
        types.put(BILL_PREFIX, String.class);
        types.put(TICKET_ON_SESSION_CLOSE_TIMEOUT, Integer.class);
        types.put(MAXIMUM_TAKEAWAY_AMOUNT_WITHOUT_CC, Double.class);
        types.put(ALLOW_SELF_SERVICE_DELIVERY_TO_LOCATION, Boolean.class);
        types.put(FREE_DELIVERY_RADIUS, Double.class);
        types.put(EMAIL_RECEIPTS_ENABLED, Boolean.class);
        types.put(PRINT_SHORT_CODE, Boolean.class);
        types.put(DOUBLE_HEIGHT_ORDER_PRINTS, Boolean.class);
        types.put(DOUBLE_WIDTH_ORDER_PRINTS, Boolean.class);
        types.put(BILL_PRINT_FONT_SIZE, Double.class);
        types.put(CONDITIONAL_SERVICE_CHARGE_TEXT, String.class);
        types.put(RESERVATIONS_ENABLED, Boolean.class);
        types.put(ITEM_PRINT_BOTTOM, Boolean.class);
        types.put(ITEM_PRINT_TOP, Boolean.class);
        types.put(PS_ALLOW_PAY_AT_TABLE, Boolean.class);
        types.put(PRINT_LINES_BETWEEN_COURSES, Boolean.class);
        types.put(REPRINT_BILL_AT_CLOSE, Boolean.class);
        types.put(FORCE_LOCATION_ON_QO, Boolean.class);
        types.put(SHOW_TABLE_ON_QO, Boolean.class);
        types.put(SHOW_TAB_ON_QO, Boolean.class);
        types.put(SHOW_REFUND_ON_QO, Boolean.class);
        types.put(ENABLE_STOCK_COUNTDOWN, Boolean.class);
        types.put(AUTO_STOCK_UNAVAILABLE, Boolean.class);
        types.put(QO_SCREENSIZE_THRESHOLD, Double.class);
        types.put(QO_FONT_SCALE_UP, Double.class);
        types.put(SHOW_ON_ACCOUNT_QO, Boolean.class);
        types.put(ENABLE_DEFER_SESSIONS, Boolean.class);
        types.put(APPLY_AUTOTIP_TO_QO, Boolean.class);
        types.put(FORCE_PAYMENT_ON_SELF_SERVICE,Boolean.class);

        checkDefaults(types);

        return Collections.unmodifiableMap(types);
    }

    private static void checkDefaults(Map<String,Class> types) {
        Field[] fields = FixedDefaults.class.getFields();
        for(Field field : fields) {
            int modifiers = field.getModifiers();
            if(Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                if(field.getType() == String.class) {
                    try {
                        String defaultName = field.get(null).toString();
                        if(!types.containsKey(defaultName)) {
                            throw new IllegalStateException(
                                    "Missing restaurant default type: " + defaultName
                            + ". This has been added to FixedDefaults but the TYPE_MAP does not contain a type for it.");
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Cannot read Default properties");
                    }
                }
            }
        }


    }
}
