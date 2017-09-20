package jackwtat.simplembta.Loaders;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.Utils.QueryUtil;

/**
 * Created by jackw on 9/14/2017.
 */

public class StopsPredictionsLoader extends AsyncTaskLoader<List<Stop>> {
    private static final String LOG_TAG = "StopsPredictionsLoader";

    List<Stop> stops;

    public StopsPredictionsLoader(Context context, List<Stop> stops) {
        super(context);
        this.stops = stops;
    }

    @Override
    public List<Stop> loadInBackground() {
        Log.i(LOG_TAG, "loadInBackground");

        if (stops ==null) {
            return null;
        }

        for(int i = 0; i < stops.size(); i++){
            stops.get(i).addPredictions(QueryUtil.fetchPredictionsByStop(stops.get(i).getId()));

            Log.i(LOG_TAG, "Stop " + stops.get(i).getName());
        }

        return stops;
    }
}
