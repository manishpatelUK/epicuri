package uk.co.epicuri.waiter.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriTable.Shape;
import uk.co.epicuri.waiter.interfaces.NewTableListener;


public class NewTableDialogFragment extends DialogFragment {
	private static final String EXTRA_ID = "id";
	private static final String EXTRA_NAME = "name";
	private static final String EXTRA_SHAPE = "shape";
	
	public static NewTableDialogFragment newInstance(String name, Shape shape){
		return newInstance("-1", name, shape);
	}
	
	public static NewTableDialogFragment newInstance(String id, String name, Shape shape){
		Bundle args = new Bundle();
		if(id != null && !id.equals("-1")) args.putString(EXTRA_ID, id);
		args.putString(EXTRA_NAME, name);
		args.putInt(EXTRA_SHAPE, shape.getId());
		NewTableDialogFragment frag = new NewTableDialogFragment();
		frag.setArguments(args);
		return frag;
	}


	
	public NewTableListener listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listener = (NewTableListener)getActivity();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View textEntryView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_newtable, null, false);
        final EditText tableName = (EditText)textEntryView.findViewById(R.id.tablename_edit);
        final Spinner tableShape = (Spinner)textEntryView.findViewById(R.id.tableshape_spinner);
        
        final ArrayAdapter<Shape> tableShapeAdapter = new ArrayAdapter<Shape>(getActivity(), android.R.layout.simple_spinner_item, android.R.id.text1, Shape.values());
        tableShapeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tableShape.setAdapter(tableShapeAdapter);

        boolean edit = false;
        if(getArguments() != null){
        	edit = true;
        	tableName.setText(getArguments().getString(EXTRA_NAME));
        	tableShape.setSelection(getArguments().getInt(EXTRA_SHAPE)); // bit cheeky using the index here
        }
        
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setTitle(edit ? "Edit table" : "Create new table")
            .setView(textEntryView)
            .setPositiveButton(edit ? "Save" : "Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
//                	EpicuriTable newTable = new EpicuriTable(tableName.getText().toString());
                	Shape shape = tableShapeAdapter.getItem(tableShape.getSelectedItemPosition());
                	String tableNameString = tableName.getText().toString().toUpperCase();
                	
                	if(getArguments() != null && getArguments().containsKey(EXTRA_ID)){
                        String tableId = getArguments().getString(EXTRA_ID);
                    	listener.updateTable(tableId, tableNameString, shape);
                    } else {
                    	listener.createNewTable(tableNameString, shape);
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                }
            })
            .create();
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	return dialog;
	}

}
