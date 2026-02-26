package uk.co.epicuri.waiter.webservice;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.PaymentsenseTerminal;

public class GetTerminalsWebServiceTask extends AsyncTask<Context, Void, Boolean> {

    Context context;
    private final EpicuriRestaurant restaurant;
    private ITerminalsListener listener;
    private static final String GET_TERMINALS = "/pac/terminals";
    String response = "";

    public interface ITerminalsListener {
        void onTerminalsLoaded(String response, EpicuriRestaurant restaurant);
    }

    public GetTerminalsWebServiceTask(Context context, EpicuriRestaurant restaurant, ITerminalsListener listener) {
        this.context = context;
        this.restaurant = restaurant;
        this.listener = listener;
    }

    @Override protected Boolean doInBackground(Context... params) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(restaurant.getPaymentsenseHost() + GET_TERMINALS);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(2500);
                urlConnection.setRequestProperty("Accept", "application/connect.v1+json");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Authorization", PaymentSenseUtil.getBasicAuthentication(restaurant.getName(), restaurant.getPaymentsenseKey()));
                urlConnection.connect();

                response = urlConnection.getErrorStream() != null ? streamToString(urlConnection
                        .getErrorStream()) : streamToString(urlConnection.getInputStream());

                if(urlConnection.getResponseCode() == 401) {
                    response = context.getString(R.string.incorrect_API_KEY_configuration_for_PS);
                    return false;
                } else if(urlConnection.getResponseCode() > 401) {
                    response = context.getString(R.string.internal_terminal_error);
                    return false;
                }

                JSONObject jsonObject = new JSONObject(response);
                JSONArray objectArray = jsonObject.getJSONArray("terminals");
                List<PaymentsenseTerminal> terminals = new ArrayList<>(objectArray.length());

                for (int i = 0; i < objectArray.length(); ++i) {
                    PaymentsenseTerminal terminal = new PaymentsenseTerminal();
                    JSONObject object = (JSONObject)objectArray.get(i);
                    terminal.setLocation(object.getString("location"));
                    terminal.setTpi(object.getString("tpi"));

                    terminals.add(terminal);
                }

                restaurant.setTerminals(terminals);
            } catch (MalformedURLException e1) {
                return false;
            } catch(UnknownHostException e){
                response = context.getString(R.string.incorrect_Host_configuration_for_PS);
                return false;
            } catch (IOException e) {
                response = context.getString(R.string.incorrect_Host_configuration_for_PS);
                return false;
            } catch (JSONException e) {
                return false;
            }
        }

        return true;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
        if(!aBoolean) Log.d("Paymentsense_printers", "Error getting paymentsense terminals");

        if (listener != null){
            if(!aBoolean){
                listener.onTerminalsLoaded(response, restaurant);
            } else {
                listener.onTerminalsLoaded("", restaurant);
            }
        }

        super.onPostExecute(aBoolean);
    }

    private static String streamToString(InputStream in){
        InputStream bufferedin = new BufferedInputStream(in);
        String response = null;
        Scanner scanner = new Scanner(bufferedin, "UTF-8").useDelimiter("\\A");
        if (scanner.hasNext()){
            response = scanner.next();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
