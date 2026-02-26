package uk.co.epicuri.waiter.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.adapters.LoginAdapter;
import uk.co.epicuri.waiter.interfaces.LoginEditListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.LoginLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.PermissionsLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriLogin;
import uk.co.epicuri.waiter.model.StaffPermissions;
import uk.co.epicuri.waiter.webservice.CreateEditLoginWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteLoginWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;


public class LoginManagerActivity extends EpicuriBaseActivity implements OnItemClickListener, LoginEditListener {
	
	private static final int LOADER_LOGINS = 1;
	public static final int LOADER_PERMISSIONS = 2;
	
	private static final String FRAGMENT_NEW_LOGIN = "NewLogin";
	
	private ListView userList;
	private ArrayList<EpicuriLogin> logins = null;
	private ArrayList<StaffPermissions> permissions = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		userList = new ListView(this);
		
		setContentView(userList);
		userList.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		getSupportLoaderManager().restartLoader(LOADER_LOGINS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriLogin>>>() {

			@Override
			public Loader<LoaderWrapper<ArrayList<EpicuriLogin>>> onCreateLoader(
					int id, Bundle args) {
				return new EpicuriLoader<ArrayList<EpicuriLogin>>(LoginManagerActivity.this, new  LoginLoaderTemplate());
			}

			@Override
			public void onLoadFinished(
					Loader<LoaderWrapper<ArrayList<EpicuriLogin>>> loader,
					LoaderWrapper<ArrayList<EpicuriLogin>> data) {
				if(null == data){ // nothing returned, ignore
					return;
				}else if(data.isError()){
					Toast.makeText(LoginManagerActivity.this, " LoginManagerActivity error loading data", Toast.LENGTH_SHORT).show();
					return;
				}
				logins = data.getPayload();
				userList.setAdapter(new LoginAdapter(LoginManagerActivity.this, R.layout.row_login, logins));
			}

			@Override
			public void onLoaderReset(
					Loader<LoaderWrapper<ArrayList<EpicuriLogin>>> loader) {
				userList.setAdapter(null);
			}
		});

		getSupportLoaderManager().restartLoader(LOADER_PERMISSIONS, null, new LoaderManager.LoaderCallbacks<LoaderWrapper<ArrayList<StaffPermissions>>>() {

            @NonNull @Override
            public Loader<LoaderWrapper<ArrayList<StaffPermissions>>> onCreateLoader(int id, @Nullable Bundle args) {
                return new EpicuriLoader<>(LoginManagerActivity.this, new PermissionsLoaderTemplate());
            }

            @Override
            public void onLoadFinished(@NonNull Loader<LoaderWrapper<ArrayList<StaffPermissions>>> loader, LoaderWrapper<ArrayList<StaffPermissions>> data) {
                if (data == null){
                    return;
                } else if (data.isError()){
                    Toast.makeText(LoginManagerActivity.this, "Error loading permissions", Toast.LENGTH_SHORT).show();
                    return;
                }

                permissions = data.getPayload();
            }

            @Override
            public void onLoaderReset(@NonNull Loader<LoaderWrapper<ArrayList<StaffPermissions>>> loader) {

            }
        });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fragment_loginmanager, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_add: {
            LoginEditFragment frag = LoginEditFragment.newInstance(null, permissions);
            frag.show(getSupportFragmentManager(), FRAGMENT_NEW_LOGIN);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		
		EpicuriLogin login = logins.get(position);
		LoginEditFragment frag = LoginEditFragment.newInstance(login, permissions);
		frag.show(getSupportFragmentManager(), FRAGMENT_NEW_LOGIN);
	}

	@Override
	public void createLogin(CharSequence name, CharSequence username,
                            CharSequence password, CharSequence pin, CharSequence role) {
		CreateEditLoginWebServiceCall call;
		call = new  CreateEditLoginWebServiceCall(name, username, password, pin, role);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
        task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override public void onSuccess(int code, String response) {
                Toast.makeText(LoginManagerActivity.this, "Login created", Toast.LENGTH_SHORT).show();
            }
        });

        task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override public void onError(int code, String response) {
                Toast.makeText(LoginManagerActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        });
	}

	@Override
	public void editLogin(String id, CharSequence name, CharSequence username,
                          CharSequence password, CharSequence pin, String role) {
		CreateEditLoginWebServiceCall call;
		call = new  CreateEditLoginWebServiceCall(id, name, username, password, pin, role);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
            @Override public void onSuccess(int code, String response) {
                Toast.makeText(LoginManagerActivity.this, "Login updated", Toast.LENGTH_SHORT).show();

            }
        });

		task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
            @Override public void onError(int code, String response) {
                Toast.makeText(LoginManagerActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        });
	}

	@Override
	public void deleteLogin(String id) {
		DeleteLoginWebServiceCall call;
		call = new DeleteLoginWebServiceCall(id);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}
    
}
