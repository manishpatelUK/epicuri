package uk.co.epicuri.waiter.ui.menueditor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.NewModifierGroupListener;

public class NewModifierGroupFragment extends DialogFragment {

    @InjectView(R.id.text1)
    EditText newModifierGroupName;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edittext, null, false);
        ButterKnife.inject(this, view);
		newModifierGroupName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        newModifierGroupName.setHint(R.string.name_of_midifier_group);

		return new AlertDialog.Builder(getActivity())
            .setTitle(getString(R.string.create_new_modifier_group))
            .setView(view)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.create), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CharSequence name = newModifierGroupName.getText();
                    ((NewModifierGroupListener)getActivity()).createNewModifierGroup(name);
                }
            })
		.create();
	}
}