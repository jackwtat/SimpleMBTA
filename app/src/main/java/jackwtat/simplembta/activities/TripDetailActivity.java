package jackwtat.simplembta.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import jackwtat.simplembta.adapters.TripDetailRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.PredictionsByTripAsyncTask;
import jackwtat.simplembta.asyncTasks.TripsAsyncTask;
import jackwtat.simplembta.asyncTasks.VehiclesByTripAsyncTask;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Trip;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.utilities.DateUtil;
import jackwtat.simplembta.utilities.DisplayNameUtil;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.RawResourceReader;

public class TripDetailActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        ErrorManager.OnErrorChangedListener {
    public static final String LOG_TAG = "TripDetailActivity";

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
    private ProgressBar mapProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView errorTextView;

    private String realTimeApiKey;
    private PredictionsByTripAsyncTask predictionsAsyncTask;
    private TripsAsyncTask tripsAsyncTask;
    private VehiclesByTripAsyncTask vehiclesAsyncTask;
    private NetworkConnectivityClient networkConnectivityClient;
    private ErrorManager errorManager;
    private TripDetailRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private boolean refreshing = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private boolean mapReady = false;
    private boolean shapesLoaded = false;
    private boolean mapCameraIsMoving = false;

    private Trip trip;
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private HashMap<String, Marker> stopMarkers = new HashMap<>();
    private HashMap<String, Marker> vehicleMarkers = new HashMap<>();
    private Marker selectedStopMarker;
    private Marker selectedVehicleMarker;

    private Route selectedRoute;
    private Stop selectedStop;
    private String selectedTripId;
    private Date selectedDate;
    private ArrayList<Prediction> predictions = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        recyclerView = findViewById(R.id.predictions_recycler_view);
        errorTextView = findViewById(R.id.error_message_text_view);

        // Get MBTA realTime API key
        realTimeApiKey = getResources().getString(R.string.v3_mbta_realtime_api_key);

        // Get network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(this);

