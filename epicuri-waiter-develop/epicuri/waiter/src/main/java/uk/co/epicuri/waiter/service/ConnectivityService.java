package uk.co.epicuri.waiter.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.MalformedURLException;
import java.net.URL;

import uk.co.epicuri.waiter.service.interfaces.IConnectionTaskListener;
import uk.co.epicuri.waiter.service.interfaces.IEpicuriConnectionListener;
import uk.co.epicuri.waiter.webservice.GetPingWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class ConnectivityService {
    public WifiInfo determineWiFiConnection(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager == null) {
            return null;
        }
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            return null;
        }

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null) {
            return null;
        } else {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo;
        }
    }

    public void determineEpicuriConnection(Context context, final IEpicuriConnectionListener listener) {
        WebServiceTask webServiceTask = new WebServiceTask(context, new GetPingWebServiceCall(), false);
        final long now = System.currentTimeMillis();
        webServiceTask.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                listener.onEpicuriConnectionMade(System.currentTimeMillis()-now);
            }
        });
        webServiceTask.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                listener.onEpicuriConnectionMade(null);
            }
        });
        webServiceTask.execute();
    }

    public void determineInternetSpeedConnection(IConnectionTaskListener listener) {
        makeConnection(listener, "https://www.epicuri.co.uk/assets/test.file?avoidCache="+System.currentTimeMillis());
    }

    private void makeConnection(IConnectionTaskListener listener, String urlString) {
        try {
            URL url = new URL(urlString);
            new DownloadTask(listener).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
