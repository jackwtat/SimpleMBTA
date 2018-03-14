package jackwtat.simplembta.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by jackw on 12/4/2017.
 */

public class LocationProviderService {
    private final String LOG_TAG = "LocationProviderService";

    private Context context;
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private OnUpdateSuccessListener onUpdateSuccessListener;
    private OnUpdateFailedListener onUpdateFailedListener;

    public LocationProviderService(Context context, long updateInterval, long fastestInterval) {
        this.context = context;

        locationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(updateInterval * 1000);
        locationRequest.setFastestInterval(fastestInterval * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                locationResult.getLastLocation();
            }
        };

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper());
        }
    }

    public void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                onUpdateSuccessListener.onUpdateSuccess(location);
                            } else {
                                onUpdateFailedListener.onUpdateFailed();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            onUpdateFailedListener.onUpdateFailed();
                        }
                    });
        } else {
            onUpdateFailedListener.onUpdateFailed();
        }

        locationClient.removeLocationUpdates(locationCallback);
    }

    // Register OnUpdateSuccessListener with this service
    public void setOnUpdateSuccessListener(OnUpdateSuccessListener listener) {
        onUpdateSuccessListener = listener;
    }

    // Register OnLocationErrorListener with this service
    public void setOnUpdateFailedListener(OnUpdateFailedListener listener) {
        onUpdateFailedListener = listener;
    }

    public interface OnUpdateSuccessListener {
        void onUpdateSuccess(Location location);
    }

    public interface OnUpdateFailedListener {
        void onUpdateFailed();
    }
}
