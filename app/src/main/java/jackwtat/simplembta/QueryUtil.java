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

import jackwtat.simplembta.data.Alert;
import jackwtat.simplembta.data.Trip;
import jackwtat.simplembta.data.Route;

/**
 * Created by jackw on 9/1/2017.
 */

public class QueryUtil {
    private static final String LOG_TAG = "Query Util";

    //URL for querying the MBTA realTime API
    private static final String MBTA_URL = "http://realtime.mbta.com/developer/api/v2/";

    //API key
    private static final String API_KEY = "?api_key=vA6fQJjTJ0akhSwzr-Mf5A";

    //Format specification
    private static final String RESPONSE_FORMAT = "&format=json";

    private QueryUtil() {
    }

    public static ArrayList<Trip> fetchPredictionsByStop(String stopId) {
        String requestUrl = MBTA_URL + "predictionsbystop" + API_KEY + RESPONSE_FORMAT +
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

    public static HashMap<String, ArrayList<Alert>> fetchAlerts() {
        String requestUrl = MBTA_URL + "alerts" + API_KEY + RESPONSE_FORMAT;

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
            JSONObject stop = new JSONObject(jsonResponse);

            // Loop through each mode of transportation at this stop
            JSONArray modes = stop.getJSONArray("mode");
            for (int i = 0; i < modes.length(); i++) {
                JSONObject currentMode = modes.getJSONObject(i);

                // Loop through all routes of the current mode at this stop
                JSONArray routes = currentMode.getJSONArray("route");
                for (int j = 0; j < routes.length(); j++) {
                    JSONObject currentRoute = routes.getJSONObject(j);

                    // Loop through all the directions the current route takes from this stop
                    JSONArray directions = currentRoute.getJSONArray("direction");
                    for (int k = 0; k < directions.length(); k++) {
                        JSONObject currentDirection = directions.getJSONObject(k);

                        // Loop through all trips in this direction
                        JSONArray trips = currentDirection.getJSONArray("trip");
                        for (int m = 0; m < trips.length(); m++) {
                            JSONObject currentTrip = trips.getJSONObject(m);

                            // Create new Trip object and populate with data
                            Trip trip = new Trip(currentTrip.getString("trip_id"));
                            trip.setRoute(new Route(currentRoute.getString("route_id"),
                                    currentRoute.getString("route_name"),
                                    currentMode.getInt("route_type")));
                            trip.setStopId(stop.getString("stop_id"));
                            trip.setStopName(stop.getString("stop_name"));
                            trip.setDirection(currentDirection.getInt("direction_id"));
                            trip.setDestination(currentTrip.getString("trip_headsign"));
                            trip.setArrivalTime(currentTrip.getLong("pre_away"));

                            // Add trip to the predictions list
                            predictions.add(trip);
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

    private static HashMap<String, ArrayList<Alert>> extractAlertsFromJson(String jsonResponse) {

        // A route may contain multiple alerts, so we use ArrayList<Alert> to store multiple
        // Alerts for each route
        HashMap<String, ArrayList<Alert>> alerts = new HashMap<>();

        // Current time in seconds
        long currentTime = (new Date().getTime()) / 1000;

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return alerts;
        }

        try {
            // Create a JSONObject from the JSON response string
            JSONObject jRoot = new JSONObject(jsonResponse);

            // Loop through each alert
            JSONArray jAlertsArray = jRoot.getJSONArray("alerts");
            for (int i = 0; i < jAlertsArray.length(); i++) {

                // Get the JSON object representing this alert
                JSONObject jAlert = jAlertsArray.getJSONObject(i);

                // Create a new Alert object
                // Pass in alert_id and header_text
                // header_text is sufficient and brief-enough information
                Alert alert = new Alert(
                        jAlert.getString("alert_id"),
                        jAlert.getString("header_text"));

                // Loop through each effect period of this alert
                JSONArray jEffectPeriods = jAlert.getJSONArray("effect_periods");
                for (int j = 0; j < jEffectPeriods.length(); j++) {

                    // Get the JSON object representing this effect period
                    JSONObject jPeriod = jEffectPeriods.getJSONObject(j);

                    // Get the start time of the alert
                    long start = jPeriod.getLong("effect_start");

                    // Get the end time of the alert, if it exists
                    long end;
                    try {
                        // There exists an end time
                        end = jPeriod.getLong("effect_end");
                    } catch (Exception e) {
                        // If there is no end time, then set to -1;
                        end = -1;
                    }

                    // If the current time is between the start/end times, then add alert
                    if (currentTime > start && (currentTime < end || end == -1)) {

                        // Loop through all the affected services
                        JSONArray jServices = jAlert.getJSONObject("affected_services")
                                .getJSONArray("services");
                        for (int k = 0; k < jServices.length(); k++) {

                            // Get the JSON object representing this affected route
                            JSONObject jRoute = jServices.getJSONObject(k);

                            // Get the route ID
                            String routeId = jRoute.getString("route_id");

                            // Check if we already have a key routeId in hash map
                            // If yes, then add the alert to the existing ArrayList in its value
                            // If not, then create a new key-value pair for this route
                            // Reminder: key = routeID, value = ArrayList<Alert>
                            if (!alerts.containsKey(routeId)) {
                                alerts.put(routeId, new ArrayList<Alert>());
                            }

                            // Check if this route already has an instance of this alert
                            if (!alerts.get(routeId).contains(alert)) {
                                alerts.get(routeId).add(alert);
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
