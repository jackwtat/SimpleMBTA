package jackwtat.simplembta.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.RouteSearchRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.RouteSearchPredictionsAsyncTask;
import jackwtat.simplembta.asyncTasks.ShapesAsyncTask;
import jackwtat.simplembta.asyncTasks.VehiclesByRouteAsyncTask;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.routes.SilverLine;
import jackwtat.simplembta.utilities.DisplayNameUtil;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.views.RouteDetailSpinners;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        ErrorManager.OnErrorChangedListener, RouteDetailSpinners.OnDirectionSelectedListener,
        RouteDetailSpinners.OnStopSelectedListener {
    public static final String LOG_TAG = "RouteDetailActivity";

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    // Vehicle locations auto update rate
    public static final long VEHICLES_UPDATE_RATE = 5000;

    // Maximum age of prediction
    public static final long MAXIMUM_PREDICTION_AGE = 90000;

    // Default level of zoom for the map
    public static final int DEFAULT_MAP_ZOOM_LEVEL = 13;

    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar mapProgressBar;
    private TextView errorTextView;
    private RouteDetailSpinners routeDetailSpinners;

    private String realTimeApiKey;
    private RouteSearchPredictionsAsyncTask predictionsAsyncTask;
    private ShapesAsyncTask shapesAsyncTask;
    private VehiclesByRouteAsyncTask vehiclesAsyncTask;
    private NetworkConnectivityClient networkConnectivityClient;
    private ErrorManager errorManager;
    private RouteSearchRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private boolean refreshing = false;
    private boolean mapReady = false;
    private boolean shapesLoaded = false;
    private boolean mapCameraIsMoving = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private Location userLocation = new Location("userLocation");
    private Route selectedRoute;
    private int selectedDirectionId;
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private HashMap<String, Marker> stopMarkers = new HashMap<>();
    private HashMap<String, Marker> vehicleMarkers = new HashMap<>();
    private Marker selectedStopMarker;
    private Marker selectedVehicleMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        // Get MBTA realTime API key
        realTimeApiKey = getResources().getString(R.string.v3_mbta_realtime_api_key);

        // Get data saved from previous session
        if (savedInstanceState != null) {
            selectedRoute = (Route) savedInstanceState.getSerializable("route");
            selectedDirectionId = savedInstanceState.getInt("direction");
            refreshTime = savedInstanceState.getLong("refreshTime");
            userLocation.setLatitude(0);
            userLocation.setLongitude(0);

            if (new Date().getTime() - refreshTime > MAXIMUM_PREDICTION_AGE) {
                for (Direction d : selectedRoute.getAllDirections()) {
                    selectedRoute.clearPredictions(d.getId());
                }
            }

            // Get values passed from calling activity/fragment
        } else {
            Intent intent = getIntent();
            selectedRoute = (Route) intent.getSerializableExtra("route");
            selectedDirectionId = intent.getIntExtra("direction", Direction.NULL_DIRECTION);
            refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);

            userLocation.setLatitude(intent.getDoubleExtra("userLat", 0));
            userLocation.setLongitude(intent.getDoubleExtra("userLon", 0));
        }

        // Get network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(this);

        // Set action bar
        setTitle(DisplayNameUtil.getLongDisplayName(this, selectedRoute));

        if (selectedRoute.getMode() != Route.BUS || SilverLine.isSilverLine(selectedRoute.getId())) {
            if (Build.VERSION.SDK_INT >= 21) {
                // Create color for status bar
                float[] hsv = new float[3];
                Color.colorToHSV(Color.parseColor(selectedRoute.getPrimaryColor()), hsv);
                hsv[2] *= .8f;

                // Set status bar color
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.HSVToColor(hsv));

                // Set action bar background color
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setBackgroundDrawable(
                            new ColorDrawable(Color.parseColor(selectedRoute.getPrimaryColor())));
                }
            }
        }

        // Get error text view
        errorTextView = findViewById(R.id.error_message_text_view);

        // Get app bar and app bar params
        appBarLayout = findViewById(R.id.app_bar_layout);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        params.height = (int) (getResources().getDisplayMetrics().heightPixels * .6);

        // Disable scrolling inside app bar
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        // Get and initialize map view
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get map progress bar
        mapProgressBar = findViewById(R.id.map_progress_bar);

        // Get the stop selector view
        routeDetailSpinners = findViewById(R.id.route_detail_spinners);
        populateDirectionSpinner(selectedRoute.getAllDirections());

        // Populate the stops spinner with the nearest stop until we query the shapes
        if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
            Stop[] selectedStopArray = {selectedRoute.getNearestStop(selectedDirectionId)};
            populateStopSpinner(selectedStopArray);
        }

        routeDetailSpinners.setOnDirectionSelectedListener(this);
        routeDetailSpinners.setOnStopSelectedListener(this);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Get recycler view
        recyclerView = findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Enable nested scrolling
        recyclerView.setNestedScrollingEnabled(true);

        // Add on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    refreshPredictions(false);
                    if (!shapesLoaded) {
                        refreshShapes();
                    }

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new RouteSearchRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);

        // Set OnClickListener
        recyclerViewAdapter.setOnItemClickListener(new RouteSearchRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Vehicle vehicle = recyclerViewAdapter.getPrediction(position).getVehicle();

                if (vehicle != null) {
                    Marker marker = vehicleMarkers.get(vehicle.getId());

                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

        // Move the map camera to the selected stop
        Stop stop = selectedRoute.getNearestStop(selectedDirectionId);
        LatLng latLng = (stop == null)
                ? new LatLng(userLocation.getLatitude(), userLocation.getLongitude())
                : new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM_LEVEL));

        // Set the map style
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Set the map UI settings
        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        // Enable map location UI features
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            mapUiSettings.setMyLocationButtonEnabled(true);
        }

        // Set the action listeners
        gMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                mapCameraIsMoving = true;
            }
        });
        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (mapCameraIsMoving) {
                    mapCameraIsMoving = false;
                }
            }
        });
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() instanceof Stop) {
                    Stop selectedStop = (Stop) marker.getTag();

                    routeDetailSpinners.selectStop(selectedStop.getId());

                } else if (marker.getTag() instanceof Vehicle) {
                    if (selectedVehicleMarker != null)
                        selectedVehicleMarker.hideInfoWindow();

                    selectedVehicleMarker = marker;

                    selectedVehicleMarker.showInfoWindow();
                }

                return false;
            }
        });
        gMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                gMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(location.getLatitude(), location.getLongitude())));
            }
        });

        // Load route shapes
        getShapes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

        errorManager = ErrorManager.getErrorManager();
        errorManager.registerOnErrorChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        // Refresh the activity to update UI so that the predictions are accurate
        // as of the last update
        refreshPredictions(false);
        refreshVehicles();

        // Get the route shapes if there aren't any
        if (polylines.size() == 0 && mapReady) {
            getShapes();
        }

        // If too much time has elapsed since last refresh, then clear predictions and force update
        if (new Date().getTime() - refreshTime > MAXIMUM_PREDICTION_AGE) {
            clearPredictions();
            swipeRefreshLayout.setRefreshing(true);
            forceUpdate();

            // If there are no predictions displayed in the recycler view, then force a refresh
        } else if (recyclerViewAdapter.getItemCount() < 1) {
            swipeRefreshLayout.setRefreshing(true);
            forceUpdate();

            // Otherwise, background update
        } else {
            backgroundUpdate();
        }

        timer = new Timer();
        timer.schedule(new PredictionsUpdateTimerTask(), 0, PREDICTIONS_UPDATE_RATE);
        timer.schedule(new VehiclesUpdateTimerTask(), 0, VEHICLES_UPDATE_RATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        refreshing = false;

        swipeRefreshLayout.setRefreshing(false);

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        if (vehiclesAsyncTask != null) {
            vehiclesAsyncTask.cancel(true);
        }

        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("route", selectedRoute);
        outState.putInt("direction", selectedDirectionId);
        outState.putLong("refreshTime", refreshTime);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onErrorChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorTextView.setOnClickListener(null);

                if (errorManager.hasNetworkError()) {
                    errorTextView.setText(R.string.network_error_text);
                    errorTextView.setVisibility(View.VISIBLE);

                    selectedRoute.clearPredictions(Direction.INBOUND);
                    selectedRoute.clearPredictions(Direction.OUTBOUND);

                    clearVehicleMarkers();

                    refreshPredictions(true);
                    refreshVehicles();

                } else if (!errorManager.hasNetworkError()) {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getPredictions() {
        if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
            if (networkConnectivityClient.isConnected()) {
                errorManager.setNetworkError(false);

                refreshing = true;

                if (predictionsAsyncTask != null) {
                    predictionsAsyncTask.cancel(true);
                }

                predictionsAsyncTask = new RouteSearchPredictionsAsyncTask(realTimeApiKey, selectedRoute,
                        selectedDirectionId, new PredictionsPostExecuteListener());
                predictionsAsyncTask.execute();

            } else {
                errorManager.setNetworkError(true);
                refreshing = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshPredictions(true);
                }
            });
        }
    }

    private void getShapes() {
        // Hard coding to save the user time and data
        if (selectedRoute.getMode() == Route.HEAVY_RAIL || selectedRoute.getMode() == Route.LIGHT_RAIL) {
            if (BlueLine.isBlueLine(selectedRoute.getId())) {
                selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_blue));

            } else if (OrangeLine.isOrangeLine(selectedRoute.getId())) {
                selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_orange));

            } else if (RedLine.isRedLine(selectedRoute.getId()) && !RedLine.isMattapanLine(selectedRoute.getId())) {
                selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_red));

            } else if (RedLine.isRedLine(selectedRoute.getId()) && RedLine.isMattapanLine(selectedRoute.getId())) {
                selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_mattapan));

            } else if (GreenLine.isGreenLine(selectedRoute.getId())) {
                if (GreenLineCombined.isGreenLineCombined(selectedRoute.getId())) {
                    selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_green_combined));
                } else if (GreenLine.isGreenLineB(selectedRoute.getId())) {
                    selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_green_b));

                } else if (GreenLine.isGreenLineC(selectedRoute.getId())) {
                    selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_green_c));

                } else if (GreenLine.isGreenLineD(selectedRoute.getId())) {
                    selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_green_d));

                } else if (GreenLine.isGreenLineE(selectedRoute.getId())) {
                    selectedRoute.addShapes(getShapesFromJson(R.raw.shapes_green_e));

                }
            }

            refreshShapes();
            populateStopSpinner(selectedRoute.getStops(selectedDirectionId));

        } else if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (shapesAsyncTask != null) {
                shapesAsyncTask.cancel(true);
            }

            shapesAsyncTask = new ShapesAsyncTask(
                    realTimeApiKey, selectedRoute.getId(), new ShapesPostExecuteListener());
            shapesAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void getVehicles() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (vehiclesAsyncTask != null) {
                vehiclesAsyncTask.cancel(true);
            }

            vehiclesAsyncTask = new VehiclesByRouteAsyncTask(
                    realTimeApiKey, selectedRoute.getId(), new VehiclesPostExecuteListener());
            vehiclesAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void refreshPredictions(boolean returnToTop) {
        if (!userIsScrolling) {
            if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
                recyclerViewAdapter.setPredictions(selectedRoute.getPredictions(selectedDirectionId));
                swipeRefreshLayout.setRefreshing(false);

                if (returnToTop) {
                    recyclerView.scrollToPosition(0);
                }
            }
        }
    }

    private void refreshShapes() {
        if (!userIsScrolling) {
            clearShapes();

            Stop selectedStop = selectedRoute.getNearestStop(selectedDirectionId);

            for (Shape shape : selectedRoute.getShapes(selectedDirectionId)) {
                if (shape.getPriority() >= 0 && shape.getStops().length > 0) {
                    // Draw the polyline
                    polylines.addAll(Arrays.asList(drawPolyline(shape)));

                    // Draw each stop that hasn't been drawn yet
                    for (Stop stop : shape.getStops()) {
                        if (!stopMarkers.containsKey(stop.getId())) {
                            // Draw the stop marker
                            Marker currentMarker = drawStopMarker(stop);

                            // Use selected stop marker if this stop is the selected stop
                            if (selectedStop != null && selectedStopMarker == null &&
                                    (selectedStop.equals(stop) ||
                                            selectedStop.isParentOf(stop.getId()) ||
                                            stop.isParentOf(selectedStop.getId()))) {
                                selectedStopMarker = currentMarker;
                                selectedStopMarker.setIcon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.icon_selected_stop));
                            }

                            // Add stop to the drawn stops hash map
                            stopMarkers.put(stop.getId(), currentMarker);
                        }
                    }
                }
            }

            // If the selected stop is not a stop included in the shape objects
            if (selectedStopMarker == null && selectedStop != null) {
                selectedStopMarker = drawStopMarker(selectedStop);
                selectedStopMarker.setIcon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_selected_stop));
                stopMarkers.put(selectedStop.getId(), selectedStopMarker);
            }

            mapProgressBar.setVisibility(View.GONE);
            shapesLoaded = true;
        }
    }

    private void refreshVehicles() {
        if (!userIsScrolling && mapReady) {
            ArrayList<String> trackedVehicleIds = new ArrayList<>();
            ArrayList<String> expiredVehicleIds = new ArrayList<>();

            // Get all vehicles moving in selected direction
            for (Vehicle vehicle : selectedRoute.getAllVehicles()) {
                trackedVehicleIds.add(vehicle.getId());
            }

            // Find the currently displayed vehicles that are no longer being tracked/now expired
            for (String vehicleId : vehicleMarkers.keySet()) {
                if (!trackedVehicleIds.contains(vehicleId))
                    expiredVehicleIds.add(vehicleId);
            }

            // Removed the expired vehicles
            for (String vehicleId : expiredVehicleIds) {
                vehicleMarkers.get(vehicleId).remove();
                vehicleMarkers.remove(vehicleId);
            }

            for (Vehicle vehicle : selectedRoute.getAllVehicles()) {
                Marker vMarker = vehicleMarkers.get(vehicle.getId());
                if (vMarker != null) {
                    vMarker.setPosition(new LatLng(
                            vehicle.getLocation().getLatitude(),
                            vehicle.getLocation().getLongitude()));
                    vMarker.setRotation(vehicle.getLocation().getBearing());

                    if (vehicle.getDestination() != null) {
                        vMarker.setSnippet("To " + vehicle.getDestination());
                    }
                } else {
                    String vehicleTitle;
                    if (selectedRoute.getMode() == Route.COMMUTER_RAIL) {
                        vehicleTitle = getResources().getString(R.string.train) + " " + vehicle.getTripName();

                    } else {
                        vehicleTitle = getResources().getString(R.string.vehicle) + " " + vehicle.getLabel();
                    }
                    vehicleMarkers.put(vehicle.getId(), drawVehicleMarker(vehicle, vehicleTitle));
                }
            }
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        appBarLayout.setExpanded(true);
    }

    private void clearShapes() {
        clearPolylines();
        clearStopMarkers();
    }

    private void clearPolylines() {
        for (Polyline pl : polylines) {
            pl.remove();
        }
        polylines.clear();
    }

    private void clearStopMarkers() {
        for (Marker m : stopMarkers.values()) {
            m.remove();
        }
        stopMarkers.clear();

        if (selectedStopMarker != null) {
            selectedStopMarker.remove();
            selectedStopMarker = null;
        }
    }

    private void clearVehicleMarkers() {
        for (Marker vm : vehicleMarkers.values()) {
            vm.remove();
        }
        vehicleMarkers.clear();
    }

    private Shape[] getShapesFromJson(int jsonFile) {
        return ShapesJsonParser.parse(RawResourceReader.toString(getResources().openRawResource(jsonFile)));
    }

    private Polyline[] drawPolyline(@NonNull Shape shape) {
        List<LatLng> shapeCoordinates = PolyUtil.decode(shape.getPolyline());

        Polyline[] polylines = {
                gMap.addPolyline(new PolylineOptions()
                        .addAll(shapeCoordinates)
                        .color(Color.parseColor(selectedRoute.getPrimaryColor()))
                        .zIndex(1)
                        .jointType(JointType.ROUND)
                        .startCap(new RoundCap())
                        .endCap(new RoundCap())
                        .width(8)),

                gMap.addPolyline(new PolylineOptions()
                        .addAll(shapeCoordinates)
                        .color(Color.parseColor("#FFFFFF"))
                        .zIndex(0)
                        .jointType(JointType.ROUND)
                        .startCap(new RoundCap())
                        .endCap(new RoundCap())
                        .width(14))};

        return polylines;
    }

    private Marker drawStopMarker(@NonNull Stop stop) {
        MarkerOptions markerOptions = selectedRoute.getStopMarkerOptions();

        markerOptions.position(new LatLng(
                stop.getLocation().getLatitude(), stop.getLocation().getLongitude()));
        markerOptions.zIndex(10);
        markerOptions.title(stop.getName());

        Marker stopMarker = gMap.addMarker(markerOptions);

        stopMarker.setTag(stop);

        return stopMarker;
    }

    private Marker drawVehicleMarker(@NonNull Vehicle vehicle, String vehicleTitle) {
        Marker vehicleMarker = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        vehicle.getLocation().getLatitude(), vehicle.getLocation().getLongitude()))
                .rotation(vehicle.getLocation().getBearing())
                .anchor(0.5f, 0.5f)
                .zIndex(20)
                .flat(true)
                .title(vehicleTitle)
                //.title(getResources().getString(R.string.vehicle) + " " + vehicle.getLabel())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_vehicle))
        );

        if (vehicle.getDestination() != null) {
            vehicleMarker.setSnippet("To " + vehicle.getDestination());
        }

        vehicleMarker.setTag(vehicle);

        return vehicleMarker;
    }

    private void populateDirectionSpinner(Direction[] directions) {
        if (directions[0].getId() == Direction.SOUTHBOUND) {
            Direction d = directions[0];
            directions[0] = directions[1];
            directions[1] = d;
        }

        routeDetailSpinners.populateDirectionSpinner(directions);
        routeDetailSpinners.selectDirection(selectedDirectionId);
    }

    private void populateStopSpinner(Stop[] stops) {
        ArrayList<Stop> stopsList = new ArrayList<>(Arrays.asList(stops));
        Stop selectedStop = selectedRoute.getNearestStop(selectedDirectionId);

        if (selectedStop != null && !stopsList.contains(selectedStop)) {
            stopsList.add(selectedStop);
            stops = stopsList.toArray(new Stop[0]);
        }

        routeDetailSpinners.populateStopSpinner(stops);

        if (selectedStop != null) {
            routeDetailSpinners.selectStop(selectedStop.getId());
        } else if (stops.length > 0 && userLocation.getLatitude() != 0 &&
                userLocation.getLongitude() != 0) {
            // Locate stop nearest to user's location
            Stop nearestStop = stops[0];
            double nearestDistance = stops[0].getLocation().distanceTo(userLocation);

            for (int i = 1; i < stops.length; i++) {
                double d = stops[i].getLocation().distanceTo(userLocation);
                if (stops[i].getLocation().distanceTo(userLocation) < nearestDistance) {
                    nearestStop = stops[i];
                    nearestDistance = d;
                }
            }

            routeDetailSpinners.selectStop(nearestStop.getId());
        }
    }

    @Override
    public void onDirectionSelected(Direction selectedDirection) {
        selectedDirectionId = selectedDirection.getId();

        populateStopSpinner(selectedRoute.getStops(selectedDirectionId));

        if (shapesLoaded) {
            refreshShapes();
            refreshVehicles();
        }
    }

    @Override
    public void onStopSelected(Stop selectedStop) {
        // Otherwise set the nearest stop to the selected stop
        selectedRoute.setNearestStop(selectedDirectionId, selectedStop);

        // Find the nearest stop in the opposite direction
        Stop nearestOppositeStop = null;
        float oppositeStopDistance = 0;
        int oppositeDirectionId = (selectedDirectionId + 1) % 2;

        for (Stop s : selectedRoute.getStops(oppositeDirectionId)) {
            float dist = s.getLocation().distanceTo(selectedStop.getLocation());
            if (nearestOppositeStop == null || dist < oppositeStopDistance) {
                nearestOppositeStop = s;
                oppositeStopDistance = dist;
            }
        }

        selectedRoute.setNearestStop(oppositeDirectionId, nearestOppositeStop);

        // Get the predictions for the selected stop
        swipeRefreshLayout.setRefreshing(true);
        getPredictions();


        // Update the stop markers on the map
        if (selectedStopMarker != null) {
            selectedStopMarker.setIcon(selectedRoute.getStopMarkerIcon());
        }

        if (mapReady) {
            selectedStopMarker = stopMarkers.get(selectedStop.getId());

            if (selectedStopMarker == null) {
                selectedStopMarker = drawStopMarker(selectedStop);
                stopMarkers.put(selectedStop.getId(), selectedStopMarker);
            }

            selectedStopMarker.setIcon(
                    BitmapDescriptorFactory.fromResource(R.drawable.icon_selected_stop));
            selectedStopMarker.showInfoWindow();

            // Center the map on the selected stop
            gMap.animateCamera(CameraUpdateFactory.newLatLng(selectedStopMarker.getPosition()));
        }
    }

    private void backgroundUpdate() {
        if (!refreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        getPredictions();
    }

    private class PredictionsPostExecuteListener implements RouteSearchPredictionsAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(List<Prediction> predictions) {
            refreshing = false;
            refreshTime = new Date().getTime();

            for (Prediction prediction : predictions) {
                if (vehicleMarkers.get(prediction.getVehicleId()) != null) {
                    prediction.setVehicle(
                            (Vehicle) (vehicleMarkers.get(prediction.getVehicleId()).getTag()));
                }
            }

            selectedRoute.clearPredictions(0);
            selectedRoute.clearPredictions(1);
            selectedRoute.addAllPredictions(predictions);

            refreshPredictions(false);
        }

        @Override
        public void onError() {
            refreshing = false;
            refreshTime = new Date().getTime();

            selectedRoute.clearPredictions(0);
            selectedRoute.clearPredictions(1);

            refreshPredictions(true);
        }
    }

    private class ShapesPostExecuteListener implements ShapesAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Shape[] shapes) {
            selectedRoute.addShapes(shapes);

            refreshShapes();
            populateStopSpinner(selectedRoute.getStops(selectedDirectionId));
        }

        @Override
        public void onError() {
            getShapes();
        }
    }

    private class VehiclesPostExecuteListener implements VehiclesByRouteAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Vehicle[] vehicles) {
            selectedRoute.setVehicles(vehicles);

            refreshVehicles();
        }

        @Override
        public void onError() {
        }
    }

    private class PredictionsUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            backgroundUpdate();
        }
    }

    private class VehiclesUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            getVehicles();
        }
    }
}
