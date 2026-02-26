package uk.co.epicuri.waiter.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.CustomerListener;
import uk.co.epicuri.waiter.interfaces.OnTimeSetListener;
import uk.co.epicuri.waiter.interfaces.SetPandingTakeawayListener;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriCustomer.Address;
import uk.co.epicuri.waiter.model.EpicuriRestaurant;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.SessionType;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateEditTakeawayWebServiceCall;
import uk.co.epicuri.waiter.webservice.PostcodeLookupWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class TakeawayDetailFragment extends Fragment implements CustomerListener {
    private static final String EXTRA_DATE = "uk.co.epicuri.waiter.date";
    private static final String EXTRA_DELIVERY_COST = "deliveryCost";
    private static final String EXTRA_REQUESTED_TIME = "requestedTime";

    public static TakeawayDetailFragment newInstance() {
        TakeawayDetailFragment fragment = new TakeawayDetailFragment();
        return fragment;
    }

    public static TakeawayDetailFragment onDate(Calendar cal) {
        TakeawayDetailFragment frag = new TakeawayDetailFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_DATE, cal.getTime().getTime());
        frag.setArguments(args);
        return frag;
    }

    private boolean modified = false;
    private String sessionId = "-1";

    public boolean isModified() {
        return modified;
    }

    @InjectView(R.id.customername_edit)
    EditText name;
    @InjectView(R.id.phonenumber_edit)
    EditText phoneNumber;
    @InjectView(R.id.addressContainer)
    View addressContainer;
    @InjectView(R.id.address1_edit)
    EditText address1;
    @InjectView(R.id.address2_edit)
    EditText address2;
    @InjectView(R.id.address3_edit)
    EditText address3;
    @InjectView(R.id.address4_edit)
    EditText address4;
    @InjectView(R.id.deliveryCollection_radiogroup)
    RadioGroup deliveryCollectionRadio;
    @InjectView(R.id.deliverydate)
    Button deliveryDate;
    @InjectView(R.id.deliverytime)
    Button deliveryTime;
    @InjectView(R.id.note_edit)
    EditText note;
    @InjectView(R.id.customer_edit)
    TextView customerText;
    @InjectView(R.id.customerlookup_button)
    Button customerLookup;
    @InjectView(R.id.pendingreason_title)
    TextView pendingReason_title;
    @InjectView(R.id.pendingreason_text)
    TextView pendingReason_text;

    private EpicuriRestaurant restaurant;

    private Calendar requestedTime = Calendar.getInstance();
    private double deliveryCost = -1;

    private EpicuriCustomer newCustomer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        restaurant = LocalSettings.getInstance(getActivity()).getCachedRestaurant();

        if (null != savedInstanceState) {
            deliveryCost = savedInstanceState.getLong(EXTRA_DELIVERY_COST);
            requestedTime.setTime(new Date(savedInstanceState.getLong(EXTRA_REQUESTED_TIME)));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(EXTRA_DELIVERY_COST, deliveryCost);
        outState.putLong(EXTRA_REQUESTED_TIME, requestedTime.getTime().getTime());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((SetPandingTakeawayListener) getActivity()).setPendingTakeaway(createPendingTakeaway());
    }

    @Override
    public void onResume() {
        super.onResume();
        TakeawayActivity.PendingTakeaway pt = ((SetPandingTakeawayListener) getActivity()).getPendingTakeaway();

        if (null == pt) {
            requestedTime = Calendar.getInstance();

            // try to add on the minimum delay for takeaways
            String defaultWindow = restaurant.getRestaurantDefault(EpicuriRestaurant.DEFAULT_TAKEAWAYMINIMUMTIME);
            if (null != defaultWindow) {
                int delay = Integer.parseInt(defaultWindow);
                requestedTime.add(Calendar.MINUTE, delay);
            }

            Date tmpRequestedDate = new Date(getArguments().getLong(EXTRA_DATE));
            if (requestedTime.getTime().before(tmpRequestedDate)) {
                // requested date is not within bounds so use that
                requestedTime.setTime(tmpRequestedDate);
            }

            requestedTime.add(Calendar.MINUTE, 10 - requestedTime.get(Calendar.MINUTE) % 10);
            requestedTime.set(Calendar.MILLISECOND, 0);
            requestedTime.set(Calendar.SECOND, 0);

            deliveryCollectionRadio.check(R.id.radioCollection);
            addressContainer.setVisibility(View.GONE);
        } else {
            sessionId = pt.getId();
            newCustomer = pt.getCustomer();
            if (null == newCustomer) {
                customerText.setText("");
            } else {
                customerText.setText(newCustomer.getName());
            }
            name.setText(pt.getName());
            phoneNumber.setText(pt.getPhoneNumber());
            address1.setText(pt.getAddress().getStreet());
            address2.setText(pt.getAddress().getTown());
            address3.setText(pt.getAddress().getCity());
            address4.setText(pt.getAddress().getPostcode());
            note.setText(pt.getNote());
            if (pt.isDelivery()) {
                deliveryCollectionRadio.check(R.id.radioDelivery);
            } else {
                deliveryCollectionRadio.check(R.id.radioCollection);
            }
            requestedTime.setTime(pt.getTime());
            requestedTime.set(Calendar.MILLISECOND, 0);
            requestedTime.set(Calendar.SECOND, 0);

            if (null != pt.getRejectedReason()) {
                pendingReason_title.setVisibility(View.VISIBLE);
                pendingReason_text.setVisibility(View.VISIBLE);
                pendingReason_text.setText(pt.getRejectedReason());
            }
        }
        updateTimeDisplay();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.dialog_takeawaydetail, null);

        ButterKnife.inject(this, v);

        TextWatcher validateAfterChange = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        for (EditText e : new EditText[]{name, phoneNumber, address1}) {
            e.addTextChangedListener(validateAfterChange);
        }

        deliveryTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        deliveryDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        deliveryCollectionRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioCollection) {
                    addressContainer.setVisibility(View.GONE);
                } else {
                    addressContainer.setVisibility(View.VISIBLE);
                }
                validate();
            }
        });
        if (!restaurant.getTakeawayTypes().contains(SessionType.COLLECTION)) {
            deliveryCollectionRadio.check(R.id.radioDelivery);
            deliveryCollectionRadio.findViewById(R.id.radioCollection).setEnabled(false);
        }
        if (!restaurant.getTakeawayTypes().contains(SessionType.DELIVERY)) {
            deliveryCollectionRadio.check(R.id.radioCollection);
            deliveryCollectionRadio.findViewById(R.id.radioDelivery).setEnabled(false);
        }

        customerLookup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EpicuriCustomerLookup frag = new EpicuriCustomerLookup();
                Bundle args = new Bundle();
                args.putString(GlobalSettings.EXTRA_TARGET_FRAGMENT, TakeawayDetailFragment.this.getTag());
                frag.setArguments(args);
                frag.show(getFragmentManager(), null);
            }
        });
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_takeawayedit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save: {
                validationFired = true;
                if (validate()) {
                    checkTakeaway();
                } else {
                    Toast.makeText(getActivity(), "Please correct errors then submit again", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker() {
        DatePickerDialog d = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                requestedTime.set(Calendar.YEAR, year);
                requestedTime.set(Calendar.MONTH, monthOfYear);
                requestedTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateTimeDisplay();
                validate();
            }
        }, requestedTime.get(Calendar.YEAR), requestedTime.get(Calendar.MONTH), requestedTime.get(Calendar.DAY_OF_MONTH));
        d.show();
    }

    private void showTimePicker() {
        TenMinuteTimePickerDialog d = new TenMinuteTimePickerDialog(getActivity(), new OnTimeSetListener() {

            @Override
            public void onTimeSet(TenMinuteTimePicker view, int hourOfDay, int minute) {
                requestedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                requestedTime.set(Calendar.MINUTE, minute);
                updateTimeDisplay();
                validate();
            }
        }, requestedTime.get(Calendar.HOUR_OF_DAY), requestedTime.get(Calendar.MINUTE), true);
        d.show();
    }

    private void rollDateToNow() {
        requestedTime = Calendar.getInstance();
        requestedTime.set(Calendar.MILLISECOND, 0);
        requestedTime.set(Calendar.SECOND, 0);
        requestedTime.add(Calendar.MINUTE, 10 - requestedTime.get(Calendar.MINUTE) % 10);
        updateTimeDisplay();
    }

    private void updateTimeDisplay() {
        deliveryDate.setText(new SimpleDateFormat("E dd-MMM-yyyy", Locale.UK).format(requestedTime.getTime()));
        deliveryTime.setText(new SimpleDateFormat("HH:mm", Locale.UK).format(requestedTime.getTime()));
    }


    private void checkTakeaway() {

        WebServiceCall call = new CreateEditTakeawayWebServiceCall(
                sessionId,
                deliveryCollectionRadio.getCheckedRadioButtonId() == R.id.radioDelivery,
                name.getText().toString(),
                phoneNumber.getText().toString(),
                note.getText().toString(),
                requestedTime.getTime(),
                new Address(address1.getText(), address2.getText(), address3.getText(), address4.getText()),
                newCustomer,
                false);

        WebServiceTask task = new WebServiceTask(getActivity(), call, true);
        task.setIndicatorText("Checking takeaway");
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

            @Override
            public void onSuccess(int code, String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);

                    deliveryCost = -1;
                    if (responseJson.has("Cost") && !responseJson.isNull("Cost")) {
                        deliveryCost = responseJson.getDouble("Cost");
                    }
                    JSONArray warnings = responseJson.getJSONArray("Warning");
                    if (warnings.length() == 0) {
                        updateTakeawayAndProceed();
                    } else {
                        StringBuilder sb = new StringBuilder(getString(R.string.submitWarnings));
                        for (int i = 0; i < warnings.length(); i++) {
                            sb.append("\n * ").append(warnings.getString(i));
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Warnings")
                                .setMessage(sb)
                                .setPositiveButton("Submit anyway",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                updateTakeawayAndProceed();
                                            }
                                        })
                                .setNegativeButton("Go Back", null)
                                .show();
                    }

                } catch (JSONException e) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Submit Error")
                            .setMessage("An error occurred parsing response)")
                            .show();

                }
            }
        });

        task.execute();
    }


    private boolean validationFired = false;

    private boolean validate() {
        modified = true;
        // don't validate until "create" is clicked
        if (!validationFired) return true;

        boolean valid = true;
        final EditText[] mandatoryFields;
        if (deliveryCollectionRadio.getCheckedRadioButtonId() == R.id.radioDelivery) {
            mandatoryFields = new EditText[]{name, phoneNumber, address1};
        } else {
            mandatoryFields = new EditText[]{name, phoneNumber};
        }

        for (EditText v : mandatoryFields) {
            if (v.getText().length() == 0) {
                v.setError("Cannot be empty");
                valid = false;
            } else {
                v.setError(null);
            }
        }
        if (requestedTime.before(Calendar.getInstance())) {
            Toast.makeText(getActivity(), "Date was in the past and has been reset to the current time", Toast.LENGTH_SHORT).show();
            rollDateToNow();
            valid = false;
        }
        return valid;
    }

    @Override
    public void setCustomer(final EpicuriCustomer customer) {
        if (null != customer) {

            if (name.length() > 0
                    || phoneNumber.length() > 0
                    || (null != customer.getAddress() && address1.length() > 0)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Overwrite values")
                        .setMessage("This will overwrite values")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setCustomerFields(customer, true);
                            }
                        })
                        .setNeutralButton("Keep", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setCustomerFields(customer, false);
                            }
                        })
                        .show();
            } else {
                setCustomerFields(customer, true);
            }
        } else {
            newCustomer = null;
        }
    }

    private void setCustomerFields(EpicuriCustomer customer, boolean overwriteFields) {
        newCustomer = customer;
        customerText.setText(customer.getName());
        if (overwriteFields) {
            phoneNumber.setText(customer.getPhoneNumber());
            name.setText(customer.getName());
            Address address = customer.getAddress();
            if (null != address) {
                address1.setText(address.getStreet());
                address2.setText(address.getTown());
                address3.setText(address.getCity());
                address4.setText(address.getPostcode());
            }
        }
    }

    private TakeawayActivity.PendingTakeaway createPendingTakeaway() {
        TakeawayActivity.PendingTakeaway pt = new TakeawayActivity.PendingTakeaway();

        pt.setId(sessionId);
        pt.setDelivery(deliveryCollectionRadio.getCheckedRadioButtonId() == R.id.radioDelivery);
        pt.setName(name.getText().toString());
        pt.setPhoneNumber(phoneNumber.getText().toString());
        pt.setNote(note.getText().toString());

        // just in case
        requestedTime.set(Calendar.SECOND, 0);
        requestedTime.set(Calendar.MILLISECOND, 0);
        pt.setTime(requestedTime.getTime());
        pt.setAddress(new Address(address1.getText(), address2.getText(), address3.getText(), address4.getText()));
        pt.setCustomer(newCustomer);
        if (0 <= deliveryCost) {
            pt.setDeliveryCost(Money.of(LocalSettings.getCurrencyUnit(), deliveryCost));
        } else {
            pt.setDeliveryCost(null);
        }

        return pt;
    }

    private void updateTakeawayAndProceed() {
        View focus = getActivity().getCurrentFocus();
        if (null != focus) {
            InputMethodManager imm = (InputMethodManager) focus.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        ((SetPandingTakeawayListener) getActivity()).setPendingTakeaway(createPendingTakeaway());
        ((SetPandingTakeawayListener) getActivity()).editItems();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.postcodelookup_button)
    public void onViewClicked() {
        if (!address4.getText().toString().isEmpty()) {
            PostcodeLookupWebServiceCall call = new PostcodeLookupWebServiceCall(address4.getText().toString());
            WebServiceTask task = new WebServiceTask(getActivity(),call,true);
            task.setIndicatorText("Searching addresses");
            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                @Override
                public void onSuccess(int code, String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        List<Address> addresses = new ArrayList<>(jsonArray.length());
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            EpicuriCustomer.Address address = new Address(jsonObject);
                            addresses.add(address);
                        }
                        showAddressPickerDialog(addresses);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            task.execute();
        }else Toast.makeText(getActivity(),"Enter postcode first",Toast.LENGTH_SHORT).show();
    }

    private void showAddressPickerDialog(List<Address> addresses) {
        final ListAdapter adapter = new ArrayAdapter<Address>(getActivity(),android.R.layout.simple_list_item_1,addresses){

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                Address address = getItem(position);
                StringBuilder builder = new StringBuilder();
                if (address.getStreet()!=null && address.getStreet().length()>0)
                    builder.append(address.getStreet());
                if (address.getCity()!=null && address.getCity().length()>0)
                    builder.append(", ").append(address.getCity());
                if (address.getTown()!=null && address.getTown().length()>0)
                    builder.append(", ").append(address.getTown());
                textView.setText(builder.toString());
                return textView;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Address address = (Address) adapter.getItem(i);
                        address1.setText(address.getStreet());
                        address2.setText(address.getTown());
                        address3.setText(address.getCity());
                        address4.setText(address.getPostcode());
                    }
                })
                .setNegativeButton("Close",null);
        builder.create().show();
    }
}
