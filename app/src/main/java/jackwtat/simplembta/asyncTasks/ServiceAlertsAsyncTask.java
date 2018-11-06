package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.jsonParsers.ServiceAlertsJsonParser;

public class ServiceAlertsAsyncTask extends AsyncTask<Void, Void, ServiceAlert[]> {
    private String realTimeApiKey;
    private String routeId;
    private OnPostExecuteListener onPostExecuteListener;

    public ServiceAlertsAsyncTask(String realTimeApiKey,
                                  String routeId,
                                  OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeId = routeId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected ServiceAlert[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] routeArgs = {
                "filter[route]=" + routeId,
                "include=stops"
        };

        return ServiceAlertsJsonParser.parse(realTimeApiClient.get("alerts", routeArgs));
    }

    @Override
    protected void onPostExecute(ServiceAlert[] serviceAlerts) {
        onPostExecuteListener.onPostExecute(serviceAlerts);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(ServiceAlert[] serviceAlerts);
    }
}
