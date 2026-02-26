package uk.co.epicuri.waiter.webservice;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;

public class PostPSReportRequestTask extends AsyncTask<Context, Void, Boolean> {

    public interface IPostTransactionListener {
        void onPostTransactionSuccess();
        void onPostTransactionFailure(String errorMessage);
    }

    Context context;
    String response, location;
    IPostTransactionListener listener;
    private static final String REPORTS = "/reports";

    public PostPSReportRequestTask(Context context, String terminalLocation) {
        this.context = context;
        location = terminalLocation;
    }

    public void setListener(
            IPostTransactionListener listener) {
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

                URL url = new URL(location + REPORTS);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Accept", "application/connect.v1+json");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Authorization", PaymentSenseUtil.getBasicAuthentication(restaurant.getName(), restaurant.getPaymentsenseKey()));
                urlConnection.setReadTimeout(20000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                String body = createRequestBody();
                urlConnection.setFixedLengthStreamingMode(body.getBytes().length);

                PrintWriter pw = new PrintWriter(new BufferedOutputStream(urlConnection.getOutputStream()));
                pw.write(body);
                pw.flush();
                pw.close();

                urlConnection.connect();

                switch (urlConnection.getResponseCode()) {
                    case 201:
                        return true;
//                    case 404:
//                        response = context.getString(R.string.terminal_unavailable);
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
        if (listener == null) return;

        if (aBoolean) listener.onPostTransactionSuccess();

        if (!aBoolean) listener.onPostTransactionFailure(response);

        super.onPostExecute(aBoolean);
    }

    private static String createRequestBody() {
        JSONObject o = new JSONObject();

        try {
            o.put("reportType", "END_OF_DAY");
        } catch (JSONException e){
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }

        return o.toString();
    }
}
