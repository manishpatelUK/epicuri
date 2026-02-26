package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.money.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.OnReceiptPrintedListener;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.PaymentActionHandler;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.SessionDetailLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.NumericalAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMewsCustomer;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.printing.SendEmailHandlerImpl;
import uk.co.epicuri.waiter.printing.FakeReceiptFragment;
import uk.co.epicuri.waiter.ui.dialog.CashDrawerSelectDialog;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.MoneyWatcher;
import uk.co.epicuri.waiter.webservice.CloseSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteAdjustmentWebServiceCall;
import uk.co.epicuri.waiter.webservice.EditSessionTipWebServiceCall;
import uk.co.epicuri.waiter.webservice.MewsPaymentWebServiceCall;
import uk.co.epicuri.waiter.webservice.NewAdjustmentWebServiceCall;
import uk.co.epicuri.waiter.webservice.PayBillWebServiceCall;
import uk.co.epicuri.waiter.webservice.ReopenSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.UnVoidSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.VoidSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public abstract class SessionActivity extends EpicuriBaseActivity
        implements SessionContainer,
        PaymentActionHandler,
        PaymentDialogFragment.Listener, OnReceiptPrintedListener {

    public static final int LOADER_SESSIONDETAIL = 1;
    private final static int LOADER_PRINTERS = 2;
    private static final String FRAGMENT_PAYMENT = "payment";
    private static final String FRAGMENT_PRINT_RECEIPT = "printReceipt";
    public static final String TYPE_ALL = "ALL";
    public static final String TYPE_FOOD = "FOOD";
    public static final String TYPE_DRINK = "DRINK";
    public static final String TYPE_OTHER = "OTHER";
    boolean reprint;
    protected EpicuriSessionDetail session;
    protected Map<String,EpicuriMenu.Printer> printers;
    protected Uri sessionUri;

    private Handler handler;
    private boolean mPayPrintAndClose;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        handler = new Handler(Looper.getMainLooper());

        String sessionId = getIntent().getExtras().getString(GlobalSettings.EXTRA_SESSION_ID);
        if (sessionId != null && !sessionId.equals("0") && !sessionId.equals("-1")) {
            loadSession(sessionId);
        }
        reprint = Boolean.valueOf(LocalSettings.getInstance(SessionActivity.this).getCachedRestaurant().getRestaurantDefault(EpicuriRestaurant.DEFAULT_REPRINT_BILL, "false"));

        getSupportLoaderManager().restartLoader(LOADER_PRINTERS, null, new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
            @Override
            public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i, Bundle bundle) {
                return new OneOffLoader<ArrayList<EpicuriMenu.Printer>>(SessionActivity.this, new PrinterLoaderTemplate());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader, ArrayList<EpicuriMenu.Printer> result) {
                if (result != null) {
                    printers = new HashMap<>(result.size());
                    for (EpicuriMenu.Printer p : result) {
                        printers.put(p.getId(), p);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {
                // don't care
            }
        });
    }

    void loadSession(String sessionId) {
        sessionUri = Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId);

        // now load the session
        Bundle args = new Bundle();
        args.putString(GlobalSettings.EXTRA_SESSION_ID, sessionId);
        getSupportLoaderManager().initLoader(LOADER_SESSIONDETAIL, args, sessionLoaderCallbacks);
    }

    // handle loading of session
    private final LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriSessionDetail>> sessionLoaderCallbacks = new LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriSessionDetail>>() {

        @Override
        public Loader<LoaderWrapper<EpicuriSessionDetail>> onCreateLoader(int id, Bundle args) {
            String sessionId = args.getString(GlobalSettings.EXTRA_SESSION_ID);
            EpicuriLoader<EpicuriSessionDetail> loader = new EpicuriLoader<EpicuriSessionDetail>(SessionActivity.this, new SessionDetailLoaderTemplate(sessionId));
            loader.setAutoRefreshPeriod(EpicuriLoader.DEFAULT_REFRESH_PERIOD);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<LoaderWrapper<EpicuriSessionDetail>> loader,
                                   LoaderWrapper<EpicuriSessionDetail> data) {
            if (null == data) { // nothing returned, ignore
                return;
            } else if (data.isError()) {
                Toast.makeText(SessionActivity.this, "SessionActivity error loading data", Toast.LENGTH_SHORT).show();
                return;
            }

            SessionActivity.this.session = data.getPayload();

            handler.post(new Runnable() {
                public void run() {
                    // tell the subclass to process the session data
                    onSessionLoaded(session);

                    // tell the listeners that the session has been updated
                    listenerIterator = listeners.iterator();
                    while (listenerIterator.hasNext()){
                        listenerIterator.next().onSessionChanged(session);
                    }
                    invalidateOptionsMenu();
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<LoaderWrapper<EpicuriSessionDetail>> loader) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addPayment:
                if (!session.isPaid()) {
                    showPaymentDialog();
                }
                return true;
            case R.id.menu_addDiscount:
                if (!session.isPaid()) {
                    showAdjustmentDialog();
                }
                return true;
            case R.id.menu_refresh: {
                UpdateService.requestUpdate(this, sessionUri);
                return true;
            }
            case R.id.menu_reseatSession:
                Intent reseatIntent = new Intent(SessionActivity.this, HubActivity.class);
                reseatIntent.putExtra(GlobalSettings.EXTRA_RESEAT_SESSION, true);
                reseatIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, session.getId());
                reseatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(reseatIntent);
                return true;

            case R.id.menu_reopen: {
                new AlertDialog.Builder(this)
                        .setTitle("Reopen session?")
                        .setMessage("This will reopen the session for editing.  If the tables are in use, this session will be reopened as a tab")
                        .setPositiveButton("Reopen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reopenSession();
                            }
                        })
                        .setNegativeButton("Do nothing", null)
                        .show();
                return true;
            }
            case R.id.menu_void: {
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_void, null, false);
                final EditText tv = (EditText) view.findViewById(R.id.reason);
                new AlertDialog.Builder(this)
                        .setTitle("Void this session?")
                        .setView(view)
                        .setPositiveButton("Void", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                voidSession(tv.getText().toString());
                            }
                        })
                        .setNegativeButton("Do nothing", null)
                        .show();
                return true;
            }
            case R.id.menu_unvoid: {
                new AlertDialog.Builder(this)
                        .setTitle("Unvoid this session?")
                        .setMessage("Clear the void state for this session?")
                        .setPositiveButton("Remove Void", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                unvoidSession();
                            }
                        })
                        .setNegativeButton("Do nothing", null)
                        .show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    abstract void onSessionLoaded(EpicuriSessionDetail session);

    void showPaymentDialog() {
        PaymentDialogFragment.newInstance(session, true).show(getSupportFragmentManager(), FRAGMENT_PAYMENT);
    }

    @Override
    public void showAutoSettleDialog() {
        if (session.isPaid()) {
            Toast.makeText(this, "Session has been paid. Please close.", Toast.LENGTH_SHORT).show();
        } else if (session.getRemainingTotal().isPositive()) {
            final String[] options = new String[]{"... new payment", "... new adjustment"};
            new AlertDialog.Builder(this)
                    .setTitle("Settle remainder as...")
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                showPaymentDialog();
                            } else if (which == 1) {
                                showAdjustmentDialog();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            if (!session.isBillRequested()) {
                Toast.makeText(this, "You need to 'request bill' before you can mark as paid", Toast.LENGTH_SHORT).show();
                return;
            }
            // session can be closed now
            showPayBillDialog();
        }
    }

    void showPayBillDialog() {
        String repaymentsString = "";
        if (session.getOverPayments().isPositive()) {
            repaymentsString = String.format(" An overpayment of %s will be recorded against the session.", LocalSettings.formatMoneyAmount(session.getOverPayments(), true));
        }
        new AlertDialog.Builder(this)
                .setTitle("Mark as paid & drawer kick")
                .setMessage("This will close the table and open the cash drawer." + repaymentsString)
                .setPositiveButton("Mark as paid", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null != printers) {
                            kickDrawer(new CashDrawerSelectDialog.OnDrawerKicked() {
                                @Override
                                public void onDrawerKicked(boolean success) {
                                    payBill(false, session.isAdHoc());
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("Wait", null)
                .show();
    }

    void showAdjustmentDialog() {
        ArrayList<EpicuriAdjustmentType> discountTypes = LocalSettings.getInstance(this).getCachedRestaurant().getDiscountTypes();
        final ArrayAdapter<EpicuriAdjustmentType> adjustmentTypeAdapter = new ArrayAdapter<EpicuriAdjustmentType>(this, android.R.layout.simple_spinner_item, discountTypes);
        adjustmentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View view = getLayoutInflater().inflate(R.layout.dialog_addadjustment, null, false);
        final EditText amountText = view.findViewById(R.id.amount);
        final RadioGroup percentageRadio = view.findViewById(R.id.percentage);
        percentageRadio.check(R.id.percentage_yes);
        final Spinner adjustmentTypeSpinner = view.findViewById(R.id.adjustmentType);
        adjustmentTypeSpinner.setAdapter(adjustmentTypeAdapter);

        final TextWatcher normalTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        //final MoneyTextWatcher moneyTextWatcher = new MoneyTextWatcher(amountText, "#,###.##", LocalSettings.getCurrencyUnit().getSymbol());
        final MoneyWatcher moneyTextWatcher = new MoneyWatcher(amountText, "#.00", null);

        final RadioGroup foodTypeRadio = (RadioGroup) view.findViewById(R.id.food_type);
        percentageRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                boolean percentage = checkedId == R.id.percentage_yes;
                amountText.setText("");
                if (percentage) {
                    amountText.removeTextChangedListener(moneyTextWatcher);
                    amountText.addTextChangedListener(normalTextWatcher);
                    for (int i = 0; i < foodTypeRadio.getChildCount(); i++) {
                        foodTypeRadio.getChildAt(i).setEnabled(true);
                    }
                } else {
                    amountText.removeTextChangedListener(normalTextWatcher);
                    amountText.addTextChangedListener(moneyTextWatcher);
                    for (int i = 0; i < foodTypeRadio.getChildCount(); i++) {
                        foodTypeRadio.getChildAt(i).setEnabled(false);
                    }
                }
            }
        });
        View adjustmentTypeHolder = (View) view.findViewById(R.id.food_type_box);
        if(EpicuriApplication.getInstance(this).getApiVersion() >= GlobalSettings.API_VERSION_6){
            adjustmentTypeHolder.setVisibility(View.VISIBLE);
            foodTypeRadio.check(R.id.type_all);
        } else {
            adjustmentTypeHolder.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(this)
                .setTitle("Add Adjustment")
                .setView(view)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean percentage = percentageRadio.getCheckedRadioButtonId() == R.id.percentage_yes;

                        final double amount;
                        try {
                            String amountValue = amountText.getText().toString();
                            if (amountValue.length() != 0 && !Character.isDigit(amountValue.charAt(0)))
                                amountValue = amountValue.replace(amountValue.charAt(0) + "", "");

                            amount = Double.parseDouble(amountValue);

                            // ensure this format is valid monetary amount
                            if (!percentage) {
                                Money.of(LocalSettings.getCurrencyUnit(), amount);
                            }
                        } catch (NumberFormatException | ArithmeticException e) {
                            new AlertDialog.Builder(SessionActivity.this)
                                    .setTitle("Cannot add adjustment")
                                    .setMessage(R.string.invalid_adjustment_amount)
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            return;
                        }

                        String itemType;
                        switch (foodTypeRadio.getCheckedRadioButtonId()) {
                            case R.id.type_all:
                                itemType = TYPE_ALL;
                                break;
                            case R.id.type_food:
                                itemType = TYPE_FOOD;
                                break;
                            case R.id.type_drink:
                                itemType = TYPE_DRINK;
                                break;
                            case R.id.type_other:
                                itemType = TYPE_OTHER;
                                break;
                            default:
                                itemType = TYPE_ALL;
                        }

                        addAdjustment(
                                amount,
                                percentage,
                                adjustmentTypeAdapter.getItem(adjustmentTypeSpinner.getSelectedItemPosition()),
                                false,
                                itemType,
                                session.isAdHoc());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void addMewsPayment(double amount, EpicuriAdjustmentType paymentMethod, EpicuriMewsCustomer customer, boolean payPrintAndClose, boolean quickOrder) {
        MewsPaymentWebServiceCall payment = new MewsPaymentWebServiceCall(this.session, customer, amount, null);

        if (payPrintAndClose && quickOrder) {
            printReceipt(false);
        }
        WebServiceTask task = new WebServiceTask(this, payment, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                new AlertDialog.Builder(SessionActivity.this)
                        .setTitle(R.string.mewserror_title)
                        .setMessage(R.string.mewserror_body)
                        .setNegativeButton("Dismiss", null)
                        .show();
            }
        });
        if(payPrintAndClose) {
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                @Override
                public void onSuccess(int code, String response) {
                    payBill(true, session.isAdHoc());
                }
            });
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void addPayment(double amount, EpicuriAdjustmentType type, boolean payPrintAndClose, EpicuriSessionDetail session, String reference) {
        addAdjustment(amount, false, type, payPrintAndClose, TYPE_ALL, session.isAdHoc());
    }

    void addAdjustment(double amount, boolean percentage, EpicuriAdjustmentType type, boolean payPrintAndClose, String itemType, final boolean quickOrder) {
        NewAdjustmentWebServiceCall adjustment = new NewAdjustmentWebServiceCall(this.session.getId(), type, percentage ? NumericalAdjustmentType.PERCENTAGE : NumericalAdjustmentType.MONETARY, amount, null, itemType);
        WebServiceTask task = new WebServiceTask(this, adjustment, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        if(payPrintAndClose) {
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                @Override
                public void onSuccess(int code, String response) {
                    payBill(true, quickOrder);
                }
            });
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void addPaymentSensePayment(EpicuriSessionDetail sessionId, double amount, EpicuriAdjustmentType type,
                                                 boolean payPrintAndClose, String reference, Integer amountGratuity,
                                                 EpicuriAdjustmentType gratuity) {
        addPaymentSenseAdjustment(amount, type, payPrintAndClose, reference, amountGratuity, gratuity);
    }

    void addPaymentSenseAdjustment(double amount, EpicuriAdjustmentType type,
                                   final boolean payPrintAndClose, final String reference, final Integer amountGratuity,
                                   final EpicuriAdjustmentType gratuity) {
        NewAdjustmentWebServiceCall adjustment = new NewAdjustmentWebServiceCall(this.session.getId(), type,NumericalAdjustmentType.MONETARY, amount, reference, SessionActivity.TYPE_ALL);
        WebServiceTask task = new WebServiceTask(this, adjustment);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        if (amountGratuity != null && amountGratuity > 0 && gratuity != null) {
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                @Override public void onSuccess(int code, String response) {
                    EpicuriAdjustment newAdjustment = null;
                    try {
                        newAdjustment = new EpicuriAdjustment(new JSONObject(response));
                    } catch (Exception e) {
                        // Dont handle
                    }

                    NewAdjustmentWebServiceCall adjustment = new NewAdjustmentWebServiceCall
                            (SessionActivity.this.session.getId(), gratuity,
                                    NumericalAdjustmentType.MONETARY,
                                    amountGratuity/100.0, reference, newAdjustment != null ?
                                    newAdjustment.getId() : null);
                    WebServiceTask task = new WebServiceTask(SessionActivity.this, adjustment);
                    task.setIndicatorText(getString(R.string.webservicetask_alertbody));
                    if (payPrintAndClose) {
                        task.setOnCompleteListener(
                                new WebServiceTask.OnSuccessListener() {
                                    @Override public void onSuccess(int code, String response) {
                                        payBill(true, session.isAdHoc());
                                    }
                                });
                    }

                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } else if (payPrintAndClose) {
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                @Override
                public void onSuccess(int code, String response) {
                    payBill(true, session.isAdHoc());
                }
            });
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void removeAdjustment(EpicuriAdjustment adjustment) {
        WebServiceCall adjustmentCall = new DeleteAdjustmentWebServiceCall(session, adjustment);
        WebServiceTask task = new WebServiceTask(this, adjustmentCall, true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    void payBill(final boolean payPrintAndClose, final boolean quickOrder) {
        WebServiceTask task = new WebServiceTask(this, new PayBillWebServiceCall(session.getId()), true);
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                if(response != null){
                    try{
                        JSONObject responseJson = new JSONObject(response);
                        session = new EpicuriSessionDetail(responseJson);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                if (!session.getChange().isZero() && payPrintAndClose){
                    if(!isFinishing()) {
                        showChangeDialog(session.getChange()).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override public void onDismiss(DialogInterface dialog) {
                                billPaid(true, quickOrder);
                            }
                        });
                    } else {
                        billPaid(true, quickOrder);
                    }
                }else {
                    billPaid(payPrintAndClose, quickOrder);
                }
            }
        });

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void billPaid(final boolean payPrintAndClose, final boolean quickOrder){
        CashDrawerSelectDialog.OnDrawerKicked kickListener = new CashDrawerSelectDialog.OnDrawerKicked() {
            @Override
            public void onDrawerKicked(boolean success) {
               if(!reprint){
                    finish();
                }
            }
        };

        if (session.isAdHoc()) {
            // no message
        } else if (session.isTab()) {
            Toast.makeText(SessionActivity.this, R.string.toast_sessionTabPaidAndClosed, Toast.LENGTH_SHORT).show();
            kickDrawer(kickListener);
            shouldFinish = false;
        } else if(session.getType() == EpicuriSessionDetail.SessionType.DELIVERY || session.getType() == EpicuriSessionDetail.SessionType.COLLECTION) {
            Toast.makeText(SessionActivity.this, R.string.toast_takeawayPaidAndClosed, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(SessionActivity.this, R.string.toast_sessionPaidAndClosed, Toast.LENGTH_SHORT).show();
            if (!quickOrder){
                kickDrawer(kickListener);
                shouldFinish = false;
            }
        }

        if(reprint && !quickOrder && session.isClosed()){
            printReceipt(false);
            return;
        }

        if (session.isClosed() && shouldFinish){
            finish();
        }

        if (!payPrintAndClose) {
            finish();
        } else if (quickOrder) {
            mPayPrintAndClose = true;
            listeners.add(payPrintAndCloseListener);
        }
    }

    private android.app.AlertDialog showChangeDialog(Money change) {
        String amount = LocalSettings.formatMoneyAmount(change, true);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setMessage("Change Due: " + amount)
                .setPositiveButton("OK", null)
                .show();
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTextSize(40);
        return dialog;
    }

    private final OnSessionChangeListener payPrintAndCloseListener = new OnSessionChangeListener() {
        @Override
        public void onSessionChanged(EpicuriSessionDetail session) {
            if (mPayPrintAndClose && session.getRemainingTotal().isPositiveOrZero()) {
                if (null != printers) {
                    printReceipt(false);
                }
            }
        }
    };

    void reopenSession() {
        WebServiceTask task = new WebServiceTask(this, new ReopenSessionWebServiceCall(session.getId()));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(SessionActivity.this, R.string.toast_sessionReopened, Toast.LENGTH_SHORT).show();
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    void voidSession(String reason) {
        WebServiceTask task = new WebServiceTask(this, new VoidSessionWebServiceCall(session.getId(), reason));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(SessionActivity.this, R.string.toast_sessionVoided, Toast.LENGTH_SHORT).show();
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    void unvoidSession() {
        WebServiceTask task = new WebServiceTask(this, new UnVoidSessionWebServiceCall(session.getId()));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(SessionActivity.this, R.string.toast_sessionUnVoided, Toast.LENGTH_SHORT).show();
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    void closeSession(final boolean blackMark) {
        WebServiceTask task = new WebServiceTask(this, new CloseSessionWebServiceCall(session.getId(), blackMark), true);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                if (session.isAdHoc()) {
                    Toast.makeText(SessionActivity.this, R.string.toast_quickorderDiscarded, Toast.LENGTH_SHORT).show();
                } else {
                    String stockMessage = "";
                    try {
                        EpicuriRestaurant restaurant = LocalSettings.getInstance(SessionActivity.this).getCachedRestaurant();
                        if(restaurant.stockCountdownEnabled()) {
                            stockMessage = "\n" + getString(R.string.toast_force_close_stock_alert);
                        }
                    } catch (Exception ex) {}
                    if (session.isTab()) {
                        String message = getString(R.string.toast_tabClosed) + stockMessage;
                        Toast.makeText(SessionActivity.this, message , Toast.LENGTH_SHORT).show();
                    } else {
                        String message = getString(blackMark ? R.string.toast_sessionForceClosed : R.string.toast_sessionClosed);
                        Toast.makeText(SessionActivity.this, message + stockMessage , Toast.LENGTH_SHORT).show();
                    }
                }

                if(isTaskRoot()){
                    Intent i = new Intent(SessionActivity.this, QuickOrderActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    finish();
                }
            }
        });
        task.setIndicatorText(getString(R.string.webservicetask_alertbody));
        task.execute();
    }

    void printReceipt(boolean disableAutoPrint) {
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_PRINT_RECEIPT) == null) {
            FakeReceiptFragment f = FakeReceiptFragment.newInstance(getLoggedInUser().getName(), session);
            f.disableAutoPrint(disableAutoPrint);
            f.setSendEmailHandler(new SendEmailHandlerImpl(this, session));
            f.show(getSupportFragmentManager(), FRAGMENT_PRINT_RECEIPT);
        }
    }

    void printReceipt(boolean kickDrawer, boolean billPrint) {
        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_PRINT_RECEIPT) == null) {
            FakeReceiptFragment f = FakeReceiptFragment.newInstance(getLoggedInUser().getName(), session);
            f.setSendEmailHandler(new SendEmailHandlerImpl(this, session));
            f.setShouldKickDrawer(kickDrawer);
            f.setShouldPrint(billPrint);
            f.show(getSupportFragmentManager(), FRAGMENT_PRINT_RECEIPT);
        }
    }


    void printDinerReceipt(EpicuriSessionDetail.Diner selectedDiner) {
        if (selectedDiner == null) {
            Toast.makeText(this, "No diners selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDiner.getOrders().length == 0) {
            Toast.makeText(this, "No items ordered.", Toast.LENGTH_SHORT).show();
            return;
        }

        int tableOrders = session.getTableDiner().getOrders().length;
        if (tableOrders > 0) {
            final EpicuriSessionDetail.Diner diner = selectedDiner;
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("Not all items have been assigned to guests.")
                    .setNegativeButton("Go back", null)
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (getSupportFragmentManager().findFragmentByTag(
                                    FRAGMENT_PRINT_RECEIPT) == null) {
                                FakeReceiptFragment f = FakeReceiptFragment.newInstance(
                                        getLoggedInUser().getName(),
                                        session, diner);
                                f.setSendEmailHandler(new SendEmailHandlerImpl(SessionActivity.this, session));
                                f.show(getSupportFragmentManager(), FRAGMENT_PRINT_RECEIPT);
                            }
                        }
                    }).create()
                    .show();
        } else {
            FakeReceiptFragment f = FakeReceiptFragment.newInstance(
                    getLoggedInUser().getName(),
                    session, selectedDiner);
            f.setSendEmailHandler(new SendEmailHandlerImpl(SessionActivity.this, session));
            f.show(getSupportFragmentManager(), FRAGMENT_PRINT_RECEIPT);
        }
    }

    boolean shouldFinish = false;
    @Override
    public void onReceiptPrinted() {
        if (session.isClosed()) shouldFinish = true;

        if(reprint && shouldFinish){
            finish();
        }

        if (mPayPrintAndClose) {
            mPayPrintAndClose = false;
            finish();
        }
    }

    @Override
    public void onReceiptPrintCancelled() {
        mPayPrintAndClose = false;
    }

    public void setTip(Double tip) {
        WebServiceTask task = new WebServiceTask(this, new EditSessionTipWebServiceCall(session.getId(), tip), true);
        task.execute();
    }

    @Override
    public void showTipDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_sessionpaymentsettings, null, false);
        final EditText tipAmount = (EditText) view.findViewById(R.id.tipPercentage_value);
        tipAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2, 1)});

        new AlertDialog.Builder(this)
                .setTitle("Set Tip")
                .setView(view)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Double tipAmountDouble = null;
                        if (!TextUtils.isEmpty(tipAmount.getText())) {
                            try {
                                tipAmountDouble = Double.parseDouble(tipAmount.getText().toString());
                            } catch (NumberFormatException e) {
                                new AlertDialog.Builder(SessionActivity.this)
                                        .setTitle("Cannot set tip")
                                        .setMessage("Percentage amount not recognised, please try again")
                                        .setNegativeButton("Cancel", null)
                                        .show();
                                return;
                            }
                        }
                        setTip(tipAmountDouble);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // http://stackoverflow.com/questions/5357455/limit-decimal-places-in-android-edittext
    public static class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero) + "}?((\\.[0-9]{0," + (digitsAfterZero) + "})?)");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            CharSequence checkText = TextUtils.concat(
                    dest.subSequence(0, dstart),
                    source.subSequence(start, end),
                    dest.subSequence(dend, dest.length()));

			Matcher matcher = mPattern.matcher(checkText);
			if(!matcher.matches()) return "";
			return null;
		}

	}

    private final HashSet<OnSessionChangeListener> listeners = new HashSet<OnSessionChangeListener>();
    private Iterator<OnSessionChangeListener> listenerIterator;

    @Override
    public void registerSessionListener(OnSessionChangeListener listener) {
        listeners.add(listener);
        if (null != session) {
            listener.onSessionChanged(session);
        }
    }

    @Override
    public void deRegisterSessionListener(OnSessionChangeListener listener) {
        listenerIterator = listeners.iterator();
        while (listenerIterator.hasNext()){
            if (listener.equals(listenerIterator.next())){
                listenerIterator.remove();
            }
        }
    }

    protected void kickDrawer(CashDrawerSelectDialog.OnDrawerKicked listener) {
        conditionalDrawerKick(this, printers, listener);
    }
}
