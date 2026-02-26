package uk.co.epicuri.waiter.ui;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import uk.co.epicuri.waiter.ui.menueditor.AdvancedSupportFragment;

public class SupportActivityPager extends FragmentStatePagerAdapter {

    private NetworkSupportFragment networkSupportFragment;
    private PrinterSupportFragment printerSupportFragment;
    private AdvancedSupportFragment advancedSupportFragment;

    public SupportActivityPager(FragmentManager fm) {
        super(fm);

        networkSupportFragment = new NetworkSupportFragment();
        printerSupportFragment = new PrinterSupportFragment();
        advancedSupportFragment = new AdvancedSupportFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return networkSupportFragment;
            case 1:
                return printerSupportFragment;
            case 2:
                return advancedSupportFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public void initItems(int position) {
        switch (position) {
            case 0:
                initNetwork();
            case 1:
                initPrinter();
            case 2:
                initAdvanced();
        }
    }

    @Nullable @Override public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Networks";
            case 1:
                return "Printers";
            case 2:
                return "Advanced";
        }
        return "";
    }

    private void initPrinter() {
        printerSupportFragment.trigger();
    }

    private void initNetwork() {
        networkSupportFragment.trigger();
    }

    private void initAdvanced() {
        advancedSupportFragment.trigger();
    }
}
