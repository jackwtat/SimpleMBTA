package jackwtat.simplembta.mbta.api;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jackwtat.simplembta.mbta.data.ServiceAlert;

/**
 * Created by jackw on 11/28/2017.
 */

public class ServiceAlertsQuery extends RestApiGetQuery {
    private final static String LOG_TAG = "ServiceAlertsQuery";

    public ServiceAlertsQuery(RealTimeApi api) {
        super(api, "alerts");
    }

    public HashMap<String, ArrayList<ServiceAlert>> get() {
        String jsonResponse = super.get(new HashMap<String, String>());

        // A route may contain multiple alerts, so we use ArrayList<ServiceAlert> to store multiple
        // alerts for each route
        HashMap<String, ArrayList<ServiceAlert>> alerts = new HashMap<>();

        // Current time in seconds
        long currentTime = (new Date().getTime()) / 1000;

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return alerts;
        }

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            // Loop through each alert
            JSONArray jAlertsArray = jRoot.getJSONArray("alerts");
            for (int i = 0; i < jAlertsArray.length(); i++) {
                JSONObject jAlert = jAlertsArray.getJSONObject(i);

                // Create a new ServiceAlert object
                // Pass in alert_id and header_text
                // header_text is sufficient and brief-enough information
                ServiceAlert serviceAlert = new ServiceAlert(
                        jAlert.getString("alert_id"),
                        jAlert.getString("header_text"),
                        jAlert.getString("alert_lifecycle"),
                        ServiceAlert.Severity.valueOf(jAlert.getString("severity").toUpperCase()));

                // Loop through each effect period of this serviceAlert
                JSONArray jEffectPeriods = jAlert.getJSONArray("effect_periods");
                for (int j = 0; j < jEffectPeriods.length(); j++) {
                    JSONObject jPeriod = jEffectPeriods.getJSONObject(j);

                    // Get the start and end times of the serviceAlert
                    long start = jPeriod.getLong("effect_start");
                    long end;
                    try {
                        end = jPeriod.getLong("effect_end");
                    } catch (Exception e) {
                        end = -1;
                    }

                    // If the current time is between the start/end times, then add serviceAlert
                    if (currentTime > start && (currentTime < end || end == -1)) {
                        try {
                            // Loop through all the affected services
                            JSONArray jServices = jAlert.getJSONObject("affected_services")
                                    .getJSONArray("services");
                            for (int k = 0; k < jServices.length(); k++) {
                                JSONObject jService = jServices.getJSONObject(k);

                                // Get the route ID
                                String routeId = jService.getString("route_id");

                                // Check if we already have a key routeId in hash map
                                // If yes, then add the serviceAlert to the existing ArrayList in its value
                                // If not, then create a new key-value pair for this route
                                // Reminder: key = routeID, value = ArrayList<ServiceAlert>
                                if (!alerts.containsKey(routeId)) {
                                    alerts.put(routeId, new ArrayList<ServiceAlert>());
                                }

                                // Check if this route already has an instance of this serviceAlert
                                // If not, then add to list of alerts
                                if (!alerts.get(routeId).contains(serviceAlert)) {
                                    alerts.get(routeId).add(serviceAlert);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "No route associated with alert");
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse alert");
            e.printStackTrace();
        }

        // Return the HashMap of Alerts
        return alerts;
    }
}
