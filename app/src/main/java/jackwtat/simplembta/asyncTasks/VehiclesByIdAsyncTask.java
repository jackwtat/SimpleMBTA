package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.VehiclesJsonParser;
import jackwtat.simplembta.model.Vehicle;

public class VehiclesByIdAsyncTask extends AsyncTask<Void, Void, Vehicle[]> {
    String realTimeApiKey;
    String vehicleId;
    OnPostExecuteListener onPostExecuteListener;

    public VehiclesByIdAsyncTask(String realTimeApiKey,
                                 String vehicleId,
                                 OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.vehicleId = vehicleId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Vehicle[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] tripArgs = {
                "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status",
                "filter[id]=" + vehicleId,
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
