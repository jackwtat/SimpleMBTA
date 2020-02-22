package jackwtat.simplembta.activities;

import android.Manifest;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.TripDetailRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.PredictionsTripDetailAsyncTask;
import jackwtat.simplembta.asyncTasks.StopAlertsAsyncTask;
import jackwtat.simplembta.asyncTasks.StopsByIdAsyncTask;
import jackwtat.simplembta.asyncTasks.TripsAsyncTask;
import jackwtat.simplembta.asyncTasks.VehiclesByIdAsyncTask;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Trip;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.routes.SilverLine;
import jackwtat.simplembta.utilities.Constants;
import jackwtat.simplembta.utilities.DisplayNameUtil;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.PastDataHolder;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.views.NoPredictionsView;
import jackwtat.simplembta.views.StopInfoBodyView;
import jackwtat.simplembta.views.StopInfoTitleView;

public class TripDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        ErrorManager.OnErrorChangedListener, Constants {
    public static final String LOG_TAG = "TripDetailActivity";

    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private ProgressBar mapProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NoPredictionsView noPredictionsView;
    private TextView errorTextView;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private ErrorManager errorManager;
    private TripDetailRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private PredictionsTripDetailAsyncTask predictionsAsyncTask;
    private TripsAsyncTask tripsAsyncTask;
    private VehiclesByIdAsyncTask vehiclesAsyncTask;
    private StopsByIdAsyncTask stopsAsyncTask;
    private StopAlertsAsyncTask stopAlertsAsyncTask;

    private boolean dataRefreshing = false;
    private boolean loaded = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private boolean mapReady = false;
    private boolean mapCameraIsMoving = false;

    private Trip trip;
    private Vehicle vehicle;
    private ArrayList<Prediction> predictions = new ArrayList<>();
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private HashMap<String, Marker> stopMarkers = new HashMap<>();
    private HashMap<String, Marker> closedStopMarkers = new HashMap<>();
    private Marker vehicleMarker;
    private PastDataHolder pastData = PastDataHolder.getHolder();

    private Route selectedRoute;
    private Stop selectedStop;
    private String selectedTripId;
    private String selectedTripName;
    private String selectedTripDestination;
    private String selectedVehicleId;
    private Date selectedDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // Get MBTA realTime API key
        realTimeApiKey = getResources().getString(R.string.v3_mbta_realtime_api_key);

        // Get network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(this);

