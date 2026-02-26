package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuModifierAdapter;
import uk.co.epicuri.waiter.interfaces.OnItemQueuedListener;
import uk.co.epicuri.waiter.interfaces.OnMenuItemModifierListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Diner;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.WaiterAppFeature;
import uk.co.epicuri.waiter.utils.GlobalSettings;


public class MenuItemFragment extends DialogFragment implements OnMenuItemModifierListener {

	private static final String FRAGMENT_MENUITEMMODIFIER = "menuItemModifier";
	private static final int REQUEST_MODIFIERS = 1;

	private MenuModifierAdapter adapter;
	
//	private int sessionId;
	private Diner diner;

	private EpicuriOrderItem orderItem;
	private int activatedModifierIndex;
	
	private ArrayList<EpicuriMenu.ModifierGroup> modifierGroups;
	
	private Spinner courseSpinner;
	private EditText quantity;
	private EditText priceText;
	private Button positiveButton;
	private EditText notes;
	private TextView priceOverrideLabel;
	private boolean enablePriceOverride = false;
	
	private Button quantAdd;
	private Button quantSub;
	
	private OnItemQueuedListener onItemQueuedListener;
	private AlertDialog alertDialog;
	private boolean recalculatePrice = true;

	static MenuItemFragment newInstance(Diner diner, EpicuriOrderItem orderItem,
    		ArrayList<EpicuriMenu.Course> courses, ArrayList<EpicuriMenu.ModifierGroup> modifierGroups) {
        MenuItemFragment f = new MenuItemFragment();
        Bundle args = new Bundle();
//        args.putInt(GlobalSettings.EXTRA_SESSION_ID, sessionId);
        args.putParcelable(GlobalSettings.EXTRA_DINER, diner);
        args.putParcelable(GlobalSettings.EXTRA_ORDERITEM, orderItem);
        args.putParcelableArrayList(GlobalSettings.EXTRA_MODIFIER_GROUPS, modifierGroups);
        args.putParcelableArrayList(GlobalSettings.EXTRA_COURSES, courses);

        f.setArguments(args);

        return f;
    }
    
    private List<EpicuriMenu.Course> courses;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
//		sessionId = getArguments().getInt(GlobalSettings.EXTRA_SESSION_ID);
		diner = getArguments().getParcelable(GlobalSettings.EXTRA_DINER);
		orderItem = getArguments().getParcelable(GlobalSettings.EXTRA_ORDERITEM);
		courses = getArguments().getParcelableArrayList(GlobalSettings.EXTRA_COURSES);
		modifierGroups = getArguments().getParcelableArrayList(GlobalSettings.EXTRA_MODIFIER_GROUPS);
		enablePriceOverride = isOverrideAllowed();

		adapter = new MenuModifierAdapter(getActivity());
		adapter.swapData(orderItem.getItem(), orderItem.getChosenModifiers(), modifierGroups);

