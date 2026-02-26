package uk.co.epicuri.waiter.ui.menueditor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.SaveCategoryListener;
import uk.co.epicuri.waiter.loaders.EpicuriLoader;
import uk.co.epicuri.waiter.loaders.LoaderWrapper;
import uk.co.epicuri.waiter.loaders.templates.CourseLoaderTemplate;
import uk.co.epicuri.waiter.loaders.templates.ServiceLoaderTemplate;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Course;
import uk.co.epicuri.waiter.model.EpicuriService;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class EditMenuCategoryDialogFragment extends DialogFragment implements OnItemClickListener, TextWatcher{

	public static final String EXTRA_OTHER_MENU_NAMES = "uk.co.epicuri.OTHER_MENU_NAMES";
	private static final int LOADER_SERVICES = 1;
	private static final int LOADER_COURSES = 2;
	private Button saveButton;
	private EpicuriMenu.Category category = null;
	private String parentMenuId ="-1";
	private ServiceCourse[] serviceCourses;
	private ArrayList<EpicuriMenu.Course> allCourses;
	private ArrayList<String> otherNames;

	@InjectView(R.id.services_list)
	ListView listView;

	@InjectView(R.id.name_edit)
	EditText nameEdit;
	
	public static EditMenuCategoryDialogFragment newInstance(EpicuriMenu.Category category, String menuId, ArrayList<String> otherNames){
		EditMenuCategoryDialogFragment frag = new EditMenuCategoryDialogFragment();
		Bundle args = new Bundle();
		
		if(null != category){
			args.putParcelable(GlobalSettings.EXTRA_CATEGORY, category);
		}
		args.putStringArrayList(EditMenuDialogFragment.EXTRA_OTHER_MENU_NAMES, otherNames);
		args.putString(GlobalSettings.EXTRA_MENU_ID, menuId);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		category = getArguments().getParcelable(GlobalSettings.EXTRA_CATEGORY);
		parentMenuId = getArguments().getString(GlobalSettings.EXTRA_MENU_ID);
		otherNames = getArguments().getStringArrayList(EXTRA_OTHER_MENU_NAMES);
	} 
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_menucategory, null);
		ButterKnife.inject(this, v);
		nameEdit.addTextChangedListener(this);
		
		if(null != category){
			nameEdit.setText(category.getName());
		}
		
		listView.setOnItemClickListener(this);
		getLoaderManager().initLoader(LOADER_SERVICES, null, serviceCallbacks);
		getLoaderManager().initLoader(LOADER_COURSES, null, courseCallbacks);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle("Edit Category")
		.setView(v)
		.setNegativeButton("Cancel", null)
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				save();
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				saveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
				saveButton.setEnabled(false);
			}
		});
		return alertDialog;
	}

	private void save(){
		CharSequence name = nameEdit.getText();
		
		ArrayList<String> chosenCourseIds = new ArrayList<String>();
		for(ServiceCourse sc: serviceCourses){
			if(sc.course != null){
				chosenCourseIds.add(sc.course.getId());
			}
		}

		String[] ids = new String[chosenCourseIds.size()];
		for(int i=0; i<chosenCourseIds.size(); i++){
			ids[i] = chosenCourseIds.get(i); 
		}
		if(null == category){
			((SaveCategoryListener)getActivity()).createCategory(parentMenuId, name, ids);
		} else {
			((SaveCategoryListener)getActivity()).saveCategory(category, parentMenuId, name, ids);
		}
	}
	
	private LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriService>>> serviceCallbacks = new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriService>>>(){
		public Loader<LoaderWrapper<ArrayList<EpicuriService>>> onCreateLoader(
				int id, Bundle args) {
			return new EpicuriLoader<ArrayList<EpicuriService>>(getActivity(), new ServiceLoaderTemplate() );
		}
	
		@Override
		public void onLoadFinished(
				Loader<LoaderWrapper<ArrayList<EpicuriService>>> loader,
				LoaderWrapper<ArrayList<EpicuriService>> data) {
			if(null == data) return;
			if(data.isError()) throw new RuntimeException();

			ArrayList<EpicuriService> services = data.getPayload();
			Iterator<EpicuriService> iterator = services.iterator();
			while(iterator.hasNext()) {
				EpicuriService service = iterator.next();
				if(service.sessionType != null && (service.sessionType.equals("TAKEAWAY") || service.sessionType.equals("ADHOC"))) {
					iterator.remove();
				}
			}
			serviceCourses = new ServiceCourse[services.size()];
			for(int i=0; i<services.size(); i++){
				ServiceCourse newSC = new ServiceCourse(services.get(i));
				if(null != category){
					for(EpicuriMenu.Course c: category.getDefaultCourses()){
						if(c.getServiceId().equals(newSC.service.id)){
							newSC.course = c;
							break;
						}
					} 
				}
				serviceCourses[i] = newSC;
			}
			listView.setAdapter(new ArrayAdapter<ServiceCourse>(getActivity(), android.R.layout.simple_list_item_1, serviceCourses));
		}
	
		@Override
		public void onLoaderReset(
				Loader<LoaderWrapper<ArrayList<EpicuriService>>> loader) {
		}
	};
	
	private LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> courseCallbacks = new LoaderCallbacks<LoaderWrapper<ArrayList<EpicuriMenu.Course>>>(){
		public Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> onCreateLoader(
				int id, Bundle args) {
			return new EpicuriLoader<ArrayList<EpicuriMenu.Course>>(getActivity(), new CourseLoaderTemplate());
		}
	
		@Override
		public void onLoadFinished(
				Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader,
				LoaderWrapper<ArrayList<EpicuriMenu.Course>> data) {
			if(null == data) return;
			if(data.isError()) throw new RuntimeException();
			
			allCourses = data.getPayload();
		}
	
		@Override
		public void onLoaderReset(
				Loader<LoaderWrapper<ArrayList<EpicuriMenu.Course>>> loader) {
		}
	};
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final ServiceCourse serviceCourse =  serviceCourses[position];
		
		final ArrayList<EpicuriMenu.Course> serviceCourses = new ArrayList<EpicuriMenu.Course>();
		int pos=0;
		int checkedItem = -1;
		if(null == allCourses) return;
		for(EpicuriMenu.Course c: allCourses){
			if(c.getServiceId().equals(serviceCourse.service.id)){
				serviceCourses.add(c);
				if(c.equals(serviceCourse.course)){
					checkedItem = pos;
				}
				pos++;
			}
		}

		final int finalCheckedItem = checkedItem;
		new AlertDialog.Builder(getActivity())
			.setTitle("Choose default course")
			.setSingleChoiceItems(
					new ArrayAdapter<EpicuriMenu.Course>(getActivity(), android.R.layout.simple_list_item_single_choice, serviceCourses),
					checkedItem,
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setCourseForService(serviceCourse.service, serviceCourses.get(which));
							if(which != finalCheckedItem) {
								saveButton.setEnabled(true);
							}
							dialog.dismiss();
						}
					})
			.show();
	}
	
	private void setCourseForService(EpicuriService service, Course course){
		for(ServiceCourse sc: serviceCourses){
			if(sc.service.equals(service)){
				sc.course = course;
				break;
			}
		}
		listView.setAdapter(new ArrayAdapter<ServiceCourse>(getActivity(), android.R.layout.simple_list_item_1, serviceCourses));
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(null == saveButton) return;
		if (s == null || s.toString().isEmpty() ){
			nameEdit.setError("Name can't be blank");
			saveButton.setEnabled(false);
			return;
		}

		for(String otherName: otherNames){
			if(s.toString().compareToIgnoreCase(otherName) == 0){
				nameEdit.setError("Name already in use");
				saveButton.setEnabled(false);
				return;
			}
		}
		nameEdit.setError(null);
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
