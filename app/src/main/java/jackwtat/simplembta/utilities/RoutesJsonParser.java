package jackwtat.simplembta.utilities;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jackwtat.simplembta.model.Route;

public class RoutesJsonParser {
    public static final String LOG_TAG = "RoutesJsonParser";

    public static HashMap<String, Route> parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new HashMap<>();
        }

        HashMap<String, Route> routes = new HashMap<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jRoute = jData.getJSONObject(i);

                    String id = jRoute.getString("id");

                    Route route = new Route(id);

                    JSONObject jAttributes = jRoute.getJSONObject("attributes");
                    int mode = jAttributes.getInt("type");
                    int sortOrder = jAttributes.getInt("sort_order");
                    String shortName = jAttributes.getString("short_name");
                    String longName = jAttributes.getString("long_name");
                    String primaryColor = jAttributes.getString("color");
                    String textColor = jAttributes.getString("text_color");

                    route.setMode(mode);
                    route.setSortOrder(sortOrder);
                    route.setShortName(shortName);
                    route.setLongName(longName);
                    route.setPrimaryColor(primaryColor);
                    route.setTextColor(textColor);

                    routes.put(id, route);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Route at position " + i);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Routes JSON response");
        }

        return routes;
    }
}
