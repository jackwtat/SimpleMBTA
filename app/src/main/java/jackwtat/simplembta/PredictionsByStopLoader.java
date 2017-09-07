package jackwtat.simplembta;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

import jackwtat.simplembta.Utils.QueryUtil;

/**
 * Created by jackw on 9/6/2017.
 */

public class PredictionsByStopLoader extends AsyncTaskLoader<List<Prediction>> {
    private String stopId;

    public PredictionsByStopLoader(Context context, String stopId) {
        super(context);
        this.stopId = stopId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Prediction> loadInBackground() {
        if (stopId == null){
            return null;
        }

        return QueryUtil.fetchPredictionsByStop(stopId);
    }
}
