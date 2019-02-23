package jackwtat.simplembta.asyncTasks;


import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.model.Prediction;

public class PredictionsByStopsAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String[] stopIds;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsByStopsAsyncTask(String realTimeApiKey,
                                       String[] stopIds,
                                       OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.stopIds = stopIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder stopArgBuilder = new StringBuilder();
        for (String stopId : stopIds) {
            stopArgBuilder.append(stopId).append(",");
        }

        String[] predictionsArgs = {
                "filter[stop]=" + stopArgBuilder.toString(),
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = realTimeApiClient.get("predictions", predictionsArgs);

        if (jsonResponse != null)
            return PredictionsJsonParser.parse(jsonResponse);
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
