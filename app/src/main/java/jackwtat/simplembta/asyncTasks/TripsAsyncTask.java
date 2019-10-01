package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.TripsJsonParser;
import jackwtat.simplembta.model.Trip;

public class TripsAsyncTask extends AsyncTask<Void, Void, Trip[]> {
    String realTimeApiKey;
    String tripId;
    OnPostExecuteListener onPostExecuteListener;

    public TripsAsyncTask(String realTimeApiKey,
                          String tripId,
                          OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.tripId = tripId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Trip[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] tripArgs = {
                "filter[id]=" + tripId,
                "include=route,shape,stops"
        };

        String jsonResponse = realTimeApiClient.get("trips", tripArgs);

        if (jsonResponse != null) {
            return TripsJsonParser.parse(jsonResponse);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Trip[] trips) {
        if (trips != null && trips.length > 0) {
            onPostExecuteListener.onSuccess(trips);
        } else {
            onPostExecuteListener.onError();
        }
    }

    public interface OnPostExecuteListener {
        void onSuccess(Trip[] trips);

        void onError();
    }
}
