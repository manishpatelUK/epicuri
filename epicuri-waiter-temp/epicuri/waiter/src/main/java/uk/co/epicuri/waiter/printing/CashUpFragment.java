package uk.co.epicuri.waiter.printing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.OnCashUpPrintListener;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterRedirectLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriCashUp;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriPrintRedirect;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.webservice.SendCashUpWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class CashUpFragment extends DialogFragment {

    private final static int LOADER_PRINTERS = 1;
    private final static int LOADER_PRINTER_REDIRECTS = 2;

    private static final String EXTRA_WAITER_NAME = "waiterName";
    private static final String ARG_CASH_UP = "cashUp";
    private static final String ARG_FROM_DATE = "fromDate";
    private static final String ARG_TO_DATE = "toDate";
    private static final String ARG_SHOW_CASH_UP = "showCashUp";
    private static final String ARG_SIMULATED = "simulated";

    private LinearLayout mLayout;

    private EpicuriRestaurant restaurant;

    private Map<String, EpicuriPrintRedirect> printerRedirects;
    private Map<String, EpicuriMenu.Printer> printers;

    private String waiterName;
    private EpicuriCashUp cashUp;
    private OnCashUpPrintListener listener;
    private LayoutInflater inflater;
    private ViewHolder vh = new ViewHolder();
    private TableLayout itemsContainer;

    public static CashUpFragment newInstance(String waiterName, EpicuriCashUp cashUp) {
        return newInstance(waiterName, cashUp, null, null, false);
    }

    public static CashUpFragment newInstance(String waiterName, EpicuriCashUp cashUp, Date fromDate, Date toDate, boolean showCashUp) {
        CashUpFragment fragment = new CashUpFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_WAITER_NAME, waiterName);
        args.putParcelable(ARG_CASH_UP, cashUp);
        if (null != fromDate) {
            args.putLong(ARG_FROM_DATE, fromDate.getTime());
        }
        if (null != toDate) {
            args.putLong(ARG_TO_DATE, toDate.getTime());
            args.putBoolean(ARG_SIMULATED, true);
            args.putBoolean(ARG_SHOW_CASH_UP, showCashUp);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnCashUpPrintListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnCashUpPrintListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cashUp = getArguments().getParcelable(ARG_CASH_UP);
        waiterName = getArguments().getString(EXTRA_WAITER_NAME);
        inflater = LayoutInflater.from(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.cashup_title);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_cashup, container, false);

        mLayout = view.findViewById(R.id.cashup);

        Button printButton = view.findViewById(R.id.print_button);
        printButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != printers && null != printerRedirects) {
                    printForReal();
                }
            }
        });
        Button dismissButton = view.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button sendEmail = view.findViewById(R.id.send_email);
        TextView cashupHeader = view.findViewById(R.id.cashup_header);
        TextView cashupFooter = view.findViewById(R.id.cashup_footer);
        if(getArguments().getBoolean(ARG_SIMULATED)){
            sendEmail.setVisibility(View.GONE);
            cashupHeader.setText(R.string.cashup_x);
            cashupFooter.setText(R.string.cashup_x);
        }else {
            sendEmail.setVisibility(View.VISIBLE);
            cashupHeader.setText(R.string.cashup_z);
            cashupFooter.setText(R.string.cashup_z);
        }
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getArguments() != null) {
                    final long fromDate = getArguments().getLong(ARG_FROM_DATE);
                    final long toDate = getArguments().getLong(ARG_TO_DATE);
                    final boolean simulated = getArguments().getBoolean(ARG_SIMULATED);
                    SendCashUpWebServiceCall sendCashUpWebServiceCall;
                    if (simulated) {
                        sendCashUpWebServiceCall = new SendCashUpWebServiceCall(
                                cashUp.getId(),
                                fromDate,
                                toDate);
                    } else {
                        sendCashUpWebServiceCall = new SendCashUpWebServiceCall(
                                cashUp.getId());
                    }

                    WebServiceTask task = new WebServiceTask(getContext(), sendCashUpWebServiceCall);
                    task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                        @Override
                        public void onSuccess(int code, String response) {
                            if (code == 200) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Email sent")
                                        .setMessage("An email has been sent to the designated internal email address for this Epicuri account.")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                CashUpFragment.this.dismiss();
                                            }
                                        }).show();
                            } else {
                                Toast.makeText(getContext(), "Error while sending email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    task.setIndicatorText("Sending email");
                    task.execute();
                }
            }
        });
        Button cashupButton = view.findViewById(R.id.cashup_button);
        if (getArguments().getBoolean(ARG_SHOW_CASH_UP)) {
            cashupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long d = getArguments().getLong(ARG_FROM_DATE);
                    Date fromDate = 0 < d ? new Date(d) : null;

                    d = getArguments().getLong(ARG_TO_DATE);
                    Date toDate = 0 < d ? new Date(d) : null;

                    listener.onCashUp(fromDate, toDate, false, false);
                }
            });
        } else {
            cashupButton.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        restaurant = LocalSettings.getInstance(getActivity()).getCachedRestaurant();

        getLoaderManager().initLoader(LOADER_PRINTERS, null, printerCallbacks);

        getLoaderManager().initLoader(LOADER_PRINTER_REDIRECTS, null, printerRedirectsCallback);

        updateCashup();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    final LoaderManager.LoaderCallbacks printerCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriMenu.Printer>>() {
        @Override
        public Loader<ArrayList<EpicuriMenu.Printer>> onCreateLoader(int i, Bundle bundle) {
            return new OneOffLoader<ArrayList<EpicuriMenu.Printer>>(getActivity(), new PrinterLoaderTemplate());
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader, ArrayList<EpicuriMenu.Printer> result) {
            if (null == result) {
                Toast.makeText(getActivity(), "Cannot load printers", Toast.LENGTH_SHORT).show();
                return;
            }
            printers = new HashMap<>(result.size());
            for (EpicuriMenu.Printer p : result) {
                printers.put(p.getId(), p);
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<EpicuriMenu.Printer>> arrayListLoader) {
            // don't care
        }
    };

    final LoaderManager.LoaderCallbacks printerRedirectsCallback = new LoaderManager.LoaderCallbacks<ArrayList<EpicuriPrintRedirect>>() {
        @Override
        public Loader<ArrayList<EpicuriPrintRedirect>> onCreateLoader(int i, Bundle bundle) {
            return new OneOffLoader<ArrayList<EpicuriPrintRedirect>>(getActivity(), new PrinterRedirectLoaderTemplate());
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<EpicuriPrintRedirect>> arrayListLoader, ArrayList<EpicuriPrintRedirect> result) {
            if (null == result) {
                Toast.makeText(getActivity(), "Cannot load printers", Toast.LENGTH_SHORT).show();
                return;
            }
            printerRedirects = new HashMap<>(result.size());
            for (EpicuriPrintRedirect p : result) {
                printerRedirects.put(p.getSourcePrinter().getId(), p);
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<EpicuriPrintRedirect>> arrayListLoader) {
            // don't care
        }
    };


    private void printForReal() {

        if (null == printers || null == printerRedirects) {
            Toast.makeText(getActivity(), "Printers not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        EpicuriMenu.Printer printer;
        EpicuriPrintRedirect redirect = printerRedirects.get(restaurant.getBillingPrinterId());
        if (null != redirect) {
            printer = redirect.getDestinationPrinter();
        } else {
            printer = printers.get(restaurant.getBillingPrinterId());
        }
        if (null == printer) {
            Toast.makeText(getActivity(), "Printer not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(mLayout.getWidth(),
                mLayout.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        mLayout.draw(c);

        PrintBitmapService.startActionPrintCashup(getActivity(), bitmap, printer);

        dismiss();
    }

    private void updateCashup() {
        if (null == restaurant) {
            return;
        }
//		iv.setImageBitmap(buildReceipt((EpicuriSessionDetail) getArguments().getParcelable(GlobalSettings.EXTRA_SESSION), false, restaurant, waiterName));
        String vatLabel = restaurant.getVatLabel();

        TextView tv;
        View view = getView();

        StringBuffer sb;

        sb = new StringBuffer(restaurant.getName() + "\n")
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

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.UK);

        setText(view, R.id.printedBy, "Printed by " + waiterName);
//        setText(view, R.id.printDate, df.format(new Date()));
        setText(view, R.id.printDate, "");
        setText(view, R.id.dateRange, String.format("%s    to    %s",
                df.format(cashUp.getStartTime()),
                df.format(cashUp.getEndTime())
        ));


        itemsContainer = (TableLayout) view.findViewById(R.id.itemsContainer_layout);
        itemsContainer.removeAllViews();

        Map<String, Double> cashLookups = cashUp.getReport();
        Map<String, Double> refundReport = cashUp.getRefundReport();
        Map<String, Double> refundPaymentsReport = cashUp.getRefundPaymentReport();

        addTitle("Sales Summary");
        addLabel("(after item adjustments, before bill adjustments & refunds)");
        addQuantityValue("On Premise", cashLookups.get("SeatedSessionsCount"), cashLookups.get("SeatedSessionsValue"), false);
        addQuantityValue("Takeaway Sessions", cashLookups.get("TakeawaySessionsCount"), cashLookups.get("TakeawaySessionsValue"), false);

        addGap();
        addQuantityValue("Unpaid On Premise Sessions", cashLookups.get("VoidSeatedSessionCount"), cashLookups.get("VoidSeatedSessionValue"), false);
        addQuantityValue("Unpaid Takeaways", cashLookups.get("VoidTakeawaySessionCount"), cashLookups.get("VoidTakeawaySessionValue"), false);
        addValue("Total Unpaid", cashLookups.get("VoidValue"), true);
        addGap();
        addQuantityValue("Food Items", cashLookups.get("FoodCount"), cashLookups.get("FoodValue"), false);
        addQuantityValue("Drink Items", cashLookups.get("DrinkCount"), cashLookups.get("DrinkValue"), false);
        addQuantityValue("Other Items", cashLookups.get("OtherCount"), cashLookups.get("OtherValue"), false);
        if (cashLookups.containsKey("TotalDelivery")) {
            addValue("Delivery Charges", cashLookups.get("TotalDelivery"), false);
        }
        addGap();

        Map<String, Double> adjustments;

        adjustments = cashUp.getItemAdjustmentLossReport();
        if (null != adjustments && !adjustments.isEmpty()) {
            addTitle("Item Adjustments");
            for (String key : adjustments.keySet()) {
                Double value = adjustments.get(key);
                if (null == value || value == 0) continue;
                addValue(key, value, false);
            }
            addGap();
        }
        addQuantityValue("Total Sales",
                cashLookups.get("SeatedSessionsCount") + cashLookups.get("TakeawaySessionsCount"),
                cashLookups.get("TotalSales"), true);
        addLabel("(after item adjustment, before bill adjustments)");
        addGap();

        adjustments = cashUp.getAdjustmentReport();
        if (null != adjustments && !adjustments.isEmpty()) {
            addTitle("Bill Adjustments");
            for (String key : adjustments.keySet()) {
                Double value = adjustments.get(key);
                if (null == value || value == 0) continue;
                addValue(key, value, false);
            }
            addGap();
        }

        addValue("Total Adjustments", cashLookups.get("TotalAdjustments"), true);
        if (null != refundPaymentsReport && !refundPaymentsReport.isEmpty()) {
            addGap();
            addTitle("Refund Summary");
            for (String key : refundPaymentsReport.keySet()) {
                Double value = refundPaymentsReport.get(key);
                if (null == value || value == 0) continue;
                addValue(key, value, false);
            }
            addGap();
            addValue("Total Refunds", refundReport.get("RefundSessionsValue"), true);
            /*addSubtitle("Of which");
            addValue("VAT", refundReport.get("VATValue"), false);*/
        }

        addGap();
        addTitle(vatLabel + " Rate Analysis");
        addValue("Total Sales (after adjustments & refunds)", cashLookups.get("GrossValue"), false);
        addValue(String.format("Total %s Charged", vatLabel), cashLookups.get("VATValue"), false);
        addValue("Net Sales", cashLookups.get("NetValue"), false);
        addGap();

        adjustments = cashUp.getPaymentReport();
        if (null != adjustments && !adjustments.isEmpty()) {
            addTitle("Payment Summary");
            for (String key : adjustments.keySet()) {
                Double value = adjustments.get(key);
                if (null == value || value == 0) continue;
                addValue(key, value, false);
            }
            addGap();
        }
        addSubtitle("Of which");
        addValue("Tips", cashLookups.get("TotalTip"), false);
        addValue("Overpayments", cashLookups.get("OverPayments"), false);
        addGap();
        addValue("Total Payments (inc overpayments/tips)", cashLookups.get("Payments"), true);
    }

    private void addTitle(String title) {
        View row = inflater.inflate(R.layout.row_cashup_title, itemsContainer, false);
        ButterKnife.inject(vh, row);
        vh.t1.setText(title);
        itemsContainer.addView(row);
    }

    private void addGap() {
        View row = inflater.inflate(R.layout.row_cashup_title, itemsContainer, false);
        ButterKnife.inject(vh, row);
        itemsContainer.addView(row);
    }

    private void addLabel(String label) {
        View row = inflater.inflate(R.layout.row_cashup_comment, itemsContainer, false);
        ButterKnife.inject(vh, row);
        vh.t1.setText(label);
        itemsContainer.addView(row);
    }

    private void addQuantityValue(String title, Double qty, Double value, boolean bold) {
        if (null == value) return;

        View row = inflater.inflate(R.layout.row_cashup_entry, itemsContainer, false);
        ButterKnife.inject(vh, row);
        vh.t1.setText(title);
        vh.t2.setText(String.format(Locale.UK, "%,.0f / %s", qty, LocalSettings.formatMoneyAmount(value, true)));
        if (bold) {
            vh.t1.setTypeface(null, Typeface.BOLD);
            vh.t2.setTypeface(null, Typeface.BOLD);
        }
        itemsContainer.addView(row);
    }

    private void addQuantity(String title, Double qty, boolean bold) {
        if (null == qty) return;
        View row = inflater.inflate(R.layout.row_cashup_entry, itemsContainer, false);
        ButterKnife.inject(vh, row);
        vh.t1.setText(title);
        vh.t2.setText(String.format(Locale.UK, "%,.0f", qty));
        if (bold) {
            vh.t1.setTypeface(null, Typeface.BOLD);
            vh.t2.setTypeface(null, Typeface.BOLD);
        }
        itemsContainer.addView(row);
    }

    private void addSubtitle(String subtitle){
        View row = inflater.inflate(R.layout.row_cashup_subtitle, itemsContainer, false);
        ButterKnife.inject(vh, row);
        vh.t1.setText(subtitle);
        itemsContainer.addView(row);
    }
    private void addValue(String title, Double value, boolean bold) {
        if (null == value) return;
        View row = inflater.inflate(R.layout.row_cashup_entry, itemsContainer, false);
        ButterKnife.inject(vh, row);
        vh.t1.setText(title);
        if(value >= 0) {
            vh.t2.setText(String.format("%s", LocalSettings.formatMoneyAmount(value, true)));
        } else {
            vh.t2.setText("-"+String.format("%s", LocalSettings.formatMoneyAmount(Math.abs(value), true)));
        }
        if (bold) {
            vh.t1.setTypeface(null, Typeface.BOLD);
            vh.t2.setTypeface(null, Typeface.BOLD);
        }
        itemsContainer.addView(row);
    }

    static class ViewHolder {
        @InjectView(android.R.id.text1)
        TextView t1;
        @Optional
        @InjectView(android.R.id.text2)
        TextView t2;
    }


    private static void setText(View view, int id, CharSequence text) {
        ((TextView) view.findViewById(id)).setText(text);
    }
}
