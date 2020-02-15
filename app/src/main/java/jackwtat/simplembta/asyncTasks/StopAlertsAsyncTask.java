package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.jsonParsers.ServiceAlertsJsonParser;

public class StopAlertsAsyncTask extends AsyncTask<Void, Void, ServiceAlert[]> {
    private String realTimeApiKey;
    private String[] stopIds;
    private OnPostExecuteListener onPostExecuteListener;

    public StopAlertsAsyncTask(String realTimeApiKey,
                               String[] stopIds,
                               OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.stopIds = stopIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected ServiceAlert[] doInBackground(Void... voids) {
        if (stopIds.length == 0) {
            return new ServiceAlert[0];
        }

        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder stopArgBuilder = new StringBuilder();

        for (String routeId : stopIds) {
            stopArgBuilder.append(routeId).append(",");
        }

        String[] stopArgs = {
                "fields[alert]=header,description,url,effect,severity,lifecycle,informed_entity,active_period",
                "filter[stop]=" + stopArgBuilder.toString(),
                "filter[activity]=ALL"
        };

        String jsonResponse = realTimeApiClient.get("alerts", stopArgs);

        ArrayList<ServiceAlert> alerts = new ArrayList<>(Arrays.asList(ServiceAlertsJsonParser.parse(jsonResponse)));
        Collections.sort(alerts);

        if (jsonResponse != null) {
            return alerts.toArray(new ServiceAlert[0]);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ServiceAlert[] serviceAlerts) {
        if (serviceAlerts != null) {
            onPostExecuteListener.onSuccess(serviceAlerts);
        } else {
            onPostExecuteListener.onError();
        }
    }

    public interface OnPostExecuteListener {
        void onSuccess(ServiceAlert[] serviceAlerts);

        void onError();
    }
}
