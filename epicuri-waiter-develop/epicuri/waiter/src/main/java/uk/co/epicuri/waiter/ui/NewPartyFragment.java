package uk.co.epicuri.waiter.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.CustomerListener;
import uk.co.epicuri.waiter.interfaces.NewPartyDialogListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.CheckinLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.ServiceLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.TableLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriService;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.utils.GlobalSettings;

@SuppressLint("NewApi")
public class NewPartyFragment extends DialogFragment implements OnClickListener, CustomerListener {

    private static final int LOADER_CHECKINS = 1;
    private static final int LOADER_TABLES = 2;
    private static final int LOADER_SERVICES = 3;

    private static final String EXTRA_CHOSEN_CUSTOMER = "uk.co.epicuri.CHOSEN_CUSTOMER";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_GO_BUTTON_LABEL = "goButtonLabel";
    private static final String EXTRA_TEXT_SELECTION = "partyTextPreSelect";

    @InjectView(R.id.partyName)
    EditText partyName;
    @InjectView(R.id.customerChooser)
    Spinner customerChooser;
    @InjectView(R.id.serviceChooser)
    Spinner serviceChooser;
    @InjectView(R.id.serviceChooser_label)
    TextView serviceChooserLabel;

    private static final int NUMBER_IN_PARTY_OTHER = 6;
    @InjectViews({R.id.numberInParty_1, R.id.numberInParty_2, R.id.numberInParty_3,
            R.id.numberInParty_4, R.id.numberInParty_5, R.id.numberInParty_6,
            R.id.numberInParty_other})
    RadioButton[] numberInPartyButton;
    @InjectView(R.id.numberInParty_other_value)
    EditText numberInPartyValue;


    private CustomerAdapter poolAdapter;

    private NewPartyDialogListener listener;
    private EpicuriCustomer.Checkin checkin;
    private boolean someTextWasEntered = false;
    private boolean selectPartyNameText = true;

    private EpicuriCustomer chosenCustomer;
    private String[] tableIds = null;
    private ArrayList<EpicuriTable> tables;
    private ArrayList<EpicuriService> services;

