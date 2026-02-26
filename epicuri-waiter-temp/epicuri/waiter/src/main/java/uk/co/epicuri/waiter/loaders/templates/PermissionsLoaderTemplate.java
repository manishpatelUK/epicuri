package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriEvent;
import uk.co.epicuri.waiter.model.EpicuriLogin;
import uk.co.epicuri.waiter.model.LocalSettings;
import uk.co.epicuri.waiter.model.StaffPermissions;

public class PermissionsLoaderTemplate implements LoadTemplate<ArrayList<StaffPermissions>>{
    @Override public Uri getUri() {
        return EpicuriContent.PERMSISSIONS_URI;
    }

    @Override public ArrayList<StaffPermissions> parseJson(String jsonString) throws JSONException {
        JSONArray permissionsJson = new JSONArray(jsonString);

        ArrayList<StaffPermissions> response = new ArrayList<>(permissionsJson.length());
        for(int i=0; i<permissionsJson.length(); i++){
            response.add(new StaffPermissions(permissionsJson.getJSONObject(i)));
        }

        return response;
    }
}
