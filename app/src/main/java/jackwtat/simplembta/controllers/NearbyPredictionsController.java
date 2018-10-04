package jackwtat.simplembta.controllers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.clients.LocationClient;
import jackwtat.simplembta.clients.NetworkConnectivityClient;

/**
 * Created by jackw on 12/1/2017.
 */

public class NearbyPredictionsController {
    private final String LOG_TAG = "NPController";

    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    public final long MINIMUM_REFRESH_INTERVAL = 30000;

    // Time between location updates, in milliseconds
    public final long LOCATION_UPDATE_INTERVAL = 1000;

    // Fastest time between location updates, in milliseconds
    public final long FASTEST_LOCATION_INTERVAL = 500;

    // Maximum age of location data, in milliseconds
    public final long MAXIMUM_LOCATION_AGE = 15000;

    // Maximum age of prediction, in milliseconds
    public final long MAXIMUM_PREDICTION_AGE = 180000;

    // Time to wait between attempts to get recent location, in milliseconds
    public final int LOCATION_ATTEMPT_WAIT_TIME = 500;

    // Maximum number of attempts to get recent location
    public final int MAXIMUM_LOCATION_ATTEMPTS = 10;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private LocationClient locationClient;
    private final Callbacks callbacks;
    private PredictionsByLocationAsyncTask asyncTask;
    private PredictionsByLocationAsyncTask.Callbacks asyncTaskCallbacks;

    private boolean refreshing;
    private Date lastRefreshed;
    private int locationAttempts;

    public NearbyPredictionsController(Context context, Callbacks controllerCallbacks) {
        realTimeApiKey = context.getString(R.string.v3_mbta_realtime_api_key);

        this.callbacks = controllerCallbacks;

        asyncTaskCallbacks = new PredictionsByLocationAsyncTask.Callbacks() {
            @Override
            public void onPreExecute() {
                callbacks.onProgressUpdate();
            }

            @Override
            public void onPostExecute(List<Route> routes) {
                refreshing = false;
                lastRefreshed = new Date();
                callbacks.onPostExecute(routes);
            }
        };

        networkConnectivityClient = new NetworkConnectivityClient(context);

        locationClient = new LocationClient(context, LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL, new LocationClient.OnUpdateCompleteListener() {
            @Override
            public void onComplete(int result) {
                switch (result) {
                    case LocationClient.SUCCESS:
                        Location location = locationClient.getLastLocation();

                        if (new Date().getTime() - location.getTime() < MAXIMUM_LOCATION_AGE) {
                            // If the location is new enough, okay to query predictions
                            asyncTask = new PredictionsByLocationAsyncTask(
                                    realTimeApiKey, location, asyncTaskCallbacks);
                            asyncTask.execute();

                        } else if (locationAttempts < MAXIMUM_LOCATION_ATTEMPTS) {
                            // If location is too old, wait and then try again
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    locationAttempts++;
                                    locationClient.updateLocation();
                                }
                            }, LOCATION_ATTEMPT_WAIT_TIME);

                        } else {
                            // If location is still too old after many attempts, query predictions anyway
                            asyncTask = new PredictionsByLocationAsyncTask(
                                    realTimeApiKey, location, asyncTaskCallbacks);
                            asyncTask.execute();
                        }
                        break;

                    case LocationClient.FAILURE:
                        onLocationUpdateFailed();
                        break;

                    case LocationClient.NO_PERMISSION:
                        onLocationPermissionDenied();
                        break;
                }
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

        if (asyncTask != null) {
            asyncTask.cancel(true);
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
        callbacks.onProgressUpdate();

        if (!networkConnectivityClient.isConnected()) {
            refreshing = false;
            callbacks.onNetworkError();
        } else {
            locationAttempts = 0;
            locationClient.updateLocation();
        }
    }

    private void onLocationUpdateFailed() {
        refreshing = false;
        callbacks.onLocationError();
    }

    private void onLocationPermissionDenied() {
        refreshing = false;
        callbacks.OnLocationPermissionDenied();
    }

    public interface Callbacks {
        void onProgressUpdate();

        void onPostExecute(List<Route> routes);

        void onNetworkError();

        void onLocationError();

        void OnLocationPermissionDenied();
    }
}