        try {
			if(null != getTargetFragment()){
				onItemQueuedListener = (OnItemQueuedListener)getTargetFragment();
			} else {
				onItemQueuedListener = (OnItemQueuedListener)getActivity();
			}
		} catch (ClassCastException e){
			throw new RuntimeException("Calling activity or fragment must implement "
					+ "OnItemQueuedListener");
		}
		super.onCreate(savedInstanceState);
	}
	
	private void promptForModifiers(int modifierGroupIndex){
		ModifierGroup item = (EpicuriMenu.ModifierGroup)adapter.getItem(modifierGroupIndex);
		ArrayList<EpicuriMenu.ModifierValue> chosenModifiers = orderItem.getChosenModifiers();
		
		MenuItemModifierDialogFragment frag = MenuItemModifierDialogFragment.newInstance(item, chosenModifiers);
		frag.setTargetFragment(this, REQUEST_MODIFIERS);
		frag.show(getFragmentManager(), FRAGMENT_MENUITEMMODIFIER);
	}

    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_menuitem, null, false);
		ListView lv = view.findViewById(android.R.id.list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View view, int position,
					long id) {
				activatedModifierIndex = position;
				promptForModifiers(activatedModifierIndex);
			}
		});

		for (int index : adapter.getMandatoryModifierIndex()){
		    promptForModifiers(index);
        }

		courseSpinner = view.findViewById(R.id.course);
	    if(null != courses) {
		    ArrayAdapter<EpicuriMenu.Course> courseAdapter = new ArrayAdapter<EpicuriMenu.Course>(getActivity(), android.R.layout.simple_spinner_item, courses);
		    courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    courseSpinner.setAdapter(courseAdapter);
		    int defaultCourse = courseAdapter.getPosition(orderItem.getCourse());
		    courseSpinner.setSelection(defaultCourse);
	    }

		quantity = view.findViewById(R.id.quantity);
		quantity.setText(String.valueOf(orderItem.getQuantity()));
		
		quantAdd = view.findViewById(R.id.quant_plus);
		quantAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int val = 1;
				try{
					val = Integer.valueOf(quantity.getText().toString()) + 1;
				} catch(NumberFormatException e){
					// ignore, default to one
				}
				quantSub.setEnabled(val > 1);
				quantity.setText(String.valueOf(val));
				resetDialogTitle();
			}
		});
		quantSub = view.findViewById(R.id.quant_minus);
		quantSub.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int val = 1;
				try{
					val = Integer.valueOf(quantity.getText().toString()) - 1;
				} catch(NumberFormatException e){
					// ignore, default to one
				}
				quantSub.setEnabled(val > 1);
				quantity.setText(String.valueOf(val));
				resetDialogTitle();
			}
		});
		quantSub.setEnabled(orderItem.getQuantity() > 1);

		priceOverrideLabel = view.findViewById(R.id.priceOverrideLabel);

		priceText = view.findViewById(R.id.price);
		priceText.setEnabled(enablePriceOverride);
		priceText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(!editable.toString().startsWith(LocalSettings.getCurrencyUnit().getSymbol())){
					priceText.setText(LocalSettings.getCurrencyUnit().getSymbol()+editable.toString());
					Selection.setSelection(priceText.getText(), priceText.getText().length());
				}

				if(customPriceEntered()) {
					priceOverrideLabel.setVisibility(View.VISIBLE);
				} else {
					priceOverrideLabel.setVisibility(View.INVISIBLE);
				}
				resetDialogTitle();
			}
		});

		if(orderItem.getPriceOverride() != null) {
			priceText.setText(LocalSettings.formatMoneyAmount(orderItem.getPriceOverride(), true));
		}
		else if(orderItem.getItem() != null && orderItem.getItem().getPrice() != null) {
			priceText.setText(LocalSettings.formatMoneyAmount(orderItem.getItem().getPrice(), true));
		}

		notes = view.findViewById(R.id.notes);
		notes.setText(orderItem.getNote());

        alertDialog = new AlertDialog.Builder(getActivity())
		        .setTitle(getDialogTitleString())
		        .setPositiveButton(orderItem.getId() != null && !orderItem.getId().equals("-1") ? "Update Item" : "Add item",
	        		new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int whichButton) {
                        	orderItem.setQuantity(Integer.valueOf(quantity.getText().toString()));
                        	if(priceText.getText().length() > 0){
                        		//if the price hasn't changed, don't override
								String valueWithoutSymbol = priceText.getText().toString().replace(LocalSettings.getCurrencyUnit().getSymbol(), "");
								valueWithoutSymbol = valueWithoutSymbol.replace("£", "");
								valueWithoutSymbol = valueWithoutSymbol.replace("$", "");
								valueWithoutSymbol = valueWithoutSymbol.replace("€", "");
								Money price = LocalSettings.parseCurrency(valueWithoutSymbol);
								if(!price.equals(orderItem.getItem().getPrice())) {
									orderItem.setPrice(price);
								}
                        	}
                        	orderItem.setNote(notes.getText().toString());
	                        if(null != courses && courseSpinner.getSelectedItem() != null) {
	                            orderItem.setCourse((EpicuriMenu.Course) courseSpinner.getSelectedItem());
	                        }
                        	
                        	onItemQueuedListener.queueItem(orderItem, diner);
                        }
                    }
                )
                .setNegativeButton(orderItem.getId() != null && !orderItem.getId().equals("-1") ? "Remove item" : "Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								if (orderItem.getId() != null && !orderItem.getId().equals("-1") ) {
									onItemQueuedListener.unQueueItem(orderItem, diner);
								}
							}
						}
				)
                .setView(view)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				validateFields();
			}
		});
        return alertDialog;
    }

	private boolean customPriceEntered() {
		return orderItem.getItem() != null && orderItem.getItem().getPrice().getAmount().doubleValue() != getEnteredPrice();
	}

	@NonNull
	private String getDialogTitleString() {
		if(customPriceEntered()) {
			try {
				Money price = LocalSettings.parseCurrency(priceText.getText().toString().replace(LocalSettings.getCurrencyUnit().getSymbol(), ""));
				//fake orderitem to do the calculation
				EpicuriOrderItem epicuriOrderItem = new EpicuriOrderItem(orderItem.getItem(), orderItem.getCourse());
				epicuriOrderItem.setPrice(price);
				String amount = LocalSettings.formatMoneyAmount(epicuriOrderItem.getCalculatedPriceIncludingQuantity(), true);
				return epicuriOrderItem.getItem().getName() + " *(" + amount + ")";
			} catch (Exception ex){}//squelch
		}

		return orderItem.getItem().getName() + " (" + LocalSettings.formatMoneyAmount(orderItem.getCalculatedPriceIncludingQuantity(), true) + ")";
	}

	private void resetDialogTitle() {
		if(alertDialog == null) {
			return;
		}

		alertDialog.setTitle(getDialogTitleString());
	}

	@Override
	public void updateMenuItemModifierChoice(ArrayList<EpicuriMenu.ModifierValue> choices) {
		adapter.swapData(orderItem.getItem(), orderItem.getChosenModifiers(), modifierGroups);
		validateFields();
		resetDialogTitle();
	}

	private double getEnteredPrice() {
		Editable text = priceText.getText();
		if(!TextUtils.isEmpty(text) && text.length() > 1) {
			try {
				String value = text.toString();
				if(value.startsWith(LocalSettings.getCurrencyUnit().getSymbol())) {
					value = value.substring(1);
				}
				return Double.parseDouble(value);
			} catch (NumberFormatException ex) {
				return orderItem.getCalculatedPrice().getAmount().doubleValue();
			}
		}
		return orderItem.getCalculatedPrice().getAmount().doubleValue();
	}
	
	private void validateFields(){
		boolean valid = true;
		StringBuilder validationString = new StringBuilder();
		
		String[] activeModifierGroups = orderItem.getItem().getModifierGroupIds();
		
		// check for mandatory options which don't have a value set
		for(String id: activeModifierGroups){
			EpicuriMenu.ModifierGroup group = null;
			for(EpicuriMenu.ModifierGroup g: modifierGroups){
				if(g.getId().equals(id)){
					group = g;
					break;
				}
			}
			if(null == group) throw new RuntimeException("Modifier not found");

			if(group.getLowerLimit() > 0 || group.getUpperLimit() > 0){
				int numberOfChoices = 0;
				for(EpicuriMenu.ModifierValue value: group.getModifierValues()){
					if(orderItem.getChosenModifiers().contains(value)){
						numberOfChoices++;
					}
				}
				if(numberOfChoices < group.getLowerLimit() || numberOfChoices > group.getUpperLimit()){
					valid = false;
					validationString
						.append("Choice for ")
						.append(group.getName())
						.append(" must have between ")
						.append(group.getLowerLimit())
						.append(" and ").append(group.getUpperLimit()).append(" values selected\n");
				}
			}
		}
		positiveButton.setEnabled(valid);
	}

	private boolean isOverrideAllowed() {
		return LocalSettings.getInstance(getContext()).isAllowed(WaiterAppFeature.PRICE_OVERRIDE);
	}
}
