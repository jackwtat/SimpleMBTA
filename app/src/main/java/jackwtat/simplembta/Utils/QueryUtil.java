package jackwtat.simplembta.Utils;

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
import java.util.List;

import jackwtat.simplembta.Prediction;
import jackwtat.simplembta.Stop;

import static android.R.attr.mode;

/**
 * Created by jackw on 9/1/2017.
 */

public class QueryUtil {
    private static final String TAG = "Query Util";

    //URL for querying the MBTA realTime API
    private static final String MBTA_URL = "http://realtime.mbta.com/developer/api/v2/";

    //API key
    private static final String API_KEY = "?api_key=0UkGTkcDrEmX_eT8sqeNoA";

    //Format specification
    private static final String RESPONSE_FORMAT = "&format=json";

    private QueryUtil() {
    }

    public static List<Prediction> fetchPredictionsByStop(String stopId) {

        String requestUrl = MBTA_URL + "predictionsbystop" + API_KEY + RESPONSE_FORMAT +
                "&stop=" + stopId;

        URL url = createUrl(requestUrl);

        Log.i(TAG, requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        return extractPredictionsFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
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
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving JSON results.", e);
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

    private static List<Prediction> extractPredictionsFromJson(String jsonResponse) {
        List<Prediction> predictions = new ArrayList<>();

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return predictions;
        }

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);

            // Loop through each mode of transportation at this stop
            JSONArray modes = baseJsonResponse.getJSONArray("mode");
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

                            // Create new Prediction object and populate with data
                            Prediction prediction = new Prediction(currentTrip.getString("trip_id"));
                            prediction.setRouteType(currentMode.getInt("route_type"));
                            prediction.setRouteId(currentRoute.getString("route_id"));
                            prediction.setRouteName(currentRoute.getString("route_name"));
                            prediction.setDirection(currentDirection.getInt("direction_id"));
                            prediction.setDestination(currentTrip.getString("trip_headsign"));
                            prediction.setPredictedArrivalTime(currentTrip.getLong("pre_away"));

                            predictions.add(prediction);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, "No data found");
        }

        // Return the list of predictions
        return predictions;
    }
}
