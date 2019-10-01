package jackwtat.simplembta.asyncTasks;

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

        String[] predictionsArgs = {
                "filter[trip]=" + tripId,
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = realTimeApiClient.get("predictions", predictionsArgs);

        if (jsonResponse != null) {
            Prediction[] predictions = PredictionsJsonParser.parse(jsonResponse);
            if (predictions.length > 0) {
                return predictions;
            }
        }

        // Failed to get predictions, get schedules
        String[] scheduleArgs = {
                "filter[trip]=" + tripId,
                "filter[date]=" + DateUtil.getMbtaDate(dateOffset),
                "include=route,trip,stop,prediction"
        };

        jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

        if (jsonResponse != null) {
            return SchedulesJsonParser.parse(jsonResponse);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions, true);
        else
            onPostExecuteListener.onError();
    }
}
