package jackwtat.simplembta.mbta.api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jackwtat.simplembta.mbta.data.RouteDEPCRECATED;
import jackwtat.simplembta.mbta.data.Trip;

/**
 * Created by jackw on 11/28/2017.
 */

public class PredictionsByStopQuery extends RestApiGetQuery {
    private static final String LOG_TAG = "PredictionsByStopQuery";

    public PredictionsByStopQuery(RealTimeApi api) {
        super(api, "predictionsbystop");
    }

    public ArrayList<Trip> get(String stopId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("stop", stopId);

        String jsonResponse = super.get(params);

        ArrayList<Trip> predictions = new ArrayList<>();

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return predictions;
        }

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject jStop = new JSONObject(jsonResponse);

            // Loop through each mode of transportation at this stop
            JSONArray jModes = jStop.getJSONArray("mode");
            for (int i = 0; i < jModes.length(); i++) {
                JSONObject jMode = jModes.getJSONObject(i);

                // Loop through all routes of this mode at this stop
                JSONArray jRoutes = jMode.getJSONArray("route");
                for (int j = 0; j < jRoutes.length(); j++) {
                    JSONObject jRoute = jRoutes.getJSONObject(j);

                    // Loop through all the directions the current route takes from this stop
                    JSONArray jDirections = jRoute.getJSONArray("direction");
                    for (int k = 0; k < jDirections.length(); k++) {
                        JSONObject jDirection = jDirections.getJSONObject(k);

                        // Loop through all trips in this direction
                        JSONArray jTrips = jDirection.getJSONArray("trip");
                        for (int m = 0; m < jTrips.length(); m++) {
                            JSONObject jTrip = jTrips.getJSONObject(m);

                            // Add trip to the predictions list
                            predictions.add(new Trip(
                                    jTrip.getString("trip_id"),
                                    new RouteDEPCRECATED(jRoute.getString("route_id"),
                                            jRoute.getString("route_name"),
                                            jMode.getInt("route_type")),
                                    jDirection.getInt("direction_id"),
                                    jTrip.getString("trip_headsign"),
                                    jStop.getString("stop_id"),
                                    jStop.getString("stop_name"),
                                    jTrip.getLong("pre_away")
                            ));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse prediction:");
            e.printStackTrace();
        }

        // Return the list of predictions
        return predictions;
    }
}
