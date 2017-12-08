package jackwtat.simplembta.controllers;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.api.PredictionsByStopQuery;
import jackwtat.simplembta.mbta.api.RealTimeApi;
import jackwtat.simplembta.mbta.api.ServiceAlertsQuery;
import jackwtat.simplembta.mbta.data.ServiceAlert;
import jackwtat.simplembta.mbta.data.Stop;
import jackwtat.simplembta.mbta.data.Trip;
import jackwtat.simplembta.mbta.database.MbtaDbHelper;
import jackwtat.simplembta.services.LocationProviderService;
import jackwtat.simplembta.services.NetworkConnectivityService;

/**
 * Created by jackw on 12/1/2017.
 */

public class NearbyPredictionsController {
    private final String LOG_TAG = "NPController";

    // Time since last refresh before predictions can automatically refresh onResume, in seconds
    private final long MINIMUM_REFRESH_INTERVAL = 120;

    // Time between location updates, in seconds
    private final long LOCATION_UPDATE_INTERVAL = 15;

    // Fastest time between location updates, in seconds
    private final long FASTEST_LOCATION_INTERVAL = 2;

    // Maximum distance to stop in miles
    private final double MAX_DISTANCE = .5;

    private RealTimeApi realTimeApi;
    private MbtaDbHelper mbtaDbHelper;
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
        realTimeApi = new RealTimeApi(context.getString(R.string.mbta_realtime_api_key));

        mbtaDbHelper = new MbtaDbHelper(context);

        networkConnectivityService = new NetworkConnectivityService(context);

        locationProviderService = new LocationProviderService(context, LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL);
    }

    public void getPredictions(boolean ignoreTimeLimit) {
        if (ignoreTimeLimit ||
                lastRefreshed == null ||
                new Date().getTime() - lastRefreshed.getTime() >= 1000 * MINIMUM_REFRESH_INTERVAL) {

            refreshing = true;

            if (!networkConnectivityService.isConnected()) {
                refreshing = false;
                onNetworkErrorListener.onNetworkError();
            } else {
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

            // Get all stops within the specified maximum distance from user's location
            List<Stop> stops = mbtaDbHelper.getStopsByLocation(locations[0], MAX_DISTANCE);

            // Get all service alerts
            HashMap<String, ArrayList<ServiceAlert>> alerts = new ServiceAlertsQuery(realTimeApi).get();

            // Get predicted trips for each stop
            PredictionsByStopQuery pbsQuery = new PredictionsByStopQuery(realTimeApi);
            for (int i = 0; i < stops.size(); i++) {
                Stop stop = stops.get(i);

                stop.addTrips(pbsQuery.get(stop.getId()));

                // Add alerts to trips whose route has alerts
                for (Trip trip : stop.getTrips()) {
                    if (alerts.containsKey(trip.getRouteId())) {
                        trip.setAlerts(alerts.get(trip.getRouteId()));
                    }
                }

                publishProgress((int) (100 * (i + 1) / stops.size()));
            }

            return stops;
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
