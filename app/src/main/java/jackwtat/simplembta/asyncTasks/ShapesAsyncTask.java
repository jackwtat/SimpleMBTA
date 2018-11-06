package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;

public class ShapesAsyncTask extends AsyncTask<Void, Void, Shape[]> {
    private String realTimeApiKey;
    private String routeId;
    private int direction;
    private OnPostExecuteListener onPostExecuteListener;

    public ShapesAsyncTask(String realTimeApiKey,
                           String routeId,
                           int direction,
                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeId = routeId;
        this.direction = direction;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Shape[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] routeArgs = {
                "filter[route]=" + routeId,
                "filter[direction_id]=" + direction,
                "include=stops"
        };

        return ShapesJsonParser.parse(realTimeApiClient.get("shapes", routeArgs));
    }

    @Override
    protected void onPostExecute(Shape[] shapes) {
        onPostExecuteListener.onPostExecute(shapes);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(Shape[] shapes);
    }
}
