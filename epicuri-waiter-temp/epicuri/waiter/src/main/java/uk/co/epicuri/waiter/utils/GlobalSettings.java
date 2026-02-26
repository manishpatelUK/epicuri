package uk.co.epicuri.waiter.utils;


import java.util.Locale;

public class GlobalSettings {
    public static final int NOTIFICATION_PRINT_QUEUE = 1;
    public static final int NOTIFICATION_RECEIPT_PRINT = 2;
    public static final int NOTIFICATION_CASHUP_PRINT = 3;
    public static final int NOTIFICATION_RECEIPT_PRINT_FAILED = 4;
    public static final int NOTIFICATION_CASHUP_PRINT_FAILED = 5;

	public static final String PREF_APP_SETTINGS = "settings";
	public static final String PREF_APP_QUICK_SWITCH = "quickSwitchData";
    public static final String PREF_KEY_TABLE = "table_%d";
    public static final String PREF_KEY_TOKEN = "token";
    public static final String PREF_KEY_ID = "id";
    public static final String PREF_KEY_RECEIPT_PRINTER = "receiptPrinterId";
    public static final String PREF_KEY_NAME = "name";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_MANAGER = "manager";
    public static final String PREF_KEY_ROLE = "role";
    public static final String PREF_KEY_PIN = "pin";
    public static final String PREF_KEY_SHOW_TABLE_NAME = "showTableNames";
    public static final String PREF_KEY_PRINT_QUEUE = "printQueue";
    public static final String PRED_KEY_SELECTED_MENU = "selectedMenu";
    public static final String PREF_KEY_USER_LIST = "userList";
    public static final String KEY_SESSION_ID = "session_id";

    public static final String EXTRA_SESSION_ID = "uk.co.thedistance.epicuri.SESSION_ID";
    public static final String EXTRA_SESSION = "uk.co.thedistance.epicuri.SESSION";
    public static final String EXTRA_DINER = "uk.co.thedistance.epicuri.DINER";
    public static final String EXTRA_CUSTOMER = "uk.co.thedistance.epicuri.CUSTOMER";
    public static final String EXTRA_ACTION_ID = "uk.co.thedistance.epicuri.ACTION_ID";
    public static final String EXTRA_MENU_ID = "uk.co.thedistance.epicuri.MENU_ID";
    public static final String EXTRA_CATEGORY_ID = "uk.co.thedistance.epicuri.CATEGORY_ID";
    public static final String EXTRA_CATEGORY = "uk.co.thedistance.epicuri.MENU_CATEGORY";
    public static final String EXTRA_GROUP_ID = "uk.co.thedistance.epicuri.GROUP_ID";
    public static final String EXTRA_GROUP  = "uk.co.thedistance.epicuri.MENU_GROUP";
    public static final String EXTRA_MENUITEM_ID = "uk.co.thedistance.epicuri.MENUITEM_ID";
    public static final String EXTRA_MENUITEM_IDS = "uk.co.thedistance.epicuri.MENUITEM_IDS";
    public static final String EXTRA_ORDERITEM = "uk.co.thedistance.epicuri.ORDERITEM";
    public static final String EXTRA_MENUITEM = "uk.co.thedistance.epicuri.MENUITEM";
    public static final String EXTRA_PARTY = "uk.co.thedistance.epicuri.PARTY";
    public static final String EXTRA_RESERVATION = "uk.co.thedistance.epicuri.RESERVATION";
    public static final String EXTRA_RESERVATION_ID = "uk.co.thedistance.epicuri.RESERVATION_ID";
    public static final String EXTRA_FLOOR = "uk.co.thedistance.epicuri.FLOOR";
    public static final String EXTRA_LOGIN = "uk.co.thedistance.epicuri.LOGIN";
    public static final String EXTRA_CHECKIN = "uk.co.thedistance.epicuri.CHECKIN";
    public static final String EXTRA_MODIFIER_GROUPS = "uk.co.epicuri.MODIFIER_GROUPS";
    public static final String EXTRA_MODIFIER_GROUP = "uk.co.epicuri.MODIFIER_GROUP";
    public static final String EXTRA_COURSES = "uk.co.epicuri.COURSES";
    public static final String EXTRA_ENFORCE_LIMITS = "uk.co.epicuri.ENFORCE_LIMITS";
    public static final String EXTRA_TABLE_IDS= "uk.co.thedistance.epicuri.TABLE_IDS";

    public static final String ACTION_SESSION_CHANGED = "uk.co.thedistance.epicuri.SESSION_CHANGED";
    public static final String ACTION_LAUNCH_MENU = "uk.co.thedistance.epicuri.LAUNCH_MENU";
    public static final String ACTION_SUBMIT_PENDING_ITEMS = "uk.co.thedistance.epicuri.SUBMIT_PENDING_ITEMS";

    public static final String EXTRA_AUTO_GROUP_ID = "uk.co.epicuri.AUTO_GROUP_ID";

    public static final String EXTRA_MODIFIER_GROUP_ID = "uk.co.epicuri.waiter.MODIFIER_GROUP_ID";
    public static final String EXTRA_MODIFIER_VALUE = "uk.co.epicuri.waiter.MODIFIER_VALUE";
    public static final String EXTRA_VAT_RATES = "uk.co.epicuri.waiter.VAT_RATES";

