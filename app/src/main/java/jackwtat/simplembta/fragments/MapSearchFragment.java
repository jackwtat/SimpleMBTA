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
import jackwtat.simplembta.asyncTasks.PredictionsByLocationAsyncTask;
import jackwtat.simplembta.asyncTasks.RoutesByStopsAsyncTask;
import jackwtat.simplembta.asyncTasks.SchedulesAsyncTask;
import jackwtat.simplembta.asyncTasks.ServiceAlertsAsyncTask;
import jackwtat.simplembta.asyncTasks.StopsByLocationAsyncTask;
import jackwtat.simplembta.clients.LocationClient;
import jackwtat.simplembta.clients.LocationClient.LocationClientCallbacks;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.map.markers.StopMarkerFactory;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.CommuterRail;
import jackwtat.simplembta.model.routes.CommuterRailNorthSide;
import jackwtat.simplembta.model.routes.CommuterRailOldColony;
import jackwtat.simplembta.model.routes.CommuterRailSouthSide;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.routes.SilverLineCombined;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.R;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;

public class MapSearchFragment extends Fragment implements OnMapReadyCallback,
        StopsByLocationAsyncTask.OnPostExecuteListener,
        RoutesByStopsAsyncTask.OnPostExecuteListener,
        PredictionsByLocationAsyncTask.OnPostExecuteListener,
        SchedulesAsyncTask.OnPostExecuteListener,
        ServiceAlertsAsyncTask.OnPostExecuteListener,
        LocationClientCallbacks,
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
    public static final int DEFAULT_MAP_ZOOM_LEVEL = 16;

    // Zoom level where stop markers become visible
    public static final int STOP_MARKER_VISIBILITY_LEVEL = 15;

    // Zoom level where key stop markers become visible
    public static final int KEY_STOP_MARKER_VISIBILITY_LEVEL = 12;

    // Distance in meters from last target location before visible refresh
    public static final int DISTANCE_TO_FORCE_REFRESH = 200;

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
    private MapSearchRecyclerViewAdapter recyclerViewAdapter;
    private ErrorManager errorManager;
    private Timer timer;

    private StopsByLocationAsyncTask stopsAsyncTask;
    private RoutesByStopsAsyncTask routesAsyncTask;
    private PredictionsByLocationAsyncTask predictionsAsyncTask;
    private SchedulesAsyncTask schedulesAsyncTask;
    private ServiceAlertsAsyncTask serviceAlertsAsyncTask;

    private boolean refreshing = false;
    private boolean mapReady = false;
    private boolean cameraIsMoving = false;
    private boolean userIsScrolling = false;
    private boolean staleLocation = true;
    private int mapState = USER_HAS_NOT_MOVED_MAP;
    private long refreshTime = 0;
    private long onPauseTime = 0;

    private HashMap<String, Stop> currentStops = new HashMap<>();
    private HashMap<String, Route> currentRoutes = new HashMap<>();

    private Location userLocation = new Location("");
    private Location targetLocation = new Location("");

    private Marker selectedStopMarker = null;
    private HashMap<String, Marker> stopMarkers = new HashMap<>();
    private HashMap<String, Marker> keyStopMarkers = new HashMap<>();
    private Route[] keyRoutes = {
            new BlueLine("Blue"),
            new OrangeLine("Orange"),
            new RedLine("Red"),
            new RedLine("Mattapan"),
            new GreenLineCombined(),
            new SilverLineCombined()};

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

        // Add the key (permanent) routes shapes
        Shape[][] shapes = {
                getShapesFromJson(R.raw.shapes_blue),
                getShapesFromJson(R.raw.shapes_orange),
                getShapesFromJson(R.raw.shapes_red),
                getShapesFromJson(R.raw.shapes_green_combined),
                getShapesFromJson(R.raw.shapes_silver),
                getShapesFromJson(R.raw.shapes_mattapan)};

        for (Shape[] s : shapes) {
            for (Route r : keyRoutes) {
                if (r.equals(s[0].getRouteId())) {
                    r.setShapes(s);
                    break;
                }
            }
        }
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
        mapView.getLayoutParams().height =
                (int) (getResources().getDisplayMetrics().heightPixels * .6);
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

                    if (!refreshing)
                        refreshPredictionViews();
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
        mapReady = true;

        // Move the map camera to the last known location
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude()),
                DEFAULT_MAP_ZOOM_LEVEL));

        // Set the map style
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        // Set the map UI settings
        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        // Enable map location UI features
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            mapUiSettings.setMyLocationButtonEnabled(true);
        }

        // Draw the key routes
        for (int i = 0; i < keyRoutes.length; i++) {
            drawRouteShapes(keyRoutes[i], keyRoutes.length - i);
            drawKeyStopMarkers(keyRoutes[i]);
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
                        Location newLocation = new Location("");

                        newLocation.setLatitude(gMap.getCameraPosition().target.latitude);
                        newLocation.setLongitude(gMap.getCameraPosition().target.longitude);

                        if (newLocation.distanceTo(targetLocation) > DISTANCE_TO_FORCE_REFRESH) {
                            targetLocation.setLatitude(newLocation.getLatitude());
                            targetLocation.setLongitude(newLocation.getLongitude());

                            cancelUpdate();

                            swipeRefreshLayout.setRefreshing(true);

                            forceUpdate();
                        }
                    } else if (mapTargetView.getVisibility() == View.VISIBLE) {
                        mapTargetView.setVisibility(View.GONE);
                    }

                    if (gMap.getCameraPosition().zoom >= KEY_STOP_MARKER_VISIBILITY_LEVEL) {
                        for (Marker marker : keyStopMarkers.values()) {
                            marker.setVisible(true);
                        }

                        if (selectedStopMarker != null) {
                            LatLng markerPosition = selectedStopMarker.getPosition();

                            Location markerLocation = new Location("");
                            markerLocation.setLatitude(markerPosition.latitude);
                            markerLocation.setLongitude(markerPosition.longitude);

                            if (markerLocation.distanceTo(targetLocation) > DISTANCE_TO_FORCE_REFRESH) {
                                selectedStopMarker.hideInfoWindow();
                                selectedStopMarker = null;
                            } else {
                                selectedStopMarker.showInfoWindow();
                            }
                        }
                    } else {
                        for (Marker marker : keyStopMarkers.values()) {
                            marker.setVisible(false);
                        }
                    }

                    for (Marker marker : stopMarkers.values()) {
                        marker.setVisible(
                                gMap.getCameraPosition().zoom >= STOP_MARKER_VISIBILITY_LEVEL);
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
        mapView.onResume();
        super.onResume();

        // Get the time
        Long onResumeTime = new Date().getTime();

        // If too much time has elapsed since last refresh, then clear the predictions
        if (onResumeTime - refreshTime > MAX_PREDICTION_AGE) {
            clearPredictions();
        }

        swipeRefreshLayout.setRefreshing(true);

        locationClient.connect();

        timer = new Timer();
        timer.schedule(new PredictionsUpdateTimerTask(), PREDICTIONS_UPDATE_RATE, PREDICTIONS_UPDATE_RATE);
        timer.schedule(new LocationUpdateTimerTask(), 0, LOCATION_UPDATE_RATE);

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

        cancelUpdate();

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

                if (targetLocation.distanceTo(userLocation) > DISTANCE_TO_FORCE_REFRESH) {
                    targetLocation = userLocation;
                    swipeRefreshLayout.setRefreshing(true);
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
    public void onErrorChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorManager.hasLocationPermissionDenied() || errorManager.hasLocationError() ||
                        errorManager.hasNetworkError()) {
                    if (currentRoutes != null) {
                        currentRoutes.clear();
                        forceUpdate();
                    }

                    if (mapReady) {
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
            update();
        }
    }

    private void forceUpdate() {
        update();
    }

    private void update() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            refreshing = true;

            refreshTime = new Date().getTime();

            getStops();
        } else {
            errorManager.setNetworkError(true);

            refreshing = false;

            swipeRefreshLayout.setRefreshing(false);

            if (currentRoutes != null)
                currentRoutes.clear();
        }
    }

    private void cancelUpdate() {
        if (stopsAsyncTask != null)
            stopsAsyncTask.cancel(true);

        if (routesAsyncTask != null)
            routesAsyncTask.cancel(true);

        if (predictionsAsyncTask != null)
            predictionsAsyncTask.cancel(true);

        if (schedulesAsyncTask != null)
            schedulesAsyncTask.cancel(true);

        if (serviceAlertsAsyncTask != null)
            serviceAlertsAsyncTask.cancel(true);
    }

    private void getStops() {
        if (stopsAsyncTask != null)
            stopsAsyncTask.cancel(true);

        stopsAsyncTask = new StopsByLocationAsyncTask(realTimeApiKey, targetLocation, this);

        stopsAsyncTask.execute();
    }

    private void getRoutes() {
        if (routesAsyncTask != null)
            routesAsyncTask.cancel(true);

        String[] stopIds = currentStops.keySet().toArray(new String[currentStops.size()]);

        routesAsyncTask = new RoutesByStopsAsyncTask(realTimeApiKey, stopIds, this);

        routesAsyncTask.execute();
    }

    private void getPredictions() {
        if (predictionsAsyncTask != null)
            predictionsAsyncTask.cancel(true);

        predictionsAsyncTask = new PredictionsByLocationAsyncTask(realTimeApiKey, targetLocation,
                this);

        predictionsAsyncTask.execute();
    }

    private void getSchedules() {
        if (schedulesAsyncTask != null)
            schedulesAsyncTask.cancel(true);

        ArrayList<String> routeIds = new ArrayList<>();

        // We'll only query routes that don't already have live pick-ups in this direction
        // Light rail and heavy rail (Green, Red, Blue, and Orange Lines) on-time performances
        // are too erratic and unreliable for scheduled predictions to be reliable
        for (Route route : currentRoutes.values()) {
            if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
                routeIds.add(route.getId());
            }
        }

        String[] stopIds = currentStops.keySet().toArray(new String[currentStops.size()]);

        schedulesAsyncTask = new SchedulesAsyncTask(realTimeApiKey,
                routeIds.toArray(new String[routeIds.size()]), stopIds, this);

        schedulesAsyncTask.execute();
    }

    private void getServiceAlerts() {
        if (serviceAlertsAsyncTask != null)
            serviceAlertsAsyncTask.cancel(true);

        serviceAlertsAsyncTask = new ServiceAlertsAsyncTask(realTimeApiKey,
                currentRoutes.keySet().toArray(new String[currentRoutes.size()]),
                this);

        serviceAlertsAsyncTask.execute();
    }

    @Override
    public void onPostExecute(Stop[] stops) {
        currentStops.clear();

        for (Stop stop : stops) {
            currentStops.put(stop.getId(), stop);

        }

        // Draw stop markers
        String[] stopMarkerIds = stopMarkers.keySet().toArray(new String[stopMarkers.size()]);
        String selectedStopId = "";
        if (selectedStopMarker != null)
            selectedStopId = ((Stop) selectedStopMarker.getTag()).getId();

        for (String stopId : stopMarkerIds) {
            if (!currentStops.containsKey(stopId) && !stopId.equals(selectedStopId)) {
                stopMarkers.get(stopId).remove();
                stopMarkers.remove(stopId);
            }
        }

        for (Stop stop : currentStops.values()) {
            if (!stopMarkers.containsKey(stop.getId()) && !keyStopMarkers.containsKey(stop.getId())) {
                stopMarkers.put(stop.getId(), drawStopMarker(stop));
            }
        }

        getRoutes();
    }

    @Override
    public void onPostExecute(Route[] routes) {
        currentRoutes.clear();

        for (Route route : routes) {
            currentRoutes.put(route.getId(), route);
        }

        getPredictions();
        getServiceAlerts();
    }

    @Override
    public void onPostExecute(Prediction[] predictions, boolean live) {
        for (Prediction prediction : predictions) {
            // Replace prediction's stop ID with its parent stop ID
            if (currentStops.containsKey(prediction.getParentStopId())) {
                prediction.setStop(currentStops.get(prediction.getParentStopId()));
            }

            // If the prediction is for the eastbound Green Line, then replace the route
            // with the Green Line Grouped route. This is to reduce the maximum number of
            // prediction cards displayed and reduces UI clutter.
            if (prediction.getRoute().getMode() == Route.LIGHT_RAIL &&
                    prediction.getDirection() == Direction.EASTBOUND &&
                    GreenLine.isGreenLineSubwayStop(prediction.getStopId())) {
                prediction.setRoute(new GreenLineCombined());
            }

            // If the prediction is for the inbound Commuter Rail, then replace the route
            // with the Commuter Rail Grouped route. This is to reduce the maximum number
            // of prediction cards displayed and reduces UI clutter.
            if (prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                    prediction.getDirection() == Direction.INBOUND &&
                    CommuterRail.isCommuterRailHub(prediction.getStopId(), false)) {

                if (CommuterRailNorthSide.isNorthSideCommuterRail(prediction.getRoute().getId())) {
                    prediction.setRoute(new CommuterRailNorthSide());

                } else if (CommuterRailSouthSide.isSouthSideCommuterRail(prediction.getRoute().getId())) {
                    prediction.setRoute(new CommuterRailSouthSide());

                } else if (CommuterRailOldColony.isOldColonyCommuterRail(prediction.getRoute().getId())) {
                    prediction.setRoute(new CommuterRailOldColony());
                }
            }

            // Add route to routes list if not already there
            if (!currentRoutes.containsKey(prediction.getRouteId())) {
                currentRoutes.put(prediction.getRouteId(), prediction.getRoute());
            }

            // Add stop to stops list if not already there
            if (!currentStops.containsKey(prediction.getStopId())) {
                currentStops.put(prediction.getStopId(), prediction.getStop());
            }

            // Add prediction to its respective route
            int direction = prediction.getDirection();
            String routeId = prediction.getRouteId();
            Stop stop = currentStops.get(prediction.getStopId());

            // If this prediction's stop is the route's nearest stop
            if (stop.equals(currentRoutes.get(routeId).getNearestStop(direction))) {
                currentRoutes.get(routeId).addPrediction(prediction);

                // If route does not have predictions in this prediction's direction
            } else if (!currentRoutes.get(routeId).hasPredictions(direction)) {
                currentRoutes.get(routeId).setNearestStop(direction, stop);
                currentRoutes.get(routeId).addPrediction(prediction);

                // If this prediction's stop is closer than route's current nearest stop
            } else if (stop.getLocation().distanceTo(targetLocation) <
                    currentRoutes.get(routeId).getNearestStop(direction).getLocation()
                            .distanceTo(targetLocation)
                    && prediction.willPickUpPassengers()) {
                currentRoutes.get(routeId).setNearestStop(direction, stop);
                currentRoutes.get(routeId).addPrediction(prediction);
            }
        }

        if (live) {
            getSchedules();

        } else {
            refreshing = false;

            refreshPredictionViews();
        }
    }

    @Override
    public void onPostExecute(ServiceAlert[] serviceAlerts) {
        for (ServiceAlert alert : serviceAlerts) {
            for (Route route : currentRoutes.values()) {
                if (alert.affectsMode(route.getMode())) {
                    route.addServiceAlert(alert);
                } else {
                    for (String affectedRouteId : alert.getAffectedRoutes()) {
                        if (route.equals(affectedRouteId)) {
                            route.addServiceAlert(alert);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void refreshPredictionViews() {
        if (!userIsScrolling && currentRoutes != null) {
            recyclerViewAdapter.setData(targetLocation, currentRoutes.values().toArray(new Route[currentRoutes.size()]));
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

    private void drawRouteShapes(Route route, int zIndex) {
        for (Shape s : route.getShapes(0)) {

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
                    .color(Color.parseColor(route.getPrimaryColor()))
                    .zIndex(zIndex)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .width(8));
        }
    }

    private void drawKeyStopMarkers(Route route) {
        HashMap<String, Marker> markers = new HashMap<>();

        for (Shape shape : route.getAllShapes()) {
            for (Stop stop : shape.getStops()) {
                if (keyStopMarkers.containsKey(stop.getId())) {
                    keyStopMarkers.get(stop.getId()).setIcon(
                            BitmapDescriptorFactory.fromResource(R.drawable.icon_stop));
                } else if (!markers.containsKey(stop.getId())) {
                    Marker stopMarker = gMap.addMarker(route.getStopMarkerOptions()
                            .position(new LatLng(
                                    stop.getLocation().getLatitude(),
                                    stop.getLocation().getLongitude()))
                            .zIndex(20)
                            .title(stop.getName()));

                    stopMarker.setTag(stop);

                    markers.put(stop.getId(), stopMarker);
                }
            }
        }

        keyStopMarkers.putAll(markers);
    }


    private Marker drawStopMarker(Stop stop) {
        Marker stopMarker = gMap.addMarker(new StopMarkerFactory().createMarkerOptions()
                .position(new LatLng(
                        stop.getLocation().getLatitude(),
                        stop.getLocation().getLongitude()))
                .zIndex(20)
                .title(stop.getName()));

        stopMarker.setTag(stop);

        stopMarker.setVisible(gMap.getCameraPosition().zoom >= STOP_MARKER_VISIBILITY_LEVEL);

        return stopMarker;
    }

    private class LocationUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            if (mapReady) {
                locationClient.updateLocation(MapSearchFragment.this);
            }
        }
    }

    private class PredictionsUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            backgroundUpdate();
        }
    }
}
