package jackwtat.simplembta.utilities;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;

public class SchedulesJsonParser {
    public static final String LOG_TAG = "SchedulesJsonParser";

    public static Prediction[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Prediction[0];
        }

        HashMap<String, Prediction> schedules = new HashMap<>();

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
                    JSONObject jSchedule = jData.getJSONObject(i);

                    // Get schedule ID
                    String id = jSchedule.getString("id");

                    // Create new instance of Prediction
                    Prediction schedule = new Prediction(id);

                    // Get schedule attributes
                    JSONObject jAttributes = jSchedule.getJSONObject("attributes");

                    schedule.setArrivalTime(DateUtil.parse(jAttributes.getString("arrival_time")));
                    schedule.setDepartureTime(DateUtil.parse(jAttributes.getString("departure_time")));
                    schedule.setIsLive(false);

                    // Get IDs of related objects
                    JSONObject jRelationships = jSchedule.getJSONObject("relationships");

                    // Retrieve stop data
                    String stopId = jRelationships
                            .getJSONObject("stop")
                            .getJSONObject("data")
                            .getString("id");
                    Stop stop = new Stop(stopId);

                    JSONObject jStop = includedData.get("stop" + stopId);
                    if (jStop != null) {
                        JSONObject jStopAttr = jStop.getJSONObject("attributes");

                        stop.setName(jStopAttr.getString("name"));
                        stop.setLatitude(jStopAttr.getDouble("latitude"));
                        stop.setLongitude(jStopAttr.getDouble("longitude"));
                    }
                    schedule.setStop(stop);

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
                    schedule.setRoute(route);

                    // Retrieve trip data
                    String tripId = jRelationships
                            .getJSONObject("trip")
                            .getJSONObject("data")
                            .getString("id");
                    schedule.setTripId(tripId);

                    JSONObject jTrip = includedData.get("trip" + tripId);
                    if (jTrip != null) {
                        JSONObject jTripAttr = jTrip.getJSONObject("attributes");

                        schedule.setDirection(jTripAttr.getInt("direction_id"));
                        schedule.setDestination(jTripAttr.getString("headsign"));
                        schedule.setTripName(jTripAttr.getString("name"));
                    }

                    if (jRelationships.getJSONObject("prediction").getString("data").equals("null")) {
                        if (!schedules.containsKey(id)) {
                            schedules.put(id, schedule);
                        } else if (schedule.getRoute().isParentOf(schedules.get(id).getRouteId())) {
                            schedules.put(id, schedule);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Schedule at position " + i);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Schedules JSON response");
        }

        return schedules.values().toArray(new Prediction[schedules.size()]);
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
