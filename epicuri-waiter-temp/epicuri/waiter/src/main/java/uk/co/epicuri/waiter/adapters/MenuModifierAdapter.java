package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.MenuItemFragment;

public class MenuModifierAdapter extends BaseAdapter {
	/**
	 * the item (e.g. "Steak")
	 */
	private EpicuriMenu.Item item;
	private ArrayList<EpicuriMenu.ModifierValue> chosenModifiers;
	private Map<String, EpicuriMenu.ModifierGroup> modifierGroupsLookup = new HashMap<>();
	
	private LayoutInflater inflater;
	private final Context context;
    public MenuModifierAdapter(Context context) {
		super();
		inflater = LayoutInflater.from(context);
		this.context = context;
	}
	
	public void swapData(EpicuriMenu.Item item,
			ArrayList<EpicuriMenu.ModifierValue> chosenModifiers,
			ArrayList<EpicuriMenu.ModifierGroup> modifierGroups){
		this.item = item;
		this.chosenModifiers = chosenModifiers;
        if(modifierGroups == null) {
            return;
        }
        modifierGroupsLookup.clear();
		for(EpicuriMenu.ModifierGroup g: modifierGroups){
			modifierGroupsLookup.put(g.getId(), g);
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if(null == item) return 0;
		return item.getModifierGroupIds().length;
	}

	@Override
	public Object getItem(int position) {
		return modifierGroupsLookup.get(item.getModifierGroupIds()[position]);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(null == convertView){
			convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
		}

		String id = item.getModifierGroupIds()[position];
		EpicuriMenu.ModifierGroup modifierGroup = modifierGroupsLookup.get(id);

		if(null == modifierGroup) {
            Toast.makeText(context, "Modifiers not found", Toast.LENGTH_SHORT).show();
        }
		
		((TextView)convertView.findViewById(android.R.id.text1)).setText(modifierGroup.getName());
		
		TextView currentSelection = ((TextView)convertView.findViewById(android.R.id.text2));

		StringBuilder sb = new StringBuilder();
		if(modifierGroup.getLowerLimit() == 0){
			if(modifierGroup.getUpperLimit() > 0){
				sb.append(context.getString(R.string.modifiers_atMost, modifierGroup.getUpperLimit())).append(context.getResources().getQuantityString(R.plurals.options, modifierGroup.getUpperLimit()));
			}
		} else {
			if(modifierGroup.getUpperLimit() == 0){
				sb.append(context.getString(R.string.modifiers_atLeast, modifierGroup.getLowerLimit())).append(context.getResources().getQuantityString(R.plurals.options, modifierGroup.getLowerLimit()));
			} else if(modifierGroup.getUpperLimit() == modifierGroup.getLowerLimit()){
				sb.append(context.getString(R.string.modifiers_exactly, modifierGroup.getLowerLimit())).append(context.getResources().getQuantityString(R.plurals.options, modifierGroup.getLowerLimit()));
			} else {
				sb.append(context.getString(R.string.modifiers_between, modifierGroup.getLowerLimit(), modifierGroup.getUpperLimit())).append(context.getResources().getQuantityString(R.plurals.options, 5));
			}
		}

		int numberOfModifiers = 0;
		if(chosenModifiers != null && chosenModifiers.size() > 0){
			sb.append("\n");
			boolean first = true;
			for(ModifierValue val: modifierGroup.getModifierValues()){
				if(chosenModifiers.contains(val)){
					if(!first){ 
						sb.append(", ");
					}
					first = false;
					sb.append(val.getName()).append(" (").append(LocalSettings.formatMoneyAmount(val.getPrice(), true)).append(") ");
					numberOfModifiers++;
				}
			}
		}
		currentSelection.setText(sb.toString());
		if(numberOfModifiers < modifierGroup.getLowerLimit() || numberOfModifiers > modifierGroup.getUpperLimit()){
			currentSelection.setTextColor(Color.RED);
		} else {
			currentSelection.setTextColor(Color.BLACK);
		}
		return convertView;
	}

    public ArrayList<Integer> getMandatoryModifierIndex() {
        ArrayList<Integer> mandatoryModifiers = new ArrayList<>();
        for (int i = 0; i < item.getModifierGroupIds().length; i++) {
            String id = item.getModifierGroupIds()[i];
            EpicuriMenu.ModifierGroup modifierGroup = modifierGroupsLookup.get(id);
            if(modifierGroup.getLowerLimit() > 0){
                mandatoryModifiers.add(i);
            }
        }
        return mandatoryModifiers;
    }
}
