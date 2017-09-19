package jackwtat.simplembta.Fragments;

import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 9/19/2017.
 */

public class TestListFragment extends PredictionsListFragment {
    @Override
    protected List<Stop> getStops() {
        ArrayList<Stop> stops = new ArrayList<>();
        stops.add(new Stop("64", "Dudley Station"));
        return null;
    }
}
