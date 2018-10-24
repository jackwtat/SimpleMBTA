package jackwtat.simplembta.activities;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Routes;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.views.ServiceAlertsListView;
import jackwtat.simplembta.views.ServiceAlertsTitleView;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        ErrorManager.OnErrorChangedListener, RouteDetailPredictionsAsyncTask.OnPostExecuteListener,
        ShapesAsyncTask.OnPostExecuteListener, ServiceAlertsAsyncTask.OnPostExecuteListener {
    public static final String LOG_TAG = "RouteDetailActivity";

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    // Service alerts auto update rate
    public static final long SERVICE_ALERTS_UPDATE_RATE = 60000;

    // Maximum age of prediction
    public static final long MAXIMUM_PREDICTION_AGE = 90000;

    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;
    private ProgressBar mapProgressBar;
    private TextView errorTextView;

    private LinearLayout serviceAlertsLayout;
    private ImageView serviceAlertIcon;
    private ImageView serviceAdvisoryIcon;
    private TextView serviceAlertsTextView;

    private String realTimeApiKey;
    private RouteDetailPredictionsAsyncTask predictionsAsyncTask;
    private ServiceAlertsAsyncTask serviceAlertsAsyncTask;
    private ShapesAsyncTask shapesAsyncTask;
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
    private int direction;
    private ArrayList<Shape> routeShapes = new ArrayList<>();
    private ArrayList<Marker> stopMarkers = new ArrayList<>();
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
        direction = intent.getIntExtra("direction", Route.NULL_DIRECTION);
        refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);

        // Get error textview
        errorTextView = findViewById(R.id.error_message_text_view);

        // Get network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(this);

        // Set action bar
        setTitle(route.getLongDisplayName(this) + " - " + route.getDirectionName(direction));
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

        // Get the no predictions indicator
        noPredictionsTextView = findViewById(R.id.no_predictions_text_view);

        // Get and initialize map view
        mapView = findViewById(R.id.map_view);
        mapView.getLayoutParams().height =
                (int) (getResources().getDisplayMetrics().heightPixels * .6);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get map progress bar
        mapProgressBar = findViewById(R.id.map_progress_bar);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

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
                    refreshRecyclerView();
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

        // Get the service alerts views
        serviceAlertsLayout = findViewById(R.id.service_alerts_layout);
        serviceAlertIcon = findViewById(R.id.service_alert_icon);
        serviceAdvisoryIcon = findViewById(R.id.service_advisory_icon);
        serviceAlertsTextView = findViewById(R.id.service_alerts_text_view);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

        // Move the map camera to the last known location
        Stop stop = route.getNearestStop(direction);
        LatLng latLng = (stop == null)
                ? new LatLng(42.3604, -71.0580)
                : new LatLng(stop.getLocation().getLatitude(), stop.getLocation().getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

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
                selectedStopMarker = marker;
                route.setNearestStop(direction, (Stop) marker.getTag(), true);

                swipeRefreshLayout.setRefreshing(true);
                clearPredictions();
                forceUpdate();

                return false;
            }
        });

        // Set the map style
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Set the map UI settings
        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        // Load the route outline and stop markers
        getRouteShapes();
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
        refreshRecyclerView();
        refreshServiceAlertsView();

        // Get the route shapes if there aren't any
        if (routeShapes.size() == 0) {
            getRouteShapes();
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
        timer.schedule(new PredictionsUpdateTimerTask(),
                PREDICTIONS_UPDATE_RATE, PREDICTIONS_UPDATE_RATE);
        timer.schedule(new ServiceAlertsUpdateTimerTask(),
                SERVICE_ALERTS_UPDATE_RATE, SERVICE_ALERTS_UPDATE_RATE);
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

                    route.clearPredictions(Route.INBOUND);
                    route.clearPredictions(Route.OUTBOUND);
                    route.clearServiceAlerts();

                    refreshRecyclerView();
                    refreshServiceAlertsView();

                } else if (!errorManager.hasNetworkError()) {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onPostExecute(List<Prediction> predictions) {
        refreshing = false;
        refreshTime = new Date().getTime();

        route.clearPredictions(direction);
        route.addAllPredictions(predictions);

        refreshRecyclerView();
    }

    @Override
    public void onPostExecute(ServiceAlert[] serviceAlerts) {
        route.clearServiceAlerts();
        route.addAllServiceAlerts(serviceAlerts);

        refreshServiceAlertsView();
    }

    @Override
    public void onPostExecute(Shape[] shapes) {
        routeShapes.addAll(Arrays.asList(shapes));

        refreshShapes();
    }

    private void backgroundUpdate() {
        if (!refreshing) {
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

            if (predictionsAsyncTask != null) {
                predictionsAsyncTask.cancel(true);
            }

            if (route != null && route.getNearestStop(direction) != null) {
                predictionsAsyncTask = new RouteDetailPredictionsAsyncTask(realTimeApiKey, route,
                        direction, this);
                predictionsAsyncTask.execute();
            } else {
                onPostExecute(new ArrayList<Prediction>());
            }
        } else {
            errorManager.setNetworkError(true);
            refreshing = false;
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void refreshRecyclerView() {
        if (!userIsScrolling) {
            recyclerViewAdapter.setPredictions(route.getPredictions(direction));

            if (recyclerViewAdapter.getItemCount() == 0) {
                appBarLayout.setExpanded(true);
                recyclerView.setNestedScrollingEnabled(false);
                noPredictionsTextView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setNestedScrollingEnabled(true);
                noPredictionsTextView.setVisibility(View.GONE);
            }
        }

        if (!refreshing) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        noPredictionsTextView.setVisibility(View.GONE);
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
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

    private void refreshServiceAlertsView() {
        if (!userIsScrolling) {
            if (route.getServiceAlerts().size() > 0) {
                final ArrayList<ServiceAlert> serviceAlerts = route.getServiceAlerts();

                String alertsText = "";
                int alertsCount = 0;
                int advisoriesCount = 0;

                // Sort the service alerts
                Collections.sort(serviceAlerts);

                // Count how the number of alerts and advisories each
                // Alerts are new, ongoing service alerts; Advisories are all other alerts
                for (ServiceAlert alert : serviceAlerts) {
                    if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                            alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                        alertsCount++;
                    } else {
                        advisoriesCount++;
                    }
                }

                // Show the number of alerts
                if (alertsCount > 0) {
                    alertsText = (alertsCount > 1)
                            ? alertsCount + " " + getResources().getString(R.string.alerts)
                            : alertsCount + " " + getResources().getString(R.string.alert);
                }

                // Show the number of advisories
                if (advisoriesCount > 0) {
                    alertsText = (alertsCount > 0) ? alertsText + ", " : alertsText;

                    alertsText = (advisoriesCount > 1)
                            ? alertsText + advisoriesCount + " " + getResources().getString(R.string.advisories)
                            : alertsText + advisoriesCount + " " + getResources().getString(R.string.advisory);
                }

                // Set the service alerts view
                serviceAlertsTextView.setText(alertsText);

                // Display the appropriate service alerts icon
                // Red icon if there is at least one alert, otherwise grey icon
                if (alertsCount > 0) {
                    serviceAlertIcon.setVisibility(View.VISIBLE);
                    serviceAdvisoryIcon.setVisibility(View.GONE);
                } else {
                    serviceAlertIcon.setVisibility(View.GONE);
                    serviceAdvisoryIcon.setVisibility(View.VISIBLE);
                }

                // Set the onClickListener to display the service alerts details
                serviceAlertsLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();

                        dialog.setCustomTitle(new ServiceAlertsTitleView(view.getContext(),
                                (serviceAlerts.size() > 1)
                                        ? view.getContext().getString(R.string.service_alerts)
                                        : view.getContext().getString(R.string.service_alert),
                                Color.parseColor(route.getTextColor()),
                                Color.parseColor(route.getPrimaryColor()),
                                route.getMode() == Route.BUS &&
                                        !Routes.isSilverLine(route.getId())));

                        dialog.setView(new ServiceAlertsListView(view.getContext(), serviceAlerts));

                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.dialog_close_button),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });

                        dialog.show();
                    }
                });

                serviceAlertsLayout.setVisibility(View.VISIBLE);
            } else {
                serviceAlertsLayout.setVisibility(View.GONE);
            }
        }
    }

    private void refreshShapes() {
        if (!userIsScrolling && routeShapes.size() > 0) {
            gMap.clear();

            HashMap<String, Stop> distinctStops = new HashMap<>();

            for (Shape shape : routeShapes) {
                if (shape.getDirection() == direction && shape.getPriority() > -1 &&
                        shape.getStops().length > 1) {
                    drawRouteShape(shape);

                    for (Stop stop : shape.getStops()) {
                        distinctStops.put(stop.getId(), stop);
                    }
                }
            }

            Stop currentStop = route.getNearestStop(direction);
            selectedStopMarker = null;
            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

            for (Stop s : distinctStops.values()) {
                Marker currentMarker = drawStopMarker(s);
                stopMarkers.add(currentMarker);
                boundsBuilder.include(currentMarker.getPosition());

                if (currentStop != null && (currentStop.equals(s) ||
                        currentStop.isParentOf(s.getId()) || s.isParentOf(currentStop.getId()))) {
                    selectedStopMarker = currentMarker;
                }
            }

            if (selectedStopMarker != null) {
                selectedStopMarker.showInfoWindow();
                gMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(selectedStopMarker.getPosition(), 15));
            } else if (currentStop != null) {
                selectedStopMarker = drawStopMarker(currentStop);
                selectedStopMarker.showInfoWindow();
                boundsBuilder.include(selectedStopMarker.getPosition());
                gMap.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50));
            } else if (distinctStops.size() > 0) {
                gMap.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50));
            }

            mapProgressBar.setVisibility(View.GONE);
            shapesLoaded = true;
        }
    }

    private void getRouteShapes() {
        if (networkConnectivityClient.isConnected()) {
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

    private void drawRouteShape(@NonNull Shape shape) {
        List<LatLng> shapeCoordinates = PolyUtil.decode(shape.getPolyline());

        gMap.addPolyline(new PolylineOptions()
                .addAll(shapeCoordinates)
                .color(Color.parseColor("#FFFFFF"))
                .zIndex(0)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .width(14));

        gMap.addPolyline(new PolylineOptions()
                .addAll(shapeCoordinates)
                .color(Color.parseColor(route.getPrimaryColor()))
                .zIndex(1)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .width(8));
    }

    private Marker drawStopMarker(@NonNull Stop stop) {
        Marker stopMarker = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        stop.getLocation().getLatitude(), stop.getLocation().getLongitude()))
                .anchor(0.5f, 0.5f)
                .title(stop.getName())
                .zIndex(2)
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mbta_stop_icon)));

        stopMarker.setTag(stop);

        return stopMarker;
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
}
