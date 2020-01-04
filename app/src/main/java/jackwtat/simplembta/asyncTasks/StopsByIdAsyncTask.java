package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.StopsJsonParser;
import jackwtat.simplembta.model.Stop;

public class StopsByIdAsyncTask extends AsyncTask<Void, Void, Stop[]> {
    private String realTimeApiKey;
    private String[] stopIds;
    private OnPostExecuteListener onPostExecuteListener;

    public StopsByIdAsyncTask(String realTimeApiKey,
                              String[] stopIds,
                              OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.stopIds = stopIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Stop[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder stopArgBuilder = new StringBuilder();
        for (String stopId : stopIds) {
            stopArgBuilder.append(stopId).append(",");
        }
        String stops = stopArgBuilder.toString();

        String[] stopArgs = {
                "filter[id]=" + stops,
                "include=child_stops"
        };

        String jsonResponse = realTimeApiClient.get("stops", stopArgs);

        if (jsonResponse != null) {
            return StopsJsonParser.parse(jsonResponse);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Stop[] stops) {
        if (stops != null) {
            onPostExecuteListener.onSuccess(stops);
        } else {
            onPostExecuteListener.onError();
        }
    }

    public interface OnPostExecuteListener {
        void onSuccess(Stop[] stops);

        void onError();
    }
}
