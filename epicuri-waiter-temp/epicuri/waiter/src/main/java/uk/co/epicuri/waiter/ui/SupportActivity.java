package uk.co.epicuri.waiter.ui;

import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.service.ConnectivityService;
import uk.co.epicuri.waiter.service.interfaces.IConnectionTaskListener;

public class SupportActivity extends EpicuriBaseActivity implements TabLayout.OnTabSelectedListener{

    @InjectView(R.id.supportTabLayout)
    TabLayout supportTabLayout;

    @InjectView(R.id.supportViewPager)
    ViewPager supportViewPager;

    private SupportActivityPager adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.support_activity);
        ButterKnife.inject(this);

        supportTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        adapter = new SupportActivityPager(getSupportFragmentManager());
        supportViewPager.setAdapter(adapter);
        supportTabLayout.setupWithViewPager(supportViewPager);
        supportTabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        supportViewPager.setCurrentItem(tab.getPosition());
        adapter.initItems(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        adapter.initItems(tab.getPosition());
    }
}
