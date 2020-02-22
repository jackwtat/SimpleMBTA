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
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.Bus;
import jackwtat.simplembta.model.routes.CommuterRail;
import jackwtat.simplembta.model.routes.Ferry;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.Route;
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

                        prediction.setStopSequence(jAttributes.getInt("stop_sequence"));
                        prediction.setArrivalTime(
                                DateUtil.parseTime(jAttributes.getString("arrival_time")));
                        prediction.setDepartureTime(
                                DateUtil.parseTime(jAttributes.getString("departure_time")));

                        if (!jAttributes.getString("arrival_time").equalsIgnoreCase("null")) {
                            prediction.setTimeZoneOffset(DateUtil.parseTimeZoneOffset(
                                    jAttributes.getString("arrival_time")));

                        } else if (!jAttributes.getString("departure_time").equalsIgnoreCase("null")) {
                            prediction.setTimeZoneOffset(DateUtil.parseTimeZoneOffset(
                                    jAttributes.getString("departure_time")));
                        }

                        prediction.setIsLive(true);

                        String status = jAttributes.getString("schedule_relationship");
                        if (status.equalsIgnoreCase("null")) {
                            prediction.setStatus(Prediction.SCHEDULED);

                        } else if (status.equalsIgnoreCase("ADDED")) {
                            prediction.setStatus(Prediction.ADDED);

                        } else if (status.equalsIgnoreCase("UNSCHEDULED")) {
                            prediction.setStatus(Prediction.UNSCHEDULED);

                        } else if (status.equalsIgnoreCase("NO_DATA")) {
                            prediction.setStatus(Prediction.NO_DATA);

                        } else if (status.equalsIgnoreCase("CANCELLED")) {
                            prediction.setStatus(Prediction.CANCELLED);

                        } else if (status.equalsIgnoreCase("SKIPPED")) {
                            prediction.setStatus(Prediction.SKIPPED);
                        }

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

                            // Get stop accessibility
                            stop.setAccessibility(jStopAttr.getInt("wheelchair_boarding"));

                            // Get the parent stop id
                            try {
                                stop.setParentId(jStop.getJSONObject("relationships")
                                        .getJSONObject("parent_station")
                                        .getJSONObject("data")
                                        .getString("id"));
                            } catch (JSONException e) {
                                stop.setParentId("");
                            }

                            // Get track number
                            prediction.setTrackNumber(jStopAttr.getString("platform_code"));

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

                                if (prediction.getPredictionTime() == null) {
                                    prediction.setArrivalTime(DateUtil.parseTime(
                                            jScheduleAttr.getString("arrival_time")));
                                    prediction.setDepartureTime(DateUtil.parseTime(
                                            jScheduleAttr.getString("departure_time")));

                                    if (!jScheduleAttr.getString("arrival_time").equalsIgnoreCase("null")) {
                                        prediction.setTimeZoneOffset(DateUtil.parseTimeZoneOffset(
                                                jScheduleAttr.getString("arrival_time")));

                                    } else if (!jScheduleAttr.getString("departure_time").equalsIgnoreCase("null")) {
                                        prediction.setTimeZoneOffset(DateUtil.parseTimeZoneOffset(
                                                jScheduleAttr.getString("departure_time")));
                                    }

                                    prediction.setIsLive(false);
                                }
                            }

                        } catch (JSONException e) {
                            Log.i(LOG_TAG, "Unable to get schedule ID for prediction " + id);
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
                            for (int j = 0; j < jDirectionNames.length(); j++) {
                                route.setDirection(new Direction(j, jDirectionNames.getString(j)));

                            }
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


                        // Retrieve vehicle data
                        try {
                            String vehicleId = jRelationships
                                    .getJSONObject("vehicle")
                                    .getJSONObject("data")
                                    .getString("id");

                            Vehicle vehicle = new Vehicle(vehicleId);

                            JSONObject jVehicle = includedData.get("vehicle" + vehicleId);
                            if (jVehicle != null) {
                                // Get vehicle attributes
                                JSONObject jVehicleAttr = jVehicle.getJSONObject("attributes");
                                vehicle.setLabel(jVehicleAttr.getString("label"));
                                vehicle.setDirection(jVehicleAttr.getInt("direction_id"));
                                vehicle.setCurrentStopSequence(jVehicleAttr.getInt("current_stop_sequence"));
                                vehicle.setCurrentStatus(jVehicleAttr.getString("current_status"));

                                // Get vehicle location and bearing
                                Location location = new Location("");
                                location.setLatitude(jVehicleAttr.getDouble("latitude"));
                                location.setLongitude(jVehicleAttr.getDouble("longitude"));
                                location.setBearing((float) jVehicleAttr.getDouble("bearing"));
                                vehicle.setLocation(location);

                                // Get vehicle trip
                                vehicle.setTripId(jVehicle
                                        .getJSONObject("relationships")
                                        .getJSONObject("trip")
                                        .getJSONObject("data")
                                        .getString("id"));

                                prediction.setVehicle(vehicle);
                            }

                            prediction.setVehicleId(vehicleId);

                        } catch (JSONException e) {
                            prediction.setVehicleId(null);
                        }

                        // If we don't already have a prediction with the same ID
                        // or if the existing prediction is for the child route of this route
                        // and the destination is not null,
                        // then add this prediction
                        if (!prediction.getDestination().equals("null") &&
                                prediction.getPredictionTime() != null) {
                            if (!predictions.containsKey(id) || Bus.isParentOf(
                                    prediction.getRoute().getId(),
                                    predictions.get(id).getRouteId())) {
                                predictions.put(id, prediction);
                            }
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
