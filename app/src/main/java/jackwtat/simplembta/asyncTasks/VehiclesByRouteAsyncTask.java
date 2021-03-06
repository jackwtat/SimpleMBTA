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

    public VehiclesByRouteAsyncTask(String realTimeApiKey,
                                    String[] routeIds,
                                    OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeId = "";
        this.onPostExecuteListener = onPostExecuteListener;

        for (String id : routeIds) {
            routeId += id + ",";
        }
    }

    @Override
    protected Vehicle[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] routeArgs = {
                "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status,occupancy_status",
                "include=trip&fields[trip]=name,headsign",
                "filter[route]=" + routeId
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
