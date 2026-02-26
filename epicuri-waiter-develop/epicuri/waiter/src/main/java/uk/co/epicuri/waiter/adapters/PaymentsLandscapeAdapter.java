package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;


public class PaymentsLandscapeAdapter extends
        RecyclerView.Adapter<PaymentsLandscapeAdapter.AdjustmentViewHolder> {

    public interface IAdjustmentListener {
        void onPaymentTypeSelected(EpicuriAdjustmentType paymentType);
        void onDiscountTypeSelected(EpicuriAdjustmentType adjustmentType);
    }

    private LayoutInflater inflater;
    private ArrayList<EpicuriAdjustmentType> adjustmentTypes;
    private IAdjustmentListener listener;

    public PaymentsLandscapeAdapter(Context context,
            ArrayList<EpicuriAdjustmentType> adjustmentTypes, IAdjustmentListener listener) {
        inflater = LayoutInflater.from(context);
        this.adjustmentTypes = adjustmentTypes;
        this.listener = listener;
    }

    @Override public AdjustmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AdjustmentViewHolder(inflater.inflate(R.layout.adjustment_item, parent, false));
    }

    @Override public void onBindViewHolder(AdjustmentViewHolder holder, int position) {
        holder.render(adjustmentTypes.get(position));
    }

    @Override public int getItemCount() {
        return adjustmentTypes.size();
    }

    public class AdjustmentViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.text) Button text;

        public AdjustmentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        public void render(EpicuriAdjustmentType adjustment) {
            text.setText(adjustment.getShortCode() != null ? adjustment.getShortCode() : adjustment.getName());
            int colorId = adjustment.getType() == EpicuriAdjustmentType.TYPE_PAYMENT ? R.color
                    .green : R.color.red;
            text.getBackground().setColorFilter(ContextCompat.getColor(text.getContext(), colorId),
                    PorterDuff.Mode.MULTIPLY);
        }

        @OnClick(R.id.text) void onItemClick() {
            if (listener == null) return;

            EpicuriAdjustmentType type = adjustmentTypes.get(getAdapterPosition());

            if (type.getType() == EpicuriAdjustmentType.TYPE_PAYMENT) {
                listener.onPaymentTypeSelected(type);
                return;
            }

            if (type.getType() == EpicuriAdjustmentType.TYPE_DISCOUNT)
                listener.onDiscountTypeSelected(type);
        }
    }
}
