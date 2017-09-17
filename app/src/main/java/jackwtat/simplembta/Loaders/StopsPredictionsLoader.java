package jackwtat.simplembta.Loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.Utils.QueryUtil;

/**
 * Created by jackw on 9/14/2017.
 */

public class StopsPredictionsLoader extends AsyncTaskLoader<List<Stop>> {
    List<Stop> stops;

    public StopsPredictionsLoader(Context context, List<Stop> stops) {
        super(context);
        this.stops = stops;
    }

    @Override
    public List<Stop> loadInBackground() {
        for(int i = 0; i < stops.size(); i++){
            stops.get(i).addPredictions(QueryUtil.fetchPredictionsByStop(stops.get(i).getId()));
        }

        return stops;
    }
}
