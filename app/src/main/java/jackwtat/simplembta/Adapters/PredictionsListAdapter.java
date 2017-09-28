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
import java.util.List;

import jackwtat.simplembta.MbtaData.Prediction;
import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Route;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 9/7/2017.
 */

public class PredictionsListAdapter extends ArrayAdapter<Stop> {
    private final static String LOG_TAG = "PredListAdapter";
    private final float SCALE = getContext().getResources().getDisplayMetrics().density;
    private final int PREDICTION_ROW_HEIGHT = (int) (SCALE * 24);

    private List<Route> routes;
    private List<Prediction> predictions;

    public PredictionsListAdapter(Activity context, ArrayList<Stop> stops) {
        super(context, 0, stops);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.predictions_list_item, parent, false);
        }

        // Get the corresponding stop object
        Stop stop = getItem(position);

        // Initalize the TextView for the stop name and set value
        TextView stopNameTextView = (TextView) listItemView.findViewById(R.id.stop_name_text);
        stopNameTextView.setText(stop.getName());

        // Get lists of routes and predictions
        routes = stop.getRoutes();
        predictions = stop.getPredictions();

        /*
            Create an array for each route's next two predictions in each direction
            nextPredictions[x][y][z]
            x = route
            y = direction, i.e. inbound/outbound
            z = next 2 predictions
        */
        Prediction nextPredictions[][][] = new Prediction[routes.size()][2][2];

        // Populate the array of next predictions
        for (int i = 0; i < predictions.size(); i++) {
            Prediction prediction = predictions.get(i);

            // Get direction of the prediction
            int dir = prediction.getDirection();

            // Find the corresponding position of route in list
            // and populate into next predictions array
            for (int j = 0; j < routes.size(); j++) {
                if (prediction.getRouteId().equals(routes.get(j).getId())) {
                    /*
                        Correct position of route & direction found
                        Order of insertion of prediction:
                          1. If route does not already have a prediction
                                 Insert new prediction into first slot
                          2. If route does not already have a second prediction
                                 Insert new prediction into second slot
                          3. If current prediction is less than first prediction
                                 Move first prediction to second slot
                                 Insert new prediction into first slot
                          4. If current prediction is less than second prediction
                                 Replace second prediction with new prediction
                          5. Else do not insert
                    */
                    if (nextPredictions[j][dir][0] == null) {
                        nextPredictions[j][dir][0] = prediction;
                    } else if (nextPredictions[j][dir][1] == null) {
                        nextPredictions[j][dir][1] = prediction;
                    } else if (prediction.getPredictedArrivalTime() <
                            nextPredictions[j][dir][0].getPredictedArrivalTime()) {
                        nextPredictions[j][dir][1] = nextPredictions[j][dir][0];
                        nextPredictions[j][dir][0] = prediction;
                    } else if (prediction.getPredictedArrivalTime() <
                            nextPredictions[j][dir][1].getPredictedArrivalTime()) {
                        nextPredictions[j][dir][1] = prediction;
                    }

                    // Terminate j-loop to move to next prediction
                    j = routes.size();
                }
            }
        }

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
            for (int j = 1; j >= 0; j--) {
                Prediction firstPrediction = nextPredictions[i][j][0];
                Prediction secondPrediction = nextPredictions[i][j][1];

                if (firstPrediction == null) {
                    // No predictions to display for this route
                    // Do nothing
                } else {
                    String routeName;
                    String destination;
                    String predictedTimes;
                    if (firstPrediction != null) {
                        routeName = firstPrediction.getRouteName();
                        destination = firstPrediction.getDestination();
                        predictedTimes = String.valueOf(firstPrediction.getPredictedArrivalTime() / 60);

                        if (secondPrediction != null) {
                            predictedTimes += ", " + String.valueOf(secondPrediction.getPredictedArrivalTime() / 60);
                        }

                        predictedTimes += " mins";

                        routesLayout.addView(newPredictionTextView(routeName, true));
                        destinationLayout.addView(newPredictionTextView(destination, false));
                        predictionsLayout.addView(newPredictionTextView(predictedTimes, true));
                    }
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
        textView.setTextSize(16);
        if (isBold) {
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }

        return textView;
    }
}
