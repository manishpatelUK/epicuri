package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriParty;

public class HubWaitingListLoaderTemplate implements LoadTemplate<List<EpicuriParty>> {
	@Override
	public Uri getUri() {
		return EpicuriContent.PARTIES_URI;
	}

	@Override
	public List<EpicuriParty> parseJson(String jsonString) throws JSONException {
		JSONArray partiesJson = new JSONArray(jsonString);

		List<EpicuriParty> hubParties = new ArrayList<EpicuriParty>(partiesJson.length());

		for(int i=0; i<partiesJson.length(); i++){
			EpicuriParty party = new EpicuriParty(partiesJson.getJSONObject(i));
			hubParties.add(party);
		}
		
		Collections.sort(hubParties, new Comparator<EpicuriParty>() {

			@Override
			public int compare(EpicuriParty lhs, EpicuriParty rhs) {
				try {
					if (lhs.getReservationTime() != null && rhs.getReservationTime() != null) {
						return lhs.getReservationTime().compareTo(rhs.getReservationTime());
					} else if (lhs.getCreateTime() != null && rhs.getCreateTime() != null) {
						return lhs.getCreateTime().compareTo(rhs.getCreateTime());
					} else {
						return 0;
					}
				} catch(Exception ex) {
					return 0;
				}
			}
		});
		
		return hubParties;
	}
}
