package uk.co.epicuri.waiter.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.PaymentAdapter;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;

public class ForceCloseInfoDialog extends DialogFragment {
    @InjectView(R.id.payments_list) ListView paymentsListView;
    public static final String adjustmentsExtra = "ADJUSTMENTS";
    public static final String sessionExtra = "SESSION";
    OnForceCloseListener listener;
    ArrayList<EpicuriAdjustment> adjustments;
    EpicuriSessionDetail sessionDetail;
    private Map<String,EpicuriAdjustmentType> adjustmentTypes;
    private PaymentAdapter paymentAdapter;
    public static ForceCloseInfoDialog newInstance(EpicuriSessionDetail session) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(adjustmentsExtra, session.getAdjustments());
        args.putParcelable(sessionExtra, session);
        ForceCloseInfoDialog fragment = new ForceCloseInfoDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        sessionDetail = getArguments().getParcelable(sessionExtra);
        adjustments = sessionDetail != null ? sessionDetail.getAdjustments() : new ArrayList<EpicuriAdjustment>();
        super.onCreate(savedInstanceState);
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) return super.onCreateDialog(savedInstanceState);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_force_close_info, null);
        ButterKnife.inject(this, view);
        adjustmentTypes = LocalSettings.getInstance(getContext()).getCachedRestaurant().getAdjustmentTypesLookup();
        ArrayList<EpicuriAdjustment> payments = new ArrayList<>();
        for (EpicuriAdjustment adjustment : adjustments){
            if(adjustmentTypes.containsKey(adjustment.getTypeId()) &&
                    adjustmentTypes.get(adjustment.getTypeId()).getType() == EpicuriAdjustmentType.TYPE_PAYMENT) {
                payments.add(adjustment);
            }
        }
        paymentAdapter = new PaymentAdapter(getActivity());
        paymentAdapter.changeData(sessionDetail);
        paymentAdapter.setPayments(payments);
        paymentsListView.setAdapter(paymentAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Force close info")
                .setView(view)
                .setPositiveButton("Apply Discount", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(listener != null) listener.onApplyDiscount();
                    }
                })
                .setNegativeButton("Remove payments", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(listener != null) listener.onRemovePayments();
                    }
                });

        return builder.create();
    }

    public void setListener(OnForceCloseListener listener) {
        this.listener = listener;
    }

    public  interface OnForceCloseListener{
        void onApplyDiscount();
        void onRemovePayments();
    }
}
