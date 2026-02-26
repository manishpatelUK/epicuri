package uk.co.epicuri.waiter.ui.menueditor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuAdapter;
import uk.co.epicuri.waiter.interfaces.SaveMenuListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.loaders.templates.MenuSummaryLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenuSummary;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateEditMenuWebServiceCall;
import uk.co.epicuri.waiter.webservice.SetTakeawayMenuWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class MenuFragment extends Fragment implements OnItemClickListener {

	private static final int LOADER_MENUS = 1;
	private static final String FRAGMENT_EDIT_MENU = "NewMenu";

	private DragSortListView menuList;
	private MenuAdapter adapter;
	public List<EpicuriMenuSummary> menus = null;
	private String takeawayMenuId;
	private ProgressDialog dialog;
    private ActionMode reorderActionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		takeawayMenuId = LocalSettings.getInstance(getActivity()).getCachedRestaurant().getTakeawayMenuId() + "";
		dialog = new ProgressDialog(getActivity());
		dialog.setMessage("Refreshing...");
		dialog.setCancelable(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		menuList = new DragSortListView(getActivity(), null);
		menuList.setBackgroundColor(getResources().getColor(R.color.lightgray));
		menuList.setOnItemClickListener(this);
		menuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		menuList.setDropListener(onDrop);
		menuList.setDragListener(onDrag);
        DragSortController controller = new DragSortController(menuList);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setBackgroundColor(getActivity().getResources().getColor(R.color.blue));
        menuList.setFloatViewManager(controller);
        menuList.setOnTouchListener(controller);
		return menuList;
	}

    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            if (from != to) {
                adapter.drop(from, to);
            }
        }
    };

    private DragSortListView.DragListener onDrag = new DragSortListView.DragListener() {
        @Override
        public void drag(int from, int to) {
            if (null == reorderActionMode && null == mMode) {//TODO check mMode
                reorderActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new MenuFragment.DragActionMode());
                if (null != reorderActionMode) reorderActionMode.invalidate();
                menuList.setChoiceMode(ListView.CHOICE_MODE_NONE);
            }
        }
    };

	@Override
	public void onResume() {
		super.onResume();
        refreshMenus();
    }

    private void refreshMenus() {
        getLoaderManager().restartLoader(LOADER_MENUS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<List<EpicuriMenuSummary>>>() {

            @Override
            public Loader<LoaderWrapper<List<EpicuriMenuSummary>>> onCreateLoader(
                    int id, Bundle args) {
                return new EpicuriLoader<>(getActivity(), new MenuSummaryLoaderTemplate(true));
            }

            @Override
            public void onLoadFinished(
                    Loader<LoaderWrapper<List<EpicuriMenuSummary>>> loader,
                    LoaderWrapper<List<EpicuriMenuSummary>> data) {
                if(null == data){ // nothing returned, ignore
                    return;
                }else if(data.isError()){
                    Toast.makeText(getActivity(), "error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                onLoaded(data.getPayload());
            }

            @Override
            public void onLoaderReset(
                    Loader<LoaderWrapper<List<EpicuriMenuSummary>>> loader) {
                menuList.setAdapter(null);
            }
        });
    }

    private void onLoaded(List<EpicuriMenuSummary> menuSummaries){
        if (dialog.isShowing())
            dialog.dismiss();

        menus = menuSummaries;
        menuList.setAdapter(adapter = new MenuAdapter(getActivity(), menus, takeawayMenuId));
        adapter.notifyDataSetChanged();
        if(null != mMode) mMode.finish();
    }

    protected void refresh(){
		if (!dialog.isShowing()) dialog.show();
		UpdateService.requestUpdate(getActivity(), ((EpicuriLoader<?>)(Loader<?>)getLoaderManager().getLoader(LOADER_MENUS)).getContentUri());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_menumanager, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_menuitems: {
				Intent intent = new Intent(getActivity(), MenuItemsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.menu_menumodifiers: {
				Intent intent = new Intent(getActivity(), ModifierGroupsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.menu_add: {
				editMenu(null);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}


	public static ActionMode mMode;
	private EpicuriMenuSummary selectedMenu = null;

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		EpicuriMenuSummary newMenu = menus.get(position);
		if(newMenu.equals(selectedMenu)){
			// "double click"
			showMenu(selectedMenu);
			mMode.finish();
			return;
		}

		selectedMenu = newMenu;
		if(null == mMode){
			mMode = ((AppCompatActivity)getActivity()).startSupportActionMode(new MenuActionMode());
			// EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
			if(null != mMode) mMode.invalidate();
		} else {
			mMode.invalidate();
		}
	}

	private class MenuActionMode implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getActivity().getMenuInflater().inflate(R.menu.action_editmenu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			if(null == selectedMenu) throw new IllegalStateException(getString(R.string.no_menu_selected));

			// hide or disable the "choose takeaway" option
			MenuItem setTakeaway = menu.findItem(R.id.menu_chooseTakeaway);
			if(takeawayMenuId.equals("0")){
				// not visible if restaurant doesn't do takeaways
				setTakeaway.setVisible(false);
			} else {
				setTakeaway.setVisible(true);
				// not enabled if this menu is the current takeaway menu
				setTakeaway.setEnabled(!selectedMenu.getId().equals(takeawayMenuId));
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){
				case R.id.menu_edit: {
					editMenu(selectedMenu);
					mode.finish();
					return true;
				}
				case R.id.menu_editmenu: {
					showMenu(selectedMenu);
					mode.finish();
					return true;
				}
				case R.id.menu_delete: {
					final String deleteMenuId = selectedMenu.getId();
					new AlertDialog.Builder(getActivity()).setTitle("Delete " + selectedMenu.getName() + "?")
							.setMessage(R.string.deleteMenuMessage)
							.setNegativeButton("Cancel", null)
							.setPositiveButton("Yes, delete it", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									((SaveMenuListener)getActivity()).deleteMenu(deleteMenuId);
								}
							})
							.show();
					mode.finish();
					return true;
				}
				case R.id.menu_chooseTakeaway: {
					setTakeaway(selectedMenu);
					mode.finish();
					return true;
				}
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			selectedMenu = null;

			int currentItem = menuList.getCheckedItemPosition();
			if(AdapterView.INVALID_POSITION != currentItem){
				// deselect current item
				menuList.setItemChecked(currentItem, false);
			}
			mMode = null;
		}
	}

	private void setTakeaway(final EpicuriMenuSummary takeawayMenu) {
		new AlertDialog.Builder(getActivity()).setTitle("Are you sure?")
				.setMessage(String.format("Do you want to set the takeaway menu for your restaurant to %s?\n\nThis will take effect immediately on the customer apps", takeawayMenu.getName()))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						WebServiceCall call = new SetTakeawayMenuWebServiceCall(takeawayMenu.getId());
						WebServiceTask task = new WebServiceTask(getActivity(), call, true);
						task.setIndicatorText("Setting takeaway menu");
						task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

							@Override
							public void onSuccess(int code, String response) {
								Toast.makeText(getActivity(), "Takeaway menu has been changed", Toast.LENGTH_SHORT).show();
								takeawayMenuId = takeawayMenu.getId();
								adapter.setTakeaway(takeawayMenuId );
								adapter.notifyDataSetChanged();
							}
						});
						task.execute();
					}
				})
				.setNegativeButton("No", null)
				.show();
	}

	private void showMenu(EpicuriMenuSummary menu){
		MenuLevelFragment frag = new MenuLevelFragment();
		Bundle arguments = new Bundle();
		arguments.putString(GlobalSettings.EXTRA_MENU_ID, menu.getId());
		frag.setArguments(arguments);

		getFragmentManager().beginTransaction()
				.replace(R.id.frame, frag)
				.addToBackStack(null)
				.setBreadCrumbTitle(menu.getName())
				.commit();
	}

	private void editMenu(EpicuriMenuSummary menu){
		EditMenuDialogFragment frag = new EditMenuDialogFragment();

		ArrayList<String> otherMenuNames = new ArrayList<String>(menus.size());
		for(EpicuriMenuSummary m: menus){
			if(m != menu){
				otherMenuNames.add(m.getName());
			}
		}
		Bundle args = new Bundle();
		args.putStringArrayList(EditMenuDialogFragment.EXTRA_OTHER_MENU_NAMES, otherMenuNames);
		if(null != menu){
			args.putParcelable(GlobalSettings.EXTRA_MENUITEM, menu);
		}
		frag.setArguments(args);
		frag.show(getFragmentManager(), FRAGMENT_EDIT_MENU);
	}

	private boolean compareLists(List<EpicuriMenuSummary> list1, List<EpicuriMenuSummary> list2) {
		if (list1 == null || list2 == null) return false;
		if (list1.size() != list2.size()) return false;

		for(int i = 0; i < list1.size(); ++i) {
			if (!list1.get(i).equals(list2.get(i))) return false;
		}

		return true;
	}

	private class DragActionMode implements ActionMode.Callback{

        @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add("Save order");
            return true;
        }

        @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            updateOrder(getActivity(), "Saving order...");
            reorderActionMode.finish();
            return true;
        }

        @Override public void onDestroyActionMode(ActionMode mode) {
            reorderActionMode = null;
            menuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    private void updateOrder(Context context, String s) {
        if(menus == null) return;
        for(int i = 0; i < menus.size(); i++){
            EpicuriMenuSummary menuSummary = menus.get(i);
            if (menuSummary.getOrder() != i){
                WebServiceCall call = new CreateEditMenuWebServiceCall(menuSummary.getId(),
                        menuSummary.getName(),
                        menuSummary.isActive(),
                        i);
                WebServiceTask task = new WebServiceTask(context, call);
                if (s != null) task.setIndicatorText(s);
                task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                    @Override public void onSuccess(int code, String response) {
                        refresh();
                    }
                });
                task.execute();

            }
        }
    }
}