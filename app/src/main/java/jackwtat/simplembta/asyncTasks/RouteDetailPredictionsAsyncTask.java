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
    private int directionId;

    private HashMap<String, Prediction> predictions = new HashMap<>();

    public RouteDetailPredictionsAsyncTask(String realTimeApiKey,
                                           Route route,
                                           int directionId,
                                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.route = route;
        this.directionId = directionId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected List<Prediction> doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        // Get live predictions
        String[] predictionsArgs = {
                "filter[route]=" + route.getId(),
                "filter[direction_id]=" + directionId,
                "filter[stop]=" + route.getNearestStop(directionId).getId(),
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = realTimeApiClient.get("predictions", predictionsArgs);

        if (jsonResponse != null) {
            for (Prediction prediction : PredictionsJsonParser
                    .parse(jsonResponse)) {
                predictions.put(prediction.getId(), prediction);
            }
        } else {
            return null;
        }

        if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
            // Get non-live scheduled predictions
            String[] scheduleArgs = {
                    "filter[route]=" + route.getId(),
                    "filter[direction_id]=" + directionId,
                    "filter[stop]=" + route.getNearestStop(directionId).getId(),
                    "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                    "filter[min_time]=" + DateUtil.getMbtaTime(0),
                    "include=route,trip,stop,prediction"
            };

            jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

            if (jsonResponse != null) {
                for (Prediction prediction : SchedulesJsonParser
                        .parse(jsonResponse)) {
                    if (!predictions.containsKey(prediction.getId())) {
                        predictions.put(prediction.getId(), prediction);
                    }
                }
            } else {
                return null;
            }
        }

        if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL
                && predictions.size() == 0) {
            // Get the next day's predictions
            String[] scheduleArgs = {
                    "filter[route]=" + route.getId(),
                    "filter[direction_id]=" + directionId,
                    "filter[stop]=" + route.getNearestStop(directionId).getId(),
                    "filter[date]=" + DateUtil.getMbtaDate(1),
                    "filter[min_time]=" + "03:00",
                    "include=route,trip,stop,prediction"
            };

            jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

            if (jsonResponse != null) {
                for (Prediction prediction : SchedulesJsonParser
                        .parse(jsonResponse)) {
                    if (!predictions.containsKey(prediction.getId())) {
                        predictions.put(prediction.getId(), prediction);
                    }
                }
            } else {
                return null;
            }
        }

        return new ArrayList<>(predictions.values());
    }

    @Override
    protected void onPostExecute(List<Prediction> predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions);
        else
            onPostExecuteListener.onError();
    }

    public interface OnPostExecuteListener {
        void onSuccess(List<Prediction> predictions);

        void onError();
    }
}
