package uk.co.epicuri.waiter.ui;


import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.Preferences;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class MenuAlertFragment extends DialogFragment {

    private static final String MENU_ITEM_EXTRA = "menu_item_extra";
    @InjectView(R.id.image)
    ImageView image;
    @InjectView(R.id.item_name)
    TextView itemName;
    @InjectView(R.id.item_type)
    TextView itemType;
    @InjectView(R.id.description)
    TextView description;
    @InjectView(R.id.price)
    TextView price;
    @InjectView(R.id.allergens)
    TextView allergens;
    @InjectView(R.id.diet)
    TextView diet;

    EpicuriMenu.Item item;
    int apiVersion;

    public static MenuAlertFragment newInstance(EpicuriMenu.Item item) {
        MenuAlertFragment frag = new MenuAlertFragment();
        Bundle args = new Bundle();
        args.putParcelable(MENU_ITEM_EXTRA, item);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, null)
                .setView(createView())
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return createView();
    }

    private View createView() {
        LayoutInflater inflater;

        inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_menu_item, null);

        ButterKnife.inject(this, view);
        item = getArguments().getParcelable(MENU_ITEM_EXTRA);
        apiVersion = EpicuriApplication.getInstance(getActivity()).getApiVersion();

        render();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void render() {
        itemName.setText(item.getName());
        itemType.setText(item.getType().toString());
        if (!item.getDescription().isEmpty())
            description.setText(item.getDescription());
        price.setText(LocalSettings.formatMoneyAmount(item.getPrice(), true));
        if (apiVersion >= GlobalSettings.API_VERSION_6) {
            if (item.getShortCode() != null && !item.getShortCode().isEmpty())
                itemName.setText(String.format("%s (%s)", item.getName(), item.getShortCode()));

            if(item != null && !item.getImageUrl().isEmpty()){
                image.setVisibility(View.VISIBLE);
                Picasso.get().load(item.getImageUrl()).into(image);
            }else {
                image.setVisibility(View.GONE);
            }

            Preferences preferences = LocalSettings.getInstance(getActivity()).getCachedPreferences();
            if(item != null && preferences != null){
                if(item.getAllergiesKeys() != null){
                    ArrayList<String> allergensList = new ArrayList<>();
                    for (String key : item.getAllergiesKeys()){
                        allergensList.add(preferences.getAllergyByKey(key));
                    }
                    allergens.setText(selectedPrefsFormat(allergensList));
                }

                if(item.getDietsKeys() != null){
                    ArrayList<String> diets = new ArrayList<>();
                    for (String key : item.getDietsKeys()) {
                        diets.add(preferences.getDietByKey(key));
                    }
                    diet.setText(selectedPrefsFormat(diets));
                }
            }
            diet.setVisibility(View.VISIBLE);
            allergens.setVisibility(View.VISIBLE);
        }
    }

    private String selectedPrefsFormat(@NotNull ArrayList<String> items) {
        if(items.size() != 0){
            StringBuilder stringBuilder = new StringBuilder();
            for (String item : items) {
                stringBuilder.append(item).append(", ");
            }
            return stringBuilder.toString().substring(0, stringBuilder.length() - 2);
        }
        return "None";
    }
}
