package jackwtat.simplembta.jsonParsers;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jackwtat.simplembta.model.Stop;

public class StopsJsonParser {
    public static final String LOG_TAG = "StopsJsonParser";

    public static Stop[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Stop[0];
        }

        ArrayList<Stop> stops = new ArrayList<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jStop = jData.getJSONObject(i);

                    JSONObject jRelationships = jStop.getJSONObject("relationships");

                    String parentId;
                    try {
                        parentId = jRelationships
                                .getJSONObject("parent_station")
                                .getJSONObject("data")
                                .getString("id");
                    } catch (JSONException e) {
                        parentId = "";
                    }

                    // To prevent redundant stops, only parse stops that are not child stops
                    if (parentId.equals("")) {

                        // Get stop ID
                        Stop stop = new Stop(jStop.getString("id"));

                        // Get child stop IDs
                        JSONArray jChildStops = jRelationships
                                .getJSONObject("child_stops")
                                .getJSONArray("data");

                        String[] childIds = new String[jChildStops.length()];

                        for (int j = 0; j < childIds.length; j++) {
                            childIds[j] = jChildStops.getJSONObject(j).getString("id");
                        }

                        stop.setChildIds(childIds);

                        // Get stop attributes
                        JSONObject jAttributes = jStop.getJSONObject("attributes");

                        // Get stop name
                        stop.setName(jAttributes.getString("name"));

                        // Get stop location
                        Location location = new Location("");
                        location.setLatitude(jAttributes.getDouble("latitude"));
                        location.setLongitude(jAttributes.getDouble("longitude"));
                        stop.setLocation(location);

                        stops.add(stop);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Stop at position " + i);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Stops JSON response");
        }

        return stops.toArray(new Stop[stops.size()]);
    }
}
