package jackwtat.simplembta.fragments;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.QueryUtil;
import jackwtat.simplembta.ServicesDbHelper;
import jackwtat.simplembta.adapters.PredictionsListAdapter;
import jackwtat.simplembta.data.ServiceAlert;
import jackwtat.simplembta.data.Route;
import jackwtat.simplembta.data.Trip;
import jackwtat.simplembta.R;
import jackwtat.simplembta.data.Stop;
import jackwtat.simplembta.listeners.OnLocationUpdateFailedListener;
import jackwtat.simplembta.listeners.LocationUpdateListener;
import jackwtat.simplembta.services.LocationService;
import jackwtat.simplembta.services.NetworkConnectivityService;

/**
 * Created by jackw on 8/21/2017.
 */

public class NearbyPredictionsFragment extends Fragment implements OnRefreshListener,
        LocationUpdateListener, OnLocationUpdateFailedListener {
    private final static String LOG_TAG = "NearbyPredsFragment";

    // Fine Location Permission
    private final int REQUEST_ACCESS_FINE_LOCATION = 1;

    // Time between location updates, in seconds
    private final long LOCATION_UPDATE_INTERVAL = 15;

    // Time since last refresh before predictions can automatically refresh onResume, in seconds
    private final long ON_RESUME_REFRESH_INTERVAL = 120;

    // Maximum distance to stop in miles
    private final double MAX_DISTANCE = .5;

    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView predictionsListView;
    private TextView statusTextView;
    private TextView errorTextView;
    private ProgressBar progressBar;

    private LocationService locationService;
    private NetworkConnectivityService networkConnectivityService;
    private PredictionAsyncTask predictionAsyncTask;
    private ServicesDbHelper servicesDbHelper;

    private boolean refreshing;
    protected Date lastRefreshed;
    private ArrayAdapter<Trip[]> predictionsListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        predictionsListAdapter = new PredictionsListAdapter(getActivity(), new ArrayList<Trip[]>());

        locationService = new LocationService(getContext(), LOCATION_UPDATE_INTERVAL);

        networkConnectivityService = new NetworkConnectivityService(getContext());

        servicesDbHelper = new ServicesDbHelper(getContext());

        locationService.addUpdateListener(this);
        locationService.addUpdateFailedListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_predictions_list, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.predictions_swipe_refresh_layout);
        predictionsListView = rootView.findViewById(R.id.predictions_list_view);
        statusTextView = rootView.findViewById(R.id.status_message_text_view);
        errorTextView = rootView.findViewById(R.id.error_message_text_view);
        progressBar = rootView.findViewById(R.id.progressBar);

        predictionsListView.setAdapter(predictionsListAdapter);
        predictionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trip[] trips = (Trip[]) parent.getItemAtPosition(position);

                if (trips[0].hasServiceAlert()) {
                    // Get and sort service alerts for this trip's route
                    ArrayList<ServiceAlert> serviceAlerts = trips[0].getAlerts();
                    Collections.sort(serviceAlerts);

                    // Construct the alerts dialog message
                    String alertMessage = serviceAlerts.get(0).getText();
                    for (int i = 1; i < serviceAlerts.size(); i++) {
                        alertMessage+= "\n\n" + serviceAlerts.get(i).getText();
                    }

                    // Create alert dialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(trips[0].getRouteLongName());
                    builder.setMessage(alertMessage);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshPredictions();
                    }
                }
        );

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get location access permission from user
        if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_FINE_LOCATION);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // If sufficient time has lapsed since last refresh, then automatically refreshed predictions
        Date currentTime = new Date();
        if (lastRefreshed == null ||
                ((currentTime.getTime() - lastRefreshed.getTime()) > 1000 * ON_RESUME_REFRESH_INTERVAL)) {
            refreshPredictions();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Disconnect LocationServices
        locationService.disconnectClient();

        // Cancel the AsyncTask if it is running
        if (predictionAsyncTask != null) {
            predictionAsyncTask.cancel(true);
        }

        // Update the UI to indicate that predictions refreshing is canceled
        if (refreshing) {
            onRefreshCanceled();
            refreshing = false;
        }
    }

    @Override
    public void onRefresh() {
        refreshPredictions();
    }

    @Override
    public void onLocationUpdate(Location location) {
        if(refreshing) {
            predictionAsyncTask = new PredictionAsyncTask();
            predictionAsyncTask.execute(location);
        }
    }

    @Override
    public void onLocationUpdateFailed(){
        onRefreshError(getResources().getString(R.string.no_location));
    }

    //
    public void refreshPredictions() {
        if (!refreshing) {
            refreshing = true;

            if(networkConnectivityService.isConnected()){
                locationService.updateLocation();
            } else {
                onRefreshError(getResources().getString(R.string.no_network_connectivity));
            }
        }
    }

    // Updates the UI to show that predictions refresh has been canceled
    public void onRefreshCanceled() {
        swipeRefreshLayout.setRefreshing(false);
        statusTextView.setText(getResources().getString(R.string.refresh_canceled));
        progressBar.setProgress(0);
    }

    // Updates the UI to display an error message if refresh fails
    private void onRefreshError(String errorMessage) {
        refreshing = false;
        setStatus(new Date(), errorMessage, false, true);
    }

    // Updates the UI to display predictions refresh progress
    public void setRefreshProgress(int percentage, String message) {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        if (!statusTextView.getText().toString().equals(message)) {
            statusTextView.setText(message);
        }
        progressBar.setProgress(percentage);
    }

    // Update the UI to display a given status message
    public void setStatus(String statusMessage, String errorMessage, boolean showRefreshIcon,
                          boolean clearList) {
        statusTextView.setText(statusMessage);
        errorTextView.setText(errorMessage);
        swipeRefreshLayout.setRefreshing(showRefreshIcon);
        if (clearList) {
            predictionsListAdapter.clear();
        }
    }

    // Update the UI to display a given timestamped status message
    public void setStatus(Date statusTime, String errorMessage, boolean showRefreshIcon,
                          boolean clearList) {
        DateFormat ft = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        String statusMessage = "Updated " + ft.format(statusTime);

        statusTextView.setText(statusMessage);
        errorTextView.setText(errorMessage);
        swipeRefreshLayout.setRefreshing(showRefreshIcon);
        if (clearList) {
            predictionsListAdapter.clear();
        }
    }


    // Display the given list of predictions
    public void publishPredictions(List<Stop> stops) {
        // Clear all previous predictions from list
        predictionsListAdapter.clear();

        // Sort the stops by distance
        Collections.sort(stops);

        // We will keep track of each route-direction pair we process and ignore duplicates
        ArrayList<String> rd = new ArrayList<>();

        // Loop through every stop
        for (int i = 0; i < stops.size(); i++) {

            // Get the next two trips for each direction for each route
            Trip[][][] allPredictions = stops.get(i).getSortedTripArray(2);

            // Loop through each route
            for (Trip[][] route : allPredictions) {

                // Get array of directions in order we want displayed
                //  1. Inbound
                //  2. Outbound
                int[] directions = {Route.Direction.INBOUND, Route.Direction.OUTBOUND};

                //Loop through each direction
                for (int direction : directions) {

                    // Get the next trip for current route going in current direction
                    Trip trip = route[direction][0];

                    // Check if there are trips for that route/direction
                    if (trip != null) {

                        // Check if we have already processed that route-direction
                        // If not, continue to display trips
                        if (!rd.contains(trip.getDirection() + "-" + trip.getRouteId())) {

                            // Add predictions to the list to display
                            predictionsListAdapter.add(route[direction]);

                            // Add route-direction pair so we know these trips are already
                            // displayed in the list
                            rd.add(trip.getDirection() + "-" + trip.getRouteId());
                        }
                    }
                }
            }
        }

        // Update refresh time
        lastRefreshed = new Date();

        // Auto-scroll to the top
        predictionsListView.setSelection(0);

        // Reset progress bar
        progressBar.setProgress(0);

        // Set statuses
        setStatus(lastRefreshed, "", false, false);

        // If there are no predictions, show status to user
        if (predictionsListAdapter.getCount() < 1) {
            setStatus(new Date(), getResources().getString(R.string.no_predictions), false, false);
        }
    }

    // AsyncTask that asynchronously queries the MBTA API and displays the results upon success
    private class PredictionAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
        private final int LOADING_DATABASE = -1;
        private final int GETTING_NEARBY_STOPS = -2;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Stop> doInBackground(Location... locations) {

            // Load the stops database
            publishProgress(LOADING_DATABASE);
            servicesDbHelper.loadDatabase(getContext());

            // Get all stops within the specified maximum distance from user's location
            publishProgress(GETTING_NEARBY_STOPS);
            List<Stop> stops = servicesDbHelper.getStopsByLocation(locations[0], MAX_DISTANCE);

            // Let user know we're not getting predictions
            publishProgress(0);

            // Get all service alerts
            HashMap<String, ArrayList<ServiceAlert>> alerts = QueryUtil.fetchAlerts(getString(R.string.mbta_realtime_api_key));

            // Get predicted trips for each stop
            for (int i = 0; i < stops.size(); i++) {
                Stop stop = stops.get(i);

                stop.addTrips(QueryUtil.fetchPredictionsByStop(getString(R.string.mbta_realtime_api_key), stop.getId()));

                // Add alerts to trips whose route has alerts
                for (Trip trip : stop.getTrips()) {
                    if (alerts.containsKey(trip.getRouteId())) {
                        trip.setAlerts(alerts.get(trip.getRouteId()));
                    }
                }

                publishProgress((int) (100 * (i + 1) / stops.size()));
            }

            return stops;
        }

        protected void onProgressUpdate(Integer... progress) {
            try {
                if (progress[0] == LOADING_DATABASE) {
                    setRefreshProgress(0, getResources().getString(R.string.loading_database));
                } else if (progress[0] == GETTING_NEARBY_STOPS) {
                    setRefreshProgress(0, getResources().getString(R.string.getting_nearby_stops));
                } else {
                    setRefreshProgress(progress[0], getResources().getString(R.string.getting_predictions));
                }
            } catch (IllegalStateException e) {
                Log.e(LOG_TAG, "Pushing progress update to nonexistent view");
            }
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            try {
                refreshing = false;
                publishPredictions(stops);
            } catch (IllegalStateException e) {
                Log.e(LOG_TAG, "Pushing query results to nonexistent view");
            }
        }
    }
}
