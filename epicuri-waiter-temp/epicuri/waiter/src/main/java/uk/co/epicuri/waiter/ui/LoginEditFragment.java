package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import uk.co.epicuri.waiter.model.StaffPermissions;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriLogin;
import uk.co.epicuri.waiter.interfaces.LoginEditListener;


public class LoginEditFragment extends DialogFragment implements TextWatcher {

	private EpicuriLogin login = null;
	private ArrayList<StaffPermissions> permissions = null;
	public static final String EXTRA_PERMISSIONS = "permissions";

	public static LoginEditFragment newInstance(EpicuriLogin login, ArrayList<StaffPermissions> permissions){
		LoginEditFragment frag = new LoginEditFragment();
		Bundle args = new Bundle();
		args.putParcelable(GlobalSettings.EXTRA_LOGIN, login);
		args.putParcelableArrayList(EXTRA_PERMISSIONS, permissions);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(null != getArguments()){
			login = getArguments().getParcelable(GlobalSettings.EXTRA_LOGIN);
			permissions = getArguments().getParcelableArrayList(EXTRA_PERMISSIONS);
		}
	}

	private EditText name;
	private EditText username;
	private EditText password;
	private EditText passwordVerify;
	private EditText pin;
	private Spinner permSpinner;
	private Button saveButton;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_loginedit, null, false);
		
		name = (EditText)v.findViewById(R.id.name_edit);
		username = (EditText)v.findViewById(R.id.username_edit);
		password = (EditText)v.findViewById(R.id.password_edit);
		passwordVerify = (EditText)v.findViewById(R.id.passwordverify_edit);
		pin = (EditText)v.findViewById(R.id.pin_edit);
		permSpinner = v.findViewById(R.id.permissions_spinner);
        ArrayAdapter<StaffPermissions> permAdapter;
        permAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, permissions);
        permSpinner.setAdapter(permAdapter);

        if(null != login){
            name.setText(login.getName());
            username.setText(login.getUsername());
            for (int i = 0; i < permissions.size(); i++){
                if(permissions.get(i).getStaffRole().toString().equals(login.getRole())){
                    permSpinner.setSelection(i);
                    break;
                }
            }
        } else {
            password.setHint(null);
            pin.setHint(null);
        }


		for(EditText e: new EditText[]{name, username, password, passwordVerify, pin}){
			e.addTextChangedListener(this);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(login == null ? "Create Login" : "Edit Login")
				.setView(v)
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						save();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				});
		
		// if current user, or new user then don't show delete button
		String currentLoginId = ((EpicuriBaseActivity) getActivity()).getLoggedInUser().getId();
		if(null != login
				&& !login.getId().equals(currentLoginId) //don't delete self
				&& !login.getUsername().equals("epicuriadmin")){ //can't delete epicuriadmin
			builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					delete();
				}
			});
		}
		
		AlertDialog d = builder.create();
		d.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				validate();
			}
		});
		return d;
	}
	
	private void validate(){
		boolean valid = true;
		
		if(username.getText().length() == 0){
			username.setError("Cannot be empty");
			valid = false;
		} else {
			username.setError(null);
		}
		
		if(name.getText().length() == 0){
			name.setError("Cannot be empty");
			valid = false;
		} else {
			name.setError(null);
		}

		// if creating a new account, pin & password are required
		if(null == login){
			if(password.getText().length() == 0){
				password.setError("Cannot be empty");
				valid = false;
			} else {
				password.setError(null);
			}
			if(pin.getText().length() == 0){
				pin.setError("Cannot be empty");
				valid = false;
			} else if(pin.getText().length() != 4){
				pin.setError("PIN must be 4 digits long");
				valid = false;
			} else {
				pin.setError(null);
			}
		} else {
			if(pin.getText().length() > 0 && pin.getText().length() != 4){
				pin.setError("PIN must be 4 digits long");
				valid = false;
			} else {
				pin.setError(null);
			}
		}
		
		if(password.getText().length() > 0
				&& !password.getText().toString().equals(passwordVerify.getText().toString())){
			passwordVerify.setError("Must match password");
			valid = false;
		} else {
			passwordVerify.setError(null);
		}

		saveButton = ((AlertDialog)getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
		saveButton.setEnabled(valid);
	}

	private void save(){
		LoginEditListener listener = (LoginEditListener)getActivity();
		String selectedRole =  permissions.get(permSpinner.getSelectedItemPosition()).getStaffRole().toString();
		if(null == login){
			listener.createLogin(name.getText(), username.getText(), password.getText(), pin.getText(), selectedRole);
		} else {
			listener.editLogin(login.getId(), name.getText(), username.getText(), password.getText(), pin.getText(), selectedRole);
		}
	}
	
	private void delete(){
		LoginEditListener listener = (LoginEditListener)getActivity();
		if(null == login){
			// do nothing, it's not saved anyway
		} else {
			listener.deleteLogin(login.getId());
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		validate();
	}
}
