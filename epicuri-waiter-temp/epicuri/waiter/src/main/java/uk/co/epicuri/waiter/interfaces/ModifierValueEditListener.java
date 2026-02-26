package uk.co.epicuri.waiter.interfaces;

import org.joda.money.Money;

/**
 * Created by Home on 7/18/16.
 */
public interface ModifierValueEditListener{
    void createModifier(CharSequence name, Money price, String vat, String plu);
    void updateModifier(String id, CharSequence name, Money price, String vat, String plu);
    void deleteModifier(String id);
}
