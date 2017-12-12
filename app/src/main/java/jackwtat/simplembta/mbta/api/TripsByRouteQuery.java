package jackwtat.simplembta.mbta.api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.mbta.dataNew.Direction;
import jackwtat.simplembta.mbta.dataNew.Prediction;
import jackwtat.simplembta.mbta.dataNew.Trip;

/**
 * Created by jackw on 12/11/2017.
 */

public class TripsByRouteQuery extends RestApiGetQuery {
    private static final String LOG_TAG = "TripsByRouteQuery";

    public TripsByRouteQuery(RestApiGettable api) {
        super(api, "predictionsbyroute");
    }

    public HashMap<Direction, List<Trip>> get(String routeId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("route", routeId);

        String jsonResponse = super.get(params);

        HashMap<Direction, List<Trip>> trips = new HashMap<>();

        if (TextUtils.isEmpty(jsonResponse)) {
            return trips;
        }

        try {
            JSONObject jRoute = new JSONObject(jsonResponse);

            // Loop through all directions of this route
            JSONArray jDirections = jRoute.getJSONArray("direction");
            for (int i = 0; i < jDirections.length(); i++) {
                JSONObject jDirection = jDirections.getJSONObject(i);

                Direction direction = new Direction(
                        jDirection.getString("direction_name"),
                        (jDirection.getInt("direction_id") + 1) % 2);

                // Initialize new trips list for this direction
                trips.put(direction, new ArrayList<Trip>());

                // Loop through all trips of this direction
                JSONArray jTrips = jDirection.getJSONArray("trip");
                for (int j = 0; j < jTrips.length(); j++) {
                    JSONObject jTrip = jTrips.getJSONObject(j);

                    // Create a new trip
                    Trip trip = new Trip(
                            jTrip.getString("trip_id"),
                            jTrip.getString("trip_headsign"));

                    // Loop through all the stops of this trip
                    JSONArray jStops = jTrip.getJSONArray("stop");
                    for (int k = 0; k < jStops.length(); k++) {
                        JSONObject jStop = jStops.getJSONObject(k);

                        // Create a new prediction
                        Prediction prediction = new Prediction(
                                jStop.getString("stop_id"),
                                jStop.getInt("stop_sequence"),
                                jStop.getLong("pre_away")
                        );

                        // Add this prediction to this trip
                        trip.addPrediction(prediction);
                    }

                    // Add this trip to trips list corresponding to this direction
                    trips.get(direction).add(trip);
                }
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse trips for route " + routeId);
            e.printStackTrace();
        }

        return trips;
    }
}
