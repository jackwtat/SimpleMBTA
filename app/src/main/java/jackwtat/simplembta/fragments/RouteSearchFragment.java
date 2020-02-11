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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.activities.MainActivity;
import jackwtat.simplembta.activities.TripDetailActivity;
import jackwtat.simplembta.adapters.RouteSearchRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.PredictionsRouteSearchAsyncTask;
import jackwtat.simplembta.asyncTasks.RoutesAsyncTask;
import jackwtat.simplembta.asyncTasks.ServiceAlertsAsyncTask;
import jackwtat.simplembta.asyncTasks.ShapesAsyncTask;
import jackwtat.simplembta.asyncTasks.VehiclesByRouteAsyncTask;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.Constants;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.PastDataHolder;
import jackwtat.simplembta.views.NoPredictionsView;
import jackwtat.simplembta.views.RouteSearchSpinners;
import jackwtat.simplembta.views.ServiceAlertsIndicatorView;

public class RouteSearchFragment extends Fragment implements
        ErrorManager.OnErrorChangedListener,
        RouteSearchSpinners.OnRouteSelectedListener,
        RouteSearchSpinners.OnDirectionSelectedListener,
        RouteSearchSpinners.OnStopSelectedListener,
        MainActivity.OutsideQueryListener,
        Constants {
    public static final String LOG_TAG = "RouteSearchFragment";

    private RouteSearchSpinners searchSpinners;
    private ServiceAlertsIndicatorView serviceAlertsIndicatorView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NoPredictionsView noPredictionsView;
    private TextView errorTextView;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private ErrorManager errorManager;
    private RouteSearchRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private RoutesAsyncTask routesAsyncTask;
    private ShapesAsyncTask shapesAsyncTask;
    private PredictionsRouteSearchAsyncTask predictionsAsyncTask;
    private ServiceAlertsAsyncTask serviceAlertsAsyncTask;
    private VehiclesByRouteAsyncTask vehiclesAsyncTask;

    private boolean dataRefreshing = false;
    private boolean viewsRefreshing = false;
    private boolean userIsScrolling = false;
    private boolean loaded = false;
    private long refreshTime = 0;

    private ArrayList<Route> allRoutes = new ArrayList<>();
    private Route selectedRoute;
    private int selectedDirectionId;

    private PastDataHolder pastData = PastDataHolder.getHolder();

    private boolean queryInProgress = false;
    private Route queryRoute = null;
    private int queryDirectionId = 0;
    private Stop queryStop = null;
    private Location queryLocation = null;

    private String savedRouteId;
    private String[] savedStopIds = new String[2];

    private HashMap<String, Vehicle> vehicles = new HashMap<>();
    private HashMap<String, Vehicle> vehicleTrips = new HashMap<>();

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

        // Get service alerts indicator
        serviceAlertsIndicatorView = rootView.findViewById(R.id.service_alerts_indicator_view);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Set the no predictions indicator
        noPredictionsView = rootView.findViewById(R.id.no_predictions_view);

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
        recyclerView.setAdapter(recyclerViewAdapter);

        // Set the onClickListener listener
        recyclerViewAdapter.setOnItemClickListener(new RouteSearchRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Prediction prediction = recyclerViewAdapter.getPrediction(position);

                if (prediction != null) {
                    Intent intent = new Intent(getActivity(), TripDetailActivity.class);
                    intent.putExtra("route", prediction.getRoute());
                    intent.putExtra("stop", prediction.getStop());
                    intent.putExtra("trip", prediction.getTripId());
                    intent.putExtra("name", prediction.getTripName());
                    intent.putExtra("destination", prediction.getDestination());
                    intent.putExtra("vehicle", prediction.getVehicleId());
                    intent.putExtra("date", prediction.getPredictionTime());
                    startActivity(intent);
                }
            }
        });

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
        timer.schedule(new ServiceAlertsUpdateTimerTask(), 0, SERVICE_ALERTS_UPDATE_RATE);
        timer.schedule(new PredictionsUpdateTimerTask(), 0, PREDICTIONS_UPDATE_RATE);
        timer.schedule(new VehiclesUpdateTimerTask(), 0, VEHICLES_UPDATE_RATE);

        MainActivity.registerOutsideQueryListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        dataRefreshing = false;

        swipeRefreshLayout.setRefreshing(false);

        if (timer != null) {
            timer.cancel();
        }

        cancelUpdate();

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

                            clearPredictions();
                            enableOnErrorView(getResources().getString(R.string.network_error_text));
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
                            forceUpdate();
                            getVehicles();
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
            enableOnErrorView(getResources().getString(R.string.error_network));
        }
    }

    private void getShapes() {
        if (selectedRoute != null) {

            if (networkConnectivityClient.isConnected()) {
                errorManager.setNetworkError(false);

                if (shapesAsyncTask != null) {
                    shapesAsyncTask.cancel(true);
                }

                shapesAsyncTask = new ShapesAsyncTask(
                        realTimeApiKey, selectedRoute.getId(), new ShapesPostExecuteListener());
                shapesAsyncTask.execute();

            } else {
                errorManager.setNetworkError(true);
                enableOnErrorView(getResources().getString(R.string.error_network));
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

                    predictionsAsyncTask = new PredictionsRouteSearchAsyncTask(realTimeApiKey,
                            selectedRoute, selectedDirectionId, new PredictionsPostExecuteListener());
                    predictionsAsyncTask.execute();

                } else {
                    dataRefreshing = false;
                    errorManager.setNetworkError(true);
                    enableOnErrorView(getResources().getString(R.string.error_network));
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

    private void getVehicles() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (vehiclesAsyncTask != null) {
                vehiclesAsyncTask.cancel(true);
            }

            if (selectedRoute != null && selectedRoute.getId() != null) {
                vehiclesAsyncTask = new VehiclesByRouteAsyncTask(
                        realTimeApiKey, selectedRoute.getId(), new VehiclesPostExecuteListener());
                vehiclesAsyncTask.execute();
            }

        } else {
            errorManager.setNetworkError(true);
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
                    ArrayList<Prediction> predictions =
                            selectedRoute.getPredictions(selectedDirectionId);

                    recyclerViewAdapter.setPredictions(predictions);
                    swipeRefreshLayout.setRefreshing(false);
                    clearOnErrorView();

                    // Show no predictions text if there are no predictions
                    if (recyclerViewAdapter.getItemCount() == 0) {
                        enableNoPredictionsView(getResources().getString(R.string.no_departures));
                    }

                    if (returnToTop) {
                        recyclerView.scrollToPosition(0);
                    }

                } else {
                    // Show no stops text if there are no stops for the selected direction
                    enableNoPredictionsView(getResources().getString(R.string.no_stops));
                }
            }
        }
    }

    private void enableOnErrorView(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setNestedScrollingEnabled(false);
                    swipeRefreshLayout.setRefreshing(false);

                    noPredictionsView.setError(message);
                }
            });
        }
    }

    private void enableNoPredictionsView(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerViewAdapter.clear();
                    recyclerView.setNestedScrollingEnabled(false);
                    swipeRefreshLayout.setRefreshing(false);

                    if (loaded) {
                        noPredictionsView.setNoPredictions(message);
                    }
                }
            });
        }
    }

    private void clearOnErrorView() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    noPredictionsView.clearError();
                }
            });
        }
    }

    private void clearPredictions() {
        recyclerViewAdapter.clear();
        recyclerView.setNestedScrollingEnabled(false);
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
        getVehicles();

        populateDirectionSpinner(selectedRoute.getAllDirections());
    }

    @Override
    public void onDirectionSelected(Direction direction) {
        selectedDirectionId = direction.getId();

        clearPredictions();
        clearOnErrorView();

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
        clearOnErrorView();
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

        } else if (!selectedRoute.getId().equals(queryRoute.getId())) {
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

    private void cancelUpdate() {
        if (routesAsyncTask != null) {
            routesAsyncTask.cancel(true);
        }

        if (shapesAsyncTask != null) {
            shapesAsyncTask.cancel(true);
        }

        if (predictionsAsyncTask != null) {
            predictionsAsyncTask.cancel(true);
        }

        if (serviceAlertsAsyncTask != null) {
            serviceAlertsAsyncTask.cancel(true);
        }

        if (vehiclesAsyncTask != null) {
            vehiclesAsyncTask.cancel(true);
        }
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
            enableOnErrorView(getContext().getResources().getString(R.string.error_routes));
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
            enableOnErrorView(getContext().getResources().getString(R.string.error_stops));
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

    private class PredictionsPostExecuteListener implements PredictionsRouteSearchAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(List<Prediction> predictions) {
            dataRefreshing = false;
            refreshTime = new Date().getTime();
            loaded = true;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int today = calendar.get(Calendar.DAY_OF_MONTH);

            // Lock the views to prevent UI changes while loading new data to views
            viewsRefreshing = true;

            // Clear old predictions
            selectedRoute.clearPredictions(0);
            selectedRoute.clearPredictions(1);

            for (Prediction p : predictions) {
                Vehicle vt = vehicleTrips.get(p.getTripId());
                int pDay = -1;
                if (p.getPredictionTime() != null) {
                    calendar.setTime(p.getPredictionTime());
                    pDay = calendar.get(Calendar.DAY_OF_MONTH);
                }

                if (selectedRoute.getMode() != Route.BUS || vt == null || pDay != today ||
                        (p.getVehicle() != null &&
                                vt.getCurrentStopSequence() <= p.getStopSequence())) {
                    // Reduce 'time bounce' by replacing current prediction time with prior prediction
                    // time if one exists if they are within one minute
                    pastData.normalizePrediction(p);

                    // Set vehicle for predictions
                    Vehicle v = vehicles.get(p.getVehicleId());
                    if (v != null) {
                        p.setVehicle(v);
                    }

                    // Put this prediction into list of prior predictions
                    pastData.add(p);

                    // Add prediction to route
                    if (p.getCountdownTime() > -60000) {
                        selectedRoute.addPrediction(p);
                    }
                }
            }

            // Unlock views
            viewsRefreshing = false;

            refreshPredictions(false);
        }

        @Override
        public void onError() {
            dataRefreshing = false;
            refreshTime = new Date().getTime();
            enableOnErrorView(getContext().getResources().getString(R.string.error_upcoming_predictions));

            selectedRoute.clearPredictions(0);
            selectedRoute.clearPredictions(1);

            refreshPredictions(true);
        }
    }

    private class VehiclesPostExecuteListener implements VehiclesByRouteAsyncTask.OnPostExecuteListener {
        @Override
        public void onSuccess(Vehicle[] vs) {
            vehicles.clear();
            vehicleTrips.clear();

            for (Vehicle v : vs) {
                vehicles.put(v.getId(), v);
                vehicleTrips.put(v.getTripId(), v);
            }

            List<Prediction> predictions = selectedRoute.getPredictions(selectedDirectionId);
            if (predictions.size() > 0) {
                for (Prediction p : predictions) {
                    Vehicle v = vehicles.get(p.getVehicleId());
                    if (v != null) {
                        p.setVehicle(v);
                    }
                }

                refreshPredictions(false);
            }
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
