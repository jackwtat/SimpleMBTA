package jackwtat.simplembta.jsonParsers;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.utilities.DateUtil;

public class ServiceAlertsJsonParser {
    public static final String LOG_TAG = "ServiceAlertsJsonParser";

    public static ServiceAlert[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new ServiceAlert[0];
        }

        ArrayList<ServiceAlert> alerts = new ArrayList<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jAlert = jData.getJSONObject(i);

                    String id = jAlert.getString("id");

                    ServiceAlert alert = new ServiceAlert(id);

                    JSONObject jAttributes = jAlert.getJSONObject("attributes");
                    String header = jAttributes.getString("header");
                    String description = jAttributes.getString("description");
                    String url = jAttributes.getString("url");
                    String effect = jAttributes.getString("effect");
                    int severity = jAttributes.getInt("severity");
                    String lifecycle = jAttributes.getString("lifecycle");

                    alert.setHeader(header);
                    alert.setDescription(description);
                    alert.setUrl(url);
                    alert.setEffect(effect);
                    alert.setSeverity(severity);
                    alert.setLifecycle(lifecycle);

                    JSONArray jRoutes = jAttributes.getJSONArray("informed_entity");
                    for (int j = 0; j < jRoutes.length(); j++) {
                        JSONObject jInformedEntity = jRoutes.getJSONObject(j);

                        if (jInformedEntity.has("route_type")) {
                            if (jInformedEntity.has("route")) {
                                alert.addAffectedRoute(
                                        jInformedEntity.getString("route"));
                            } else {
                                alert.addAffectedMode(jInformedEntity.getInt("route_type"));
                            }
                        }

                        // Add affected stops
                        if (jInformedEntity.has("stop")) {
                            alert.addAffectedStop(jInformedEntity.getString("stop"));
                        }

                        // Add affected facilities
                        if (jInformedEntity.has("facility")) {
                            alert.addAffectedFacility(jInformedEntity.getString("facility"));
                        }
                    }

                    JSONArray jActivePeriods = jAttributes.getJSONArray("active_period");
                    for (int j = 0; j < jActivePeriods.length(); j++) {
                        JSONObject jActiveTimes = jActivePeriods.getJSONObject(j);
                        String startTime = jActiveTimes.getString("start");
                        String endTime = jActiveTimes.getString("end");

                        alert.addActivePeriod(DateUtil.parseTime(startTime),
                                DateUtil.parseTime(endTime),
                                DateUtil.parseTimeZoneOffset(startTime));
                    }

                    alerts.add(alert);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse service alert at position " + i);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Alerts from JSON");
        }

        return alerts.toArray(new ServiceAlert[alerts.size()]);
    }
}