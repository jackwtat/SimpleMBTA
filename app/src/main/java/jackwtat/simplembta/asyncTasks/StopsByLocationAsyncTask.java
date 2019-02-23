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

        String jsonResponse = realTimeApiClient.get("stops", stopArgs);

        if (jsonResponse != null)
            return StopsJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Stop[] stops) {
        if (stops != null)
            onPostExecuteListener.onSuccess(stops);
        else
            onPostExecuteListener.onError();
    }

    public interface OnPostExecuteListener {
        void onSuccess(Stop[] stops);

        void onError();
    }
}
