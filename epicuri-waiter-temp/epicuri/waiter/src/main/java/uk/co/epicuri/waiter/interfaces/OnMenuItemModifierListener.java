package uk.co.epicuri.waiter.interfaces;

import java.util.ArrayList;

import uk.co.epicuri.waiter.model.EpicuriMenu;

public interface OnMenuItemModifierListener {
    void updateMenuItemModifierChoice(ArrayList<EpicuriMenu.ModifierValue> chosenModifiers);
}
