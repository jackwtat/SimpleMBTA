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

public class PredictionsJsonParser {
    public static final String LOG_TAG = "PredictionsJsonParser";

    public static Prediction[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Prediction[0];
        }

        Prediction[] predictions;

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

            predictions = new Prediction[jData.length()];

            for (int i = 0; i < predictions.length; i++) {
                try {
                    JSONObject jPrediction = jData.getJSONObject(i);

                    // Get prediction ID
                    String id = jPrediction.getString("id");

                    // Create new instance of Prediction
                    Prediction prediction = new Prediction(id);

                    // Get prediction attributes
                    JSONObject jAttributes = jPrediction.getJSONObject("attributes");
                    Date departureTime = parseMbtaDate(jAttributes.getString("departure_time"));
                    Date arrivalTime = parseMbtaDate(jAttributes.getString("arrival_time"));

                    prediction.setArrivalTime(arrivalTime);
                    prediction.setDepartureTime(departureTime);

                    // Get IDs of related objects
                    JSONObject jRelationships = jPrediction.getJSONObject("relationships");

                    // Retrieve route data
                    String routeId = jRelationships
                            .getJSONObject("route")
                            .getJSONObject("data")
                            .getString("id");
                    prediction.setRouteId(routeId);

                    // Retrieve stop data
                    String stopId = jRelationships
                            .getJSONObject("stop")
                            .getJSONObject("data")
                            .getString("id");
                    prediction.setStopId(stopId);

                    // Retrieve trip data
                    String tripId = jRelationships
                            .getJSONObject("trip")
                            .getJSONObject("data")
                            .getString("id");
                    prediction.setTripId(tripId);

                    JSONObject jTrip = includedData.get("trip" + tripId);
                    if (jTrip != null) {
                        JSONObject jTripAttr = jTrip.getJSONObject("attributes");

                        int direction = jTripAttr.getInt("direction_id");
                        String destination = jTripAttr.getString("headsign");
                        String tripName = jTripAttr.getString("name");

                        prediction.setDirection(direction);
                        prediction.setDestination(destination);
                        prediction.setTripName(tripName);
                    }

                    predictions[i] = prediction;

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Stop at position " + i);

                    predictions[i] = null;
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Predictions JSON response");

            predictions = new Prediction[0];
        }


        return predictions;
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
