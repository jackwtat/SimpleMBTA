package jackwtat.simplembta.mbta.api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jackw on 12/7/2017.
 */

public class RoutesByStopQuery extends RestApiGetQuery {
    private static final String LOG_TAG = "RoutesByStopQuery";

    public RoutesByStopQuery(RealTimeApi api) {
        super(api, "routesbystop");
    }

    public ArrayList<String> get(String stopId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("stop", stopId);

        String jsonResponse = super.get(params);

        ArrayList<String> routes = new ArrayList<>();

        if (TextUtils.isEmpty(jsonResponse)) {
            return routes;
        }

        try {
            // Create a JSONObject from the JSON response string
            JSONObject jStop = new JSONObject(jsonResponse);

            // Loop through all the modes at this stop
            JSONArray jModes = jStop.getJSONArray("mode");
            for (int i = 0; i < jModes.length(); i++) {
                JSONObject jMode = jModes.getJSONObject(i);

                // Loop through all the routes for this mode
                JSONArray jRoutes = jMode.getJSONArray("route");
                for (int j = 0; j < jRoutes.length(); j++) {
                    JSONObject jRoute = jRoutes.getJSONObject(j);

                    // Add route ID to the routes list
                    routes.add(jRoute.getString("route_id"));
                }
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse routes for stop " + stopId);
            e.printStackTrace();
        }

        return routes;
    }
}
