package jackwtat.simplembta.controllers;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.v3api.PredictionsByLocationQuery;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.clients.LocationClient;
import jackwtat.simplembta.clients.LocationClient.OnUpdateSuccessListener;
import jackwtat.simplembta.clients.LocationClient.OnUpdateFailedListener;
import jackwtat.simplembta.clients.LocationClient.OnPermissionDeniedListener;
import jackwtat.simplembta.clients.NetworkConnectivityClient;

/**
 * Created by jackw on 12/1/2017.
 */

public class NearbyPredictionsController {
    private final String LOG_TAG = "NPController";

    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    public final long MINIMUM_REFRESH_INTERVAL = 30000;

    // Time between location updates, in milliseconds
    public final long LOCATION_UPDATE_INTERVAL = 10000;

    // Fastest time between location updates, in milliseconds
    public final long FASTEST_LOCATION_INTERVAL = 2000;

    // Maximum age of prediction, in milliseconds
    public final long MAXIMUM_PREDICTION_AGE = 180000;

    // Maximum age of location data, in milliseconds
    public final long MAXIMUM_LOCATION_AGE = 15000;

    // Time to wait between attempts to get recent location, in milliseconds
    public final int LOCATION_ATTEMPT_WAIT_TIME = 500;

    // Maximum number of attempts to get recent location
    public final int MAXIMUM_LOCATION_ATTEMPTS = 10;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private LocationClient locationClient;
    private PredictionsAsyncTask predictionsAsyncTask;

    private boolean refreshing;
    private Date lastRefreshed;
    private int locationAttempts;

    private OnNetworkErrorListener onNetworkErrorListener;
    private OnLocationErrorListener onLocationErrorListener;
    private OnLocationPermissionDeniedListener onLocationPermissionDeniedListener;
    private OnProgressUpdateListener onProgressUpdateListener;
    private OnPostExecuteListener onPostExecuteListener;

