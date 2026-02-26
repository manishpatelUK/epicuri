package uk.co.epicuri.waiter.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.DeferredSession;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.ui.SeatedSessionActivity;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.ReopenDeferredSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.VoidDeferredSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class DeferredSessionAdapter extends RecyclerView.Adapter<DeferredSessionAdapter.ViewHolder>{
    private final CurrencyUnit currencyUnit;
    private final List<DeferredSession> deferredSessions;
    private final MoneyFormatter formatter;
    private final Activity parentActivity;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.UK);

    public DeferredSessionAdapter(Activity parentActivity, CurrencyUnit currencyUnit, List<DeferredSession> deferredSessions) {
        this.parentActivity = parentActivity;
        this.currencyUnit = currencyUnit;
        this.deferredSessions = deferredSessions;
        formatter = new MoneyFormatterBuilder().appendLiteral(currencyUnit.getSymbol()).appendAmount().toFormatter();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View customerView = parentActivity.getLayoutInflater().inflate(R.layout.row_deferred_sessions, parent, false);
        return new DeferredSessionAdapter.ViewHolder(customerView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final DeferredSession session = deferredSessions.get(i);
        if(session == null) {
            return;
        }
        EpicuriCustomer customer = session.getCustomer();
        if(customer != null) {
            viewHolder.nameView.setText(customer.getName());
            if(customer.getPhoneNumber() != null) {
                viewHolder.phoneNumber.setText(customer.getPhoneNumber());
            }
        }
        final String amount = formatter.print(Money.of(currencyUnit, session.getRemaining()));
        viewHolder.billAmount.setText(amount);
        viewHolder.settleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(viewHolder.itemView.getContext())
                        .setTitle("Settle & Close")
                        .setMessage("Settle this bill for " + amount + "?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Settle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                triggerOpenSession(session.getSessionId());
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        viewHolder.dateView.setText(DATE_FORMAT.format(new Date(session.getCreationTime())));
        viewHolder.voidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(viewHolder.itemView.getContext())
                        .setTitle("Void this Bill?")
                        .setMessage("Void this bill for " + amount + "?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Void", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                triggerVoidSession(session.getSessionId());
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }

    private void triggerOpenSession(String deferredSessionId) {
        WebServiceTask task = new WebServiceTask(parentActivity, new ReopenDeferredSessionWebServiceCall(deferredSessionId));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                String copyId = null;
                try {
                    JSONObject object = new JSONObject(response);
                    copyId = object.getString("Id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(copyId == null) {
                    Toast.makeText(parentActivity, "Could not reopen this session", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(parentActivity, SeatedSessionActivity.class);
                    intent.putExtra(GlobalSettings.EXTRA_SESSION_ID, copyId);
                    intent.putExtra(GlobalSettings.EXTRA_BILL_PRINT, true);
                    intent.putExtra(SeatedSessionActivity.EXTRA_IMMEDIATE_PAYMENT, true);
                    parentActivity.startActivity(intent);
                }
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Log.e("DeferredSessionA", response);
                Toast.makeText(parentActivity, "Could not reopen this session", Toast.LENGTH_LONG).show();
            }
        });
        task.execute();
    }

    private void triggerVoidSession(final String deferredSessionId) {
        WebServiceTask task = new WebServiceTask(parentActivity, new VoidDeferredSessionWebServiceCall(deferredSessionId));
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                Toast.makeText(parentActivity, "Account session has been voided", Toast.LENGTH_LONG).show();
                Iterator<DeferredSession> iterator = deferredSessions.iterator();
                while(iterator.hasNext()) {
                    DeferredSession next = iterator.next();
                    if(next.getSessionId().equals(deferredSessionId)) {
                        iterator.remove();
                        notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Log.e("DeferredSessionA", response);
                Toast.makeText(parentActivity, "Could not void this session", Toast.LENGTH_LONG).show();
            }
        });
        task.execute();
    }

    @Override
    public int getItemCount() {
        return deferredSessions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dateView;
        private TextView phoneNumber;
        private TextView billAmount;
        private TextView nameView;
        private Button settleButton;
        private Button voidButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateView = itemView.findViewById(R.id.endDate);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            billAmount = itemView.findViewById(R.id.billAmount);
            nameView = itemView.findViewById(R.id.nameView);
            settleButton = itemView.findViewById(R.id.settleButton);
            voidButton = itemView.findViewById(R.id.btn_void);
        }
    }
}
