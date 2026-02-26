package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.epicuri.waiter.interfaces.OnTableChangeListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.templates.LayoutLoaderTemplate;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriFloor.Layout;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.utils.GlobalSettings;


public class FloorplanFragment extends Fragment implements LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>{

	private static final int LOADER_SESSION = 101;
	private static final int LOADER_FLOORPLAN = 102;
	public static final String EXTRA_HIGHLIGHTED_TABLES = "highlightedTables";
	public static final String EXTRA_SELECT_TABLES = "selectTables";

	private List<EpicuriSessionDetail> state;
	private EpicuriFloor floor;
	private FloorplanView floorplanView;
	
	private boolean tableStateShown = true;
	
	public FloorplanView getFloorplanView() {
		return floorplanView;
	}
	
	public static FloorplanFragment newInstance(EpicuriFloor floor, boolean selectTables, String[] highlightedTables){
		Bundle b = new Bundle(3);
		b.putParcelable(GlobalSettings.EXTRA_FLOOR, floor);
		b.putBoolean(EXTRA_SELECT_TABLES, selectTables);
		b.putStringArray(EXTRA_HIGHLIGHTED_TABLES, highlightedTables);
		FloorplanFragment f = new FloorplanFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(null == savedInstanceState){
			floor = getArguments().getParcelable(GlobalSettings.EXTRA_FLOOR);
			persistTableSelectMode = getArguments().getBoolean(EXTRA_SELECT_TABLES);
			highlightedTables = getArguments().getStringArray(EXTRA_HIGHLIGHTED_TABLES);
		} else {
			floor = savedInstanceState.getParcelable(GlobalSettings.EXTRA_FLOOR);
			persistTableSelectMode = savedInstanceState.getBoolean(EXTRA_SELECT_TABLES);
			highlightedTables = savedInstanceState.getStringArray(EXTRA_HIGHLIGHTED_TABLES);
		}
		requestedLayoutId = floor.getLayoutId();
	}

	// fix for bug in library
	// http://stackoverflow.com/questions/8748064/starting-activity-from-fragment-causes-nullpointerexception
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(GlobalSettings.EXTRA_FLOOR, floor);
		outState.putBoolean(EXTRA_SELECT_TABLES, persistTableSelectMode);
		outState.putStringArray(EXTRA_HIGHLIGHTED_TABLES, highlightedTables);
		setUserVisibleHint(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().restartLoader(LOADER_FLOORPLAN, null, loaderCallbacks);
	}

	String activeLayoutId = "-1";
	String requestedLayoutId = "-1";

	public void setFloor(final EpicuriFloor floor){
		if(isAdded() && null != floorplanView) {
			floorplanView.setBackgroundFilename(floor.getFloorBackgroundImage());
		}
		setLayout(floor.getLayoutId());
		this.floor = floor;
	}


	public void setLayout(final String layoutId){
		// prevent reload if ID is unchanged
		if(activeLayoutId.equals(layoutId)) return;
		
		requestedLayoutId = layoutId;
		if(isAdded()){
			getLoaderManager().restartLoader(LOADER_FLOORPLAN, null, loaderCallbacks);
		}
	}
	public String getLayout(){
		return activeLayoutId;
	}

	private Layout currentLayout;
	private String[] highlightedTables = null;
	public boolean highlightTables(String[] tablesToHighlight){
		highlightedTables = tablesToHighlight;
		if(null != floorplanView){
			return floorplanView.highlightTables(tablesToHighlight);
		}
		if(null == currentLayout || null == tablesToHighlight) return false;
		for(int i=0; i<tablesToHighlight.length; i++){
			for(EpicuriTable t: currentLayout.getTables()){
				if(t.getId().equals(tablesToHighlight[i])){
					return true;
				}
			}
		}
		return false;
	}


	public void setTableStateShown(boolean tableStateShown) {
		if(this.tableStateShown == tableStateShown) return;
		
		this.tableStateShown = tableStateShown;
		if(!tableStateShown){
			AsyncTaskLoader<?> sessionLoader = (AsyncTaskLoader<?>)getLoaderManager().getLoader(LOADER_SESSION);
			sessionLoader.cancelLoad();
			sessionLoader.abandon();
			floorplanView.setState(null);
		} else {
			getLoaderManager().initLoader(LOADER_SESSION, null, this);
		}
	}

