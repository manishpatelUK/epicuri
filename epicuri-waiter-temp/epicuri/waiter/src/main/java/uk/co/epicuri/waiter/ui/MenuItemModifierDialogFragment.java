package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import uk.co.epicuri.waiter.interfaces.OnMenuItemModifierListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class MenuItemModifierDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String EXTRA_CHOSEN_MODIFIERS = "uk.co.epicuri.CHOSEN_MODIFIERS";
    private EpicuriMenu.ModifierGroup item;
    private boolean[] selections;
    private ArrayList<EpicuriMenu.ModifierValue> chosenModifiers;
    private int selectedItem = -1;
    private int preselectedItem = -1;

    public static MenuItemModifierDialogFragment newInstance(EpicuriMenu.ModifierGroup item, ArrayList<EpicuriMenu.ModifierValue> chosenOptions) {
        Bundle b = new Bundle();
        b.putParcelable(GlobalSettings.EXTRA_MENUITEM, item);
        b.putParcelableArrayList(EXTRA_CHOSEN_MODIFIERS, chosenOptions);

        MenuItemModifierDialogFragment f = new MenuItemModifierDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        item = getArguments().getParcelable(GlobalSettings.EXTRA_MENUITEM);
        chosenModifiers = getArguments().getParcelableArrayList(EXTRA_CHOSEN_MODIFIERS);

        selections = new boolean[item.getModifierValues().length];
        for (int i = 0; i < selections.length; i++) {
            selections[i] = chosenModifiers.contains(item.getModifierValues()[i]);
            if (selections[i]) selectedItem = i;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(item.getName())
                .setPositiveButton("OK", this);

        if (item.getLowerLimit() != 1 || item.getUpperLimit() != 1) {
            b.setMultiChoiceItems(item.getModifierValues(), selections, new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    int count = 0;
                    for (boolean check : selections) if (check) count++;

                    if(count == item.getUpperLimit()) MenuItemModifierDialogFragment.this.itemSelected();
                    if (count - 1 == item.getUpperLimit()) {
                        selections[which] = false;
                        ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                    }
                }
            });
        } else {
            b.setSingleChoiceItems(item.getModifierValues(), selectedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedItem = which;
                    for (int i = 0; i < selections.length; i++) {
                        selections[i] = (which == i);
                    }
                    MenuItemModifierDialogFragment.this.itemSelected();
                }
            });
        }

        return b.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        itemSelected();
    }

    private void itemSelected() {
        FragmentManager fm = getFragmentManager();

        // pass message to the other fragment
        OnMenuItemModifierListener listener = (OnMenuItemModifierListener) getTargetFragment();
        //	Log.w("MenuItemModifierDialogFragment", "No target fragment set for response");
        if (null != listener) {
//			if(item.getLowerLimit() == 1 && item.getUpperLimit() == 1){
//				selections[selectedItem] = true;
//			}
            int numberOfTicks = 0;
            for (int i = 0; i < selections.length; i++) {
                if (selections[i]) {
                    numberOfTicks++;
                }
            }


            String[] returnValue = new String[numberOfTicks];
            numberOfTicks = 0;
            for (int i = 0; i < selections.length; i++) {
                if (selections[i]) {
                    returnValue[numberOfTicks++] = item.getModifierValues()[i].getId();
                }
            }
            for (int i = 0; i < selections.length; i++) {
                EpicuriMenu.ModifierValue modifierValue = item.getModifierValues()[i];
                if (selections[i] && !chosenModifiers.contains(modifierValue)) {
                    chosenModifiers.add(modifierValue);
                } else if (!selections[i] && chosenModifiers.contains(modifierValue)) {
                    chosenModifiers.remove(modifierValue);
                }
            }
            listener.updateMenuItemModifierChoice(chosenModifiers);
        }
        dismiss();
    }

}
