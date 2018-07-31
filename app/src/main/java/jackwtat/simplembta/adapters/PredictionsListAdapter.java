package jackwtat.simplembta.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.views.PredictionsCardView;

/**
 * Created by jackw on 12/26/2017.
 */

public class PredictionsListAdapter extends ArrayAdapter<ArrayList<Prediction>> {
    public PredictionsListAdapter(
            @NonNull Context context, ArrayList<ArrayList<Prediction>> predictions) {
        super(context, 0, predictions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PredictionsCardView listItemView = (PredictionsCardView) convertView;

        ArrayList<Prediction> predictions = getItem(position);

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = new PredictionsCardView(getContext());
        }

        // Check if there are no Predictions
        if (predictions == null || predictions.size() < 1) {
            return listItemView;
        }

        listItemView.clear();
        listItemView.setPredictions(predictions);

        return listItemView;
    }
}
