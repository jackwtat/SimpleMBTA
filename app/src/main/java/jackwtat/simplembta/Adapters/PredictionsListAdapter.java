package jackwtat.simplembta.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.text.Line;

import org.w3c.dom.Text;

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

    public PredictionsListAdapter(Activity context, ArrayList<Stop> stops) {
        super(context, 0, stops);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.predictions_list_item, parent, false);
        }

        //Initialize the root layout
        LinearLayout rootLayout = (LinearLayout) listItemView.findViewById(R.id.root_layout);

        // Initalize the TextView for the stop name
        TextView stopNameTextView = (TextView) listItemView.findViewById(R.id.stop_name_text);

        // Initialize TextViews for predictions
        //TextView routeNameTextView = (TextView) listItemView.findViewById(R.id.route_name_text);
        //TextView destinationTextView = (TextView) listItemView.findViewById(R.id.destination_text);
        //TextView predictionTimesTextView = (TextView) listItemView.findViewById(R.id.prediction_times_text);

        // Initialize Strings for storing TextView values prior to setting
        String routeName = "";
        String destination = "";
        String predictionTimes = "";

        // Get the stop
        Stop stop = getItem(position);

        // Get lists of routes and predictions
        List<Route> routes = stop.getRoutes();
        List<Prediction> predictions = stop.getPredictions();

        // Create an array for each route's next two predictions in each direction
        // nextPredictions[x][y][z]
        // x = route
        // y = direction, i.e. inbound/outbound
        // z = next 2 predictions
        Prediction nextPredictions[][][] = new Prediction[routes.size()][2][2];

        // Populate the array of next predictions
        for (int i = 0; i < predictions.size(); i++) {
            Prediction prediction = predictions.get(i);

            // Get direction of the prediction
            int dir = prediction.getDirection();

            // Find the corresponding position of route in list
            for (int j = 0; j < routes.size(); j++) {
                if (prediction.getRouteId() == routes.get(j).getId()) {
                    // Correct position of route & direction found
                    // Order of insertion of prediction:
                    //   1. If route does not already have a prediction
                    //          Insert new prediction into first slot
                    //   2. If route does not already have a second prediction
                    //          Insert new prediction into second slot
                    //   3. If current prediction is less than first prediction
                    //          Move first prediction to second slot
                    //          Insert new prediction into first slot
                    //   4. If current prediction is less than second prediction
                    //          Replace second prediction with new prediction
                    //   5. Else do not insert
                    if (nextPredictions[j][dir][0] == null) {
                        nextPredictions[j][dir][0] = prediction;
                    } else if (nextPredictions[j][1] == null) {
                        nextPredictions[j][dir][1] = prediction;
                    } else if (prediction.getPredictedArrivalTime() <
                            nextPredictions[j][dir][0].getPredictedArrivalTime()) {
                        nextPredictions[j][dir][1] = nextPredictions[j][dir][0];
                        nextPredictions[j][dir][0] = prediction;
                    } else if (prediction.getPredictedArrivalTime() <
                            nextPredictions[j][dir][1].getPredictedArrivalTime()) {
                        nextPredictions[j][dir][1] = prediction;
                    }

                    // Terminate j-for loop to move to next prediction
                    j = routes.size();
                }
            }
        }

        // Create views for each route
        // Group by route
        // Inbound first
        // Outbound second
        for (int i = 0; i < routes.size(); i++) {


            Prediction firstInbound = nextPredictions[i][Route.INBOUND][0];
            Prediction secondInbound = nextPredictions[i][Route.INBOUND][1];
            Prediction firstOutbound = nextPredictions[i][Route.OUTBOUND][0];
            Prediction secondOutbound = nextPredictions[i][Route.OUTBOUND][1];

            if (firstInbound == null && firstOutbound == null) {
                // Route Number TextView
                TextView routeNumberTextView = new TextView(getContext());
                routeNumberTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Destination TextView
                TextView destinationTextView = new TextView(getContext());
                destinationTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Prediction Times TextView
                TextView predictionTimesTextView = new TextView(getContext());
                predictionTimesTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));


                rootLayout.addView(new TextView(getContext()));

                routeName += "\n" + routes.get(i).getName();
                destination += "\n" + "No Predictions";
                predictionTimes += "\n";
            } else {
                if (firstInbound != null) {
                    routeName += "\n" + routes.get(i).getName();
                    destination += "\n" + firstInbound.getDestination();
                    predictionTimes += "\n" + firstInbound.getPredictedArrivalTime();

                    if (secondInbound != null) {
                        predictionTimes += ", " + secondInbound.getPredictedArrivalTime();
                    }

                    predictionTimes += " mins";
                }

                if (firstOutbound != null) {
                    routeName += "\n" + routes.get(i).getName();
                    destination += "\n" + firstOutbound.getDestination();
                    predictionTimes += "\n" + firstOutbound.getPredictedArrivalTime();

                    if (secondOutbound != null) {
                        predictionTimes += ", " + secondOutbound.getPredictedArrivalTime();
                    }

                    predictionTimes += " mins";
                }
            }
        }

        // Finally, set the values for all the TextViews
        //stopNameTextView.setText(stop.getName());
        //routeNameTextView.setText(routeName);
        //destinationTextView.setText(destination);
        //predictionTimesTextView.setText(predictionTimes);

        return listItemView;
    }
}
