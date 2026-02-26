package uk.co.epicuri.waiter.ui.menueditor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.NewModifierGroupListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.ModifierGroupLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu.ModifierGroup;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.CreateEditModifierGroupWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class ModifierGroupsActivity extends EpicuriBaseActivity implements NewModifierGroupListener {

	private ArrayList<ModifierGroup> modifierGroups = null;
	private ArrayAdapter<ModifierGroup> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		Utils.initActionBar(this);
		
		final ListView lv = new ListView(this);
		setContentView(lv);
		lv.setBackgroundColor(getResources().getColor(R.color.lightgray));

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				ModifierGroup group = (ModifierGroup)adapter.getItemAtPosition(position);
				Intent intent = new Intent(ModifierGroupsActivity.this, EditModifierGroupActivity.class);
				intent.putExtra(GlobalSettings.EXTRA_MODIFIER_GROUP, (Parcelable) group);
				startActivity(intent);
			}
			
		});
		
		getSupportLoaderManager().restartLoader(GlobalSettings.LOADER_MODIFIER_GROUPS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<ModifierGroup>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<ModifierGroup>>> onCreateLoader(
					int id, Bundle args) {
				return new EpicuriLoader<>(ModifierGroupsActivity.this, new ModifierGroupLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader,
					LoaderWrapper<ArrayList<ModifierGroup>> data) {
				if(null == data) return;
				modifierGroups = data.getPayload();

				adapter = new ArrayAdapter<>(ModifierGroupsActivity.this, android.R.layout.simple_list_item_1, modifierGroups);
				lv.setAdapter(adapter);	
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<ModifierGroup>>> loader) {
			}
		}); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fragment_editmenu, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_add: {
			NewModifierGroupFragment newModifier = new NewModifierGroupFragment();
			newModifier.show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_NEW_MODIFIER_GROUP);
			return true;
		}
		case android.R.id.home: {
			Intent intent = new Intent(this, EditMenuActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void createNewModifierGroup(CharSequence name){

		WebServiceTask task = new WebServiceTask(this, new CreateEditModifierGroupWebServiceCall(name, 0, 1), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				ModifierGroup newGroup = null;
				try{
					JSONObject responseJson = new JSONObject(response);
					newGroup = new ModifierGroup(responseJson);
				} catch (JSONException e){
					throw new RuntimeException(e);
				}
				Intent intent = new Intent(ModifierGroupsActivity.this, EditModifierGroupActivity.class);
				intent.putExtra(GlobalSettings.EXTRA_MODIFIER_GROUP, (Parcelable) newGroup);
				startActivity(intent);		
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}
	
}
