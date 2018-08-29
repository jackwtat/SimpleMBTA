package jackwtat.simplembta.mbta.v3api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.clients.MbtaApiClient;
import jackwtat.simplembta.model.Mode;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.utilities.DateUtil;

/**
 * Created by jackw on 3/11/2018.
 */

public class AlertsByRoutesQuery extends MbtaApiClient {
    final private String LOG_TAG = "AlertsByRouteQuery";

    public AlertsByRoutesQuery(String apiKey) {
        super(apiKey);
    }

    public ArrayList<ServiceAlert> get(String routeId) {
        ArrayList<String> rte = new ArrayList<>();
        rte.add(routeId);
        return get(rte);
    }

    public ArrayList<ServiceAlert> get(List<String> routeIds) {
        StringBuilder arg = new StringBuilder();
        for (int i = 0; i < routeIds.size(); i++) {
            arg.append(routeIds.get(i));
            if (i < routeIds.size() - 1) {
                arg.append(",");
            }
        }

        String[] params = {"filter[route]=" + arg.toString()};

        String jsonResponse = super.get("alerts", params);

        ArrayList<ServiceAlert> alerts = new ArrayList<>();

        if (TextUtils.isEmpty(jsonResponse)) {
            return alerts;
        }

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                JSONObject jAlert = jData.getJSONObject(i);

                String id = jAlert.getString("id");

                JSONObject jAttributes = jAlert.getJSONObject("attributes");
                String header = jAttributes.getString("header");
                String effect = jAttributes.getString("effect");
                int severity = jAttributes.getInt("severity");
                String lifecycle = jAttributes.getString("lifecycle");

                ServiceAlert alert = new ServiceAlert(
                        id, header, effect, severity, lifecycle);

                JSONArray jRoutes = jAttributes.getJSONArray("informed_entity");

                for (int j = 0; j < jRoutes.length(); j++) {
                    JSONObject jAffectedRoute = jRoutes.getJSONObject(j);
                    if (jAffectedRoute.has("route_type")) {
                        if (jAffectedRoute.has("route")) {
                            alert.addAffectedRoute(
                                    jAffectedRoute.getString("route"));
                        } else {
                            alert.addBlanketMode(Mode.getModeFromType(
                                    jAffectedRoute.getInt("route_type")));
                        }
                    }
                }

                JSONArray jActivePeriods =
                        jAttributes.getJSONArray("active_period");

                for (int j = 0; j < jActivePeriods.length(); j++) {
                    JSONObject jActiveTimes = jActivePeriods.getJSONObject(j);
                    String startTime = jActiveTimes.getString("start");
                    String endTime = jActiveTimes.getString("end");

                    alert.addActivePeriod(
                            DateUtil.parse(startTime),
                            DateUtil.parse(endTime));
                }

                alerts.add(alert);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Alerts from JSON");
        }

        return alerts;
    }
}
