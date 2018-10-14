package jackwtat.simplembta.utilities;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;

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

                    try {

                        // Create new instance of Prediction
                        Prediction prediction = new Prediction(id);

                        // Get prediction attributes
                        JSONObject jAttributes = jPrediction.getJSONObject("attributes");

                        prediction.setArrivalTime(DateUtil.parse(jAttributes.getString("arrival_time")));
                        prediction.setDepartureTime(DateUtil.parse(jAttributes.getString("departure_time")));
                        prediction.setIsLive(true);

                        // Get IDs of related objects
                        JSONObject jRelationships = jPrediction.getJSONObject("relationships");

                        // Retrieve stop data
                        String stopId = jRelationships
                                .getJSONObject("stop")
                                .getJSONObject("data")
                                .getString("id");
                        Stop stop = new Stop(stopId);

                        JSONObject jStop = includedData.get("stop" + stopId);
                        if (jStop != null) {
                            // Get stop attributes
                            JSONObject jStopAttr = jStop.getJSONObject("attributes");

                            // Get stop name
                            stop.setName(jStopAttr.getString("name"));

                            // Get stop location
                            Location location = new Location("");
                            location.setLatitude(jStopAttr.getDouble("latitude"));
                            location.setLongitude(jStopAttr.getDouble("longitude"));
                            stop.setLocation(location);
                        }
                        prediction.setStop(stop);

                        try {
                            // Retrieve schedule data
                            String scheduleId = jRelationships
                                    .getJSONObject("schedule")
                                    .getJSONObject("data")
                                    .getString("id");

                            JSONObject jSchedule = includedData.get("schedule" + scheduleId);
                            if (jSchedule != null) {
                                JSONObject jScheduleAttr = jSchedule.getJSONObject("attributes");

                                prediction.setId(scheduleId);
                                prediction.setPickUpType(jScheduleAttr.getInt("pickup_type"));
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Unable to parse schedule for prediction " + id);
                        }

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

                        if (!predictions.containsKey(id) ||
                                prediction.getRoute().isParentOf(predictions.get(id).getRouteId())) {
                            predictions.put(id, prediction);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to parse Prediction " + id);
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Prediction at position " + i);
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
}
