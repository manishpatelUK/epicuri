package uk.co.epicuri.waiter.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class EpicuriContent extends ContentProvider {

    public static final String AUTHORITY = "uk.co.epicuri.waiter.contentprovider.EpicuriContent";

	/**
	 * all menus are at api/Menu  specific ones at api/Menu/{id}
	 */
	public static final Uri MENU_URI = Uri.parse("content://" + AUTHORITY  + "/Menu");
	public static final Uri MENUITEM_URI = Uri.parse("content://" + AUTHORITY  + "/MenuItem");
	public static final Uri MENUMODIFIER_URI = Uri.parse("content://" + AUTHORITY  + "/ModifierGroup");
	public static final Uri PREFERENCES_URI = Uri.parse("content://" + AUTHORITY  + "/preferences");

	/**
	 * api/Session Gets a list of all active (started and not closed) sessions
	 * api/Session/{id} Gets a order history, events and diners for a session
	 */
	public static final Uri SESSION_URI = Uri.parse("content://" + AUTHORITY  + "/Session");
	public static final Uri CLOSED_SESSION_URI = Uri.parse("content://" + AUTHORITY + "/Session/NotInCashup");

	/**
	 * api/Event Get a list of all events (schedule items, notifications and adhoc events)
	 */
	public static final Uri EVENT_URI = Uri.parse("content://" + AUTHORITY  + "/Event");

	/**
	 * api/Reservation?fromTime={unix_timestamp}[&toTime={unix_timestamp}]
	 * Get a list of all reservations between times, default for totime is fromtime+3 months
	 */
	public static final Uri RESERVATIONS_URI = Uri.parse("content://" + AUTHORITY  + "/Reservation");
	/**
	 * Get a list of all parties
	 */
	public static final Uri PARTIES_URI = Uri.parse("content://" + AUTHORITY  + "/Party");
	/**
	 * api/Floor Get a list of all events (schedule items, notifications and adhoc events)
	 * api/Floor/{id} Gets a list of floors in the restaurant including the active layout for the floor
	 */
	public static final Uri FLOOR_URI = Uri.parse("content://" + AUTHORITY  + "/Floor");
	/**
	 * api/Layout/{id} Gets a list of layouts for a floor
	 */
	public static final Uri LAYOUT_URI = Uri.parse("content://" + AUTHORITY  + "/Layout");

	public static final Uri COURSE_URI = Uri.parse("content://" + AUTHORITY + "/Course");

	public static final Uri CHECKIN_URI = Uri.parse("content://" + AUTHORITY + "/Checkin");
	public static final Uri SERVICE_URI = Uri.parse("content://" + AUTHORITY + "/Service/all");
	public static final Uri TABLE_URI = Uri.parse("content://" + AUTHORITY  + "/Table");
	public static final Uri LOGIN_URI = Uri.parse("content://" + AUTHORITY  + "/Staff");
	public static final Uri PERMSISSIONS_URI = Uri.parse("content://" + AUTHORITY  + "/Staff/permissions");
	public static final Uri PRINTER_URI = Uri.parse("content://" + AUTHORITY  + "/Printer");
	public static final Uri PRINTER_REDIRECT_URI = Uri.parse("content://" + AUTHORITY  + "/Printer/RedirectedPrinters");

	public static final Uri SETTING_URI = Uri.parse("content://" + AUTHORITY  + "/Setting");
	public static final Uri TAKEAWAY_URI = Uri.parse("content://" + AUTHORITY  + "/TakeAway");
	public static final Uri RESTAURANT_URI = Uri.parse("content://" + AUTHORITY  + "/Restaurant");
	public static final Uri VATRATES_URI = Uri.parse("content://" + AUTHORITY  + "/TaxType");

	public static final Uri CASHUP_URI = Uri.parse("content://" + AUTHORITY + "/CashUp");

	public static final String CACHE_FILE = "mycache";

    @Override public boolean onCreate() {
        return true;
    }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable @Override public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
