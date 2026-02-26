package uk.co.epicuri.waiter.ui.menueditor;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.CustomerListener;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.webservice.EditPartySessionWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

@SuppressLint("NewApi")
public class PartyDetailsFragment extends DialogFragment implements OnClickListener, CustomerListener {

    private static final String EXTRA_SESSION_ID = "sessionId";
    private static final String EXTRA_PARTY_ID = "partyId";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_NO_DINERS = "diners";
    private static final String EXTRA_REFRESH_SESSION = "refreshSession";

    @InjectView(R.id.partyName)
    EditText partyName;

    private static final int NUMBER_IN_PARTY_OTHER = 6;
    @InjectViews({R.id.numberInParty_1, R.id.numberInParty_2, R.id.numberInParty_3,
            R.id.numberInParty_4, R.id.numberInParty_5, R.id.numberInParty_6,
            R.id.numberInParty_other})
    RadioButton[] numberInPartyButton;
    @InjectView(R.id.numberInParty_other_value)
    EditText numberInPartyValue;

    private boolean someTextWasEntered = false;

    public static PartyDetailsFragment newInstance(String sessionId, String partyId, String name,
            int numDiners, boolean refreshSession){
        PartyDetailsFragment frag = new PartyDetailsFragment();
        Bundle args = new Bundle(4);
        args.putString(EXTRA_SESSION_ID,sessionId);
        args.putString(EXTRA_PARTY_ID, partyId);
        args.putString(EXTRA_TITLE, name);
        args.putInt(EXTRA_NO_DINERS, numDiners);
        args.putBoolean(EXTRA_REFRESH_SESSION, refreshSession);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // manually show keyboard
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_partydetailsdialog, null);
        ButterKnife.inject(this, v);
        if(partyName.getText().toString().length() == 0){
            partyName.setText("Guest");
            partyName.requestFocus();
            partyName.selectAll();
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

        if(null != getArguments().getString(EXTRA_TITLE)){
            partyName.setText(getArguments().getString(EXTRA_TITLE));
        }

        if(0 != getArguments().getInt(EXTRA_NO_DINERS)){
            int nDiners = getArguments().getInt(EXTRA_NO_DINERS);
            numberInPartyValue.setText(String.valueOf(nDiners));
            if(nDiners <= NUMBER_IN_PARTY_OTHER) {
                numberInPartyButton[nDiners-1].setChecked(true);
            }
        } else {
            // default to two
            numberInPartyButton[1].setChecked(true);
            numberInPartyValue.setText("2");
        }

        partyName.addTextChangedListener(watcher);

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
                .setTitle("Edit party")
                .setNegativeButton(R.string.newParty_cancel, null)
                .setPositiveButton("SAVE", this);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                validate();
            }
        });

        if(partyName.getText().length() > 0) {
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
        final int numberInParty;
        try {
            numberInParty = Integer.parseInt(numberInPartyValue.getText().toString());
            if(numberInParty <= 0){
                return;
            }
        } catch (NumberFormatException e){
            return;
        }

        WebServiceTask task = new WebServiceTask(getActivity(), new
                EditPartySessionWebServiceCall(getArguments().getString(EXTRA_SESSION_ID),
                getArguments().getString(EXTRA_PARTY_ID), numberInParty, partyName.getText()
                .toString(), getArguments().getBoolean(EXTRA_REFRESH_SESSION)));
        final WeakReference<FragmentActivity> activity = new WeakReference<>(getActivity());
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override
            public void onSuccess(int code, String response) {
                try {
                    JSONObject sessionsJson = new JSONObject(response);
                    EpicuriSessionDetail sessionDetail = new EpicuriSessionDetail(sessionsJson);
                    if((sessionDetail.getDiners().size()-1) != numberInParty) {
                        Toast.makeText(activity.get(), "Cannot reduce number of diners: orders are present.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        task.setIndicatorText("Updating, please wait.");
        task.execute();
    }

    @Override
    public void setCustomer(EpicuriCustomer customer) {
        partyName.setText(customer.getName());
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
        if(null == getDialog()) return;
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(valid);
    }
}
