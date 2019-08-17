package jackwtat.simplembta.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.activities.MainActivity;
import jackwtat.simplembta.activities.RouteDetailActivity;
import jackwtat.simplembta.adapters.RouteSearchRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.RouteSearchPredictionsAsyncTask;
import jackwtat.simplembta.asyncTasks.RoutesAsyncTask;
import jackwtat.simplembta.asyncTasks.ServiceAlertsAsyncTask;
import jackwtat.simplembta.asyncTasks.ShapesAsyncTask;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.views.RouteSearchSpinners;
import jackwtat.simplembta.views.ServiceAlertsIndicatorView;

public class RouteSearchFragment extends Fragment implements
        ErrorManager.OnErrorChangedListener,
        RouteSearchSpinners.OnRouteSelectedListener,
        RouteSearchSpinners.OnDirectionSelectedListener,
        RouteSearchSpinners.OnStopSelectedListener,
        MainActivity.OutsideQueryListener {
    public static final String LOG_TAG = "RouteSearchFragment";

    // Maximum age of prediction
    public static final long MAX_PREDICTION_AGE = 90000;

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    // Service alerts auto update rate
    public static final long SERVICE_ALERTS_UPDATE_RATE = 60000;

    private RouteSearchSpinners searchSpinners;
    private ServiceAlertsIndicatorView serviceAlertsIndicatorView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;
    private TextView errorTextView;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private RoutesAsyncTask routesAsyncTask;
    private ShapesAsyncTask shapesAsyncTask;
    private ServiceAlertsAsyncTask serviceAlertsAsyncTask;
    private RouteSearchPredictionsAsyncTask predictionsAsyncTask;
    private ErrorManager errorManager;
    private RouteSearchRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private boolean dataRefreshing = false;
    private boolean viewsRefreshing = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private ArrayList<Route> allRoutes = new ArrayList<>();
    private Route selectedRoute;
    private int selectedDirectionId;

    private boolean queryInProgress = false;
    private Route queryRoute = null;
    private int queryDirectionId = 0;
    private Stop queryStop = null;
    private Location queryLocation = null;

    private String savedRouteId;
    private String[] savedStopIds = new String[2];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get MBTA realTime API key
        realTimeApiKey = getContext().getString(R.string.v3_mbta_realtime_api_key);

        // Initialize network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(getContext());

        // Get the route and stop the user last viewed
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                getResources().getString(R.string.saved_route_search_route), Context.MODE_PRIVATE);
        savedRouteId = sharedPreferences.getString("routeId", null);
        savedStopIds[0] = sharedPreferences.getString("stopId_0", null);
        savedStopIds[1] = sharedPreferences.getString("stopId_1", null);
        selectedDirectionId = sharedPreferences.getInt("directionId", Direction.INBOUND);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route_search, container, false);

        // Get the spinner
        searchSpinners = rootView.findViewById(R.id.route_search_spinners);
        searchSpinners.setOnRouteSelectedListener(this);
        searchSpinners.setOnDirectionSelectedListener(this);
        searchSpinners.setOnStopSelectedListener(this);
        searchSpinners.setOnMapButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RouteDetailActivity.class);
                intent.putExtra("route", selectedRoute);
                intent.putExtra("direction", selectedDirectionId);
                intent.putExtra("refreshTime", refreshTime);
                startActivity(intent);
            }
        });

        // Get service alerts indicator
        serviceAlertsIndicatorView = rootView.findViewById(R.id.service_alerts_indicator_view);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Set the no predictions indicator
        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);

        // Get recycler view
        recyclerView = rootView.findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Disable recycler view scrolling until predictions loaded;
        recyclerView.setNestedScrollingEnabled(false);

        // Add onScrollListener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    if (!viewsRefreshing) {
                        refreshPredictions(false);
                    }

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new RouteSearchRecyclerViewAdapter();
        recyclerViewAdapter.enableVehicleNumber(false);
        recyclerView.setAdapter(recyclerViewAdapter);

        // Set the onClickListener listener
        /*recyclerViewAdapter.setOnItemClickListener(new RouteDetailRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Prediction prediction = recyclerViewAdapter.getPrediction(position);

                AlertDialog dialog = new AlertDialog.Builder(getContext()).create();

                TextView testTextView = new TextView(getContext());

                String testText = "";

                testText += "Trip: " + prediction.getTripId();
                testText += "\nVehicle: " + prediction.getVehicleId();
                testText += "\nDestination: " + prediction.getDestination();
                testText += "\nDeparture: " + new SimpleDateFormat("h:mm:ss").format(prediction.getPredictionTime());

                testTextView.setText(testText);

                dialog.setView(testTextView);

                dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.dialog_close_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                dialog.show();
            }
        });*/

        // Set the error text message
        errorTextView = rootView.findViewById(R.id.error_message_text_view);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get error manager
        errorManager = ErrorManager.getErrorManager();
        errorManager.registerOnErrorChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (allRoutes == null || allRoutes.size() == 0) {
            getRoutes();
        }

        // Refresh the activity to update UI so that the predictions and service alerts are accurate
        // as of the last update
        refreshPredictions(false);
        refreshServiceAlerts();

        // If too much time has elapsed since last refresh, then clear predictions and force update
        if (new Date().getTime() - refreshTime > MAX_PREDICTION_AGE) {
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
        timer.schedule(new ServiceAlertsUpdateTimerTask(), 0, SERVICE_ALERTS_UPDATE_RATE);
        timer.schedule(new PredictionsUpdateTimerTask(), 0, PREDICTIONS_UPDATE_RATE);

        MainActivity.registerOutsideQueryListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        dataRefreshing = false;

        swipeRefreshLayout.setRefreshing(false);

        if (routesAsyncTask != null) {
            routesAsyncTask.cancel(true);
        }

        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (timer != null) {
            timer.cancel();
        }

        // Save the location the user last viewed
        if (selectedRoute != null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                    getResources().getString(R.string.saved_route_search_route), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("routeId", selectedRoute.getId());
            editor.putInt("directionId", selectedDirectionId);

            for (int i = 0; i < 2; i++) {
                if (selectedRoute.getNearestStop(i) != null) {
                    editor.putString("stopId_" + i, selectedRoute.getNearestStop(i).getId());
                }
            }

            editor.apply();
        }

        MainActivity.deregisterOutsideQueryListener();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onErrorChanged() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (errorManager.hasNetworkError()) {
                        errorTextView.setText(R.string.network_error_text);
                        errorTextView.setVisibility(View.VISIBLE);

                        if (selectedRoute != null) {
                            selectedRoute.clearPredictions(Direction.INBOUND);
                            selectedRoute.clearPredictions(Direction.OUTBOUND);
                            selectedRoute.clearServiceAlerts();

                            refreshPredictions(true);
                            refreshServiceAlerts();
                        }
                    } else {
                        errorTextView.setVisibility(View.GONE);

                        if (allRoutes == null || allRoutes.size() == 0) {
                            getRoutes();
                        } else if (selectedRoute.getStops(selectedDirectionId).length == 0) {
                            Route route = selectedRoute;
                            searchSpinners.clearRoutes();
                            searchSpinners.populateRouteSpinner(
                                    allRoutes.toArray(new Route[0]));
                            searchSpinners.selectRoute(route.getId());
                        } else {
                            swipeRefreshLayout.setRefreshing(true);
                            getPredictions();
                        }
                    }
                }
            });
        }
    }

    private void getRoutes() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (routesAsyncTask != null) {
                routesAsyncTask.cancel(true);
            }

            routesAsyncTask = new RoutesAsyncTask(realTimeApiKey, new RoutesPostExecuteListener());
            routesAsyncTask.execute();

        } else {
            errorManager.setNetworkError(true);
        }
    }

    private void getShapes() {
        if (selectedRoute != null) {
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
    }

    private void getServiceAlerts() {
        if (selectedRoute != null) {
            if (networkConnectivityClient.isConnected()) {
                errorManager.setNetworkError(false);

                if (serviceAlertsAsyncTask != null) {
                    serviceAlertsAsyncTask.cancel(true);
                }

                String[] routeId = {selectedRoute.getId()};

                serviceAlertsAsyncTask = new ServiceAlertsAsyncTask(
                        realTimeApiKey, routeId, new ServiceAlertsPostExecuteListener());
                serviceAlertsAsyncTask.execute();

            } else {
                errorManager.setNetworkError(true);
            }
        }
    }

    private void getPredictions() {
        if (selectedRoute != null) {
            if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
                if (networkConnectivityClient.isConnected()) {
                    errorManager.setNetworkError(false);

                    dataRefreshing = true;

                    if (predictionsAsyncTask != null) {
                        predictionsAsyncTask.cancel(true);
                    }

                    predictionsAsyncTask = new RouteSearchPredictionsAsyncTask(realTimeApiKey,
                            selectedRoute, selectedDirectionId, new PredictionsPostExecuteListener());
                    predictionsAsyncTask.execute();

                } else {
                    errorManager.setNetworkError(true);
                    dataRefreshing = false;
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshPredictions(true);
                        }
                    });
                }
            }
        }
    }

    private void refreshRoutes() {
        populateRouteSpinner(allRoutes.toArray(new Route[0]));
    }

    private void refreshShapes() {
        if (selectedRoute != null) {
            populateStopSpinner(selectedRoute.getStops(selectedDirectionId));
        }
    }

    private void refreshServiceAlerts() {
        if (selectedRoute != null) {
            if (!userIsScrolling) {
                if (selectedRoute.getServiceAlerts().size() > 0) {
                    serviceAlertsIndicatorView.setServiceAlerts(selectedRoute);
                    serviceAlertsIndicatorView.setVisibility(View.VISIBLE);

                } else {
                    serviceAlertsIndicatorView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void refreshPredictions(boolean returnToTop) {
        if (!userIsScrolling) {
            if (selectedRoute != null) {
                if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
                    recyclerViewAdapter.setPredictions(selectedRoute.getPredictions(selectedDirectionId));
                    swipeRefreshLayout.setRefreshing(false);

                    if (recyclerViewAdapter.getItemCount() == 0) {
                        noPredictionsTextView.setText(getResources()
                                .getString(R.string.no_predictions_this_stop));
                        noPredictionsTextView.setVisibility(View.VISIBLE);

                        recyclerView.setNestedScrollingEnabled(false);
                    } else {
                        noPredictionsTextView.setVisibility(View.GONE);
                        recyclerView.setNestedScrollingEnabled(true);
                    }

                    if (returnToTop) {
                        recyclerView.scrollToPosition(0);
                    }
                } else {
                    noPredictionsTextView.setText(getResources().getString(R.string.no_stops));
                    noPredictionsTextView.setVisibility(View.VISIBLE);

                    recyclerView.setNestedScrollingEnabled(false);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        noPredictionsTextView.setVisibility(View.GONE);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private Shape[] getShapesFromJson(int jsonFile) {
        return ShapesJsonParser.parse(RawResourceReader.toString(getResources().openRawResource(jsonFile)));
    }

    private void populateRouteSpinner(Route[] routes) {
        searchSpinners.populateRouteSpinner(routes);

        if (queryInProgress && queryRoute != null) {
            searchSpinners.selectRoute(queryRoute.getId());

        } else if (selectedRoute != null) {
            searchSpinners.selectRoute(selectedRoute.getId());

        } else if (savedRouteId != null) {
            searchSpinners.selectRoute(savedRouteId);
        }
    }

    private void populateDirectionSpinner(Direction[] directions) {
        if (directions[0].getId() == Direction.SOUTHBOUND) {
            Direction d = directions[0];
            directions[0] = directions[1];
            directions[1] = d;
        }

        searchSpinners.populateDirectionSpinner(directions);

        if (queryInProgress) {
            searchSpinners.selectDirection(queryDirectionId);
        } else {
            searchSpinners.selectDirection(selectedDirectionId);
        }
    }

    private void populateStopSpinner(Stop[] stops) {
        searchSpinners.populateStopSpinner(stops);

        if (queryInProgress) {
            if (queryStop != null) {
                searchSpinners.selectStop(queryStop.getId());
            } else if (queryLocation != null) {
                Stop stop = getStopFromLocation(selectedRoute, selectedDirectionId, queryLocation);
                if (stop != null) {
                    searchSpinners.selectStop(stop.getId());
                }
            }
            queryInProgress = false;

        } else {
            if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
                searchSpinners.selectStop(selectedRoute.getNearestStop(selectedDirectionId).getId());
            } else if (savedStopIds != null) {
                searchSpinners.selectStop(savedStopIds[selectedDirectionId]);
            }
        }
    }

    @Override
    public void onRouteSelected(Route route) {
        selectedRoute = route;

        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        if (serviceAlertsAsyncTask != null) {
            serviceAlertsAsyncTask.cancel(true);
        }

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        refreshServiceAlerts();
        getServiceAlerts();

        populateDirectionSpinner(selectedRoute.getAllDirections());
    }

    @Override
    public void onDirectionSelected(Direction direction) {
        selectedDirectionId = direction.getId();

        clearPredictions();

        if (selectedRoute.getShapes(selectedDirectionId).length == 0) {
            searchSpinners.clearStops();
            swipeRefreshLayout.setRefreshing(true);
            getShapes();
        } else {
            populateStopSpinner(selectedRoute.getStops(selectedDirectionId));
        }
    }

    @Override
    public void onStopSelected(Stop stop) {
        Stop selectedStop = stop;
        selectedRoute.setNearestStop(selectedDirectionId, stop);

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

        // Clear the current predictions and get the predictions for the selected stop
        clearPredictions();
        swipeRefreshLayout.setRefreshing(true);
        forceUpdate();
    }

    @Override
    public void outsideQuery(Route route, int directionId, Location location) {
        queryRoute = route;
        queryDirectionId = directionId;
        queryStop = null;
        queryLocation = location;

        executeQuery();
    }

    private void executeQuery() {
        queryInProgress = true;

        boolean routeExists = false;
        for (Route r : allRoutes) {
            if (r.getId().equals(queryRoute.getId())) {
                routeExists = true;
            }
        }

        if (!routeExists) {
            allRoutes.add(queryRoute);
            Collections.sort(allRoutes);
            populateRouteSpinner(allRoutes.toArray(new Route[0]));

        } else if (!selectedRoute.equals(queryRoute)) {
            searchSpinners.selectRoute(queryRoute.getId());

        } else if (selectedDirectionId != queryDirectionId) {
            searchSpinners.selectDirection(queryDirectionId);

        } else if (queryStop != null) {
            searchSpinners.selectStop(queryStop.getId());

        } else if (queryLocation != null && selectedRoute.getStops(selectedDirectionId) != null) {
            Stop stop = getStopFromLocation(selectedRoute, selectedDirectionId, queryLocation);
            if (stop != null) {
                searchSpinners.selectStop(stop.getId());
            } else {
                queryInProgress = false;
            }

        } else {
            queryInProgress = false;
        }
    }

    private Stop getStopFromLocation(Route route, int directionId, Location location) {
        Stop[] stops = route.getStops(directionId);
        if (stops != null && stops.length > 0) {
            Stop nearestStop = stops[0];
            double nearestDistance = stops[0].getLocation().distanceTo(location);

            for (int i = 1; i < stops.length; i++) {
                double d = stops[i].getLocation().distanceTo(location);
                if (stops[i].getLocation().distanceTo(location) < nearestDistance) {
                    nearestStop = stops[i];
                    nearestDistance = d;
                }
            }

            return nearestStop;

        } else {
            return null;
        }
    }

    private void backgroundUpdate() {
        if (!dataRefreshing) {
            getPredictions();
        }
    }

    private void forceUpdate() {
        getPredictions();
    }

    private class RoutesPostExecuteListener implements RoutesAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Route[] routes) {
            allRoutes.clear();
            allRoutes.addAll(Arrays.asList(routes));
            Collections.sort(allRoutes);

            refreshRoutes();
        }

        @Override
        public void onError() {
            getRoutes();
        }
    }

    private class ShapesPostExecuteListener implements ShapesAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Shape[] shapes) {
            selectedRoute.addShapes(shapes);

            refreshShapes();
        }

        @Override
        public void onError() {
            getShapes();
        }
    }

    private class ServiceAlertsPostExecuteListener implements ServiceAlertsAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(ServiceAlert[] serviceAlerts) {
            selectedRoute.clearServiceAlerts();
            selectedRoute.addAllServiceAlerts(serviceAlerts);

            refreshServiceAlerts();
        }

        @Override
        public void onError() {
            getServiceAlerts();
        }
    }

    private class PredictionsPostExecuteListener implements RouteSearchPredictionsAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(List<Prediction> predictions) {
            dataRefreshing = false;
            refreshTime = new Date().getTime();

            // Lock the views to prevent UI changes while loading new data to views
            viewsRefreshing = true;

            // Load new data to views
            selectedRoute.clearPredictions(0);
            selectedRoute.clearPredictions(1);
            selectedRoute.addAllPredictions(predictions);

            // Unlock views
            viewsRefreshing = false;

            refreshPredictions(false);
        }

        @Override
        public void onError() {
            dataRefreshing = false;
            refreshTime = new Date().getTime();

            selectedRoute.clearPredictions(0);
            selectedRoute.clearPredictions(1);

            refreshPredictions(true);
        }
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
