package uk.co.epicuri.waiter.ui.menueditor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.service.LoggerAsyncTask;
import uk.co.epicuri.waiter.service.interfaces.ILoggingListener;
import uk.co.epicuri.waiter.webservice.PostLogFilesWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class AdvancedSupportFragment extends Fragment implements ILoggingListener {
    private volatile boolean currentlyCollating = false;

    @InjectView(R.id.sendLogButton)
    Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.support_advanced, container, false);
        ButterKnife.inject(this, view);
        trigger();
        return view;
    }

    @OnClick(R.id.sendLogButton)
    public void onSendLogPressed() {
        if(currentlyCollating) {
            Toast.makeText(getContext(), "Log collation is still in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), "Sending log...", Toast.LENGTH_SHORT).show();
        new LoggerAsyncTask(this).execute();
    }

    public void trigger() {

    }

    @Override
    public void onLoggingStart() {
        currentlyCollating = true;
        button.setEnabled(false);
    }

    @Override
    public void onLoggingComplete(List<String> logs) {
        currentlyCollating = false;
        button.setEnabled(true);

        //send to server
        WebServiceTask task = new WebServiceTask(getContext(), new PostLogFilesWebServiceCall(logs), true);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(getContext(), "Logs sent to Epicuri Support", Toast.LENGTH_SHORT).show();
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Toast.makeText(getContext(), "Could not send logs at this time: " + response, Toast.LENGTH_SHORT).show();
            }
        });
        task.setIndicatorText("Getting log data");
        task.execute();
    }
}
