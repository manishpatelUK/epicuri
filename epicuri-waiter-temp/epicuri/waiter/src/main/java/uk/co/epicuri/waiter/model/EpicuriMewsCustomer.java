package uk.co.epicuri.waiter.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pharris on 09/02/15.
 */
public class EpicuriMewsCustomer {
	String firstName;
	String lastName;
	String roomNumber;
	String id;
//	Money creditLimit;

	public EpicuriMewsCustomer(JSONObject object) throws JSONException {
		firstName = object.getString("FirstName");
		lastName = object.getString("LastName");
		roomNumber = object.isNull("RoomNumber") ? "" : object.getString("RoomNumber");
		id = object.getString("Id");
	}

	@Override
	public String toString() {
		return String.format("%s %s (%s)", firstName, lastName, roomNumber);
	}

	public JSONObject toJson() throws JSONException {
		JSONObject customer = new JSONObject();
		customer.put("FirstName", firstName);
		customer.put("LastName", lastName);
		customer.put("RoomNumber", roomNumber);
		customer.put("Id", id);
		return customer;
	}
}
