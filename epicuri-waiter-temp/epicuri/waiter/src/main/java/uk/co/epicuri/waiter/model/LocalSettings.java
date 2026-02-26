package uk.co.epicuri.waiter.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class LocalSettings {
	private static final String CACHE = "cache";
	private static final String RESTAURANT_KEY = "restaurant";
	private static final String PREFERENCES_KEY = "preferences";
    private static final String PRINTERS_KEY = "printers";
	private static final String QO_MENU_KEY = "qoMenuId";
	public static final String QO_BILL_PRINT = "qoBillPrint";
	public static final String QO_ORDER_PRINT = "qoOrderPrint";
	public static final String QO_ALPHABETIC_ORDERING = "qoItemAlphaOrder";
	private static final String SCREEN_SIZE = "screenSize";

	private static EpicuriRestaurant cachedRestaurant = null;
	private List<EpicuriMenu.Printer> cachedPrinters;

	private static String qoMenuId = null;
	private final WeakReference<Context> context;
	private Preferences cachedPreferences = null;

	public static CurrencyUnit getCurrencyUnit(){
		if(null == cachedRestaurant) return CurrencyUnit.GBP; // default to GBP if nothing loaded yet
		return cachedRestaurant.getCurrency();
	}

	public static TimeZone getTimezone(){
		if(null == cachedRestaurant) return TimeZone.getDefault(); // default to device timezone if nothing loaded yet
		return cachedRestaurant.getTimezone();
	}

	public static EpicuriRestaurant getStaticCachedRestaurant() {
		return cachedRestaurant;
	}

	private static final MoneyFormatter sMoneyFormatterWithSymbol = new MoneyFormatterBuilder().appendCurrencySymbolLocalized().appendAmount().toFormatter(Locale.UK);
	private static final MoneyFormatter sMoneyFormatter = new MoneyFormatterBuilder().appendAmount().toFormatter(Locale.UK);
	
	private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm", Locale.UK);
	private static final SimpleDateFormat sDateFormatWithDate = new SimpleDateFormat("HH:mm dd-MMM", Locale.UK);

	static {
		sDateFormat.setTimeZone(getTimezone());
		sDateFormatWithDate.setTimeZone(getTimezone());
	}

	/**
	 * Parse currency entered by the user
	 * @param moneyString User entered currency string (e.g. "10.00")
	 * @return A monetary amount in the restaurant currency, e.g. 10.00 USD
	 */
	public static Money parseCurrency(CharSequence moneyString){
	    if(moneyString.toString().isEmpty()){
	        moneyString = "0.00";
        }
		return Money.parse(getCurrencyUnit().getCurrencyCode() + " " + moneyString);
	}
	
	public static String formatMoneyAmount(Money amount, boolean withSymbol){
		if(withSymbol){
			return sMoneyFormatterWithSymbol.print(amount);
		} else {
			return sMoneyFormatter.print(amount);
		}
	}

	public static String formatMoneyAmount(double value, boolean withSymbol) {
		value = Math.round(100d * value) / 100d; // force round the value
		return formatMoneyAmount(Money.of(getCurrencyUnit(), value), withSymbol);
	}
	
	public static SimpleDateFormat getDateFormat(){
		return sDateFormat;
	}
	public static SimpleDateFormat getDateFormatWithDate(){
		return sDateFormatWithDate;
	}

	public static CharSequence niceFormat(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		Calendar now = Calendar.getInstance();
		if(now.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == c.get(Calendar.YEAR)){
			// same day
			return sDateFormat.format(c.getTime());
		} else {
			return sDateFormatWithDate.format(c.getTime());
		}
	}
	
	public static LocalSettings getInstance(Context context){
		return new LocalSettings(context);
	}
	private LocalSettings(Context context){
		this.context = new WeakReference<Context>(context);
	}

	public void cacheRestaurant(EpicuriRestaurant r){
		if (cachedRestaurant != null) r.setTerminals(cachedRestaurant.getTerminals());

		context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putString(RESTAURANT_KEY, r.toJson()).commit();
		cachedRestaurant = r;
		sDateFormat.setTimeZone(getTimezone());
		sDateFormatWithDate.setTimeZone(getTimezone());
	}

	public boolean isAllowed(WaiterAppFeature feature) {
        return feature == null || getCachedRestaurant() == null || getCachedRestaurant().getPermission(feature);
	}

	public EpicuriRestaurant getCachedRestaurant(){
		if(null == cachedRestaurant){
			String restaurantJson = context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getString(RESTAURANT_KEY, null);
			try {
				cachedRestaurant = new EpicuriRestaurant(new JSONObject(restaurantJson));
			} catch (JSONException e) {
				// cannot decode cached restaurant
				e.printStackTrace();
			} catch (NullPointerException nullException){
				//do nothing
			}
		}
		return cachedRestaurant;
	}

	public void cacheQuickOrderServiceMenuId(String menuId) {
		context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putString(QO_MENU_KEY, menuId).apply();
		qoMenuId = menuId;
	}

	public String getQuickOrderMenuId() {
		if(qoMenuId == null) {
			qoMenuId = context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getString(QO_MENU_KEY, null);
		}
		return qoMenuId;
	}

	public void cacheBillPrint(boolean billPrint){
        context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putBoolean(QO_BILL_PRINT, billPrint).apply();
    }

    public boolean isBillPrint(){
	    return context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getBoolean(QO_BILL_PRINT, true);
    }

	public boolean isOrderItemsAlphabeticallyInQO(){
		return context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getBoolean(QO_ALPHABETIC_ORDERING, false);
	}

	public void cacheOrderItemsAlphabeticallyInQO(boolean isOrdered){
		context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putBoolean(QO_ALPHABETIC_ORDERING, isOrdered).apply();
	}

    public void cacheOrderPrint(boolean orderPrint){
        context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putBoolean(QO_ORDER_PRINT, orderPrint).apply();
    }

    public boolean isOrderPrint() {
        return context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getBoolean(QO_ORDER_PRINT, true);
    }

    public void cachePreferences(Preferences preferences) {
		context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putString(PREFERENCES_KEY, preferences.toJson()).apply();
		cachedPreferences = preferences;
	}

	public Preferences getCachedPreferences(){
	    if (cachedPreferences == null){
	        String preferencesJson = context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getString(PREFERENCES_KEY, null);
	        try {
	            cachedPreferences = new Preferences(new JSONObject(preferencesJson));
            }catch (Exception e){
	            //cannot decode or NPE
                e.printStackTrace();
            }
        }
        return cachedPreferences;
    }

    public List<EpicuriMenu.Printer> getCachedPrinters() {
		String printersJson = context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).getString(PRINTERS_KEY,null);
		if(cachedPrinters == null && printersJson != null) {
	        try {
	            cachedPrinters = new ArrayList<>();
	            JSONArray array = new JSONArray(printersJson);
	            for(int i = 0; i < array.length(); i++) {
	                cachedPrinters.add(new EpicuriMenu.Printer(new JSONObject(array.get(i).toString())));
                }
            } catch (Exception e) {
                //cannot decode or NPE
	            e.printStackTrace();
	            cachedPrinters = new ArrayList<>();
            }
        }

        return cachedPrinters;
    }

    public void cachePrinters(List<EpicuriMenu.Printer> cachedPrinters) {
        JSONArray array = new JSONArray();
        for(EpicuriMenu.Printer printer : cachedPrinters) {
            array.put(printer.toJson());
        }

	    context.get().getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit().putString(PRINTERS_KEY, array.toString()).apply();
        this.cachedPrinters = cachedPrinters;
    }

    public float getQOFontSize() {
		Context context = this.context.get();
		double screenSize = 7;
		EpicuriRestaurant cachedRestaurant = getCachedRestaurant();
		if(context instanceof Activity) {
			SharedPreferences sharedPreferences = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE);
			if(sharedPreferences.contains(SCREEN_SIZE)) {
				screenSize = sharedPreferences.getFloat(SCREEN_SIZE, 7);
			} else {
				DisplayMetrics dm = new DisplayMetrics();
				((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
				double x = Math.pow(dm.widthPixels/dm.xdpi,2);
				double y = Math.pow(dm.heightPixels/dm.ydpi,2);
				screenSize = Math.sqrt(x+y);
				screenSize = (double)Math.round(screenSize * 10) / 10;
				sharedPreferences.edit().putFloat(SCREEN_SIZE, (float)screenSize).apply();
			}

			if(screenSize >= cachedRestaurant.getQOScreenThresholdInches()) {
				return cachedRestaurant.getQOFontUpscale();
			} else {
				return 1f;
			}
		}

		return 1f;
	}
}
