package uk.co.epicuri.waiter.loaders.templates;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.model.EpicuriReservation;

public class ReservationsLoaderTemplate implements LoadTemplate<ArrayList<EpicuriReservation>> {

    private final Uri uri;

    private static Uri generateUri(Date fromTime, Date toTime) {
        Uri.Builder buildUri = EpicuriContent.RESERVATIONS_URI.buildUpon();
        buildUri.appendQueryParameter("fromTime",
                String.valueOf((int) (fromTime.getTime() / 1000)));
        if (null != toTime) {
            buildUri.appendQueryParameter("toTime",
                    String.valueOf((int) (toTime.getTime() / 1000)));
        }
        return buildUri.build();
    }

    /**
     * loader for pending reservations
     */
    public ReservationsLoaderTemplate() {
        uri = EpicuriContent.RESERVATIONS_URI.buildUpon().appendQueryParameter(
                "pendingWaiterAction", "true").build();
        limitId = "-1";
    }

    /**
     * loader for reservations
     *
     * @param fromTime start search time
     * @param toTime   end search time (optional - null for +3 months)
     */
    public ReservationsLoaderTemplate(Date fromTime, Date toTime) {
        uri = generateUri(fromTime, toTime);
        limitId = "-1";
    }

    private final String limitId;

    /**
     * Get reservation
     *
     * @param reservationId - the id of the reservation to retrieve
     */
    public ReservationsLoaderTemplate(String reservationId) {
        uri = EpicuriContent.RESERVATIONS_URI.buildUpon().appendPath(reservationId).build();
        limitId = reservationId;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    /*s
     *  [
     *  {"Notes":"Wedding Anniversairy",
     *  "ReservationTime":1355849421.0,
     *  "Id":2,
     *  "NumberofPeople":2,
     *  "Name":"John Smith",
     *  "Created":1355849421.0},
     *
     *  {"Notes":"Wedding Anniversairy",
     *  "ReservationTime":1355850116.0,"Id":5,"NumberofPeople":2,"Name":"John Smith",
     *  "Created":1355850116.0}]

     */
    @Override
    public ArrayList<EpicuriReservation> parseJson(String jsonString) throws JSONException {
        JSONArray reservationsJson = new JSONArray(jsonString);

        ArrayList<EpicuriReservation> reservations = new ArrayList<EpicuriReservation>(
                reservationsJson.length());

        for (int i = 0; i < reservationsJson.length(); i++) {
            JSONObject reservationJson = reservationsJson.getJSONObject(i);

            EpicuriReservation r = new EpicuriReservation(reservationJson);
            if (limitId.equals("-1") || limitId.equals(r.getId())) {
                reservations.add(r);
            }

        }
        Collections.sort(reservations, new Comparator<EpicuriReservation>() {
            @Override
            public int compare(EpicuriReservation lhs, EpicuriReservation rhs) {
                if (lhs.getStartDate().before(rhs.getStartDate())) {
                    return -1;
                } else if (lhs.getStartDate().after(rhs.getStartDate())) {
                    return 1;
                } else {
                    return lhs.getName().compareTo(rhs.getName());
                }
            }
        });
        return reservations;
    }

}
