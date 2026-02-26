package uk.co.epicuri.waiter.ui.menueditor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.joda.money.Money;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.ModifierValueEditListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.VatRateLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierValue;
import uk.co.epicuri.waiter.model.EpicuriVatRate;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateEditModifierGroupWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditModifierValueWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteModifierGroupWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteModifierValueWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;


public class EditModifierGroupActivity extends EpicuriBaseActivity implements ModifierValueEditListener {

	private static final String FRAGMENT_MODIFIER_VALUE = "newValue";

	private static final int LOADER_MODIFIER = 1;
	private static final int LOADER_VAT = 2;

	private ModifierGroup group;
	private String modifierGroupId;

	private EditText name;
	private EditText min;
	private EditText max;
	private ListView values;

	private ArrayList<EpicuriVatRate> vatRates;
	private ArrayList<ModifierValue> modifierValues = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		group = getIntent().getParcelableExtra(GlobalSettings.EXTRA_MODIFIER_GROUP);
		Collections.addAll(modifierValues, group.getModifierValues());
		if (null == group) throw new IllegalArgumentException("Group cannot be null");
		modifierGroupId = group.getId();

		setContentView(R.layout.activity_edit_modifier_group);

		name = (EditText) findViewById(R.id.name_edit);
		min = (EditText) findViewById(R.id.min_edit);
		max = (EditText) findViewById(R.id.max_edit);
		values = (ListView) findViewById(R.id.values_list);

