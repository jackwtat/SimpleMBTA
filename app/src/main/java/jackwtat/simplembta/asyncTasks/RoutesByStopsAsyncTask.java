package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.RoutesJsonParser;
import jackwtat.simplembta.model.Route;

public class RoutesByStopsAsyncTask extends AsyncTask<Void, Void, Route[]> {
    private String realTimeApiKey;
    private String[] stopIds;
    private OnPostExecuteListener onPostExecuteListener;

    public RoutesByStopsAsyncTask(String realTimeApiKey,
                                  String[] stopIds,
                                  OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.stopIds = stopIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Route[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder stopArgBuilder = new StringBuilder();
        for (String stopId : stopIds) {
            stopArgBuilder.append(stopId).append(",");
        }
        String[] routesArgs = {"filter[stop]=" + stopArgBuilder.toString()};

        String jsonResponse = realTimeApiClient.get("routes", routesArgs);

        if (jsonResponse != null)
            return RoutesJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Route[] routes) {
        if (routes != null) {
            onPostExecuteListener.onSuccess(routes);
        }else {
            onPostExecuteListener.onError();
        }
    }

    public interface OnPostExecuteListener {
        void onSuccess(Route[] routes);

        void onError();
    }
}
