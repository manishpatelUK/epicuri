package uk.co.epicuri.waiter.ui.menueditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.OnMenuItemsSelectedListener;
import uk.co.epicuri.waiter.interfaces.SaveCategoryListener;
import uk.co.epicuri.waiter.interfaces.SaveGroupListener;
import uk.co.epicuri.waiter.interfaces.SaveMenuListener;
import uk.co.epicuri.waiter.loaders.UpdateService;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Group;
import uk.co.epicuri.waiter.ui.EpicuriBaseActivity;
import uk.co.epicuri.waiter.ui.HubActivity;
import uk.co.epicuri.waiter.utils.Utils;
import uk.co.epicuri.waiter.webservice.CreateEditMenuCategoryWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditMenuGroupWebServiceCall;
import uk.co.epicuri.waiter.webservice.CreateEditMenuWebServiceCall;
import uk.co.epicuri.waiter.webservice.DeleteMenuLevelWebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceCall;
import uk.co.epicuri.waiter.webservice.WebServiceTask;

public class EditMenuActivity extends EpicuriBaseActivity implements
		SaveMenuListener,
		SaveCategoryListener,
		SaveGroupListener,
		OnMenuItemsSelectedListener {

	public static final String FRAGMENT_MENU_EDITOR = "MenuEditor";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.breadcrumbs)
    TextView breadcrumbs;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit_menu);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

		Utils.initActionBar(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(backStackChangedListener);
        updateBreadcrumbs();
		if(null == savedInstanceState){
			MenuFragment frag = new MenuFragment();
			fm.beginTransaction().add(R.id.frame, frag, FRAGMENT_MENU_EDITOR).commit();
		}
	}

    private FragmentManager.OnBackStackChangedListener backStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            updateBreadcrumbs();
        }
    };

    private void updateBreadcrumbs() {
        FragmentManager fm = getSupportFragmentManager();
        StringBuilder sb = new StringBuilder("Menu Manager");
        for(int i=0; i< fm.getBackStackEntryCount(); i++){
            sb.append(" > ").append(fm.getBackStackEntryAt(i).getBreadCrumbTitle());
        }
        breadcrumbs.setText(sb);
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home: {
			if(getSupportFragmentManager().getBackStackEntryCount() > 0){
				// clear back stack
				getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			} else {
				Intent intent = new Intent(this, HubActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void createMenu(CharSequence name, boolean active) {
		WebServiceCall call = new CreateEditMenuWebServiceCall(name.toString(), active);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {

				MenuFragment f = (MenuFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_MENU_EDITOR);
				if(null != f){
					f.refresh();
				}
			}
		});
		task.setIndicatorText("Saving menu");
		task.execute();
	}

	@Override
	public void saveMenu(String menuId, CharSequence name, boolean active, int order) {
		WebServiceCall call = new CreateEditMenuWebServiceCall(menuId, name.toString(), active, order);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				MenuFragment f = (MenuFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_MENU_EDITOR);
				if(null != f){
					f.refresh();
				}
			}
		});
		task.setIndicatorText("Saving menu");
		task.execute();
	}

	@Override
	public void deleteMenu(String menuId) {
		WebServiceCall call = new DeleteMenuLevelWebServiceCall(menuId, MenuLevelFragment.Level.MENU, menuId);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {

				MenuFragment f = (MenuFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_MENU_EDITOR);
				if(null != f){
					f.refresh();
				}
			}
		});
		task.setOnErrorListener(new WebServiceTask.OnErrorListener() {
			@Override
			public void onError(int code, String response) {
				if(code == 400) {
					Toast.makeText(EditMenuActivity.this, "Cannot delete this menu: it is currently in use.", Toast.LENGTH_SHORT).show();
				} else if(code == 404){
					Toast.makeText(EditMenuActivity.this, "Menu not found.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(EditMenuActivity.this, "Cannot delete this menu.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		task.setIndicatorText("Deleting menu");
		task.execute();
	}


	@Override
	public void createCategory(final String menuId, final CharSequence name,
			final String[] defaultCourses) {
		WebServiceCall call = new CreateEditMenuCategoryWebServiceCall(name.toString(), menuId, defaultCourses, 0);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(EditMenuActivity.this, new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}


	@Override
	public void saveCategory(EpicuriMenu.Category category, final String menuId, CharSequence name,
			String[] defaultCourses) {
		WebServiceCall call = new CreateEditMenuCategoryWebServiceCall(category.getId(), name.toString(), menuId, category.getGroups(), defaultCourses, category.getOrderIndex());
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(EditMenuActivity.this, new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}


	@Override
	public void deleteCategory(String categoryId, final String menuId) {
		WebServiceCall call = new DeleteMenuLevelWebServiceCall(categoryId, MenuLevelFragment.Level.CATEGORY, menuId);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(EditMenuActivity.this, new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}


	@Override
	public void createGroup(CharSequence name, String categoryId, final String menuId) {
		WebServiceCall call = new CreateEditMenuGroupWebServiceCall(name.toString(), categoryId, menuId, new ArrayList<String>(0), 0);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(EditMenuActivity.this, new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}

	@Override
	public void saveGroup(Group group, CharSequence name, String categoryId, final String menuId) {
		WebServiceCall call = new CreateEditMenuGroupWebServiceCall(group.getId(), name.toString(), categoryId, menuId, group.getItemIds(), group.getOrderIndex());
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.execute();
	}

	@Override
	public void deleteGroup(String id, final String menuId) {
		WebServiceCall call = new DeleteMenuLevelWebServiceCall(id, MenuLevelFragment.Level.GROUP, menuId);
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(EditMenuActivity.this, new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}


	@Override
	public void selectMenuItems(Group group, ArrayList<String> menuItemIds, final String menuId) {
		WebServiceCall call = new CreateEditMenuGroupWebServiceCall(group.getId(), group.getName(), group.getCategoryId(), menuId, menuItemIds, group.getOrderIndex());
		WebServiceTask task = new WebServiceTask(this, call, true);
		task.setOnCompleteListener(new WebServiceTask.OnSuccessListener() {
			
			@Override
			public void onSuccess(int code, String response) {
				UpdateService.expireData(EditMenuActivity.this, new Uri[]{Uri.withAppendedPath(EpicuriContent.MENU_URI, String.valueOf(menuId))});
			}
		});
		task.setIndicatorText(getString(R.string.webservicetask_alertbody));
		task.execute();
	}
}
