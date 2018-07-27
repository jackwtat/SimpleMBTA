package jackwtat.simplembta.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.controllers.MapSearchController;
import jackwtat.simplembta.controllers.listeners.OnNetworkErrorListener;
import jackwtat.simplembta.controllers.listeners.OnPostExecuteListener;
import jackwtat.simplembta.controllers.listeners.OnProgressUpdateListener;
import jackwtat.simplembta.mbta.structure.Stop;
import jackwtat.simplembta.views.PredictionsListView;

public class MapSearchFragment extends RefreshableFragment implements OnMapReadyCallback {
    private final static String LOG_TAG = "MapSearchFragment";

    private final long AUTO_REFRESH_RATE = 60000;

    private View rootView;
    private MapView mapView;
    private GoogleMap gMap;
    private PredictionsListView predictionsListView;

    private MapSearchController controller;
    private Timer autoRefreshTimer;
    private Location lastLocation;

    private boolean mapReady = false;
    private boolean mapCameraMoving = false;
    private boolean resetUI = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new MapSearchController(getContext(),
                new OnPostExecuteListener() {
                    public void onPostExecute(List<Stop> stops) {
                        try {
                            predictionsListView.publishPredictions(getContext(), stops, resetUI);
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, "Pushing get results to nonexistent view");
                        }
                    }
                },
                new OnProgressUpdateListener() {
                    public void onProgressUpdate(int progress) {
                        try {
                            predictionsListView.setRefreshProgress();
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, "Pushing progress update to nonexistent view");
                        }
                    }
                },
                new OnNetworkErrorListener() {
                    public void onNetworkError() {
                        predictionsListView.onRefreshError(getResources().getString(R.string.no_network_connectivity));
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map_search, container, false);

        mapView = rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        predictionsListView = rootView.findViewById(R.id.predictions_list_view);
        predictionsListView.displayStatusTime(false);
        predictionsListView.setOnSwipeRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        forceRefresh();
                    }
                }
        );

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        lastLocation = new Location("");
        lastLocation.setLatitude(sharedPreferences.getFloat("latitude", (float) 42.3604));
        lastLocation.setLongitude(sharedPreferences.getFloat("longitude", (float) -71.0580));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        autoRefresh();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        refresh();
    }

    @Override
    public void onPause() {
        mapCameraMoving = false;
        predictionsListView.hideAlertsDialog();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("latitude", (float) lastLocation.getLatitude());
        editor.putFloat("longitude", (float) lastLocation.getLongitude());
        editor.apply();

        mapView.onPause();
        super.onPause();

    }

    @Override
    public void onStop() {
        if (controller.isRunning()) {
            predictionsListView.onRefreshCanceled();
        }

        autoRefreshTimer.cancel();
        controller.cancel();

        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        gMap = googleMap;

        gMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == REASON_GESTURE ||
                        reason == REASON_API_ANIMATION) {
                    mapCameraMoving = true;
                }
            }
        });

        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (mapCameraMoving) {
                    mapCameraMoving = false;

                    LatLng latLng = gMap.getCameraPosition().target;
                    lastLocation.setLatitude(latLng.latitude);
                    lastLocation.setLongitude(latLng.longitude);

                    controller.cancel();
                    forceRefresh();
                }
            }
        });

        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapUiSettings.setMyLocationButtonEnabled(true);
            gMap.setMyLocationEnabled(true);
        }

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
        forceRefresh();
    }


    @Override
    public void refresh() {
        if (mapReady) {
            resetUI = true;
            controller.update(lastLocation);
        }
    }

    @Override
    public void autoRefresh() {
        if (mapReady) {
            resetUI = false;
            controller.forceUpdate(lastLocation);
        }
    }

    @Override
    public void forceRefresh() {
        if (mapReady) {
            resetUI = true;
            controller.forceUpdate(lastLocation);
        }
    }
}
