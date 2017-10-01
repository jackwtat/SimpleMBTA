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
import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsListFragment extends Fragment {
    private final static String LOG_TAG = "PredListsFragment";

    private View rootView;
    private ArrayAdapter<Stop> predictionsListAdapter;

    protected abstract List<Stop> getStops();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        predictionsListAdapter = new GroupedPredictionsListAdapter(getActivity(), new ArrayList<Stop>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stops_list, container, false);
        ListView predictionsListView = (ListView) rootView.findViewById(R.id.predictions_list_view);
        predictionsListView.setAdapter(predictionsListAdapter);
        return rootView;
    }

    protected void populateList(List<Stop> data){
        predictionsListAdapter.clear();
        for(int i = 0; i < data.size(); i++){
            predictionsListAdapter.add(data.get(i));
        }
    }

    protected void clearList(){
        predictionsListAdapter.clear();
    }
}
