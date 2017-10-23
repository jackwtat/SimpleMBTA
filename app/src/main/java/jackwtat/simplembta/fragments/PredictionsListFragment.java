package jackwtat.simplembta.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jackwtat.simplembta.adapters.PredictionsListAdapter;
import jackwtat.simplembta.data.Alert;
import jackwtat.simplembta.data.Route;
import jackwtat.simplembta.data.Trip;
import jackwtat.simplembta.R;
import jackwtat.simplembta.data.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final static String LOG_TAG = "PredListFragment";

    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView predictionsListView;
    private TextView updateStatusTextView;
    private TextView errorStatusTextView;
    private TextView debugTextView;

    protected Date lastUpdated;
    private ArrayAdapter<Trip[]> predictionsListAdapter;

    public abstract void refreshPredictions();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        predictionsListAdapter = new PredictionsListAdapter(getActivity(), new ArrayList<Trip[]>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_predictions_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.predictions_swipe_refresh_layout);
        predictionsListView = (ListView) rootView.findViewById(R.id.predictions_list_view);
        updateStatusTextView = (TextView) rootView.findViewById(R.id.update_status_text_view);
        errorStatusTextView = (TextView) rootView.findViewById(R.id.status_text_view);
        debugTextView = (TextView) rootView.findViewById(R.id.debug_text_view);

        predictionsListView.setAdapter(predictionsListAdapter);
        predictionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trip[] trips = (Trip[]) parent.getItemAtPosition(position);

                if (trips[0].hasAlerts()) {
                    ArrayList<Alert> alerts = trips[0].getAlerts();
                    String alertMessage = alerts.get(0).getText();

                    for(int i = 1; i < alerts.size(); i++){
                        alertMessage += "\n\n" + alerts.get(i).getText();
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
    public void onRefresh() {
        refreshPredictions();
    }

    public void displayUpdateTime(Date updatedTime, boolean refreshing) {
        SimpleDateFormat ft = new SimpleDateFormat("h:mm a");
        String message = "Updated " + ft.format(updatedTime);
        updateStatusTextView.setText(message);
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    public void displayUpdateStatus(String message, boolean refreshing) {
        updateStatusTextView.setText(message);
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    public void displayTimedErrorStatus(String message, boolean refreshing) {
        clearList(refreshing);
        errorStatusTextView.setText(message);
        displayUpdateTime(new Date(), refreshing);
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    public void displayUntimedErrorStatus(String message, boolean refreshing) {
        clearList(refreshing);
        errorStatusTextView.setText(message);
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    public void clearErrorStatus(boolean refreshing) {
        errorStatusTextView.setText("");
        swipeRefreshLayout.setRefreshing(refreshing);

    }

    public void clearList(boolean refreshing) {
        predictionsListAdapter.clear();
        errorStatusTextView.setText("");
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    public void displayDebugMessage(String message) {
        debugTextView.setText(message);
    }

    public void populateList(List<Stop> stops) {
        // Clear all previous predictions from list
        predictionsListAdapter.clear();

        // Sort the stops by distance
        Collections.sort(stops);

        // We will keep track of each route-direction pair we process and ignore duplicates
        ArrayList<String> rd = new ArrayList<>();

        // Loop through every stop
        for (int i = 0; i < stops.size(); i++) {

            // Get the next two trips for each direction for each route
            Trip[][][] predArray = stops.get(i).getSortedTripArray(2);

            // Loop through each route
            for (int route = 0; route < predArray.length; route++) {

                // Get array of directions in order we want displayed
                //  1. Inbound
                //  2. Outbound
                int[] directions = {Route.Direction.INBOUND, Route.Direction.OUTBOUND};

                //Loop through each direction
                for (int dir : directions) {

                    // Get the next trip for current going in current direction
                    Trip trip = predArray[route][dir][0];

                    // Check if there are trips for that route/direction
                    if (trip != null) {

                        // Check if we have already processed that route-direction
                        // If not, continue to display trips
                        if (!rd.contains(trip.getDirection() + "-" + trip.getRouteId())) {

                            // Add predictions to the list to display
                            predictionsListAdapter.add(predArray[route][dir]);

                            // Add route-direction pair so we know these trips are already
                            // displayed in the list
                            rd.add(trip.getDirection() + "-" + trip.getRouteId());
                        }
                    }
                }
            }
        }


        // Update the query time
        lastUpdated = new Date();
        displayUpdateTime(lastUpdated, false);

        // Hide refreshing icon
        swipeRefreshLayout.setRefreshing(false);

        // If there are no predictions, show status to user
        if (predictionsListAdapter.getCount() < 1) {
            displayTimedErrorStatus(getResources().getString(R.string.no_predictions), false);
        }
    }


}
