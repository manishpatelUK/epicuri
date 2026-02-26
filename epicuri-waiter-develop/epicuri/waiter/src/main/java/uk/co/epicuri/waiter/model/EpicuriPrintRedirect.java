package uk.co.epicuri.waiter.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pharris on 24/02/14.
 */
public class EpicuriPrintRedirect {
	String redirectId;
	EpicuriMenu.Printer sourcePrinter;
	EpicuriMenu.Printer destinationPrinter;

	public EpicuriPrintRedirect(JSONObject o) throws JSONException{
		redirectId = o.getString("Id");
		sourcePrinter = new EpicuriMenu.Printer(o.getJSONObject("From"));
		destinationPrinter = new EpicuriMenu.Printer(o.getJSONObject("To"));
	}

	public String getRedirectId() {
		return redirectId;
	}

	public EpicuriMenu.Printer getSourcePrinter() {
		return sourcePrinter;
	}

	public EpicuriMenu.Printer getDestinationPrinter() {
		return destinationPrinter;
	}
}
