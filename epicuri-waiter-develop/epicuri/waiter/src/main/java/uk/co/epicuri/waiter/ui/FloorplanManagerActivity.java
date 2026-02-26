package uk.co.epicuri.waiter.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import uk.co.epicuri.waiter.ui.CustomViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.FloorPlanFragmentAdapter;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.FloorLayoutLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.FloorLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.LayoutLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriFloor.Layout;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.webservice.CreateEditLayoutWebServiceCall;
import uk.co.epicuri.waiter.webservice.SelectLayoutWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class FloorplanManagerActivity extends EpicuriBaseActivity {

	private static final int LOADER_FLOORS = 1;
	private static final int LOADER_LAYOUTS_FOR_FLOOR = 2;
	private static final int LOADER_CURRENT_LAYOUT = 3;
	private static final int LOADER_SESSIONS = 4;

	private ListView layoutListView;
	private TextView emptyOrLoadingTextView;
	private CustomViewPager floorAnimator;

	private ArrayAdapter<EpicuriFloor.Layout> layoutAdapter;
	private FloorPlanFragmentAdapter floorplanAdapter;

	private List<EpicuriFloor> floors;
	private List<EpicuriFloor.Layout> layouts;
	private EpicuriFloor selectedFloor = null;

	/**
	 * the active layout for the currently selected floor - used to check for active sessions
	 */
	private EpicuriFloor.Layout currentFloorActiveLayout = null;
	/**
	 * list of active sessions to check for empty floors
	 */
	private ArrayList<EpicuriSessionDetail> currentSessions = null;
	/**
	 * whether the currently selected layout is empty
	 */
	private boolean currentFloorIsEmpty = false;

	private String jumpToFloor = "-1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_floorplanmanager);

		layoutListView = (ListView)findViewById(R.id.layouts);
		emptyOrLoadingTextView = (TextView)findViewById(R.id.layoutListEmpty);
		layoutListView.setEmptyView(emptyOrLoadingTextView);

		layoutListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		layoutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
				chooseLayout(layouts.get(position));
			}

		});

		floorAnimator = (CustomViewPager)findViewById(R.id.floorPager);

		if(null == savedInstanceState){
			jumpToFloor = getIntent().getStringExtra(GlobalSettings.EXTRA_FLOOR);//(GlobalSettings.EXTRA_FLOOR, "-1")
		}

		floorAnimator.addOnPageChangeListener(new CustomViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				EpicuriFloor floor = getCurrentFloor();
				selectFloor(floor);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
			                           int positionOffsetPixels) {
				// do nothing
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// do nothing
			}
		});

		getSupportLoaderManager().initLoader(LOADER_FLOORS, null, floorsCallback);

	}

	private void selectFloor(EpicuriFloor floor) {
		if(selectedFloor == floor) return;
		selectedFloor = floor;

		layoutListView.setAdapter(null);
		emptyOrLoadingTextView.setText("Loading");

		Bundle args = new Bundle();
		args.putString("floorId", floor.getId());
		getSupportLoaderManager().restartLoader(LOADER_LAYOUTS_FOR_FLOOR, args, floorCallback);
	}

	private final LoaderManager.LoaderCallbacks<List<EpicuriFloor>> floorsCallback = new LoaderManager.LoaderCallbacks<List<EpicuriFloor>>(){
		@Override
		public Loader<List<EpicuriFloor>> onCreateLoader(int id, Bundle args) {
			return new OneOffLoader<List<EpicuriFloor>>(FloorplanManagerActivity.this, new FloorLoaderTemplate());
		}

		@Override
		public void onLoadFinished(Loader<List<EpicuriFloor>> loader, List<EpicuriFloor> result) {
			// if floors have already loaded, don't refresh
//            if(null != floors) return;

			if(null == result) return;

			floors = result;

			// identify the previously selected floor
			int previousfloor;
			if (floorAnimator.getChildCount() == 0) {
				previousfloor = 0;
				for (int pos = 0; pos < floors.size(); pos++) {
					if (floors.get(pos).getId().equals(jumpToFloor)) {
						previousfloor = pos;
						break;
					}
				}
			} else {
				previousfloor = floorAnimator.getCurrentItem();
			}

			if(null == floorplanAdapter) {
				floorplanAdapter = new FloorPlanFragmentAdapter(getSupportFragmentManager(), floors);
				floorAnimator.setAdapter(floorplanAdapter);
			} else {
				floorplanAdapter.setFloors(floors, floorAnimator.getId());
			}

			floorAnimator.setCurrentItem(previousfloor);
			floorAnimator.setCurrentItem(floorAnimator.getCurrentItem());
			// explicitly call the listener
			selectFloor(floors.get(floorAnimator.getCurrentItem()));
		}

		@Override
		public void onLoaderReset(Loader<List<EpicuriFloor>> arg0) {

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_floorplanmanager, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean superResponse = super.onPrepareOptionsMenu(menu);
		if(null == layouts || layoutListView.getCheckedItemPosition() < 0){
			return superResponse;
		}
		// TODO 04-29 15:38:34.904: E/AndroidRuntime(19445): java.lang.IndexOutOfBoundsException: Invalid index 3, size is 3
		// when deleting the last item in the list
		EpicuriFloor.Layout currentLayout = layouts.get(layoutListView.getCheckedItemPosition());

		boolean isTemp = currentLayout.isTemporary();

		menu.findItem(R.id.menu_delete).setVisible(!isTemp);
		menu.findItem(R.id.menu_applyToFloor).setVisible(!isTemp && currentFloorIsEmpty);

		return true;
	}

	private ArrayList<String> getLayoutNames(){
		if(null == layouts) return new ArrayList<String>(0);
		
		ArrayList<String> names = new ArrayList<String>(layouts.size());
		for(Layout l: layouts){
			names.add(l.getName());
		}
		return names;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.menu_new: {
				Intent newFloorplanIntent = new Intent(this, FloorplanEditActivity.class);
				newFloorplanIntent.putExtra(GlobalSettings.EXTRA_FLOOR, getCurrentFloor());
				newFloorplanIntent.putExtra(GlobalSettings.EXTRA_LAYOUT_NAMES, getLayoutNames());
				startActivity(newFloorplanIntent);
				break;
			}
			case R.id.menu_edit: {
				int selectedPosition = layoutListView.getCheckedItemPosition();
				if(AbsListView.INVALID_POSITION == selectedPosition){
					return true;
				}
				final EpicuriFloor.Layout currentLayout = layouts.get(selectedPosition);


				if(!currentLayout.isTemporary() && currentLayout.getId().equals(selectedFloor.getLayoutId())){
					new AlertDialog.Builder(this).setTitle("Editing Currently Active Layout")
							.setMessage("This layout is currently applied to the floor. Editing it will also edit the currently active floorplan")
							.setPositiveButton("Edit", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent editFloorplanIntent = new Intent(FloorplanManagerActivity.this, FloorplanEditActivity.class);
									editFloorplanIntent.putExtra(GlobalSettings.EXTRA_LAYOUT_ID, currentLayout.getId());
									editFloorplanIntent.putExtra(GlobalSettings.EXTRA_CURRENT, currentLayout.isCurrent());
									editFloorplanIntent.putExtra(GlobalSettings.EXTRA_FLOOR, getCurrentFloor());
									editFloorplanIntent.putExtra(GlobalSettings.EXTRA_LAYOUT_NAMES, getLayoutNames());
									startActivity(editFloorplanIntent);
								}
							})
							.show();
					return true;
				}

				Intent editFloorplanIntent = new Intent(FloorplanManagerActivity.this, FloorplanEditActivity.class);
				editFloorplanIntent.putExtra(GlobalSettings.EXTRA_LAYOUT_ID, currentLayout.getId());
				editFloorplanIntent.putExtra(GlobalSettings.EXTRA_CURRENT, currentLayout.isCurrent());
				editFloorplanIntent.putExtra(GlobalSettings.EXTRA_FLOOR, getCurrentFloor());
				editFloorplanIntent.putExtra(GlobalSettings.EXTRA_LAYOUT_NAMES, getLayoutNames());
				startActivity(editFloorplanIntent);
				return true;
			}
			case R.id.menu_delete: {
				final EpicuriFloor.Layout currentLayout = layouts.get(layoutListView.getCheckedItemPosition());
				new AlertDialog.Builder(FloorplanManagerActivity.this)
						.setTitle(currentLayout.getName())
						.setMessage("Are you sure you want to delete this layout?")
						.setPositiveButton("Delete Layout", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								layoutListView.clearChoices();
								WebServiceTask task = new WebServiceTask(FloorplanManagerActivity.this,
										new CreateEditLayoutWebServiceCall(currentLayout, currentLayout.getFloorId(), true), true);
								task.setIndicatorText("Deleting layout");
								task.execute();
							}
						})
						.setNegativeButton("Keep Layout", null)
						.show();
				return true;
			}
			case R.id.menu_applyToFloor: {
				if(layouts != null) {
                    final EpicuriFloor.Layout currentLayout = layouts.get(layoutListView.getCheckedItemPosition());

                    WebServiceTask task = new WebServiceTask(FloorplanManagerActivity.this, new SelectLayoutWebServiceCall(getCurrentFloor().getId(), currentLayout.getId()), true);
                    task.execute();
                    return true;
                }
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private EpicuriFloor getCurrentFloor(){
		int floorIndex = floorAnimator.getCurrentItem();
		return floors.get(floorIndex);
	}

	private final LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriFloor>> floorCallback = new LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriFloor>>() {
		@Override
		public Loader<LoaderWrapper<EpicuriFloor>> onCreateLoader(int arg0, Bundle args) {
			return new EpicuriLoader<EpicuriFloor>(FloorplanManagerActivity.this, new FloorLayoutLoaderTemplate(args.getString("floorId")));
		}
		@Override
		public void onLoadFinished(Loader<LoaderWrapper<EpicuriFloor>> arg0,
		                           LoaderWrapper<EpicuriFloor> result) {
			if(result == null || null == result.getPayload()) return;

			emptyOrLoadingTextView.setText("No Layouts Found");

			EpicuriFloor floor = result.getPayload();
			selectedFloor = floor; // slightly risky
			layouts = new ArrayList<EpicuriFloor.Layout>();

			EpicuriFloor.Layout dummy = null;
			for(EpicuriFloor.Layout l : floor.getLayouts()){
				if(l.getId().equals(floor.getLayoutId())){
					dummy =  EpicuriFloor.newDummyLayout(floor.getLayoutId(), l.getName());
				}
			}
			if(null == dummy){
				dummy =  EpicuriFloor.newDummyLayout(floor.getLayoutId(), null);
			}

			// insert a dummy layout for "Current" floorplan
			layouts.add(dummy);
			layouts.addAll(floor.getLayouts());

			int selectedItem = layoutListView.getCheckedItemPosition();

			layoutAdapter =
					new ArrayAdapter<EpicuriFloor.Layout>(
							FloorplanManagerActivity.this,
							android.R.layout.simple_list_item_single_choice,
							android.R.id.text1,
							layouts);
			layoutListView.setAdapter(layoutAdapter);

			int floorIndex = floorAnimator.getCurrentItem();
			FloorplanFragment floorFragment = (FloorplanFragment)floorplanAdapter.getItem(floorIndex, floorAnimator.getId());
			
//			int activeLayout = floorFragment.getLayout();
//			if(0<activeLayout){
//				for(int i=0; i<layouts.size(); i++){
//					if(layouts.get(i).getId() == activeLayout){
//						selectedItem = i;
//						break;
//					}
//				}
//			}
			if(AdapterView.INVALID_POSITION == selectedItem){
				selectedItem = 0;
			}
			layoutListView.setItemChecked(selectedItem, true);
			chooseLayout(layouts.get(selectedItem));

			loadCurrentLayoutAndSessions(floor.getLayoutId());

			invalidateOptionsMenu();
		}
		@Override
		public void onLoaderReset(Loader<LoaderWrapper<EpicuriFloor>> arg0) {
			emptyOrLoadingTextView.setText("Loading cancelled");
		}
	};

	/**
	 * choose a layout from the list, tell the layout UI to update itself
	 * @param layout
	 */
	private void chooseLayout(EpicuriFloor.Layout layout){
		int floorIndex = floorAnimator.getCurrentItem();
		FloorplanFragment floorFragment = (FloorplanFragment)floorplanAdapter.getItem(floorIndex, floorAnimator.getId());
		floorFragment.setLayout(layout.getId());
		floorFragment.setTableStateShown(layout.isTemporary());
		invalidateOptionsMenu();
	}

	/**
	 * compare the list of tables on the active floorplan to the active sessions to determine
	 * whether the floor is empty
	 */
	private void checkCurrentFloor(){
		if(null == currentFloorActiveLayout || null == currentSessions){
			currentFloorIsEmpty = true;
			invalidateOptionsMenu();
			return;
		}
		Set<String> tablesOnFloor = new HashSet<>();
		for(EpicuriTable t: currentFloorActiveLayout.getTables()){
			tablesOnFloor.add(t.getId());
		}
		for(EpicuriSessionDetail session: currentSessions){
			if(null != session.getTables()){
				for(EpicuriTable table: session.getTables()){
					if(tablesOnFloor.contains(table.getId())){
						currentFloorIsEmpty = false;
						invalidateOptionsMenu();
						return;
					}
				}
			}
		}
		currentFloorIsEmpty = true;
		invalidateOptionsMenu();
	}

	/**
	 * fire off loaders for this layout to refresh the session list & active layout
	 * @param layoutId
	 */
	private void loadCurrentLayoutAndSessions(final String layoutId){
		currentFloorActiveLayout = null;
		getSupportLoaderManager().restartLoader(LOADER_CURRENT_LAYOUT, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<EpicuriFloor.Layout>>() {

			@Override
			public Loader<LoaderWrapper<Layout>> onCreateLoader(int arg0,
			                                                    Bundle arg1) {
				return new EpicuriLoader<Layout>(FloorplanManagerActivity.this, new LayoutLoaderTemplate(layoutId));
			}

			@Override
			public void onLoadFinished(Loader<LoaderWrapper<Layout>> arg0,
			                           LoaderWrapper<Layout> data) {
				if(null == data) return;
				else if(data.isError()){
					Toast.makeText(FloorplanManagerActivity.this, "FloorplanManagerActivity Error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				currentFloorActiveLayout = data.getPayload();
				checkCurrentFloor();
			}

			@Override
			public void onLoaderReset(Loader<LoaderWrapper<Layout>> arg0) {
				// TODO Auto-generated method stub

			}

		});

		getSupportLoaderManager().initLoader(LOADER_SESSIONS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(
					int arg0, Bundle arg1) {
				return new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(FloorplanManagerActivity.this, new SessionsLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> arg0,
					LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
				if(null == data) return;
				else if(data.isError()){
					Toast.makeText(FloorplanManagerActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				currentSessions = data.getPayload();
				checkCurrentFloor();
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> arg0) {
				// TODO Auto-generated method stub

			}

		});
	}

}
