package jackwtat.simplembta.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jackwtat.simplembta.data.Route;
import jackwtat.simplembta.data.Trip;
import jackwtat.simplembta.R;

import static android.support.v4.content.ContextCompat.getColor;

/**
 * Created by jackw on 10/1/2017.
 */

public class PredictionsListAdapter extends ArrayAdapter<Trip[]> {
    public PredictionsListAdapter(Activity context, ArrayList<Trip[]> trips) {
        super(context, 0, trips);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.prediction_list_item, parent, false);
        }

        // Initialize list item elements
        TextView routeTextView = (TextView)
                listItemView.findViewById(R.id.route_text_view);
        TextView destinationTextView = (TextView)
                listItemView.findViewById(R.id.destination_text_view);
        TextView alertTextView = (TextView)
                listItemView.findViewById(R.id.alert_indicator_text_view);
        TextView stopTextView = (TextView)
                listItemView.findViewById(R.id.stop_text_view);
        TextView firstPredTextView = (TextView)
                listItemView.findViewById(R.id.first_time_text_view);
        TextView secondPredTextView = (TextView)
                listItemView.findViewById(R.id.second_time_text_view);

        Trip[] trips = getItem(position);

        if (trips.length > 0 && trips[0] != null) {
            // Display the route name
            routeTextView.setText(trips[0].getRouteName());

            // Set the background color of the route name
            routeTextView.setBackgroundColor(getBackgroundColorId(listItemView,
                    trips[0].getRouteId()));

            // Set the font color of the route name
            routeTextView.setTextColor(getRouteColor(listItemView, trips[0].getMode(),
                    trips[0].getRouteId()));

            // Display service alert indicator if there are alerts
            if (trips[0].hasAlerts()){
                alertTextView.setVisibility(View.VISIBLE);
            } else {
                alertTextView.setVisibility(View.INVISIBLE);
            }

            // Display the destination
            destinationTextView.setText(trips[0].getDestination());

            // Display the name of the stop
            stopTextView.setText(trips[0].getStopName());

            // Display the predicted arrival time of the first trip
            long firstPrediction = trips[0].getArrivalTime() / 60;
            firstPredTextView.setText(firstPrediction + " min");

            // Set the font and background color of the predicted arrival time
            // White font and red text if it's 5 minutes or less
            if (firstPrediction <= 5) {
                firstPredTextView.setBackgroundColor(ContextCompat.getColor(getContext(),
                        R.color.ApproachingAlert));
                firstPredTextView.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.HighlightedText));
            } else {
                firstPredTextView.setBackgroundColor(ContextCompat.getColor(getContext(),
                        R.color.Transparent));
                firstPredTextView.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.PrimaryText));
            }

            // If there is a second third trip, display their predicted arrival times also
            if (trips.length > 1 && trips[1] != null) {
                long secondPrediction = trips[1].getArrivalTime() / 60;

                if (trips.length > 2 && trips[2] != null) {
                    long thirdPrediction = trips[2].getArrivalTime() / 60;
                    secondPredTextView.setText(secondPrediction + ", " + thirdPrediction + " min");
                } else {
                    secondPredTextView.setText(secondPrediction + " min");
                }
            } else {
                secondPredTextView.setText("");
            }
        } else {
            // Empty trip object error
            // Should not normally happen
            routeTextView.setText("---");
            destinationTextView.setText("Empty Trip Error");
            stopTextView.setText("");
            firstPredTextView.setText("");
            secondPredTextView.setText("");
        }

        return listItemView;
    }

    // Returns the background color of the respective route
    // Should correspond
    private int getBackgroundColorId(View view, String routeId) {

        // Green Line
        if (routeId.length() >= 5 && routeId.substring(0, 5).equals("Green")) {
            return getColor(view.getContext(), R.color.GreenLine);

            // Red Line and Mattapan-Ashmont High Speed Line
        } else if (routeId.equals("Red") || routeId.equals("Mattapan")) {
            return getColor(view.getContext(), R.color.RedLine);

            // Blue Line
        } else if (routeId.equals("Blue")) {
            return getColor(view.getContext(), R.color.BlueLine);

            // Orange Line
        } else if (routeId.equals("Orange")) {
            return getColor(view.getContext(), R.color.OrangeLine);

            // Commuter Rail
        } else if (routeId.length() >= 2 && routeId.substring(0, 2).equals("CR")) {
            return getColor(view.getContext(), R.color.CommuterRail);

            // Silver Line
        } else if (routeId.equals("741") || routeId.equals("742") || routeId.equals("746") ||
                routeId.equals("749") || routeId.equals("751")) {
            return getColor(view.getContext(), R.color.SilverLine);

            // Boat/Ferry
        } else if (routeId.length() >= 4 && routeId.substring(0, 4).equals("Boat")) {
            return getColor(view.getContext(), R.color.Boat);

            // Bus and all others
        } else {
            return getColor(view.getContext(), R.color.Transparent);
        }
    }

    // Returns the text color of the route display
    private int getRouteColor(View view, int mode, String routeId) {
        if ((mode != Route.Mode.BUS && mode != Route.Mode.UNKNOWN) ||
                (routeId.equals("741") || routeId.equals("742") || routeId.equals("746") ||
                        routeId.equals("749") || routeId.equals("751"))) {
            return getColor(view.getContext(), R.color.HighlightedText);
        } else {
            return getColor(view.getContext(), R.color.PrimaryText);
        }
    }
}
