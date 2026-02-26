package uk.co.epicuri.waiter.ui.menueditor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuItemAdapter;
import uk.co.epicuri.waiter.interfaces.OnMenuItemsSelectedListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.MenuItemLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Group;
import uk.co.epicuri.waiter.model.EpicuriMenu.Item;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.utils.Utils;


public class MenuItemSelectorFragment extends DialogFragment implements TextWatcher{
	private static final int LOADER_MENUITEMS = 1;
	private ListView lv;
	private ArrayList<EpicuriMenu.Item> items;
	private MenuItemAdapter adapter;

	@InjectView(R.id.search_edit)
	EditText searchBox;

	private EpicuriMenu.Group group;

	public static MenuItemSelectorFragment newInstance(Group group, String menuId){
		if(null == group) return null;

		MenuItemSelectorFragment frag = new MenuItemSelectorFragment();
		Bundle args = new Bundle();
		args.putParcelable(GlobalSettings.EXTRA_GROUP, group);
		args.putString(GlobalSettings.EXTRA_MENU_ID, menuId);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		group = getArguments().getParcelable(GlobalSettings.EXTRA_GROUP);

		View containerView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_menuitemselector, null, false);
		ButterKnife.inject(this, containerView);

		searchBox.addTextChangedListener(this);
		lv = (ListView)containerView.findViewById(android.R.id.list);
		adapter = new MenuItemAdapter(getActivity(), true);

		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				adapter.setItemChecked(adapter.filteredItems.get(position).getId(), !adapter.isItemChecked(id));
				Utils.closeKeyboard(getActivity(), v.getWindowToken());
			}

		});

		getLoaderManager().restartLoader(LOADER_MENUITEMS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Item>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<Item>>> onCreateLoader(int id,
																		 Bundle args) {
				return new EpicuriLoader<>(getActivity(), new MenuItemLoaderTemplate(false));
			}

			@Override
			public void onLoadFinished(Loader<LoaderWrapper<ArrayList<Item>>> loader, LoaderWrapper<ArrayList<Item>> data) {
				if(null == data) return;
				items = data.getPayload();
				adapter.swapData(items, group.getItemIds());
				lv.setAdapter(adapter);
			}

			@Override
			public void onLoaderReset(Loader<LoaderWrapper<ArrayList<Item>>> loader) {
			}

		});
		AlertDialog d = new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.choose_menu_items))
				.setView(containerView)
				.setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						save();
					}
				})
				.create();
		return d;
	}

	private void save(){
		// get a list of the checked ID's
		ArrayList<String> menuItemIds = adapter.getCheckedItemIds();

		Intent data = new Intent();
		data.putExtra(GlobalSettings.EXTRA_MENUITEM_IDS, menuItemIds);

		((OnMenuItemsSelectedListener)getActivity()).selectMenuItems(group, menuItemIds, getArguments().getString(GlobalSettings.EXTRA_MENU_ID));
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		adapter.getFilter().filter(s.toString());
	}
}