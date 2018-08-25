package jackwtat.simplembta.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.ErrorManager;
import jackwtat.simplembta.R;
import jackwtat.simplembta.RawResourceReader;
import jackwtat.simplembta.adapters.PredictionsAdapter;
import jackwtat.simplembta.controllers.MapSearchController;
import jackwtat.simplembta.controllers.MapSearchController.OnProgressUpdateListener;
import jackwtat.simplembta.controllers.MapSearchController.OnPostExecuteListener;
import jackwtat.simplembta.controllers.MapSearchController.OnNetworkErrorListener;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Route;
import jackwtat.simplembta.mbta.structure.ServiceAlert;
import jackwtat.simplembta.mbta.structure.Stop;
import jackwtat.simplembta.views.AlertsListView;
import jackwtat.simplembta.views.RouteNameView;

public class MapSearchFragment extends RefreshableFragment implements OnMapReadyCallback {
    private final static String LOG_TAG = "MapSearchFragment";

    private final long AUTO_REFRESH_RATE = 45000;

    private View rootView;
    private AppBarLayout appBarLayout;
    private MapView mapView;
    private GoogleMap gMap;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AlertDialog alertDialog;

    private MapSearchController controller;
    private PredictionsAdapter predictionsAdapter;
    private ErrorManager errorManager;
    private Timer autoRefreshTimer;

    private boolean mapReady = false;
    private boolean mapCameraMoving = false;
    private Location lastLocation;

    private boolean autoScrollToTop = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        errorManager = ErrorManager.getErrorManager();

        controller = new MapSearchController(getContext(),
                new OnPostExecuteListener() {
                    public void onPostExecute(List<Stop> stops) {
                        predictionsAdapter.setPredictions(Prediction.getUniqueSortedPredictions(stops));

                        swipeRefreshLayout.setRefreshing(false);

                        if (autoScrollToTop) {
                            recyclerView.scrollToPosition(0);
                            appBarLayout.setExpanded(true);
                        }

                        errorManager.setNetworkError(false);
                    }
                },
                new OnProgressUpdateListener() {
                    public void onProgressUpdate(int progress) {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                },
                new OnNetworkErrorListener() {
                    public void onNetworkError() {
                        swipeRefreshLayout.setRefreshing(false);
                        predictionsAdapter.clear();
                        errorManager.setNetworkError(true);
                    }
                });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        lastLocation = new Location("");
        lastLocation.setLatitude(sharedPreferences.getFloat("latitude", (float) 42.3604));
        lastLocation.setLongitude(sharedPreferences.getFloat("longitude", (float) -71.0580));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map_search, container, false);

        // Get app bar and app bar params
        appBarLayout = rootView.findViewById(R.id.app_bar_layout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();

        // Set app bar height
        double height = getResources().getDisplayMetrics().heightPixels * .5;
        params.height = (int) height;

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
        swipeRefreshLayout.setEnabled(false);

        // Get recycler view
        recyclerView = rootView.findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        GridLayoutManager glm = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(glm);

        // Set recycler view predictions adapter
        predictionsAdapter = new PredictionsAdapter();
        predictionsAdapter.setOnItemClickListener(new PredictionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                Route route = predictionsAdapter.getRoute(i);

                if (route.hasServiceAlerts()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    ArrayList<ServiceAlert> alerts = route.getServiceAlerts();
                    Collections.sort(alerts);

                    RouteNameView routeNameView = new RouteNameView(getContext(), route,
                            getContext().getResources().getDimension(R.dimen.large_route_name_text_size), RouteNameView.SQUARE_BACKGROUND,
                            false, true);
                    routeNameView.setGravity(Gravity.CENTER);

                    builder.setCustomTitle(routeNameView);

                    builder.setView(new AlertsListView(getContext(), alerts));

                    builder.setPositiveButton(getResources().getString(R.string.dialog_close_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
        recyclerView.setAdapter(predictionsAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        autoRefresh();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        recyclerView.scrollToPosition(0);

        appBarLayout.setExpanded(true);

        if (new Date().getTime() - controller.getTimeOfLastRefresh() >
                controller.MAXIMUM_PREDICTION_AGE) {
            predictionsAdapter.clear();
        }

        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (alertDialog != null) {
            alertDialog.hide();
        }

        mapView.onPause();

        mapCameraMoving = false;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_map_search_latlon), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("latitude", (float) lastLocation.getLatitude());
        editor.putFloat("longitude", (float) lastLocation.getLongitude());
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        controller.cancel();

        autoRefreshTimer.cancel();

        swipeRefreshLayout.setRefreshing(false);
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
        mapReady = true;
        gMap = googleMap;

        gMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == REASON_GESTURE ||
                        reason == REASON_API_ANIMATION) {
                    mapCameraMoving = true;
                }
            }
        });

        gMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (mapCameraMoving) {
                    mapCameraMoving = false;

                    LatLng latLng = gMap.getCameraPosition().target;
                    lastLocation.setLatitude(latLng.latitude);
                    lastLocation.setLongitude(latLng.longitude);

                    controller.cancel();
                    forceRefresh();
                }
            }
        });

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
                            .width(7));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse route shape at index " + i);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse route shapes JSON");
        }

        UiSettings mapUiSettings = gMap.getUiSettings();
        mapUiSettings.setRotateGesturesEnabled(false);
        mapUiSettings.setTiltGesturesEnabled(false);
        mapUiSettings.setZoomControlsEnabled(true);

        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapUiSettings.setMyLocationButtonEnabled(true);
            gMap.setMyLocationEnabled(true);
        }

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 13));
        forceRefresh();
    }

    @Override
    public void refresh() {
        if (mapReady) {
            autoScrollToTop = true;
            controller.update(lastLocation);
        }
    }

    @Override
    public void autoRefresh() {
        if (mapReady) {
            autoScrollToTop = false;
            controller.forceUpdate(lastLocation);
        }
    }

    @Override
    public void forceRefresh() {
        if (mapReady) {
            autoScrollToTop = true;
            controller.forceUpdate(lastLocation);
        }
    }
}
