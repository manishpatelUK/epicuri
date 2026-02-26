package uk.co.epicuri.waiter.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {
    private LinkedHashMap<String, Fragment> pages = new LinkedHashMap<>();

    TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override public Fragment getItem(int position) {
        return pages.get(getPageTitle(position));
    }

    @Override public int getCount() {
        return pages.size();
    }

    public void addPage(String tabName, Fragment fragment){
        pages.put(tabName, fragment);
    }

    public int getPositionOfFragmentByTag(String tag){
        return new ArrayList<>(pages.keySet()).indexOf(tag);
    }

    public Fragment findFragmentByTag(String tag){
        return pages.get(tag);
    }

    @Nullable @Override public CharSequence getPageTitle(int position) {
        return pages.keySet().toArray()[position].toString();
    }

    public void resetTabs(){
        pages.clear();
    }
}