        // Get data saved from previous session
        if (savedInstanceState != null) {
            selectedRoute = (Route) savedInstanceState.getSerializable("route");
            selectedStop = (Stop) savedInstanceState.getSerializable("stop");
            selectedTripId = savedInstanceState.getString("trip");
            selectedTripName = savedInstanceState.getString("name");
            selectedTripDestination = savedInstanceState.getString("destination");
            selectedVehicleId = savedInstanceState.getString("vehicle");
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
            selectedTripName = intent.getStringExtra("name");
            selectedTripDestination = intent.getStringExtra("destination");
            selectedVehicleId = intent.getStringExtra("vehicle");
            selectedDate = (Date) intent.getSerializableExtra("date");
            refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);
        }

        // Set action bar title
        String title = " "
                + getResources().getString(R.string.trip_detail_to_title)
                + " "
                + selectedTripDestination;

        if (selectedRoute.getMode() == Route.COMMUTER_RAIL) {
            title = getResources().getString(R.string.train) + " " + selectedTripName + title;
        } else {
            title = DisplayNameUtil.getLongDisplayName(this, selectedRoute) + title;
        }
        setTitle(title);

        // Set action bar color
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

        // Get error text view
        errorTextView = findViewById(R.id.error_message_text_view);

        // Set the no predictions indicator
        noPredictionsView = findViewById(R.id.no_predictions_view);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Get and set recycler view
        recyclerView = findViewById(R.id.predictions_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Disable scrolling while activity is still initializing
        recyclerView.setNestedScrollingEnabled(false);

        // Add on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    if (!dataRefreshing && !noPredictionsView.isError()) {
                        refreshPredictions();
                    }

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new TripDetailRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setSelectedStop(selectedStop);

        // Add OnItemClickListener
        recyclerViewAdapter.setOnClickListener(new TripDetailRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onItemClick(int position) {
                Stop stop = recyclerViewAdapter.getPrediction(position).getStop();
                Collections.sort(stop.getRoutes());
                Route route = stop.getRoutes().get(0);

                Collections.sort(stop.getServiceAlerts());

                AlertDialog dialog = new AlertDialog.Builder(TripDetailActivity.this).create();

                StopInfoTitleView titleView = new StopInfoTitleView(TripDetailActivity.this);
                titleView.setText(stop.getName());
                titleView.setTextColor(Color.parseColor(route.getTextColor()));
                titleView.setBackgroundColor(Color.parseColor(route.getPrimaryColor()));

                StopInfoBodyView bodyView = new StopInfoBodyView(TripDetailActivity.this);
                bodyView.setAccessibility(stop.getAccessibility());
                bodyView.setAlerts(stop.getServiceAlerts());

                dialog.setCustomTitle(titleView);
                dialog.setView(bodyView);
                dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getResources().getString(R.string.dialog_close_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                dialog.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

        // Move the map camera to the selected stop
        LatLng latLng = (selectedStop == null)
                ? new LatLng(0, 0)
                : new LatLng(selectedStop.getLocation().getLatitude(), selectedStop.getLocation().getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_FAR_ZOOM_LEVEL));

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

        dataRefreshing = false;

        swipeRefreshLayout.setRefreshing(false);

        if (timer != null) {
            timer.cancel();
        }

        cancelUpdate();
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
        outState.putString("name", selectedTripName);
        outState.putString("destination", selectedTripDestination);
        outState.putString("vehicle", selectedVehicleId);
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

                    clearPredictions();
                    enableOnErrorView(getResources().getString(R.string.network_error_text));

                } else if (errorManager.hasTimeZoneMismatch()) {
                    errorTextView.setText(R.string.time_zone_warning);
                    errorTextView.setVisibility(View.VISIBLE);

                } else if (!errorManager.hasNetworkError()) {
                    errorTextView.setVisibility(View.GONE);
                    clearOnErrorView();
                    swipeRefreshLayout.setRefreshing(true);
                    forceUpdate();
                }
            }
        });
    }

    private void getPredictions() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            dataRefreshing = true;

            if (predictionsAsyncTask != null) {
                predictionsAsyncTask.cancel(true);
            }

            if (selectedDate == null) {
                selectedDate = new Date();
            }

            predictionsAsyncTask = new PredictionsTripDetailAsyncTask(realTimeApiKey, selectedTripId,
                    selectedDate, new PredictionsPostExecuteListener());
            predictionsAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
            enableOnErrorView(getResources().getString(R.string.error_network));
            dataRefreshing = false;
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

            vehiclesAsyncTask = new VehiclesByIdAsyncTask(
                    realTimeApiKey, selectedVehicleId, new VehiclesPostExecuteListener());
            vehiclesAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void getParentStops(String[] stopIds) {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (stopsAsyncTask != null) {
                stopsAsyncTask.cancel(true);
            }

            stopsAsyncTask = new StopsByIdAsyncTask(
                    realTimeApiKey, stopIds, new StopsPostExecuteListener());
            stopsAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void getStopAlerts() {
        if (stopAlertsAsyncTask != null) {
            stopAlertsAsyncTask.cancel(true);
        }

        ArrayList<String> stopIds = new ArrayList<>();
        for (Prediction prediction : predictions) {
            stopIds.add(prediction.getStopId());
        }

        if (stopIds.size() > 0) {
            stopAlertsAsyncTask = new StopAlertsAsyncTask(realTimeApiKey,
                    stopIds.toArray(new String[0]), new StopAlertsPostExecuteListener());

            stopAlertsAsyncTask.execute();
        } else {

        }
    }

    private void refreshPredictions() {
        if (!userIsScrolling && predictions != null) {
            for (int i = 0; i < predictions.size(); i++) {
                Prediction p = predictions.get(i);

                Marker stopMarker = stopMarkers.get(p.getStopId());
                if (stopMarker != null) {
                    stopMarker.setSnippet(getPredictionSnippet(p));
                }

                if (p.getStatus() == Prediction.SKIPPED || p.getStatus() == Prediction.CANCELLED) {
                    Marker closedStop = drawStopMarker(p.getStop(), true);
                    closedStop.setSnippet(getPredictionSnippet(p));
                    closedStopMarkers.put(p.getStopId(), closedStop);

                } else {
                    Marker closedStop = closedStopMarkers.get(p.getStopId());
                    if (closedStop != null) {
                        closedStop.remove();
                        closedStopMarkers.remove(p.getStopId());
                    }
                }

                if (p.getStop().equals(selectedStop) ||
                        p.getStop().isParentOf(selectedStop.getId()) ||
                        selectedStop.isParentOf(p.getStopId())) {
                    recyclerViewAdapter.setSelectedStopSequence(p.getStopSequence());
                }
            }

            recyclerViewAdapter.setPredictions(predictions);
            swipeRefreshLayout.setRefreshing(false);

            if (predictions.size() == 0) {
                enableNoPredictionsView(getResources().getString(R.string.no_predictions_this_stop));
            } else {
                loaded = true;
                clearOnErrorView();
                noPredictionsView.clearNoPredictions();
                recyclerView.setNestedScrollingEnabled(true);
            }
        }
    }

    private void refreshShapes() {
        if (!userIsScrolling) {
            clearShapes();

            if (trip.getShape() == null || trip.getShape().getPolyline().equals("")) {
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
                        Marker currentMarker = drawStopMarker(stop, false);

                        // Use selected stop marker if this stop is the selected stop
                        if (selectedStop != null &&
                                (selectedStop.equals(stop) ||
                                        selectedStop.isParentOf(stop.getId()) ||
                                        stop.isParentOf(selectedStop.getId()))) {
                            currentMarker.setIcon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.icon_selected_stop));
                        }

                        // Add stop to the drawn stops hash map
                        stopMarkers.put(stop.getId(), currentMarker);
                    }
                }
            }

            // Add predictions to stop markers
            for (Prediction p : predictions) {
                Marker marker = stopMarkers.get(p.getStopId());

                if (marker != null) {
                    marker.setSnippet(getPredictionSnippet(p));
                }

                if (p.getStatus() == Prediction.SKIPPED ||
                        p.getStatus() == Prediction.CANCELLED) {
                    Marker closedStop = drawStopMarker(p.getStop(), true);
                    closedStop.setSnippet(getPredictionSnippet(p));
                    closedStopMarkers.put(p.getStopId(), closedStop);
                }
            }

            mapProgressBar.setVisibility(View.GONE);
        }
    }

    private void refreshVehicles() {
        recyclerViewAdapter.setVehicle(vehicle);

        if (mapReady) {
            if (vehicle != null) {
                // Draw vehicle marker
                if (vehicleMarker == null) {
                    int mode = selectedRoute.getMode();
                    if (mode == Route.COMMUTER_RAIL) {
                        vehicleMarker = drawVehicleMarker(vehicle,
                                getResources().getString(R.string.map_train) + " " + vehicle.getTripName());
                    } else if (mode == Route.LIGHT_RAIL || mode == Route.HEAVY_RAIL) {
                        vehicleMarker = drawVehicleMarker(vehicle,
                                getResources().getString(R.string.map_train) + " " + vehicle.getLabel());
                    } else {
                        vehicleMarker = drawVehicleMarker(vehicle,
                                getResources().getString(R.string.map_vehicle) + " " + vehicle.getLabel());
                    }
                } else {
                    vehicleMarker.setPosition(new LatLng(
                            vehicle.getLocation().getLatitude(),
                            vehicle.getLocation().getLongitude()));
                    vehicleMarker.setRotation(vehicle.getLocation().getBearing());
                }

                // Set snippet
                vehicleMarker.setSnippet("");
                if (vehicle.getTripId().equalsIgnoreCase(selectedTripId)) {
                    for (Prediction p : predictions) {
                        if (p.getStopSequence() == vehicle.getCurrentStopSequence()) {
                            vehicleMarker.setSnippet(vehicle.getCurrentStatus().getText() + " " +
                                    p.getStop().getName());
                            break;
                        }
                    }
                } else {
                    // 'Currently on '
                    String snippet = getResources().getString(R.string.map_currently_on) + " ";

                    // 'route ' (Optional)
                    if (selectedRoute.getMode() == Route.BUS) {
                        snippet += getResources().getString(R.string.map_route) + " ";
                    }

                    // route_id + ' to ' + destination
                    snippet += vehicle.getRoute()
                            + " "
                            + getResources().getString(R.string.map_to)
                            + " "
                            + vehicle.getDestination();

                    // Set snippet text
                    vehicleMarker.setSnippet(snippet);
                }

                // Refresh info window
                if (vehicleMarker.isInfoWindowShown()) {
                    vehicleMarker.hideInfoWindow();
                    vehicleMarker.showInfoWindow();
                }
            } else {
                if (vehicleMarker != null) {
                    vehicleMarker.remove();
                }
                vehicleMarker = null;
            }
        }

        if (!userIsScrolling) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private void enableOnErrorView(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerViewAdapter.clear();
                recyclerView.setNestedScrollingEnabled(false);
                swipeRefreshLayout.setRefreshing(false);
                appBarLayout.setExpanded(true);
                noPredictionsView.setError(message);
            }
        });
    }

    private void enableNoPredictionsView(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                appBarLayout.setExpanded(true);
                predictions.clear();
                if (loaded) {
                    noPredictionsView.setNoPredictions(message);
                }
            }
        });
    }

    private void clearOnErrorView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noPredictionsView.clearError();
            }
        });

    }

    private void clearPredictions() {
        predictions.clear();
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

        for (Marker m : closedStopMarkers.values()) {
            m.remove();
        }

        stopMarkers.clear();
        closedStopMarkers.clear();
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

    private Marker drawStopMarker(@NonNull Stop stop, boolean closed) {
        int zIndex = (closed)
                ? 11
                : 10;

        MarkerOptions markerOptions = (closed)
                ? selectedRoute.getClosedStopMarkerOptions()
                : selectedRoute.getStopMarkerOptions();

        markerOptions.position(new LatLng(
                stop.getLocation().getLatitude(), stop.getLocation().getLongitude()));
        markerOptions.zIndex(zIndex);
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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_vehicle))
        );

        vehicleMarker.setTag(vehicle);

        return vehicleMarker;
    }

    private String getPredictionSnippet(Prediction prediction) {
        String snippet = "";
        Date predictionTime = prediction.getPredictionTime();
        boolean predictionCancelled = prediction.getStatus() == Prediction.CANCELLED ||
                prediction.getStatus() == Prediction.SKIPPED;

        if (predictionTime != null && !predictionCancelled) {
            if (prediction.getCountdownTime() > 0) {
                long countdownTime = prediction.getCountdownTime();

                if (prediction.willPickUpPassengers()) {
                    snippet = getResources().getString(R.string.map_departs) + " ";
                } else {
                    snippet = getResources().getString(R.string.map_arrives) + " ";
                }

                if (countdownTime < 60 * 60000) {
                    snippet += getResources().getString(R.string.map_in) + " " +
                            prediction.getCountdownTime() / 60000 + " " +
                            getResources().getString(R.string.map_min);
                } else {
                    snippet += getResources().getString(R.string.map_at) + " " +
                            new SimpleDateFormat("h:mm").format(predictionTime) + " " +
                            new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                }
            } else {
                if (prediction.willPickUpPassengers()) {
                    snippet = getResources().getString(R.string.map_already_departed_at) + " ";
                } else {
                    snippet = getResources().getString(R.string.map_already_arrived_at) + " ";
                }

                snippet += new SimpleDateFormat("h:mm").format(predictionTime) + " " +
                        new SimpleDateFormat("a").format(predictionTime).toLowerCase();
            }
        } else if (predictionCancelled) {
            if (prediction.getStatus() == Prediction.CANCELLED) {
                snippet = getResources().getString(R.string.map_cancelled);
            } else {
                snippet = getResources().getString(R.string.map_skipped);
            }
        }

        return snippet;
    }

    private void backgroundUpdate() {
        if (!dataRefreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        getPredictions();
    }

    private void cancelUpdate() {
        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (tripsAsyncTask != null) {
            tripsAsyncTask.cancel(true);
        }

        if (vehiclesAsyncTask != null) {
            vehiclesAsyncTask.cancel(true);
        }

        if (stopsAsyncTask != null) {
            stopsAsyncTask.cancel(true);
        }

    }

    private class PredictionsPostExecuteListener implements PredictionsTripDetailAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Prediction[] p, boolean live) {
            dataRefreshing = false;
            refreshTime = new Date().getTime();

            ArrayList<String> parentStopIds = new ArrayList<>();

            for (Prediction prediction : p) {
                // Reduce 'time bounce' by replacing current prediction time with prior prediction
                // time if one exists if they are within one minute
                pastData.normalizePrediction(prediction);

                // If this prediction's stop has a parent stop, add it to the stop query
                String parentStopId = prediction.getParentStopId();
                if (parentStopId != null && !parentStopId.equals("")) {
                    parentStopIds.add(parentStopId);
                }

                // Add this route to prediction
                prediction.getStop().addRoute(selectedRoute);

                // Put this prediction into list of prior predictions
                pastData.add(prediction);
            }

            predictions.clear();
            predictions.addAll(Arrays.asList(p));

            if (parentStopIds.size() > 0) {
                getParentStops(parentStopIds.toArray(new String[0]));
            } else {
                getStopAlerts();
            }
        }

        @Override
        public void onError() {
            dataRefreshing = false;
            refreshTime = new Date().getTime();
            enableOnErrorView(getResources().getString(R.string.error_upcoming_predictions));

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

    private class VehiclesPostExecuteListener implements VehiclesByIdAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Vehicle[] vehicles) {
            if (vehicles != null && vehicles.length > 0) {
                vehicle = vehicles[0];
            } else {
                vehicle = null;
            }

            refreshVehicles();
        }

        @Override
        public void onError() {
            vehicle = null;
            refreshVehicles();
        }
    }

    private class StopsPostExecuteListener implements StopsByIdAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Stop[] stopsArray) {
            HashMap<String, Stop> stops = new HashMap<>();

            for (Stop s : stopsArray) {
                s.addRoute(selectedRoute);
                stops.put(s.getId(), s);
            }

            for (Prediction p : predictions) {
                String parentId = p.getParentStopId();

                if (parentId != null && stops.get(parentId) != null) {
                    p.setStop(stops.get(parentId));
                }
            }
            getStopAlerts();
        }

        @Override
        public void onError() {
            getStopAlerts();
        }
    }

    private class StopAlertsPostExecuteListener implements StopAlertsAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(ServiceAlert[] alerts) {
            for (ServiceAlert alert : alerts) {
                if (alert.getAffectedRoutes().size() == 0) {
                    for (Prediction prediction : predictions) {
                        if (alert.affectsStop(prediction.getStopId())) {
                            prediction.getStop().addServiceAlert(alert);
                        }
                    }
                }
            }

            refreshPredictions();
        }

        @Override
        public void onError() {
            refreshPredictions();
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
            if (selectedVehicleId != null && !selectedVehicleId.equalsIgnoreCase("")) {
                getVehicles();
            }
        }
    }
}
