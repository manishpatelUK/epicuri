package uk.co.epicuri.waiter.ui;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import uk.co.epicuri.waiter.utils.GlobalSettings;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.Furniture;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.model.EpicuriTable.Shape;
import uk.co.epicuri.waiter.webservice.SaveTableSeatingWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class TableEditActivity extends EpicuriBaseActivity {
	public static final String EXTRA_FURNITURE = "uk.co.epicuri.waiter.FURNITURE";
	public static final String EXTRA_DINERS = "uk.co.epicuri.waiter.DINERS";
	public static final String EXTRA_SHAPE = "uk.co.epicuri.waiter.SHAPE";
	
	private TableSeatingView tv;
	private String sessionId;
	private ArrayList<Furniture> furniture;
	private ArrayList<EpicuriSessionDetail.Diner> diners;
	private EpicuriTable.Shape tableShape;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sessionId = getIntent().getExtras().getString(GlobalSettings.EXTRA_SESSION_ID);
		furniture = getIntent().getExtras().getParcelableArrayList(EXTRA_FURNITURE);
		tableShape = Shape.fromInt(getIntent().getExtras().getInt(EXTRA_SHAPE));

		diners = getIntent().getExtras().getParcelableArrayList(EXTRA_DINERS);
		
		if(sessionId == null || sessionId.equals("-1") || sessionId.equals("0")){
			throw new IllegalArgumentException("sessionId invalid");
		}
		
		ActionBar ab = getSupportActionBar();
		if(ab != null) {
			ab.setDisplayUseLogoEnabled(false);
		}
		
		setContentView(R.layout.activity_tableedit);
		tv = findViewById(R.id.tableView);
		
		tv.setLayout(furniture, diners, tableShape);
		tv.setEditMode(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_tableedit, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_clear:
			tv.setLayout(null, diners, tableShape);
			return true;
		case android.R.id.home: {
			Intent intent = new Intent(this, SeatedSessionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}
		case R.id.menu_save:
			saveData();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
//		saveData();
		super.onPause();
	}

	private void saveData(){
		String tableDefinition = tv.getTableDefinition();
		WebServiceTask task = new WebServiceTask(this, new SaveTableSeatingWebServiceCall(tableDefinition, sessionId), true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				finish();
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

}
