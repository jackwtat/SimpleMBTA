package jackwtat.simplembta.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

        // Prediction Layouts
        RelativeLayout leftSideLayout = listItemView.findViewById(R.id.prediction_left_side);
        LinearLayout rightSideLayout = listItemView.findViewById(R.id.prediction_right_side);
        RelativeLayout secondaryLayout = listItemView.findViewById(R.id.secondary_layout);
        RelativeLayout tertiaryLayout = listItemView.findViewById(R.id.tertiary_layout);

        // Route number/name TextView
        TextView routeTextView = listItemView.findViewById(R.id.route_text_view);

        // ServiceAlert indicator TextView
        TextView alertTextView = listItemView.findViewById(R.id.alert_indicator_text_view);

        // Stop name TextView and spacer
        TextView stopTextView = listItemView.findViewById(R.id.stop_text_view);

        // Prediction data
        Trip[] trips = getItem(position);

        // Display the prediction data
        try {
            leftSideLayout.setVisibility(View.VISIBLE);
            rightSideLayout.setVisibility(View.VISIBLE);

            // Display the route name
            routeTextView.setText(trips[0].getRouteName());

            // Set the background and font colors of the route name
            routeTextView.setBackgroundColor(getBackgroundColorId(listItemView,
                    trips[0].getRouteId()));
            routeTextView.setTextColor(getRouteColor(listItemView, trips[0].getMode(),
                    trips[0].getRouteId()));

            // Display service alert indicator if there are alerts
            if (trips[0].hasServiceAlert()) {
                alertTextView.setVisibility(View.VISIBLE);

                // Set the urgency
                if(trips[0].hasHighUrgencyServiceAlert()){
                    alertTextView.setText(getContext().getResources().getString(R.string.service_alert_urgent));
                } else {
                    alertTextView.setText(getContext().getResources().getString(R.string.service_alert_advisory));
                }

            } else {
                alertTextView.setVisibility(View.INVISIBLE);
            }

            // Display the name of the stop
            stopTextView.setText(trips[0].getStopName());

            // Display the first predicted trip
            populatePrimaryPrediction(listItemView, trips[0]);

            // Display second predicted trip if it exists
            if (trips.length > 1 && trips[1] != null) {

                // If the first and second trips have the same destination,
                // then display the second trip in the tertiary layout so its font is smaller
                if (trips[1].getDestination().equals(trips[0].getDestination())) {

                    // Call method to populate tertiary prediction
                    populateTertiaryPrediction(listItemView, trips[1]);

                    // Make the tertiary prediction layout visible
                    tertiaryLayout.setVisibility(View.VISIBLE);

                    // Hide the secondary prediction layout
                    secondaryLayout.setVisibility(View.GONE);


                } else {
                    // If the first and second trips have the same destination,
                    // then display the second trip in the secondary prediction layout
                    // so that its font is more prominent

                    // Make the secondary prediction layout visible
                    secondaryLayout.setVisibility(View.VISIBLE);

                    // Call method to populate the secondary prediction
                    populateSecondaryPrediction(listItemView, trips[1]);

                    // Hide the tertiary prediction layout
                    tertiaryLayout.setVisibility(View.GONE);
                }
            } else {
                // If there are no 2nd, 3rd, or 4th trips,
                // then hide the secondary and tertiary prediction layouts
                secondaryLayout.setVisibility(View.GONE);
                tertiaryLayout.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            leftSideLayout.setVisibility(View.GONE);
            rightSideLayout.setVisibility(View.GONE);
        }

        return listItemView;
    }

    private void populatePrimaryPrediction(View listItemView, Trip trip) {
        TextView primaryDestTextView =
                listItemView.findViewById(R.id.primary_destination_text_view);
        TextView primaryPredTextView =
                listItemView.findViewById(R.id.primary_time_text_view);

        primaryDestTextView.setText(trip.getDestination());

        String predictionText = (trip.getArrivalTime() / 60) + " min";
        primaryPredTextView.setText(predictionText);

        // Set the font and background color of the predicted arrival time
        // White font and red text if it's 5 minutes or less
        if (trip.getArrivalTime() / 60 <= 5) {
            primaryPredTextView.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.ApproachingAlert));
            primaryPredTextView.setTextColor(ContextCompat.getColor(getContext(),
                    R.color.HighlightedText));
        } else {
            primaryPredTextView.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.Transparent));
            primaryPredTextView.setTextColor(ContextCompat.getColor(getContext(),
                    R.color.PrimaryText));
        }

    }

    private void populateSecondaryPrediction(View listItemView, Trip trip) {
        TextView secondaryDestTextView =
                listItemView.findViewById(R.id.secondary_destination_text_view);
        TextView secondaryPredTextView =
                listItemView.findViewById(R.id.secondary_time_text_view);

        secondaryDestTextView.setText(trip.getDestination());

        String predictionText = (trip.getArrivalTime() / 60) + " min";
        secondaryPredTextView.setText(predictionText);

        // Set the font and background color of the predicted arrival time
        // White font and red text if it's 5 minutes or less
        if (trip.getArrivalTime() / 60 <= 5) {
            secondaryPredTextView.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.ApproachingAlert));
            secondaryPredTextView.setTextColor(ContextCompat.getColor(getContext(),
                    R.color.HighlightedText));
        } else {
            secondaryPredTextView.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.Transparent));
            secondaryPredTextView.setTextColor(ContextCompat.getColor(getContext(),
                    R.color.PrimaryText));
        }
    }

    private void populateTertiaryPrediction(View listItemView, Trip trip) {
        TextView tertiaryPredTextView =
                listItemView.findViewById(R.id.tertiary_time_text_view);

        String predictionText = Long.toString(trip.getArrivalTime() / 60);

        predictionText += " min";

        tertiaryPredTextView.setText(predictionText);
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
