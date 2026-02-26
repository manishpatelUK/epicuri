package uk.co.epicuri.waiter.printing;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.money.Money;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.BitmapCallback;
import uk.co.epicuri.waiter.interfaces.SendEmailHandler;
import uk.co.epicuri.waiter.interfaces.OnReceiptPrintedListener;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterRedirectLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriOrderItem.GroupedOrderItem;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.FloorplanBackgroundCache;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class FakeReceiptFragment extends DialogFragment {

    private final static int LOADER_PRINTERS = 1;
    private final static int LOADER_PRINTER_REDIRECTS = 2;
    public static final int WRITE_PERMISSION = 8;

    public static final String EXTRA_WAITER_NAME = "waiterName";
    private static final long TIMEOUT = 1000;

    private LinearLayout receiptLayout;
//	private CheckBox receiptMethodCheckBox;

    private View countdownContainer;
    private TextView countdownLabel;
    private CountDownTimer timer;

    private Spinner printerChooser;
    private EpicuriRestaurant restaurant;
    private EpicuriSessionDetail session;

    private EpicuriSessionDetail.Diner diner;
    private Map<String, EpicuriPrintRedirect> printerRedirects;

    private Map<String, EpicuriMenu.Printer> printers;
    private String waiterName;
    private double billPrintFontSize = 30;
    private View view;

    private SendEmailHandler sendEmailHandler;

    private static final String DINER_EXTRA = "dinerExtra";
    private boolean printImmediately;
    private boolean disableAutoPrint;

    public void setShouldKickDrawer(boolean shouldKickDrawer) {
        this.shouldKickDrawer = shouldKickDrawer;
    }

    private boolean shouldKickDrawer;
    private boolean shouldPrint = true;
    int totalSizeEnlarge = 8;

    void kickDrawer(EpicuriMenu.Printer printer){
        PrintUtil.kickDrawer(getContext(), printer);
    }

    public static final FakeReceiptFragment newInstance(String waiterName, EpicuriSessionDetail session) {
        FakeReceiptFragment fragment = new FakeReceiptFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_WAITER_NAME, waiterName);
        if (null != session) {
            args.putParcelable(GlobalSettings.EXTRA_SESSION, session);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static final FakeReceiptFragment newInstance(String waiterName, EpicuriSessionDetail
            session, EpicuriSessionDetail.Diner diner) {
        FakeReceiptFragment fragment = new FakeReceiptFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_WAITER_NAME, waiterName);
        if (null != session) {
            args.putParcelable(GlobalSettings.EXTRA_SESSION, session);
        }
        args.putParcelable(DINER_EXTRA, diner);
        fragment.setArguments(args);
        return fragment;
    }

    public void setSendEmailHandler(SendEmailHandler sendEmailHandler){
        this.sendEmailHandler = sendEmailHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (getContext() != null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
            }
        }

        restaurant = LocalSettings.getInstance(getActivity()).getCachedRestaurant();

        session = getArguments().getParcelable(GlobalSettings.EXTRA_SESSION);
        diner = getArguments().getParcelable(DINER_EXTRA);

        if(restaurant != null) {
            billPrintFontSize = restaurant.getBillPrintFontSize();
        }
        else {
            LocalSettings localSettings = LocalSettings.getInstance(getContext());
            if(localSettings.getCachedRestaurant() != null) {
                billPrintFontSize = localSettings.getCachedRestaurant().getBillPrintFontSize();
            }
        }
        boolean reprint = false;
        if(restaurant != null){
            reprint = Boolean.valueOf(restaurant.getRestaurantDefault(EpicuriRestaurant.DEFAULT_REPRINT_BILL, "false"));
        }
        if((session != null)  //Quick order session
                && !session.isClosed()
                && !disableAutoPrint && (session.isAdHoc() || session.isRefund())
        ) {
            printImmediately = true;
        }

        if(session != null && session.isClosed() && !disableAutoPrint && reprint && !session.isAdHoc() && !session.isRefund()){
            printImmediately = true;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        waiterName = getArguments().getString(EXTRA_WAITER_NAME);
        view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_printpreview, null, false);
        ButterKnife.inject(this, view);
        receiptLayout = (LinearLayout) view.findViewById(R.id.receipt);
        ScrollView scrollview = (ScrollView) view.findViewById(R.id.scrollView);
        scrollview.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                cancelCountdown();
            }
        });

        printerChooser = view.findViewById(R.id.printer_spinner);
        printerChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EpicuriMenu.Printer printer = (EpicuriMenu.Printer) printerChooser.getSelectedItem();
                if(printer != null) {
                    getActivity()
                            .getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE)
                            .edit()
                            .putString(GlobalSettings.PREF_KEY_RECEIPT_PRINTER, printer.getId())
                            .apply();
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        countdownLabel = view.findViewById(R.id.printCountdown);

        countdownContainer = view.findViewById(R.id.printCountdownContainer);
        if((session != null && (session.isAdHoc() || session.isRefund())) ||
                !(restaurant != null && !restaurant.isEmailReceiptsEnabled()) &&
                (null != countdownContainer)) {
            countdownContainer.setVisibility(View.GONE);
        }

        countdownContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelCountdown();
            }
        });

        String dialogTitle = "Print & Kick Drawer at location...";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(dialogTitle)
                .setView(view)
                .setPositiveButton("Print Copy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null != printers && null != printerRedirects) {

                            printForReal();
                        }
                    }
                })
                .setNegativeButton("Save", null);
        if (LocalSettings.getInstance(getActivity()).getCachedRestaurant().isEmailReceiptsEnabled() &&
                sendEmailHandler !=null)
            builder.setNeutralButton(getString(R.string.send_via_email), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (sendEmailHandler !=null)
                        sendEmailHandler.sendEmail();
                }
            });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        restaurant = LocalSettings.getInstance(getActivity()).getCachedRestaurant();

        if (session != null && !session.isAdHoc() && !session.isRefund() && restaurant.isEmailReceiptsEnabled() && sendEmailHandler != null) {
            countdownLabel.setVisibility(View.GONE);
        }

        getLoaderManager().initLoader(LOADER_PRINTERS, null, new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
            @Override
            public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i, Bundle bundle) {
                return new OneOffLoader<ArrayList<EpicuriMenu.Printer>>(getActivity(), new PrinterLoaderTemplate());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader, ArrayList<EpicuriMenu.Printer> result) {
                if (null == result) return; // failed to get printers

                printers = new HashMap<>(result.size());
                LocalSettings settings = LocalSettings.getInstance(getContext());

                EpicuriRestaurant restaurant = settings.getCachedRestaurant();
                List<EpicuriMenu.Printer> validPrinters = new ArrayList<>();
                if(restaurant != null) {
                    for(EpicuriMenu.Printer printer : result) {
                        if(restaurant.getConnectedCashDrawers().contains(printer.getId())) {
                            validPrinters.add(printer);
                        }
                    }
                } else {
                    validPrinters = result;
                }

                printerChooser.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, validPrinters));
                String previousPrinterId = getActivity().getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE).getString(GlobalSettings.PREF_KEY_RECEIPT_PRINTER, "");

                boolean selected = false;
                for (int i = 0; i < validPrinters.size(); i++) {
                    EpicuriMenu.Printer p = validPrinters.get(i);
                    printers.put(p.getId(), p);
                    if (p.getId().equals(previousPrinterId)) {
                        printerChooser.setSelection(i);
                        selected = true;
                    }
                }

                if (!selected) {
                    // current printer choice isn't there any more, revert to restaurant setting
                    previousPrinterId = restaurant.getBillingPrinterId();

                    for (int i = 0; i < validPrinters.size(); i++) {
                        if (validPrinters.get(i).getId().equals(previousPrinterId)) {
                            printerChooser.setSelection(i);
                            break;
                        }
                    }
                }

                if (printerRedirects != null && printImmediately){
                    startCountdown();
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {
                // don't care
            }
        });

        getLoaderManager().initLoader(LOADER_PRINTER_REDIRECTS, null, new LoaderManager.LoaderCallbacks<ArrayList<EpicuriPrintRedirect>>() {
            @Override
            public Loader<ArrayList<EpicuriPrintRedirect>> onCreateLoader(int i, Bundle bundle) {
                return new OneOffLoader<ArrayList<EpicuriPrintRedirect>>(getActivity(), new PrinterRedirectLoaderTemplate());
            }

            @Override
            public void onLoadFinished(Loader<ArrayList<EpicuriPrintRedirect>> arrayListLoader, ArrayList<EpicuriPrintRedirect> result) {
                if (null == result) return;
                printerRedirects = new HashMap<>(result.size());
                for (EpicuriPrintRedirect p : result) {
                    printerRedirects.put(p.getSourcePrinter().getId(), p);
                }

                if (printers != null && printImmediately){
                    startCountdown();
                }
            }

            @Override
            public void onLoaderReset(Loader<ArrayList<EpicuriPrintRedirect>> arrayListLoader) {
                // don't care
            }
        });

        updateReceipt();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof OnReceiptPrintedListener) {
            ((OnReceiptPrintedListener) getActivity()).onReceiptPrintCancelled();
        }
        cancelCountdown();
    }

    private void startCountdown() {
        timer = new CountDownTimer(TIMEOUT, 100) {

            @Override
            public void onTick(long millisUntilFinished) {
                Activity activity = getActivity();
                if (activity != null && isAdded() && null != countdownLabel) {
                    countdownLabel.setText(getString(R.string.printCountdown, millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                printForReal();
            }
        };

        timer.start();
    }

    private void cancelCountdown() {
        if (null == timer) return;
        timer.cancel();
        timer = null;
        if (null != countdownContainer) countdownContainer.setVisibility(View.GONE);
    }

    private void printForReal() {
        cancelCountdown();

        FragmentActivity activity = getActivity();
        if(activity == null) {
            return;
        }

        if (null == printers || null == printerRedirects) {
            Toast.makeText(activity, "Printers not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // get selected printer from spinner
        EpicuriMenu.Printer printer = (EpicuriMenu.Printer) printerChooser.getSelectedItem();

        if(shouldKickDrawer) kickDrawer(printer);
        if(printer != null) {
            activity
                    .getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(GlobalSettings.PREF_KEY_RECEIPT_PRINTER, printer.getId())
                    .apply();
        }

        if(!shouldPrint){
            dismiss();
            return;
        }
        // check for a redirect
        EpicuriPrintRedirect redirect = printerRedirects.get(printer.getId());
        if (null != redirect) {
            printer = redirect.getDestinationPrinter();
        }

        if (activity instanceof OnReceiptPrintedListener) {
            ((OnReceiptPrintedListener) activity).onReceiptPrinted();
        }

        if (null == printer) {
            if(!activity.isFinishing()) Toast.makeText(activity, "Printer not found", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(receiptLayout.getWidth(),
                receiptLayout.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        receiptLayout.draw(c);

        String id = session.getReadableId().equals("") ? session.getId() : session.getReadableId();
        Boolean isSeated = session.getType().equals(EpicuriSessionDetail.SessionType.DINE);
        PrintBitmapService.startActionPrintReceipt(activity, id, session.getId(),
                bitmap, printer, isSeated);
        try {
            dismissAllowingStateLoss();
        } catch (Exception ex) {
            Log.e("FakeRFrag","Error in dismissal",ex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_PERMISSION){
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission denied, please allow required permission to continue printing uninterrupted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateReceipt() {
        if (null == restaurant) {
            return;
        }

        buildReceipt(session, diner, restaurant, waiterName);

        String receiptURL = restaurant.getReceiptImageURL();
        if (null != receiptURL) {
            loadReceiptImage(restaurant.getReceiptImageURL(), getActivity());
        }
    }


    private void setText(View view, int id, CharSequence text) {
        TextView viewById = view.findViewById(id);
        if(viewById == null) return;

        viewById.setText(text);
        if(id == R.id.total){
            viewById.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)billPrintFontSize*2);
        }else {
            viewById.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)billPrintFontSize);
        }
    }

    private void boldify(View view, int id) {
        TextView viewById = view.findViewById(id);
        if(viewById == null) return;

        viewById.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void upSize(View view, int id, float factor) {
        TextView viewById = view.findViewById(id);
        if(viewById == null) return;
        viewById.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)billPrintFontSize*factor);
    }

    private void borderify(View view, int id) {
        TextView viewById = view.findViewById(id);
        if(viewById == null) return;

        Context context = getContext();
        if(context != null) {
            viewById.setBackground(ContextCompat.getDrawable(context, R.drawable.blackbox));
            viewById.setBackgroundColor(getResources().getColor(R.color.lightgray));
        }
    }

    private void buildReceipt(EpicuriSessionDetail session, EpicuriSessionDetail.Diner diner,
                              EpicuriRestaurant restaurant, CharSequence waiterName) {

        StringBuffer sb =  new StringBuffer(restaurant.getName() + "\n")
                .append(restaurant.getAddress().toString());

        if (null != restaurant.getPhoneNumber()) {
            sb.append(restaurant.getPhoneNumber()).append("\n");
        }
        setText(view, R.id.restaurantAddress, sb);

        if (TextUtils.isEmpty(restaurant.getEmail())) {
            view.findViewById(R.id.restaurantEmail).setVisibility(View.GONE);
        } else {
            setText(view, R.id.restaurantEmail, restaurant.getEmail());
        }
        if (TextUtils.isEmpty(restaurant.getWebsite())) {
            view.findViewById(R.id.restaurantWeb).setVisibility(View.GONE);
        } else {
            setText(view, R.id.restaurantWeb, restaurant.getWebsite());
        }

        String pricePrefix = session.isRefund() ? "-" : "";

        if (session.getTables() != null && session.getTables().length > 0 && session.getTablesString() != null) {
            setText(view, R.id.tableName, "Table: " + session.getTablesString());
        } else if(session.getTables() != null && session.getTables().length == 0) {
            setText(view, R.id.tableName, getTableLabelForTabOrQO(session));
            boldify(view, R.id.tableName);
            upSize(view, R.id.tableName, 1.5f);
        } else {
            setText(view, R.id.tableName, "Bill Receipt");
        }
        setText(view, R.id.dueDate, new SimpleDateFormat("dd-MM-yyyy", Locale.UK).format(new Date()));
        setText(view, R.id.dueTime, new SimpleDateFormat("HH:mm", Locale.UK).format(new Date()));
        setText(view, R.id.servedBy, "Served by " + waiterName);
        setText(view, R.id.sessionId, getString(R.string.receipt_session_id, session.getReadableId().equals("") ? session.getId() : session.getReadableId()));
        if (session.getType().equals(EpicuriSessionDetail.SessionType.DINE)) {
            view.findViewById(R.id.deliveryRow).setVisibility(View.GONE);
            view.findViewById(R.id.takeawayDetail).setVisibility(View.GONE);
            view.findViewById(R.id.guestCount).setVisibility(View.VISIBLE);
            if ((session.isAdHoc() || session.isRefund()) || diner != null) {
                setText(view, R.id.guestCount, getString(R.string.receipt_guests, 1));
            } else {
                setText(view, R.id.guestCount, getString(R.string.receipt_guests, session.getDiners().size() - 1));
            }

            if (session.getSuggestedTipAmount().isPositive()) {
                view.findViewById(R.id.tipRow).setVisibility(View.VISIBLE);
                setText(view, R.id.tip, LocalSettings.formatMoneyAmount(session.getFudgedReceiptSuggestedTipAmount(), true));
                setText(view, R.id.tipLabel, String.format("Gratuity (%s%%):", session.getFudgedReceiptTipPercentage()));
            } else {
                view.findViewById(R.id.tipRow).setVisibility(View.GONE);
            }
        } else {
            setText(view, R.id.guestCount, "");
            setText(view, R.id.phoneNumber, session.getDeliveryPhoneNumber());
            setText(view, R.id.partyName, session.getName());
            view.findViewById(R.id.takeawayDetail).setVisibility(View.VISIBLE);

            if (session.getType().equals(EpicuriSessionDetail.SessionType.DELIVERY)) {
                setText(view, R.id.deliveryAddress, session.getDeliveryAddress().toString());
                view.findViewById(R.id.deliveryAddressRow).setVisibility(View.VISIBLE);
                if (null == session.getDeliveryCost()) {
                    view.findViewById(R.id.deliveryRow).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.deliveryRow).setVisibility(View.VISIBLE);
                    setText(view, R.id.deliveryCost, LocalSettings.formatMoneyAmount(session.getDeliveryCost(), true));
                }
                setText(view, R.id.tableName, "Takeaway: Delivery");
            } else {
                setText(view, R.id.tableName, "Takeaway: Collection");
                view.findViewById(R.id.deliveryAddressRow).setVisibility(View.GONE);
                view.findViewById(R.id.deliveryRow).setVisibility(View.GONE);
            }
            view.findViewById(R.id.tipRow).setVisibility(View.GONE);
        }

        ArrayList<GroupedOrderItem> groupedOrders = new ArrayList<>();
        List<EpicuriOrderItem> orders;
        Money subtotal, total, discount, vat;

        if (diner == null) {
            orders = session.getOrders();
            subtotal = session.getSubtotal();
            total = session.getFudgedReceiptTotal();
            discount = session.getDiscountTotal();
            vat = session.getVatTotal();
        } else {
            orders = Arrays.asList(diner.getObj_orders());
            subtotal = diner.getSubTotal();
            total = diner.getTotal();
            discount = diner.getDiscounts();
            vat = diner.getVat();
        }

        Collections.sort(orders, new Comparator<EpicuriOrderItem>() {

            @Override
            public int compare(EpicuriOrderItem lhs, EpicuriOrderItem rhs) {
                return lhs.getCourse().getOrdering() - rhs.getCourse().getOrdering();
            }

        });

        int numberOfItems = 0;
        for (EpicuriOrderItem o : orders) {
            if(o.getAdjustment() != null
                    && o.getAdjustment().getType() != null
                    && !o.getAdjustment().getType().isShowOnReceipt()) {
                continue;
            }
            boolean merged = false;

            for (GroupedOrderItem go : groupedOrders) {
                if (go.getOrderItem().isSameOrder(o, true)) {
                    go.mergeWith(o, true);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                groupedOrders.add(new GroupedOrderItem(o));
            }

            numberOfItems += o.getQuantity();
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        TableLayout itemsContainer = (TableLayout) view.findViewById(R.id.itemsContainer_layout);
        itemsContainer.removeAllViews();

        for (GroupedOrderItem item : groupedOrders) {
            TableRow row = (TableRow) inflater.inflate(R.layout.receipt_item, itemsContainer, false);

            StringBuilder orderString = new StringBuilder();
            StringBuilder itemThings = new StringBuilder(item.getItem().getName());
            for (ModifierValue v : item.getChosenModifiers()) {
                itemThings.append(" - ").append(v.getName());
            }

            orderString.append(String.format("%s", itemThings));

            setText(row, R.id.itemName, orderString);
            setText(row, R.id.itemQuantity, String.format(Locale.UK, "Qty: %d", item.getQuantity()));

            setText(row, R.id.itemPrice, pricePrefix + LocalSettings.formatMoneyAmount(item.getCalculatedPriceIncludingQuantity(), true));
            itemsContainer.addView(row);
        }

        // Printing items

        String orderString = session.isRefund() ? "You are refunded" : "You ordered";
        setText(view, R.id.numberOfItems, String.format(Locale.UK, orderString + " %d items today",numberOfItems));
        setText(view, R.id.subtotal, pricePrefix + LocalSettings.formatMoneyAmount(subtotal, true));
        setText(view, R.id.total, pricePrefix + LocalSettings.formatMoneyAmount(total, true));
        if(session.isRefund()) {
            setText(view, R.id.total_label, "Refund");
        }

        // Total and subtotal

        if (discount != null && !discount.isZero()) {
            setText(view, R.id.adjustments, "- " + LocalSettings.formatMoneyAmount(discount.abs(), true));
            view.findViewById(R.id.adjustmentsRow).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.adjustmentsRow).setVisibility(View.GONE);
        }

        // Discount section

        TableLayout paymentsContainer = (TableLayout) view.findViewById(R.id.paymentsContainer_layout);
        paymentsContainer.removeAllViews();

        Map<String, EpicuriAdjustmentType> adjustmentTypesLookup = restaurant.getAdjustmentTypesLookup();
        boolean paymentsMade = false;
        for (EpicuriAdjustment a : session.getAdjustments()) {
            EpicuriAdjustmentType adjustmentType = adjustmentTypesLookup.get(a.getTypeId());
            if (null != adjustmentType && adjustmentType.getType() == EpicuriAdjustmentType.TYPE_PAYMENT) {
                paymentsMade = true;
                TableRow row = (TableRow) inflater.inflate(R.layout.receipt_item, itemsContainer, false);

                setText(row, R.id.itemName, String.format("%s Payment", adjustmentType.getName()));
                setText(row, R.id.itemPrice, String.format("-%s", LocalSettings.formatMoneyAmount(a.getAmount(), true)));
                paymentsContainer.addView(row);
            }
        }
        if (!paymentsMade || diner != null) {
            paymentsContainer.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.dueAndChangeSection).setVisibility(View.VISIBLE);
            if (session.getRemainingTotal().isNegative()) {
                setText(view, R.id.outstanding, LocalSettings.formatMoneyAmount(0, true));
            } else {
                setText(view, R.id.outstanding, LocalSettings.formatMoneyAmount(session.getRemainingTotal(), true));
            }
            if (session.getChange().isPositive()) {
                view.findViewById(R.id.changeRow).setVisibility(View.VISIBLE);
                setText(view, R.id.change, LocalSettings.formatMoneyAmount(session.getChange(), true));
            }
        }

        if (TextUtils.isEmpty(restaurant.getVatCode())) {
            view.findViewById(R.id.vatBlock).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.vatBlock).setVisibility(View.VISIBLE);
            setText(view, R.id.vatLabel, String.format("(%s:", restaurant.getVatLabel()));
            setText(view, R.id.vatNumberLabel, String.format("%s:", restaurant.getVatNumberLabel()));
            setText(view, R.id.vatTotal, LocalSettings.formatMoneyAmount(vat, true) + ")");
            setText(view, R.id.vatNumber, restaurant.getVatCode());
        }

        String serviceChargeText = restaurant.getServiceChargeText();
        if (TextUtils.isEmpty(serviceChargeText)) {
            view.findViewById(R.id.serviceChargeText).setVisibility(View.GONE);
        } else {
            if(session.getTipPercentageFormatted().equals("0")) {
                view.findViewById(R.id.serviceChargeText).setVisibility(View.VISIBLE);
                setText(view, R.id.serviceChargeText, serviceChargeText);
            } else {
                view.findViewById(R.id.serviceChargeText).setVisibility(View.GONE);
            }
        }

        if (TextUtils.isEmpty(restaurant.getReceiptFooter())) {
            view.findViewById(R.id.receiptFooter).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.receiptFooter).setVisibility(View.VISIBLE);
            setText(view, R.id.receiptFooter, restaurant.getReceiptFooter().replace("\\n","\n"));
        }

        if (restaurant.isMewsEnabled()
                || restaurant.getReceiptType() == EpicuriRestaurant.RECEIPT_TYPE_HOTEL) {
            view.findViewById(R.id.hotelSection).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.hotelSection).setVisibility(View.GONE);
        }

        setToCorrectSize(R.id.subtotal_label,
                R.id.total_label,
                R.id.name_label,
                R.id.contact_label,
                R.id.address_label,
                R.id.delivery_label,
                R.id.adj_label,
                R.id.outstanding_label,
                R.id.change_label,
                R.id.hotel_name_label,
                R.id.room_label,
                R.id.signature_label);

    }

    private String getTableLabelForTabOrQO(EpicuriSessionDetail session) {
        // a quick order with a delivery location should populate table name, otherwise just use Tab
        List<String> deliveryLocations = new ArrayList<>();
        for(EpicuriOrderItem item : session.getOrders()) {
            if(item.getDeliveryLocation() != null
                    && item.getDeliveryLocation().length() > 0
                    && !deliveryLocations.contains(item.getDeliveryLocation())) {
                deliveryLocations.add(item.getDeliveryLocation());
            }
        }

        if(deliveryLocations.size() == 0) {
            return "Tab";
        } else {
            return "Order ID: " + deliveryLocations.get(0); // should only ever be size 1 on a QO
        }
    }

    private void setToCorrectSize(int... ids) {
        for(int id : ids) {
            TextView textView = view.findViewById(id);
            if(textView != null) {
                if (id == R.id.total_label){
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)billPrintFontSize*2);
                } else {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) billPrintFontSize);
                }
            }
        }
    }


    private Bitmap receiptImageBitmap;

    private void refreshReceiptImage() {
        ((ImageView) receiptLayout.findViewById(R.id.receiptLogo)).setImageBitmap(receiptImageBitmap);
    }

    private void loadReceiptImage(String url, Context context) {
        final FloorplanBackgroundCache cache = FloorplanBackgroundCache.getInstance(context);
        receiptImageBitmap = cache.getCachedBitmap(url, -1, -1);
        if (null != receiptImageBitmap) {
            refreshReceiptImage();
        } else {
            try {
                cache.downloadAndCacheBitmap(url, new BitmapCallback() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        receiptImageBitmap = bitmap;
                        refreshReceiptImage();
                    }
                }, -1, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void setShouldPrint(boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }

    public void disableAutoPrint(boolean disableAutoPrint) {
        this.disableAutoPrint = disableAutoPrint;
    }
}