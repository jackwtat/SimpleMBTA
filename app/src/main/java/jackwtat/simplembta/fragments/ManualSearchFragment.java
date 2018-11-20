package jackwtat.simplembta.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.Arrays;
import java.util.Timer;

import jackwtat.simplembta.R;
import jackwtat.simplembta.asyncTasks.RoutesAsyncTask;
import jackwtat.simplembta.asyncTasks.ServiceAlertsAsyncTask;
import jackwtat.simplembta.asyncTasks.ShapesAsyncTask;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;
import jackwtat.simplembta.model.Direction;
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
        ManualSearchSpinners.OnRouteSelectedListener,
        ManualSearchSpinners.OnDirectionSelectedListener,
        ManualSearchSpinners.OnStopSelectedListener {
    public static final String LOG_TAG = "ManualSearchFragment";

    // Maximum age of prediction
    public static final long MAX_PREDICTION_AGE = 90000;

    // Predictions auto update rate
    public static final long PREDICTIONS_UPDATE_RATE = 15000;

    private View rootView;
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
    private ErrorManager errorManager;
    private Timer timer;

    private boolean refreshing = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;
    private long onPauseTime = 0;

    private Route[] routes;
    private Route selectedRoute;
    private int selectedDirectionId = Direction.INBOUND;
    private Stop selectedStop;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get MBTA realTime API key
        realTimeApiKey = getContext().getString(R.string.v3_mbta_realtime_api_key);

        // Initialize network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manual_search, container, false);

        // Get the spinner
        searchSpinners = rootView.findViewById(R.id.manual_search_spinners);
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
        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);

        // Get recycler view
        recyclerView = rootView.findViewById(R.id.predictions_recycler_view);

        // Set recycler view layout
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Add onScrollListener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    refreshPredictions();
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onErrorChanged() {

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

    private void getPredictions(){
        // TODO: getPredictions
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

    private void refreshRoutes() {
        populateRouteSpinner(routes);
    }

    private void refreshShapes() {
        populateStopSpinner(selectedRoute.getStops(selectedDirectionId));
    }

    private void refreshPredictions() {
        // TODO: refreshPredictions
    }

    private void clearPredictions(){
        // TODO: clearPredictions
    }

    private Shape[] getShapesFromJson(int jsonFile) {
        return ShapesJsonParser.parse(RawResourceReader.toString(getResources().openRawResource(jsonFile)));
    }

    private void populateRouteSpinner(Route[] routes) {
        searchSpinners.populateRouteSpinner(routes);
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
    }

    @Override
    public void onRouteSelected(Route route) {
        selectedRoute = route;

        populateDirectionSpinner(selectedRoute.getAllDirections());
    }

    @Override
    public void onDirectionSelected(Direction direction) {
        selectedDirectionId = direction.getId();

        clearPredictions();

        if (selectedRoute.getShapes(selectedDirectionId).length == 0) {
            populateStopSpinner(new Stop[0]);
            getShapes();
        } else {
            populateStopSpinner(selectedRoute.getStops(selectedDirectionId));
        }
    }

    @Override
    public void onStopSelected(Stop stop) {
        selectedStop = stop;
    }
}
