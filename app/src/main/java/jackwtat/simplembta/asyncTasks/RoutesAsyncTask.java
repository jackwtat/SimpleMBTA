package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.RoutesJsonParser;
import jackwtat.simplembta.model.routes.Route;

public class RoutesAsyncTask extends AsyncTask<Void, Void, Route[]> {
    String realTimeApiKey;
    OnPostExecuteListener onPostExecuteListener;

    public RoutesAsyncTask(String realTimeApiKey,
                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Route[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String jsonResponse = realTimeApiClient.get("routes", new String[0]);

        if (jsonResponse != null)
            return RoutesJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Route[] routes) {
        if (routes != null)
            onPostExecuteListener.onSuccess(routes);
        else
            onPostExecuteListener.onError();
    }

    public interface OnPostExecuteListener {
        void onSuccess(Route[] routes);

        void onError();
    }
}
