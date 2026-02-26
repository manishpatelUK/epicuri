package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriReservation;

public class CreateEditReservationWebServiceCall implements WebServiceCall {
	private final String body;
	private final String method;
	private final String path;

	public CreateEditReservationWebServiceCall(CharSequence partyName,
                                               CharSequence phoneNumber, int numberInParty, Date date,
                                               CharSequence notes, EpicuriCustomer customer, boolean submit, boolean omitFromChecks){
		method = "POST";
		path = submit ? "/Reservation" : "/Reservation/ReservationCheck";
		body = setupBody("-1", partyName, phoneNumber, numberInParty, date, notes, customer, omitFromChecks,null);
	}

	public CreateEditReservationWebServiceCall(CharSequence partyName,
											   CharSequence phoneNumber, int numberInParty, Date date,
											   CharSequence notes, EpicuriCustomer customer, boolean submit, boolean omitFromChecks, CharSequence emailAddress){
		method = "POST";
		path = submit ? "/Reservation" : "/Reservation/ReservationCheck";
		body = setupBody("-1", partyName, phoneNumber, numberInParty, date, notes, customer, omitFromChecks, emailAddress);
	}
	
	public CreateEditReservationWebServiceCall(String id, CharSequence partyName,
                                               CharSequence phoneNumber, int numberInParty, Date date,
                                               CharSequence notes, EpicuriCustomer customer, boolean submit, boolean omitFromChecks){

		method = submit ? "PUT" : "POST";
		path = submit ? String.format("/Reservation/%s", id) : "/Reservation/ReservationCheck";
		body = setupBody(id, partyName, phoneNumber, numberInParty, date, notes, customer, omitFromChecks,null);
	}
	
	private String setupBody(String id, CharSequence partyName,
                             CharSequence phoneNumber, int numberInParty, Date date,
                             CharSequence notes, EpicuriCustomer customer, boolean omitFromChecks, CharSequence emailAddress){
		JSONObject o = new JSONObject();
		try {
			if(id != null && !id.equals("0") & !id.equals("-1")){
				o.put("Id", id);
			}
			o.put("Name", partyName.toString());
			o.put("NumberOfPeople", numberInParty);
			o.put("Notes", notes.toString());
			o.put("Telephone", phoneNumber.toString());
			o.put("ReservationTime", date.getTime() / 1000);
			o.put("omitFromChecks", omitFromChecks);
			if(emailAddress != null) {
				o.put("Email", emailAddress);
			}
			if(null != customer){
				o.put("LeadCustomerId", customer.getId());
			}
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		return o.toString();
	}
	
	public CreateEditReservationWebServiceCall(EpicuriReservation res){
		JSONObject o = new JSONObject();
		try {
			o.put("Name", res.getName());
			o.put("NumberOfPeople", res.getNumberInParty());
			o.put("Notes", res.getNotes());
			o.put("Telephone", res.getPhoneNumber());
			o.put("ReservationTime", (int)(res.getStartDate().getTime() / 1000));
			// cannot edit customer for reservation once saved
//			if(res.getEpicuriUser() != null){
//				o.put("LeadCustomerId", res.getEpicuriUser().getId());
//			}
		} catch (JSONException e){
			e.printStackTrace();
			throw new RuntimeException("cannot continue");
		}
		body = o.toString();
		if(res.getId() != null && !res.getId().equals("0") && !res.getId().equals("-1")){
			path = String.format("/Reservation/%s", res.getId());
			method = "PUT";
		} else {
			path = "/Reservation";
			method = "POST";
		}
	}

	@Override
	public String getMethod() {
		return method;
	}
	
	@Override
	public boolean requiresToken() {
		return true;
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
		return new Uri[]{EpicuriContent.RESERVATIONS_URI, EpicuriContent.PARTIES_URI};
	}
}
