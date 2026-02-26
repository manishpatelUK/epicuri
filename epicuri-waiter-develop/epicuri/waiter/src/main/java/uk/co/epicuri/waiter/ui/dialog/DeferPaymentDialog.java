package uk.co.epicuri.waiter.ui.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.CustomerAdapter;
import uk.co.epicuri.waiter.interfaces.OnChargeListener;
import uk.co.epicuri.waiter.interfaces.OnDeferCompleteListener;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.ui.HubActivity;
import uk.co.epicuri.waiter.ui.QuickOrderActivity;
import uk.co.epicuri.waiter.webservice.DeferSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.GetCustomerInteractionsWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class DeferPaymentDialog extends DialogFragment implements OnChargeListener {
    private static final String ARG_SESSION = "session";
    private static final String ARG_AMOUNT = "amount";
    private EpicuriSessionDetail session;
    private double amount;
    private List<EpicuriCustomer> customers = new ArrayList<>();
    private OnDeferCompleteListener onDeferCompleteListener;

    public static DeferPaymentDialog newInstance(EpicuriSessionDetail session, double amount) {
        DeferPaymentDialog frag = new DeferPaymentDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SESSION, session);
        args.putDouble(ARG_AMOUNT, amount);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if(arguments != null) {
            this.session = arguments.getParcelable(ARG_SESSION);
            this.amount = arguments.getDouble(ARG_AMOUNT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_defer_payment, null,false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);


        final CustomerAdapter adapter = new CustomerAdapter(customers, this.session.getId(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        WebServiceTask webServiceTask = new WebServiceTask(getContext(),new GetCustomerInteractionsWebServiceCall(),true);
        webServiceTask.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        EpicuriCustomer customer = new EpicuriCustomer(jsonObject);
                        customers.add(customer);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        webServiceTask.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override
            public void onError(int code, String response) {
                Log.e("DefPayment", response);
            }
        });
        webServiceTask.execute();

        setUpNewCustomerButton(view);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Defer Payment")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(onDeferCompleteListener != null) {
                            onDeferCompleteListener.deferComplete(null);
                        }
                        dismiss();
                    }
                })
                .setView(view)
                .create();
    }

    public void setOnDeferCompleteListener(OnDeferCompleteListener onDeferCompleteListener) {
        this.onDeferCompleteListener = onDeferCompleteListener;
    }

    private void setUpNewCustomerButton(View view) {
        Button newCustomerButton = view.findViewById(R.id.newCustomerButton);
        newCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                final EditText name = new EditText(getActivity());
                name.setId(R.id.name);
                name.setHint("Name");
                final EditText number = new EditText(getActivity());
                number.setInputType(InputType.TYPE_CLASS_PHONE);
                number.setHint("Phone number");
                number.setId(R.id.phoneNumber);
                layout.addView(name);
                layout.addView(number);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(layout);
                builder.setTitle("Create Customer Account");
                builder.setPositiveButton("Charge", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Context context = getContext();
                        if(context == null) return;

                        String enteredName = name.getText().toString().trim();
                        String phoneNumber = number.getText().toString().trim();
                        charge(session.getId(), enteredName, phoneNumber);

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                final boolean[] states = new boolean[]{false,false};
                name.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String s = editable.toString().trim();
                        states[0] = !TextUtils.isEmpty(s);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(states[0] && states[1]);
                    }
                });
                number.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String s = editable.toString().trim();
                        states[1] = !TextUtils.isEmpty(s);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(states[0] && states[1]);
                    }
                });
            }
        });
    }

    @Override
    public void charge(final String sessionId, final EpicuriCustomer customer) {
        final FragmentActivity context = getActivity();
        if(context == null) {
            return;
        }

        final Money totalChargeable = Money.of(LocalSettings.getCurrencyUnit(), amount);
        new AlertDialog.Builder(context)
                .setTitle("Are you sure?")
                .setMessage(Html.fromHtml("Charge <b>" + LocalSettings.formatMoneyAmount(totalChargeable, true) + "</b> to <b>" + customer.getName() + "</b>?"))
                .setPositiveButton("Charge", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        WebServiceTask task = new WebServiceTask(getContext(), new DeferSessionWebServiceCall(sessionId, customer));
                        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                            @Override
                            public void onSuccess(int code, String response) {
                                if(onDeferCompleteListener != null) {
                                    onDeferCompleteListener.deferComplete(customer);
                                }

                                String text = totalChargeable + " has been charged to " + customer.getName();
                                Toast.makeText(context, text, Toast.LENGTH_LONG).show();

                                if(!(context instanceof QuickOrderActivity)){
                                    Intent intent = new Intent(context, HubActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    context.startActivity(intent);
                                }
                            }
                        });
                        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
                            @Override
                            public void onError(int code, String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    String text = object.getString("message");
                                    Toast.makeText(context, "Error: " + text, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error: Could not put this session on account", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        task.execute();
                        dismissAllowingStateLoss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void charge(final String sessionId, final String name, final String phoneNumber) {
        EpicuriCustomer customer = new EpicuriCustomer(name, phoneNumber);
        charge(sessionId, customer);
    }
}
