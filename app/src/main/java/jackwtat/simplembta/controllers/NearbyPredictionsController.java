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
import jackwtat.simplembta.mbta.structure.*;
import jackwtat.simplembta.services.LocationProviderService;
import jackwtat.simplembta.services.NetworkConnectivityService;

/**
 * Created by jackw on 12/1/2017.
 */

public class NearbyPredictionsController {
    private final String LOG_TAG = "NPController";

    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    private final long MINIMUM_REFRESH_INTERVAL = 30000;

    // Time between location updates, in milliseconds
    private final long LOCATION_UPDATE_INTERVAL = 10000;

    // Fastest time between location updates, in milliseconds
    private final long FASTEST_LOCATION_INTERVAL = 2000;

    // Maximum age of location data, in milliseconds
    private final long MAXIMUM_LOCATION_AGE = 15000;

    // Time to wait between attempts to get recent location, in milliseconds
    private final int LOCATION_ATTEMPT_WAIT_TIME = 500;

    // Maximum number of attempts to get recent location
    private final int MAXIMUM_LOCATION_ATTEMPTS = 10;

    private String realTimeApiKey;
    private NetworkConnectivityService networkConnectivityService;
    private LocationProviderService locationProviderService;
    private PredictionsAsyncTask predictionsAsyncTask;

    private boolean refreshing;
    private Date lastRefreshed;
    private int locationAttempts;

    private OnNetworkErrorListener onNetworkErrorListener;
    private OnLocationErrorListener onLocationErrorListener;
    private OnProgressUpdateListener onProgressUpdateListener;
    private OnPostExecuteListener onPostExecuteListener;

    public NearbyPredictionsController(Context context,
                                       OnPostExecuteListener onPostExecuteListener,
                                       OnProgressUpdateListener onProgressUpdateListener,
                                       OnNetworkErrorListener onNetworkErrorListener,
                                       OnLocationErrorListener onLocationErrorListener) {
        realTimeApiKey = context.getString(R.string.v3_mbta_realtime_api_key);

        this.onPostExecuteListener = onPostExecuteListener;
        this.onProgressUpdateListener = onProgressUpdateListener;
        this.onNetworkErrorListener = onNetworkErrorListener;
        this.onLocationErrorListener = onLocationErrorListener;

        networkConnectivityService = new NetworkConnectivityService(context);

        locationProviderService = new LocationProviderService(context, LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL);

        locationProviderService.setOnUpdateSuccessListener(new LocationProviderService.OnUpdateSuccessListener() {
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
                            locationProviderService.getLastLocation();
                        }
                    }, LOCATION_ATTEMPT_WAIT_TIME);

                } else {
                    // If location is still too old after many attempts, query predictions anyway
                    predictionsAsyncTask = new PredictionsAsyncTask();
                    predictionsAsyncTask.execute(location);
                }
            }
        });

        locationProviderService.setOnUpdateFailedListener(new LocationProviderService.OnUpdateFailedListener() {
            @Override
            public void onUpdateFailed() {
                onLocationUpdateFailed();
            }
        });
    }

    public void connect() {
        locationProviderService.connect();
    }

    public void disconnect() {
        locationProviderService.disconnect();
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

        locationProviderService.disconnect();
    }

    public boolean isRunning() {
        return refreshing;
    }

    private void getPredictions() {
        refreshing = true;
        onProgressUpdateListener.onProgressUpdate(0);

        if (!networkConnectivityService.isConnected()) {
            refreshing = false;
            onNetworkErrorListener.onNetworkError();
        } else {
            locationAttempts = 0;
            locationProviderService.getLastLocation();
        }
    }

    private void onLocationUpdateFailed() {
        refreshing = false;
        onLocationErrorListener.onLocationError();
    }

    private class PredictionsAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
        @Override
        protected void onPreExecute() {
            onProgressUpdateListener.onProgressUpdate(0);
        }

        @Override
        protected List<Stop> doInBackground(Location... locations) {
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
}