    public static NewPartyFragment newInstance(EpicuriCustomer.Checkin checkin, String[] tableIds, String title, String goButtonLabel, boolean preSelectPartyName){
        NewPartyFragment frag = new NewPartyFragment();
        Bundle args = new Bundle(5);
        if(null != checkin) args.putParcelable(GlobalSettings.EXTRA_CHECKIN, checkin);
        if(null != tableIds) args.putStringArray(GlobalSettings.EXTRA_TABLE_IDS, tableIds);
        if(null != title) args.putString(EXTRA_TITLE, title);
        if(null != goButtonLabel) args.putString(EXTRA_GO_BUTTON_LABEL, goButtonLabel);
        args.putBoolean(EXTRA_TEXT_SELECTION, preSelectPartyName);

        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null != savedInstanceState){
            chosenCustomer = savedInstanceState.getParcelable(EXTRA_CHOSEN_CUSTOMER);
            checkin = savedInstanceState.getParcelable(GlobalSettings.EXTRA_CHECKIN);
        } else if(null != getArguments() && getArguments().containsKey(GlobalSettings.EXTRA_CHECKIN)){
            checkin = getArguments().getParcelable(GlobalSettings.EXTRA_CHECKIN);
        }
        if(null != getArguments()){
            tableIds = getArguments().getStringArray(GlobalSettings.EXTRA_TABLE_IDS);
            selectPartyNameText = getArguments().getBoolean(EXTRA_TEXT_SELECTION);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savestate) {
        savestate.putParcelable(EXTRA_CHOSEN_CUSTOMER, chosenCustomer);
        savestate.putParcelable(GlobalSettings.EXTRA_CHECKIN, checkin);
        super.onSaveInstanceState(savestate);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            listener = (NewPartyDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NewPartyDialogListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // manually show keyboard
        if(selectPartyNameText){
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            View view = getActivity() != null ? getActivity().getCurrentFocus(): null;
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        getLoaderManager().initLoader(LOADER_CHECKINS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>>() {

            @Override
            public Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> onCreateLoader(int id,
                                                                                            Bundle args) {
                return new EpicuriLoader<ArrayList<EpicuriCustomer.Checkin>>(getActivity(), new CheckinLoaderTemplate());
            }

            @Override
            public void onLoadFinished(
                    Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> loader,
                    LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>> data) {
                if (null == data) { // nothing returned, ignore
                    return;
                } else if (data.isError()) {
                    Toast.makeText(getActivity(), "NewPartyFragment error loading data", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkins = data.getPayload();
                poolAdapter.notifyDataSetChanged();

                if (checkin != null) {
                    int pos = checkins.indexOf(checkin);
                    if (pos >= 0) {
                        customerChooser.setSelection(pos + CustomerAdapter.NUMBER_OF_HEADERS);
                    }
                    checkin = null;
                }
            }

            @Override
            public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriCustomer.Checkin>>> loader) {
            }
        });

        if (null != tableIds) {
            getLoaderManager().initLoader(LOADER_TABLES, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriTable>>>() {
                        @Override
                        public Loader<LoaderWrapper<ArrayList<EpicuriTable>>> onCreateLoader(int id, Bundle args) {
                            return new EpicuriLoader<ArrayList<EpicuriTable>>(getActivity(), new TableLoaderTemplate());
                        }

                        @Override
                        public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriTable>>> loader, LoaderWrapper<ArrayList<EpicuriTable>> data) {
                            if (null == data) { // nothing returned, ignore
                                return;
                            } else if (data.isError()) {
                                Toast.makeText(getActivity(), "NewPartyFragment error loading data", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            tables = new ArrayList<EpicuriTable>();
                            for (EpicuriTable t : data.getPayload()) {
                                for (String tId : tableIds) {
                                    if (t.getId().equals(tId)) {
                                        tables.add(t);
                                    }
                                }
                            }
                            if (null != getDialog()) {
                                String title = setTitleFromTables();
                                if (!title.isEmpty() && (partyName != null && partyName.getText()
                                        .length() == 0 || partyName.getText().toString().equals(getString(R.string.default_party_name)))) {
                                    partyName.setText(title);
                                }
                            }
                            validate();
                        }

                        @Override
                        public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriTable>>> loader) {

                        }
                    }
            );
            getLoaderManager().initLoader(LOADER_SERVICES, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriService>>>() {

                @Override
                public Loader<LoaderWrapper<ArrayList<EpicuriService>>> onCreateLoader(int id, Bundle arguments) {
                    return new EpicuriLoader<ArrayList<EpicuriService>>(getActivity(), new ServiceLoaderTemplate());
                }

                @Override
                public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriService>>> loader,
                                           LoaderWrapper<ArrayList<EpicuriService>> data) {
                    if (null == data) { // nothing returned, ignore
                        return;
                    } else if (data.isError()) {
                        Toast.makeText(getActivity(), "NewPartyFragment error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    services = data.getPayload();
                    Iterator<EpicuriService> iterator = services.iterator();
                    while(iterator.hasNext()) {
                        EpicuriService service = iterator.next();
                        if(service.sessionType != null && (service.sessionType.equals("TAKEAWAY") || service.sessionType.equals("ADHOC"))) {
                            iterator.remove();
                        }
                    }
                    if (services.size() == 1) {
                        serviceChooser.setVisibility(View.GONE);
                        serviceChooserLabel.setVisibility(View.GONE);
                    } else {
                        ArrayAdapter<EpicuriService> serviceAdapter
                                = new ArrayAdapter<EpicuriService>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1, services);
                        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        serviceChooser.setAdapter(serviceAdapter);
                        serviceChooser.setVisibility(View.VISIBLE);
                        serviceChooserLabel.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriService>>> data) {
                }

            });
        }
    }

    private String setTitleFromTables() {
        if (getArguments().containsKey(EXTRA_TITLE)) {
            getDialog().setTitle(getArguments().getString(EXTRA_TITLE));
            return "";
        } else if (null == tableIds) {
            getDialog().setTitle(R.string.newParty_title);
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (EpicuriTable t : tables) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(t.getName());
            }
            getDialog().setTitle(getString(R.string.newParty_title_with_tables, sb));
            return sb.toString();
        }
    }

    ArrayList<EpicuriCustomer.Checkin> checkins = new ArrayList<EpicuriCustomer.Checkin>(0);

    private class CustomerAdapter extends BaseAdapter {
        private static final int NUMBER_OF_HEADERS = 2;

        private static final int ITEM_NO_CUSTOMER = -1;
        private static final int ITEM_CHOOSE_CUSTOMER = -2;

        public final LayoutInflater inflater;

        public CustomerAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);

            if (position == 0) {
                tv.setText("No Epicuri Account attached");
            } else if (position == 1) {
                if (chosenCustomer == null) {
                    tv.setText("Find Epicuri Account");
                } else {
                    tv.setText("Find Epicuri Account (" + chosenCustomer.getName() + ")");
                }
            } else {
                tv.setText(checkins.get(position - NUMBER_OF_HEADERS).getCustomer().getName());
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return checkins.size() + NUMBER_OF_HEADERS;
        }

        @Override
        public EpicuriCustomer.Checkin getItem(int position) {
            if (position < NUMBER_OF_HEADERS) return null;
            return checkins.get(position - NUMBER_OF_HEADERS);
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) return ITEM_NO_CUSTOMER;
            else if (position == 1) return ITEM_CHOOSE_CUSTOMER;
            else {
                return checkins.get(position - CustomerAdapter.NUMBER_OF_HEADERS).getCustomer().getId().hashCode();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);

            if (position == 0) {
                tv.setText("No epicuri account attached");
            } else if (position == 1) {
                if (chosenCustomer == null) {
                    tv.setText("Find Epicuri Account");
                } else {
                    tv.setText("Find Epicuri Account (" + chosenCustomer.getName() + ")");
                }
            } else {
                tv.setText(checkins.get(position - NUMBER_OF_HEADERS).getCustomer().getName());
            }

            return convertView;
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_newpartydialog, null);
        ButterKnife.inject(this, v);
        if(partyName.getText().toString().length() == 0){
            partyName.setText(R.string.default_party_name);

            if(selectPartyNameText) {
                partyName.requestFocus();
                partyName.selectAll();
            }
        }

        numberInPartyValue.setEnabled(numberInPartyButton[NUMBER_IN_PARTY_OTHER].isChecked());

        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                someTextWasEntered = true;
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

        if(null != checkin){
            partyName.setText(checkin.getCustomer().getName());
        }

        // default to two
        numberInPartyButton[1].setChecked(true);
        numberInPartyValue.setText("2");

        partyName.addTextChangedListener(watcher);
        poolAdapter = new CustomerAdapter(getActivity());
        customerChooser.setAdapter(poolAdapter);
        customerChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                if (id > 0 && partyName.getText().length() == 0) {
                    EpicuriCustomer cust = poolAdapter.getItem(position).getCustomer();
                    partyName.setText(cust.getName());
                } else if (id == CustomerAdapter.ITEM_CHOOSE_CUSTOMER) {
                    EpicuriCustomerLookup frag = new EpicuriCustomerLookup();
                    Bundle args = new Bundle();
                    args.putString(GlobalSettings.EXTRA_TARGET_FRAGMENT, NewPartyFragment.this.getTag());
                    frag.setArguments(args);
                    frag.show(getFragmentManager(), null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });

        numberInPartyValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate();
            }
        });

        builder.setView(v)
                .setTitle(R.string.newParty_title)
                .setNegativeButton(R.string.newParty_cancel, null)
                .setPositiveButton(getArguments().getString(EXTRA_GO_BUTTON_LABEL, "Add Party"), this);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                validate();
            }
        });

        if(partyName.getText().length() > 0 || null != tableIds) {
            // don't show keyboard if party name has already been populated
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        return dialog;
    }

    @OnClick({R.id.numberInParty_1, R.id.numberInParty_2, R.id.numberInParty_3,
            R.id.numberInParty_4, R.id.numberInParty_5, R.id.numberInParty_6,
            R.id.numberInParty_other})
    public void changePartySize(View v){
        for(int i=0; i<NUMBER_IN_PARTY_OTHER; i++){
            if(v == numberInPartyButton[i]){
                numberInPartyValue.setText(String.valueOf(i+1));
            } else {
                numberInPartyButton[i].setChecked(false);
            }
        }
        if(v == numberInPartyButton[NUMBER_IN_PARTY_OTHER]){
            numberInPartyValue.setEnabled(true);
            numberInPartyValue.selectAll();
            if(numberInPartyValue.requestFocus()){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(numberInPartyValue, InputMethodManager.SHOW_IMPLICIT);
            }

        } else {
            numberInPartyValue.setEnabled(false);
            numberInPartyButton[NUMBER_IN_PARTY_OTHER].setChecked(false);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        EpicuriCustomer customer;
        switch(customerChooser.getSelectedItemPosition()){
            case 0:
                customer = null;
                break;
            case 1:
                customer = chosenCustomer;
                break;
            default:
                customer = poolAdapter.getItem(customerChooser.getSelectedItemPosition()).getCustomer();
                break;
        }

        int numberInParty;
        try {
            numberInParty = Integer.parseInt(numberInPartyValue.getText().toString());
            if(numberInParty <= 0){
                return;
            }
        } catch (NumberFormatException e){
            return;
        }

        if(null == tableIds) {
            listener.onCreateNewParty(partyName.getText(), numberInParty, customer);
        } else {
            if(services.size() == 1) {
                listener.onCreateNewSession(partyName.getText(), numberInParty, customer, tableIds, services.get(0).id);
            } else {
                // If there is no item in spinner selected, return object is null
                // and EpicuriService object id cant be reached, so this must be handled
                if(serviceChooser.getSelectedItem() == null) {
                    Toast.makeText(getActivity(), "No service chosen! Try again.", Toast.LENGTH_SHORT).show();
                }
                // Proper feedback, should be fixed with android snackbar.


            }
        }
    }

    @Override
    public void setCustomer(EpicuriCustomer customer) {
        partyName.setText(customer.getName());
        chosenCustomer = customer;
        poolAdapter.notifyDataSetChanged();
    }

    private void validate(){
        boolean valid = true;
        if(partyName.getText().toString().trim().length() == 0){
            if(someTextWasEntered) partyName.setError("Cannot be blank");
            valid = false;
        } else {
            partyName.setError(null);
        }
        try {
            int numberInParty = Integer.parseInt(numberInPartyValue.getText().toString());
            if(numberInParty <= 0){
                valid = false;
            }
        } catch (NumberFormatException e){
            valid = false;
        }
        if(null != tableIds && null == services){
            // services not loaded
            valid = false;
        }
        if(null == getDialog()) return;
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(valid);
    }
}
