package jackwtat.simplembta.controllers;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.mbta.v3api.PredictionsByLocationQuery;
import jackwtat.simplembta.clients.NetworkConnectivityClient;

public class MapSearchController {
    private final String LOG_TAG = "MSController";

    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    public final long MINIMUM_REFRESH_INTERVAL = 30000;

    // Maximum age of prediction, in milliseconds
    public final long MAXIMUM_PREDICTION_AGE = 180000;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
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

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
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
        onProgressUpdateListener.onProgressUpdate(0);

        if (!networkConnectivityClient.isConnected()) {
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

    public interface OnProgressUpdateListener {
        void onProgressUpdate(int progress);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(List<Stop> stops);
    }

    public interface OnNetworkErrorListener {
        void onNetworkError();
    }
}