        // Get data saved from previous session
        if (savedInstanceState != null) {
            selectedRoute = (Route) savedInstanceState.getSerializable("route");
            selectedStop = (Stop) savedInstanceState.getSerializable("stop");
            selectedTripId = savedInstanceState.getString("trip");
            selectedDate = (Date) savedInstanceState.getSerializable("date");
            refreshTime = savedInstanceState.getLong("refreshTime");

            if (new Date().getTime() - refreshTime > MAXIMUM_PREDICTION_AGE) {
                predictions.clear();
            }

            // Get values passed from calling activity/fragment
        } else {
            Intent intent = getIntent();
            selectedRoute = (Route) intent.getSerializableExtra("route");
            selectedStop = (Stop) intent.getSerializableExtra("stop");
            selectedTripId = intent.getStringExtra("trip");
            selectedDate = (Date) intent.getSerializableExtra("date");
            refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);
        }

        // Set action bar
        String title = DisplayNameUtil.getLongDisplayName(this, selectedRoute);
        setTitle(title);

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

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Get and set recycler view
        recyclerView = findViewById(R.id.predictions_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Add on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    refreshPredictions();

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new TripDetailRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setSelectedStop(selectedStop);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

        // Move the map camera to the selected stop
        LatLng latLng = (selectedStop == null)
                ? new LatLng(0, 0)
                : new LatLng(selectedStop.getLocation().getLatitude(), selectedStop.getLocation().getLongitude());
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
        /*
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

                return true;
            }
        });
         */
        gMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                gMap.animateCamera(CameraUpdateFactory.newLatLng(
                        new LatLng(location.getLatitude(), location.getLongitude())));
            }
        });

        // Load route shapes
        getTrip();
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
        refreshPredictions();

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

        if (tripsAsyncTask != null) {
            tripsAsyncTask.cancel(true);
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
        outState.putSerializable("stop", selectedStop);
        outState.putString("trip", selectedTripId);
        outState.putLong("refreshTime", refreshTime);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
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

                } else if (!errorManager.hasNetworkError()) {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getPredictions() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            refreshing = true;

            if (predictionsAsyncTask != null) {
                predictionsAsyncTask.cancel(true);
            }

            predictionsAsyncTask = new PredictionsByTripAsyncTask(realTimeApiKey, selectedTripId,
                    DateUtil.getMbtaDateOffset(selectedDate), new PredictionsPostExecuteListener());
            predictionsAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
            refreshing = false;
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getTrip() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (tripsAsyncTask != null) {
                tripsAsyncTask.cancel(true);
            }

            tripsAsyncTask = new TripsAsyncTask(
                    realTimeApiKey, selectedTripId, new TripsPostExecuteListener());
            tripsAsyncTask.execute();

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

            vehiclesAsyncTask = new VehiclesByTripAsyncTask(
                    realTimeApiKey, selectedTripId, new VehiclesPostExecuteListener());
            vehiclesAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void refreshPredictions() {
        if (!userIsScrolling && predictions != null && predictions.size() > 0) {
            for (int i = 0; i < predictions.size(); i++) {
                Prediction p = predictions.get(i);
                if (p.getStop().equals(selectedStop)) {
                    recyclerViewAdapter.setSelectedStopSequence(p.getStopSequence());
                }
            }

            recyclerViewAdapter.setPredictions(predictions);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshShapes() {
        if (!userIsScrolling) {
            clearShapes();

            if(trip.getShape() == null || trip.getShape().getPolyline().equals("")) {
                if (selectedRoute.getMode() == Route.HEAVY_RAIL || selectedRoute.getMode() == Route.LIGHT_RAIL) {
                    if (BlueLine.isBlueLine(selectedRoute.getId())) {
                        trip.setShape(getShapesFromJson(R.raw.shapes_blue)[0]);

                    } else if (OrangeLine.isOrangeLine(selectedRoute.getId())) {
                        trip.setShape(getShapesFromJson(R.raw.shapes_orange)[0]);

                    } else if (RedLine.isRedLine(selectedRoute.getId()) && !RedLine.isMattapanLine(selectedRoute.getId())) {
                        trip.setShape(getShapesFromJson(R.raw.shapes_red)[0]);

                    } else if (RedLine.isRedLine(selectedRoute.getId()) && RedLine.isMattapanLine(selectedRoute.getId())) {
                        trip.setShape(getShapesFromJson(R.raw.shapes_mattapan)[0]);

                    } else if (GreenLine.isGreenLine(selectedRoute.getId())) {
                        if (GreenLineCombined.isGreenLineCombined(selectedRoute.getId())) {
                            trip.setShape(getShapesFromJson(R.raw.shapes_green_combined)[0]);
                        } else if (GreenLine.isGreenLineB(selectedRoute.getId())) {
                            trip.setShape(getShapesFromJson(R.raw.shapes_green_b)[0]);

                        } else if (GreenLine.isGreenLineC(selectedRoute.getId())) {
                            trip.setShape(getShapesFromJson(R.raw.shapes_green_c)[0]);

                        } else if (GreenLine.isGreenLineD(selectedRoute.getId())) {
                            trip.setShape(getShapesFromJson(R.raw.shapes_green_d)[0]);

                        } else if (GreenLine.isGreenLineE(selectedRoute.getId())) {
                            trip.setShape(getShapesFromJson(R.raw.shapes_green_e)[0]);
                        }
                    }
                }
            }

            Shape shape = trip.getShape();

            if (shape != null) {
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
                if (!trackedVehicleIds.contains(vehicleId)) {
                    expiredVehicleIds.add(vehicleId);
                }
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
        return ShapesJsonParser.parse(
                RawResourceReader.toString(getResources().openRawResource(jsonFile)));
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

    private void backgroundUpdate() {
        if (!refreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        getPredictions();
    }

    private class PredictionsPostExecuteListener implements PredictionsByTripAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Prediction[] p, boolean live) {
            refreshing = false;
            refreshTime = new Date().getTime();

            predictions.clear();
            predictions.addAll(Arrays.asList(p));

            refreshPredictions();
        }

        @Override
        public void onError() {
            refreshing = false;
            refreshTime = new Date().getTime();

            refreshPredictions();
        }
    }

    private class TripsPostExecuteListener implements TripsAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Trip[] trips) {
            trip = trips[0];

            refreshShapes();
        }

        @Override
        public void onError() {
            getTrip();
        }
    }

    private class VehiclesPostExecuteListener implements VehiclesByTripAsyncTask.OnPostExecuteListener {
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
