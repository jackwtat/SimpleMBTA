package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.utilities.DateUtil;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;

public class RouteDetailPredictionsAsyncTask extends AsyncTask<Void, Void, List<Prediction>> {
    private String realTimeApiKey;
    private OnPostExecuteListener onPostExecuteListener;

    private Route route;
    private String stopIds;
    private HashMap<String, Prediction> predictions = new HashMap<>();

    public RouteDetailPredictionsAsyncTask(String realTimeApiKey,
                                           Route route,
                                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.route = route;

        if (route.getNearestStop(0) != null) {
            stopIds = route.getNearestStop(0).getId();

            if (route.getNearestStop(1) != null) {
                stopIds += "," + route.getNearestStop(1).getId();
            }
        } else {
            stopIds = route.getNearestStop(1).getId();
        }

        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected List<Prediction> doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        // Get live predictions
        String[] predictionsArgs = {
                "filter[route]=" + route.getId(),
                "filter[stop]=" + stopIds,
                "include=route,trip,stop,schedule"
        };

        for (Prediction prediction : PredictionsJsonParser
                .parse(realTimeApiClient.get("predictions", predictionsArgs))) {
            predictions.put(prediction.getId(), prediction);
        }

        if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
            // Get non-live scheduled predictions
            String[] scheduleArgs = {
                    "filter[route]=" + route.getId(),
                    "filter[stop]=" + stopIds,
                    "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                    "filter[min_time]=" + DateUtil.getMbtaTime(0),
                    "include=route,trip,stop,prediction"
            };

            for (Prediction prediction : SchedulesJsonParser
                    .parse(realTimeApiClient.get("schedules", scheduleArgs))) {
                if (!predictions.containsKey(prediction.getId())) {
                    predictions.put(prediction.getId(), prediction);
                }
            }
        }

        return new ArrayList<>(predictions.values());
    }

    @Override
    protected void onPostExecute(List<Prediction> predictions) {
        onPostExecuteListener.onPostExecute(predictions);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(List<Prediction> predictions);
    }
}
