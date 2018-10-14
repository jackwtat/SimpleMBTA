package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.utilities.ShapesJsonParser;

public class ShapesAsyncTask extends AsyncTask<Void, Void, Shape[]> {
    private String realTimeApiKey;
    private String routeId;
    private Callbacks callbacks;

    public ShapesAsyncTask(String realTimeApiKey,
                           String routeId,
                           Callbacks callbacks) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeId = routeId;
        this.callbacks = callbacks;
    }

    @Override
    protected void onPreExecute() {
        callbacks.onPreExecute();
    }

    @Override
    protected Shape[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] routeArgs = {
                "filter[route]=" + routeId,
                "include=stops"
        };

        Shape[] shapes = ShapesJsonParser.parse(realTimeApiClient.get("shapes", routeArgs));

        return shapes;
    }

    @Override
    protected void onPostExecute(Shape[] shapes) {
        callbacks.onPostExecute(shapes);
    }

    public interface Callbacks {
        void onPreExecute();

        void onPostExecute(Shape[] shapes);
    }
}
