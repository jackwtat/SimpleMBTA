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
import java.util.List;
import java.util.Date;

import jackwtat.simplembta.data.Route;
import jackwtat.simplembta.data.Stop;
import jackwtat.simplembta.R;
import jackwtat.simplembta.QueryUtil;
import jackwtat.simplembta.StopDbHelper;
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
    private StopDbHelper stopDbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationServicesClient = new LocationServicesClient();
        stopDbHelper = new StopDbHelper(getContext());
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

        // Get current time
        // If sufficient time has lapsed since last refresh,
        // then automatically refreshed predictions
        Date currentTime = new Date();
        if (lastUpdated == null ||
                ((currentTime.getTime() - lastUpdated.getTime()) > 1000 * ON_RESUME_REFRESH_INTERVAL)) {
            refreshPredictions();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Disconnect the LocationServicesClient
        locationServicesClient.disconnectClient();

        // Cancel the AsyncTask if it is running
        if (predictionAsyncTask != null && predictionAsyncTask.cancel(true)) {
            displayTimedErrorStatus(getResources().getString(R.string.no_predictions), false);
        }
    }

    @Override
    public void refreshPredictions() {
        // Clear error status message
        clearErrorStatus(true);

        if (!checkNetworkConnection()) {
            // If no network connectivity found, show error message
            displayTimedErrorStatus(getResources().getString(R.string.no_network_connectivity), false);
        } else {
            // Refresh current location
            locationServicesClient.refreshLocation();
        }
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void onLocationFound(boolean found) {
        if (!found) {
            displayTimedErrorStatus(getResources().getString(R.string.no_location), false);
        } else {
            // We have both internet and location
            // Get predictions from MBTA API
            predictionAsyncTask = new PredictionAsyncTask();
            predictionAsyncTask.execute(lastLocation);
        }
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
            getLocation();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(LOG_TAG, "Location connection suspended");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e(LOG_TAG, "Location connection failed");
            onLocationFound(false);
        }

        @Override
        public void onLocationChanged(Location location) {
        }

        private void disconnectClient() {
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }

        private void refreshLocation() {
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            } else {
                Log.i(LOG_TAG, "Google API client already connected");
                getLocation();
            }
        }

        private void getLocation() {
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(LOG_TAG, "Location permission missing");
                onLocationFound(false);
            } else {
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                            locationRequest, this);
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    onLocationFound(true);
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "Location services error");
                    onLocationFound(false);
                }
            }
        }
    }

    // AsyncTask that asynchronously queries the MBTA API and displays the results upon success
    private class PredictionAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
        // AsyncTask statuses
        private final int LOADING_DATABASE = 1;
        private final int GETTING_NEARBY_STOPS = 2;
        private final int GETTING_PREDICTIONS = 3;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Stop> doInBackground(Location... locations) {
            publishProgress(LOADING_DATABASE);
            stopDbHelper.loadDatabase(getContext());

            ArrayList<Route> routes = new ArrayList<>();

            publishProgress(GETTING_NEARBY_STOPS);
            List<Stop> stops = stopDbHelper.getStopsByLocation(locations[0], MAX_DISTANCE);

            publishProgress(GETTING_PREDICTIONS);
            for (Stop stop : stops) {
                // Get predictions via MBTA API and add them as instances of Trip
                stop.addTrips(QueryUtil.fetchPredictionsByStop(stop.getId()));

                // Get service alerts for each trip via the MBTA API
                for (Trip trip : stop.getTrips()){
                    if (!routes.contains(trip.getRoute())){
                        trip.setAlerts(QueryUtil.fetchAlertsByRoute(trip.getRouteId()));
                        routes.add(trip.getRoute());
                    } else {
                        for(Route route : routes){
                            if (trip.getRouteId().equals(route.getId())){
                                trip.setAlerts(route.getAlerts());
                            }
                        }
                    }
                }
            }

            return stops;
        }

        protected void onProgressUpdate(Integer... progress) {
            int status = progress[0];

            switch (status) {
                case LOADING_DATABASE:
                    displayUpdateStatus(getResources().getString(R.string.loading_first_time), true);
                    break;
                case GETTING_NEARBY_STOPS:
                    displayUpdateStatus(getResources().getString(R.string.getting_nearby_stops), true);
                    break;
                case GETTING_PREDICTIONS:
                    displayUpdateStatus(getResources().getString(R.string.getting_predictions), true);
                    break;
            }
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            populateList(stops);
        }
    }
}