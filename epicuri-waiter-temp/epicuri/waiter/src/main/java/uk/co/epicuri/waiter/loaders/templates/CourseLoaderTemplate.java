package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriMenu.Course;

public class CourseLoaderTemplate implements LoadTemplate<ArrayList<Course>> {
	private final Uri uri;
	/** get all courses */
	public CourseLoaderTemplate() {
		uri = EpicuriContent.COURSE_URI;
	}

	/** get courses for the specified service */
	public CourseLoaderTemplate(String serviceId) {
		uri = Uri.withAppendedPath(EpicuriContent.COURSE_URI, serviceId);
	}
	
	@Override
	public Uri getUri() {
		return uri;
	}

	@Override
	public ArrayList<Course> parseJson(String jsonString) throws JSONException {
		JSONArray courseArrayJson = new JSONArray(jsonString);
		
		ArrayList<Course> response = new ArrayList<EpicuriMenu.Course>(courseArrayJson.length());
		for(int i=0; i<courseArrayJson.length(); i++){
			response.add(new Course(courseArrayJson.getJSONObject(i)));
		}
		return response;
	}


}
