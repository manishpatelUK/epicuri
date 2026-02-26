package uk.co.epicuri.waiter.webservice;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.TimeZone;

import uk.co.epicuri.waiter.BuildConfig;

public class SendDeviceInformationWebServiceCall implements WebServiceCall {
	private final String body;

	private static String versionName;
	private static String versionCode;

	public SendDeviceInformationWebServiceCall(Context context) {
		JSONObject bodyJSON = new JSONObject();
		try {
			bodyJSON.put("TimezoneSetting", TimeZone.getDefault().getDisplayName());
			bodyJSON.put("LanguageSetting", Locale.getDefault().getDisplayLanguage());

			try {
				getPackageInfo(context);
				bodyJSON.put("WaiterAppVersion", versionName);
				bodyJSON.put("WaiterAppVersionId", versionCode);
			} catch (PackageManager.NameNotFoundException e) {
			}
			bodyJSON.put("Device", Build.BOARD);
			bodyJSON.put("OS", Build.VERSION.RELEASE);
			bodyJSON.put("IsAutoUpdating", 0);
			bodyJSON.put("Note", null);
		} catch (JSONException e) {
			throw new RuntimeException("really shouldn't happen");
		}
		body = bodyJSON.toString();
	}

	private void getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
		if(versionName == null) {
			try {
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				versionName = pInfo.versionName;
			} catch (Exception e) {
				versionName = BuildConfig.VERSION_NAME;
			}
		}
		if(versionCode == null) {
			try {
				PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
				versionCode = String.valueOf(pInfo.versionCode);
			} catch (Exception e) {
				versionCode = String.valueOf(BuildConfig.VERSION_CODE);
			}
		}

	}

	@Override
	public String getMethod() {
		return "POST";
	}

	@Override
	public String getPath() {
		return "/device";
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public boolean requiresToken() {
		return true;
	}

	@Override
	public Uri[] getUrisToRefresh() {
		return null;
	}

}
