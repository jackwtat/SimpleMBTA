package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.jsonParsers.VehiclesJsonParser;

public class VehiclesByRouteAsyncTask extends AsyncTask<Void, Void, Vehicle[]> {
    String realTimeApiKey;
    String routeId;
    OnPostExecuteListener onPostExecuteListener;

    public VehiclesByRouteAsyncTask(String realTimeApiKey,
                                    String routeId,
                                    OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeId = routeId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Vehicle[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] routeArgs = {
                "filter[route]=" + routeId,
                "include=trip"
        };

        String jsonResponse = realTimeApiClient.get("vehicles", routeArgs);

        if (jsonResponse != null)
            return VehiclesJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Vehicle[] vehicles) {
        if (vehicles != null)
            onPostExecuteListener.onSuccess(vehicles);
        else
            onPostExecuteListener.onError();
    }

    public interface OnPostExecuteListener {
        void onSuccess(Vehicle[] vehicles);

        void onError();
    }
}