	private LoaderManager.LoaderCallbacks<LoaderWrapper<Layout>> loaderCallbacks = new LoaderManager.LoaderCallbacks<LoaderWrapper<Layout>>(){
		@Override
		public Loader<LoaderWrapper<Layout>> onCreateLoader(int arg0, Bundle arg1) {
			if(null != floorplanView) floorplanView.setLayout(null);
			EpicuriLoader<EpicuriFloor.Layout> loader = new EpicuriLoader<EpicuriFloor.Layout>(getActivity(), new LayoutLoaderTemplate(requestedLayoutId));
			loader.setAutoRefreshPeriod(60000);
			return loader;
		}
		
		@Override
		public void onLoadFinished(Loader<LoaderWrapper<Layout>> arg0, LoaderWrapper<Layout> data) {
			if(null == data) return;
			if(null != floorplanView){
				activeLayoutId = data.getPayload().getId();
				currentLayout = data.getPayload();
				floorplanView.setLayout(currentLayout.getTables());
			}
		}
		
		@Override
		public void onLoaderReset(Loader<LoaderWrapper<Layout>> arg0) {
		}
	};
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if(tableStateShown) {
			getLoaderManager().initLoader(LOADER_SESSION, null, this);
		}
		setLayout(requestedLayoutId);
		
		floorplanView = new FloorplanView(getActivity());
		
		if(getActivity() instanceof OnTableChangeListener){
			floorplanView.setOnTableChangeListener((OnTableChangeListener)getActivity());
		}
		
		if(null != state){
			floorplanView.setState(state);
		}
		floorplanView.setBackgroundFilename(floor.getFloorBackgroundImage());
		floorplanView.setTableSelectionMode(persistTableSelectMode);
		floorplanView.highlightTables(highlightedTables); // XXX this is the bugger
		return floorplanView;
	}
	
	@Override
	public void onDestroyView() {
		// persist selected tables
		Map<String,Boolean> tables = floorplanView.getHighlightedTables();
		if(null == tables){
			highlightedTables = null;
		} else {
			int size = 0;
			for(Map.Entry<String,Boolean> entry : tables.entrySet()){
				if(entry.getValue()){
					size++;
				}
			}
			highlightedTables = new String[size];
			for(Map.Entry<String,Boolean> entry : tables.entrySet()){
                if(entry.getValue()){
                    highlightedTables[--size] = entry.getKey();
                    Log.d("FloorplanFragment","table " + entry.getKey() + " is selected (" + size + " to go)");
                }
			}
		}

		floorplanView = null;
		super.onDestroyView();
	}

	@Override
	public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int id, Bundle args) {
		return new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(getActivity(), new SessionsLoaderTemplate());
	}

	@Override
	public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
			LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
		if(null == data){ // nothing returned, ignore
			return;
		}else if(data.isError()){
			Toast.makeText(getActivity(), "FloorplanFragment error loading data", Toast.LENGTH_SHORT).show();
			return;
		}
		state = data.getPayload();
		if(null != floorplanView) floorplanView.setState(data.getPayload());
	}

	@Override
	public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader) {
		if(null != floorplanView){
			floorplanView.setState(null);
		}
	}


	private boolean persistTableSelectMode = false;
	public void setTableSelectionMode(boolean tableSelectMode){
		if(null != floorplanView) floorplanView.setTableSelectionMode(tableSelectMode);
		persistTableSelectMode = tableSelectMode;
		highlightedTables = tableSelectMode ? new String[0] : null;
	}

	public void setTableSelectionMode(boolean tableSelectMode, EpicuriSessionDetail sessionToReseat) {
		if(null != floorplanView) floorplanView.setTableSelectionMode(tableSelectMode, sessionToReseat);
		persistTableSelectMode = tableSelectMode;
		highlightedTables = tableSelectMode ? new String[0] : null;
	}


	@Override
	public void onResume() {
		getLoaderManager().restartLoader(LOADER_FLOORPLAN, null, loaderCallbacks);
		super.onResume();
	}
	
	public Map<String,Boolean> getSelectedTables() {
		if(!isVisible()){
			return new HashMap<>();
		}
		return floorplanView.getHighlightedTables();
	}
	
	
}