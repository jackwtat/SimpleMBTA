package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.utilities.VehiclesJsonParser;

public class VehiclesAsyncTask extends AsyncTask<Void, Void, Vehicle[]> {
    String realTimeApiKey;
    String routeId;
    int direction;
    OnPostExecuteListener onPostExecuteListener;

    public VehiclesAsyncTask(String realTimeApiKey,
                             String routeId,
                             int direction,
                             OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeId = routeId;
        this.direction = direction;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Vehicle[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] routeArgs = {
                "filter[route]=" + routeId,
                "filter[direction_id]=" + direction
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
