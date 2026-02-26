package uk.co.epicuri.waiter.ui;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriCustomer;

public class DinerDetailsFragment extends DialogFragment {

	public static DinerDetailsFragment newInstance(EpicuriCustomer customer, boolean isBirthday) {
		DinerDetailsFragment frag = new DinerDetailsFragment();
		Bundle args = new Bundle();
		args.putParcelable(GlobalSettings.EXTRA_CUSTOMER, customer);
		args.putBoolean(GlobalSettings.EXTRA_IS_BIRTHDAY, isBirthday);
		frag.setArguments(args);
		return frag;
	}

	private EpicuriCustomer customer;
	private boolean isBirthday;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customer = getArguments().getParcelable(GlobalSettings.EXTRA_CUSTOMER);
		isBirthday = getArguments().getBoolean(GlobalSettings.EXTRA_IS_BIRTHDAY);
	}

	@InjectView(R.id.diet_text)
	TextView diet;
	@InjectView(R.id.allergies_text)
	TextView allergies;
	@InjectView(R.id.favouriteDrink_text)
	TextView favouriteDrink;
	@InjectView(R.id.favouriteFood_text)
	TextView favouriteFood;
	@InjectView(R.id.hatedFood_text)
	TextView hated;
	@InjectView(R.id.foodPreferences_text)
	TextView foodPreferences;
	@InjectView(R.id.birthday_image)
	ImageView birthday;
	@InjectView(R.id.blackmark_image)
	ImageView blackmark;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_dinerdetails, null, false);

		ButterKnife.inject(this, v);
		StringBuilder sb;

		sb = new StringBuilder();
		for (String food : customer.getDietaryRequirements()) {
			if(sb.length() > 0 ) sb.append(", ");
			sb.append(food);
		}
		diet.setText(sb.length() == 0 ? getString(R.string.none_specified) : sb);

		sb = new StringBuilder();
		for (String food : customer.getAllergies()) {
			if(sb.length() > 0 ) sb.append(", ");
			sb.append(food);
		}
		allergies.setText(sb.length() == 0 ? getString(R.string.none_specified) : sb);

		sb = new StringBuilder();
		for (String food : customer.getFoodPreferences()) {
			if(sb.length() > 0 ) sb.append(", ");
			sb.append(food);
		}
		foodPreferences.setText(sb.length() == 0 ? getString(R.string.none_specified) : sb);

		favouriteDrink.setText(TextUtils.isEmpty(customer.getFavouriteDrink()) ? getString(R.string.none_specified) : customer.getFavouriteDrink());
		favouriteFood.setText(TextUtils.isEmpty(customer.getFavouriteFood()) ? getString(R.string.none_specified) : customer.getFavouriteFood());
		hated.setText(TextUtils.isEmpty(customer.getHatedFood()) ? getString(R.string.none_specified) : customer.getHatedFood());

		birthday.setVisibility(isBirthday ? View.VISIBLE : View.GONE);
		blackmark.setVisibility(customer.isBlackMarked() ? View.VISIBLE: View.GONE);

		return new AlertDialog.Builder(getActivity())
				.setTitle(customer.getName())
				.setView(v)
				.setPositiveButton(getString(R.string.done), null)
				.create();
	}
}
