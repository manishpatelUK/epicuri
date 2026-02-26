package uk.co.epicuri.waiter.ui;

import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.service.ConnectivityService;
import uk.co.epicuri.waiter.service.interfaces.IConnectionTaskListener;
import uk.co.epicuri.waiter.service.interfaces.IEpicuriConnectionListener;

public class NetworkSupportFragment extends Fragment implements IConnectionTaskListener, IEpicuriConnectionListener {
    @InjectView(R.id.wifiGateway)
    TextView wifiGateway;

    @InjectView(R.id.connectionSpeed)
    TextView connectionSpeed;

    @InjectView(R.id.internetSpeed)
    TextView internetSpeed;

    @InjectView(R.id.networkState)
    TextView networkState;

    @InjectView(R.id.epicuriConnection)
    TextView epicuriConnection;

    private long lastCall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.support_network_diagnostic, container, false);
        ButterKnife.inject(this, view);
        trigger();
        return view;
    }

    @OnClick(R.id.networkRefreshButton)
    public void trigger() {
        if(System.currentTimeMillis() - lastCall < 5000) {
            return;
        }

        lastCall = System.currentTimeMillis();

        ConnectivityService connectivityService = new ConnectivityService();
        connectivityService.determineInternetSpeedConnection(this);
        connectivityService.determineEpicuriConnection(getContext(), this);

        WifiInfo wifiInfo = connectivityService.determineWiFiConnection(getContext());
        if(wifiInfo == null) {
            setValues(R.string.network_error, wifiGateway, connectionSpeed, networkState);
        } else {
            wifiGateway.setText(wifiInfo.getSSID());
            networkState.setText(wifiInfo.getSupplicantState().toString());
            connectionSpeed.setText(String.format(Locale.getDefault(), "%d Mb/s", wifiInfo.getLinkSpeed()));
        }
    }

    @Override
    public void onDownloadTaskFinished(Long timeTaken) {
        if(timeTaken == null) {
            internetSpeed.setText(R.string.network_error);
        } else {
            double speed = 1000D / timeTaken;
            internetSpeed.setText(String.format(Locale.getDefault(), "%.2f",speed) + " MB/s");
        }
    }

    private void setValues(int value, TextView... views) {
        for(TextView view : views) {
            view.setText(value);
        }
    }

    @Override
    public void onEpicuriConnectionMade(Long time) {
        if(getActivity() == null) {
            return;
        }

        if(time == null) {
            epicuriConnection.setText("ERROR");
            epicuriConnection.setTextColor(getResources().getColor(R.color.red));
        } else {
            //this is a bit of hack; can't be bothered to refactor WebServiceTask. I know it takes approx 300ms for the async call to make it back here
            //so I'm fudging it.
            time = time - 300;
            if (time <= 100) {
                epicuriConnection.setText("VERY FAST");
                epicuriConnection.setTextColor(getResources().getColor(R.color.green));
            } else if (time <= 250) {
                epicuriConnection.setText("FAST");
                epicuriConnection.setTextColor(getResources().getColor(R.color.green));
            } else if (time <= 350) {
                epicuriConnection.setText("OK");
                epicuriConnection.setTextColor(getResources().getColor(R.color.green));
            } else if (time <= 650) {
                epicuriConnection.setText("SLOW");
                epicuriConnection.setTextColor(getResources().getColor(R.color.orange));
            } else {
                epicuriConnection.setText("VERY SLOW");
                epicuriConnection.setTextColor(getResources().getColor(R.color.darkOrange));
            }
        }
    }
}
