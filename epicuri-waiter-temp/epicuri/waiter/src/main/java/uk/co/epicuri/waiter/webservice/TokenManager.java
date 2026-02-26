package uk.co.epicuri.waiter.webservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import uk.co.epicuri.waiter.printing.PrintQueueService;
import uk.co.epicuri.waiter.printing.PrintQueueServiceState;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.ui.LoginActivity;

public class TokenManager {
	public static class NotLoggedInException extends Exception{ }
	private static String token = null;

	public static String getTokenKey(Context context) throws NotLoggedInException{
		if(null != token){
			Log.v("TokenManager","Returned cached token " + token);
			return token;
		}
		SharedPreferences sp = context.getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		token = sp.getString(GlobalSettings.PREF_KEY_TOKEN, null);
		if(null == token){
			newTokenAction(context);
			Log.v("TokenManager","No token retrieved");
			throw new NotLoggedInException();
		}
		Log.v("TokenManager","Returned new token " + token);
		return token;
	}
	
	public static String getToken(Context context) throws NotLoggedInException{
		String token = getTokenKey(context);
		if(null == token) return null;
		return "Basic " + token;
	}
	
	public static boolean checkToken(Context context){
		try {
			getTokenKey(context);
			return true;
		} catch (NotLoggedInException e) {
			return false;
		}
	}
	
	public static void newTokenAction(Context context){
		// no token, kick 'em out
		Intent i = new Intent(context, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
	
	public static void logout(Context context){
		token = null;
		context.getSharedPreferences(EpicuriContent.CACHE_FILE, Context.MODE_PRIVATE).edit().clear().commit();
		
		SharedPreferences sp = context.getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
		
		sp.edit().clear().commit();
		newTokenAction(context);
        if (PrintQueueService.mBound && PrintQueueService.mPrintQueue != null && PrintQueueService.mPrintQueue.isRunning()) {
            PrintQueueService.mPrintQueue.stopProcessingPrintQueue(true);
        }
    }
}
