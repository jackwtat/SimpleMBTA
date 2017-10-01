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

public class StopPredictionsLoader extends AsyncTaskLoader<List<Stop>> {
    private static final String LOG_TAG = "StopPredictionsLoader";

    List<Stop> stops;

    public StopPredictionsLoader(Context context, List<Stop> stops) {
        super(context);
        this.stops = stops;
    }

    @Override
    public List<Stop> loadInBackground() {
        if (stops ==null) {
            return null;
        }

        for(int i = 0; i < stops.size(); i++){
            String stopId = stops.get(i).getId();

            stops.get(i).addRoutes(QueryUtil.fetchRoutesByStop(stopId));
            stops.get(i).addPredictions(QueryUtil.fetchPredictionsByStop(stopId));
        }

        return stops;
    }
}
