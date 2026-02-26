package uk.co.epicuri.waiter.webservice;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.PaymentSenseResponse;

public class PollPaymentSenseTransactionWebServiceTask extends AsyncTask<Context, Void, Boolean> {

    public interface IPollPaymentSenseListener {
        void onSignatureVerificationNeeded();

        void onSignatureVerificationTimeout();

        void onTransactionSuccess(String paymentId, Integer amountGratuity);

        void onTransactionFailure(String errorMessage);

        void onTransactionStatusChanges(String status);

        void onSignatureVerificationError(String error, boolean canRetry);
    }

    private Context context;
    private String urlPath;
    PaymentSenseResponse response;
    private boolean signaturePrompted;
    private Boolean signatureAnswered;
    private boolean signatureTimeoutTriggered;
    private Boolean previousSignatureAnswer;
    private IPollPaymentSenseListener listener;
    private Activity activity;
    private String signatureError;

    private static final String TRANSACTION_FINISHED = "TRANSACTION_FINISHED";
    private static final String TRANSACTION_SUCCESS = "SUCCESSFUL";
    private static final String TRANSACTION_UNSUCCESS = "UNSUCCESSFUL";
    private static final String TRANSACTION_CANCELLED = "CANCELLED";
    private static final String TRANSACTION_DECLINED = "DECLINED";
    private static final String TRANSACTION_VOID = "VOID";
    private static final String TRANSACTION_TIMEOUT = "TIMED_OUT";
    private static final String SIGNATURE_VERIFICATION = "SIGNATURE_VERIFICATION";

    private static final String NOTIFICATION_SIGNATURE_TIME_OUT = "SIGNATURE_VERIFICATION_TIMEOUT";

    private static final String SIGNATURE = "/signature";

    public PollPaymentSenseTransactionWebServiceTask(Context context, String url) {
        this.context = context;
        this.urlPath = url;
    }

    public void setListener(IPollPaymentSenseListener listener, Activity activity) {
        this.listener = listener;
        this.activity = activity;
    }

    public void setSignatureVerified(Boolean signatureVerified) {
        signatureAnswered = signatureVerified;
    }

    public void skipSignatureVerification() {
        previousSignatureAnswer = signatureAnswered = null;
        signaturePrompted = false;
    }

    public void retrySignatureVerification() {
        if (previousSignatureAnswer != null) {
            signatureAnswered = previousSignatureAnswer;
        }

        previousSignatureAnswer = null;
    }

    public String getTransactionId() {
        return response.getTransactionId();
    }

    public Integer getAmountGratuity() {
        return response.getGratuity();
    }

