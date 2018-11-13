package jackwtat.simplembta.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import jackwtat.simplembta.adapters.RouteDetailRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.RouteDetailPredictionsAsyncTask;
import jackwtat.simplembta.asyncTasks.ServiceAlertsAsyncTask;
import jackwtat.simplembta.asyncTasks.ShapesAsyncTask;
import jackwtat.simplembta.asyncTasks.VehiclesAsyncTask;
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
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.utilities.DisplayNameUtil;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.views.ServiceAlertsIndicatorView;
import jackwtat.simplembta.views.StopSelectorView;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        ErrorManager.OnErrorChangedListener, RouteDetailPredictionsAsyncTask.OnPostExecuteListener,
        ShapesAsyncTask.OnPostExecuteListener, ServiceAlertsAsyncTask.OnPostExecuteListener,
        VehiclesAsyncTask.OnPostExecuteListener,
        StopSelectorView.OnDirectionSelectedListener, StopSelectorView.OnStopSelectedListener {
    public static final String LOG_TAG = "RouteDetailActivity";

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    // Service alerts auto update rate
    public static final long SERVICE_ALERTS_UPDATE_RATE = 60000;

    // Vehicle locations auto update rate
    public static final long VEHICLES_UPDATE_RATE = 15000;

    // Shapes auto update rate
    public static final long SHAPES_UPDATE_RATE = 15000;

    // Maximum age of prediction
    public static final long MAXIMUM_PREDICTION_AGE = 90000;

    // Default level of zoom for the map
    public static final int DEFAULT_MAP_ZOOM_LEVEL = 15;

    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;
    private ProgressBar mapProgressBar;
    private TextView errorTextView;
    private ServiceAlertsIndicatorView serviceAlertsIndicatorView;
    private StopSelectorView stopSelectorView;

    private String realTimeApiKey;
    private RouteDetailPredictionsAsyncTask predictionsAsyncTask;
    private ServiceAlertsAsyncTask serviceAlertsAsyncTask;
    private ShapesAsyncTask shapesAsyncTask;
    private VehiclesAsyncTask vehiclesAsyncTask;
    private NetworkConnectivityClient networkConnectivityClient;
    private ErrorManager errorManager;
    private RouteDetailRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private boolean refreshing = false;
    private boolean mapReady = false;
    private boolean shapesLoaded = false;
    private boolean mapCameraIsMoving = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private Route route;
    private int selectedDirectionId;
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private HashMap<String, Marker> stopMarkers = new HashMap<>();
    private ArrayList<Marker> vehicleMarkers = new ArrayList<>();
    private Marker selectedStopMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        // Get MBTA realTime API key
        realTimeApiKey = getResources().getString(R.string.v3_mbta_realtime_api_key);

        // Get values passed from calling activity/fragment
        Intent intent = getIntent();
        route = (Route) intent.getSerializableExtra("route");
        selectedDirectionId = intent.getIntExtra("direction", Direction.NULL_DIRECTION);
        refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);

        // Get network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(this);

        // Set action bar
        setTitle(DisplayNameUtil.getLongDisplayName(this, route));

        if (Build.VERSION.SDK_INT >= 21) {
            // Create color for status bar
            float[] hsv = new float[3];
            Color.colorToHSV(Color.parseColor(route.getPrimaryColor()), hsv);
            hsv[2] *= .8f;

            // Set status bar color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.HSVToColor(hsv));

            // Set action bar background color
            ActionBar actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(Color.parseColor(route.getPrimaryColor())));
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
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        // Get service alerts indicator
        serviceAlertsIndicatorView = findViewById(R.id.service_alerts_indicator_view);

        // Get and initialize map view
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get map progress bar
        mapProgressBar = findViewById(R.id.map_progress_bar);

        // Get the stop selector view
        stopSelectorView = findViewById(R.id.stop_selector_view);
        populateDirectionSpinner(route.getAllDirections());

        // Populate the stops spinner with the nearest stop until we query the shapes
        if (route.getNearestStop(selectedDirectionId) != null) {
            Stop[] selectedStopArray = {route.getNearestStop(selectedDirectionId)};
            populateStopSpinner(selectedStopArray);
        }

        stopSelectorView.setOnDirectionSelectedListener(this);
        stopSelectorView.setOnStopSelectedListener(this);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Get the no predictions indicator
        noPredictionsTextView = findViewById(R.id.no_predictions_text_view);

        // Get recycler view
        recyclerView = findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Disable recycler view scrolling until predictions loaded;
        recyclerView.setNestedScrollingEnabled(false);

        // Add on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    refreshPredictions(false);
                    refreshServiceAlertsView();
                    if (!shapesLoaded) {
                        refreshShapes();
                    }

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new RouteDetailRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

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

                    stopSelectorView.selectStop(selectedStop.getId());
                }

                return true;
            }
        });

        // Move the map camera to the selected stop
        Stop stop = route.getNearestStop(selectedDirectionId);
        LatLng latLng = (stop == null)
                ? new LatLng(42.3604, -71.0580)
                : new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM_LEVEL));

        // Set the map style
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Set the map UI settings
        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

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

        // Refresh the activity to update UI so that the predictions and service alerts are accurate
        // as of the last update
        refreshPredictions(false);
        refreshServiceAlertsView();
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
        timer.schedule(new ServiceAlertsUpdateTimerTask(), 0, SERVICE_ALERTS_UPDATE_RATE);
        timer.schedule(new VehiclesUpdateTimerTask(), 0, VEHICLES_UPDATE_RATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        if (vehiclesAsyncTask != null) {
            vehiclesAsyncTask.cancel(true);
        }

        timer.cancel();
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

                    route.clearPredictions(Direction.INBOUND);
                    route.clearPredictions(Direction.OUTBOUND);
                    route.clearServiceAlerts();

                    clearVehicleMarkers();

                    refreshPredictions(true);
                    refreshServiceAlertsView();
                    refreshVehicles();

                } else if (!errorManager.hasNetworkError()) {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getPredictions() {
        if (networkConnectivityClient.isConnected() &&
                route.getNearestStop(selectedDirectionId) != null) {
            errorManager.setNetworkError(false);

            refreshing = true;

            if (predictionsAsyncTask != null) {
                predictionsAsyncTask.cancel(true);
            }

            predictionsAsyncTask = new RouteDetailPredictionsAsyncTask(realTimeApiKey, route, this);
            predictionsAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
            refreshing = false;
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getServiceAlerts() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (serviceAlertsAsyncTask != null) {
                serviceAlertsAsyncTask.cancel(true);
            }

            serviceAlertsAsyncTask = new ServiceAlertsAsyncTask(
                    realTimeApiKey,
                    route.getId(),
                    this);
            serviceAlertsAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void getShapes() {
        // Hard coding to save the user time and data
        if (route.getMode() == Route.HEAVY_RAIL || route.getMode() == Route.LIGHT_RAIL) {
            if (BlueLine.isBlueLine(route.getId())) {
                route.setShapes(getShapesFromJson(R.raw.shapes_blue));

            } else if (OrangeLine.isOrangeLine(route.getId())) {
                route.setShapes(getShapesFromJson(R.raw.shapes_orange));

            } else if (RedLine.isRedLine(route.getId()) && !RedLine.isMattapanLine(route.getId())) {
                route.setShapes(getShapesFromJson(R.raw.shapes_red));

            } else if (RedLine.isRedLine(route.getId()) && RedLine.isMattapanLine(route.getId())) {
                route.setShapes(getShapesFromJson(R.raw.shapes_mattapan));

            } else if (GreenLine.isGreenLine(route.getId())) {
                if (GreenLineCombined.isGreenLineCombined(route.getId())) {
                    route.setShapes(getShapesFromJson(R.raw.shapes_green_combined));
                } else if (GreenLine.isGreenLineB(route.getId())) {
                    route.setShapes(getShapesFromJson(R.raw.shapes_green_b));

                } else if (GreenLine.isGreenLineC(route.getId())) {
                    route.setShapes(getShapesFromJson(R.raw.shapes_green_c));

                } else if (GreenLine.isGreenLineD(route.getId())) {
                    route.setShapes(getShapesFromJson(R.raw.shapes_green_d));

                } else if (GreenLine.isGreenLineE(route.getId())) {
                    route.setShapes(getShapesFromJson(R.raw.shapes_green_e));

                }
            }

            refreshShapes();
            populateStopSpinner(route.getStops(selectedDirectionId));

        } else if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (shapesAsyncTask != null) {
                shapesAsyncTask.cancel(true);
            }

            shapesAsyncTask = new ShapesAsyncTask(
                    realTimeApiKey,
                    route.getId(),
                    this);
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

            vehiclesAsyncTask = new VehiclesAsyncTask(
                    realTimeApiKey,
                    route.getId(),
                    this);
            vehiclesAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    @Override
    public void onPostExecute(List<Prediction> predictions) {
        refreshing = false;
        refreshTime = new Date().getTime();

        route.clearPredictions(0);
        route.clearPredictions(1);
        route.addAllPredictions(predictions);

        refreshPredictions(false);
    }

    @Override
    public void onPostExecute(ServiceAlert[] serviceAlerts) {
        route.clearServiceAlerts();
        route.addAllServiceAlerts(serviceAlerts);

        refreshServiceAlertsView();
    }

    @Override
    public void onPostExecute(Shape[] shapes) {
        route.setShapes(shapes);

        refreshShapes();
        populateStopSpinner(route.getStops(selectedDirectionId));
    }

    @Override
    public void onPostExecute(Vehicle[] vehicles) {
        route.setVehicles(vehicles);

        refreshVehicles();
    }

    private void refreshPredictions(boolean returnToTop) {
        if (!userIsScrolling) {
            recyclerViewAdapter.setPredictions(route.getPredictions(selectedDirectionId));
            swipeRefreshLayout.setRefreshing(false);

            if (recyclerViewAdapter.getItemCount() == 0) {
                if (route.getNearestStop(selectedDirectionId) == null) {
                    noPredictionsTextView.setText(getResources().getString(R.string.select_stop));
                } else {
                    noPredictionsTextView.setText(getResources().getString(R.string.no_predictions_this_stop));
                }

                noPredictionsTextView.setVisibility(View.VISIBLE);
                appBarLayout.setExpanded(true);
                recyclerView.setNestedScrollingEnabled(false);
            } else {
                noPredictionsTextView.setVisibility(View.GONE);
                recyclerView.setNestedScrollingEnabled(true);
            }

            if (returnToTop) {
                recyclerView.scrollToPosition(0);
            }
        }
    }

    private void refreshServiceAlertsView() {
        if (!userIsScrolling) {
            if (route.getServiceAlerts().size() > 0) {
                serviceAlertsIndicatorView.setServiceAlerts(route);
                serviceAlertsIndicatorView.setVisibility(View.VISIBLE);

            } else {
                serviceAlertsIndicatorView.setVisibility(View.GONE);
            }
        }
    }

    private void refreshShapes() {
        if (!userIsScrolling) {
            clearShapes();

            Stop selectedStop = route.getNearestStop(selectedDirectionId);

            for (Shape shape : route.getShapes(selectedDirectionId)) {
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
        if (!userIsScrolling) {
            clearVehicleMarkers();

            for (Vehicle vehicle : route.getVehicles(selectedDirectionId)) {
                vehicleMarkers.add(drawVehicleMarker(vehicle));
            }
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        noPredictionsTextView.setVisibility(View.GONE);
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
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
        for (Marker vm : vehicleMarkers) {
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
                        .color(Color.parseColor(route.getPrimaryColor()))
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
        MarkerOptions markerOptions = route.getStopMarkerOptions();

        markerOptions.position(new LatLng(
                stop.getLocation().getLatitude(), stop.getLocation().getLongitude()));
        markerOptions.zIndex(10);
        markerOptions.title(stop.getName());

        Marker stopMarker = gMap.addMarker(markerOptions);

        stopMarker.setTag(stop);

        return stopMarker;
    }

    private Marker drawVehicleMarker(@NonNull Vehicle vehicle) {
        Marker vehicleMarker = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        vehicle.getLocation().getLatitude(), vehicle.getLocation().getLongitude()))
                .rotation(vehicle.getLocation().getBearing())
                .anchor(0.5f, 0.5f)
                .zIndex(20)
                .flat(true)
                .title(vehicle.getLabel())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_vehicle))
        );

        vehicleMarker.setTag(vehicle);

        return vehicleMarker;
    }

    private void populateDirectionSpinner(Direction[] directions) {
        stopSelectorView.populateDirectionSpinner(directions);
        stopSelectorView.selectDirection(selectedDirectionId);
    }

    private void populateStopSpinner(Stop[] stops) {
        stopSelectorView.populateStopSpinner(stops);

        if (route.getNearestStop(selectedDirectionId) != null) {
            stopSelectorView.selectStop(route.getNearestStop(selectedDirectionId).getId());
        }
    }

    @Override
    public void onDirectionSelected(Direction selectedDirection) {
        selectedDirectionId = selectedDirection.getId();

        populateStopSpinner(route.getStops(selectedDirectionId));

        refreshPredictions(true);

        if (shapesLoaded) {
            refreshShapes();
            refreshVehicles();
        }
    }

    @Override
    public void onStopSelected(Stop selectedStop) {
        // If already have the predictions for this stop, then just display those predictions
        if (selectedStop.equals(route.getNearestStop(0)) ||
                selectedStop.equals(route.getNearestStop(1))) {
            refreshPredictions(true);
        } else {
            // Otherwise set the nearest stop to the selected stop
            route.setNearestStop(selectedDirectionId, selectedStop, true);

            // Find the nearest stop in the opposite direction
            Stop nearestOppositeStop = null;
            float oppositeStopDistance = 0;
            int oppositeDirectionId = (selectedDirectionId + 1) % 2;

            for (Stop s : route.getStops(oppositeDirectionId)) {
                float dist = s.getLocation().distanceTo(selectedStop.getLocation());
                if (nearestOppositeStop == null || dist < oppositeStopDistance) {
                    nearestOppositeStop = s;
                    oppositeStopDistance = dist;
                }
            }

            route.setNearestStop(oppositeDirectionId, nearestOppositeStop, true);

            // Clear the current predictions and get the predictions for the selected stop
            recyclerViewAdapter.clear();
            swipeRefreshLayout.setRefreshing(true);
            getPredictions();
        }

        // Update the stop markers on the map
        if (selectedStopMarker != null) {
            selectedStopMarker.setIcon(route.getStopMarkerIcon());
        }
        selectedStopMarker = stopMarkers.get(selectedStop.getId());
        selectedStopMarker.setIcon(
                BitmapDescriptorFactory.fromResource(R.drawable.icon_selected_stop));

        // Center the map on the selected stop
        gMap.animateCamera(CameraUpdateFactory.newLatLng(selectedStopMarker.getPosition()));
    }

    private void backgroundUpdate() {
        if (!refreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        getPredictions();
    }

    private class PredictionsUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            backgroundUpdate();
        }
    }

    private class ServiceAlertsUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            getServiceAlerts();
        }
    }

    private class VehiclesUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            getVehicles();
        }
    }
}
