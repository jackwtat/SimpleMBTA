package jackwtat.simplembta.asyncTasks;

import android.location.Location;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.model.Prediction;

public class PredictionsByLocationAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private Location targetLocation;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsByLocationAsyncTask(String realTimeApiKey,
                                          Location targetLocation,
                                          OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.targetLocation = targetLocation;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] predictionsArgs = {
                "filter[latitude]=" + Double.toString(targetLocation.getLatitude()),
                "filter[longitude]=" + Double.toString(targetLocation.getLongitude()),
                "include=route,trip,stop,schedule,vehicle"
        };

        return PredictionsJsonParser.parse(realTimeApiClient.get("predictions", predictionsArgs));
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        onPostExecuteListener.onPostExecute(predictions, true);
    }
}
