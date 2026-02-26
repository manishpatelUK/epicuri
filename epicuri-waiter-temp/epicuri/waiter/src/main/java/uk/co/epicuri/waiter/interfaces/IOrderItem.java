package uk.co.epicuri.waiter.interfaces;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.Date;

import uk.co.epicuri.waiter.model.EpicuriMenu;

public interface IOrderItem {
	int getQuantity();
	String getNote();
	EpicuriMenu.Course getCourse();
	String getDinerId();
	String getId();
	Date getDelivered();
	EpicuriMenu.Item getItem();
	ArrayList<EpicuriMenu.ModifierValue> getChosenModifiers();
	boolean isPriceOverridden();
	Money getCalculatedPriceIncludingQuantity();
	Money getCalculatedPrice();
	Money getPriceOverride();
	String getDiscountReason();
}
