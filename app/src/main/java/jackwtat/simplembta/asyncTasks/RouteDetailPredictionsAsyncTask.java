package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.DateUtil;
import jackwtat.simplembta.utilities.PredictionsJsonParser;
import jackwtat.simplembta.utilities.SchedulesJsonParser;

public class RouteDetailPredictionsAsyncTask extends AsyncTask<Void, Void, List<Prediction>> {
    private String realTimeApiKey;
    private Callbacks callbacks;

    private Route route;
    private int direction;
    private HashMap<String, Prediction> predictions = new HashMap<>();

    public RouteDetailPredictionsAsyncTask(String realTimeApiKey,
                                           Route route,
                                           int direction,
                                           Callbacks callbacks) {
        this.realTimeApiKey = realTimeApiKey;
        this.route = route;
        this.direction = direction;
        this.callbacks = callbacks;
    }

    @Override
    protected void onPreExecute() {
        callbacks.onPreExecute();
    }

    @Override
    protected List<Prediction> doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        // Get live predictions
        String[] predictionsArgs = {
                "filter[route]=" + route.getId(),
                "filter[stop]=" + route.getNearestStop(direction).getId(),
                "filter[direction_id]=" + direction,
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
                    "filter[stop]=" + route.getNearestStop(direction).getId(),
                    "filter[direction_id]=" + direction,
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
        callbacks.onPostExecute(predictions);
    }

    public interface Callbacks {
        void onPreExecute();

        void onPostExecute(List<Prediction> predictions);
    }
}
