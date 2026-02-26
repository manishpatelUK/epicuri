package uk.co.epicuri.waiter.ui.menueditor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.SaveMenuListener;
import uk.co.epicuri.waiter.model.EpicuriMenuSummary;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class EditMenuDialogFragment extends DialogFragment implements TextWatcher {
    public static final String EXTRA_OTHER_MENU_NAMES = "uk.co.epicuri.OTHER_MENU_NAMES";

    private Button saveButton;

    @InjectView(R.id.name_edit)
    EditText newName;

    @InjectView(R.id.active_check)
    CheckBox active;

    private ArrayList<String> otherNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otherNames = getArguments().getStringArrayList(EXTRA_OTHER_MENU_NAMES);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final EpicuriMenuSummary menu = getArguments().getParcelable(GlobalSettings.EXTRA_MENUITEM);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_menu, null);
        ButterKnife.inject(this, v);

        newName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        if (null != menu) {
            newName.setText(menu.getName());
            active.setChecked(menu.isActive());
        }

        newName.addTextChangedListener(this);

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(menu == null ? "New Menu" : "Edit Menu")
                .setView(v)
                .setNegativeButton("Cancel", null);

        if (null == menu) {
            b.setPositiveButton("Create", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CharSequence name = newName.getText();
                    ((SaveMenuListener) getActivity()).createMenu(name, active.isChecked());
                }
            });
        } else {
            b.setPositiveButton("Save", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CharSequence name = newName.getText();
                    ((SaveMenuListener) getActivity()).saveMenu(menu.getId(), name,
                            active.isChecked(), menu.getOrder());
                }
            });
        }
        AlertDialog alertDialog = b.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                saveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                if (newName.getText().toString().isEmpty()) {
                    saveButton.setEnabled(false);
                    saveButton.setAlpha(.5f);
                    return;
                }

            }
        });
        return alertDialog;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        saveButton.setEnabled(!s.toString().isEmpty());

        if (s.toString().isEmpty()) {
            saveButton.setEnabled(false);
            saveButton.setAlpha(.5f);
            return;
        }

        saveButton.setAlpha(1f);

        for (String otherName : otherNames) {
            if (s.toString().compareToIgnoreCase(otherName) == 0) {
                newName.setError("Name already in use");
                saveButton.setEnabled(false);
                saveButton.setAlpha(.5f);
                return;
            }
        }
        newName.setError(null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}