package uk.co.epicuri.waiter.ui.menueditor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.ModifierValueEditListener;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriVatRate;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class ModifierValueFragment extends DialogFragment {

	private EditText name;
	private EditText price;
	private Spinner vatRateSpinner;
	private TextView vatLabel;
	private TextInputEditText plu;

	private ModifierValue editedValue = null;
	private ArrayList<EpicuriVatRate> vatRates;
	
	public static ModifierValueFragment newInstance(String groupId, ArrayList<EpicuriVatRate> vatRates){
		final ModifierValueFragment frag = new ModifierValueFragment();
		Bundle args = new Bundle();
		args.putString(GlobalSettings.EXTRA_MODIFIER_GROUP_ID, groupId);
		args.putParcelableArrayList(GlobalSettings.EXTRA_VAT_RATES, vatRates);
		frag.setArguments(args);
		return frag;
	}

	public static ModifierValueFragment newInstance(String groupId, ModifierValue modifier, ArrayList<EpicuriVatRate> vatRates){
		final ModifierValueFragment frag = new ModifierValueFragment();
		Bundle args = new Bundle();
		args.putString(GlobalSettings.EXTRA_MODIFIER_GROUP_ID, groupId);
		args.putParcelable(GlobalSettings.EXTRA_MODIFIER_VALUE, modifier);
		args.putParcelableArrayList(GlobalSettings.EXTRA_VAT_RATES, vatRates);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_modifiervalue,
				null, false);
		name = (EditText)v.findViewById(R.id.name_edit);
		price = (EditText)v.findViewById(R.id.price_edit);
		vatRateSpinner = (Spinner)v.findViewById(R.id.vat_spinner);
		vatLabel = (TextView) v.findViewById(R.id.vatLabel);
		plu = (TextInputEditText) v.findViewById(R.id.plu_edit);

		if(null != getArguments()){
			editedValue = getArguments().getParcelable(GlobalSettings.EXTRA_MODIFIER_VALUE);
			vatRates = getArguments().getParcelableArrayList(GlobalSettings.EXTRA_VAT_RATES);
		}

		EpicuriRestaurant restaurant = LocalSettings.getInstance(getActivity()).getCachedRestaurant();
		vatLabel.setText(restaurant.getVatLabel());


		ArrayAdapter<EpicuriVatRate> rateAdapter = new ArrayAdapter<EpicuriVatRate>(getActivity(), android.R.layout.simple_spinner_item, vatRates);
		rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vatRateSpinner.setAdapter(rateAdapter);

		if(null != editedValue){
			name.setText(editedValue.getName());
			price.setText(LocalSettings.formatMoneyAmount(editedValue.getPrice(), false));
			for(int i=0; i<vatRates.size();i++){
				if(vatRates.get(i).getId().equals(editedValue.getTaxTypeId())){
					vatRateSpinner.setSelection(i);
					break;
				}
			}
			plu.setText(editedValue.getPlu());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle("Modifier Value")
            .setView(v)
            .setNegativeButton(getString(R.string.cancel), null);
		
		if(null == editedValue){
			builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CharSequence priceString = price.getText();
					if(priceString.length() == 0) priceString = "0";
					((ModifierValueEditListener)getActivity()).createModifier(
							name.getText(),
							LocalSettings.parseCurrency(priceString),
							((EpicuriVatRate)vatRateSpinner.getSelectedItem()).getId(),
							plu.getText().toString());
				}
			});
		} else {
			builder.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ModifierValueEditListener) getActivity()).updateModifier(
							editedValue.getId(),
							name.getText(),
							LocalSettings.parseCurrency(price.getText()),
							((EpicuriVatRate)vatRateSpinner.getSelectedItem()).getId(),
							plu.getText().toString());

				}
			})
			.setNeutralButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ModifierValueEditListener)getActivity()).deleteModifier(
							editedValue.getId());
				}
			});
		}
		return builder.create();
	}
	
}