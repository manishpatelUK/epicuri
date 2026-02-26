package uk.co.epicuri.waiter.ui.menueditor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuItemAdapter;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.MenuItemLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;

public class MenuItemsActivity extends EpicuriBaseActivity implements LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Item>>>{
	private static final int LOADER_MENUITEMS = 1;

	private static final int REQUEST_EDIT_ITEM = 1;

	private ListView lv;
	private ArrayList<EpicuriMenu.Item> items;
	private MenuItemAdapter adapter;
	private CheckBox showOrphans;
	private TextView emptyText;
	SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

		Utils.initActionBar(this);

		setContentView(R.layout.activity_menuitems);

		showOrphans = (CheckBox)findViewById(R.id.orphans_check);
		showOrphans.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getSupportLoaderManager().restartLoader(LOADER_MENUITEMS, null, MenuItemsActivity.this);
				invalidateOptionsMenu();
			}
		});

		CheckBox searchDetails = (CheckBox) findViewById(R.id.search_details);
		searchDetails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				adapter.setSearchDetails(b);
				if(searchView != null) adapter.getFilter().filter(searchView.getQuery());
				adapter.notifyDataSetChanged();
			}
		});

		CheckBox searchSKU = (CheckBox) findViewById(R.id.search_sku);
		searchSKU.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				adapter.setSearchSKU(b);
				if(searchView != null) adapter.getFilter().filter(searchView.getQuery());
				adapter.notifyDataSetChanged();
			}
		});

		CheckBox noAvailable = findViewById(R.id.not_available);
		noAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				adapter.showNotAvailable(isChecked);
				if(searchView != null) adapter.getFilter().filter(searchView.getQuery());
				adapter.notifyDataSetChanged();
			}
		});

		emptyText = (TextView)findViewById(android.R.id.empty);

		lv = (ListView)findViewById(android.R.id.list);
		adapter = new MenuItemAdapter(MenuItemsActivity.this, false);
		lv.setAdapter(adapter);
		lv.setEmptyView(findViewById(android.R.id.empty));

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
									long id) {
				EpicuriMenu.Item item = (EpicuriMenu.Item)adapter.getItemAtPosition(position);
				Intent intent = new Intent(MenuItemsActivity.this, EditMenuItemActivity.class);
				intent.putExtra(GlobalSettings.EXTRA_MENUITEM, (Parcelable) item);
				startActivityForResult(intent, REQUEST_EDIT_ITEM);
			}

		});
		getSupportLoaderManager().restartLoader(LOADER_MENUITEMS, null, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_EDIT_ITEM){
			if(resultCode == 1){
				getSupportLoaderManager().restartLoader(LOADER_MENUITEMS, null, this);
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public Loader<LoaderWrapper<ArrayList<Item>>> onCreateLoader(int id,
																 Bundle args) {
		adapter.swapData(null, null);
		emptyText.setText(R.string.menuedit_loading);
		return new EpicuriLoader<ArrayList<Item>>(getApplicationContext(), new MenuItemLoaderTemplate(showOrphans.isChecked()));
	}

	@Override
	public void onLoadFinished(
			Loader<LoaderWrapper<ArrayList<Item>>> loader,
			LoaderWrapper<ArrayList<Item>> data) {
		if(null == data) return;
		emptyText.setText(R.string.menuedit_empty);
		items = data.getPayload();

		Collections.sort(items, new Comparator<EpicuriMenu.Item>() {

			@Override
			public int compare(Item lhs, Item rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}

		});

		adapter.swapData(items, null);
	}

	@Override
	public void onLoaderReset(Loader<LoaderWrapper<ArrayList<Item>>> loader) {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menuitem, menu);

		MenuItem searchItem = menu.findItem(R.id.menu_search);
		searchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				adapter.getFilter().filter(newText);

				return true;
			}
		});

		searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override public boolean onMenuItemActionExpand(MenuItem item) {
				return true;
			}

			@Override public boolean onMenuItemActionCollapse(MenuItem item) {
				adapter.getFilter().filter("");
				return true;
			}
		});

		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_add: {
				Intent intent = new Intent(this, EditMenuItemActivity.class);
				startActivity(intent);
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
}