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

import java.util.ArrayList;

import jackwtat.simplembta.MbtaData.Prediction;
import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.R;

/**
 * Created by jackw on 10/1/2017.
 */

public class IndividualPredictionsListAdapter extends ArrayAdapter<Prediction> {
    public IndividualPredictionsListAdapter(Activity context, ArrayList<Prediction> predictions) {
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

        return super.getView(position, convertView, parent);
    }
}