    public static final String ARG_EARLIEST_DATE = "earliestDate";
    public static final String ARG_CAN_CASH_UP = "canCashUp";

    public static final int LOADER_MODIFIER_GROUPS = 1;
    public static final String FRAGMENT_NEW_MODIFIER_GROUP = "modifierGroup";

    public static final String FRAGMENT_DINER_DETAILS = "DinerDetails";
    public static final String FRAGMENT_CUSTOMER_CHOOSER = "CustomerChooser";

    public static final String EXTRA_IS_BIRTHDAY = "uk.co.epicuri.IS_BIRTHDAY";

    public static final String EXTRA_TYPE = "type";
    public static final int LOADER_LIST = 1;

    public static final int LOADER_CASH_UPS = 1;
    public static final int LOADER_PRINTERS = 18;
    public static final int LOADER_PRINTER_REDIRECTS = 3;

    public static final String FRAGMENT_CASH_UP = "cashUp";

    public static final int LOADER_MODIFIER = 1;
    public static final int LOADER_PRINTER = 2;
    public static final int LOADER_VAT = 3;

    public static final int API_VERSION_6 = 6;

	public static final int LOADER_SESSIONS = 1;
	public static final int LOADER_EVENTS = 2;
	public static final int LOADER_PARTIES = 3;
	public static final int LOADER_FLOORS = 4;
	public static final int LOADER_SERVICES = 5;
	public static final int LOADER_CHECKINS = 6;
	public static final int LOADER_RESTAURANT = 7;
	public static final int LOADER_RESERVATIONS = 8;
	public static final int LOADER_TAKEAWAYS = 9;public static final int LOADER_PREFERENCES = 10;

    public static final String EXTRA_START_PRINT_QUEUE = "uk.co.epicuri.waiter.START_PRINT_QUEUE";
    public static final String EXTRA_TAB = "uk.co.epicuri.waiter.TAB";
    public static final String EXTRA_RESEAT_SESSION = "uk.co.epicuri.waiter.MOVE_SESSION";

    public static final String KEY_PREF_ENVIRONMENT = "apiEnvironment";
    public static final String KEY_PREF_URL_PREFIX = "apiPrefix";
    public static final String KEY_PREF_KITCHEN_VIEW_PREFIX = "kitchenViewPrefix";

    public static final String PREF_FILE = "backgroundCacheFilenames";

    public static final String EXTRA_LAYOUT_ID = "uk.co.epicuri.waiter.LAYOUT_ID";
    public static final String EXTRA_CURRENT = "uk.co.epicuri.waiter.CURRENT";
    public static final String EXTRA_LAYOUT_NAMES = "uk.co.epicuri.waiter.LAYOUT_NAMES";
    public static final String EXTRA_LAYOUT = "uk.co.epicuri.waiter.LAYOUT";
    public static final String FRAGMENT_NEW_TABLE = "newTable";
    public static final String TRANSIENT_LAYOUT_NAME = "TRANSIENT";
    public static final int LOADER_LAYOUT = 1;
    public static final int LOADER_SESSION = 2;

    public static final String FRAGMENT_NEW_PARTY = "newParty";

    public static final String EXTRA_TARGET_FRAGMENT = "target";
    public static final String EXTRA_BILL_PRINT = "billPrint";

    public static String autoselectPartyId = "-1"; //todo find usages, set default value to null

	public enum ActionType {
		ACTION_SEAT, ACTION_SERVICE, ACTION_ALERT, ACTION_OVERDUE;

		public static ActionType fromString(String code) {
			if ("SEAT".equals(code)) {
				return ACTION_SEAT;
			} else if ("SERVICE".equals(code)) {
				return ACTION_SERVICE;
			} else if ("ALERT".equals(code)) {
				return ACTION_ALERT;
			} else if ("OVERDUE".equals(code)) {
				return ACTION_OVERDUE;
			}
			throw new IllegalArgumentException(String.format(
					"Unknown action: %s", code));
		}
	}

	public static String TARGET = "waiter/action";

	public static final long ON_TIME_THRESHOLD = 60000;
	public static final long FEW_MINS_THRESHOLD = 300000;
	private static final long AN_HOUR = 3600000;
	public static CharSequence minsLate(long millisecondsLate){
		if(millisecondsLate > AN_HOUR ){
			float hoursLate = (float)millisecondsLate / AN_HOUR;
			hoursLate = Math.round(2 * hoursLate) / 2f;
			if(hoursLate == 1) return "About an hour late";
			else if(hoursLate == (int)hoursLate) return String.format(Locale.UK, "About %d hours late", (int)hoursLate);
			else return String.format(Locale.UK, "About %.1f hours late", hoursLate);
		}
		else if(millisecondsLate > FEW_MINS_THRESHOLD ) return String.format(Locale.UK, "%d mins late", millisecondsLate / 60000);
		else if(millisecondsLate > ON_TIME_THRESHOLD ) return "A few mins late";
		else if(millisecondsLate > -ON_TIME_THRESHOLD) return "On time";
		else if(millisecondsLate > -FEW_MINS_THRESHOLD ) return "A few mins early";
		else return String.format(Locale.UK, "%d mins early", Math.abs(millisecondsLate/60000));
	}


	public static final String IS_NOT_FIRST_TIME_LOGGED_IN = "isNotFirstTimeLoggedIn";

	public static final String CHANNEL_PRINT = "ch_print";
}
