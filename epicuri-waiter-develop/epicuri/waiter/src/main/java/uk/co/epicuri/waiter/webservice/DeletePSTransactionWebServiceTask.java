package uk.co.epicuri.waiter.webservice;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;

public class DeletePSTransactionWebServiceTask extends AsyncTask<Context, Void, Boolean> {

    public interface IDeleteTransactionListener {
        void onDeleteTransactionFailure(String errorMessage);
        void onDeleteTransactionSuccess();
    }

    Context context;
    String response;
    String location;
    IDeleteTransactionListener listener;

    public DeletePSTransactionWebServiceTask(Context context, String location) {
        this.context = context;
        this.location = location;
    }

    public void setListener(IDeleteTransactionListener listener) {
        this.listener = listener;
    }

    @Override protected Boolean doInBackground(Context... params) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            try {
                EpicuriRestaurant restaurant = LocalSettings.getInstance(context)
                        .getCachedRestaurant();

                URL url = new URL(location);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty("Accept", "application/connect.v1+json");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Authorization", PaymentSenseUtil.getBasicAuthentication(restaurant.getName(), restaurant.getPaymentsenseKey()));
                urlConnection.setReadTimeout(20000);
                urlConnection.setConnectTimeout(10000);

                Log.d("DeletePs","Send request");

                urlConnection.connect();

                switch (urlConnection.getResponseCode()) {
                    case 204:
                        response = PaymentSenseUtil.streamToString(urlConnection.getInputStream());
                        return true;
//                    case 422:
//                        response = "Transaction could not be cancelled.";
//                        break;
//                    case 404:
//                        response = "The requestId could not be found";
//                        break;
                    default:
                        response = PaymentSenseUtil.streamToString(urlConnection.getErrorStream());
                        response = PaymentSenseUtil.getErrorFromResponse(response);
                        if (response == null || response.isEmpty())
                            response = "An error occured while canceling transaction.";
                }

            } catch (MalformedURLException e1) {
                return false;

            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
        if (!aBoolean) listener.onDeleteTransactionFailure(response);

        if (aBoolean) listener.onDeleteTransactionSuccess();

        super.onPostExecute(aBoolean);
    }
}
