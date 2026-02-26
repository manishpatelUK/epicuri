package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;

public class DeferSessionWebServiceCall implements WebServiceCall{
    private String path;
    private String sessionId;
    private EpicuriCustomer customer;

    public DeferSessionWebServiceCall(String sessionId, EpicuriCustomer customer) {
        setUp(sessionId, customer);
    }

    private void setUp(String sessionId, EpicuriCustomer customer) {
        this.path = "/Session/" + sessionId + "/defer";
        this.sessionId = sessionId;
        this.customer = customer;
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
        JSONObject json = new JSONObject();

        try {
            if(customer.getId() != null) {
                json.put("Id", customer.getId());
            } else {
                if(customer.getName() != null) {
                    JSONObject nameJson = new JSONObject();
                    String[] names = customer.getName().trim().split("\\s");
                    nameJson.put("Firstname", names[0]);
                    StringBuilder lastName = new StringBuilder();
                    for(int i = 1; i < names.length; i++) {
                        lastName.append(names[i]);
                        if(i != (names.length-1)) {
                            lastName.append(" ");
                        }
                    }
                    nameJson.put("Surname", lastName.toString());

                    json.put("Name", nameJson);
                }
                if(customer.getPhoneNumber() != null) {
                    json.put("PhoneNumber", customer.getPhoneNumber());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[]{Uri.withAppendedPath(EpicuriContent.SESSION_URI, sessionId)};
    }

    @Override
    public boolean requiresToken() {
        return true;
    }
}
