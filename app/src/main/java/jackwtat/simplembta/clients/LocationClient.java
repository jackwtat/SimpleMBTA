package jackwtat.simplembta.clients;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

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

public class LocationClient {
    private final String LOG_TAG = "LocationClient";

    public final static int SUCCESS = 0;
    public final static int FAILURE = 1;
    public final static int NO_PERMISSION = 2;

    private Context context;
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private OnUpdateCompleteListener onUpdateCompleteListener;

    @SuppressLint("RestrictedApi")
    public LocationClient(Context context, long updateInterval, long fastestUpdateInterval,
                          OnUpdateCompleteListener onUpdateCompleteListener) {
        this.context = context;

        locationClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(fastestUpdateInterval);

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

        this.onUpdateCompleteListener = onUpdateCompleteListener;

        lastLocation = null;
    }

    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lastLocation = location;
                                onUpdateCompleteListener.onComplete(SUCCESS);
                            } else {
                                lastLocation = null;
                                onUpdateCompleteListener.onComplete(FAILURE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            lastLocation = null;
                            onUpdateCompleteListener.onComplete(FAILURE);
                        }
                    });
        } else {
            onUpdateCompleteListener.onComplete(NO_PERMISSION);
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void connect() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper());
        }
    }

    public void disconnect() {
        locationClient.removeLocationUpdates(locationCallback);
    }

    public interface OnUpdateCompleteListener {
        void onComplete(int result);
    }
}
