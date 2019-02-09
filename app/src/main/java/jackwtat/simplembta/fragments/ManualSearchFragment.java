package jackwtat.simplembta.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.activities.RouteDetailActivity;
import jackwtat.simplembta.adapters.RouteDetailRecyclerViewAdapter;
import jackwtat.simplembta.asyncTasks.RouteDetailPredictionsAsyncTask;
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
import jackwtat.simplembta.views.ManualSearchSpinners;
import jackwtat.simplembta.views.ServiceAlertsIndicatorView;

public class ManualSearchFragment extends Fragment implements
        ErrorManager.OnErrorChangedListener,
        RoutesAsyncTask.OnPostExecuteListener,
        ShapesAsyncTask.OnPostExecuteListener,
        ServiceAlertsAsyncTask.OnPostExecuteListener,
        RouteDetailPredictionsAsyncTask.OnPostExecuteListener,
        ManualSearchSpinners.OnRouteSelectedListener,
        ManualSearchSpinners.OnDirectionSelectedListener,
        ManualSearchSpinners.OnStopSelectedListener {
    public static final String LOG_TAG = "ManualSearchFragment";

    // Maximum age of prediction
    public static final long MAX_PREDICTION_AGE = 90000;

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    // Service alerts auto update rate
    public static final long SERVICE_ALERTS_UPDATE_RATE = 60000;

    private AppBarLayout appBarLayout;
    private ManualSearchSpinners searchSpinners;
    private ServiceAlertsIndicatorView serviceAlertsIndicatorView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView noPredictionsTextView;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private RoutesAsyncTask routesAsyncTask;
    private ShapesAsyncTask shapesAsyncTask;
    private ServiceAlertsAsyncTask serviceAlertsAsyncTask;
    private RouteDetailPredictionsAsyncTask predictionsAsyncTask;
    private ErrorManager errorManager;
    private RouteDetailRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    private boolean refreshing = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private Route[] routes;
    private Route selectedRoute;
    private int selectedDirectionId;

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
                getResources().getString(R.string.saved_manual_search_route), Context.MODE_PRIVATE);
        savedRouteId = sharedPreferences.getString("routeId", null);
        savedStopIds[0] = sharedPreferences.getString("stopId_0", null);
        savedStopIds[1] = sharedPreferences.getString("stopId_1", null);
        selectedDirectionId = sharedPreferences.getInt("directionId", Direction.INBOUND);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manual_search, container, false);

        // Get the spinner
        searchSpinners = rootView.findViewById(R.id.manual_search_spinners);
        searchSpinners.setOnRouteSelectedListener(this);
        searchSpinners.setOnDirectionSelectedListener(this);
        searchSpinners.setOnStopSelectedListener(this);
        searchSpinners.setOnMapIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RouteDetailActivity.class);
                intent.putExtra("route", selectedRoute);
                intent.putExtra("direction", selectedDirectionId);
                intent.putExtra("refreshTime", refreshTime);
                startActivity(intent);
            }
        });

        // Get app bar
        appBarLayout = rootView.findViewById(R.id.app_bar_layout);

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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    refreshPredictions(false);
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new RouteDetailRecyclerViewAdapter();
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

        if (routes == null || routes.length == 0) {
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
    }

    @Override
    public void onPause() {
        super.onPause();

        refreshing = false;

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
                    getResources().getString(R.string.saved_manual_search_route), Context.MODE_PRIVATE);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorManager.hasNetworkError()) {
                    if (selectedRoute != null) {
                        selectedRoute.clearPredictions(Direction.INBOUND);
                        selectedRoute.clearPredictions(Direction.OUTBOUND);
                        selectedRoute.clearServiceAlerts();

                        refreshPredictions(true);
                        refreshServiceAlerts();
                    }
                }
            }
        });
    }

    private void getRoutes() {
        if (networkConnectivityClient.isConnected()) {
            errorManager.setNetworkError(false);

            if (routesAsyncTask != null) {
                routesAsyncTask.cancel(true);
            }

            routesAsyncTask = new RoutesAsyncTask(realTimeApiKey, this);
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
                    selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_blue));

                } else if (OrangeLine.isOrangeLine(selectedRoute.getId())) {
                    selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_orange));

                } else if (RedLine.isRedLine(selectedRoute.getId()) && !RedLine.isMattapanLine(selectedRoute.getId())) {
                    selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_red));

                } else if (RedLine.isRedLine(selectedRoute.getId()) && RedLine.isMattapanLine(selectedRoute.getId())) {
                    selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_mattapan));

                } else if (GreenLine.isGreenLine(selectedRoute.getId())) {
                    if (GreenLineCombined.isGreenLineCombined(selectedRoute.getId())) {
                        selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_green_combined));
                    } else if (GreenLine.isGreenLineB(selectedRoute.getId())) {
                        selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_green_b));

                    } else if (GreenLine.isGreenLineC(selectedRoute.getId())) {
                        selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_green_c));

                    } else if (GreenLine.isGreenLineD(selectedRoute.getId())) {
                        selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_green_d));

                    } else if (GreenLine.isGreenLineE(selectedRoute.getId())) {
                        selectedRoute.setShapes(getShapesFromJson(R.raw.shapes_green_e));

                    }
                }

                refreshShapes();

            } else if (networkConnectivityClient.isConnected()) {
                errorManager.setNetworkError(false);

                if (shapesAsyncTask != null) {
                    shapesAsyncTask.cancel(true);
                }

                shapesAsyncTask = new ShapesAsyncTask(
                        realTimeApiKey,
                        selectedRoute.getId(),
                        this);
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
                        realTimeApiKey,
                        routeId,
                        this);
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

                    refreshing = true;

                    if (predictionsAsyncTask != null) {
                        predictionsAsyncTask.cancel(true);
                    }

                    predictionsAsyncTask = new RouteDetailPredictionsAsyncTask(realTimeApiKey,
                            selectedRoute, selectedDirectionId, this);
                    predictionsAsyncTask.execute();

                } else {
                    errorManager.setNetworkError(true);
                    refreshing = false;
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshPredictions(true);
                    }
                });
            }
        }
    }

    @Override
    public void onPostExecute(Route[] routes) {
        this.routes = routes;
        Arrays.sort(this.routes);

        refreshRoutes();
    }

    @Override
    public void onPostExecute(Shape[] shapes) {
        selectedRoute.setShapes(shapes);

        refreshShapes();
    }

    @Override
    public void onPostExecute(ServiceAlert[] serviceAlerts) {
        selectedRoute.clearServiceAlerts();
        selectedRoute.addAllServiceAlerts(serviceAlerts);

        refreshServiceAlerts();
    }

    @Override
    public void onPostExecute(List<Prediction> predictions) {
        refreshing = false;
        refreshTime = new Date().getTime();

        selectedRoute.clearPredictions(0);
        selectedRoute.clearPredictions(1);
        selectedRoute.addAllPredictions(predictions);

        refreshPredictions(false);
    }

    private void refreshRoutes() {
        populateRouteSpinner(routes);
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
        appBarLayout.setExpanded(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private Shape[] getShapesFromJson(int jsonFile) {
        return ShapesJsonParser.parse(RawResourceReader.toString(getResources().openRawResource(jsonFile)));
    }

    private void populateRouteSpinner(Route[] routes) {
        searchSpinners.populateRouteSpinner(routes);

        if (selectedRoute != null) {
            searchSpinners.selectedRoute(selectedRoute.getId());

        } else if (savedRouteId != null) {
            searchSpinners.selectedRoute(savedRouteId);
        }
    }

    private void populateDirectionSpinner(Direction[] directions) {
        if (directions[0].getId() == Direction.SOUTHBOUND) {
            Direction d = directions[0];
            directions[0] = directions[1];
            directions[1] = d;
        }

        searchSpinners.populateDirectionSpinner(directions);
        searchSpinners.selectDirection(selectedDirectionId);
    }

    private void populateStopSpinner(Stop[] stops) {
        searchSpinners.populateStopSpinner(stops);

        if (selectedRoute.getNearestStop(selectedDirectionId) != null) {
            searchSpinners.selectStop(selectedRoute.getNearestStop(selectedDirectionId).getId());
        } else if (savedStopIds != null) {
            searchSpinners.selectStop(savedStopIds[selectedDirectionId]);
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
        selectedRoute.setNearestStop(selectedDirectionId, stop);

        // Clear the current predictions and get the predictions for the selected stop
        clearPredictions();
        swipeRefreshLayout.setRefreshing(true);
        forceUpdate();
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
}
