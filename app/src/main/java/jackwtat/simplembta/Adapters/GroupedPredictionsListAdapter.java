package jackwtat.simplembta.Adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jackwtat.simplembta.MbtaData.Prediction;
import jackwtat.simplembta.MbtaData.Route;
import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.R;

/**
 * Created by jackw on 9/7/2017.
 */

public class GroupedPredictionsListAdapter extends ArrayAdapter<Stop> {
    private final static String LOG_TAG = "GroupedListAdapter";
    private final float SCALE = getContext().getResources().getDisplayMetrics().density;
    private final int PREDICTION_ROW_HEIGHT = (int) (SCALE * 30);
    private final int PREDICTION_LIMIT = 2;

    public GroupedPredictionsListAdapter(Activity context, ArrayList<Stop> stops) {
        super(context, 0, stops);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grouped_predictions_list_item, parent, false);
        }

        // Get the corresponding stop object
        Stop stop = getItem(position);

        // Get predictions
        Prediction nextPredictions[][][] = stop.getSortedPredictions(PREDICTION_LIMIT);

        // Initalize the TextView for the stop name and set value
        TextView stopNameTextView = (TextView) listItemView.findViewById(R.id.stop_name_text);
        stopNameTextView.setText(stop.getName());


        // Populate the listItemView with predictions
        listItemView = populateInnerPredictionsList(listItemView, nextPredictions);

        return listItemView;
    }

    private View populateInnerPredictionsList(View listItemView, Prediction[][][] nextPredictions) {
        // Initialize layouts
        LinearLayout routesLayout = (LinearLayout) listItemView.findViewById(R.id.route_names);
        LinearLayout destinationLayout = (LinearLayout) listItemView.findViewById(R.id.destinations);
        LinearLayout predictionsLayout = (LinearLayout) listItemView.findViewById(R.id.predictions);

        // Clear out any older predictions
        clearLayout(routesLayout);
        clearLayout(destinationLayout);
        clearLayout(predictionsLayout);

        // Create views for each route
        // Group by route
        // Inbound first
        // Outbound second
        for (int i = 0; i < nextPredictions.length; i++) {
            for (int j : Route.DIRECTIONS) {
                Prediction firstPrediction = nextPredictions[i][j][0];

                if (firstPrediction == null) {
                    // No predictions to display for this route
                    // Do nothing
                } else {
                    String routeName = firstPrediction.getRouteName();
                    String destination = firstPrediction.getDestination();
                    String predictedTimes = String.valueOf(firstPrediction.getArrivalTime() / 60);

                    for (int k = 1; k < nextPredictions[i][j].length; k++) {
                        if (nextPredictions[i][j][k] != null) {
                            predictedTimes += ", " + String.valueOf(nextPredictions[i][j][k].getArrivalTime() / 60);
                        }
                    }

                    predictedTimes += " min";

                    routesLayout.addView(newPredictionTextView(routeName, true));
                    destinationLayout.addView(newPredictionTextView(destination, false));
                    predictionsLayout.addView(newPredictionTextView(predictedTimes, true));

                }
            }
        }

        return listItemView;
    }

    // Clear the given LinearLayout of all child views
    private void clearLayout(LinearLayout linearLayout) {
        if (linearLayout.getChildCount() > 0)
            linearLayout.removeAllViews();
    }

    // Create and return a new prediction TextView with given text and styling
    private View newPredictionTextView(String text, boolean isBold) {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                PREDICTION_ROW_HEIGHT
        ));

        textView.setText(text);
        textView.setTextSize(20);
        textView.setSingleLine();
        if (isBold) {
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        return textView;
    }
}
