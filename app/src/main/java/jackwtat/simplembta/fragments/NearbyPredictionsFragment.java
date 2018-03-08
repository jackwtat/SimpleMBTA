package jackwtat.simplembta.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.support.v7.app.AlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.PredictionPairListAdapter;
import jackwtat.simplembta.controllers.NearbyPredictionsController;
import jackwtat.simplembta.controllers.NearbyPredictionsController.OnLocationErrorListener;
import jackwtat.simplembta.controllers.NearbyPredictionsController.OnNetworkErrorListener;
import jackwtat.simplembta.controllers.NearbyPredictionsController.OnPostExecuteListener;
import jackwtat.simplembta.controllers.NearbyPredictionsController.OnProgressUpdateListener;
import jackwtat.simplembta.mbta.structures.Mode;
import jackwtat.simplembta.mbta.structures.Prediction;
import jackwtat.simplembta.mbta.structures.ServiceAlert;
import jackwtat.simplembta.mbta.structures.Stop;


/**
 * Created by jackw on 8/21/2017.
 */

public class NearbyPredictionsFragment extends Fragment {
    private final static String LOG_TAG = "NearbyPredsFragment";

    private final int REQUEST_ACCESS_FINE_LOCATION = 1;

    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView predictionsListView;
    private TextView statusMsgTextView;
    private TextView statusTimeTextView;
    private TextView errorTextView;
    private ProgressBar progressBar;

    private NearbyPredictionsController controller;
    private ArrayAdapter<ArrayList<Prediction>> predictionsListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        predictionsListAdapter = new PredictionPairListAdapter(
                getActivity(), new ArrayList<ArrayList<Prediction>>());

        controller = new NearbyPredictionsController(getContext());

        controller.setNetworkErrorListener(new OnNetworkErrorListener() {
            @Override
            public void onNetworkError() {
                onRefreshError(getResources().getString(R.string.no_network_connectivity));
            }
        });

        controller.setOnLocationErrorListener(new OnLocationErrorListener() {
            @Override
            public void onLocationError() {
                onRefreshError(getResources().getString(R.string.no_location));
            }
        });

        controller.setOnProgressUpdateListener(new OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(int progress) {
                try {
                    setRefreshProgress(progress,
                            getResources().getString(R.string.getting_predictions));
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, "Pushing progress update to nonexistent view");
                }
            }
        });

        controller.setOnPostExecuteListener(new OnPostExecuteListener() {
            @Override
            public void onPostExecute(List<Stop> stops) {
                try {
                    publishPredictions(stops);
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, "Pushing get results to nonexistent view");
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_predictions_list, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.predictions_swipe_refresh_layout);
        predictionsListView = rootView.findViewById(R.id.predictions_list_view);
        statusMsgTextView = rootView.findViewById(R.id.status_message_text_view);
        statusTimeTextView = rootView.findViewById(R.id.status_time_text_view);
        errorTextView = rootView.findViewById(R.id.error_message_text_view);
        progressBar = rootView.findViewById(R.id.progressBar);

        predictionsListView.setAdapter(predictionsListAdapter);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        forceRefresh();
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
        controller.getPredictions(false);
    }

    @Override
    public void onStop() {
        if (controller.isRunning()) {
            onRefreshCanceled();
        }

        controller.cancel();

        super.onStop();
    }

    // Call the controller to get the latest MBTA values
    public void forceRefresh() {
        controller.getPredictions(true);
    }

    // Updates the UI to show that values refresh has been canceled
    private void onRefreshCanceled() {
        swipeRefreshLayout.setRefreshing(false);
        statusMsgTextView.setText(getResources().getString(R.string.refresh_canceled));
        progressBar.setProgress(0);
    }

    // Updates the UI to display an error message if refresh fails
    private void onRefreshError(String errorMessage) {
        setStatus(new Date(), errorMessage, false, true);
    }

    // Updates the UI to display values refresh progress
    private void setRefreshProgress(int percentage, String message) {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        if (!statusMsgTextView.getText().toString().equals(message)) {
            statusMsgTextView.setText(message);
        }
        progressBar.setProgress(percentage);
    }

    // Update the UI to display a given timestamped status message
    private void setStatus(Date statusTime, String errorMessage, boolean showRefreshIcon,
                           boolean clearList) {
        DateFormat ft = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        String statusMessage = getResources().getString(R.string.updated) + " " + ft.format(statusTime);

        statusTimeTextView.setText(statusMessage);
        statusMsgTextView.setText("");
        errorTextView.setText(errorMessage);
        swipeRefreshLayout.setRefreshing(showRefreshIcon);
        if (clearList) {
            predictionsListAdapter.clear();
        }
    }

    // Display the given list of values
    private void publishPredictions(List<Stop> stops) {
        // Clear all previous values from list
        predictionsListAdapter.clear();

        // Initialize the data structure that stores route-direction combos that have been processed
        ArrayList<String> displayed = new ArrayList<>();

        // Sort the stops
        Collections.sort(stops);

        // Loop through each stop
        for (Stop s : stops) {

            // Sort the predictions
            Collections.sort(s.getPredictions());

            // Loop through each prediction
            for (int i = 0; i < s.getPredictions().size(); i++) {
                Prediction p = s.getPredictions().get(i);

                // Create a unique identifier for the route-direction combo by concatenating
                // the direction ID and the route ID
                String rd = p.getTrip().getDirection() + ":" + p.getRoute().getId();

                // If we haven't already processed this route-direction combo,
                // then okay to display
                if (!displayed.contains(rd)) {
                    displayed.add(rd);

                    ArrayList<Prediction> predictions = new ArrayList<>();

                    predictions.add(p);

                    // Get the next prediction, too, if same route-direction
                    if (i + 1 < s.getPredictions().size()) {
                        Prediction p2 = s.getPredictions().get(i + 1);
                        if (p2.getRoute().getId().equals(p.getRoute().getId()) &&
                                p2.getTrip().getDirection() == p.getTrip().getDirection()) {
                            predictions.add(p2);
                        }
                    }

                    // Display in the list
                    predictionsListAdapter.add(predictions);
                }
            }
        }

        // Set the OnItemClickListener for displaying ServiceAlerts
        predictionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (predictionsListAdapter.getItem(position) != null &&
                        predictionsListAdapter.getItem(position).size() > 0) {
                    Prediction p = predictionsListAdapter.getItem(position).get(0);

                    if (p.getRoute().getServiceAlerts().size() > 0) {
                        ArrayList<ServiceAlert> alerts = p.getRoute().getServiceAlerts();
                        Collections.sort(alerts);

                        // Construct the alerts dialog message
                        StringBuilder alertMessage = new StringBuilder(alerts.get(0).getHeader());
                        for (int i = 1; i < alerts.size(); i++) {
                            alertMessage.append("\n\n").append(alerts.get(i).getHeader());
                        }

                        // Create alert dialog builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        if (p.getRoute().getMode() == Mode.BUS)
                            builder.setTitle(p.getRoute().getShortName());
                        else
                            builder.setTitle(p.getRoute().getLongName());

                        builder.setMessage(alertMessage.toString());

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });

        // Auto-scroll to the top
        predictionsListView.setSelection(0);

        // Reset progress bar
        progressBar.setProgress(0);

        // Set statuses
        setStatus(controller.getLastRefreshedDate(), "", false, false);

        // If there are no values, show status to user
        if (predictionsListAdapter.getCount() < 1)
            setStatus(new Date(), getResources().getString(R.string.no_predictions), false, false);
    }
}
