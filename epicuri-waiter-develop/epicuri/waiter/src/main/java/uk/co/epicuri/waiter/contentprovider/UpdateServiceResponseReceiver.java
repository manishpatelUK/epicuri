package uk.co.epicuri.waiter.contentprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

/**
 * Created by Home on 7/20/16.
 */
public class UpdateServiceResponseReceiver extends BroadcastReceiver {

    private Context context;
    private ProgressBar progressSpinner;
    private View errorView;

    public UpdateServiceResponseReceiver(Context context, ProgressBar progressSpinner, View errorView) {
        this.context = context;
        this.progressSpinner = progressSpinner;
        this.errorView = errorView;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction().equals(UpdateService.ACTION_REFRESH_STARTED)){
            if(null != progressSpinner) progressSpinner.setVisibility(View.VISIBLE);
//                setProgressBarIndeterminateVisibility(true);
        } else if(intent.getAction().equals(UpdateService.ACTION_REFRESH_STOPPED)){
            if(null != progressSpinner) progressSpinner.setVisibility(View.GONE);
//				setProgressBarIndeterminateVisibility(false);
        }

        if(intent.getAction().equals(WebServiceTask.ACTION_UPDATE_REQUIRED)){
       //     View errorView = context.findViewById(R.id.upgradeRequired);
            if(null != errorView){
                errorView.setVisibility(View.VISIBLE);
                errorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent upgradeIntent = new Intent(Intent.ACTION_VIEW);
                        upgradeIntent.setData(Uri.parse("market://details?id=uk.co.epicuri.waiter"));
                        upgradeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(upgradeIntent);
                    }
                });
            }
        }
    }
}
