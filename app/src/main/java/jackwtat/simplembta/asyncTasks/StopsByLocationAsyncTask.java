package jackwtat.simplembta.asyncTasks;

import android.location.Location;
import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.StopsJsonParser;
import jackwtat.simplembta.model.Stop;

public class StopsByLocationAsyncTask extends AsyncTask<Void, Void, Stop[]> {
    private String realTimeApiKey;
    private Location targetLocation;
    private OnPostExecuteListener onPostExecuteListener;

    public StopsByLocationAsyncTask(String realTimeApiKey,
                                    Location targetLocation,
                                    OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.targetLocation = targetLocation;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Stop[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] stopArgs = {
                "filter[latitude]=" + Double.toString(targetLocation.getLatitude()),
                "filter[longitude]=" + Double.toString(targetLocation.getLongitude()),
                "include=child_stops"
        };

        return StopsJsonParser.parse(realTimeApiClient.get("stops", stopArgs));
    }

    @Override
    protected void onPostExecute(Stop[] stops) {
        onPostExecuteListener.onPostExecute(stops);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(Stop[] stops);
    }
}
