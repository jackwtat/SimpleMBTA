package jackwtat.simplembta.asyncTasks;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;
import jackwtat.simplembta.model.Prediction;

public class SchedulesByTripAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String tripId;
    private OnPostExecuteListener onPostExecuteListener;

    public SchedulesByTripAsyncTask(String realTimeApiKey,
                                    String tripId,
                                    OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.tripId = tripId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] scheduleArgs = {
                "filter[trip]=" + tripId,
                "include=route,trip,stop,prediction,vehicle"
        };

        String jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

        if (jsonResponse != null)
            return SchedulesJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions, true);
        else
            onPostExecuteListener.onError();
    }
}
