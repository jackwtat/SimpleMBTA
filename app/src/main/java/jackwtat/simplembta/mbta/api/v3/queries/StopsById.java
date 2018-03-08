package jackwtat.simplembta.mbta.api.v3.queries;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.mbta.api.v3.RestApiGetQuery;
import jackwtat.simplembta.mbta.api.v3.V3RealTimeApi;
import jackwtat.simplembta.mbta.structures.Stop;

/**
 * Created by jackw on 3/2/2018.
 */

public class StopsById extends RestApiGetQuery {
    private static final String LOG_TAG = "StopsByIdQuery";

    public StopsById(V3RealTimeApi api) {
        super(api, "stops");
    }

    public HashMap<String, Stop> get(List<String> stopIds) {
        StringBuilder arg = new StringBuilder();
        for (String id : stopIds) {
            arg.append(id).append(",");
        }

        HashMap<String, String> params = new HashMap<>();
        params.put("filter[id]", arg.toString());

        String jsonResponse = super.get(params);

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
