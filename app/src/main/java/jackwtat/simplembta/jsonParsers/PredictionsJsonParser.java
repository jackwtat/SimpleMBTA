package jackwtat.simplembta.jsonParsers;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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
                            String[] directionNames = new String[jDirectionNames.length()];
                            for (int j = 0; j < jDirectionNames.length(); j++) {
                                directionNames[j] = jDirectionNames.getString(j);
                            }
                            route.setDirectionNames(directionNames);
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

                        // If we don't already have a prediction with the same ID
                        // or if the existing prediction is for the child route of this route,
                        // then add this prediction
                        if (!predictions.containsKey(id) || Bus.isParentOf(
                                prediction.getRoute().getId(),
                                predictions.get(id).getRouteId())) {
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
