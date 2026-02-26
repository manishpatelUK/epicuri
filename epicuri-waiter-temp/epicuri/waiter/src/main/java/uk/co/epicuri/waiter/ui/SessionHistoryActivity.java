package uk.co.epicuri.waiter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.SessionAdapter;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.ClosedSessionsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class SessionHistoryActivity extends EpicuriBaseActivity implements LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>, AdapterView.OnItemClickListener {

    private static final int LOADER_SESSIONS = 1;

    @InjectView(android.R.id.list)
    ListView listview;
    private SessionAdapter sessionAdapter;
    @InjectView(android.R.id.empty)
    LoaderEmptyView ev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_history);
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionAdapter = new SessionAdapter(this);
        listview.setAdapter(sessionAdapter);
        listview.setOnItemClickListener(this);
        ev.setText("No sessions found");
        listview.setEmptyView(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(LOADER_SESSIONS, null, this);
    }

    @Override
    public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int id, Bundle args) {
        return new EpicuriLoader<>(this, new ClosedSessionsLoaderTemplate());
    }

    @Override
    public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader, LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
        if(null == data || data.getPayload() == null){
            return;
        }
        ((LoaderEmptyView) listview.getEmptyView()).setDataLoaded();
        ArrayList<EpicuriSessionDetail> sessions = data.getPayload();
        Collections.sort(sessions, new Comparator<EpicuriSessionDetail>() {
            @Override
            public int compare(EpicuriSessionDetail lhs, EpicuriSessionDetail rhs) {
                return rhs.getClosedTime().compareTo(lhs.getClosedTime());
            }
        });
        sessionAdapter.setState(data.getPayload());
    }

    @Override
    public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EpicuriSessionDetail session = sessionAdapter.getItem(position);

        if(session.getType() == EpicuriSessionDetail.SessionType.DINE) {
            Intent sessionIntent = new Intent(this, SeatedSessionActivity.class);
            sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, session.getId());
            sessionIntent.putExtra("history", true);
            startActivity(sessionIntent);
        } else {
            Intent sessionIntent = new Intent(this, TakeawayActivity.class);
            sessionIntent.putExtra(GlobalSettings.EXTRA_SESSION_ID, session.getId());
            startActivity(sessionIntent);
        }
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
