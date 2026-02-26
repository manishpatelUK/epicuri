package uk.co.epicuri.waiter.model;

import org.joda.money.Money;

import java.util.ArrayList;
import java.util.Date;

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

	Money getCalculatedPrice();

	Money getPriceOverride();

	String getDiscountReason();

}
