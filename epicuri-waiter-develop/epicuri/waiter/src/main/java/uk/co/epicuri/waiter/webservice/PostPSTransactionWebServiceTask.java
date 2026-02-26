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
import java.net.UnknownHostException;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.PaymentsenseTerminal;

public class PostPSTransactionWebServiceTask extends AsyncTask<Context, Void, Boolean> {

    public interface IPostTransactionListener {
        void onPostTransactionSuccess(String requestLocation);
        void onPostTransactionFailure(String errorMessage);
    }

    Context context;
    PaymentsenseTerminal terminal;
    int amount;
    String response;
    IPostTransactionListener listener;
    boolean isRefund;
    private static final String TERMINALS = "/pac/terminals/";
    private static final String TRANSACTIONS = "/transactions";

    public PostPSTransactionWebServiceTask(Context context, PaymentsenseTerminal terminal, double
            amount, boolean isRefund) {
        this.context = context;
        this.terminal = terminal;
        this.amount = (int)Math.rint(amount * 100);
        this.isRefund = isRefund;
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

                URL url = new URL(restaurant.getPaymentsenseHost() + TERMINALS + terminal
                        .getTpi() + TRANSACTIONS);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Accept", "application/connect.v1+json");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Authorization", PaymentSenseUtil.getBasicAuthentication(restaurant.getName(), restaurant.getPaymentsenseKey()));
                urlConnection.setReadTimeout(20000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setUseCaches(false);
                urlConnection.setDoOutput(true);

                String body = createRequestBody(amount, restaurant.getCurrency().getCode());
                urlConnection.setFixedLengthStreamingMode(body.getBytes().length);

                PrintWriter pw = new PrintWriter(new BufferedOutputStream(urlConnection.getOutputStream()));
                pw.write(body);
                pw.flush();
                pw.close();

                urlConnection.connect();

                if(urlConnection.getResponseCode() == 201) {
                    try {
                        response = PaymentSenseUtil.streamToString(urlConnection.getInputStream());
                        JSONObject o = new JSONObject(response);
                        response = o.getString("location");
                    } catch (Exception e) { /* Dont handle; */}
                    return true;
                } else if(urlConnection.getResponseCode() == 401) {
                    response = context.getString(R.string.incorrect_API_KEY_configuration_for_PS);
                } else if(urlConnection.getResponseCode() > 401) {
                    response = PaymentSenseUtil.streamToString(urlConnection.getErrorStream());
                    response = PaymentSenseUtil.getErrorFromResponse(response);
                    if (response == null || response.isEmpty()) {
                        response = context.getString(R.string.internal_terminal_error);
                    }
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch(UnknownHostException e){
                response = context.getString(R.string.incorrect_Host_configuration_for_PS);
                return false;
            } catch (IOException e) {
                response = context.getString(R.string.incorrect_Host_configuration_for_PS);
                return false;
            }
        }

        return false;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
        if (listener == null) return;

        if (aBoolean) listener.onPostTransactionSuccess(response);

        if (!aBoolean) listener.onPostTransactionFailure(response);

        super.onPostExecute(aBoolean);
    }

    private String createRequestBody(int amount, String currency) {
        JSONObject o = new JSONObject();

        try {
            o.put("transactionType", isRefund ? "REFUND" : "SALE");
            o.put("amount", amount);
            o.put("currency", currency);
        } catch (JSONException e){
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }

        return o.toString();
    }
}
