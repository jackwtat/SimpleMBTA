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

        return RoutesJsonParser.parse(realTimeApiClient.get("routes", new String[0]));
    }

    @Override
    protected void onPostExecute(Route[] routes) {
        onPostExecuteListener.onPostExecute(routes);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(Route[] routes);
    }
}
