package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.jsonParsers.VehiclesJsonParser;

public class VehiclesAsyncTask extends AsyncTask<Void, Void, Vehicle[]> {
    String realTimeApiKey;
    String routeId;
    OnPostExecuteListener onPostExecuteListener;

    public VehiclesAsyncTask(String realTimeApiKey,
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

        return VehiclesJsonParser.parse(realTimeApiClient.get("vehicles", routeArgs));
    }

    @Override
    protected void onPostExecute(Vehicle[] vehicles) {
        onPostExecuteListener.onPostExecute(vehicles);
    }

    public interface OnPostExecuteListener {
        public void onPostExecute(Vehicle[] vehicles);
    }
}
