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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

public class MapSearchFragment extends Fragment implements OnMapReadyCallback {
    private final static String LOG_TAG = "MapSearchFragment";

    // Maximum age of prediction
    public static final long MAXIMUM_PREDICTION_AGE = 90000;

    // Time since last refresh before predictions can automatically refresh
    private static final long AUTO_REFRESH_RATE = 15000;

    // Time between location updates
    public static final long LOCATION_UPDATE_INTERVAL = 500;

    // Fastest time between location updates
    public static final long FASTEST_LOCATION_INTERVAL = 250;

    // Maximum elapsed time since time of last location query
    public static final long MAXIMUM_LOCATION_TIME = 90000;

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

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private LocationClient locationClient;
    private LocationClientCallbacks locationClientCallbacks;
    private MapSearchPredictionsAsyncTask predictionsAsyncTask;
    private MapSearchPredictionsAsyncTask.onPostExecuteListener predictionsOnPostExecuteListener;
    private MapSearchRecyclerViewAdapter recyclerViewAdapter;
    private ErrorManager errorManager;
    private Timer autoRefreshTimer;

    private boolean refreshing = false;
    private boolean mapReady = false;
    private boolean mapCameraMoving = false;
    private boolean userHasMovedMap = false;
    private boolean routeDetailOpened = false;
    private long refreshTime = 0;
    private long locationQueryTime = 0;
    private int locationAttemptsCount = 0;

    private List<Route> currentRoutes;
    private Location currentLocation;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get MBTA realTime API key
        realTimeApiKey = getContext().getString(R.string.v3_mbta_realtime_api_key);

        // Get error manager
        errorManager = ErrorManager.getErrorManager();

        // Initialize network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(getContext());

        // Get the location the user last viewed
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        currentLocation = new Location("");
        currentLocation.setLatitude(sharedPreferences.getFloat("latitude", (float) 42.3604));
        currentLocation.setLongitude(sharedPreferences.getFloat("longitude", (float) -71.0580));

        locationClient = new LocationClient(getContext(), LOCATION_UPDATE_INTERVAL,
                FASTEST_LOCATION_INTERVAL);

