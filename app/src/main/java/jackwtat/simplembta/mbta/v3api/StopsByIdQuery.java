package jackwtat.simplembta.mbta.v3api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.mbta.structure.Stop;

/**
 * Created by jackw on 3/2/2018.
 */

public class StopsByIdQuery extends Query {
    private static final String LOG_TAG = "StopsByIdQuery";

    public StopsByIdQuery(String apiKey) {
        super(apiKey);
    }

    public HashMap<String, Stop> get(List<String> stopIds) {
        StringBuilder arg = new StringBuilder();
        for (int i = 0; i < stopIds.size(); i++) {
            arg.append(stopIds.get(i));
            if (i < stopIds.size() - 1) {
                arg.append(",");
            }
        }

        String[] params = {"filter[id]=" + arg.toString()};

        String jsonResponse = super.get("stops", params);

        HashMap<String, Stop> stops = new HashMap<>();

        if (TextUtils.isEmpty(jsonResponse)) {
            return stops;
        }

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");
            for (int i = 0; i < jData.length(); i++) {
                JSONObject jStop = jData.getJSONObject(i);

                JSONObject jAttributes = jStop.getJSONObject("attributes");

                String id = jStop.getString("id");
                String name = jAttributes.getString("name");
                Double longitude = jAttributes.getDouble("longitude");
                Double latitude = jAttributes.getDouble("latitude");
                String parentStopId = jStop.getJSONObject("relationships")
                        .getJSONObject("parent_station").getString("data");

                stops.put(id, new Stop(id, name, parentStopId, latitude, longitude));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse JSON response for Stops");
            e.printStackTrace();
        }

        return stops;
    }
}