    public NearbyPredictionsController(Context context,
                                       OnPostExecuteListener onPostExecuteListener,
                                       OnProgressUpdateListener onProgressUpdateListener,
                                       OnNetworkErrorListener onNetworkErrorListener,
                                       OnLocationErrorListener onLocationErrorListener,
                                       final OnLocationPermissionDeniedListener onLocationPermissionDeniedListener) {
        realTimeApiKey = context.getString(R.string.v3_mbta_realtime_api_key);

        this.onPostExecuteListener = onPostExecuteListener;
        this.onProgressUpdateListener = onProgressUpdateListener;
        this.onNetworkErrorListener = onNetworkErrorListener;
        this.onLocationErrorListener = onLocationErrorListener;
        this.onLocationPermissionDeniedListener = onLocationPermissionDeniedListener;

        networkConnectivityClient = new NetworkConnectivityClient(context);

        locationClient = new LocationClient(context, LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL);

        locationClient.setOnUpdateSuccessListener(new OnUpdateSuccessListener() {
            @Override
            public void onUpdateSuccess(Location location) {
                if (new Date().getTime() - location.getTime() < MAXIMUM_LOCATION_AGE) {
                    // If the location is new enough, okay to query predictions
                    predictionsAsyncTask = new PredictionsAsyncTask();
                    predictionsAsyncTask.execute(location);

                } else if (locationAttempts < MAXIMUM_LOCATION_ATTEMPTS) {
                    // If location is too old, wait and then try again
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            locationAttempts++;
                            locationClient.getLastLocation();
                        }
                    }, LOCATION_ATTEMPT_WAIT_TIME);

                } else {
                    // If location is still too old after many attempts, query predictions anyway
                    predictionsAsyncTask = new PredictionsAsyncTask();
                    predictionsAsyncTask.execute(location);
                }
            }
        });

        locationClient.setOnUpdateFailedListener(new OnUpdateFailedListener() {
            @Override
            public void onUpdateFailed() {
                onLocationUpdateFailed();
            }
        });
        locationClient.setOnPermissionDeniedListener(new OnPermissionDeniedListener() {
            @Override
            public void onPermissionDenied() {
                onLocationPermissionDenied();
            }
        });
    }

    public void connect() {
        locationClient.connect();
    }

    public void disconnect() {
        locationClient.disconnect();
    }

    public void update() {
        if (!refreshing && (lastRefreshed == null ||
                new Date().getTime() - lastRefreshed.getTime() >= MINIMUM_REFRESH_INTERVAL)) {

            getPredictions();
        }
    }

    public void forceUpdate() {
        if (!refreshing) {
            getPredictions();
        }
    }

    public void cancel() {
        refreshing = false;

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        locationClient.disconnect();
    }

    public boolean isRunning() {
        return refreshing;
    }

    public long getTimeOfLastRefresh() {
        if (lastRefreshed != null) {
            return lastRefreshed.getTime();
        } else {
            return 0;
        }
    }

    private void getPredictions() {
        refreshing = true;
        onProgressUpdateListener.onProgressUpdate(0);

        if (!networkConnectivityClient.isConnected()) {
            refreshing = false;
            onNetworkErrorListener.onNetworkError();
        } else {
            locationAttempts = 0;
            locationClient.getLastLocation();
        }
    }

    private void onLocationUpdateFailed() {
        refreshing = false;
        onLocationErrorListener.onLocationError();
    }

    private void onLocationPermissionDenied() {
        refreshing = false;
        onLocationPermissionDeniedListener.OnLocationPermissionDenied();
    }

    private class PredictionsAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
        @Override
        protected void onPreExecute() {
            onProgressUpdateListener.onProgressUpdate(0);
        }

        @Override
        protected List<Stop> doInBackground(Location... locations) {
            /*
            // Get the user's current latitude and longitude
            Double lat = locations[0].getLatitude();
            Double lon = locations[0].getLongitude();

            MbtaApiClient query = new MbtaApiClient(realTimeApiKey);

            // Get the stops near the user
            String[] stopArgs = {
                    "filter[latitude]=" + Double.toString(lat),
                    "filter[longitude]=" + Double.toString(lon),
                    "include=child_stops"
            };
            String stopsJsonResponse = query.get("stops", stopArgs);
            ArrayList<MbtaStop> stops =
                    new ArrayList<>(Arrays.asList(StopsJsonParser.parse(stopsJsonResponse)));


            if (stops.size() > 0) {
                // Get all the routes at these stops
                StringBuilder routesArgBuilder = new StringBuilder(stops.get(0).getId());
                for (int i = 1; i < stops.size(); i++) {
                    routesArgBuilder.append(",").append(stops.get(i).getId());
                }
                String[] routesArgs = {"filter[stop]" + routesArgBuilder.toString()};
                String routesJsonResponse = query.get("routes", routesArgs);
                MbtaRoute[] routes = RoutesJsonParser.parse(routesJsonResponse);

                // Get all alerts for these routes
                if (routes.length > 0) {
                    StringBuilder alertsArgBuilder = new StringBuilder(routes[0].getId());
                    for (int i = 1; i < routes.length; i++) {
                        alertsArgBuilder.append(",").append(routes[i].getId());
                    }
                    String[] alertsArgs = {"filter[route]=" + alertsArgBuilder.toString()};
                    String alertsJsonResponse = query.get("alerts", alertsArgs);
                }

                // Get the predictions near the user
                String[] predictionArgs = {
                        "filter[latitude]=" + Double.toString(lat),
                        "filter[longitude]=" + Double.toString(lon),
                        "include=route,trip"
                };
                String predictionsJsonResponse = query.get("predictions", predictionArgs);
                MbtaPrediction[] predictions = PredictionsJsonParser.parse(predictionsJsonResponse);

                // Add predictions to their respective stops
                for (MbtaPrediction prediction : predictions) {
                    for (MbtaStop stop : stops) {
                        if (stop.isParentOf(prediction.getStopId())) {
                            prediction.setStopId(stop.getId());
                            prediction.setStopName(stop.getName());

                            stop.addPrediction(prediction);

                            break;
                        }
                    }
                }
            }

            for (MbtaStop stop : stops) {
                stop.setDistance(lat, lon);
            }

            Collections.sort(stops);
            */

            return new ArrayList<>(
                    new PredictionsByLocationQuery(realTimeApiKey)
                            .get(locations[0].getLatitude(), locations[0].getLongitude())
                            .values());
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            refreshing = false;
            lastRefreshed = new Date();
            onPostExecuteListener.onPostExecute(stops);
        }
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(int progress);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(List<Stop> stops);
    }

    public interface OnNetworkErrorListener {
        void onNetworkError();
    }

    public interface OnLocationErrorListener {
        void onLocationError();
    }

    public interface OnLocationPermissionDeniedListener {
        void OnLocationPermissionDenied();
    }
}
