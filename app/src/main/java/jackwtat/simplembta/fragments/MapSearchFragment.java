package jackwtat.simplembta.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Timer;

import jackwtat.simplembta.R;
import jackwtat.simplembta.controllers.MapSearchController;
import jackwtat.simplembta.controllers.PredictionsController;
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

    private boolean mapReady = false;
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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
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

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.3604, -71.0580), 15));
    }


    @Override
    public void refresh() {
        if (mapReady) {
            LatLng latLng = gMap.getCameraPosition().target;

            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            controller.forceUpdate(location);
        }
    }

    @Override
    public void autoRefresh() {
        if (mapReady) {
            LatLng latLng = gMap.getCameraPosition().target;

            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            controller.forceUpdate(location);
        }
    }

    @Override
    public void forceRefresh() {
        if (mapReady) {
            LatLng latLng = gMap.getCameraPosition().target;

            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            controller.forceUpdate(location);
        }
    }
}
