package jackwtat.simplembta.jsonParsers;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Trip;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.Bus;
import jackwtat.simplembta.model.routes.CommuterRail;
import jackwtat.simplembta.model.routes.Ferry;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.routes.SilverLine;

public class TripsJsonParser {
    public static final String LOG_TAG = "RoutesJsonParser";

    public static Trip[] parse(String jsonResponse) {

        if (TextUtils.isEmpty(jsonResponse)) {
            return new Trip[0];
        }

        ArrayList<Trip> trips = new ArrayList<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            // Get array related data, organized in a HashMap for easy searching by ID
            HashMap<String, JSONObject> includedData;
            if (jRoot.has("included")) {
                includedData = jsonArrayToHashMap(jRoot.getJSONArray("included"));
            } else {
                includedData = new HashMap<>();
            }

            // Get array of trip data
            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jTrip = jData.getJSONObject(i);

                    // Get trip ID
                    String id = jTrip.getString("id");

                    // Create new instance of trip
                    Trip trip = new Trip(id);

                    // Get trip attributes
                    JSONObject jAttributes = jTrip.getJSONObject("attributes");

                    trip.setDirection(jAttributes.getInt("direction_id"));
                    trip.setDestination(jAttributes.getString("headsign"));
                    trip.setName(jAttributes.getString("name"));

                    // Get IDs of related objects
                    JSONObject jRelationships = jTrip.getJSONObject("relationships");

                    // Retrieve route data
                    try {
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

                        trip.setRoute(route);

                    } catch (JSONException e) {
                        Log.i(LOG_TAG, "No route associated with trip " + id);
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
                            JSONObject jVehicleAttr = jVehicle.getJSONObject("attributes");

                            vehicle.setLabel(jVehicleAttr.getString("label"));

                            double latitude = jVehicleAttr.getDouble("latitude");
                            double longitude = jVehicleAttr.getDouble("longitude");
                            float bearing = (float) jVehicleAttr.getDouble("bearing");

                            Location location = new Location("");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            location.setBearing(bearing);
                            vehicle.setLocation(location);
                        }

                        trip.setVehicle(vehicle);

                    } catch (JSONException e) {
                        Log.i(LOG_TAG, "No vehicle associated with trip " + id);
                    }

                    // Retrieve shape data
                    try {
                        String shapeId = jRelationships
                                .getJSONObject("shape")
                                .getJSONObject("data")
                                .getString("id");
                        Shape shape = new Shape(shapeId);

                        JSONObject jShape = includedData.get("shape" + shapeId);
                        if (jShape != null) {
                            JSONObject jShapeAttr = jShape.getJSONObject("attributes");

                            shape.setPolyline(jShapeAttr.getString("polyline"));
                            shape.setPriority(jShapeAttr.getInt("priority"));
                            shape.setDirection(jShapeAttr.getInt("direction_id"));
                        }

                        trip.setShape(shape);

                    } catch (JSONException e) {
                        Log.i(LOG_TAG, "No shape associated with trip " + id);
                        trip.setShape(new Shape("null"));
                    }

                    // Retrieve stops data
                    try {
                        if (trip.getShape() != null) {
                            JSONArray jStops = jRelationships
                                    .getJSONObject("stops")
                                    .getJSONArray("data");

                            Stop[] stops = new Stop[jStops.length()];
                            for (int j = 0; j < jStops.length(); j++) {
                                String stopId = jStops.getJSONObject(j).getString("id");
                                Stop stop = new Stop(stopId);

                                JSONObject jStop = includedData.get("stop" + stopId);
                                if (jStop != null) {
                                    JSONObject jStopAttr = jStop.getJSONObject("attributes");

                                    stop.setName(jStopAttr.getString("name"));

                                    Location location = new Location("");
                                    location.setLatitude(jStopAttr.getDouble("latitude"));
                                    location.setLongitude(jStopAttr.getDouble("longitude"));
                                    stop.setLocation(location);
                                }

                                stops[j] = stop;
                            }

                            trip.getShape().setStops(stops);
                        }

                    } catch (JSONException e) {
                        Log.i(LOG_TAG, "No stops associated with trip " + id);
                    }

                    trips.add(trip);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Trip at position " + i);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Trips JSON response");
        }

        return trips.toArray(new Trip[0]);
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
