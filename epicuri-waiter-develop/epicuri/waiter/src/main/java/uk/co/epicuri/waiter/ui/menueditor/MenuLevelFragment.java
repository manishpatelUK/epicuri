package uk.co.epicuri.waiter.ui.menueditor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.ListView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.MenuItemAdapter;
import uk.co.epicuri.waiter.adapters.MenuLevelAdapter;
import uk.co.epicuri.waiter.interfaces.OnMenuItemsSelectedListener;
import uk.co.epicuri.waiter.interfaces.SaveCategoryListener;
import uk.co.epicuri.waiter.interfaces.SaveGroupListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.MenuLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Category;
import uk.co.epicuri.waiter.model.EpicuriMenu.Group;
import uk.co.epicuri.waiter.model.EpicuriMenu.MenuLevel;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateEditMenuCategoryWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditMenuGroupWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditMenuItemWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class MenuLevelFragment extends Fragment {

    private static final int LOADER_MENU = 1;

    private static final int REQUEST_GET_MENUITEMS = 1;

	public MenuLevelAdapter adapter;
	
	public enum Level {
		MENU(0, "Menu"), CATEGORY(1, "Category"), GROUP(2, "Group"), ITEM(3, "Item");
		
		private final int val;
		private final String name;
		Level(int val, String name){ this.val = val; this.name = name;}
		public static Level fromInt(int val){
			for(Level l: values()){
				if(l.val == val) return l;
			}
			throw new IllegalArgumentException("Val not recognised");
		}
		public int getVal(){
			return val;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	private DragSortListView listView;
	
	private Level level;
	
	private String menuId;
	private String categoryId;
	private String groupId;
	
	private EpicuriMenu theMenu;
	private EpicuriMenu.Category theCategory;
	private EpicuriMenu.Group theGroup;

	private ActionMode editRowActionMode;
	private ActionMode reorderActionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		Bundle extras = getArguments();
	
		menuId = extras.getString(GlobalSettings.EXTRA_MENU_ID);
		if(extras.containsKey(GlobalSettings.EXTRA_CATEGORY_ID)){
			categoryId = extras.getString(GlobalSettings.EXTRA_CATEGORY_ID);
			if(extras.containsKey(GlobalSettings.EXTRA_GROUP_ID)){
				groupId = extras.getString(GlobalSettings.EXTRA_GROUP_ID);
				level = Level.GROUP;
			} else {
				level = Level.CATEGORY;
			}
		} else {
			level = Level.MENU;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
//		View v = inflater.inflate(R.layout.fragment_edit_menu, container, false);
		
		listView = new DragSortListView(getActivity(), null);
		listView.setBackgroundColor(getResources().getColor(R.color.lightgray));

		listView.setDropListener(onDrop);
		listView.setDragListener(onDrag);
		DragSortController mController = buildController(listView);
		// manually override background colour of floating view
		mController.setBackgroundColor(getActivity().getResources().getColor(R.color.blue));
		listView.setFloatViewManager(mController);
		listView.setOnTouchListener(mController);

		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				if(null != reorderActionMode){
					return;
				}

				MenuLevel newLevel = rows.get(position);
				if(newLevel.equals(selectedLevel)){
					// "double click"
					showLevel(selectedLevel);
					editRowActionMode.finish();
					return;
				}

				selectedLevel = newLevel;
				if(null == editRowActionMode){
					editRowActionMode = ((AppCompatActivity)getActivity()).startSupportActionMode(new MenuLevelActionMode());
                    if(null != editRowActionMode) editRowActionMode.invalidate();
                } else {
					editRowActionMode.invalidate();
				}
			}
		});
	
		return listView;
	}
	
	private void showLevel(MenuLevel menuItem){
		MenuLevelFragment newFrag = new MenuLevelFragment();
		Bundle arguments = new Bundle();
		CharSequence label;
		switch(level){
		case MENU: {
			Category cat = (Category)menuItem;
			
			arguments.putString(GlobalSettings.EXTRA_MENU_ID, menuId);
			arguments.putString(GlobalSettings.EXTRA_CATEGORY_ID, cat.getId());
			label = cat.getName();
			break;
		}
		case CATEGORY: {
			Group grp = (Group)menuItem;
			
			arguments.putString(GlobalSettings.EXTRA_MENU_ID, menuId);
			arguments.putString(GlobalSettings.EXTRA_CATEGORY_ID, categoryId);
			arguments.putString(GlobalSettings.EXTRA_GROUP_ID, grp.getId());
			label = grp.getName();
			break;
		}
		case GROUP: {
			EpicuriMenu.Item item = (EpicuriMenu.Item)menuItem;
			Intent intent = new Intent(getActivity(), EditMenuItemActivity.class);
			intent.putExtra(GlobalSettings.EXTRA_MENUITEM, (Parcelable) item);
			startActivity(intent);
			return;
		}
		default:
			throw new IllegalArgumentException("level not recognised " + level);
		}
		newFrag.setArguments(arguments);
		getFragmentManager().beginTransaction().setBreadCrumbTitle(label).addToBackStack(null).replace(R.id.frame, newFrag).commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		getLoaderManager().restartLoader(LOADER_MENU, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriMenu>>() {

            @Override
            public Loader<LoaderWrapper<EpicuriMenu>> onCreateLoader(int id,
                                                                     Bundle args) {
                return new EpicuriLoader<EpicuriMenu>(getActivity(), new MenuLoaderTemplate(menuId));
            }

            @Override
            public void onLoadFinished(Loader<LoaderWrapper<EpicuriMenu>> loader,
                                       LoaderWrapper<EpicuriMenu> data) {

                if (null == data) return;
                if (data.isError()) {
                    Toast.makeText(getActivity(), "Error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                onLoaded(data.getPayload());
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<EpicuriMenu>> loader) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_editmenu, menu);
        Level levelBelow = Level.fromInt(level.val + 1);
        menu.findItem(R.id.menu_add).setTitle("New " + levelBelow.name);
        if (level == Level.GROUP) {
            menu.findItem(R.id.menu_manage).setVisible(true);
			menu.findItem(R.id.sort_items).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add: {
                addItem();
                return true;
            }
            case R.id.menu_manage: {
                MenuItemSelectorFragment frag = MenuItemSelectorFragment.newInstance(theGroup, menuId);
                frag.show(getFragmentManager(), null);
                return true;
            }
			case R.id.sort_items: {
				sortItemsAlphabetically();
				return true;
			}
        }
        return super.onOptionsItemSelected(item);
    }

    private void addItem() {
        // launch intent to add new category/group/item
        switch (level) {
            case MENU: {
                if (theMenu == null) return;
                ArrayList<String> otherNames = new ArrayList<String>(theMenu.getCategories().size());
                for (Category c : theMenu.getCategories()) {
                    otherNames.add(c.getName());
                }
                EditMenuCategoryDialogFragment frag = EditMenuCategoryDialogFragment.newInstance(null, menuId, otherNames);
                frag.show(getFragmentManager(), null);
                return;
            }
            case CATEGORY: {
                ArrayList<String> otherNames = new ArrayList<String>(theCategory.getGroups().size());
                for (Group g : theCategory.getGroups()) {
                    otherNames.add(g.getName());
                }
                EditMenuGroupFragment frag = EditMenuGroupFragment.newInstance(null, categoryId, menuId, otherNames);
                frag.show(getFragmentManager(), null);
                return;
            }
            case GROUP: {
                Intent intent = new Intent(getActivity(), EditMenuItemActivity.class);
                intent.putExtra(GlobalSettings.EXTRA_AUTO_GROUP_ID, theGroup.getId());
                startActivity(intent);
                return;
            }
        }
    }

    @Override
    public void onPause() {
        // TODO: only fire if changes have occurred, not if data hasn't loaded yet
        new AsyncTask<Context, Void, Void>() {

            @Override
            protected Void doInBackground(Context... params) {
                updateOrder(params[0], null);
                return null;
            }

        }.execute(getActivity());
        super.onPause();
    }

    private void updateOrder(Context context, String message) {
        if (null == rows) return;
        if(!rows.isEmpty() && rows.get(0).getType() == Level.ITEM){
            ArrayList<String> itemIds = new ArrayList<>(rows.size());
            for (int in = 0; in < rows.size(); in++) {
                itemIds.add(rows.get(in).getId());
            }
            WebServiceCall call = new CreateEditMenuGroupWebServiceCall(theGroup.getId(), theGroup.getName(), categoryId, menuId, itemIds, theGroup.getOrderIndex());
            WebServiceTask task = new WebServiceTask(context, call);
            if (null != message) task.setIndicatorText(message);
            task.execute();
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            EpicuriMenu.MenuLevel level = rows.get(i);
            if (level.getOrderIndex() != i) {
                Log.d("SetOrdering", "Set order index for " + level.getName() + " to " + i + " insetead of " + level.getOrderIndex());
                switch (level.getType()) {
                    case CATEGORY: {
                        EpicuriMenu.Category cat = (EpicuriMenu.Category) level;
                        WebServiceCall call = new CreateEditMenuCategoryWebServiceCall(cat.getId(), cat.getName(), menuId, cat.getGroups(), cat.getDefaultCourseIds(), i);
                        WebServiceTask task = new WebServiceTask(context, call);
                        if (null != message) task.setIndicatorText(message);
                        task.execute();
                        break;
                    }
                    case GROUP: {
                        EpicuriMenu.Group grp = (EpicuriMenu.Group) level;
                        WebServiceCall call = new CreateEditMenuGroupWebServiceCall(grp.getId(), grp.getName(), categoryId, menuId, grp.getItemIds(), i);
                        WebServiceTask task = new WebServiceTask(context, call);
                        if (null != message) task.setIndicatorText(message);
                        task.execute();
                        break;
                    }

                    case MENU: {

                    }
                }
            } else {
                Log.d("SetOrdering", "leave order index for " + level.getName() + " as " + i);
            }
        }
    }

    private void sortItemsAlphabetically() {
		if(null == rows || rows.isEmpty() || rows.get(0).getType() != Level.ITEM || theGroup == null){
			return;
		}

		FragmentActivity activity = getActivity();
		if(activity == null) {
			return;
		}

		new AlertDialog.Builder(activity)
				.setTitle("Sort alphabetically?")
				.setMessage("This will sort all items alphabetically within this group. Proceed?")
				.setNegativeButton("No", null)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Collections.sort(rows, new Comparator<MenuLevel>() {
							@Override
							public int compare(MenuLevel o1, MenuLevel o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
						theGroup.getItems().clear();
						for(int i = 0; i < rows.size(); i++) {
							theGroup.getItems().add((EpicuriMenu.Item) rows.get(i));
						}
						adapter.notifyDataSetChanged();
						WebServiceCall call = new CreateEditMenuGroupWebServiceCall(theGroup.getId(), theGroup.getName(), theCategory.getId(), theMenu.getId(), theGroup.getItemIds(), theGroup.getOrderIndex());
						WebServiceTask task = new WebServiceTask(MenuLevelFragment.this.getContext(), call);
						task.setIndicatorText("Saving");
						task.execute();
					}
				}).show();
	}

    private ArrayList<EpicuriMenu.MenuLevel> rows;

    private void onLoaded(EpicuriMenu menu) {
        theMenu = menu;
        rows = new ArrayList<>();
        if (level == Level.MENU) {

            rows.addAll(theMenu.getCategories());
        } else {
            theCategory = theMenu.getCategory(categoryId);

            if (null == theCategory) {
                return;
            }
            if (level == Level.CATEGORY) {
                rows.addAll(theCategory.getGroups());
            } else {
                theGroup = theCategory.getGroup(groupId);
                if (null == theGroup) {
                    return;
                }
                rows.addAll(theGroup.getItems());
            }
        }
        adapter = new MenuLevelAdapter(getActivity(), R.layout.listitem_row_draggable, rows, editRowActionMode);
        listView.setAdapter(adapter);
        if (null != editRowActionMode) {
            editRowActionMode.finish();
        }
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
            if (null == reorderActionMode && null == editRowActionMode) {
                reorderActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new DragActionMode());
                // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
                if (null != reorderActionMode) reorderActionMode.invalidate();
                listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            }
        }
    };

    public DragSortController buildController(DragSortListView dslv) {
        // defaults are
        //   dragStartMode = onDown
        //   removeMode = flingRight
        DragSortController controller = new DragSortController(dslv);
        controller.setDragHandleId(R.id.drag_handle);
        return controller;
    }

    private EpicuriMenu.MenuLevel selectedLevel;

    private class DragActionMode implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add("Save order");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            updateOrder(getActivity(), "Saving order...");
            reorderActionMode.finish();
            return true;
        }

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			reorderActionMode = null;
			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}
	
	private class MenuLevelActionMode implements ActionMode.Callback{

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if(null != reorderActionMode) return false;
			getActivity().getMenuInflater().inflate(R.menu.action_editmenu, menu);
			adapter.notifyDataSetChanged();
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			menu.findItem(R.id.menu_chooseTakeaway).setVisible(false);
			menu.findItem(R.id.menu_remove).setVisible(level == Level.GROUP);
			menu.findItem(R.id.menu_delete).setVisible(level != Level.GROUP);
			
			MenuItem editSubLevel = menu.findItem(R.id.menu_editmenu);
			switch(level){
			case MENU:
				editSubLevel.setTitle("Manage Groups");
				break;
			case CATEGORY:
				editSubLevel.setTitle("Manage Items");
				break;
			default:
				editSubLevel.setVisible(false);
				break;
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){
			case R.id.menu_edit: {
			    if (selectedLevel == null) return false;
				switch(selectedLevel.getType()){
				case CATEGORY: {
					EpicuriMenu.Category chosenCategory = ((EpicuriMenu.Category)selectedLevel);
					ArrayList<String> otherNames = new ArrayList<String>(theMenu.getCategories().size());
					for(Category c: theMenu.getCategories()){
						if(!c.equals(chosenCategory)) otherNames.add(c.getName());
					}
					EditMenuCategoryDialogFragment frag = EditMenuCategoryDialogFragment.newInstance(chosenCategory, menuId, otherNames);
					frag.show(getFragmentManager(), null);
					mode.finish();
					return true;
				}
				case GROUP: {
					ArrayList<String> otherNames = new ArrayList<String>(theCategory.getGroups().size());
					for(Group g: theCategory.getGroups()){
						otherNames.add(g.getName());
					}
					EpicuriMenu.Group chosenGroup = ((EpicuriMenu.Group)selectedLevel);
					EditMenuGroupFragment frag = EditMenuGroupFragment.newInstance(chosenGroup, categoryId, menuId, otherNames);
					frag.show(getFragmentManager(), null);
					mode.finish();
					return true;
				}
				case ITEM: {
					EpicuriMenu.Item menuitem = (EpicuriMenu.Item)selectedLevel;
					Intent intent = new Intent(getActivity(), EditMenuItemActivity.class);
					intent.putExtra(GlobalSettings.EXTRA_MENUITEM,(Parcelable)  menuitem);
					startActivity(intent);
					mode.finish();
					return true;
				}
				}
				return false;
			}
			case R.id.menu_editmenu: {
				showLevel(selectedLevel);
				editRowActionMode.finish();
				return true;
			}
			case R.id.menu_remove: {
				if(selectedLevel.getType() != Level.ITEM){
					throw new IllegalStateException();
				}
				final EpicuriMenu.Item chosenItem = ((EpicuriMenu.Item)selectedLevel);
				final String chosenItemId = chosenItem.getId();
				new AlertDialog.Builder(getActivity()).setTitle("Remove " + chosenItem.getName())
					.setNegativeButton("Do nothing", null)
					.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ArrayList<String> newListOfItems = new ArrayList<String>(theGroup.getItemIds().size() - 1);
							for(String i: theGroup.getItemIds()){
								if(!i.equals(chosenItemId)){
									newListOfItems.add(i);
								}
							}
							((OnMenuItemsSelectedListener)getActivity()).selectMenuItems(theGroup, newListOfItems, menuId);
						}
					})
					.show();
				mode.finish();
				return true;

			}
			case R.id.menu_delete: {
				DialogInterface.OnClickListener deleteListener = null;
				CharSequence title = null;

				switch(selectedLevel.getType()){
				case CATEGORY: {
					final EpicuriMenu.Category chosenCategory = ((EpicuriMenu.Category)selectedLevel);
					title = "Delete Category " + chosenCategory.getName() + "?";
					deleteListener = new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							((SaveCategoryListener)getActivity()).deleteCategory(chosenCategory.getId(), menuId);
						}
					};
					break;
				}
				case GROUP: {
					final EpicuriMenu.Group chosenGroup = ((EpicuriMenu.Group)selectedLevel);
					title = "Delete Group " + chosenGroup.getName() + "?";
					deleteListener = new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							((SaveGroupListener)getActivity()).deleteGroup(chosenGroup.getId(), menuId);
						}
					};
					break;
				}
				default:
				}
				
				if(null != title){
					new AlertDialog.Builder(getActivity()).setTitle(title)
						.setNegativeButton("Do nothing", null)
						.setPositiveButton("Delete", deleteListener)
						.show();
					mode.finish();
					return true;
				}
			}
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			selectedLevel = null;

			int currentItem = listView.getCheckedItemPosition();
			if(AdapterView.INVALID_POSITION != currentItem){
				// deselect current item
				listView.setItemChecked(currentItem, false);
			}
			editRowActionMode = null;
		}
		
	}
}
