package jackwtat.simplembta.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsFragment extends Fragment implements LoaderCallbacks<List<Stop>>{
    private View rootView;
    private LoaderManager loaderManager;
    private ArrayAdapter<Stop> stopAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loaderManager = getLoaderManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stops_list, container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
