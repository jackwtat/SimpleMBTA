package jackwtat.simplembta.controllers;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    // Time since last refresh before values can automatically refresh onResume, in seconds
    private final long MINIMUM_REFRESH_INTERVAL = 60;

    // Time between location updates, in seconds
    private final long LOCATION_UPDATE_INTERVAL = 15;

    // Fastest time between location updates, in seconds
    private final long FASTEST_LOCATION_INTERVAL = 2;

    private String realTimeApiKey;
    private NetworkConnectivityService networkConnectivityService;
    private LocationProviderService locationProviderService;
    private PredictionsAsyncTask predictionsAsyncTask;

    private OnNetworkErrorListener onNetworkErrorListener;
    private OnLocationErrorListener onLocationErrorListener;
    private OnProgressUpdateListener onProgressUpdateListener;
    private OnPostExecuteListener onPostExecuteListener;

    private boolean refreshing;
    private Date lastRefreshed;

    public NearbyPredictionsController(Context context) {
        realTimeApiKey = context.getString(R.string.v3_mbta_realtime_api_key);

        networkConnectivityService = new NetworkConnectivityService(context);

        locationProviderService = new LocationProviderService(context, LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL);

        locationProviderService.setOnUpdateSuccessListener(new LocationProviderService.OnUpdateSuccessListener() {
            @Override
            public void onUpdateSuccess(Location location) {
                predictionsAsyncTask = new PredictionsAsyncTask();
                predictionsAsyncTask.execute(location);
            }
        });

        locationProviderService.setOnUpdateFailedListener(new LocationProviderService.OnUpdateFailedListener() {
            @Override
            public void onUpdateFailed() {
                refreshing = false;
                onLocationErrorListener.onLocationError();
            }
        });
    }

    public void getPredictions(boolean ignoreTimeLimit) {
        if (!refreshing && (ignoreTimeLimit ||
                lastRefreshed == null ||
                new Date().getTime() - lastRefreshed.getTime() >= 1000 * MINIMUM_REFRESH_INTERVAL)) {

            refreshing = true;
            onProgressUpdateListener.onProgressUpdate(0);

            if (!networkConnectivityService.isConnected()) {
                refreshing = false;
                onNetworkErrorListener.onNetworkError();
            } else {
                locationProviderService.getLastLocation();
            }
        }
    }

    public void cancel() {
        refreshing = false;

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }
    }

    public boolean isRunning() {
        return refreshing;
    }

    public Date getLastRefreshedDate() {
        return lastRefreshed;
    }

    public void connectLocationService() {
        locationProviderService.connect();
    }

    public void disconnectLocationService() {
        locationProviderService.disconnect();
    }

    /*
        Event Listener Setter Methods
     */
    public void setNetworkErrorListener(OnNetworkErrorListener listener) {
        onNetworkErrorListener = listener;
    }

    public void setOnLocationErrorListener(OnLocationErrorListener listener) {
        onLocationErrorListener = listener;
    }

    public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
        onProgressUpdateListener = listener;
    }

    public void setOnPostExecuteListener(OnPostExecuteListener listener) {
        onPostExecuteListener = listener;
    }

    /*
        Event Listener Interfaces
     */
    public interface OnNetworkErrorListener {
        void onNetworkError();
    }

    public interface OnLocationErrorListener {
        void onLocationError();
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(int progress);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(List<Stop> stops);
    }


    private class PredictionsAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Stop> doInBackground(Location... locations) {
            return new ArrayList<>(
                    new PredictionsByLocationQuery(realTimeApiKey)
                            .get(locations[0].getLatitude(), locations[0].getLongitude())
                            .values());
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            onProgressUpdateListener.onProgressUpdate(progress[0]);
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            refreshing = false;
            lastRefreshed = new Date();
            onPostExecuteListener.onPostExecute(stops);
        }
    }
}
