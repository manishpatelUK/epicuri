package uk.co.epicuri.waiter.webservice;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Created by manish on 23/10/2017.
 */

public class PaymentSenseUtil {

    private static final String MESSAGE_TAG = "messages";

    public static String getBasicAuthentication(String userName, String key) {
        String plainCreds = userName + ":" + key;
        byte[] plainCredsBytes = plainCreds.getBytes();
        String base64Creds = Base64.encodeToString(plainCredsBytes, Base64.NO_WRAP);
        return "Basic " + base64Creds;
    }

    public static String getErrorFromResponse(String response) {
        JSONObject messageObject = null;
        StringBuilder errorMessage = new StringBuilder();

        try {
            JSONObject responseObject = new JSONObject(response);

            if (!responseObject.has(MESSAGE_TAG)) return null;

            messageObject = responseObject.getJSONObject(MESSAGE_TAG);

            Iterator it = messageObject.keys();
            while (it.hasNext()) {
                String key = (String)it.next();
                JSONArray array = messageObject.getJSONArray(key);
                for (int i = 0; i < array.length(); ++i)
                    errorMessage.append(array.get(i)).append("\n");
            }

        } catch (Exception e) {
            return errorMessage.toString();
        }

        return errorMessage.toString();
    }

    public static String streamToString(InputStream in){
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
