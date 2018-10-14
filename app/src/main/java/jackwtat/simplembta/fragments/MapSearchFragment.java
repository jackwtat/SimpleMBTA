package jackwtat.simplembta.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.activities.RouteDetailActivity;
import jackwtat.simplembta.adapters.MapSearchRecyclerViewAdapter;
import jackwtat.simplembta.adapters.MapSearchRecyclerViewAdapter.OnItemClickListener;
import jackwtat.simplembta.clients.LocationClient;
import jackwtat.simplembta.clients.LocationClient.OnLocationUpdateCompleteListener;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.R;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.controllers.MapSearchController;

public class MapSearchFragment extends Fragment implements OnMapReadyCallback,
        OnCameraMoveStartedListener, OnCameraIdleListener, OnItemClickListener,
        OnLocationUpdateCompleteListener {
    private final static String LOG_TAG = "MapSearchFragment";

    // Time since last refresh before predictions can refresh
    public static final long MINIMUM_REFRESH_INTERVAL = 30000;

    // Maximum age of prediction
    public static final long MAXIMUM_PREDICTION_AGE = 90000;

    // Time since last refresh before predictions can automatically refresh
    private static final long AUTO_REFRESH_RATE = 30000;

    // Time since last refresh before location can automatically refresh
    private static final long AUTO_LOCATION_UPDATE_RATE = 30000;

    // Time between location updates
    public static final long LOCATION_UPDATE_INTERVAL = 500;

    // Fastest time between location updates
    public static final long FASTEST_LOCATION_INTERVAL = 250;

    // Maximum elapsed time since time of last location query
    public static final long MAXIMUM_TIME_SINCE_LAST_LOCATION = 90000;

    // Maximum age of fresh location data
    public static final long MAXIMUM_LOCATION_AGE = 1000;

    // Time to wait between attempts to get recent location
    public static final int LOCATION_ATTEMPT_WAIT_TIME = 250;

    // Maximum number of attempts to get recent location
    public static final int MAXIMUM_LOCATION_ATTEMPTS = 10;

    private View rootView;
    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;

    private MapSearchController controller;
    private MapSearchRecyclerViewAdapter recyclerViewAdapter;
    private ErrorManager errorManager;
    private Timer autoRefreshTimer;
    private Timer autoLocationUpdatetimer;
    private LocationClient locationClient;

    private boolean mapReady = false;
    private boolean mapCameraMoving = false;
    private boolean userHasMovedMap = false;
    private boolean routeDetailOpened = false;

    private Location currentLocation;
    private Date locationQueryTime;
    private int locationAttemptsCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        errorManager = ErrorManager.getErrorManager();

        controller = new MapSearchController(getContext(),
                new MapSearchController.Callbacks() {
                    @Override
                    public void onProgressUpdate() {
                    }

                    @Override
                    public void onPostExecute(List<Route> routes) {
                        recyclerViewAdapter.setRoutes(routes);

                        swipeRefreshLayout.setRefreshing(false);

                        if (recyclerViewAdapter.getItemCount() == 0) {
                            appBarLayout.setExpanded(true);
                            recyclerView.setNestedScrollingEnabled(false);
                            noPredictionsTextView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setNestedScrollingEnabled(true);
                            noPredictionsTextView.setVisibility(View.GONE);
                        }

                        errorManager.setNetworkError(false);
                    }

                    @Override
                    public void onNetworkError() {
                        swipeRefreshLayout.setRefreshing(false);
                        recyclerViewAdapter.clear();
                        errorManager.setNetworkError(true);
                    }
                });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        currentLocation = new Location("");
        currentLocation.setLatitude(sharedPreferences.getFloat("latitude", (float) 42.3604));
        currentLocation.setLongitude(sharedPreferences.getFloat("longitude", (float) -71.0580));

        locationClient = new LocationClient(getContext(), LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map_search, container, false);

        // Get app bar and app bar params
        appBarLayout = rootView.findViewById(R.id.app_bar_layout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();

        // Set app bar height
        params.height = (int) (getResources().getDisplayMetrics().heightPixels * .6);

        // Disable scrolling inside app bar
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        // Initialize map view
        mapView = rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize swipe refresh layout
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceRefresh();
            }
        });

        // Initialize recycler view
        recyclerView = rootView.findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Disable scrolling while fragment is still initializing
        recyclerView.setNestedScrollingEnabled(false);

        // Set recycler view predictions adapter
        recyclerViewAdapter = new MapSearchRecyclerViewAdapter();
        recyclerViewAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(recyclerViewAdapter);

        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);
        noPredictionsTextView.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        locationClient.connect();

        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        Long onResumeTime = new Date().getTime();

        // If the user navigated to Route Detail and too much time has elapsed when they returned,
        // then clear the predictions and force a refresh
        if (routeDetailOpened &&
                onResumeTime - controller.getTimeOfLastRefresh() > MAXIMUM_PREDICTION_AGE) {
            clearPredictions();
            swipeRefreshLayout.setRefreshing(true);
            forceRefresh();

            // If too much time has elapsed since the user has navigated away from the app,
            // then clear the predictions and update the location to force a refresh
        } else if (!routeDetailOpened && (locationQueryTime == null ||
                onResumeTime - locationQueryTime.getTime() > MAXIMUM_TIME_SINCE_LAST_LOCATION)) {
            clearPredictions();
            swipeRefreshLayout.setRefreshing(true);
            refreshLocation();
        } else {
            refresh();
        }

        routeDetailOpened = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        mapCameraMoving = false;

        // Get the location the user last viewed
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("latitude", (float) currentLocation.getLatitude());
        editor.putFloat("longitude", (float) currentLocation.getLongitude());
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        locationClient.disconnect();

        controller.cancel();

        autoRefreshTimer.cancel();

        swipeRefreshLayout.setRefreshing(false);

        userHasMovedMap = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

        // Set the action listeners
        gMap.setOnCameraMoveStartedListener(this);
        gMap.setOnCameraIdleListener(this);

        // Set the map style
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        // Set the map UI settings
        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapUiSettings.setMyLocationButtonEnabled(true);
            gMap.setMyLocationEnabled(true);
        }

        // Move the map camera to the last known location
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));

        // Draw the subway and commuter rail lines
        try {
            JSONObject jRouteShapes = new JSONObject(
                    RawResourceReader.toString(getResources().openRawResource(R.raw.route_shapes)));

            JSONArray jData = jRouteShapes.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jRoute = jData.getJSONObject(i);
                    gMap.addPolyline(new PolylineOptions()
                            .addAll(PolyUtil.decode(jRoute.getString("shape")))
                            .color(Color.parseColor("#FFFFFF"))
                            .zIndex(0)
                            .jointType(JointType.ROUND)
                            .startCap(new RoundCap())
                            .endCap(new RoundCap())
                            .width(14));
                    gMap.addPolyline(new PolylineOptions()
                            .addAll(PolyUtil.decode(jRoute.getString("shape")))
                            .color(Color.parseColor(jRoute.getString("color")))
                            .zIndex(jRoute.getInt("z-index"))
                            .jointType(JointType.ROUND)
                            .startCap(new RoundCap())
                            .endCap(new RoundCap())
                            .width(8));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse route shape at index " + i);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse route shapes JSON");
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        mapCameraMoving = true;

        userHasMovedMap = reason == REASON_GESTURE || reason == REASON_API_ANIMATION;
    }

    @Override
    public void onCameraIdle() {
        if (mapCameraMoving) {
            mapCameraMoving = false;

            LatLng latLng = gMap.getCameraPosition().target;

            currentLocation.setLatitude(latLng.latitude);
            currentLocation.setLongitude(latLng.longitude);

            swipeRefreshLayout.setRefreshing(true);
            forceRefresh();
        }
    }

    @Override
    public void onLocationUpdateComplete(int result) {
        switch (result) {
            case LocationClient.SUCCESS:
                Location currentLocation = locationClient.getLastLocation();
                locationQueryTime = new Date();

                if (locationAttemptsCount < MAXIMUM_LOCATION_ATTEMPTS) {
                    if (locationQueryTime.getTime() - currentLocation.getTime() < MAXIMUM_LOCATION_AGE) {
                        locationAttemptsCount = MAXIMUM_LOCATION_ATTEMPTS;
                    } else {
                        locationAttemptsCount++;
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            locationClient.updateLocation();
                        }
                    }, LOCATION_ATTEMPT_WAIT_TIME);
                } else {
                    recenterMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                    errorManager.setNetworkError(false);
                    errorManager.setLocationError(false);
                    errorManager.setLocationPermissionDenied(false);
                }

                break;

            case LocationClient.FAILURE:
                errorManager.setLocationError(true);
                swipeRefreshLayout.setRefreshing(true);
                forceRefresh();

                break;

            case LocationClient.NO_PERMISSION:
                errorManager.setLocationPermissionDenied(true);
                swipeRefreshLayout.setRefreshing(true);
                forceRefresh();

                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        routeDetailOpened = true;

        Intent intent = new Intent(getActivity(), RouteDetailActivity.class);
        intent.putExtra("route", recyclerViewAdapter.getData(position).getRoute());
        intent.putExtra("stop", recyclerViewAdapter.getData(position).getStop());
        intent.putExtra("direction", recyclerViewAdapter.getData(position).getDirection());
        startActivity(intent);
    }

    public void refresh() {
        if (mapReady) {
            controller.update(currentLocation);
        }
    }

    public void forceRefresh() {
        if (mapReady) {
            controller.forceUpdate(currentLocation);
        }
    }

    private void refreshLocation() {
        locationAttemptsCount = 0;
        locationClient.updateLocation();
    }

    private void recenterMap(LatLng latLng) {
        if (mapReady && !userHasMovedMap) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
    }
}
