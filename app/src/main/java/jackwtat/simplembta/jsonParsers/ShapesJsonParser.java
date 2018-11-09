package jackwtat.simplembta.jsonParsers;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;

public class ShapesJsonParser {
    public static final String LOG_TAG = "ShapesJsonParser";

    public static Shape[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Shape[0];
        }

        ArrayList<Shape> shapes = new ArrayList<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            HashMap<String, Stop> includedStops;
            if (jRoot.has("included")) {
                includedStops = includedStopsJsonToHashMap(jRoot.getJSONArray("included"));
            } else {
                includedStops = new HashMap<>();
            }

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jShape = jData.getJSONObject(i);
                    String id = jShape.getString("id");

                    Shape shape = new Shape(id);

                    JSONObject jAttributes = jShape.getJSONObject("attributes");
                    String polyline = jAttributes.getString("polyline");
                    int direction = jAttributes.getInt("direction_id");
                    int priority = jAttributes.getInt("priority");

                    shape.setPolyline(polyline);
                    shape.setDirection(direction);
                    shape.setPriority(priority);

                    JSONObject jRelationships = jShape.getJSONObject("relationships");
                    String routeId = jRelationships.getJSONObject("route")
                            .getJSONObject("data")
                            .getString("id");

                    shape.setRouteId(routeId);

                    JSONArray jStops = jRelationships.getJSONObject("stops").getJSONArray("data");
                    ArrayList<Stop> stops = new ArrayList<>();
                    for (int j = 0; j < jStops.length(); j++) {
                        String stopId = jStops.getJSONObject(j).getString("id");

                        if (includedStops.containsKey(stopId)) {
                            stops.add(includedStops.get(stopId));
                        }
                    }
                    shape.setStops(stops.toArray(new Stop[stops.size()]));

                    shapes.add(shape);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse shape at position " + i);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Shapes from JSON");
        }

        return shapes.toArray(new Shape[shapes.size()]);
    }

    private static HashMap<String, Stop> includedStopsJsonToHashMap(JSONArray jStops) {
        HashMap<String, Stop> stops = new HashMap<>();

        for (int i = 0; i < jStops.length(); i++) {
            try {
                JSONObject jStop = jStops.getJSONObject(i);
                String id = jStop.getString("id");

                Stop stop = new Stop(id);

                // Get stop attributes
                JSONObject jAttributes = jStop.getJSONObject("attributes");

                // Get stop name
                stop.setName(jAttributes.getString("name"));

                // Get stop location
                Location location = new Location("");
                location.setLatitude(jAttributes.getDouble("latitude"));
                location.setLongitude(jAttributes.getDouble("longitude"));
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

                stops.put(id, stop);

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse related stop at position " + i);
            }
        }

        return stops;
    }
}
