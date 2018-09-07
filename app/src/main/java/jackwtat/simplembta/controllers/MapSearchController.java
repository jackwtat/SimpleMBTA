package jackwtat.simplembta.controllers;

import android.content.Context;
import android.location.Location;

import java.util.Date;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.clients.NetworkConnectivityClient;

public class MapSearchController {
    private final String LOG_TAG = "MSController";

    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    public final long MINIMUM_REFRESH_INTERVAL = 30000;

    // Maximum age of prediction, in milliseconds
    public final long MAXIMUM_PREDICTION_AGE = 180000;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private Callbacks callbacks;
    private PredictionsByLocationAsyncTask asyncTask;
    private PredictionsByLocationAsyncTask.Callbacks asyncTaskCallbacks;

    private Location location;
    private boolean refreshing;
    private Date lastRefreshed;

    public MapSearchController(Context context, Callbacks controllerCallbacks) {
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
    }

    public void update(Location location) {
        if (!refreshing && (lastRefreshed == null ||
                new Date().getTime() - lastRefreshed.getTime() >= MINIMUM_REFRESH_INTERVAL)) {

            this.location = location;
            getPredictions();
        }
    }

    public void forceUpdate(Location location) {
        if (!refreshing) {
            this.location = location;
            getPredictions();
        }
    }

    public void cancel() {
        refreshing = false;

        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
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
            asyncTask = new PredictionsByLocationAsyncTask(
                    realTimeApiKey, location, asyncTaskCallbacks);
            asyncTask.execute();
        }

    }

    public interface Callbacks {
        void onProgressUpdate();

        void onPostExecute(List<Route> routes);

        void onNetworkError();
    }
}
