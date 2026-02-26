package uk.co.epicuri.waiter.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.OnChargeListener;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.webservice.DeferSessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private List<EpicuriCustomer> customerList;
    private final String sessionId;
    private final OnChargeListener onChargeListener;

    public CustomerAdapter(List<EpicuriCustomer> customerList, String sessionId, OnChargeListener onChargeListener) {
        this.customerList = customerList;
        this.sessionId = sessionId;
        this.onChargeListener = onChargeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View customerView = inflater.inflate(R.layout.row_deferred_customer, parent, false);
        return new ViewHolder(customerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position < 0 || position >= customerList.size()) {
            return;
        }

        fillCustomerPosition(holder, position);
    }

    private void fillCustomerPosition(@NonNull ViewHolder holder, int position) {
        final EpicuriCustomer customer = customerList.get(position);
        holder.nameView.setText(customer.getName());
        String phoneNumber = customer.getPhoneNumber();
        if(phoneNumber == null || phoneNumber.length() == 0 || phoneNumber.equals("null")) {
            phoneNumber = "(no phone number)";
        }
        holder.phoneNumberView.setText(phoneNumber);
        holder.chargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                onChargeListener.charge(sessionId, customer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private TextView phoneNumberView;
        private Button chargeButton;

        public ViewHolder(View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.nameView);
            phoneNumberView = itemView.findViewById(R.id.phoneView);
            chargeButton = itemView.findViewById(R.id.chargeButton);
        }
    }
}
