package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.jsonParsers.ShapesJsonParser;

public class ShapesAsyncTask extends AsyncTask<Void, Void, Shape[]> {
    private String realTimeApiKey;
    private String[] routeIds;
    private OnPostExecuteListener onPostExecuteListener;

    public ShapesAsyncTask(String realTimeApiKey,
                           String[] routeIds,
                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeIds = routeIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    public ShapesAsyncTask(String realTimeApiKey,
                           String routeId,
                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        String[] routeIds = {routeId};
        this.routeIds = routeIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Shape[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder routeArgBuilder = new StringBuilder();
        for (String routeId : routeIds) {
            routeArgBuilder.append(routeId).append(",");
        }

        String[] shapeArgs = {
                "filter[route]=" + routeArgBuilder.toString(),
                "include=stops"
        };

        String jsonResponse = realTimeApiClient.get("shapes", shapeArgs);

        if (jsonResponse != null)
            return ShapesJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Shape[] shapes) {
        if (shapes != null)
            onPostExecuteListener.onSuccess(shapes);
        else
            onPostExecuteListener.onError();
    }

    public interface OnPostExecuteListener {
        void onSuccess(Shape[] shapes);

        void onError();
    }
}