        locationClientCallbacks = new LocationClientCallbacks() {
            @Override
            public void onSuccess() {
                Location currentLocation = locationClient.getLastLocation();
                locationQueryTime = new Date().getTime();

                if (locationAttemptsCount < MAXIMUM_LOCATION_ATTEMPTS) {
                    if (locationQueryTime - currentLocation.getTime() < MAXIMUM_LOCATION_AGE) {
                        locationAttemptsCount = MAXIMUM_LOCATION_ATTEMPTS;
                        recenterMap(new LatLng(
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude()));
                    } else {
                        locationAttemptsCount++;
                    }

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            locationClient.updateLocation(locationClientCallbacks);
                        }
                    }, LOCATION_ATTEMPT_WAIT_TIME);
                } else {
                    recenterMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                    errorManager.setNetworkError(false);
                    errorManager.setLocationError(false);
                    errorManager.setLocationPermissionDenied(false);
                }
            }

            @Override
            public void onFailure() {
                errorManager.setLocationError(true);
                forceUpdate();
            }

            @Override
            public void onNoPermission() {
                errorManager.setLocationPermissionDenied(true);
                forceUpdate();
            }
        };

        predictionsOnPostExecuteListener = new MapSearchPredictionsAsyncTask.onPostExecuteListener() {
            @Override
            public void onPostExecute(List<Route> routes) {
                refreshing = false;

                refreshTime = new Date().getTime();

                currentRoutes = routes;

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
        };
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

        // Get and initialize map view
        mapView = rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

        // Set recycler view predictions adapter
        recyclerViewAdapter = new MapSearchRecyclerViewAdapter();

        recyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                routeDetailOpened = true;

                Intent intent = new Intent(getActivity(), RouteDetailActivity.class);
                intent.putExtra("route", recyclerViewAdapter.getData(position).getRoute());
                intent.putExtra("direction", recyclerViewAdapter.getData(position).getDirection());
                intent.putExtra("refreshTime", refreshTime);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);

        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);
        noPredictionsTextView.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        mapReady = true;

        // Set the action listeners
        gMap.setOnCameraMoveStartedListener(new OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                mapCameraMoving = true;
                userHasMovedMap = reason == REASON_GESTURE || reason == REASON_API_ANIMATION;
            }
        });
        gMap.setOnCameraIdleListener(new OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (mapCameraMoving) {
                    mapCameraMoving = false;

                    LatLng latLng = gMap.getCameraPosition().target;
                    currentLocation.setLatitude(latLng.latitude);
                    currentLocation.setLongitude(latLng.longitude);

                    forceUpdate();
                }
            }
        });
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                recenterMap(marker.getPosition());
                return false;
            }
        });

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

        // Get the subway, Silver Line, and commuter rail shapes
        Shape[] blueShapes = getShapesFromJson(R.raw.shape_blue);
        Shape[] orangeShapes = getShapesFromJson(R.raw.shape_orange);
        Shape[] redShapes = getShapesFromJson(R.raw.shape_red);
        Shape[] greenShapes = getShapesFromJson(R.raw.shape_green);
        Shape[] silverShapes = getShapesFromJson(R.raw.shape_silver);
        Shape[] mattapanShapes = getShapesFromJson(R.raw.shape_mattapan);

        // Draw the route shapes
        drawRouteShapes(blueShapes, getContext().getResources().getColor(R.color.blue_line));
        drawRouteShapes(orangeShapes, getContext().getResources().getColor(R.color.orange_line));
        drawRouteShapes(redShapes, getContext().getResources().getColor(R.color.red_line));
        drawRouteShapes(greenShapes, getContext().getResources().getColor(R.color.green_line));
        drawRouteShapes(silverShapes, getContext().getResources().getColor(R.color.silver_line));
        drawRouteShapes(mattapanShapes, getContext().getResources().getColor(R.color.red_line));

        // Draw the stop markers
        HashMap<String, Stop> distinctStops = new HashMap<>();

        distinctStops.putAll(getStopsFromShapes(blueShapes));
        distinctStops.putAll(getStopsFromShapes(orangeShapes));
        distinctStops.putAll(getStopsFromShapes(redShapes));
        distinctStops.putAll(getStopsFromShapes(greenShapes));
        distinctStops.putAll(getStopsFromShapes(silverShapes));
        distinctStops.putAll(getStopsFromShapes(mattapanShapes));

        drawStopMarkers(distinctStops.values());
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
                        backgroundUpdate();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        // Force recycler view to update UI so that the minutes are accurate
        if (currentRoutes != null) {
            recyclerViewAdapter.setRoutes(currentRoutes);
        }

        // If the user navigated to Route Detail and too much time has elapsed when they returned,
        // then clear the predictions and force a refresh
        Long onResumeTime = new Date().getTime();
        if (routeDetailOpened && onResumeTime - refreshTime > MAXIMUM_PREDICTION_AGE) {
            clearPredictions();
            forceUpdate();

            // If too much time has elapsed since the user has navigated away from the app,
            // then clear the predictions and update the location to force a refresh
        } else if (!routeDetailOpened && onResumeTime - locationQueryTime > MAXIMUM_LOCATION_TIME) {
            clearPredictions();
            refreshLocation();

            // If there are no predictions displayed in the recycler view, then force a refresh
        } else if (recyclerViewAdapter.getItemCount() < 1) {
            forceUpdate();

        } else {
            backgroundUpdate();
        }

        routeDetailOpened = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        mapCameraMoving = false;

        // Save the location the user last viewed
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

        refreshing = false;

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        locationClient.disconnect();

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

    private void refreshLocation() {
        swipeRefreshLayout.setRefreshing(true);

        locationAttemptsCount = 0;

        locationClient.updateLocation(locationClientCallbacks);
    }

    private void recenterMap(LatLng latLng) {
        if (mapReady && !userHasMovedMap) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        noPredictionsTextView.setVisibility(View.GONE);
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void getPredictions() {
        if (!networkConnectivityClient.isConnected()) {
            refreshing = false;
            errorManager.setNetworkError(true);
            swipeRefreshLayout.setRefreshing(false);
            clearPredictions();
        } else {
            refreshing = true;

            if (predictionsAsyncTask != null) {
                predictionsAsyncTask.cancel(true);
            }

            predictionsAsyncTask = new MapSearchPredictionsAsyncTask(
                    realTimeApiKey, currentLocation, predictionsOnPostExecuteListener);
            predictionsAsyncTask.execute();
        }
    }

    private void backgroundUpdate() {
        if (!refreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        swipeRefreshLayout.setRefreshing(true);
        getPredictions();
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

    private void drawRouteShapes(Shape[] shapes, int color) {
        for (Shape s : shapes) {
            List<LatLng> shapeCoordinates = PolyUtil.decode(s.getPolyline());

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
                    .color(color)
                    .zIndex(1)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .width(8));
        }
    }

    private void drawStopMarkers(Collection<Stop> stops) {
        for (Stop stop : stops) {
            Marker stopMarker = gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            stop.getLocation().getLatitude(), stop.getLocation().getLongitude()))
                    .anchor(0.5f, 0.5f)
                    .title(stop.getName())
                    .zIndex(2)
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mbta_stop_icon)));

            stopMarker.setTag(stop);
        }
    }
}
