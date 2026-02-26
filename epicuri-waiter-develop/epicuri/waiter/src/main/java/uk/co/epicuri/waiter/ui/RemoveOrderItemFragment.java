package uk.co.epicuri.waiter.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.OrderAdapter;
import uk.co.epicuri.waiter.interfaces.RemoveOrderListener;
import uk.co.epicuri.waiter.model.EpicuriAdjustmentType;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.utils.GlobalSettings;


public class RemoveOrderItemFragment extends DialogFragment {

	private RemoveOrderListener listener;
	
	public static RemoveOrderItemFragment newInstance(ArrayList<EpicuriOrderItem> items){
		RemoveOrderItemFragment frag = new RemoveOrderItemFragment();
		Bundle args = new Bundle();
		args.putParcelableArrayList(GlobalSettings.EXTRA_ORDERITEM, items);
		frag.setArguments(args);
		return frag;
	}
	
	private ArrayList<EpicuriOrderItem> items;

	private Spinner adjustmentTypeSpinner;
	private ListView itemlistView;
	private OrderAdapter adapter;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		items = getArguments().getParcelableArrayList(GlobalSettings.EXTRA_ORDERITEM);
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_removeitem, null, false);

		ArrayList<EpicuriAdjustmentType> discountTypes = LocalSettings.getInstance(getActivity()).getCachedRestaurant().getDiscountTypes();
		final ArrayAdapter<EpicuriAdjustmentType> adjustmentTypeAdapter = new ArrayAdapter<EpicuriAdjustmentType>(getActivity(), android.R.layout.simple_spinner_item, discountTypes);
		adjustmentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		adjustmentTypeSpinner = (Spinner) view.findViewById(R.id.adjustmentType);
		adjustmentTypeSpinner.setAdapter(adjustmentTypeAdapter);

		itemlistView = (ListView) view.findViewById(android.R.id.list);
		adapter = new OrderAdapter(getActivity());
		adapter.changeData(items);
		itemlistView.setAdapter(adapter);
		itemlistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		itemlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				validate();
			}
		});

		// select the first item by default
		itemlistView.setItemChecked(0, true);
		
		dialog = new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.removeItem_title))
		.setView(view)
		.setPositiveButton(R.string.removeItem_remove, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ArrayList<EpicuriOrderItem> orderItems = new ArrayList<EpicuriOrderItem>();
				SparseBooleanArray checkedItems = itemlistView.getCheckedItemPositions();
				for (int i = 0; i < items.size(); i++) {
					if (checkedItems.get(i)) {
						orderItems.add(items.get(i));
					}
				}
				listener.removeOrderItems(orderItems, (EpicuriAdjustmentType) adjustmentTypeSpinner.getSelectedItem());
			}
		})
		.setNegativeButton(R.string.removeItem_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		})
		.create();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				validate();
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setEnabled(LocalSettings.getInstance(getContext()).isAllowed(WaiterAppFeature.ORDER_VOID));
			}
		});
		return dialog;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if(null != getTargetFragment()){
			listener = (RemoveOrderListener) getTargetFragment();
		} else {
			listener = (RemoveOrderListener) context;
		}
	}

	private void validate(){
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(itemlistView.getCheckedItemCount() > 0);
	}


	private AlertDialog dialog;
}
