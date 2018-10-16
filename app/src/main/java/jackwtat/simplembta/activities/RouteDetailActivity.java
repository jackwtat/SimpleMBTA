package jackwtat.simplembta.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.RouteDetailRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.RouteDetailPredictionsAsyncTask;
import jackwtat.simplembta.asyncTasks.ShapesAsyncTask;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.ServiceAlertsListView;
import jackwtat.simplembta.views.RouteNameView;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Time since last refresh before values can automatically refresh onResume, in milliseconds
    private final long AUTO_REFRESH_RATE = 15000;

    // Maximum age of prediction
    public static final long MAXIMUM_PREDICTION_AGE = 90000;

    private String realTimeApiKey;
    private RouteDetailPredictionsAsyncTask predictionsAsyncTask;
    private RouteDetailPredictionsAsyncTask.Callbacks predictionsCallbacks;
    private ShapesAsyncTask shapesAsyncTask;
    private ShapesAsyncTask.Callbacks shapesCallbacks;

    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;

    private RouteDetailRecyclerViewAdapter recyclerViewAdapter;
    private Timer autoRefreshTimer;
    private long refreshTime;
    private boolean refreshing;

    private Route route;
    private int direction;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        Intent intent = getIntent();
        route = (Route) intent.getSerializableExtra("route");
        direction = intent.getIntExtra("direction", Route.NULL_DIRECTION);
        refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);

        realTimeApiKey = getResources().getString(R.string.v3_mbta_realtime_api_key);

        // Set action bar
        setTitle(route.getLongDisplayName(this));
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
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(route.getPrimaryColor())));
        }

        // Get app bar and app bar params
        appBarLayout = findViewById(R.id.app_bar_layout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();

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

        // Initialize service alerts
        initializeServiceAlerts();

        // Initialize map view
        mapView = findViewById(R.id.map_view);
        mapView.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * .6);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get and set swipe refresh layout
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

        // Create and set the recycler view adapter
        recyclerViewAdapter = new RouteDetailRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);

        // Load predictions from the current route
        loadPredictions(route.getPredictions(direction));

        // Create the predictions async task callbacks
        predictionsCallbacks = new RouteDetailPredictionsAsyncTask.Callbacks() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(List<Prediction> predictions) {
                swipeRefreshLayout.setRefreshing(false);

                refreshTime = new Date().getTime();

                refreshing = false;

                route.clearPredictions(direction);

                route.addPredictions(predictions);

                loadPredictions(predictions);
            }
        };

        // Create the shapes async task callbacks
        shapesCallbacks = new ShapesAsyncTask.Callbacks() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void onPostExecute(Shape[] shapes) {
                HashMap<String, Stop> distinctStops = new HashMap<>();

                for (Shape shape : shapes) {
                    if (shape.getDirection() == direction && shape.getPriority() > -1 &&
                            shape.getStops().length > 1) {
                        drawRouteShape(route, shape);

                        for (Stop stop : shape.getStops()) {
                            if (!distinctStops.containsKey(stop.getId())) {
                                distinctStops.put(stop.getId(), stop);
                            }
                        }
                    }
                }

                Marker selectedStopMarker = null;
                LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();

                Stop currentStop = route.getNearestStop(direction);

                for (Stop s : distinctStops.values()) {
                    Marker currentMarker = drawStopMarker(s);
                    boundsBuilder.include(new LatLng(
                            s.getLocation().getLatitude(), s.getLocation().getLongitude()));

                    if (currentStop != null && (currentStop.equals(s) || currentStop.isParentOf(s.getId()) ||
                            s.isParentOf(currentStop.getId()))) {
                        selectedStopMarker = currentMarker;
                    }
                }

                if (selectedStopMarker != null) {
                    selectedStopMarker.showInfoWindow();
                } else if (distinctStops.size() > 0) {
                    gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50));
                } else if (currentStop != null) {
                    drawStopMarker(currentStop).showInfoWindow();
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Set the action listener
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                route.setNearestStop(direction, (Stop) marker.getTag());

                clearPredictions();

                swipeRefreshLayout.setRefreshing(true);

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

        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        backgroundUpdate();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        // Force recycler view to update UI so that the minutes are accurate
        if (route != null) {
            recyclerViewAdapter.setPredictions(route.getPredictions(direction));
        }

        if (new Date().getTime() - refreshTime > MAXIMUM_PREDICTION_AGE) {
            clearPredictions();

            swipeRefreshLayout.setRefreshing(true);

            forceUpdate();

            // If there are no predictions displayed in the recycler view, then force a refresh
        } else if (recyclerViewAdapter.getItemCount() < 1) {
            swipeRefreshLayout.setRefreshing(true);

            forceUpdate();

        } else {
            backgroundUpdate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        autoRefreshTimer.cancel();
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

    private void initializeServiceAlerts() {
        LinearLayout serviceAlertsLayout = findViewById(R.id.service_alerts_layout);
        ImageView serviceAlertIcon = findViewById(R.id.service_alert_icon);
        ImageView serviceAdvisoryIcon = findViewById(R.id.service_advisory_icon);
        TextView serviceAlertsTextView = findViewById(R.id.service_alerts_text_view);

        final ArrayList<ServiceAlert> serviceAlerts = route.getServiceAlerts();
        String alertsText = "";
        int alertsCount = 0;
        int advisoriesCount = 0;

        for (ServiceAlert alert : serviceAlerts) {
            if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                    alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                alertsCount++;
            } else {
                advisoriesCount++;
            }
        }

        if (alertsCount == 1) {
            alertsText += alertsCount + " " + getResources().getString(R.string.alert);
        } else if (alertsCount > 1) {
            alertsText += alertsCount + " " + getResources().getString(R.string.alerts);
        }

        if (advisoriesCount > 0) {
            if (!alertsText.equals("")) {
                alertsText += ", ";
            }

            if (advisoriesCount == 1) {
                alertsText += advisoriesCount + " " + getResources().getString(R.string.advisory);
            } else {
                alertsText += advisoriesCount + " " + getResources().getString(R.string.advisories);
            }
        }

        if (alertsCount + advisoriesCount == 0) {
            serviceAlertsLayout.setVisibility(View.GONE);
        } else {
            serviceAlertsTextView.setText(alertsText);

            if (alertsCount > 0) {
                serviceAlertIcon.setVisibility(View.VISIBLE);
                serviceAdvisoryIcon.setVisibility(View.GONE);
            } else {
                serviceAlertIcon.setVisibility(View.GONE);
                serviceAdvisoryIcon.setVisibility(View.VISIBLE);
            }
            serviceAlertsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    createServiceAlertDialog(view.getContext(), serviceAlerts).show();
                }
            });
        }
    }

    private AlertDialog createServiceAlertDialog(Context context, List<ServiceAlert> serviceAlerts) {
        Collections.sort(serviceAlerts);

        AlertDialog serviceAlertDialog = new AlertDialog.Builder(context).create();

        RouteNameView routeNameView = new RouteNameView(context, route,
                context.getResources().getDimension(
                        R.dimen.large_route_name_text_size), RouteNameView.SQUARE_BACKGROUND,
                false, true);
        routeNameView.setGravity(Gravity.CENTER);

        serviceAlertDialog.setCustomTitle(routeNameView);

        serviceAlertDialog.setView(new ServiceAlertsListView(context, serviceAlerts));

        serviceAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.dialog_close_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        return serviceAlertDialog;
    }

    private void backgroundUpdate() {
        if (route != null && route.getNearestStop(direction) != null && !refreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        if (route != null && route.getNearestStop(direction) != null) {
            getPredictions();
        }
    }

    private void getPredictions() {
        refreshing = true;

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        predictionsAsyncTask = new RouteDetailPredictionsAsyncTask(realTimeApiKey, route, direction,
                predictionsCallbacks);
        predictionsAsyncTask.execute();
    }

    private void loadPredictions(List<Prediction> predictions) {
        recyclerViewAdapter.setPredictions(predictions);

        if (recyclerViewAdapter.getItemCount() == 0) {
            recyclerView.setNestedScrollingEnabled(false);
            noPredictionsTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setNestedScrollingEnabled(true);
            noPredictionsTextView.setVisibility(View.GONE);
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        noPredictionsTextView.setVisibility(View.GONE);
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void getRouteShapes() {
        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        shapesAsyncTask = new ShapesAsyncTask(
                realTimeApiKey,
                route.getId(),
                shapesCallbacks);
        shapesAsyncTask.execute();
    }

    private void drawRouteShape(Route route, Shape shape) {
        List<LatLng> shapeCoordinates = PolyUtil.decode(shape.getPolyline());

        int lineColor;
        if (route.getMode() != Route.BUS || Route.isSilverLine(route.getId())) {
            lineColor = Color.parseColor(route.getPrimaryColor());
        } else {
            lineColor = getResources().getColor(R.color.colorPrimary);
        }

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
                .color(lineColor)
                .zIndex(1)
                .jointType(JointType.ROUND)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .width(8));

    }

    private Marker drawStopMarker(Stop stop) {
        Marker stopMarker = gMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        stop.getLocation().getLatitude(), stop.getLocation().getLongitude()))
                .title(stop.getName())
                .zIndex(2)
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mbta_stop_icon)));

        stopMarker.setTag(stop);

        return stopMarker;
    }
}
