package uk.co.epicuri.waiter.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.ui.FloorplanFragment;

public class FloorPlanFragmentAdapter extends FragmentPagerAdapter {
	private List<EpicuriFloor> floors;
	private FragmentManager fm;
	private String[] highlightedTables = null;

	public FloorPlanFragmentAdapter(FragmentManager fm, List<EpicuriFloor> floors) {
		super(fm);
		this.fm = fm;
		setFloors(floors, 0);
	}

	public List<Integer> highlightTables(String[] tables, int viewId){
		highlightedTables = tables;
		LinkedList<Integer> activeFloors = new LinkedList<Integer>();
		if(floors != null) {
			for (int i = 0; i < floors.size(); i++) {
				FloorplanFragment f = (FloorplanFragment) getItem(i, viewId);
				if (null != f && f.highlightTables(tables)) {
					activeFloors.add(i);
				}
			}
		}
		return activeFloors;
	}
	
	public void setFloors(List<EpicuriFloor> floors, int viewId){
		// same number of floors, assume that the layouts may have changed
		if(null != floors && null != this.floors && floors.size() == this.floors.size()){
			this.floors = floors;
			for(int i=0; i<floors.size(); i++){
				FloorplanFragment floor = (FloorplanFragment)getItem(i, viewId);
				if(null != floor)
					floor.setFloor(floors.get(i));
			}
			return;
		}
		this.floors = floors;
		notifyDataSetChanged();
	}

	private boolean selectTables = false;
	public void setTableSelectionMode(boolean selectTables, int viewId){
		this.selectTables = selectTables;
		if(selectTables) highlightedTables = new String[0];
		for(int i=0; i<floors.size(); i++){
			String fragmentName = "android:switcher:" + viewId + ":" + i;
			FloorplanFragment f = (FloorplanFragment)fm.findFragmentByTag(fragmentName);
			if(null != f)
				f.setTableSelectionMode(selectTables);
		}
	}

	private EpicuriSessionDetail sessionToReseat;
	public void setTableSelectionMode(int viewId, EpicuriSessionDetail sessionToReseat) {
		this.selectTables = true;
		this.sessionToReseat = sessionToReseat;

		highlightedTables = new String[sessionToReseat.getTables().length];
		EpicuriTable[] tables = sessionToReseat.getTables();
		for(int i=0; i<tables.length; i++) {
			highlightedTables[i] = tables[i].getId();
		}

		for(int i=0; i<floors.size(); i++) {
			String fragmentName = "android:switcher:" + viewId + ":" + i;
			FloorplanFragment f = (FloorplanFragment)fm.findFragmentByTag(fragmentName);
			if(null != f) f.setTableSelectionMode(selectTables, sessionToReseat);
		}
	}

	@Override
	public int getCount() {
		if(null == floors) return 0;
		return floors.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		EpicuriFloor f = floors.get(position);
		return String.format(Locale.UK, "%s (%d)", f.getName(), f.getCapacity());
	}

	public Fragment getItem(int position, int viewId){
		// XXX: undocumented feature to retrieve fragment by tag
		String fragmentName = "android:switcher:" + viewId + ":" + position;
		Fragment f = fm.findFragmentByTag(fragmentName);
		return f;
	}

	@Override
	public Fragment getItem(int position) {
		return FloorplanFragment.newInstance(floors.get(position), selectTables, highlightedTables);
	}

}
