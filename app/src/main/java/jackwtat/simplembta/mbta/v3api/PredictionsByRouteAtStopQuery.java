package jackwtat.simplembta.mbta.v3api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Route;
import jackwtat.simplembta.mbta.structure.Trip;

/**
 * Created by jackw on 3/19/2018.
 */

public class PredictionsByRouteAtStopQuery extends Query {
    private static final String LOG_TAG = "PredByStopQuery";

    public PredictionsByRouteAtStopQuery(String apiKey) {
        super(apiKey);
    }

    public ArrayList<Prediction> get(String routeId, int direction, String stopId, String stopName) {
        ArrayList<Prediction> predictions = new ArrayList<>();
        HashMap<String, Trip> trips = new HashMap<>();
        Route route = new Route(routeId);

        HashMap<String, String> params = new HashMap<>();

        params.put("filter[route]", routeId);
        params.put("filter[stop]", stopId);
        params.put("filter[direction]", Integer.toString(direction));
        params.put("include", "route,trip");

        String jsonResponse = super.get("predictions", params);

        if (TextUtils.isEmpty(jsonResponse)) {
            return predictions;
        }

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            // Parse linked data (Stop, Route, and Trip)
            if (jRoot.has("included")) {
                JSONArray jLinked = jRoot.getJSONArray("included");

                // Loop through all the linked objects
                for (int i = 0; i < jLinked.length(); i++) {
                    JSONObject jLinkedObj = jLinked.getJSONObject(i);

                    // Parse linked object
                    try {
                        String id = jLinkedObj.getString("id");
                        String type = jLinkedObj.getString("type");
                        JSONObject jAttributes = jLinkedObj.getJSONObject("attributes");

                        if (type.equals("route")) {
                            Mode mode = Mode.getModeFromType(jAttributes.getInt("type"));
                            String shortName = jAttributes.getString("short_name");
                            String longName = jAttributes.getString("long_name");
                            String color = "#" + jAttributes.getString("color");
                            String textColor = "#" + jAttributes.getString("text_color");
                            int sortOrder = jAttributes.getInt("sort_order");

                            route = new Route(
                                    id, mode, shortName, longName, color, textColor, sortOrder);

                        } else if (type.equals("trip")) {
                            String destination = jAttributes.getString("headsign");
                            String name = jAttributes.getString("name");

                            trips.put(id, new Trip(id, direction, destination, name));

                        } else {
                            Log.e(LOG_TAG, "Unknown linked object type:\n" +
                                    jLinkedObj.toString());
                        }

                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to parse linked object:\n" +
                                jLinkedObj.toString());
                        e.printStackTrace();
                    }
                }
            } else {
                Log.i(LOG_TAG, "No linked Stop, Route, or Trip data returned");
            }

            // Add service alerts to their respective routes
            route.addServiceAlerts(new AlertsByRoutesQuery(apiKey).get(routeId));

            // Parse Prediction data
            if (jRoot.has("data")) {
                JSONArray jData = jRoot.getJSONArray("data");

                // Loop through all Predictions
                for (int i = 0; i < jData.length(); i++) {
                    JSONObject jPrediction = jData.getJSONObject(i);

                    // Parse Prediction object
                    try {
                        String id = jPrediction.getString("id");

                        JSONObject jAttributes = jPrediction.getJSONObject("attributes");

                        String departure = jAttributes.getString("departure_time");
                        String arrival = jAttributes.getString("arrival_time");

                        if (!departure.equals("null") ||
                                (departure.equals("null") && arrival.equals("null"))) {

                            JSONObject jRelatedData = jPrediction.getJSONObject("relationships");

                            Trip relatedTrip = trips.get(jRelatedData
                                    .getJSONObject("trip")
                                    .getJSONObject("data")
                                    .getString("id"));

                            if (relatedTrip != null) {
                                Date deptTime = Query.parseDate(departure);

                                predictions.add(new Prediction(
                                        id,
                                        stopId,
                                        stopName,
                                        route,
                                        relatedTrip,
                                        deptTime));
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to parse Prediction:\n" +
                                jPrediction.toString());
                    }
                }
            } else {
                Log.i(LOG_TAG, "No Prediction data returned");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse JSON response for Predictions");
            e.printStackTrace();
        }

        return predictions;
    }
}
