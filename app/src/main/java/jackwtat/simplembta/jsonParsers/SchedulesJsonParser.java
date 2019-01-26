package jackwtat.simplembta.jsonParsers;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.Bus;
import jackwtat.simplembta.model.routes.CommuterRail;
import jackwtat.simplembta.model.routes.Ferry;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.routes.SilverLine;
import jackwtat.simplembta.utilities.DateUtil;

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

                    schedule.setStopSequence(jAttributes.getInt("stop_sequence"));
                    schedule.setArrivalTime(DateUtil.parse(jAttributes.getString("arrival_time")));
                    schedule.setDepartureTime(DateUtil.parse(jAttributes.getString("departure_time")));
                    schedule.setIsLive(false);
                    schedule.setPickUpType(jAttributes.getInt("pickup_type"));

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
                        // Get stop attributes
                        JSONObject jStopAttr = jStop.getJSONObject("attributes");

                        // Get stop name
                        stop.setName(jStopAttr.getString("name"));

                        // Get stop location
                        Location location = new Location("");
                        location.setLatitude(jStopAttr.getDouble("latitude"));
                        location.setLongitude(jStopAttr.getDouble("longitude"));
                        stop.setLocation(location);

                        // Get the parent stop id
                        try {
                            stop.setParentId(jStop.getJSONObject("relationships")
                                    .getJSONObject("parent_station")
                                    .getJSONObject("data")
                                    .getString("id"));
                        } catch (JSONException e) {
                            stop.setParentId("");
                        }

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
                        // Get route attributes
                        JSONObject jRouteAttr = jRoute.getJSONObject("attributes");

                        int mode = jRouteAttr.getInt("type");
                        int sortOrder = (jRouteAttr.getInt("sort_order"));
                        String shortName = (jRouteAttr.getString("short_name"));
                        String longName = (jRouteAttr.getString("long_name"));
                        String primaryColor = (jRouteAttr.getString("color"));
                        String textColor = (jRouteAttr.getString("text_color"));

                        if (mode == Route.BUS) {
                            if (SilverLine.isSilverLine(routeId)) {
                                route = new SilverLine(routeId);
                            } else {
                                route = new Bus(routeId);
                            }
                        } else if (mode == Route.HEAVY_RAIL) {
                            if (BlueLine.isBlueLine(routeId)) {
                                route = new BlueLine(routeId);
                            } else if (OrangeLine.isOrangeLine(routeId)) {
                                route = new OrangeLine(routeId);
                            } else if (RedLine.isRedLine(routeId)) {
                                route = new RedLine(routeId);
                            }
                        } else if (mode == Route.LIGHT_RAIL) {
                            if (GreenLine.isGreenLine(routeId)) {
                                route = new GreenLine(routeId);
                            } else if (RedLine.isRedLine(routeId)) {
                                route = new RedLine(routeId);
                            }
                        } else if (mode == Route.COMMUTER_RAIL) {
                            route = new CommuterRail(routeId);
                        } else if (mode == Route.FERRY) {
                            route = new Ferry(routeId);
                        }

                        route.setMode(jRouteAttr.getInt("type"));
                        route.setSortOrder(sortOrder);
                        route.setShortName(shortName);
                        route.setLongName(longName);
                        route.setPrimaryColor(primaryColor);
                        route.setTextColor(textColor);

                        JSONArray jDirectionNames = jRouteAttr.getJSONArray("direction_names");
                        for (int j = 0; j < jDirectionNames.length(); j++) {
                            route.setDirection(new Direction(j, jDirectionNames.getString(j)));

                        }
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
                        // Get trip attributes
                        JSONObject jTripAttr = jTrip.getJSONObject("attributes");

                        schedule.setDirection(jTripAttr.getInt("direction_id"));
                        schedule.setDestination(jTripAttr.getString("headsign"));
                        schedule.setTripName(jTripAttr.getString("name"));
                    }

                    if (jRelationships.getJSONObject("prediction").getString("data").equals("null") &&
                            !schedule.getDestination().equals("null")) {
                        // If we don't already have a prediction with the same ID
                        // or if the existing prediction is for the child route of this route,
                        // then add this prediction
                        if (!schedules.containsKey(id) || Bus.isParentOf(
                                schedule.getRoute().getId(),
                                schedules.get(id).getRouteId())) {
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
