package uk.co.epicuri.waiter.ui;

import static uk.co.epicuri.waiter.ui.PaymentDialogFragment.DEFAULT_TPI;
import static uk.co.epicuri.waiter.ui.PaymentDialogFragment.TPI_STRING;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.PaymentsenseTerminal;
import uk.co.epicuri.waiter.webservice.DeletePSTransactionWebServiceTask;
import uk.co.epicuri.waiter.webservice.GetTerminalsWebServiceTask;
import uk.co.epicuri.waiter.webservice.PollPaymentSenseTransactionWebServiceTask;
import uk.co.epicuri.waiter.webservice.PostPSTransactionWebServiceTask;
import uk.co.epicuri.waiter.webservice.PutAdjustmentRefundWebServiceCall;
import uk.co.epicuri.waiter.webservice.RefreshRestaurantWebService;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;


public class RefundFragment extends DialogFragment implements DialogInterface.OnClickListener,
        WebServiceTask.OnSuccessListener, WebServiceTask.OnErrorListener,
        PostPSTransactionWebServiceTask.IPostTransactionListener,
        PollPaymentSenseTransactionWebServiceTask.IPollPaymentSenseListener,
        DeletePSTransactionWebServiceTask.IDeleteTransactionListener {

    static String ADJUSTMENT_EXTRA = "ADJUSTMENT_EXTRA";
    static String SESSION_EXTRA = "SESSION_EXTRA";
    static String AMOUNT_EXTRA = "AMOUNT_EXTRA";

    EpicuriAdjustment adjustment;
    EpicuriSessionDetail session;
    SharedPreferences sharedPreferences;
    EpicuriRestaurant restaurant;
    Spinner pdq_id;
    String location;
    double refundAmount;
    PollPaymentSenseTransactionWebServiceTask transactionTask;

    private AlertDialog signatureDialog;

    @InjectView(R.id.paymentsense_progress)
    ProgressBar paymentSenseProgressBar;

    @InjectView(R.id.errors_text) TextView errorTextView;
    static RefundFragment newInstance(EpicuriSessionDetail session, EpicuriAdjustment adjustment,
                                      double refundAmount) {
        RefundFragment fragment = new RefundFragment();

        Bundle args = new Bundle();
        args.putParcelable(ADJUSTMENT_EXTRA, adjustment);
        args.putParcelable(SESSION_EXTRA, session);
        args.putDouble(AMOUNT_EXTRA, refundAmount);
        fragment.setArguments(args);

        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustment = getArguments().getParcelable(ADJUSTMENT_EXTRA);
        refundAmount = getArguments().getDouble(AMOUNT_EXTRA);
        session = getArguments().getParcelable(SESSION_EXTRA);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_refund, null);
        ButterKnife.inject(this, v);
        pdq_id = (Spinner) v.findViewById(R.id.pdq_id);
        final TextView text = (TextView) v.findViewById(R.id.select_terminal);

        final String default_id = sharedPreferences.getString(DEFAULT_TPI, "");
        restaurant = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant();
        if (restaurant.hasPaymentSenseAdjustmentType() && !TextUtils.isEmpty(restaurant.getPaymentsenseHost())) {
            //showTerminals(default_id, null);
            paymentSenseProgressBar.setVisibility(View.VISIBLE);
            (new GetTerminalsWebServiceTask(getContext(), restaurant, new GetTerminalsWebServiceTask.ITerminalsListener() {
                @Override public void onTerminalsLoaded(String response, EpicuriRestaurant restaurant) {
                    if(response == null || !response.isEmpty()){
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText(response);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                    showTerminals(default_id, text, restaurant);
                    paymentSenseProgressBar.setVisibility(View.GONE);
                }
            })).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton("Refund", null)
                .setNegativeButton("Cancel", null)
                .setView(v)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        RefundFragment.this.onClick(null, 0);
                    }
                });
            }
        });

        return dialog;
    }

    private void showTerminals(String default_id, TextView noTerminalsTextView, EpicuriRestaurant restaurant) {
        int selectId = -1;
        PaymentsenseTerminal terminal;
        List<String> terminalIds = new ArrayList<>(restaurant.getTerminals().size());

        for (int i = 0; i < restaurant.getTerminals().size(); ++i) {
            terminal = restaurant.getTerminals().get(i);
            terminalIds.add(TPI_STRING + terminal.getTpi());

            if (terminal.getTpi().equals(default_id)) selectId = i;
        }

        if (terminalIds.isEmpty() && noTerminalsTextView != null) {
            noTerminalsTextView.setText(getString(R.string.no_terminals));
            pdq_id.setVisibility(View.GONE);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity()
                    , android.R
                    .layout.simple_spinner_item, terminalIds);

            pdq_id.setAdapter(adapter);

            if (selectId != -1) pdq_id.setSelection(selectId);
        }
    }

    @Override public void onResume() {
        super.onResume();
        if (transactionTask != null) ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog
                (getString(R.string.checking_payment) + "Restoring status");
    }

    @Override public void onClick(DialogInterface dialogInterface, int i) {
        WebServiceTask task = new WebServiceTask(getActivity(), new RefreshRestaurantWebService());
        task.setIndicatorText(getString(R.string.refund_progress) + "STATUS: Initiating refund");
        task.setOnCompleteListener(this);
        task.setOnErrorListener(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override public void onSuccess(int code, String response) {
        if (code == 200) {
            try {
                JSONObject rJson = new JSONObject(response);
                EpicuriRestaurant restaurant = new EpicuriRestaurant(rJson);

                LocalSettings.getInstance(getActivity()).cacheRestaurant(restaurant);
            } catch (Exception e) { /* Dont handle any error */ }
        }

        doThePSTransaction();
    }

    @Override public void onError(int code, String response) {
        doThePSTransaction();
    }

    @Override public void onPostTransactionSuccess(final String location) {
        this.location = location;
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();
        ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog(getString(R.string
                        .checking_payment) + "STATUS: Initiating payment",
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        if (transactionTask == null || transactionTask.isCancelled()) return;

                        transactionTask.cancel(true);
                        DeletePSTransactionWebServiceTask task = new
                                DeletePSTransactionWebServiceTask(getActivity(), location);
                        task.setListener(RefundFragment.this);
                        task.execute();
                        ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog(getString(R.string.canceling_transaction));

                        startPaymentSensePolling();
                    }
                });
        startPaymentSensePolling();
    }

    private void startPaymentSensePolling() {
        transactionTask = new PollPaymentSenseTransactionWebServiceTask(getActivity(), location);
        transactionTask.setListener(this, getActivity());
        transactionTask.execute();
    }

    @Override public void onDeleteTransactionFailure(String errorMessage) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        getActivity().runOnUiThread(new Runnable() {
            @Override public void run() {
                onPostTransactionSuccess(RefundFragment.this.location);
            }
        });
    }

    @Override public void onDeleteTransactionSuccess() {
        onTransactionFailure("CANCELLED: " + getString(R.string.no_payment_taken));
    }

    @Override public void onPostTransactionFailure(String errorMessage) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        new AlertDialog.Builder(getActivity())
                .setTitle("Error using terminal")
                .setMessage(errorMessage)
                .setNegativeButton("Cancel", null)
                .show();

        getDialog().dismiss();
    }

    @Override public void onSignatureVerificationNeeded() {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        signatureDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Verify card signature")
                .setMessage("Is the signature verified?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        transactionTask.setSignatureVerified(false);
                        //triggerSignatureAcceptanceOrReversal(getString(R.string.reversing_transaction), false);
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        transactionTask.setSignatureVerified(true);
                        //triggerSignatureAcceptanceOrReversal(getString(R.string.refund_progress), true);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void triggerSignatureAcceptanceOrReversal(final String message, final boolean verified) {
        final EpicuriBaseActivity activity = (EpicuriBaseActivity) getActivity();
        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.showPleaseWaitDialog(message);
                    transactionTask.setSignatureVerified(verified);
                }
            });
        }
    }

    @Override
    public void onSignatureVerificationTimeout() {
        if(signatureDialog != null) {
            signatureDialog.cancel();
            signatureDialog = null;
        }

        final FragmentActivity activity = getActivity();

        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(activity)
                            .setTitle("TIMED OUT")
                            .setMessage("Signature verification took too long!\nPayment is SUCCESSFUL.")
                            .setNegativeButton("OK", null)
                            .show();
                }
            });
        }
    }

    @Override public void onTransactionSuccess(String paymentId, Integer amountGratuity) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();
        WebServiceCall adjustmentCall = new PutAdjustmentRefundWebServiceCall(session,
                adjustment, paymentId, location);
        WebServiceTask task = new WebServiceTask(getActivity(), adjustmentCall);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();

        transactionTask = null;
        getDialog().dismiss();
    }

    @Override public void onTransactionFailure(String errorMessage) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        new AlertDialog.Builder(getActivity())
                .setTitle("Error using terminal")
                .setMessage(errorMessage)
                .setNegativeButton("Cancel", null)
                .show();

        getDialog().dismiss();
        transactionTask.cancel(true);
        transactionTask = null;

    }

    @Override public void onTransactionStatusChanges(String status) {
        ((EpicuriBaseActivity) getActivity()).changeDialogText(getString(R.string
                .refund_progress) + " STATUS: " + status);
    }

    @Override public void onSignatureVerificationError(String error, boolean canRetry) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        new AlertDialog.Builder(getActivity())
                .setTitle("Signature verification failure")
                .setMessage(error)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        ((EpicuriBaseActivity) getActivity()).changeDialogText(getString(R.string
                                .refund_progress));
                        transactionTask.skipSignatureVerification();
                    }
                })
                .setPositiveButton(canRetry ? "Retry" : "", canRetry ? new DialogInterface
                        .OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog(
                                getString(R.string.refund_progress));
                        transactionTask.retrySignatureVerification();
                    }
                } : null)
                .setCancelable(false)
                .show();
    }

    private void doThePSTransaction() {
        ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog(
                getActivity().getString(R.string.refund_progress) + "STATUS: Initiating refund");

        List<PaymentsenseTerminal> terminals = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant().getTerminals();

        if (pdq_id.getSelectedItemPosition() == -1) {
            Toast.makeText(getContext(), "Cannot refund: PDQ terminals not detected", Toast.LENGTH_SHORT).show();
            return;
        }

        String tpi = terminals.get(pdq_id.getSelectedItemPosition()).getTpi();
        if (tpi == null || tpi.isEmpty()) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(DEFAULT_TPI).apply();
        editor.putString(DEFAULT_TPI, tpi).apply();

        PostPSTransactionWebServiceTask task = new PostPSTransactionWebServiceTask(getActivity(),
                terminals.get(pdq_id.getSelectedItemPosition()), refundAmount, true);
        task.setListener(this);
        task.execute();
    }
}
