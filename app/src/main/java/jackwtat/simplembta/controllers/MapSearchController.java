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
import jackwtat.simplembta.controllers.listeners.OnLocationErrorListener;
import jackwtat.simplembta.controllers.listeners.OnNetworkErrorListener;
import jackwtat.simplembta.controllers.listeners.OnPostExecuteListener;
import jackwtat.simplembta.controllers.listeners.OnProgressUpdateListener;
import jackwtat.simplembta.mbta.structure.Stop;
import jackwtat.simplembta.mbta.v3api.PredictionsByLocationQuery;
import jackwtat.simplembta.services.LocationProviderService;
import jackwtat.simplembta.services.NetworkConnectivityService;

public class MapSearchController {
    private final String LOG_TAG = "MSController";

    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    private final long MINIMUM_REFRESH_INTERVAL = 30000;

    private String realTimeApiKey;
    private NetworkConnectivityService networkConnectivityService;
    private PredictionsAsyncTask predictionsAsyncTask;

    private Location location;
    private boolean refreshing;
    private Date lastRefreshed;

    private OnNetworkErrorListener onNetworkErrorListener;
    private OnProgressUpdateListener onProgressUpdateListener;
    private OnPostExecuteListener onPostExecuteListener;


    public MapSearchController(Context context,
                               OnPostExecuteListener onPostExecuteListener,
                               OnProgressUpdateListener onProgressUpdateListener,
                               OnNetworkErrorListener onNetworkErrorListener) {
        realTimeApiKey = context.getString(R.string.v3_mbta_realtime_api_key);

        this.onPostExecuteListener = onPostExecuteListener;
        this.onProgressUpdateListener = onProgressUpdateListener;
        this.onNetworkErrorListener = onNetworkErrorListener;

        networkConnectivityService = new NetworkConnectivityService(context);
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

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }
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
            predictionsAsyncTask = new PredictionsAsyncTask();
            predictionsAsyncTask.execute(location);
        }

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
}