		values.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				ModifierValue value = modifierValues.get(position);
				ModifierValueFragment frag = ModifierValueFragment.newInstance(group.getId(), value, vatRates);
				frag.show(getSupportFragmentManager(), FRAGMENT_MODIFIER_VALUE);
			}
		});

		// Show the custom action bar view and hide the normal Home icon and title.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		View addButton = findViewById(R.id.newValue_button);
		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ModifierValueFragment frag = ModifierValueFragment.newInstance(group.getId(), vatRates);
				frag.show(getSupportFragmentManager(), FRAGMENT_MODIFIER_VALUE);
			}
		});


		if (null == group) {
			setTitle("Create new modifier group");
		} else {
			setModifierGroup();
		}

		getSupportLoaderManager().restartLoader(LOADER_VAT, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriVatRate>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<EpicuriVatRate>>> onCreateLoader(
					int arg0, Bundle arg1) {
				return new EpicuriLoader<>(EditModifierGroupActivity.this, new VatRateLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<EpicuriVatRate>>> loader,
					LoaderWrapper<ArrayList<EpicuriVatRate>> data) {
				if (null == data) return;
				if (data.isError()) {
					Toast.makeText(EditModifierGroupActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				vatRates = data.getPayload();
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<EpicuriVatRate>>> arg0) {
				// TODO Auto-generated method stub

			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_editmodifiergroup, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save: {
				save();
				return true;
			}
			case R.id.menu_delete: {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                WebServiceTask task = new WebServiceTask(EditModifierGroupActivity.this, new DeleteModifierGroupWebServiceCall(group.getId()), true);
                                task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                                    @Override
                                    public void onSuccess(int code, String response) {
                                        refresh();
                                        finish();
                                    }
                                });
                                task.setIndicatorText(getString(R.string.webservicetask_alertbody));
                                task.execute();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
				dialog.show();

				return true;
			}
			case android.R.id.home: {
                this.onBackPressed();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void save() {
		WebServiceTask task = new WebServiceTask(this, new CreateEditModifierGroupWebServiceCall(
				group.getId(),
				name.getText(),
				Integer.parseInt(min.getText().toString()),
				Integer.parseInt(max.getText().toString())
		), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				finish();
			}

		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	@Override
	public void createModifier(CharSequence name, Money price, String vat, String plu) {
		CreateEditModifierValueWebServiceCall call = new CreateEditModifierValueWebServiceCall(
				name,
				price,
				vat,
				plu,
				group.getId());
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				ModifierValue value = null;
				try {
					JSONObject responseJson = new JSONObject(response);
					value = new ModifierValue(responseJson);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}

				modifierValues.add(value);
				refresh();
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		// TODO: add the newly created menuitem to the menugroup
//		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
//
//			@Override
//			public void onSuccess(int code, String response) {
//			}
//		});
		task.execute();
	}

	@Override
	public void updateModifier(final String id, final CharSequence name, final Money price, final String vat, String plu) {
		CreateEditModifierValueWebServiceCall call = new CreateEditModifierValueWebServiceCall(id, name, price, vat, plu, group.getId());
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				for(ModifierValue modifierValue : modifierValues){
					if (modifierValue.getId().equals(id)){
						modifierValue.setName(name.toString());
						modifierValue.setPrice(price);
						modifierValue.setTaxTypeId(vat);

						break;
					}
				}

				final ArrayAdapter<ModifierValue> adapter = new ArrayAdapter<ModifierValue>(EditModifierGroupActivity.this, android.R.layout.simple_list_item_1, modifierValues);

				EditModifierGroupActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						values.setAdapter(adapter);
					}
				});
			}
		});
		task.execute();

	}

	@Override
	public void deleteModifier(final String id) {
		DeleteModifierValueWebServiceCall call = new DeleteModifierValueWebServiceCall(id);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				for (ModifierValue value : modifierValues)
					if (value.getId().equals(id)) {
						modifierValues.remove(value);
						break;
					}

				final ArrayAdapter<ModifierValue> adapter = new ArrayAdapter<ModifierValue>
						(EditModifierGroupActivity.this, android.R.layout.simple_list_item_1, modifierValues);
				EditModifierGroupActivity.this.runOnUiThread(new Runnable() {
					@Override public void run() {
						values.setAdapter(adapter);
					}
				});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

    @Override
    public void onBackPressed() {
	    if(isFieldsChanged()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("Unsaved changes will be lost!")
                    .setPositiveButton("Save & Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            save();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton("Exit without saving", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(EditModifierGroupActivity.this, ModifierGroupsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }
                    });
            builder.show();
        } else {
            Intent intent = new Intent(EditModifierGroupActivity.this, ModifierGroupsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    private boolean isFieldsChanged(){
	    return (!name.getText().toString().equals(group.getName()) ||
                !min.getText().toString().equals(String.valueOf(group.getLowerLimit())) ||
                !max.getText().toString().equals(String.valueOf(group.getUpperLimit())));

    }

    private void refresh(){
		getSupportLoaderManager().restartLoader(LOADER_MODIFIER, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.ModifierGroup>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<ModifierGroup>>> onCreateLoader(
					int arg0, Bundle arg1) {
				return new EpicuriLoader<ArrayList<ModifierGroup>>(EditModifierGroupActivity.this, new ModifierGroupLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> arg0,
					LoaderWrapper<ArrayList<ModifierGroup>> data) {
				if(null == data) return;
				if(data.isError()){
					Toast.makeText(EditModifierGroupActivity.this, "error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				for(ModifierGroup g:data.getPayload()){
					if(g.getId().equals(modifierGroupId)){
						group = g;
						break;
					}
				}

				EditModifierGroupActivity.this.runOnUiThread(new Runnable() {
					@Override public void run() {
						setModifierGroup();
					}
				});
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> arg0) {
				// TODO Auto-generated method stub

			}


		});
	}
	private void setModifierGroup(){
		setTitle("Editing " + group.getName());

		name.setText(group.getName());
		min.setText(String.valueOf(group.getLowerLimit()));
		max.setText(String.valueOf(group.getUpperLimit()));
		ArrayAdapter<ModifierValue> adapter = new ArrayAdapter<ModifierValue>(EditModifierGroupActivity.this, android.R.layout.simple_list_item_1, modifierValues);
		values.setAdapter(adapter);
	}
}