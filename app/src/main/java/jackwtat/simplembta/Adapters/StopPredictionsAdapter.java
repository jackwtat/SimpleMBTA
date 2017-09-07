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
import java.util.List;

import jackwtat.simplembta.Prediction;
import jackwtat.simplembta.R;
import jackwtat.simplembta.Stop;

/**
 * Created by jackw on 9/7/2017.
 */

public class StopPredictionsAdapter extends ArrayAdapter<Stop> {

    public StopPredictionsAdapter(Activity context, ArrayList<Stop> stops) {
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

        Stop stop = getItem(position);

        // Get and set the name of the stop
        TextView stopName = (TextView) listItemView.findViewById(R.id.stop_name_text);
        stopName.setText(stop.getName());

        // Populate the list of predictions
        for (int i = 0; i < stop.getPredictions().size(); i++){

        }

        return super.getView(position, convertView, parent);
    }
}
