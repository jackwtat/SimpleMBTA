package jackwtat.simplembta.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.Adapters.PredictionsListAdapter;
import jackwtat.simplembta.Loaders.StopsLoader;
import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsListFragment extends Fragment implements LoaderCallbacks<List<Stop>>{
    private final static String LOG_TAG = "PredListsFragment";

    private final static int LOADER_ID = 1;

    private View rootView;
    private LoaderManager loaderManager;
    private ArrayAdapter<Stop> predictionsListAdapter;
    private Stop[] stops;

    protected abstract List<Stop> getStops();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = getLoaderManager();
        predictionsListAdapter = new PredictionsListAdapter(getActivity(), new ArrayList<Stop>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stops_list, container, false);
        ListView predictionsListView = (ListView) rootView.findViewById(R.id.predictions_list_view);
        predictionsListView.setAdapter(predictionsListAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        loaderManager.restartLoader(LOADER_ID, null, this).forceLoad();
    }

    /*
        LOADER METHODS
     */
    @Override
    public Loader<List<Stop>> onCreateLoader(int id, Bundle args) {
        List<Stop> stops = getStops();
        return new StopsLoader(getActivity(), stops);
    }

    @Override
    public void onLoadFinished(Loader<List<Stop>> loader, List<Stop> data) {
        predictionsListAdapter.clear();
        for(int i = 0; i < data.size(); i++){
            predictionsListAdapter.add(data.get(i));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Stop>> loader) {
        predictionsListAdapter.clear();
    }


}
