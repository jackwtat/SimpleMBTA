package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.VehiclesJsonParser;
import jackwtat.simplembta.model.Vehicle;

public class VehiclesByTripAsyncTask extends AsyncTask<Void, Void, Vehicle[]> {
    String realTimeApiKey;
    String tripId;
    OnPostExecuteListener onPostExecuteListener;

    public VehiclesByTripAsyncTask(String realTimeApiKey,
                                   String tripId,
                                   OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.tripId = tripId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Vehicle[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] tripArgs = {
                "filter[trip]=" + tripId,
                "include=trip"
        };

        String jsonResponse = realTimeApiClient.get("vehicles", tripArgs);

        if (jsonResponse != null) {
            return VehiclesJsonParser.parse(jsonResponse);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Vehicle[] vehicles) {
        if (vehicles != null) {
            onPostExecuteListener.onSuccess(vehicles);
        } else {
            onPostExecuteListener.onError();
        }
    }

    public interface OnPostExecuteListener {
        void onSuccess(Vehicle[] vehicles);

        void onError();
    }
}
