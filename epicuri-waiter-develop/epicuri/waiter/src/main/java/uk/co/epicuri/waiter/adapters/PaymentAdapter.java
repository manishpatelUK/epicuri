package uk.co.epicuri.waiter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.LocalSettings;

public class PaymentAdapter extends BaseAdapter {
	private static final String TAG = "EpicuriPaymentAdapter";
	public static final String PAYMENTSENSE_TYPE = "PAYMENTSENSE";
	public static final String PAYMENTSENSE_DISCOUNT_TYPE = "PAYMENTSENSE (GRATUITY)";

	private EpicuriSessionDetail session = null;
	private ArrayList<EpicuriAdjustment> payments = new ArrayList<EpicuriAdjustment>();
	private ArrayList<EpicuriAdjustment> discounts = new ArrayList<EpicuriAdjustment>();
	private IOnRefundListener listener;

	private String suggestedTip;

	private LayoutInflater inflater;

	private Diner selectedDiner;

	private Map<String,EpicuriAdjustmentType> adjustmentTypes;

	public void setPayments(ArrayList<EpicuriAdjustment> payments) {
	    this.payments.clear();
		this.payments = payments;
		discounts.clear();
		notifyDataSetChanged();
	}

    public interface IOnRefundListener {
		void onRefundClicked(View view, int position);
	}

	public PaymentAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		adjustmentTypes = LocalSettings.getInstance(context).getCachedRestaurant().getAdjustmentTypesLookup();
	}

	boolean combineSimilarItems = false;
	public void setCombineSimilarItems(boolean group){
		combineSimilarItems = group;
	}

	public void changeData(EpicuriSessionDetail session){

		payments.clear();
		discounts.clear();
		for(EpicuriAdjustment a: session.getAdjustments()){
			if(adjustmentTypes.containsKey(a.getTypeId()) && adjustmentTypes.get(a.getTypeId()).getType() == EpicuriAdjustmentType.TYPE_PAYMENT) {
				payments.add(a);
			} else {
				discounts.add(a);
			}
		}
		this.suggestedTip = String.format("Suggested Tip %s%%", session.getTipPercentageFormatted());
		this.session = session;
		notifyDataSetChanged();
	}

	public void setListener(IOnRefundListener listener) {
		this.listener = listener;
	}

	@Override
	public int getCount() {
		if(null == session) return 0;
		return payments.size() + discounts.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if(position < discounts.size()) return discounts.get(position);
		else if(position == discounts.size()) return suggestedTip;
		else return payments.get(position - discounts.size() - 1);
	}

	@Override
	public long getItemId(int position) {
		if(position < discounts.size()) return discounts.get(position).getId().hashCode();
		else if(position == discounts.size()) return 0;
		else return payments.get(position - discounts.size() - 1).getId().hashCode();
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		if(position < discounts.size()) return 0;
		else if(position == discounts.size()) return 2;
		else return 1;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == 2) {
			convertView = inflater.inflate(R.layout.row_subtotal, parent, false);
			convertView.findViewById(R.id.orderQuantity).setVisibility(View.INVISIBLE);
			StringBuilder deliveryText = new StringBuilder();
			if(null != session.getDeliveryCost()
					&& session.getDeliveryCost().isPositive()){
				deliveryText.append(String.format("Delivery cost: %s\n", LocalSettings.formatMoneyAmount(session.getDeliveryCost(), true)));
			}
			if(session.getSuggestedTipAmount().isPositive()){
				deliveryText.append(String.format("%sTip: %s\n", deliveryText, LocalSettings.formatMoneyAmount(session.getSuggestedTipAmount(), true)));
			}
			String totalString = session.isRefund() ? "Refund Total" : "Bill Total";
			if(session.isDeferred()) {
				deliveryText.append(String.format(totalString + ": %s", LocalSettings.formatMoneyAmount(session.getTotalBeforeDeferment(), true)));
			} else {
				deliveryText.append(String.format(totalString + ": %s", LocalSettings.formatMoneyAmount(session.getTotal(), true)));
			}
			((TextView) convertView.findViewById(R.id.orderTotal)).setText(deliveryText);
		} else {
			PaymentViewHolder vh;
			if (null == convertView) {
				convertView = inflater.inflate(R.layout.row_payment_item, parent, false);
				vh = new PaymentViewHolder();
				ButterKnife.inject(vh, convertView);
				convertView.setTag(vh);
			} else {
				vh = (PaymentViewHolder) convertView.getTag();
			}

			if (position < discounts.size()) {
				EpicuriAdjustment adjustment = discounts.get(position);
				String adjustmentName = adjustmentTypes.containsKey(adjustment.getTypeId()) ? adjustmentTypes.get(adjustment.getTypeId()).getName() : "Unknown Discount Type";
				vh.refund.setVisibility(View.GONE);
				vh.type.setText(adjustmentName);
				if (adjustment.getAmount() != null) {
					vh.amount.setText(String.format(Locale.UK, "- %s", LocalSettings.formatMoneyAmount(adjustment.getAmount(), true)));
				} else {
					vh.amount.setText(String.format(Locale.UK, "- %.0f%%", adjustment.getPercentage()));
				}
				if(adjustment.getDefermentInfo() != null) {
					vh.info.setText(adjustment.getDefermentInfo());
				}
			} else {
				EpicuriAdjustment payment = payments.get(position - discounts.size() - 1);
				String adjustmentName = adjustmentTypes.containsKey(payment.getTypeId()) ? adjustmentTypes.get(payment.getTypeId()).getName() : "Unknown Payment Type";
				vh.type.setText(adjustmentName);
				vh.refund.setVisibility(adjustmentName.equals(PAYMENTSENSE_TYPE) ? View.VISIBLE :
						View.GONE);
				vh.amount.setText(LocalSettings.formatMoneyAmount(payment.getAmount(), true));
				vh.refund.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View view) {
						if (listener != null) listener.onRefundClicked(view, position);
					}
				});

				if (payment.getStripeCharge() != null){
                    EpicuriAdjustment.StripeCharge stripe = payment.getStripeCharge();
				    @SuppressLint("DefaultLocale")
                    String info = String.format("**** **** **** %s Expiry: %d/%d", stripe.getLast4digits(), stripe.getExpMonth(), stripe.getExpYear());
				    vh.info.setText(info);
                } else {
				    vh.info.setText("");
                }
			}
		}
		return convertView;
	}
	
	public static class PaymentViewHolder {
		@InjectView(R.id.amount)
		TextView amount;
		@InjectView(R.id.type)
		TextView type;
		@InjectView(R.id.refund)
		Button refund;
		@InjectView(R.id.info)
        TextView info;
	}
}
