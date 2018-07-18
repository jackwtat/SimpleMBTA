package jackwtat.simplembta.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v7.app.AlertDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.activities.MbtaRouteWebPageActivity;
import jackwtat.simplembta.adapters.PredictionsListAdapter;
import jackwtat.simplembta.controllers.NearbyPredictionsController;
import jackwtat.simplembta.controllers.PredictionsController;
import jackwtat.simplembta.controllers.listeners.OnLocationErrorListener;
import jackwtat.simplembta.controllers.listeners.OnNetworkErrorListener;
import jackwtat.simplembta.controllers.listeners.OnPostExecuteListener;
import jackwtat.simplembta.controllers.listeners.OnProgressUpdateListener;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Stop;
import jackwtat.simplembta.views.AlertsListView;
import jackwtat.simplembta.views.RouteNameView;


/**
 * Created by jackw on 8/21/2017.
 */

public class NearbyFragment extends RefreshableFragment {
    private final static String LOG_TAG = "NearbyFragment";

    private final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private final long AUTO_REFRESH_RATE = 90000;

    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView predictionsListView;
    private TextView statusTimeTextView;
    private TextView errorTextView;
    private AlertDialog alertsDialog;

    private PredictionsController controller;
    private ArrayAdapter<ArrayList<Prediction>> predictionsListAdapter;
    private Timer autoRefreshTimer;

    private boolean resetUI = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        predictionsListAdapter = new PredictionsListAdapter(
                getActivity(), new ArrayList<ArrayList<Prediction>>());

        controller = new NearbyPredictionsController(getContext(),
                new OnPostExecuteListener() {
                    public void onPostExecute(List<Stop> stops) {
                        try {
                            publishPredictions(stops);
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, "Pushing get results to nonexistent view");
                        }
                    }
                },
                new OnProgressUpdateListener() {
                    public void onProgressUpdate(int progress) {
                        try {
                            setRefreshProgress(getResources().getString(R.string.getting_predictions));
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, "Pushing progress update to nonexistent view");
                        }
                    }
                },
                new OnNetworkErrorListener() {
                    public void onNetworkError() {
                        onRefreshError(getResources().getString(R.string.no_network_connectivity));
                    }
                },
                new OnLocationErrorListener() {
                    public void onLocationError() {
                        onRefreshError(getResources().getString(R.string.no_location));
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_predictions_list, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.predictions_swipe_refresh_layout);
        predictionsListView = rootView.findViewById(R.id.predictions_list_view);
        statusTimeTextView = rootView.findViewById(R.id.status_time_text_view);
        errorTextView = rootView.findViewById(R.id.error_message_text_view);

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

        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        controller.connect();
        forceRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Hide alert dialog if user has it open
        if (alertsDialog != null) {
            alertsDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        if (controller.isRunning()) {
            onRefreshCanceled();
        }

        autoRefreshTimer.cancel();
        controller.cancel();

        super.onStop();
    }

    public void refresh() {
        resetUI = false;
        controller.update();
    }

    // Call the controller to get the latest MBTA values
    public void forceRefresh() {
        resetUI = true;
        controller.forceUpdate();
    }

    // Updates the UI to show that values forceRefresh has been canceled
    private void onRefreshCanceled() {
        swipeRefreshLayout.setRefreshing(false);
    }

    // Updates the UI to display an error message if forceRefresh fails
    private void onRefreshError(String errorMessage) {
        setStatus(new Date(), errorMessage, false, true);
    }

    // Updates the UI to display values forceRefresh progress
    private void setRefreshProgress(String message) {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    // Update the UI to display a given timestamped status message
    private void setStatus(Date statusTime, String errorMessage, boolean showRefreshIcon,
                           boolean clearList) {
        DateFormat ft = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        String statusMessage = getResources().getString(R.string.updated) + " " + ft.format(statusTime);

        statusTimeTextView.setText(statusMessage);
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
        ArrayList<String> processedRDs = new ArrayList<>();

        // Initialize HashMap to track predictions for route-directions that have been processed
        HashMap<String, ArrayList<Prediction>> toDisplay = new HashMap<>();

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

                // If this route-direction has been processed with a prediction with null departure
                // and this prediction does not have a null departure, then we want to remove the
                // processed prediction and replace it with this one
                if (processedRDs.contains(rd) &&
                        toDisplay.get(rd).get(0).getDepartureTime() == null &&
                        p.getDepartureTime() != null) {
                    processedRDs.remove(rd);
                    toDisplay.remove(rd);
                }

                // If this route-direction has not processed or has been previously unprocessed,
                // then okay to process
                if (!processedRDs.contains(rd)) {
                    processedRDs.add(rd);

                    toDisplay.put(rd, new ArrayList<Prediction>());

                    toDisplay.get(rd).add(p);

                    // Add the next predictions, too, if same route-direction
                    for (int j = i + 1;
                         j < s.getPredictions().size() &&
                                 (s.getPredictions().get(j).getRoute().getId().equals(p.getRoute().getId()) &&
                                         s.getPredictions().get(j).getTrip().getDirection() == p.getTrip().getDirection());
                         j++) {
                        toDisplay.get(rd).add(s.getPredictions().get(j));
                        i = j;
                    }
                }
            }
        }

        // Add processed predictions to the prediction list adaptor
        for (String rd : processedRDs) {
            predictionsListAdapter.add(toDisplay.get(rd));
        }

        // Set the OnItemClickListener for displaying ServiceAlerts
        predictionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (predictionsListAdapter.getItem(position) != null &&
                        predictionsListAdapter.getItem(position).size() > 0) {
                    final Prediction p = predictionsListAdapter.getItem(position).get(0);
                    Collections.sort(p.getRoute().getServiceAlerts());

                    // Create alert dialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    RouteNameView routeNameView = new RouteNameView(getActivity(), p.getRoute(),
                            RouteNameView.LARGE_TEXT_SIZE, RouteNameView.SQUARE_BACKGROUND,
                            false, true);
                    routeNameView.setGravity(Gravity.CENTER);
                    builder.setCustomTitle(routeNameView);
                    builder.setView(new AlertsListView(getActivity(), p.getRoute().getServiceAlerts()));
                    builder.setPositiveButton(getResources().getString(R.string.dialog_close_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertsDialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.mbta_com), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(), MbtaRouteWebPageActivity.class);
                            intent.putExtra("routeId", p.getRoute().getId());
                            intent.putExtra("routeName", p.getRoute().getLongDisplayName(getContext()));
                            intent.putExtra("routeColor", p.getRoute().getPrimaryColor());
                            intent.putExtra("textColor", p.getRoute().getTextColor());
                            intent.putExtra("direction", p.getTrip().getDirection());
                            startActivity(intent);
                        }
                    });

                    alertsDialog = builder.create();
                    alertsDialog.show();
                }
            }
        });

        if (resetUI) {
            // Hide alerts dialog if user has it open
            if (alertsDialog != null) {
                alertsDialog.dismiss();
            }

            // Auto-scroll to the top
            predictionsListView.setSelection(0);
        }

        // Set statuses
        setStatus(new Date(), "", false, false);

        // If there are no values, show status to user
        if (predictionsListAdapter.getCount() < 1)

            setStatus(new Date(), getResources().

                    getString(R.string.no_predictions), false, false);
    }
}
