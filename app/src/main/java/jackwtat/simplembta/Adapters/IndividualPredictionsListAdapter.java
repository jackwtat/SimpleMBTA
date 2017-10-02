package jackwtat.simplembta.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jackwtat.simplembta.MbtaData.Prediction;
import jackwtat.simplembta.MbtaData.Route;
import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.R;

/**
 * Created by jackw on 10/1/2017.
 */

public class IndividualPredictionsListAdapter extends ArrayAdapter<Prediction[]> {
    public IndividualPredictionsListAdapter(Activity context, ArrayList<Prediction[]> predictions) {
        super(context, 0, predictions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.individual_prediction_list_item, parent, false);
        }

        // Initialize list item view elements
        TextView routeTextView = (TextView) listItemView.findViewById(R.id.route_text_view);
        TextView destinationTextView = (TextView) listItemView.findViewById(R.id.destination_text_view);
        TextView stopTextView = (TextView) listItemView.findViewById(R.id.stop_text_view);
        TextView firstTimeTextView = (TextView) listItemView.findViewById(R.id.first_time_text_view);
        TextView secondTimeTextView = (TextView) listItemView.findViewById(R.id.second_time_text_view);

        Prediction[] predArray = getItem(position);

        if (predArray[0] != null) {
            // We can only display first two predictions in this list item view
            Prediction firstPred;
            Prediction secondPred;

            firstPred = predArray[0];
            if (predArray.length > 1) {
                secondPred = predArray[1];
            } else {
                secondPred = null;
            }

            routeTextView.setText(firstPred.getRouteName());
            destinationTextView.setText(firstPred.getDestination());
            stopTextView.setText(firstPred.getStopName());

            String firstTime = firstPred.getArrivalTime() / 60 + " min";
            firstTimeTextView.setText(firstTime);

            if (secondPred != null) {
                String secondTime = secondPred.getArrivalTime() / 60 + " min";
                secondTimeTextView.setText(secondTime);
            } else {
                secondTimeTextView.setText("");
            }
        } else {
            routeTextView.setText("---");
            destinationTextView.setText("Empty Prediction Error");
            stopTextView.setText("");
            firstTimeTextView.setText("");
            secondTimeTextView.setText("");
        }

        return listItemView;
    }
}
