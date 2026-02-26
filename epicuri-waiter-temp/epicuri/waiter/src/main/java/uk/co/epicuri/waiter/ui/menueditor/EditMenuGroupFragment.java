package uk.co.epicuri.waiter.ui.menueditor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.SaveGroupListener;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class EditMenuGroupFragment extends DialogFragment implements TextWatcher {
	public static final String EXTRA_OTHER_MENU_NAMES = "uk.co.epicuri.OTHER_MENU_NAMES";

	@InjectView(R.id.text1)
	EditText newName;

	public static EditMenuGroupFragment newInstance(EpicuriMenu.Group group, String categoryId, String menuId, ArrayList<String> otherNames){
		EditMenuGroupFragment frag = new EditMenuGroupFragment();
		Bundle args = new Bundle();
		
		if(null != group){
			args.putParcelable(GlobalSettings.EXTRA_GROUP, group);
		}
		args.putString(GlobalSettings.EXTRA_CATEGORY_ID, categoryId);
		args.putString(GlobalSettings.EXTRA_MENU_ID, menuId);
		args.putStringArrayList(EXTRA_OTHER_MENU_NAMES, otherNames);
		frag.setArguments(args);
		return frag;
	}
	
	private Button saveButton;
	private String parentCategoryId;
	private ArrayList<String> otherNames;
	
	private EpicuriMenu.Group group = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		group = getArguments().getParcelable(GlobalSettings.EXTRA_GROUP);
		otherNames = getArguments().getStringArrayList(EXTRA_OTHER_MENU_NAMES);
		
		parentCategoryId = getArguments().getString(GlobalSettings.EXTRA_CATEGORY_ID);
		final String menuId = getArguments().getString(GlobalSettings.EXTRA_MENU_ID);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edittext, null, false);
		ButterKnife.inject(this, view);

		newName.addTextChangedListener(this);
		newName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		if(null != group){
			newName.setText(group.getName());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
		.setTitle("Edit Group")
		.setView(view)
		.setNegativeButton("Cancel", null)
		.setPositiveButton("Create", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				CharSequence name = newName.getText();
				if(null == group){
					((SaveGroupListener)getActivity()).createGroup(name, parentCategoryId, menuId);
				} else {
					((SaveGroupListener)getActivity()).saveGroup(group, name, parentCategoryId, menuId);
				}
			}
		});

		AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				saveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
			}
		});

		return dialog;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(null == saveButton) return;
		for(String otherName: otherNames){
			if(s.toString().compareToIgnoreCase(otherName) == 0){
				newName.setError("Name already in use");
				saveButton.setEnabled(false);
				return;
			}
		}

		newName.setError(null);
		saveButton.setEnabled(true);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}