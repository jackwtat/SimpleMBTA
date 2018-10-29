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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.activities.RouteDetailActivity;
import jackwtat.simplembta.adapters.MapSearchRecyclerViewAdapter;
import jackwtat.simplembta.adapters.MapSearchRecyclerViewAdapter.OnItemClickListener;
import jackwtat.simplembta.asyncTasks.MapSearchPredictionsAsyncTask;
import jackwtat.simplembta.clients.LocationClient;
import jackwtat.simplembta.clients.LocationClient.LocationClientCallbacks;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.R;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.utilities.ShapesJsonParser;

public class MapSearchFragment extends Fragment implements OnMapReadyCallback,
        MapSearchPredictionsAsyncTask.OnPostExecuteListener, LocationClientCallbacks,
        ErrorManager.OnErrorChangedListener {
    public static final String LOG_TAG = "MapSearchFragment";

    // Maximum age of prediction
    public static final long MAX_PREDICTION_AGE = 90000;

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    // Location auto update rate
    public static final long LOCATION_UPDATE_RATE = 1000;

    // Time between location client updates
    public static final long LOCATION_CLIENT_INTERVAL = 500;

    // Fastest time between location updates
    public static final long FASTEST_LOCATION_CLIENT_INTERVAL = 250;

    // Time since last onStop() before restarting the location
    public static final long LOCATION_UPDATE_RESTART_TIME = 300000;

    // Default level of zoom for the map
    public static final int DEFAULT_MAP_ZOOM_LEVEL = 15;

    // Map interaction statuses
    private static final int USER_HAS_NOT_MOVED_MAP = 0;
    private static final int USER_HAS_MOVED_MAP = 1;

    private View rootView;
    private AppBarLayout appBarLayout;
    private MapView mapView;
    private ImageView mapTargetView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private LocationClient locationClient;
    private MapSearchPredictionsAsyncTask predictionsAsyncTask;
    private MapSearchRecyclerViewAdapter recyclerViewAdapter;
    private ErrorManager errorManager;
    private Timer timer;

    private boolean refreshing = false;
    private boolean mapReady = false;
    private boolean cameraIsMoving = false;
    private boolean userIsScrolling = false;
    private boolean staleLocation = true;
    private int mapState = USER_HAS_NOT_MOVED_MAP;
    private long refreshTime = 0;
    private long onPauseTime = 0;

    private List<Route> currentRoutes;
    private Location userLocation = new Location("");
    private Location targetLocation = new Location("");
    private ArrayList<Marker> stopMarkers = new ArrayList<>();
    private Marker selectedStopMarker = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get MBTA realTime API key
        realTimeApiKey = getContext().getString(R.string.v3_mbta_realtime_api_key);

        // Initialize network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(getContext());

        // Get the location the user last viewed
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        targetLocation.setLatitude(sharedPreferences.getFloat("latitude", (float) 42.3604));
        targetLocation.setLongitude(sharedPreferences.getFloat("longitude", (float) -71.0580));

        LocationClient.requestLocationPermission(getActivity());
        locationClient = new LocationClient(getContext(), LOCATION_CLIENT_INTERVAL,
                FASTEST_LOCATION_CLIENT_INTERVAL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

        // Get and initialize map view
        mapView = rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get the map target view
        mapTargetView = rootView.findViewById(R.id.map_target_view);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Initialize recycler view
        recyclerView = rootView.findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Disable scrolling while fragment is still initializing
        recyclerView.setNestedScrollingEnabled(false);

        // Add onScrollListener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    refreshPredictions();
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Set  predictions adapter
        recyclerViewAdapter = new MapSearchRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);

        // Set the onClickListener listener
        recyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), RouteDetailActivity.class);
                intent.putExtra("route", recyclerViewAdapter.getAdapterItem(position).getRoute());
                intent.putExtra("direction", recyclerViewAdapter.getAdapterItem(position).getDirection());
                intent.putExtra("refreshTime", refreshTime);
                startActivity(intent);
            }
        });

        // Set the no predictions indicator
        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);

        return rootView;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gMap = googleMap;

        // Set the map style
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        // Set the map UI settings
        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            mapUiSettings.setMyLocationButtonEnabled(true);
        }

        // Move the map camera to the last known location
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude()),
                DEFAULT_MAP_ZOOM_LEVEL));

        // Get the subway, Silver Line, and commuter rail shapes
        Shape[] blueShapes = getShapesFromJson(R.raw.shapes_blue);
        Shape[] orangeShapes = getShapesFromJson(R.raw.shapes_orange);
        Shape[] redShapes = getShapesFromJson(R.raw.shapes_red);
        Shape[] greenShapes = getShapesFromJson(R.raw.shapes_green);
        Shape[] silverShapes = getShapesFromJson(R.raw.shapes_silver);
        Shape[] mattapanShapes = getShapesFromJson(R.raw.shapes_mattapan);

        // Draw the route shapes
        drawRouteShapes(blueShapes, getResources().getColor(R.color.blue_line), 2);
        drawRouteShapes(orangeShapes, getResources().getColor(R.color.orange_line), 4);
        drawRouteShapes(redShapes, getResources().getColor(R.color.red_line), 5);
        drawRouteShapes(greenShapes, getResources().getColor(R.color.green_line), 3);
        drawRouteShapes(silverShapes, getResources().getColor(R.color.silver_line), 1);
        drawRouteShapes(mattapanShapes, getResources().getColor(R.color.red_line), 5);

        // Draw the stop markers
        HashMap<String, Stop> distinctStops = new HashMap<>();

        distinctStops.putAll(getStopsFromShapes(blueShapes));
        distinctStops.putAll(getStopsFromShapes(orangeShapes));
        distinctStops.putAll(getStopsFromShapes(redShapes));
        distinctStops.putAll(getStopsFromShapes(greenShapes));
        distinctStops.putAll(getStopsFromShapes(silverShapes));
        distinctStops.putAll(getStopsFromShapes(mattapanShapes));

        for (Stop s : distinctStops.values()) {
            stopMarkers.add(drawStopMarkers(s));
        }

        // Set the action listeners
        gMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                cameraIsMoving = true;

                if (reason == REASON_GESTURE) {
                    mapState = USER_HAS_MOVED_MAP;
                    mapTargetView.setVisibility(View.VISIBLE);
                }
            }
        });
        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (cameraIsMoving) {
                    cameraIsMoving = false;

                    // If the user has moved the map, then force a predictions update
                    if (mapState == USER_HAS_MOVED_MAP) {
                        targetLocation.setLatitude(gMap.getCameraPosition().target.latitude);
                        targetLocation.setLongitude(gMap.getCameraPosition().target.longitude);
                        swipeRefreshLayout.setRefreshing(true);
                        forceUpdate();

                    } else if (mapTargetView.getVisibility() == View.VISIBLE) {
                        mapTargetView.setVisibility(View.GONE);
                    }

                    if (gMap.getCameraPosition().zoom <= 12) {
                        for (Marker marker : stopMarkers) {
                            marker.setVisible(false);
                        }
                    } else {
                        for (Marker marker : stopMarkers) {
                            marker.setVisible(true);
                        }

                        if (selectedStopMarker != null) {
                            LatLng markerPosition = selectedStopMarker.getPosition();

                            Location markerLocation = new Location("");
                            markerLocation.setLatitude(markerPosition.latitude);
                            markerLocation.setLongitude(markerPosition.longitude);

                            if (markerLocation.distanceTo(targetLocation) > 200) {
                                selectedStopMarker.hideInfoWindow();
                                selectedStopMarker = null;
                            } else {
                                selectedStopMarker.showInfoWindow();
                            }
                        }
                    }
                }
            }
        });
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedStopMarker = marker;
                mapState = USER_HAS_MOVED_MAP;
                mapTargetView.setVisibility(View.VISIBLE);
                gMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)
                ));
                selectedStopMarker.showInfoWindow();

                return false;
            }
        });
        gMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                mapState = USER_HAS_NOT_MOVED_MAP;
                swipeRefreshLayout.setRefreshing(true);
                targetLocation = userLocation;
                gMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude())));
                forceUpdate();
            }
        });
        gMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mapState = USER_HAS_NOT_MOVED_MAP;
                swipeRefreshLayout.setRefreshing(true);
                targetLocation = userLocation;
                forceUpdate();

                return false;
            }
        });

        mapReady = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        // Get error manager
        errorManager = ErrorManager.getErrorManager();
        errorManager.registerOnErrorChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        // Get the time
        Long onResumeTime = new Date().getTime();

        // If too much time has elapsed since last refresh, then clear the predictions
        if (onResumeTime - refreshTime > MAX_PREDICTION_AGE) {
            clearPredictions();
        }

        swipeRefreshLayout.setRefreshing(true);

        locationClient.connect();

        timer = new Timer();
        timer.schedule(new LocationUpdateTimerTask(),
                0, LOCATION_UPDATE_RATE);
        timer.schedule(new PredictionsUpdateTimerTask(),
                PREDICTIONS_UPDATE_RATE, PREDICTIONS_UPDATE_RATE);

        boolean locationPermissionGranted = ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        staleLocation = mapState == USER_HAS_NOT_MOVED_MAP ||
                onResumeTime - onPauseTime > LOCATION_UPDATE_RESTART_TIME;

        if (locationPermissionGranted && staleLocation) {
            mapState = USER_HAS_NOT_MOVED_MAP;
            if (mapReady) {
                mapTargetView.setVisibility(View.GONE);
            }
        } else {
            forceUpdate();
            if (mapReady) {
                mapTargetView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        cameraIsMoving = false;

        refreshing = false;

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (timer != null) {
            timer.cancel();
        }

        locationClient.disconnect();

        onPauseTime = new Date().getTime();

        swipeRefreshLayout.setRefreshing(false);

        // Save the location the user last viewed
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("latitude", (float) targetLocation.getLatitude());
        editor.putFloat("longitude", (float) targetLocation.getLongitude());
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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
    public void onSuccess() {
        if (mapReady && !cameraIsMoving) {
            userLocation = locationClient.getLastLocation();

            if (mapState == USER_HAS_NOT_MOVED_MAP) {
                if (staleLocation) {
                    // If the user is far outside the MBTA's operating area, then center to Boston
                    if (userLocation.getLatitude() < 41.3 ||
                            userLocation.getLatitude() > 43.3 ||
                            userLocation.getLongitude() < -72.5 ||
                            userLocation.getLongitude() > -69.9) {
                        userLocation.setLatitude(42.3604);
                        userLocation.setLongitude(-71.0580);
                        mapTargetView.setVisibility(View.VISIBLE);
                        mapState = USER_HAS_MOVED_MAP;
                    }

                    gMap.moveCamera(CameraUpdateFactory.newLatLng(
                            new LatLng(userLocation.getLatitude(), userLocation.getLongitude())));
                    staleLocation = false;
                } else {
                    gMap.animateCamera(CameraUpdateFactory.newLatLng(
                            new LatLng(userLocation.getLatitude(), userLocation.getLongitude())));
                }

                if (targetLocation.distanceTo(userLocation) > 400) {
                    targetLocation = userLocation;
                    forceUpdate();
                } else {
                    targetLocation = userLocation;
                    backgroundUpdate();
                }
            }
        }

        errorManager.setLocationError(false);
        errorManager.setLocationPermissionDenied(false);
    }

    @Override
    public void onFailure() {
        errorManager.setLocationError(true);
    }

    @Override
    public void onNoPermission() {
        errorManager.setLocationPermissionDenied(true);
    }

    @Override
    public void onPostExecute(List<Route> routes) {
        refreshing = false;
        currentRoutes = new ArrayList<>(routes);
        refreshPredictions();
    }

    @Override
    public void onErrorChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorManager.hasLocationPermissionDenied() || errorManager.hasLocationError() ||
                        errorManager.hasNetworkError()) {
                    if (currentRoutes != null) {
                        currentRoutes.clear();
                        refreshPredictions();
                    }

                    if (mapReady) {
                        Log.i(LOG_TAG, "Error found");
                        UiSettings mapUiSettings = gMap.getUiSettings();
                        mapUiSettings.setMyLocationButtonEnabled(false);
                        mapTargetView.setVisibility(View.VISIBLE);
                    }

                } else if (mapReady && ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    UiSettings mapUiSettings = gMap.getUiSettings();
                    gMap.setMyLocationEnabled(true);
                    mapUiSettings.setMyLocationButtonEnabled(true);
                }
            }
        });
    }

    private void backgroundUpdate() {
        if (!refreshing && new Date().getTime() - refreshTime > PREDICTIONS_UPDATE_RATE) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        getPredictions();
    }

    private void getPredictions() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            refreshing = true;
            refreshTime = new Date().getTime();

            if (predictionsAsyncTask != null) {
                predictionsAsyncTask.cancel(true);
            }

            predictionsAsyncTask = new MapSearchPredictionsAsyncTask(
                    realTimeApiKey, targetLocation, this);
            predictionsAsyncTask.execute();
        } else {
            errorManager.setNetworkError(true);
            refreshing = false;
            swipeRefreshLayout.setRefreshing(false);
            if (currentRoutes != null) {
                currentRoutes.clear();
            }
        }
    }

    private void refreshPredictions() {
        if (!userIsScrolling && currentRoutes != null) {
            recyclerViewAdapter.setRoutes(currentRoutes);
            swipeRefreshLayout.setRefreshing(false);

            if (recyclerViewAdapter.getItemCount() == 0) {
                if (currentRoutes.size() == 0) {
                    noPredictionsTextView.setText(getResources().getString(R.string.no_nearby_services));
                } else {
                    noPredictionsTextView.setText(getResources().getString(R.string.no_nearby_predictions));
                }

                noPredictionsTextView.setVisibility(View.VISIBLE);
                appBarLayout.setExpanded(true);
                recyclerView.setNestedScrollingEnabled(false);
            } else {
                noPredictionsTextView.setVisibility(View.GONE);
                recyclerView.setNestedScrollingEnabled(true);
            }
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        noPredictionsTextView.setVisibility(View.GONE);
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private Shape[] getShapesFromJson(int jsonFile) {
        return ShapesJsonParser.parse(RawResourceReader.toString(getResources().openRawResource(jsonFile)));
    }

    private HashMap<String, Stop> getStopsFromShapes(Shape[] shapes) {
        HashMap<String, Stop> stops = new HashMap<>();

        for (Shape shape : shapes) {
            for (Stop stop : shape.getStops()) {
                stops.put(stop.getId(), stop);
            }
        }

        return stops;
    }

    private void drawRouteShapes(Shape[] shapes, int color, int zIndex) {
        for (Shape s : shapes) {
            List<LatLng> coordinates = PolyUtil.decode(s.getPolyline());

            // Draw white background/line padding
            gMap.addPolyline(new PolylineOptions()
                    .addAll(coordinates)
                    .color(Color.parseColor("#FFFFFF"))
                    .zIndex(0)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .width(14));

            // Draw primary polyline
            gMap.addPolyline(new PolylineOptions()
                    .addAll(coordinates)
                    .color(color)
                    .zIndex(zIndex)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .width(8));
        }
    }

    private Marker drawStopMarkers(Stop stop) {
        Marker stopMarker = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        stop.getLocation().getLatitude(), stop.getLocation().getLongitude()))
                .anchor(0.5f, 0.5f)
                .title(stop.getName())
                .zIndex(300)
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mbta_stop_icon)));

        stopMarker.setTag(stop);

        return stopMarker;
    }

    private class LocationUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            locationClient.updateLocation(MapSearchFragment.this);
        }
    }

    private class PredictionsUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            backgroundUpdate();
        }
    }
}
