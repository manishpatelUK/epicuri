package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import uk.co.epicuri.waiter.ui.CustomViewPager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;

public class EndOfDayActivity extends EpicuriBaseActivity {

    @InjectView(R.id.viewPager)
    CustomViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_endofday);
		ButterKnife.inject(this);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch(position){
                    case 0:
                        return EndOfDayListFragment.newInstance(EndOfDayListFragment.Type.SESSION);
                    case 1:
                        return EndOfDayListFragment.newInstance(EndOfDayListFragment.Type.RESERVATION);
                    case 2:
                        return EndOfDayListFragment.newInstance(EndOfDayListFragment.Type.PENDING_TAKEAWAY);
                    case 3:
                        return EndOfDayListFragment.newInstance(EndOfDayListFragment.Type.PENDING_RESERVATION);
                }
                return null;
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.tabs_tables_takeaways);
                    case 1:
                        return getString(R.string.reservations);
                    case 2:
                        return getString(R.string.pending_takeaways);
                    case 3:
                        return getString(R.string.pending_reservations);
                }
                return null;
            }
        });
	}


}
