package uk.co.epicuri.waiter.ui;

import static uk.co.epicuri.waiter.ui.PaymentDialogFragment.DEFAULT_TPI;
import static uk.co.epicuri.waiter.ui.PaymentDialogFragment.TPI_STRING;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.PaymentsenseTerminal;
import uk.co.epicuri.waiter.webservice.GetTerminalsWebServiceTask;
import uk.co.epicuri.waiter.webservice.PostPSReportRequestTask;

public class PaymentSenseActivity extends EpicuriBaseActivity implements
        PostPSReportRequestTask.IPostTransactionListener,
        GetTerminalsWebServiceTask.ITerminalsListener {

    private List<PaymentsenseTerminal> terminals;
    private SharedPreferences sharedPreferences;

    @InjectView(R.id.host_address) TextView hostAddress;
    @InjectView(R.id.api_key) TextView apiKey;
    @InjectView(R.id.terminals) Spinner terminalsList;
    @InjectView(R.id.loader) LoaderEmptyView loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_payment_sense);
        ButterKnife.inject(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Update to fresh available terminals
        (new GetTerminalsWebServiceTask(this, LocalSettings.getInstance(this).getCachedRestaurant(), this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override protected void onResume() {
        super.onResume();
        render();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override public void onPostTransactionSuccess() {
        dismissPleaseWaitDialog();
        Toast.makeText(this, R.string.report_success, Toast.LENGTH_SHORT).show();
    }

    @Override public void onPostTransactionFailure(String errorMessage) {
        dismissPleaseWaitDialog();
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override public void onTerminalsLoaded(String response, EpicuriRestaurant restaurant) {
        terminals = restaurant.getTerminals();

        String default_id = sharedPreferences.getString(DEFAULT_TPI, "");
        int selectId = -1;
        PaymentsenseTerminal terminal;

        List<String> terminalIds = new ArrayList<>(terminals.size());

        for (int i = 0; i < terminals.size(); ++i) {
            terminal = terminals.get(i);
            terminalIds.add(TPI_STRING + terminal.getTpi());

            if (terminal.getTpi().equals(default_id)) selectId = i;
        }

        if (terminalIds.isEmpty()) {
            terminalsList.setEnabled(false);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R
                    .layout.simple_spinner_item, terminalIds);
            terminalsList.setAdapter(adapter);

            if (selectId != -1) terminalsList.setSelection(selectId);
        }

        loader.setVisibility(View.GONE);
    }

    /*@OnClick(R.id.print_reports) public void onViewClicked() {
        if (terminalsList.getSelectedItemPosition() < 0 || terminalsList.getSelectedItemPosition
                () >= terminals.size()) return;

        String terminalLocation = terminals.get(terminalsList.getSelectedItemPosition())
                .getLocation();
        if (terminalLocation == null) {
            Toast.makeText(this, "Please, select PDQ.", Toast.LENGTH_SHORT).show();
            return;
        }

        showPleaseWaitDialog(getString(R.string.print_report));
        PostPSReportRequestTask task = new PostPSReportRequestTask(this, terminalLocation);
        task.setListener(this);
        task.execute();
    }*/

    void render() {
        EpicuriRestaurant restaurant = LocalSettings.getInstance(this).getCachedRestaurant();
        hostAddress.setText(restaurant.getPaymentsenseHost());
        apiKey.setText(restaurant.getPaymentsenseKey());
    }
}
