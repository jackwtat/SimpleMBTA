package jackwtat.simplembta.utilities;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;

public class PredictionsJsonParser {
    public static final String LOG_TAG = "PredictionsJsonParser";

    public static Prediction[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Prediction[0];
        }

        HashMap<String, Prediction> predictions = new HashMap<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            // Get array related data, organized in a HashMap for easy searching by ID
            HashMap<String, JSONObject> includedData;
            if (jRoot.has("included")) {
                includedData = jsonArrayToHashMap(jRoot.getJSONArray("included"));
            } else {
                includedData = new HashMap<>();
            }

            // Get array of prediction data
            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jPrediction = jData.getJSONObject(i);

                    // Get prediction ID
                    String id = jPrediction.getString("id");

                    // Create new instance of Prediction
                    Prediction prediction = new Prediction(id);

                    // Get prediction attributes
                    JSONObject jAttributes = jPrediction.getJSONObject("attributes");

                    prediction.setArrivalTime(parseMbtaDate(jAttributes.getString("arrival_time")));
                    prediction.setDepartureTime(parseMbtaDate(jAttributes.getString("departure_time")));

                    // Get IDs of related objects
                    JSONObject jRelationships = jPrediction.getJSONObject("relationships");

                    // Retrieve stop data
                    String stopId = jRelationships
                            .getJSONObject("stop")
                            .getJSONObject("data")
                            .getString("id");
                    prediction.setStopId(stopId);

                    // Retrieve route data
                    String routeId = jRelationships
                            .getJSONObject("route")
                            .getJSONObject("data")
                            .getString("id");
                    Route route = new Route(routeId);

                    JSONObject jRoute = includedData.get("route" + routeId);
                    if (jRoute != null) {
                        JSONObject jRouteAttr = jRoute.getJSONObject("attributes");

                        route.setMode(jRouteAttr.getInt("type"));
                        route.setSortOrder(jRouteAttr.getInt("sort_order"));
                        route.setShortName(jRouteAttr.getString("short_name"));
                        route.setLongName(jRouteAttr.getString("long_name"));
                        route.setPrimaryColor(jRouteAttr.getString("color"));
                        route.setTextColor(jRouteAttr.getString("text_color"));
                    }
                    prediction.setRoute(route);

                    // Retrieve trip data
                    String tripId = jRelationships
                            .getJSONObject("trip")
                            .getJSONObject("data")
                            .getString("id");
                    prediction.setTripId(tripId);

                    JSONObject jTrip = includedData.get("trip" + tripId);
                    if (jTrip != null) {
                        JSONObject jTripAttr = jTrip.getJSONObject("attributes");

                        prediction.setDirection(jTripAttr.getInt("direction_id"));
                        prediction.setDestination(jTripAttr.getString("headsign"));
                        prediction.setTripName(jTripAttr.getString("name"));
                    }

                    if (!predictions.containsKey(id)) {
                        predictions.put(id, prediction);
                    } else if (prediction.getRoute().isParentOf(predictions.get(id).getRouteId())) {
                        predictions.put(id, prediction);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Stop at position " + i);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Predictions JSON response");
        }

        return predictions.values().toArray(new Prediction[predictions.size()]);
    }

    private static HashMap<String, JSONObject> jsonArrayToHashMap(JSONArray jRelated) {
        HashMap<String, JSONObject> data = new HashMap<>();

        for (int i = 0; i < jRelated.length(); i++) {
            try {
                JSONObject jObj = jRelated.getJSONObject(i);

                String id = jObj.getString("id");
                String type = jObj.getString("type").toLowerCase();
                String key = type + id;

                data.put(key, jObj);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse related data at position " + i);
            }
        }

        return data;
    }

    /**
     * Convert Date/Time from MBTA's string format to Java's Data object
     */
    private static Date parseMbtaDate(String date) {
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7)) - 1;
            int day = Integer.parseInt(date.substring(8, 10));
            int hour = Integer.parseInt(date.substring(11, 13));
            int minute = Integer.parseInt(date.substring(14, 16));
            int second = Integer.parseInt(date.substring(17, 19));

            return new GregorianCalendar(year, month, day, hour, minute, second).getTime();
        } catch (Exception e) {
            return null;
        }
    }
}
