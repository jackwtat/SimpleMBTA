package jackwtat.simplembta.utilities;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jackwtat.simplembta.model.Vehicle;

public class VehiclesJsonParser {
    public final static String LOG_TAG = "VehiclesJsonParser";

    public static Vehicle[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Vehicle[0];
        }

        ArrayList<Vehicle> vehicles = new ArrayList<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                JSONObject jVehicle = jData.getJSONObject(i);
                String id = jVehicle.getString("id");

                Vehicle vehicle = new Vehicle(id);

                // Get route
                JSONObject jRelationships = jVehicle.getJSONObject("relationships");
                String routeId;
                try {
                    routeId = jRelationships
                            .getJSONObject("route")
                            .getJSONObject("data")
                            .getString("id");
                } catch (JSONException e) {
                    routeId = "";
                }
                vehicle.setRoute(routeId);

                // Get vehicle attributes
                JSONObject jAttributes = jVehicle.getJSONObject("attributes");
                vehicle.setLabel(jAttributes.getString("label"));
                vehicle.setDirection(jAttributes.getInt("direction_id"));

                // Get vehicle location and bearing
                Location location = new Location("");
                location.setLatitude(jAttributes.getDouble("latitude"));
                location.setLongitude(jAttributes.getDouble("longitude"));
                location.setBearing((float) jAttributes.getDouble("bearing"));
                vehicle.setLocation(location);

                vehicles.add(vehicle);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse vehicles from JSON");
        }

        return vehicles.toArray(new Vehicle[vehicles.size()]);
    }
}
