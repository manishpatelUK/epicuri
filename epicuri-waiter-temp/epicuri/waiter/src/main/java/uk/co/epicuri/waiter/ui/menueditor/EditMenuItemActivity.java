package uk.co.epicuri.waiter.ui.menueditor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.EpicuriApplication;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PreferencesLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PrinterLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.VatRateLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item.ItemType;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriVatRate;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.Preference;
import uk.co.epicuri.waiter.model.Preferences;
import uk.co.epicuri.waiter.model.StockLevel;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.ui.dialog.MultiSelectDialog;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.CreateEditMenuItemWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditStockLevelWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteMenuLevelWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetStockControlWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class EditMenuItemActivity extends EpicuriBaseActivity {

	private EpicuriMenu.Item item;
    public static final int LOADER_PREFERENCES = 8;
	@InjectView(R.id.name_edit)
	EditText name;
	@InjectView(R.id.price_edit)
	EditText price;
	@InjectView(R.id.description_edit)
	EditText description;
	@InjectView(R.id.unavailable_check)
	CheckBox unavailable;
	@InjectView(R.id.plu_edit)
	TextInputEditText plu;
	@InjectView(R.id.plu_stock_count_edit)
	TextInputEditText pluStockCount;
	@InjectView(R.id.clear_sku)
	Button pluClearButton;
	@InjectView(R.id.vat_spinner)
	Spinner vatSpinner;
	@InjectView(R.id.printer_spinner)
	Spinner printerSpinner;
	@InjectView(R.id.itemtype_spinner)
	Spinner itemtypeSpinner;
	@InjectView(R.id.modifier_container)
	LinearLayout modifierGroupsList;
	@InjectView(R.id.vatLabel)
	TextView vatLabel;
	@InjectView(R.id.short_code)
    EditText shortCode;
	@InjectView(R.id.short_code_field)
    View shortCodeField;
	@InjectView(R.id.allergies_holder)
    View allergiesHolder;
	@InjectView(R.id.allergies)
    TextView allergiesText;
    @InjectView(R.id.diet)
    TextView dietText;
    @InjectView(R.id.add_image) Button addImageBtn;
    @InjectView(R.id.menu_item_image) ImageView itemImage;

	private ArrayList<ModifierGroup> modifierGroups;
	private ArrayList<EpicuriMenu.Printer> printers;
	private ArrayList<EpicuriVatRate> vatRates;
	private Preferences preferences;
    private ArrayList<String> allergiesSelected = new ArrayList<>();
    private ArrayList<String> dietsSelected = new ArrayList<>();
    private String imageUrl;
	private ItemType[] itemTypes = new ItemType[]{ItemType.FOOD, ItemType.DRINK, ItemType.OTHER};
	private int flag;
	private StockLevel associatedStockLevel;
	private List<StockLevel> allStockLevels = new ArrayList<>();
	private boolean stockCountEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit_menuitem);
		ButterKnife.inject(this);

		if(EpicuriApplication.getInstance(this).getApiVersion() >= GlobalSettings.API_VERSION_6) {
			shortCodeField.setVisibility(View.VISIBLE);
        } else {
			shortCodeField.setVisibility(View.GONE);
        }

		flag = 0;

		if(null != savedInstanceState){
			selectedModifiers = savedInstanceState.getParcelableArrayList("selectedModifiers");
		}

		Utils.initActionBar(this);

        item = getIntent().getParcelableExtra(GlobalSettings.EXTRA_MENUITEM);


		ArrayAdapter<ItemType> adapter = new ArrayAdapter<ItemType>(this, android.R.layout.simple_spinner_item, itemTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		itemtypeSpinner.setAdapter(adapter);

        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final EditText input = new EditText(EditMenuItemActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);

                new AlertDialog.Builder(EditMenuItemActivity.this)
                        .setTitle("Add url of an image")
                        .setView(input)
                        .setPositiveButton("Add image", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                imageUrl = input.getText().toString();
                                if(!imageUrl.isEmpty()) Picasso.get().load(imageUrl).into(itemImage);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

		loadItem(item);

		EpicuriRestaurant restaurant = LocalSettings.getInstance(this).getCachedRestaurant();
		vatLabel.setText(restaurant.getVatLabel());
        preferences = LocalSettings.getInstance(this).getCachedPreferences();
        if(preferences == null){
			getSupportLoaderManager().initLoader(LOADER_PREFERENCES, null, preferencesLoaderCallbacks);
		}else {
			setupPreferences();
		}

		this.stockCountEnabled = restaurant.stockCountdownEnabled();
		if(stockCountEnabled) {
			pluStockCount.setVisibility(View.VISIBLE);
            populateStockCount();
		}

		plu.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable pluField) {
				if(pluField == null || pluField.toString() == null) return;

				String newPlu = pluField.toString().trim();

				associatedStockLevel = findStockLevelPlu(newPlu);
				if(newPlu.length() == 0 || associatedStockLevel == null) {
					pluStockCount.setText("");
					pluStockCount.setVisibility(View.VISIBLE);
				} else if(associatedStockLevel.isTrackable()) {
					pluStockCount.setVisibility(View.VISIBLE);
					pluStockCount.setText(Integer.toString(associatedStockLevel.getLevel()));
				} else if(!associatedStockLevel.isTrackable()) {
					pluStockCount.setVisibility(View.GONE);
				}
			}
		});

		getSupportLoaderManager().initLoader(GlobalSettings.LOADER_MODIFIER, null, modifierLoaderCallbacks);
		getSupportLoaderManager().initLoader(GlobalSettings.LOADER_PRINTER, null, printerLoaderCallbacks);
		getSupportLoaderManager().initLoader(GlobalSettings.LOADER_VAT, null, vatLoaderCallbacks);
	}

	@OnClick(R.id.clear_sku)
	void onClearSKU() {
		if(associatedStockLevel != null) {
			associatedStockLevel = null;
		}
		plu.getText().clear();
		pluStockCount.getText().clear();
		if(stockCountEnabled) {
			pluStockCount.setEnabled(true);
			pluStockCount.setVisibility(View.VISIBLE);
		} else {
			pluStockCount.setEnabled(false);
			pluStockCount.setVisibility(View.GONE);
		}
	}

	private void populateStockCount() {
		WebServiceTask task = new WebServiceTask(this, new GetStockControlWebServiceCall());
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			@Override
			public void onSuccess(int code, String response) {
				try {
					allStockLevels.clear();
					JSONArray jsonArray = new JSONArray(response);
					for(int i = 0; i < jsonArray.length(); i++) {
						StockLevel stockLevel = new StockLevel(jsonArray.getJSONObject(i));
						allStockLevels.add(stockLevel);
						if(item != null && stockLevel.getPlu() != null && stockLevel.getPlu().equals(item.getPlu())) {
							associatedStockLevel = stockLevel;
							if(!associatedStockLevel.isTrackable()) {
								pluStockCount.setEnabled(false);
								pluStockCount.setText(R.string.skuCountDisabled);
							} else {
								pluStockCount.setText(Integer.toString(stockLevel.getLevel()));
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		task.execute();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("selectedModifiers", selectedModifiers);
	}


    private final LoaderManager.LoaderCallbacks<LoaderWrapper<Preferences>> preferencesLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<Preferences>>() {
        @NonNull @Override
        public Loader<LoaderWrapper<Preferences>> onCreateLoader(int id, @Nullable Bundle args) {
            return new EpicuriLoader<>(EditMenuItemActivity.this, new PreferencesLoaderTemplate());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<LoaderWrapper<Preferences>> loader, LoaderWrapper<Preferences> data) {
            if (data == null || data.getPayload() == null) return;
            preferences = data.getPayload();
            LocalSettings.getInstance(EditMenuItemActivity.this).cachePreferences(preferences);
			setupPreferences();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<LoaderWrapper<Preferences>> loader) {

        }
    };

	private void setupPreferences() {
		ArrayList<Preference> allergies = preferences.getAllergies();
		ArrayList<Preference> diets = preferences.getDietaryRequirements();
		setupAllergiesDialog(allergies);
		setupDietsDialog(diets);
		if(item != null) {
            if (item.getAllergiesKeys() != null) {
                allergiesSelected.clear();
                for (String key : item.getAllergiesKeys()) {
                    allergiesSelected.add(preferences.getAllergyByKey(key));
                }
                allergiesSelectedText(allergiesSelected);
            }

            if (item.getDietsKeys() != null) {
                dietsSelected.clear();
                for (String key : item.getDietsKeys()) {
                    dietsSelected.add(preferences.getDietByKey(key));
                }
                dietsSelectedText(dietsSelected);
            }
        }
	}

	private void setupAllergiesDialog(ArrayList<Preference> allergies) {
        final MultiSelectDialog dialogAllergies = new MultiSelectDialog();
        dialogAllergies.getItems().add("None");
        for (Preference pref: allergies){
            dialogAllergies.getItems().add(pref.getValue());
        }
        dialogAllergies.setSelectedItems(allergiesSelected);
        allergiesText.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialogAllergies.setOnItemsSelectedListener(new MultiSelectDialog.OnItemsSelectedListener() {
                    @Override
                    public void onItemsSelectedListener(@NotNull ArrayList<String> items) {
                        allergiesSelected = items;
                        allergiesSelectedText(items);
                    }
                });
                if(!dialogAllergies.isAdded())
                dialogAllergies.show(getFragmentManager(), "allergies");
            }
        });
    }

    private void allergiesSelectedText(@NotNull ArrayList<String> items) {
        allergiesText.setText(R.string.allergies_text_empty);
        if(items.size() != 0){
            StringBuilder stringBuilder = new StringBuilder();
            for (String item : items) {
                stringBuilder.append(item).append(", ");
            }
            allergiesText.setText(stringBuilder.toString().substring(0, stringBuilder.length() - 2));
        }
    }

    private void setupDietsDialog(ArrayList<Preference> diets) {
        final MultiSelectDialog dialogDiets = new MultiSelectDialog();
        dialogDiets.getItems().add("None");
        for(Preference pref: diets){
            dialogDiets.getItems().add(pref.getValue());
        }
        dialogDiets.setSelectedItems(dietsSelected);
        dietText.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialogDiets.setOnItemsSelectedListener(new MultiSelectDialog.OnItemsSelectedListener() {
                    @Override
                    public void onItemsSelectedListener(@NotNull ArrayList<String> items) {
                        dietsSelected = items;
                        dietsSelectedText(items);
                    }
                });
                if(!dialogDiets.isAdded())
                dialogDiets.show(getFragmentManager(), "diets");
            }
        });
    }

    private void dietsSelectedText(@NotNull ArrayList<String> items) {

        dietText.setText(R.string.dietary_text_empty);
        if(items.size() != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String item : items) {
                stringBuilder.append(item).append(", ");
            }
            dietText.setText(stringBuilder.toString().substring(0, stringBuilder.length() - 2));
        }
    }

    private final LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> modifierLoaderCallbacks =
		new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>() {

		@Override
		public Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> onCreateLoader(int id, Bundle args) {
			Log.d("pete2", "Loader called");

			return new EpicuriLoader<ArrayList<EpicuriMenu.ModifierGroup>>(EditMenuItemActivity.this, new ModifierGroupLoaderTemplate());
		}

		@Override
		public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> loader, LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>> data) {

			flag++;
			if(flag == 1){
				if(null == data) return;
				modifierGroups = data.getPayload();

				modifierGroupsList.removeAllViews();

				if (null == selectedModifiers) {
					selectedModifiers = new ArrayList<ModifierGroup>();
					if (null != item) {
						String[] itemModifierGroups = item.getModifierGroupIds();
						for (ModifierGroup g : modifierGroups) {
							for (int j = 0; j < itemModifierGroups.length; j++) {
								if (g.getId().equals(itemModifierGroups[j])) {
									selectedModifiers.add(g);
									break;
								}
							}
						}
					}
				}

				for(ModifierGroup g: modifierGroups){
					final View v = getLayoutInflater().inflate(R.layout.row_checkable_group, modifierGroupsList, false);

					((TextView)v.findViewById(android.R.id.text1)).setText(g.getName());
					final CheckBox c = (CheckBox) v.findViewById(android.R.id.checkbox);
					c.setSaveEnabled(false); // I'll persist the state myself, thanks

					c.setChecked(selectedModifiers.contains(g));

					c.setOnCheckedChangeListener(modifierChangeListener);
					v.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							c.performClick();
						}
					});
					c.setTag(g);
					modifierGroupsList.addView(v);
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>> loader) {
		}

	};

	private final LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Printer>>> printerLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Printer>>>() {

		@Override
		public Loader<LoaderWrapper<ArrayList<EpicuriMenu.Printer>>> onCreateLoader(int id,
		                                                                            Bundle args) {
			return new EpicuriLoader<ArrayList<EpicuriMenu.Printer>>(EditMenuItemActivity.this, new PrinterLoaderTemplate());
		}

		@Override
		public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Printer>>> loader,
		                           LoaderWrapper<ArrayList<EpicuriMenu.Printer>> data) {
			if(null == data) return;
			printers = data.getPayload();

			ArrayAdapter<?> adapter = new ArrayAdapter<EpicuriMenu.Printer>(EditMenuItemActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1, printers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			printerSpinner.setAdapter(adapter);

			if(null != item){
				// set current selection
				for(int i=0; i<printers.size(); i++){
					if(printers.get(i).getId().equals(item.getDefaultPrinterId())){
						printerSpinner.setSelection(i);
						break;
					}
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriMenu.Printer>>> loader) {

		}

	};

	private final LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriVatRate>>> vatLoaderCallbacks = new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriVatRate>>>() {

		@Override
		public Loader<LoaderWrapper<ArrayList<EpicuriVatRate>>> onCreateLoader(int id,
		                                                                       Bundle args) {
			return new EpicuriLoader<ArrayList<EpicuriVatRate>>(EditMenuItemActivity.this, new VatRateLoaderTemplate());
		}

		@Override
		public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriVatRate>>> loader,
		                           LoaderWrapper<ArrayList<EpicuriVatRate>> data) {
			if(null == data) return;
			vatRates = data.getPayload();

			ArrayAdapter<?> adapter = new ArrayAdapter<EpicuriVatRate>(EditMenuItemActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1, vatRates);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			vatSpinner.setAdapter(adapter);

			if(null != item){
				// set current selection
				for(int i=0; i<vatRates.size(); i++){
					if(vatRates.get(i).getId().equals(item.getTaxTypeid())){
						vatSpinner.setSelection(i);
						break;
					}
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriVatRate>>> loader) {
		}
	};

	private ArrayList<ModifierGroup> selectedModifiers;
	private CompoundButton.OnCheckedChangeListener modifierChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ModifierGroup g = (ModifierGroup) buttonView.getTag();
			if(isChecked){
				selectedModifiers.add(g);
			} else {
				selectedModifiers.remove(g);
			}
		}
	};

	private void loadItem(Item item){
		if(null != item){
			setTitle("Editing " + item.getName());
			name.setText(item.getName());
			shortCode.setText(item.getShortCode());
			price.setText(LocalSettings.formatMoneyAmount(item.getPrice(), false));
			description.setText(item.getDescription());
			itemtypeSpinner.setSelection(item.getItemTypeId());
			unavailable.setChecked(item.isUnavailable());
			imageUrl = item.getImageUrl();
			if(imageUrl != null && !imageUrl.isEmpty()) Picasso.get().load(imageUrl).into(itemImage);
			plu.setText(item.getPlu());
		} else {
			setTitle("New Menu Item");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_editmenuitem, menu);
		menu.findItem(R.id.menu_delete).setVisible(item != null);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home: {
			finish();
			return true;
		}
		case R.id.menu_save:{
			save();
			return true;
		}
		case R.id.menu_delete:{
			new AlertDialog.Builder(this).setTitle("Delete item").setMessage(getString(R.string.delete_menuitem_prompt, this.item.getName()))
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							delete();
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private int validate(){
		price.setError(null);
		name.setError(null);
		plu.setError(null);
		try{
		    String priceStr = price.getText().toString().replaceAll(",", "");
			LocalSettings.parseCurrency(priceStr);
		} catch (IllegalArgumentException | ArithmeticException e){
			price.setError("Amount not recognised");
			return R.string.menu_item_validation_price;
		}

		if(null == printers || printers.size() == 0){
			return R.string.menu_item_validation_printer;
		}
		if(name.getText().length() == 0){
			name.setError("Cannot be empty");
			return R.string.menu_item_validation_name;
		}
		if(pluStockCount.getText() != null
				&& pluStockCount.getText().length()>0
				&& plu.getText() != null
				&& plu.getText().length() == 0) {
			plu.setError("Cannot be empty when stock level is populated");
			return R.string.menu_item_validation_empty_sku;
		}
		return -1;
	}

	private void delete(){
		WebServiceCall call = new DeleteMenuLevelWebServiceCall(item.getId(), MenuLevelFragment.Level.ITEM, "-1");
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				Toast.makeText(EditMenuItemActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
				setResult(1);
				finish();
			}
		});
		task.setIndicatorText("Deleting item");
		task.execute();
	}

	private void save(){
		int validationMessage = validate();
		if(validationMessage != -1){
			new AlertDialog.Builder(this)
					.setTitle("Errors exist").setMessage(validationMessage)
					.setPositiveButton("OK",null)
					.show();
			return;
		}
		if(null == vatRates ){
			Toast.makeText(this, "Data not loaded", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			updateStockCount();
		} catch (Exception ex){}

		EpicuriMenu.Printer printer = printers == null || printers.size() == 0 ? null : printers.get(printerSpinner.getSelectedItemPosition());
		EpicuriVatRate vat = (EpicuriVatRate) vatSpinner.getSelectedItem();
		int itemTypeId = itemtypeSpinner.getSelectedItemPosition();

		String[] groups;
		String autoGroupId = getIntent().getStringExtra(GlobalSettings.EXTRA_AUTO_GROUP_ID);
		if(autoGroupId != null &&!autoGroupId.equals("uk.co.epicuri.AUTO_GROUP_ID")){
			groups = new String[]{autoGroupId+""};
		} else {
			groups = new String[0];
		}
		WebServiceCall call;
        String priceStr = price.getText().toString().replaceAll(",", "");
		Money money = LocalSettings.parseCurrency(priceStr);

		ArrayList<String> allergiesKeys = new ArrayList<>();
		if (allergiesSelected != null && allergiesSelected.size() != 0)
        for (String allergy : allergiesSelected) {
               allergiesKeys.add(preferences.getAllergyKey(allergy));
        }

        if(unavailable.isChecked() && associatedStockLevel != null && associatedStockLevel.getLevel() > 0 && associatedStockLevel.isTrackable()) {
        	unavailable.setChecked(false);
		} else if(!unavailable.isChecked() && associatedStockLevel != null && associatedStockLevel.getLevel() == 0 && associatedStockLevel.isTrackable()) {
			unavailable.setChecked(true);
		}

        ArrayList<String> dietsKeys = new ArrayList<>();
        if(dietsSelected != null && dietsSelected.size() != 0) {
            for (String diet : dietsSelected) {
                dietsKeys.add(preferences.getDietKey(diet));
            }
        }
		if(null == item){
			call = new CreateEditMenuItemWebServiceCall(
				    name.getText(),
				    money,
				    description.getText(),
				    vat.getId(),
				    itemTypeId,
				    printer,
				    selectedModifiers,
				    new EpicuriMenu.Tag[0],
				    groups,
				    unavailable.isChecked(),
				    shortCode.getText().toString(),
                    allergiesKeys,
                    dietsKeys,
                    imageUrl,
					plu.getText());
		} else {
			call = new CreateEditMenuItemWebServiceCall(
					item.getId(),
					name.getText(),
					money,
					description.getText(),
					vat.getId(),
					itemTypeId,
					printer,
					selectedModifiers,
					item.getTags(),
					item.getMenuGroups(),
					unavailable.isChecked(),
					shortCode.getText().toString(),
                    allergiesKeys,
                    dietsKeys,
                    imageUrl,
					plu.getText());
		}
		WebServiceTask itemUpdateTask = new WebServiceTask(this, call, true);
		itemUpdateTask.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				setResult(1);
				finish();
			}
		});
		itemUpdateTask.setOnErrorListener(new WebServiceTask.OnErrorListener() {
			@Override
			public void onError(int code, String response) {
				Toast.makeText(EditMenuItemActivity.this, response, Toast.LENGTH_LONG).show();
			}
		});
		itemUpdateTask.setIndicatorText(getString(R.string.webservicetask_alertbody));
		itemUpdateTask.execute();
	}

	private void updateStockCount() {
		if(item == null) {
			return;
		}
		String count = pluStockCount.getText().toString();
		boolean countWasFilled = true;
		if(count.equals("")) {
			count = "0";
			countWasFilled = false;
		}
		int stockCount = Integer.parseInt(count);
		if(stockCount < 0) {
			Toast.makeText(this, "Cannot set stock level to a negative number", Toast.LENGTH_SHORT).show();
			return;
		}


		String newPlu = plu.getText().toString().trim();
		if(associatedStockLevel == null && newPlu.length() > 0) {
			StockLevel stockLevel = new StockLevel(null, null, newPlu, stockCount, countWasFilled && associatedStockLevel == null);
			executeUpdateStockCount(stockLevel);
		}
		else if(stockLevelPluHasChanged(newPlu) && !stockLevelPluExists(newPlu)) {
			StockLevel stockLevel = new StockLevel(null, null, newPlu, stockCount, countWasFilled && associatedStockLevel == null);
			executeUpdateStockCount(stockLevel);
		}
		else if (stockLevelCountHasChanged(stockCount)) {
			// stock level has been updated
			associatedStockLevel.setLevel(stockCount);
			executeUpdateStockCount(associatedStockLevel);
		}

	}

	private boolean stockLevelPluExists(String newPlu) {
		return findStockLevelPlu(newPlu) != null;
	}

	private StockLevel findStockLevelPlu(String newPlu) {
		for(StockLevel stockLevel : allStockLevels) {
			if(stockLevel.getPlu().equals(newPlu)) {
				return stockLevel;
			}
		}

		return null;
	}

	private void executeUpdateStockCount(StockLevel stockLevel) {
		WebServiceTask updateStockTask = new WebServiceTask(this, new CreateEditStockLevelWebServiceCall(stockLevel), false);
		updateStockTask.setIndicatorText(getString(R.string.webservicetask_alertbody));
		updateStockTask.execute();
	}

	private boolean stockLevelCountHasChanged(int stockCount) {
		return associatedStockLevel != null && associatedStockLevel.getLevel() != stockCount;
	}

	private boolean stockLevelPluHasChanged(String plu) {
		return associatedStockLevel != null && !associatedStockLevel.getPlu().equals(plu);
	}
}
