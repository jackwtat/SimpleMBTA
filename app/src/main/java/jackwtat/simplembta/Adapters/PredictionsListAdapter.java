package jackwtat.simplembta.Adapters;

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

import jackwtat.simplembta.MbtaData.Route;
import jackwtat.simplembta.MbtaData.Trip;
import jackwtat.simplembta.R;

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

        // Initialize list item view elements
        TextView routeTextView = (TextView)
                listItemView.findViewById(R.id.route_text_view);
        TextView destinationTextView = (TextView)
                listItemView.findViewById(R.id.destination_text_view);
        TextView stopTextView = (TextView)
                listItemView.findViewById(R.id.stop_text_view);
        TextView firstPredictionTextView = (TextView)
                listItemView.findViewById(R.id.first_time_text_view);
        TextView secondPredictionTextView = (TextView)
                listItemView.findViewById(R.id.second_time_text_view);

        Trip[] tripArray = getItem(position);

        if (tripArray[0] != null) {
            // We can only display first two predictions in this list item view
            Trip firstTrip;
            Trip secondTrip;

            // Get the first trip
            firstTrip = tripArray[0];

            // If there is more than one trip, get the second trip
            if (tripArray.length > 1) {
                secondTrip = tripArray[1];
            } else {
                secondTrip = null;
            }

            // Display the route name
            routeTextView.setText(firstTrip.getRouteName());

            // Display the destination
            destinationTextView.setText(firstTrip.getDestination());

            // Display the name of the stop
            stopTextView.setText(firstTrip.getStopName());

            // Display the prediction time of the first trip
            String firstTime = firstTrip.getArrivalTime() / 60 + " min";
            firstPredictionTextView.setText(firstTime);

            // If there is a second trip, display its prediction time also
            if (secondTrip != null) {
                String secondTime = secondTrip.getArrivalTime() / 60 + " min";
                secondPredictionTextView.setText(secondTime);
            } else {
                secondPredictionTextView.setText("");
            }

            // Set the background color of the route name
            routeTextView.setBackgroundColor(getBackgroundColorId(listItemView, firstTrip.getRouteId()));

            // Set the font color of the route name
            routeTextView.setTextColor(getTextColorId(listItemView, firstTrip.getMode(),
                    firstTrip.getRouteId()));

        } else {
            routeTextView.setText("---");
            destinationTextView.setText("Empty Trip Error");
            stopTextView.setText("");
            firstPredictionTextView.setText("");
            secondPredictionTextView.setText("");
        }

        return listItemView;
    }

    private int getBackgroundColorId(View view, String routeId) {
        if (routeId.length() >= 5 && routeId.substring(0, 5).equals("Green")) {
            return ContextCompat.getColor(view.getContext(), R.color.GreenLine);
        } else if (routeId.equals("Red") || routeId.equals("Mattapan")) {
            return ContextCompat.getColor(view.getContext(), R.color.RedLine);
        } else if (routeId.equals("Blue")) {
            return ContextCompat.getColor(view.getContext(), R.color.BlueLine);
        } else if (routeId.equals("Orange")) {
            return ContextCompat.getColor(view.getContext(), R.color.OrangeLine);
        } else if (routeId.length() >= 2 && routeId.substring(0, 2).equals("CR")) {
            return ContextCompat.getColor(view.getContext(), R.color.CommuterRail);
        } else if (routeId.equals("741") || routeId.equals("742") || routeId.equals("746") ||
                routeId.equals("749") || routeId.equals("751")) {
            return ContextCompat.getColor(view.getContext(), R.color.SilverLine);
        } else if (routeId.length() >= 4 && routeId.substring(0, 4).equals("Boat")) {
            return ContextCompat.getColor(view.getContext(), R.color.Boat);
        } else {
            return ContextCompat.getColor(view.getContext(), R.color.Transparent);
        }
    }

    private int getTextColorId(View view, int mode, String routeId) {
        if ((mode != Route.Mode.BUS && mode != Route.Mode.UNKNOWN) ||
                (routeId.equals("741") || routeId.equals("742") || routeId.equals("746") ||
                        routeId.equals("749") || routeId.equals("751"))) {
            return ContextCompat.getColor(view.getContext(), R.color.HighlightedText);
        } else {
            return ContextCompat.getColor(view.getContext(), R.color.PrimaryText);
        }
    }
}
