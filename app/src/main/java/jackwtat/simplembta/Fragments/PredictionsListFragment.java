package jackwtat.simplembta.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.Adapters.GroupedPredictionsListAdapter;
import jackwtat.simplembta.Adapters.IndividualPredictionsListAdapter;
import jackwtat.simplembta.MbtaData.Prediction;
import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsListFragment extends Fragment {
    private final static String LOG_TAG = "PredListsFragment";

    private View rootView;
    private ArrayAdapter<Prediction[]> predictionsListAdapter;

    protected abstract List<Stop> getStops();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        predictionsListAdapter = new IndividualPredictionsListAdapter(getActivity(), new ArrayList<Prediction[]>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stops_list, container, false);
        ListView predictionsListView = (ListView) rootView.findViewById(R.id.predictions_list_view);
        predictionsListView.setAdapter(predictionsListAdapter);
        return rootView;
    }

    protected void populateList(List<Stop> stops) {
        predictionsListAdapter.clear();

        ArrayList<String> trips = new ArrayList<>();

        for (int i = 0; i < stops.size(); i++) {
            Prediction[][][] predArray = stops.get(i).getSortedPredictions(2);

            for (int j = 0; j < predArray.length; j++) {
                for (int k = 0; k < predArray[j].length; k++) {
                    if (predArray[j][k][0] != null) {
                        if (!trips.contains(predArray[j][k][0].getTripId())) {
                            predictionsListAdapter.add(predArray[j][k]);
                            trips.add(predArray[j][k][0].getTripId());
                            if (predArray[j][k][1] != null) {
                                trips.add(predArray[j][k][1].getTripId());
                            }
                        } else if (predArray[j][k][1] != null) {
                            if (!trips.contains(predArray[j][k][1].getTripId())) {
                                predictionsListAdapter.add(predArray[j][k]);
                                trips.add(predArray[j][k][0].getTripId());
                            }
                        }
                    }
                }
            }
        }
    }

    protected void clearList() {
        predictionsListAdapter.clear();
    }
}
