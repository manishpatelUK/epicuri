package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.DeferredSessionAdapter;
import uk.co.epicuri.waiter.model.DeferredSession;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.webservice.GetDeferredWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class DeferredSessionsActivity extends EpicuriBaseActivity {

    private TextView sessionListEmpty;
    private final List<DeferredSession> deferredSessionList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DeferredSessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_deferred_sessions);

        sessionListEmpty = findViewById(R.id.sessionListEmpty);
        recyclerView = findViewById(R.id.deferredSessions);

        adapter = new DeferredSessionAdapter(this, LocalSettings.getCurrencyUnit(), deferredSessionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);

        triggerGetSessions();
    }

    @Override
    protected void onPause() {
        deferredSessionList.clear();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        triggerGetSessions();
    }

    private void triggerGetSessions() {
        showPleaseWaitDialog("Updating Account Details...");
        WebServiceTask task = new WebServiceTask(this, new GetDeferredWebServiceCall(), false);
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    deferredSessionList.clear();
                    adapter.notifyDataSetChanged();
                    JSONArray array = new JSONArray(response);
                    for(int i = 0; i < array.length(); i++) {
                        DeferredSession session = new DeferredSession(array.getJSONObject(i));
                        deferredSessionList.add(session);
                    }

                    if(deferredSessionList.size() == 0) {
                        sessionListEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        sessionListEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    dismissPleaseWaitDialog();
                }
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                dismissPleaseWaitDialog();
                Toast.makeText(DeferredSessionsActivity.this, "Cannot get deferred tabs at this time", Toast.LENGTH_SHORT).show();
            }
        });
        task.execute();
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
    }
}
