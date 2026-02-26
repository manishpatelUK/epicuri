package uk.co.epicuri.waiter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import uk.co.epicuri.waiter.model.EpicuriLogin;

public class LoginSessionService extends Service {
	private static final int TIMEOUT = 600000;
	
	public static final String ACTION_SHOW_LOCK_SCREEN = "uk.co.thedistance.epicuri.LOCK_SCREEN";

    private final IBinder mBinder = new LocalBinder();
    
    /**
     * default to timed out
     */
    boolean locked = true;

    public class LocalBinder extends Binder {
    	public LoginSessionService getService(){
    		return LoginSessionService.this;
    	}
    }
    
	@Override
	public void onCreate() {
		{
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
			registerReceiver(mMessageReceiver, filter);
		}
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("LoginSessionService","started session service");
		
		resetTimer();
		return mBinder;
	}

	private Handler handler = new Handler();

	public void resetTimer(){
		handler.removeCallbacks(timeoutAction);
		handler.postDelayed(timeoutAction, TIMEOUT);
		startService(new Intent(this, LoginSessionService.class));
		Log.d("LoginSessionService","started timer");
	}
	
	public void clearLogin(){
		cachedLogin = null;
		handler.removeCallbacks(timeoutAction);
		stopSelf();
	}
	
	/**
	 * this is necessary once the timer has elapsed.  Call if the user correctly enters the PIN
	 */
    public void unlock(){
    	locked = false;
    	resetTimer();
    }
    
    public boolean isLocked(){
    	return locked;
    }
    
    public boolean isLoggedIn(){
    	return null != getLoggedInUser();
    }
    
    public void lock(){
		locked= true;
		
		LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
		bm.sendBroadcast(new Intent(LoginSessionService.ACTION_SHOW_LOCK_SCREEN));
		stopSelf();
    }
	
	private Runnable timeoutAction = new Runnable() {
		
		@Override
		public void run() {
	    	Log.d("LoginSessionService","timer expired");
			lock();
		}
	};
	
	private EpicuriLogin cachedLogin = null;
	public EpicuriLogin getLoggedInUser(){
		if(null == cachedLogin){
			cachedLogin = EpicuriLogin.fromPreferences(this);
		}
		return cachedLogin;
	}

	private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				Log.d("LoginSessionService","Screen off -> lock");
				locked = true;
				return;
			}
		}
	};

}
