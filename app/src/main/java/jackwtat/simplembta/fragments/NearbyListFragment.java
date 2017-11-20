package jackwtat.simplembta.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import jackwtat.simplembta.ServicesDbHelper;
import jackwtat.simplembta.data.ServiceAlert;
import jackwtat.simplembta.data.Stop;
import jackwtat.simplembta.R;
import jackwtat.simplembta.QueryUtil;
import jackwtat.simplembta.data.Trip;

/**
 * Created by jackw on 9/30/2017.
 */

public class NearbyListFragment extends PredictionsListFragment {
    private final String LOG_TAG = "NearbyListFragment";

    // Fine Location Permission
    private final int REQUEST_ACCESS_FINE_LOCATION = 1;

    // Time between location updates, in seconds
    private final long LOCATION_UPDATE_INTERVAL = 15;

    // Time since last refresh before predictions can automatically refresh onResume, in seconds
    private final long ON_RESUME_REFRESH_INTERVAL = 120;

    // Maximum distance to stop in miles
    private final double MAX_DISTANCE = .5;

    private LocationServicesClient locationServicesClient;
    private Location lastLocation;
    private PredictionAsyncTask predictionAsyncTask;
    private ServicesDbHelper servicesDbHelper;

    private boolean refreshing;
    private boolean networkConnectionChecked;
    private boolean locationConnectionChecked;
    private boolean hasNetworkConnection;
    private boolean hasLocationConnection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationServicesClient = new LocationServicesClient();
        servicesDbHelper = new ServicesDbHelper(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get location access permission from user
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // If sufficient time has lapsed since last refresh, then automatically refreshed predictions
        Date currentTime = new Date();
        if (lastRefreshed == null ||
                ((currentTime.getTime() - lastRefreshed.getTime()) > 1000 * ON_RESUME_REFRESH_INTERVAL)) {
            refreshPredictions();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        locationServicesClient.disconnectClient();

        if (predictionAsyncTask!= null){
            predictionAsyncTask.cancel(true);
        }

        if (refreshing){
            onRefreshCanceled();
            refreshing = false;
        }
    }

    @Override
    public void refreshPredictions() {
        if (!refreshing) {
            refreshing = true;

            networkConnectionChecked = false;
            locationConnectionChecked = false;

            updateNetworkStatus();
            locationServicesClient.updateLocation();
        }
    }

    private void onConnectivityStatusUpdate() {
        if (networkConnectionChecked && locationConnectionChecked) {
            if (!hasNetworkConnection) {
                onRefreshError(getResources().getString(R.string.no_network_connectivity));
            } else if (!hasLocationConnection) {
                onRefreshError(getResources().getString(R.string.no_location));
            } else {
                predictionAsyncTask = new PredictionAsyncTask();
                predictionAsyncTask.execute(lastLocation);
            }
        }
    }

    private void updateNetworkStatus() {
        setRefreshProgress(0, "Checking Network...");

        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        hasNetworkConnection = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        networkConnectionChecked = true;

        onConnectivityStatusUpdate();
    }

    private void updateLocationStatus(boolean connected) {
        hasLocationConnection = connected;
        locationConnectionChecked = true;

        onConnectivityStatusUpdate();
    }

    private void onRefreshError(String errorMessage) {
        refreshing = false;
        setStatus(new Date(), errorMessage, false, true);
    }

    /*
     * Wrapper around the GoogleApiClient and LocationServices API
     * Allows for easy access to the device's current location and GPS coordinates
     */
    private class LocationServicesClient implements LocationListener, ConnectionCallbacks,
            OnConnectionFailedListener {
        private GoogleApiClient googleApiClient;
        private LocationRequest locationRequest;

        private LocationServicesClient() {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000 * LOCATION_UPDATE_INTERVAL);
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i(LOG_TAG, "Location connection successful");
            updateLocation();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(LOG_TAG, "Location connection suspended");
            updateLocationStatus(false);
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e(LOG_TAG, "Location connection failed");
            updateLocationStatus(false);
        }

        @Override
        public void onLocationChanged(Location location) {
        }

        private void disconnectClient() {
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }

        private void updateLocation() {
            setRefreshProgress(0, getResources().getString(R.string.getting_location));

            // Check if googleApiClient is connected
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            }

            // Check if we have Location permissions
            else if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(LOG_TAG, "Location permission missing");
                updateLocationStatus(false);
            }

            // Try getting location
            else {
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                            locationRequest, this);
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    if (lastLocation == null) {
                        updateLocationStatus(false);
                    } else {
                        updateLocationStatus(true);
                    }
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "Location services error");
                    updateLocationStatus(false);
                }
            }
        }
    }

    /*
        AsyncTask that asynchronously queries the MBTA API and displays the results upon success
    */
    private class PredictionAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
        private final int LOADING_DATABASE = -1;
        private final int GETTING_NEARBY_STOPS = -2;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Stop> doInBackground(Location... locations) {

            // Load the stops database
            publishProgress(LOADING_DATABASE);
            servicesDbHelper.loadDatabase(getContext());

            // Get all stops within the specified maximum distance from user's location
            publishProgress(GETTING_NEARBY_STOPS);
            List<Stop> stops = servicesDbHelper.getStopsByLocation(locations[0], MAX_DISTANCE);

            // Let user know we're not getting predictions
            publishProgress(0);

            // Get all service alerts
            HashMap<String, ArrayList<ServiceAlert>> alerts = QueryUtil.fetchAlerts(getString(R.string.mbta_realtime_api_key));

            // Get predicted trips for each stop
            for (int i = 0; i < stops.size(); i++) {
                Stop stop = stops.get(i);

                stop.addTrips(QueryUtil.fetchPredictionsByStop(getString(R.string.mbta_realtime_api_key), stop.getId()));

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

        protected void onProgressUpdate(Integer... progress) {
            try {
                if (progress[0] == LOADING_DATABASE) {
                    setRefreshProgress(0, getResources().getString(R.string.loading_database));
                } else if (progress[0] == GETTING_NEARBY_STOPS) {
                    setRefreshProgress(0, getResources().getString(R.string.getting_nearby_stops));
                } else {
                    setRefreshProgress(progress[0], getResources().getString(R.string.getting_predictions));
                }
            } catch (IllegalStateException e){
                Log.e(LOG_TAG, "Pushing progress update to nonexistent view");
            }
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            try {
                refreshing = false;
                publishPredictions(stops);
            } catch (IllegalStateException e){
                Log.e(LOG_TAG, "Pushing query results to nonexistent view");
            }
        }
    }
}