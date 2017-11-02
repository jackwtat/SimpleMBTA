package jackwtat.simplembta;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jackwtat.simplembta.data.ServiceAlert;
import jackwtat.simplembta.data.Trip;
import jackwtat.simplembta.data.Route;

/**
 * Created by jackw on 9/1/2017.
 */

public class QueryUtil {
    private static final String LOG_TAG = "Query Util";

    //URL for querying the MBTA realTime API
    private static final String MBTA_URL = "http://realtime.mbta.com/developer/api/v2/";

    //Format specification
    private static final String RESPONSE_FORMAT = "&format=json";

    private QueryUtil() {
    }

    public static ArrayList<Trip> fetchPredictionsByStop(String apiKey, String stopId) {
        String requestUrl = MBTA_URL + "predictionsbystop" + "?api_key=" + apiKey + RESPONSE_FORMAT +
                "&stop=" + stopId;

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        return extractPredictionsFromJson(jsonResponse);
    }

    public static HashMap<String, ArrayList<ServiceAlert>> fetchAlerts(String apiKey) {
        String requestUrl = MBTA_URL + "alerts" + "?api_key=" + apiKey + RESPONSE_FORMAT;

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        return extractAlertsFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static ArrayList<Trip> extractPredictionsFromJson(String jsonResponse) {
        ArrayList<Trip> predictions = new ArrayList<>();

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return predictions;
        }

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject jStop = new JSONObject(jsonResponse);

            // Loop through each mode of transportation at this stop
            JSONArray jModes = jStop.getJSONArray("mode");
            for (int i = 0; i < jModes.length(); i++) {
                JSONObject jMode = jModes.getJSONObject(i);

                // Loop through all routes of this mode at this stop
                JSONArray routes = jMode.getJSONArray("route");
                for (int j = 0; j < routes.length(); j++) {
                    JSONObject jRoute = routes.getJSONObject(j);

                    // Loop through all the directions the current route takes from this stop
                    JSONArray jDirections = jRoute.getJSONArray("direction");
                    for (int k = 0; k < jDirections.length(); k++) {
                        JSONObject jDirection = jDirections.getJSONObject(k);

                        // Loop through all trips in this direction
                        JSONArray jTrips = jDirection.getJSONArray("trip");
                        for (int m = 0; m < jTrips.length(); m++) {
                            JSONObject jTrip = jTrips.getJSONObject(m);

                            // Add trip to the predictions list
                            predictions.add(new Trip(
                                    jTrip.getString("trip_id"),
                                    new Route(jRoute.getString("route_id"),
                                            jRoute.getString("route_name"),
                                            jMode.getInt("route_type")),
                                    jDirection.getInt("direction_id"),
                                    jTrip.getString("trip_headsign"),
                                    jStop.getString("stop_id"),
                                    jStop.getString("stop_name"),
                                    jTrip.getLong("pre_away")
                            ));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse prediction");
        }

        // Return the list of predictions
        return predictions;
    }

    private static HashMap<String, ArrayList<ServiceAlert>> extractAlertsFromJson(String jsonResponse) {

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
                        jAlert.getString("alert_lifecycle"));

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
                                alerts.get(routeId).add(0,serviceAlert);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse alert");
        }

        // Return the HashMap of Alerts
        return alerts;
    }
}
