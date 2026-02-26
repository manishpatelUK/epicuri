package uk.co.epicuri.waiter.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.joda.money.Money;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.ListenerMews;
import uk.co.epicuri.waiter.interfaces.NumberFormatCallback;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriMewsCustomer;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.PaymentsenseTerminal;
import uk.co.epicuri.waiter.utils.MoneyWatcher;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.DeletePSTransactionWebServiceTask;
import uk.co.epicuri.waiter.webservice.GetTerminalsWebServiceTask;
import uk.co.epicuri.waiter.webservice.PollPaymentSenseTransactionWebServiceTask;
import uk.co.epicuri.waiter.webservice.PostPSTransactionWebServiceTask;
import uk.co.epicuri.waiter.webservice.RefreshRestaurantWebService;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class PaymentDialogFragment extends DialogFragment implements ListenerMews,
        PostPSTransactionWebServiceTask.IPostTransactionListener,
        PollPaymentSenseTransactionWebServiceTask.IPollPaymentSenseListener,
        DeletePSTransactionWebServiceTask.IDeleteTransactionListener {

    private static final String LOGGER = "PaymentDialogFragment";
    public static int savedPaymentMethod = -1;
	private static final String ARG_SESSION = "session";
	private static final String ARG_PRESELECTED = "preselected";
	private static final String ARG_AMOUNT = "AMOUNT";
	private static final int REQUEST_LOOKUP_MEWS = 1;
    public static final String TPI_STRING = "tpi: ";
    public static final String DEFAULT_TPI = "default_tpi";
    public static final String RESTORE_PAYMENT_METHOD = "RESTORE_PAYMENT_METHOD";

    private EpicuriSessionDetail session;
    private Listener listener;
    private ArrayAdapter<EpicuriAdjustmentType> paymentMethodAdapter;
    private EpicuriMewsCustomer customer;
    private TextView mewsError;
	private boolean keyboardForcedOpen = false;

	private List<EpicuriAdjustmentType> paymentTypes;
	private Integer preselectedPosition = null;
    private EpicuriAdjustmentType paymentSenseType, paymentSenseGratuityType;
    private SharedPreferences sharedPreferences;
    private PollPaymentSenseTransactionWebServiceTask transactionTask;
    private double btnAmount = 0;
    EpicuriBaseActivity activity;

    private AlertDialog signatureDialog;

    public interface Listener {
        void addPayment(double amount, EpicuriAdjustmentType type, boolean payPrintAndClose, EpicuriSessionDetail epicuriSessionDetail, String reference);

        void addMewsPayment(double amount, EpicuriAdjustmentType paymentMethod,
                EpicuriMewsCustomer customer, boolean payPrintAndClose, boolean quickOrder);

        void addPaymentSensePayment(EpicuriSessionDetail sessionId,double amount, EpicuriAdjustmentType type,
                boolean payPrintAndClose, String reference, Integer amountGratuity,
                EpicuriAdjustmentType gratuity);
    }

    public static PaymentDialogFragment newInstance(EpicuriSessionDetail session, boolean restorePaymentMethod) {
        PaymentDialogFragment frag = new PaymentDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(RESTORE_PAYMENT_METHOD, restorePaymentMethod);
        args.putParcelable(ARG_SESSION, session);
        frag.setArguments(args);
        return frag;
    }

	public static PaymentDialogFragment newInstance(EpicuriSessionDetail session,
			String preselectedType) {
		PaymentDialogFragment frag = new PaymentDialogFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_SESSION, session);
		args.putString(ARG_PRESELECTED, preselectedType);
		frag.setArguments(args);
		return frag;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        this.btnAmount = savedInstanceState.getDouble(ARG_AMOUNT);
        this.session = getArguments().getParcelable(ARG_SESSION);
		String preselectedId = getArguments().containsKey(ARG_PRESELECTED) ? getArguments()
				.getString(ARG_PRESELECTED) : null;

        EpicuriRestaurant restaurant = LocalSettings.getInstance(
                getActivity()).getCachedRestaurant();
		paymentTypes = new ArrayList<>(restaurant.getPaymentTypes());

        if (preselectedId != null) {
            for (int i = 0; i < paymentTypes.size(); i++) {
                if (paymentTypes.get(i).getId().equals(preselectedId)) {
                    preselectedPosition = i;
                    break;
                }
            }
        }

        if (restaurant.hasPaymentSenseAdjustmentType() && restaurant.getPaymentsenseHost() != null && restaurant.getPaymentsenseKey() != null) {
            paymentSenseType = restaurant.getPaymentSensePaymentType();
            paymentSenseGratuityType = restaurant.getPaymentSenseGratuityType();
        }

        if(session.isRefund()) {
            Iterator<EpicuriAdjustmentType> types = paymentTypes.iterator();
            while(types.hasNext()) {
                EpicuriAdjustmentType next = types.next();
                if(isMewsName(next.getName())) {
                    types.remove();
                    break;
                }
            }
        }
		paymentMethodAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, paymentTypes);
		paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

    @Override public void onResume() {
        super.onResume();
        if (transactionTask != null) {
            ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog
                    (getString(R.string.checking_payment) + "Restoring status");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

	@InjectView(R.id.amount)
	EditText amountText;
	@InjectView(R.id.paymentMethod)
	Spinner paymentMethodSpinner;
	@InjectView(R.id.mews_customer_row)
	View mewsCustomerRow;
	@InjectView(R.id.mewsCustomer)
	TextView mewsCustomer;
	@InjectView(R.id.cancel_button)
	Button cancelButton;
	@InjectView(R.id.pay_button)
	Button payButton;
	@InjectView(R.id.pay_and_close_button)
	Button payAndCloseButton;
	@InjectView(R.id.print_preferences)
    Button printPrefs;
    @InjectView(R.id.paymentsense) ViewGroup paymentSenseDetails;
    @InjectView(R.id.paymentsense_progress) ProgressBar paymentSenseProgressBar;
    @InjectView(R.id.pdq_id) Spinner pdq_id;
    @InjectView(R.id.payment_sense_button) View pdq_pay_button;
    @InjectView(R.id.btn_1) Button btn1;
    @InjectView(R.id.btn_5) Button btn5;
    @InjectView(R.id.btn_10) Button btn10;
    @InjectView(R.id.btn_20) Button btn20;
    @InjectView(R.id.btn_equal_split) Button btnEqualSplit;
    @InjectView(R.id.errors_text) TextView errorTextView;
    @InjectView(R.id.refundLabel) TextView refundLabel;

    @OnClick({R.id.btn_1, R.id.btn_5, R.id.btn_10, R.id.btn_20, R.id.btn_equal_split})
    public void amountBtnClick(View view) {
        try {
            String amountValue = amountText.getText().toString();
            if (amountValue.length() != 0 && !Character.isDigit(amountValue.charAt(0)))
                amountValue = amountValue.replace(amountValue.charAt(0) + "", "");

            Double textViewAmount = Double.parseDouble(amountValue);
            // ensure this format is valid monetary amount
            Money.of(LocalSettings.getCurrencyUnit(), textViewAmount);
            if (Math.abs(textViewAmount - btnAmount) > 0.000001) {
                btnAmount = 0;
            }
        } catch (NumberFormatException | ArithmeticException e) {
            btnAmount = 0;
        }
        switch (view.getId()) {
            case R.id.btn_1:
                btnAmount += 1;
                break;
            case R.id.btn_5:
                btnAmount += 5;
                break;
            case R.id.btn_10:
                btnAmount += 10;
                break;
            case R.id.btn_20:
                btnAmount += 20;
                break;
            case R.id.btn_equal_split:
                double equalSplit = session.getTotal().dividedBy(session.getNumberInParty(), RoundingMode.CEILING).getAmount().doubleValue();
                btnAmount += equalSplit;
                break;
        }
        amountText.setText(String.format("%.2f", btnAmount));
    }

    private String location;

    private WebServiceTask.OnSuccessListener successListener = new WebServiceTask
            .OnSuccessListener() {
        @Override
        public void onSuccess(int code, String response) {
            if (code == 200) {
                try {
                    JSONObject rJson = new JSONObject(response);
                    EpicuriRestaurant restaurant = new EpicuriRestaurant(rJson);

                    LocalSettings.getInstance(getActivity()).cacheRestaurant(restaurant);
                } catch (Exception e) { /* Dont handle any error */ }
            }

            doThePSTransaction();
        }
    };

    private WebServiceTask.OnErrorListener errorListener = new WebServiceTask.OnErrorListener() {
        @Override public void onError(int code, String response) {
            doThePSTransaction();
        }
    };

	@SuppressLint("DefaultLocale")
    @NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_acceptpayment, null,
                false);
        ButterKnife.inject(this, view);
        btn1.setText(String.format("+%s1", LocalSettings.getCurrencyUnit().getSymbol()));
        btn5.setText(String.format("+%s5", LocalSettings.getCurrencyUnit().getSymbol()));
        btn10.setText(String.format("+%s10", LocalSettings.getCurrencyUnit().getSymbol()));
        btn20.setText(String.format("+%s20", LocalSettings.getCurrencyUnit().getSymbol()));
        Money equalSplit = session.getTotal().dividedBy(session.getNumberInParty(), RoundingMode.CEILING);
        btnEqualSplit.setText(String.format(getString(R.string.equal_split), LocalSettings.getCurrencyUnit().getSymbol(), LocalSettings.formatMoneyAmount(equalSplit, false)));
		BigDecimal remainingAmount = session.getRemainingTotal().getAmount();
		if(remainingAmount.compareTo(BigDecimal.ZERO) >= 0) {
			amountText.setText(remainingAmount.toString());
			amountText.setSelectAllOnFocus(true);
		}

		paymentMethodSpinner.setAdapter(paymentMethodAdapter);

        final EpicuriRestaurant restaurant = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant();
        boolean containsPaymentSense = restaurant.hasPaymentSenseAdjustmentType() && !TextUtils.isEmpty(restaurant.getPaymentsenseHost());

        if(session.isRefund() || !containsPaymentSense) {
            paymentSenseDetails.setVisibility(View.GONE);
        } else {
            paymentSenseDetails.setVisibility(View.VISIBLE);
        }
        if(session.isRefund()) {
            refundLabel.setVisibility(View.VISIBLE);
            amountText.setEnabled(false);
            btnEqualSplit.setEnabled(false);
            btn1.setEnabled(false);
            btn5.setEnabled(false);
            btn10.setEnabled(false);
            btn20.setEnabled(false);
        }
        paymentSenseProgressBar.setVisibility(containsPaymentSense ? View.VISIBLE : View.GONE);
        if (containsPaymentSense) {
            if (restaurant.hasPaymentSenseAdjustmentType() && !TextUtils.isEmpty(restaurant.getPaymentsenseHost())) {
                //showTerminals(restaurant);
                (new GetTerminalsWebServiceTask(getContext(), restaurant, new GetTerminalsWebServiceTask.ITerminalsListener() {
                    @Override public void onTerminalsLoaded(String response, EpicuriRestaurant restaurant) {
                        paymentSenseProgressBar.setVisibility(View.GONE);
                        if(response == null || !response.isEmpty()){
                            errorTextView.setVisibility(View.VISIBLE);
                            errorTextView.setText(response);
                        } else {
                            errorTextView.setVisibility(View.GONE);
                        }
                        showTerminals(restaurant);
                    }
                })).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        if (getArguments() != null && getArguments().getBoolean(RESTORE_PAYMENT_METHOD) && savedPaymentMethod != -1) {
            paymentMethodSpinner.setSelection(savedPaymentMethod);
        }

        // if the system would choose mews payment, decide whether it's allowed and choose the
        // next payment type if so
        int mewsPaymentTypePosition = getMewsPaymentTypePosition();
        if (paymentMethodAdapter.getCount() > 1 && mewsPaymentTypePosition >= 0) {
            boolean mewsAllowed = true;
            // check for the right state
            if (!session.isBillRequested() || session.isRefund()) {
                mewsAllowed = false;
            } else {
                boolean paymentsExist = false;

                // check for payments or monetary adjustments
                for (EpicuriAdjustment a : session.getAdjustments()) {
                    // bit of a cheat, means we don't need to look up the adjustment type etc.
                    if (null != a.getAmount()) {
                        paymentsExist = true;
                        break;
                    }
                }
                if (paymentsExist) mewsAllowed = false;
            }

            if (!mewsAllowed && paymentMethodSpinner.getSelectedItemPosition() == mewsPaymentTypePosition) {
                paymentMethodSpinner.setSelection(getNextNonMewsPosition(mewsPaymentTypePosition));
            }
        }

        if (preselectedPosition != null) {
            paymentMethodSpinner.setSelection(preselectedPosition);
        }

        amountText.addTextChangedListener(new MoneyWatcher(amountText, "#.00",
				new NumberFormatCallback() {
					@Override
					public void finishedFormatting() {
						validate();
					}
				}));

		paymentMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				paymentMethodSelected(paymentMethodAdapter.getItem(i));
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		mewsError = (TextView) view.findViewById(R.id.mewsError);

		payButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makePayment(false, session.isAdHoc());
			}
		});
		payAndCloseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makePayment(true, session.isAdHoc());
			}
		});

		printPrefs.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(getActivity() != null){
                    if(getActivity() instanceof SessionActivity){
                        ((SessionActivity) getActivity()).printReceipt(true);
                    }else if(getActivity() instanceof QuickOrderActivity){
                        ((QuickOrderActivity) getActivity()).printReceipt();
                    }
                }
            }
        });

        if(session != null && session.isAdHoc()) {
            btnEqualSplit.setVisibility(View.GONE);
        }

        forceKeyboardClosed();

		return new AlertDialog.Builder(getActivity())
				.setTitle(session.isRefund() ? "Add New Refund" : "Add New Payment")
				.setView(view)
				.create();
	}

    private void showTerminals(EpicuriRestaurant restaurant) {
        String default_id = sharedPreferences.getString(DEFAULT_TPI, "");
        int selectId = -1;
        PaymentsenseTerminal terminal;

        List<String> terminalIds = new ArrayList<>(restaurant.getTerminals().size());

        for (int i = 0; i < restaurant.getTerminals().size(); ++i) {
            terminal = restaurant.getTerminals().get(i);
            terminalIds.add(TPI_STRING + terminal.getTpi());

            if (terminal.getTpi().equals(default_id)) selectId = i;
        }

        boolean disablePS = !terminalIds.isEmpty();

        paymentSenseDetails.setEnabled(disablePS);
        pdq_id.setEnabled(disablePS);
        pdq_pay_button.setEnabled(disablePS);

        Context context = getContext();
        if(context != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R
                    .layout.simple_spinner_item, terminalIds);
            pdq_id.setAdapter(adapter);
            if (selectId != -1) pdq_id.setSelection(selectId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(ARG_AMOUNT, btnAmount);
    }

    private int getNextNonMewsPosition(int mewsPosition) {
		for(int i = 0; i < paymentMethodAdapter.getCount(); i++) {
			if(i != mewsPosition) {
				return i;
			}
		}
		return 0;
	}

	private int getMewsPaymentTypePosition() {
		for(int i = 0; i < paymentTypes.size(); i++) {
			EpicuriAdjustmentType item = paymentTypes.get(i);
			if(item != null && isMewsName(item.getName())) {
				Log.d(LOGGER, "Mews flag=true, so returning " + i);
				return i;
			}
		}
		return -1;
	}

	private boolean isMewsName(String name) {
		return name.equalsIgnoreCase("mews");
	}

    private void makePayment(boolean payPrintAndClose, boolean quickOrder) {
        if(keyboardForcedOpen) {
            forceKeyboardClosed();
        }

        EpicuriAdjustmentType paymentMethod = paymentMethodAdapter.getItem(
                paymentMethodSpinner.getSelectedItemPosition());
        savedPaymentMethod = paymentMethodSpinner.getSelectedItemPosition();
		final double amount;
		try {
			String amountValue = amountText.getText().toString();
			if (amountValue.length() != 0 && !Character.isDigit(amountValue.charAt(0)))
				amountValue = amountValue.replace(amountValue.charAt(0)+"", "");

			amount = Double.parseDouble(amountValue);
			// ensure this format is valid monetary amount
			Money.of(LocalSettings.getCurrencyUnit(), amount);
		} catch (NumberFormatException | ArithmeticException e){
			new AlertDialog.Builder(getActivity())
					.setTitle("Cannot add payment")
					.setMessage(R.string.invalid_money_amount)
					.setNegativeButton("Cancel", null)
					.show();
			return;
		}

		if(paymentMethod != null && isMewsName(paymentMethod.getName())){
			if(null == customer){
				new AlertDialog.Builder(getActivity())
						.setTitle("Cannot add payment")
						.setMessage("No customer specified")
						.setNegativeButton("Cancel", null)
						.show();
				return;
			}
			listener.addMewsPayment(amount, paymentMethod, customer, payPrintAndClose, quickOrder);
			dismiss();
		} else {
			Utils.closeKeyboard(getActivity(), amountText.getWindowToken());
			listener.addPayment(amount, paymentMethod, payPrintAndClose, session, null);
			dismiss();
		}
	}

	@OnClick(R.id.cancel_button)
	void cancel(){
	    if(keyboardForcedOpen) {
	        forceKeyboardClosed();
        }
		dismiss();
	}

    @OnClick(R.id.payment_sense_button) void onClickPaymentsense() {
        if (paymentSenseType == null) return;

        WebServiceTask task = new WebServiceTask(getActivity(), new RefreshRestaurantWebService());
        task.setIndicatorText(getString(R.string.checking_payment) + "STATUS: Initiating payment");
        task.setOnCompleteListener(successListener);
        task.setOnErrorListener(errorListener);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override public void onSignatureVerificationNeeded() {
        if (activity == null && getActivity() != null) activity = (EpicuriBaseActivity) getActivity();
        activity.dismissPleaseWaitDialog();

        signatureDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Verify card signature")
                .setMessage("Is the signature verified?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        if (transactionTask != null) {
                            transactionTask.setSignatureVerified(false);
                        }
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        if (transactionTask != null) {
                            transactionTask.setSignatureVerified(true);
                        }
                        //triggerSignatureAcceptanceOrReversal("Payment in progress.", true);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void triggerSignatureAcceptanceOrReversal(final String message, final boolean verified) {
	    activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity == null && getActivity() != null) activity = (EpicuriBaseActivity) getActivity();
                activity.showPleaseWaitDialog(message);
                if (transactionTask != null) {
                    transactionTask.setSignatureVerified(verified);
                } else {
                    Crashlytics.log("Transaction task is null in onSignatureVerificationNeeded");
                }
            }
        });
    }

    @Override
    public void onSignatureVerificationTimeout() {
	    if(signatureDialog != null) {
	        signatureDialog.cancel();
            signatureDialog = null;
        }

        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("TIMED OUT")
                            .setMessage("Signature verification took too long!\nPayment is SUCCESSFUL.")
                            .setNegativeButton("OK", null)
                            .show();
                }
            });
        }
    }

    @Override public void onPause() {
        super.onPause();
        activity = ((EpicuriBaseActivity) getActivity());
    }

    @Override public void onPostTransactionSuccess(final String location) {
        this.location = location;

        final EpicuriBaseActivity epicuriBaseActivity = (EpicuriBaseActivity) getActivity();
        if (epicuriBaseActivity == null) {
            Toast.makeText(this.getContext(), "Cannot process PDQ transaction - quit the app and try again", Toast.LENGTH_LONG).show();
            return;
        }
        epicuriBaseActivity.dismissPleaseWaitDialog();
        epicuriBaseActivity.showPleaseWaitDialog(getString(R.string
                        .checking_payment) + "STATUS: Initiating payment",
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        if (transactionTask == null || transactionTask.isCancelled()) return;

                        transactionTask.cancel(true);
                        DeletePSTransactionWebServiceTask task = new
                                DeletePSTransactionWebServiceTask(getActivity(), location);
                        task.setListener(PaymentDialogFragment.this);
                        task.execute();
                        epicuriBaseActivity.showPleaseWaitDialog(getString(R.string.canceling_transaction));

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
        ((EpicuriBaseActivity)getActivity()).dismissPleaseWaitDialog();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        getActivity().runOnUiThread(new Runnable() {
            @Override public void run() {
                onPostTransactionSuccess(PaymentDialogFragment.this.location);
            }
        });
    }

    @Override public void onDeleteTransactionSuccess() {
	    //onTransactionFailure("CANCELLED: " + getString(R.string.no_payment_taken));
    }

    @Override public void onPostTransactionFailure(String errorMessage) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        new AlertDialog.Builder(getActivity())
                .setTitle("PDQ Connect Error")
                .setMessage(errorMessage)
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override public void onTransactionStatusChanges(String status) {
        ((EpicuriBaseActivity) getActivity()).changeDialogText(getString(R.string
                .checking_payment) + " STATUS: " + status);
    }

    @Override public void onTransactionSuccess(String paymentId, Integer amountGratuity) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        final double amount;
        amount = Double.parseDouble(amountText.getText().toString());

        Utils.closeKeyboard(getActivity(), amountText.getWindowToken());
        boolean payPrintAndClose = session.getRemainingTotal().getAmount().doubleValue() - amount <= 0 && session.isAdHoc();
        listener.addPaymentSensePayment(session, amount, paymentSenseType, payPrintAndClose, paymentId,
                amountGratuity, paymentSenseGratuityType);
        dismiss();

        transactionTask = null;
    }

    @Override public void onSignatureVerificationError(String error, boolean canRetry) {
        if(activity == null && getActivity() != null) activity = ((EpicuriBaseActivity) getActivity());
        activity.dismissPleaseWaitDialog();

        if(getActivity() == null) Log.d("ACTIVITY_NULL", "Activity in onSignatureVerificationError is null");
        new AlertDialog.Builder(getActivity())
                .setTitle("Signature verification failure")
                .setMessage(error)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.changeDialogText(activity.getString(R.string.checking_payment));
                        if (transactionTask != null) transactionTask.skipSignatureVerification();
                    }
                })
                .setPositiveButton(canRetry ? "Retry" : "", canRetry ? new DialogInterface
                        .OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        activity.showPleaseWaitDialog(activity.getString(R.string.checking_payment));
                        if (transactionTask != null) transactionTask.retrySignatureVerification();
                    }
                } : null)
                .setCancelable(false)
                .show();
    }

    @Override public void onTransactionFailure(String errorMessage) {
        ((EpicuriBaseActivity) getActivity()).dismissPleaseWaitDialog();

        new AlertDialog.Builder(getActivity())
                .setTitle("Payment failure")
                .setMessage(errorMessage)
                .setNegativeButton("Cancel", errorMessage.equals(getString(R.string.pdq_timeout))
                        ? new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        new AlertDialog.Builder(getActivity())
                                .setTitle("Transaction result")
                                .setMessage("Please manually record transaction result. Was the "
                                        + "transaction successful?")
                                .setCancelable(false)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        transactionTask = null;
                                        dialogInterface.dismiss();
                                        showTryAgainDialog();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        onTransactionSuccess(transactionTask.getTransactionId(),
                                                transactionTask.getAmountGratuity());
                                    }
                                }).show();
                    }
                } : new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        transactionTask = null;
                    }
                })
                .show();
    }

    private void showTryAgainDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Complete manually?")
                .setMessage("Would you like to complete the transaction manually?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        PaymentDialogFragment.this.dismiss();
                    }
                }).show();
    }

    private void paymentMethodSelected(EpicuriAdjustmentType type) {
        if (isMewsName(type.getName())) {
            mewsCustomerRow.setVisibility(View.VISIBLE);
            amountText.setText(LocalSettings.formatMoneyAmount(session.getTotal(), false));
            amountText.setEnabled(false);

			// check for the right state
			if(!session.isBillRequested()) {
				payButton.setEnabled(false);
				mewsError.setVisibility(View.VISIBLE);
				mewsError.setText(R.string.mewsError_cannot_pay_before_bill_requested);
			} else {
				boolean paymentsExist = false;

				// check for payments or monetary adjustments
				for(EpicuriAdjustment a: session.getAdjustments()){
					// bit of a cheat, means we don't need to look up the adjustment type etc.
					if(null != a.getAmount()){
						paymentsExist = true;
						break;
					}
				}
				if(paymentsExist){
					mewsError.setText(R.string.mewsError_payments_exist);
					mewsError.setVisibility(View.VISIBLE);
					payButton.setEnabled(false);
				} else {
					mewsError.setVisibility(View.GONE);
					payButton.setEnabled(true);
				}
			}
		} else {
			if(!amountText.isEnabled()){
				// previously was MEWS, reset amount to the correct amount
				BigDecimal remainingAmount = session.getRemainingTotal().getAmount();
				if(remainingAmount.compareTo(BigDecimal.ZERO) > 0){
					//NumberFormat formatter = NumberFormat.getCurrencyInstance();
					//amountText.setText(formatter.format(remainingAmount.doubleValue()));
					amountText.setText(LocalSettings.formatMoneyAmount(remainingAmount.doubleValue(), false));
				}
			}
			mewsError.setVisibility(View.GONE);
			mewsCustomerRow.setVisibility(View.GONE);
			if(!session.isRefund()) {
                amountText.setEnabled(true);
            }
			payButton.setEnabled(true);
		}
		validate();
	}

	private boolean validate(){
	    payButton.setEnabled(true);
		payAndCloseButton.setEnabled(false);
		final Money moneyAmount;
		try {
			String amountValue = amountText.getText().toString();
			if (amountValue.length() != 0 && !Character.isDigit(amountValue.charAt(0)))
				amountValue = amountValue.replace(amountValue.charAt(0)+"", "");

			double amount = Double.parseDouble(amountValue);

			// ensure this format is valid monetary amount
			moneyAmount = Money.of(LocalSettings.getCurrencyUnit(), amount);
		} catch (NumberFormatException | ArithmeticException e){
			payButton.setEnabled(false);
            payAndCloseButton.setEnabled(false);
			return false;
		}

		if (session.isAdHoc() && moneyAmount.isZero()){
			payButton.setEnabled(false);
			if(session.getRemainingTotal().minus(moneyAmount).isNegativeOrZero()){
				payAndCloseButton.setEnabled(true);
			}

			return true;
		}

		if (session.isAdHoc() && (moneyAmount.isEqual(session.getRemainingTotal()) || moneyAmount.isGreaterThan(session.getRemainingTotal()))) {
			payAndCloseButton.setEnabled(true);
			payButton.setEnabled(false);

			return true;
		}

		if(moneyAmount.isNegativeOrZero()){
			payButton.setEnabled(false);
			return false;
		} else if(session.getRemainingTotal().minus(moneyAmount).isNegativeOrZero()){
			payAndCloseButton.setEnabled(true);
		}
		return true;
	}

    private void doThePSTransaction() {
	    if(pdq_id.getSelectedItemPosition() == -1) {
	        try {
	            Toast.makeText(getContext(), "PDQ not selected", Toast.LENGTH_SHORT).show();
            } catch (Exception ex){}
	        return;
        }
        List<PaymentsenseTerminal> terminals = LocalSettings.getInstance(getActivity())
                .getCachedRestaurant().getTerminals();

        String tpi = terminals.get(pdq_id.getSelectedItemPosition()).getTpi();
        if (tpi == null || tpi.isEmpty()) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(DEFAULT_TPI).apply();
        editor.putString(DEFAULT_TPI, tpi).apply();
        if (amountText.getText().toString() == null || amountText.getText().toString().isEmpty())
            amountText.setText("0");

        final PostPSTransactionWebServiceTask task = new PostPSTransactionWebServiceTask(getActivity(),
                terminals.get(pdq_id.getSelectedItemPosition()),
                Double.parseDouble(amountText.getText().toString()), false);
        task.setListener(PaymentDialogFragment.this);
        task.execute();

        ((EpicuriBaseActivity) getActivity()).showPleaseWaitDialog(
                getString(R.string.checking_payment) + "STATUS: Initiating payment", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (!task.isCancelled()){
                            task.cancel(true);
                        }
                    }
                });
    }

    private void forceKeyboardOpen() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            keyboardForcedOpen = true;
        }
    }

    private void forceKeyboardClosed() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            keyboardForcedOpen = false;
        }
    }

    @OnClick(R.id.mewsCustomer)
	void promptForMewsCustomer(){
		MewsCustomerLookup picker = MewsCustomerLookup.newInstance();
		picker.setTargetFragment(this, REQUEST_LOOKUP_MEWS);
		picker.show(getFragmentManager(), null);

	}

	@Override
	public void setCustomer(EpicuriMewsCustomer customer) {
		this.customer = customer;
		if(null != customer ){
			mewsCustomer.setText(customer.toString());
		} else {
			mewsCustomer.setText("Please select customer");
		}
	}

}
