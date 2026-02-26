package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONObject;

public class CourseAwayWebServiceCall implements WebServiceCall {
    private final String body;
    private final String path;

    public CourseAwayWebServiceCall(String courseId, String sessionId){
        path = String.format("/Session/%s/courseAwaySent", sessionId);
        body = String.format("{\"Id\": \"%s\"}", courseId);
    }

    @Override
    public String getMethod() {
        return "PUT";
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[0];
    }

    @Override
    public boolean requiresToken() {
        return true;
    }
}
