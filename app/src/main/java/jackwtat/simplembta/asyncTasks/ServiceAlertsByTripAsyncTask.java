package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.ServiceAlertsJsonParser;
import jackwtat.simplembta.model.ServiceAlert;

public class ServiceAlertsByTripAsyncTask extends AsyncTask<Void, Void, ServiceAlert[]> {
    private String realTimeApiKey;
    private String tripId;
    private OnPostExecuteListener onPostExecuteListener;

    public ServiceAlertsByTripAsyncTask(String realTimeApiKey,
                                  String tripId,
                                  OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.tripId = tripId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected ServiceAlert[] doInBackground(Void... voids) {
        if (tripId == null || tripId.equals("")) {
            return new ServiceAlert[0];
        }

        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        String[] alertArgs = {
                "fields[alert]=header,description,url,effect,severity,lifecycle,informed_entity,active_period",
                "filter[trip]=" + tripId
        };

        String jsonResponse = realTimeApiClient.get("alerts", alertArgs);

        ArrayList<ServiceAlert> alerts = new ArrayList<>(Arrays.asList(ServiceAlertsJsonParser.parse(jsonResponse)));
        Collections.sort(alerts);

        if (jsonResponse != null)
            return alerts.toArray(new ServiceAlert[alerts.size()]);
        else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ServiceAlert[] serviceAlerts) {
        if (serviceAlerts != null)
            onPostExecuteListener.onSuccess(serviceAlerts);
        else
            onPostExecuteListener.onError();
    }

    public interface OnPostExecuteListener {
        void onSuccess(ServiceAlert[] serviceAlerts);

        void onError();
    }
}