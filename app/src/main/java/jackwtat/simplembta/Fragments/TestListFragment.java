package jackwtat.simplembta.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.Loaders.StopPredictionsLoader;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 9/19/2017.
 */

public class TestListFragment extends PredictionsListFragment implements LoaderCallbacks<List<Stop>> {
    private final static int LOADER_ID = 1;
    private LoaderManager loaderManager;

    /**********************
        FRAGMENT METHODS
     **********************/
    @Override
    protected List<Stop> getStops() {
        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(new Stop("64", "Dudley Station"));
        stops.add(new Stop("64000", "Dudley Station"));
        stops.add(new Stop("place-andrw", "Andrew"));
        return stops;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderManager = getLoaderManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        loaderManager.restartLoader(LOADER_ID, null, this).forceLoad();
    }

    /********************
        LOADER METHODS
     ********************/
    @Override
    public Loader<List<Stop>> onCreateLoader(int id, Bundle args) {
        List<Stop> stops = getStops();
        return new StopPredictionsLoader(getActivity(), stops);
    }

    @Override
    public void onLoadFinished(Loader<List<Stop>> loader, List<Stop> data) {
        populateList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Stop>> loader) {
        clearList();
    }
}
