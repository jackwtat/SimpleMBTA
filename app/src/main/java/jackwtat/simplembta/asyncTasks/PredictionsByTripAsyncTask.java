package jackwtat.simplembta.asyncTasks;

import java.util.HashMap;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.utilities.DateUtil;

public class PredictionsByTripAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String tripId;
    private int dateOffset;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsByTripAsyncTask(String realTimeApiKey,
                                      String tripId,
                                      int dateOffset,
                                      OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.tripId = tripId;
        this.dateOffset = dateOffset;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        HashMap<String, Prediction> predictions = new HashMap<>();

        String[] scheduleArgs = {
                "filter[trip]=" + tripId,
                "filter[date]=" + DateUtil.getMbtaDate(dateOffset),
                "include=route,trip,stop,prediction"
        };

        String jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);
        if (jsonResponse != null) {
            for (Prediction p : SchedulesJsonParser.parse(jsonResponse)) {
                predictions.put(p.getId(), p);
            }
        }

        String[] predictionsArgs = {
                "filter[trip]=" + tripId,
                "include=route,trip,stop,schedule,vehicle"
        };

        jsonResponse = realTimeApiClient.get("predictions", predictionsArgs);

        if (jsonResponse != null) {
            for (Prediction p : PredictionsJsonParser.parse(jsonResponse)) {
                predictions.put(p.getId(), p);
            }
        }

        return predictions.values().toArray(new Prediction[0]);
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions, true);
        else
            onPostExecuteListener.onError();
    }
}
