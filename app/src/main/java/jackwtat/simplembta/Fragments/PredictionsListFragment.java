package jackwtat.simplembta.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jackwtat.simplembta.Adapters.PredictionsListAdapter;
import jackwtat.simplembta.MbtaData.Route;
import jackwtat.simplembta.MbtaData.Trip;
import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final static String LOG_TAG = "PredListFragment";

    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView predictionsListView;
    private TextView updateTimeTextView;
    private TextView statusTextView;
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
        updateTimeTextView = (TextView) rootView.findViewById(R.id.updated_time_text_view);
        statusTextView = (TextView) rootView.findViewById(R.id.status_text_view);
        debugTextView = (TextView) rootView.findViewById(R.id.debug_text_view);

        predictionsListView.setAdapter(predictionsListAdapter);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
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

    public void updateTime(Date updatedTime) {
        TextView updatedTextView = (TextView) rootView.findViewById(R.id.updated_time_text_view);
        SimpleDateFormat ft = new SimpleDateFormat("h:mm a");
        String text = "Updated " + ft.format(updatedTime);
        updatedTextView.setText(text);
    }

    public void displayStatusMessage(String message) {
        statusTextView.setText(message);
        showStatusMessage(true);
        updateTime(new Date());
        swipeRefreshLayout.setRefreshing(false);
    }

    public void showStatusMessage(boolean show) {
        if (show) {
            statusTextView.setVisibility(View.VISIBLE);
        } else {
            statusTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void clearList(boolean isLoading) {
        predictionsListAdapter.clear();
        showStatusMessage(false);
        swipeRefreshLayout.setRefreshing(isLoading);
    }

    public void displayDebugMessage(String message){
        debugTextView.setText(message);
    }

    public void populateList(List<Stop> stops) {
        // Clear all previous predictions from list
        predictionsListAdapter.clear();

        // Sort the stops by distance
        Collections.sort(stops);

        // We will keep track of each route-direction pair we process
        // and ignore duplicates
        ArrayList<String> rd = new ArrayList<>();

        // Loop through every stop
        for (int i = 0; i < stops.size(); i++) {

            // Get the next two trips for each direction for each route
            Trip[][][] predArray = stops.get(i).getSortedTrips(2);

            // Loop through each route
            for (int route = 0; route < predArray.length; route++) {
                int[] directions = {Route.Direction.INBOUND, Route.Direction.OUTBOUND};
                //Loop through each direction
                for (int dir : directions) {
                    Trip trip = predArray[route][dir][0];
                    // Check if there are trips for that route/direction
                    if (trip != null) {

                        // Check if we have already processed that route-direction
                        if (!rd.contains(trip.getDirection() + "-" + trip.getRouteId())) {

                            // Add predictions
                            predictionsListAdapter.add(predArray[route][dir]);

                            // Add route-direction pair
                            rd.add(trip.getDirection() + "-" + trip.getRouteId());
                        }
                    }
                }
            }
        }

        lastUpdated = new Date();
        updateTime(lastUpdated);
        swipeRefreshLayout.setRefreshing(false);

        if (predictionsListAdapter.getCount() < 1) {
            displayStatusMessage(getResources().getString(R.string.no_predictions));
        }
    }


}
