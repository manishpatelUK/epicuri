package uk.co.epicuri.waiter.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.NewTableListener;
import uk.co.epicuri.waiter.interfaces.OnTableChangeListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.templates.LayoutLoaderTemplate;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.OneOffLoader;
import uk.co.epicuri.waiter.loaders.templates.SessionsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriFloor;
import uk.co.epicuri.waiter.model.EpicuriFloor.Layout;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.model.EpicuriTable.Shape;
import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.webservice.CreateEditTableWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteTableWebServiceCall;
import uk.co.epicuri.waiter.webservice.SaveLayoutWebServiceCall;
import uk.co.epicuri.waiter.webservice.SelectLayoutWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

import static uk.co.epicuri.waiter.utils.GlobalSettings.EXTRA_LAYOUT;
import static uk.co.epicuri.waiter.utils.GlobalSettings.LOADER_LAYOUT;

public class FloorplanEditActivity extends EpicuriBaseActivity implements NewTableListener, OnTableChangeListener {

	private List<EpicuriSessionDetail> state;

	private FloorplanView floorplanView;
	private EpicuriFloor floor;
	private EpicuriFloor.Layout layout;

	private static final String NO_TABLE = "-1";
	private String selectedTableId = NO_TABLE;

	/** this is true when the waiter is temporarily editing the live layout */
	private boolean editingLiveLayout = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_floorplanedit);
		floorplanView = (FloorplanView)findViewById(R.id.floorPlan);

		floorplanView.setOnTableChangeListener(this);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

		final Bundle extras = getIntent().getExtras();
		if(!extras.containsKey(GlobalSettings.EXTRA_FLOOR)){
			throw new IllegalArgumentException("mising EXTRA_FLOOR");
		}
		if(extras.containsKey(GlobalSettings.EXTRA_CURRENT) && extras.getBoolean(GlobalSettings.EXTRA_CURRENT)){
			editingLiveLayout = true;
			getSupportLoaderManager().initLoader(GlobalSettings.LOADER_SESSION, null, sessionDetailLoaderCallbacks);
		}

		floor = extras.getParcelable(GlobalSettings.EXTRA_FLOOR);

		floorplanView.setBackgroundFilename(floor.getFloorBackgroundImage());

		floorplanView.setIsEditable(true);

		LoaderManager lm = getSupportLoaderManager();

		// on first load only
		if(null == savedInstanceState){
			// if layout is set, then load it otherwise create new empty one
			if(extras.containsKey(GlobalSettings.EXTRA_LAYOUT_ID)){
				final String layoutId = extras.getString(GlobalSettings.EXTRA_LAYOUT_ID);
				if(layoutId == null || layoutId.equals("0") || layoutId.equals("-1")){
					lm.destroyLoader(GlobalSettings.LOADER_LAYOUT);
				} else {
					lm.initLoader(GlobalSettings.LOADER_LAYOUT, extras, new LoaderManager.LoaderCallbacks<Layout>() {
						@Override
						public Loader<Layout> onCreateLoader(int id, Bundle args) {
							LayoutLoaderTemplate ll = new LayoutLoaderTemplate(layoutId);
							return new OneOffLoader<EpicuriFloor.Layout>(FloorplanEditActivity.this, ll);
						}

						@Override
						public void onLoadFinished(Loader<Layout> loader, Layout data) {
							if (null == data) {
								Toast.makeText(FloorplanEditActivity.this, "FloorplanEditActivity error loading data", Toast.LENGTH_SHORT).show();
								return;
							}
							if(!paused) {
                                layout = data;
                                floorplanView.setLayout(layout.getTables());
                            }
						}

						@Override
						public void onLoaderReset(Loader<Layout> arg0) {
						}
					});
				}
			}
		} else {
		    layout = savedInstanceState.getParcelable(GlobalSettings.EXTRA_LAYOUT);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(GlobalSettings.EXTRA_LAYOUT, layout);
        super.onSaveInstanceState(outState);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }
    boolean paused = false;
    @Override protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_floorplanedit, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home: {
			// if unsaved changes, prompt. Otherwise just close
			if(floorplanView.isChanged()){
				promptUnsavedChanges();
			} else {
				finish();
			}
			return true;
		}
		case R.id.menu_addTable: {
			NewTableDialogFragment fragment = new NewTableDialogFragment();
			fragment.show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_NEW_TABLE);
			return true;
		}
		case R.id.menu_save: {
			showSaveDialog();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if(floorplanView.isChanged()){
			promptUnsavedChanges();
		} else {
			super.onBackPressed();
		}
	}

	private void promptUnsavedChanges(){
		new AlertDialog.Builder(this)
		.setTitle("Unsaved changes exist")
		.setPositiveButton("Exit and discard changes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		})
		.setNegativeButton("Stay here", null)
		.show();
	}

	private void showSaveDialog(){
		showSaveDialog(false, "");
	}

	private void showSaveDialog(boolean asNew, String newName){
		final View saveView = getLayoutInflater().inflate(R.layout.dialog_savelayout, null, false);
		final CheckBox saveAsNew = (CheckBox)saveView.findViewById(R.id.saveAsNew_check);
		final EditText layoutName = (EditText)saveView.findViewById(R.id.layoutname_edit);
		final View layoutNameContainer = saveView.findViewById(R.id.layoutname_container);
		final TextView saveTextBlurb = (TextView)saveView.findViewById(R.id.saveblurb_text);

		saveAsNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					layoutNameContainer.setVisibility(View.VISIBLE);
				} else {
					layoutNameContainer.setVisibility(View.GONE);
				}
			}
		});
		String label;

		if(null != layout && layout.getId() != null && !layout.getId().equals("0") && !layout.getId().equals("-1")){
			layoutName.setText(layout.getName());
			// Editing
			saveAsNew.setVisibility(View.VISIBLE);
			saveAsNew.setChecked(asNew);
			layoutNameContainer.setVisibility(asNew ? View.VISIBLE : View.GONE);
			label = getString(R.string.save_layout);
			if(editingLiveLayout){
				layoutName.setText("");
				saveTextBlurb.setText(R.string.savecurrentlayout_overwrite);
			} else {
				saveTextBlurb.setText(R.string.savelayout_overwrite);
			}
		} else {
			// creating new
			saveAsNew.setVisibility(View.GONE);
			saveAsNew.setChecked(true);
			label = getString(R.string.create_layout);
			saveTextBlurb.setText(R.string.savelayout_create);
		}

		new AlertDialog.Builder(FloorplanEditActivity.this)
		.setTitle(label)
		.setView(saveView)
		.setPositiveButton(label, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(editingLiveLayout && !saveAsNew.isChecked()){
					// if we're editing live then either amend the existing transient layout or save a transient copy of the current layout
					if(layout.isTemporary()){
						saveLayout(false, GlobalSettings.TRANSIENT_LAYOUT_NAME, true);
					} else {
						saveLayout(true, GlobalSettings.TRANSIENT_LAYOUT_NAME, true);
					}
					return;
				} else {
					saveLayout(saveAsNew.isChecked(), layoutName.getText().toString(), false);
				}
			}
		})
		.setNegativeButton(getString(R.string.cancel), null)
		.show();
	}

	private void saveLayout(final boolean asNew, final String newName, final boolean liveLayout){
		if(asNew){
			ArrayList<String> names = getIntent().getExtras().getStringArrayList(GlobalSettings.EXTRA_LAYOUT_NAMES);
			for(String name: names){
				if(newName.equals(name)){
					new AlertDialog.Builder(this).setTitle("Name already in use")
					.setMessage("That name is already in use for a layout on this floor")
					.setPositiveButton("Edit", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							showSaveDialog(asNew, newName);
						}
					})
					.show();
					return;
				}
			}
		}
		if(newName.isEmpty()){
			new AlertDialog.Builder(this).setTitle("Name cannot be blank")
			.setMessage("You need to specify a name to save this layout")
			.setPositiveButton("Edit", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					showSaveDialog(asNew, newName);
				}
			})
			.show();
			return;
		}

		SaveLayoutWebServiceCall.Builder saveLayoutCallBuilder = new SaveLayoutWebServiceCall.Builder(floorplanView.getTables(), floor, newName);

		// layout exists, has an ID and overwrite tickbox is ticked
		if(null != layout && layout.getId() != null && !layout.getId().equals("0") && !layout.getId().equals("-1") && !asNew){
			saveLayoutCallBuilder.setOverwrite(layout.getId());
			saveLayoutCallBuilder.setName(layout.getName());
		}
		if(liveLayout){
			saveLayoutCallBuilder.setTemporary(true);
		}

		WebServiceTask task = new WebServiceTask(FloorplanEditActivity.this, saveLayoutCallBuilder.build(), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				if(liveLayout && asNew && response != null){
					try{
						// if that worked, then apply the layout to the floor
						JSONObject responseJson = new JSONObject(response);
						String newLayoutId = responseJson.getString("Id");
						WebServiceTask task = new WebServiceTask(FloorplanEditActivity.this, new SelectLayoutWebServiceCall(floor.getId(), newLayoutId), true);

						task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

							@Override
							public void onSuccess(int code, String response) {
								// if that worked, then finish this activity
								finish();
							}
						});
						task.setIndicatorText(getString(R.string.webservicetask_alertbody));
						task.execute();
					} catch (JSONException e){
						throw new RuntimeException(e);
					}
				} else {
					// if that worked, then finish this activity
					finish();
				}
			}
		});
		task.setIndicatorText("Saving layout");
		task.execute();
	}

	@Override
	public void createNewTable(final String name, final Shape shape) {
		updateTable("-1", name, shape);
	}

	@Override
	public void updateTable(final String tableId, final String name, final Shape shape) {
		boolean valid = true;
		if(null == name || name.isEmpty()){
			valid = false;
		} else {
			for(EpicuriTable t: floorplanView.getTables()){
				if(t.getName().equals(name) && !t.getId().equals(tableId)){
					valid =false;
					break;
				}
			}
		}

		if(!valid){
			new AlertDialog.Builder(this).setTitle("Invalid Name")
			.setMessage("Sorry, name cannot be empty or already in use on this floor")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					NewTableDialogFragment fragment;
					if(tableId != null && !tableId.equals("0") && !tableId.equals("-1")){
						fragment = NewTableDialogFragment.newInstance(tableId, name, shape);
					} else {
						fragment = NewTableDialogFragment.newInstance(name, shape);
					}
					fragment.show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_NEW_TABLE);
				}
			})
			.show();

			return;
		}

		WebServiceCall call;
		if(tableId != null && !tableId.equals("0") && !tableId.equals("-1")){
			call = new CreateEditTableWebServiceCall(tableId, name, shape);
		} else {
			call = new CreateEditTableWebServiceCall(name, shape);
		}
		WebServiceTask task = new WebServiceTask(this,call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {

			@Override
			public void onSuccess(int code, String response) {
				try{
					JSONObject newTableJson = new JSONObject(response);
					EpicuriTable t = new EpicuriTable(newTableJson);
					t.scaleHeight(50);
					t.scaleWidth(50);
					floorplanView.addTable(t);
				} catch (JSONException e){
					e.printStackTrace();
					Toast.makeText(FloorplanEditActivity.this, "Cannot understand response", Toast.LENGTH_SHORT).show();
				}
			}
		});
		task.setOnErrorListener(new WebServiceTask.OnErrorListener() {

			@Override
			public void onError(int code, String response) {
				if(code == 400) {
					Toast.makeText(FloorplanEditActivity.this, "Table name already exists on another floor", Toast.LENGTH_SHORT).show();
				} else if(code == 403) {
					Toast.makeText(FloorplanEditActivity.this, "Not allowed on this account. To upgrade please call Support.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	ActionMode mMode = null;


	@Override
	public void onTableSelected(String tableId) {
		selectedTableId = tableId;
		if(null == mMode){
			mMode = startSupportActionMode(new TableSelectionActionMode());
            // EP-876 bugfix https://code.google.com/p/android/issues/detail?id=159527
            if(null != mMode) mMode.invalidate();
		} else {
			mMode.invalidate();
		}
	}

	@Override
	public void onNoTableSelected() {
		selectedTableId = NO_TABLE;
		if(null != mMode) mMode.finish();
	}

	@Override
	public void onHighlightedTablesChanged(boolean selected) {

	}

	private LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> sessionDetailLoaderCallbacks = new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriSessionDetail>>>() {

		@Override
		public Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> onCreateLoader(int id, Bundle args) {
			return new EpicuriLoader<ArrayList<EpicuriSessionDetail>>(FloorplanEditActivity.this, new SessionsLoaderTemplate());
		}

		@Override
		public void onLoadFinished(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader,
				LoaderWrapper<ArrayList<EpicuriSessionDetail>> data) {
			if(null == data){ // nothing returned, ignore
				return;
			}else if(data.isError()){
				Toast.makeText(FloorplanEditActivity.this, "error loading data", Toast.LENGTH_SHORT).show();
				return;
			}
			state = data.getPayload();
			floorplanView.setState(data.getPayload());
		}

		@Override
		public void onLoaderReset(Loader<LoaderWrapper<ArrayList<EpicuriSessionDetail>>> loader) {
			if(null != floorplanView){
				floorplanView.setState(null);
			}
		}
	};

	private class TableSelectionActionMode implements ActionMode.Callback{

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getMenuInflater().inflate(R.menu.action_tableselection, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO decide whether to show table
			if(null == state) return false;
			EpicuriSessionDetail session = EpicuriSessionDetail.getSessionForTable(state, selectedTableId);
			menu.findItem(R.id.menu_delete).setVisible(null == session);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()){
			case R.id.menu_edit: {
				EpicuriTable t = floorplanView.getTable(selectedTableId);

				NewTableDialogFragment fragment = NewTableDialogFragment.newInstance(t.getId(), t.getName(), t.getShape());
				fragment.show(getSupportFragmentManager(), GlobalSettings.FRAGMENT_NEW_TABLE);

				return true;
			}
			case R.id.menu_delete: {
				new AlertDialog.Builder(FloorplanEditActivity.this)
					.setTitle("Delete table")
					.setMessage("Are you sure you want to delete?")
					.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							WebServiceCall call = new DeleteTableWebServiceCall(selectedTableId);
                            WebServiceTask task = new WebServiceTask(FloorplanEditActivity.this, call);
                            task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
                                @Override
                                public void onSuccess(int code, String response) {
                                    if(code != 400 && code != 403){
                                        Toast.makeText(FloorplanEditActivity.this, "Table is deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            task.execute();
                            floorplanView.deleteTable(selectedTableId);
                        }
					})
					.setNegativeButton("Cancel", null)
					.show();
				return true;
			}
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			floorplanView.deselectTable();
			mMode = null;
		}

	}
}