    @Override protected Boolean doInBackground(Context... params) {
        boolean transactionFinished;

        response = new PaymentSenseResponse("", false); //init

        while(!response.getNotification().equals(TRANSACTION_FINISHED)) {
            if(response.isError()) {
                listener.onTransactionStatusChanges("Error, transaction not complete");
                break;
            }

            if(isCancelled()) {
                listener.onTransactionStatusChanges("Cancelled");
                break;
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) { /*don't handle*/}

            response = pollTransactionService();
            Log.d("PaymentSense", response.getNotification());
            if(response.getTransactionResult() != null && response.getTransactionResult().length() > 0) {
                listener.onTransactionStatusChanges(response.getTransactionResult());
            }

            if(signaturePrompted
                    && !signatureTimeoutTriggered
                    && signatureAnswered == null
                    && response.getNotifications().contains(NOTIFICATION_SIGNATURE_TIME_OUT)) {
                listener.onSignatureVerificationTimeout();
                signatureTimeoutTriggered = true;
                continue;
            }

            if(signaturePrompted) {
                listener.onTransactionStatusChanges("Signature Verification");
                verifySignature();
            } else {
                if (response.getNotification().equals(SIGNATURE_VERIFICATION) && listener != null && activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSignatureVerificationNeeded();
                        }
                    });

                    signaturePrompted = true;
                } else if (!response.getNotification().equals("")) {
                    final String notification = response.getNotification().replaceAll("_", " ");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCancelled()) {
                                listener.onTransactionStatusChanges(notification);
                            }
                        }
                    });
                }
            }
        }

        return true;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
        if (isCancelled() || listener == null) return;

        /*if (TRANSACTION_SUCCESS.equals(response.getTransactionResult())
                && signatureRequestTime > 0
                && (System.currentTimeMillis()-signatureRequestTime) > 80000) {
            listener.onSignatureVerificationTimeout();
            listener.onTransactionSuccess(response.getTransactionId(), response.getGratuity());
            return;
        }*/

        if (TRANSACTION_SUCCESS.equals(response.getTransactionResult())) {
            listener.onTransactionSuccess(response.getTransactionId(), response.getGratuity());
            return;
        }

        String messageToShow = response.getTransactionResult().replaceAll("_", " ");
        if(TRANSACTION_TIMEOUT.equals(response.getTransactionResult())) {
            listener.onTransactionFailure(messageToShow + ": " + context.getString(R.string.pdq_timeout));
        } else if(TRANSACTION_CANCELLED.equals(response.getTransactionResult())){
            listener.onTransactionFailure(messageToShow + ": " + context.getString(R.string.pdq_cancelled));
        } else {
            listener.onTransactionFailure(messageToShow + ": " + context.getString(R.string.no_payment_taken));
        }

        super.onPostExecute(aBoolean);
    }

    private PaymentSenseResponse pollTransactionService() {
        try {
            EpicuriRestaurant restaurant = LocalSettings.getInstance(context).getCachedRestaurant();
            HttpURLConnection urlConnection = poll(restaurant);
            if(urlConnection.getResponseCode() == 401) {
                String errorResponse = PaymentSenseUtil.streamToString(urlConnection.getErrorStream());
                errorResponse = PaymentSenseUtil.getErrorFromResponse(errorResponse);
                if (errorResponse == null || errorResponse.isEmpty()) {
                    errorResponse = context.getString(R.string.internal_terminal_error);   //todo shouldn't be reusing response like this
                }

                errorResponse += ": API Key in configuration is incorrect";
            } else if (urlConnection.getResponseCode() > 401) {
                String errorResponse = PaymentSenseUtil.streamToString(urlConnection.getErrorStream());
                errorResponse = PaymentSenseUtil.getErrorFromResponse(errorResponse);
                if (errorResponse == null || errorResponse.isEmpty()) {
                    errorResponse = context.getString(R.string.internal_terminal_error);   //todo shouldn't be reusing response like this
                }

                return new PaymentSenseResponse(errorResponse, true);
            }

            if (urlConnection.getErrorStream() == null) {
                JSONObject obj = new JSONObject(PaymentSenseUtil.streamToString
                        (urlConnection.getInputStream()));

                PaymentSenseResponse paymentSenseResponse = new PaymentSenseResponse();
                if (obj.has("notifications")) {
                    JSONArray array = obj.getJSONArray("notifications");
                    paymentSenseResponse.getNotifications().addAll(toList(array));
                    String recentNotification = array.getString(0);

                    paymentSenseResponse.setNotification(recentNotification);
                }

                if (obj.has("transactionResult")) {
                    paymentSenseResponse.setTransactionResult(obj.getString("transactionResult"));
                }
                if (obj.has("transactionId")) {
                    paymentSenseResponse.setTransactionId(obj.getString("transactionId"));
                }
                if (obj.has("amountGratuity")) {
                    paymentSenseResponse.setGratuity(obj.getInt("amountGratuity"));
                }
                if (obj.has("amountBase")) {
                    paymentSenseResponse.setAmountBase(obj.getInt("amountBase"));
                }
                return paymentSenseResponse;
            }

        } catch (Exception e) {
            // timeout is causing the exception
            return new PaymentSenseResponse(TRANSACTION_TIMEOUT, true);
        }

        return  new PaymentSenseResponse("Waiting", false); // shouldn't really get here
    }

    private List<String> toList(JSONArray array) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getString(i));
            } catch (JSONException e) {
                continue;
            }
        }
        return list;
    }

    private HttpURLConnection poll(EpicuriRestaurant restaurant) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(20000);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setRequestProperty("Accept", "application/connect.v1+json");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Authorization",
                PaymentSenseUtil.getBasicAuthentication(restaurant.getName(),
                        restaurant.getPaymentsenseKey()));
        urlConnection.connect();
        return urlConnection;
    }

    private boolean verifySignature() {
        if (signatureAnswered == null) return false;

        boolean signatureErrorFlag = false;
        boolean canRetry = true;

        try {
            EpicuriRestaurant restaurant = LocalSettings.getInstance(context)
                    .getCachedRestaurant();

            URL url = new URL(urlPath + SIGNATURE);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setReadTimeout(20000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestProperty("Accept", "application/connect.v1+json");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization",
                    PaymentSenseUtil.getBasicAuthentication(restaurant.getName(),
                            restaurant.getPaymentsenseKey()));
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true);

            String body = createRequestBody(signatureAnswered);
            urlConnection.setFixedLengthStreamingMode(body.getBytes().length);

            PrintWriter pw = new PrintWriter(
                    new BufferedOutputStream(urlConnection.getOutputStream()));
            pw.write(body);
            pw.flush();
            pw.close();

            urlConnection.connect();

            if (urlConnection.getResponseCode() != 202) {
                signatureError = "Verifying signature: ";
                signatureErrorFlag = true;

                if (urlConnection.getResponseCode() == 422) canRetry = true;

                String message = PaymentSenseUtil.getErrorFromResponse(PaymentSenseUtil
                        .streamToString(urlConnection.getInputStream()));
                if (message == null || message.isEmpty())
                    signatureError += context.getString(R.string.internal_terminal_error);
                else
                    signatureError += message;
            }

        } catch (Exception e) {
            // timeout is causing the exception
            signatureErrorFlag = true;
            signatureError = "Verifying signature: Error connecting to PaymentSense";
        }

        final boolean retry = canRetry;
        if (signatureErrorFlag) {
            activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                    listener.onSignatureVerificationError(signatureError, retry);
                }
            });
        } else {
            signaturePrompted = false;
        }

        previousSignatureAnswer = signatureAnswered;
        signatureAnswered = null;
        return false;
    }

    private static String createRequestBody(boolean accepted) {
        JSONObject o = new JSONObject();

        try {
            o.put("accepted", accepted);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }

        return o.toString();
    }
}
