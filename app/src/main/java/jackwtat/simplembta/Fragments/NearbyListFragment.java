package jackwtat.simplembta.Fragments;

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
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Date;

import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.R;
import jackwtat.simplembta.Utils.QueryUtil;

/**
 * Created by jackw on 9/30/2017.
 */

public class NearbyListFragment extends PredictionsListFragment {
    private final String TAG = "NearbyListFragment";

    private boolean hasPredictions = false;
    private Date lastUpdated = new Date();

    private LocationServicesClient locationServicesClient;
    private double currentLatitude;
    private double currentLongitude;

    PredictionAsyncTask predictionAsyncTask;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationServicesClient = new LocationServicesClient();
    }

    @Override
    public void onResume() {
        Date currentTime = new Date();

        super.onResume();
        if (!hasPredictions) {
            update();
        } else if ((currentTime.getTime() - lastUpdated.getTime()) / 1000 > 60) {
            update();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void update() {
        // Clear predictions list
        // Show progress bar
        clearList();
        showProgressBar(true);
        showStatusMessage(false);

        // If no network connectivity found, show error message
        // Or if no location gotten, show error message
        if (!checkNetworkConnection()) {
            showProgressBar(false);
            setStatusMessage(getResources().getString(R.string.no_network_connectivity));
            showStatusMessage(true);
            lastUpdated = new Date();
            updateTime(lastUpdated);
            hasPredictions = false;
        } else {
            // Refresh current location
            locationServicesClient.refreshLocation();
        }
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private void locationFound(boolean found) {
        if (!found) {
            showProgressBar(false);
            setStatusMessage(getResources().getString(R.string.no_location));
            showStatusMessage(true);
            lastUpdated = new Date();
            updateTime(lastUpdated);
            hasPredictions = false;
        } else {
            // We have both internet and location
            // Get predictions from MBTA API
            predictionAsyncTask = new PredictionAsyncTask();
            double[] coordinates = {currentLatitude, currentLongitude};
            predictionAsyncTask.execute(coordinates);
        }
    }

    /*
     * Wrapper around the GoogleApiClient and LocationServices API
     * Allows for easy access to the device's current location and GPS coordinates
     */
    private class LocationServicesClient implements ConnectionCallbacks, OnConnectionFailedListener {
        private GoogleApiClient googleApiClient;

        private LocationServicesClient() {
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i(TAG, "Location connection successful");
            getLocation();
            locationFound(true);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(TAG, "Location connection suspended");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e(TAG, "Location connection failed");
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
                Log.i(TAG, "Google API client already connected");
                getLocation();
            }
        }

        private void getLocation() {
            Log.i(TAG, "getLocation() called");
            boolean locationFound = false;

            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showProgressBar(false);
                lastUpdated = new Date();
                updateTime(lastUpdated);
                setStatusMessage(getResources().getString(R.string.no_location));
                hasPredictions = false;
            } else {
                try {
                    Log.i(TAG, "Location found");
                    Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                } catch (Exception ex) {
                    Log.e(TAG, "Location services error");
                }
            }
            disconnectClient();
        }
    }

    /*
     * AsyncTask that asynchronously queries the MBTA API and displays the results upon success
     */
    private class PredictionAsyncTask extends AsyncTask<double[], Void, List<Stop>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Stop> doInBackground(double[]... coordinates) {

            List<Stop> stops = QueryUtil.fetchStopsByLocation(coordinates[0][0], coordinates[0][1]);

            for (Stop stop : stops) {
                String id = stop.getId();

                stop.addRoutes(QueryUtil.fetchRoutesByStop(id));
                stop.addTrips(QueryUtil.fetchPredictionsByStop(id));
            }
            return stops;
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            showProgressBar(false);
            populateList(stops);
            lastUpdated = new Date();
            updateTime(lastUpdated);
            hasPredictions = true;
        }
    }
}
