package jackwtat.simplembta.views;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.PredictionsListAdapter;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Stop;

public class PredictionsListView extends RelativeLayout {
    private View rootView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView predictionsListView;
    private TextView statusTimeTextView;
    private TextView errorTextView;
    private AlertDialog alertsDialog;

    private ArrayAdapter<ArrayList<Prediction>> predictionsListAdapter;

    public PredictionsListView(Context context) {
        super(context);
        init(context);
    }

    public PredictionsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PredictionsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PredictionsListView(Context context, OnRefreshListener onSwipeRefreshListener) {
        super(context);
        swipeRefreshLayout.setOnRefreshListener(onSwipeRefreshListener);
    }


    // Sets the onSwipeRefresherListener
    public void setOnSwipeRefreshListener(OnRefreshListener onSwipeRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(onSwipeRefreshListener);
    }

    // Updates the UI to show that values forceRefresh has been canceled
    public void onRefreshCanceled() {
        swipeRefreshLayout.setRefreshing(false);
    }

    // Updates the UI to display an error message if forceRefresh fails
    public void onRefreshError(String errorMessage) {
        setStatus(new Date(), errorMessage, false, true);
    }

    // Updates the UI to display values forceRefresh progress
    public void setRefreshProgress() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    // Update the UI to display a given timestamped status message
    public void setStatus(Date statusTime, String errorMessage, boolean showRefreshIcon,
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

    public void hideAlertsDialog() {
        if (alertsDialog != null) {
            alertsDialog.dismiss();
        }
    }

    // Display the given list of values
    public void publishPredictions(final Context context, List<Stop> stops, boolean scrollToTop) {
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
                    RouteNameView routeNameView = new RouteNameView(context, p.getRoute(),
                            context.getResources().getDimension(R.dimen.large_route_name_text_size), RouteNameView.SQUARE_BACKGROUND,
                            false, true);
                    routeNameView.setGravity(Gravity.CENTER);
                    builder.setCustomTitle(routeNameView);
                    builder.setView(new AlertsListView(context, p.getRoute().getServiceAlerts()));
                    builder.setPositiveButton(getResources().getString(R.string.dialog_close_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertsDialog.dismiss();
                        }
                    });

                    /*
                    builder.setNegativeButton(getResources().getString(R.string.mbta_com), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(context, MbtaRouteWebPageActivity.class);
                            intent.putExtra("routeId", p.getRoute().getId());
                            intent.putExtra("routeName", p.getRoute().getLongDisplayName(getContext()));
                            intent.putExtra("routeColor", p.getRoute().getPrimaryColor());
                            intent.putExtra("textColor", p.getRoute().getTextColor());
                            intent.putExtra("direction", p.getTrip().getDirection());
                            context.startActivity(intent);
                        }
                    });
                    */

                    alertsDialog = builder.create();
                    alertsDialog.show();
                }
            }
        });

        if (scrollToTop) {
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

    private void init(Context context) {
        rootView = inflate(context, R.layout.predictions_list_view, this);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        predictionsListView = rootView.findViewById(R.id.list_view);
        statusTimeTextView = rootView.findViewById(R.id.status_time_text_view);
        errorTextView = rootView.findViewById(R.id.error_message_text_view);

        predictionsListAdapter = new PredictionsListAdapter(context,
                new ArrayList<ArrayList<Prediction>>());

        predictionsListView.setAdapter(predictionsListAdapter);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
    }
}
