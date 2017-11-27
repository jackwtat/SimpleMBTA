package jackwtat.simplembta.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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

import jackwtat.simplembta.listeners.OnLocationUpdateFailedListener;
import jackwtat.simplembta.listeners.LocationUpdateListener;


/**
 * Created by jackw on 11/24/2017.
 */

public class LocationService implements LocationListener, ConnectionCallbacks,
        OnConnectionFailedListener {
    private static final String LOG_TAG = "LocationService";

    private Context context;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private List<LocationUpdateListener> updateListeners = new ArrayList<>();
    private List<OnLocationUpdateFailedListener> updateFailedListeners = new ArrayList<>();

    public LocationService(Context context, long updateInterval) {
        this.context = context;

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000 * updateInterval);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "Location connection successful");
        updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Location connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Location connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    // Register LocationUpdateListener with this service
    public void addUpdateListener(LocationUpdateListener listener) {
        updateListeners.add(listener);
    }

    // Register OnLocationUpdateFailedListener with this service
    public void addUpdateFailedListener(OnLocationUpdateFailedListener listener) {
        updateFailedListeners.add(listener);
    }

    public void connectClient() {
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    public void disconnectClient() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public void updateLocation() {

        // Check if googleApiClient is connected
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }

        // Check if we have Location permissions
        else if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "Location permission missing");

            for (OnLocationUpdateFailedListener listener : updateFailedListeners) {
                listener.onLocationUpdateFailed();
            }
        }

        // Try getting location
        else {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                        locationRequest, this);

                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                for (LocationUpdateListener listener : updateListeners) {
                    listener.onLocationUpdate(lastLocation);
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, "Location services error");

                for (OnLocationUpdateFailedListener listener : updateFailedListeners) {
                    listener.onLocationUpdateFailed();
                }
            }
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}
