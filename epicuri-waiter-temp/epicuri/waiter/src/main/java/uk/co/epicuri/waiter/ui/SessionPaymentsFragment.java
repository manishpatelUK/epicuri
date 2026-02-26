package uk.co.epicuri.waiter.ui;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.money.Money;

import java.math.BigDecimal;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriAdjustment;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.interfaces.OnSessionChangeListener;
import uk.co.epicuri.waiter.interfaces.PaymentActionHandler;
import uk.co.epicuri.waiter.interfaces.SessionContainer;
import uk.co.epicuri.waiter.adapters.PaymentAdapter;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.webservice.DeleteAdjustmentWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class SessionPaymentsFragment extends Fragment implements OnSessionChangeListener, AdapterView.OnItemClickListener, PaymentAdapter.IOnRefundListener {

	@InjectView(android.R.id.list)
	ListView paymentListView;
	@InjectView(android.R.id.empty)
	LoaderEmptyView ev;
	@InjectView(R.id.orderQuantity)
	TextView orderQuantityText;
	@InjectView(R.id.orderTotal)
	TextView orderTotalText;
	@InjectView(R.id.remainder)
	TextView remainingTotalText;
	@InjectView(R.id.tip_or_overpayment)
	TextView tipText;

	private PaymentAdapter paymentAdapter;
	private PaymentActionHandler listener;

	private EpicuriSessionDetail session;

    public SessionPaymentsFragment() {
        // Required empty public constructor
    }
	public static SessionPaymentsFragment newInstance(){
    	return new SessionPaymentsFragment();
	}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.sessiondetail_payments, container, false);

	    ButterKnife.inject(this, view);

	    paymentAdapter = new PaymentAdapter(getActivity());
	    ev.setText("No Payments Made");
	    paymentListView.setEmptyView(ev);
	    paymentListView.setAdapter(paymentAdapter);

	    paymentListView.setOnItemClickListener(this);
	    return view;
    }

	@Override
	public void onResume() {
		super.onResume();
		listener = (PaymentActionHandler) getActivity();
		if (paymentAdapter != null) paymentAdapter.setListener(this);
		((SessionContainer)getActivity()).registerSessionListener(this);	}

	@Override
	public void onPause() {
		super.onPause();
		((SessionContainer)getActivity()).deRegisterSessionListener(this);
		if (paymentAdapter != null) paymentAdapter.setListener(null);
		listener = null;
	}


	@Override
	public void onSessionChanged(EpicuriSessionDetail session) {
		this.session = session;

		((LoaderEmptyView) paymentListView.getEmptyView()).setDataLoaded();
		String dishesOrdered = getString(R.string.orderQuantity, session.getNumberOfDishes());
		String orderTotal = "Subtotal: " + LocalSettings.formatMoneyAmount(session.getSubtotal(), true);

		orderQuantityText.setText(dishesOrdered);
		orderTotalText.setText(orderTotal);

		Money remaining = session.getRemainingTotal();

		if(null == remaining) {
			// do nothing
		} else if(remaining.isPositive()) {
			remainingTotalText.setText(String.format("Remaining: %s", LocalSettings.formatMoneyAmount(remaining, true)));
		} else if(remaining.isZero()) {
			String paidString = session.isRefund() ? "Refunded" : "Paid in full";
			remainingTotalText.setText(paidString);
		} else {
            StringBuilder sb = new StringBuilder();
            if(session.getOverPayments().isPositive()){
                sb.append(String.format("Overpayment: %s", LocalSettings.formatMoneyAmount(session.getOverPayments(), true)));
            }
            if(session.getChange().isPositive()) {
                if(sb.length() > 0) sb.append(" ");
				sb.append(String.format("Change: %s", LocalSettings.formatMoneyAmount(session.getChange(), true)));
			}
            remainingTotalText.setText(sb);
		}

		if(session.getType() == EpicuriSessionDetail.SessionType.DINE) {
			tipText.setText(String.format("Suggested Tip %s%% (%s)",
					session.getTipPercentage(),
					LocalSettings.formatMoneyAmount(session.getSuggestedTipAmount(), true)));
			tipText.setVisibility(View.VISIBLE);
		} else {
			// don't show tip for takeaways
			tipText.setVisibility(View.GONE);
		}

		paymentAdapter.changeData(session);
		ev.setDataLoaded();
	}

	@OnClick(R.id.tip_or_overpayment)
	void adjustTip(){
		// don't allow editing once session is paid
		if(session.isPaid() || session.isClosed()) return;
		listener.showTipDialog();
	}

	@OnClick(R.id.remainder)
	void showPaymentsDialog(){
		if(session != null){
			if(session.isPaid() || session.isClosed()) return;
			listener.showAutoSettleDialog();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Object item = parent.getItemAtPosition(position);
        final EpicuriAdjustment adjustment;
		if (item instanceof EpicuriAdjustment) {
		    adjustment = (EpicuriAdjustment) item;
        } else return;

		EpicuriAdjustmentType type = LocalSettings.getInstance(getActivity()).getCachedRestaurant().getAdjustmentTypesLookup().get(adjustment.getTypeId());

		if(session.isClosed()){
			// allow paymentsense refunds in here
			if(!(type.getType() == EpicuriAdjustmentType.TYPE_PAYMENT
					&& LocalSettings.getInstance(getContext()).isAllowed(WaiterAppFeature.ADD_DELETE_PAYMENT)
					&& type.getName().equals(PaymentAdapter.PAYMENTSENSE_TYPE))) {
				Toast.makeText(getActivity(), "Cannot remove items once session is paid or closed", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		if (type.getName().equals(PaymentAdapter.PAYMENTSENSE_DISCOUNT_TYPE)) {
			return;
		}

		if (type.getType() == EpicuriAdjustmentType.TYPE_PAYMENT &&
				LocalSettings.getInstance(getContext()).isAllowed(WaiterAppFeature.ADD_DELETE_PAYMENT)) {
			if (type.getName().equals(PaymentAdapter.PAYMENTSENSE_TYPE)) {
				BigDecimal amount = new BigDecimal(adjustment.getReference() != null ? 0 :
						adjustment.getAmount().getAmount().doubleValue());

				if (adjustment.getReference() != null)
					for (EpicuriAdjustment adj : session.getAdjustments()) {
						if (adj.getReference() == null) continue;

						if (adj.getReference().equals(adjustment.getReference()))
							amount = amount.add(adj.getAmount().getAmount());
					}

				RefundFragment.newInstance(session, adjustment, amount.doubleValue()).show(getActivity()
						.getSupportFragmentManager(), "Refund");
				return;
			}

			new AlertDialog.Builder(getActivity())
					.setTitle("Delete payment?")
					.setMessage(String.format("Do you want to delete this %s payment of %s from the system?",
							type.getName(),
							LocalSettings.formatMoneyAmount(adjustment.getAmount(), true)))
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeAdjustment(adjustment);
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
		} else if(LocalSettings.getInstance(getContext()).isAllowed(WaiterAppFeature.ADD_DELETE_DISCOUNT)){
			new AlertDialog.Builder(getActivity())
					.setTitle("Delete adjustment?")
					.setMessage(String.format("Do you want to delete this %s adjustment of %s from the system?",
							type.getName(),
							adjustment.getAmount() == null ? (String.format(Locale.UK, "%.0f%%", adjustment.getPercentage())) : LocalSettings.formatMoneyAmount(adjustment.getAmount(), true)))
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							removeAdjustment(adjustment);
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
		}

	}

	@Override public void onRefundClicked(View view, int position) {
		onItemClick(paymentListView, view, position, 0L);
	}

	private void removeAdjustment(EpicuriAdjustment adjustment){
		WebServiceCall adjustmentCall = new DeleteAdjustmentWebServiceCall(session, adjustment);
		WebServiceTask task = new WebServiceTask(getActivity(), adjustmentCall, true);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

}
