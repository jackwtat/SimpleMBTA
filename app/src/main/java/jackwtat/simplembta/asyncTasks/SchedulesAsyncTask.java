package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.utilities.DateUtil;

public class SchedulesAsyncTask extends AsyncTask<Void, Void, Prediction[]> {
    private String realTimeApiKey;
    private String[] routeIds;
    private String[] stopIds;
    private OnPostExecuteListener onPostExecuteListener;

    public SchedulesAsyncTask(String realTimeApiKey,
                              String[] routeIds,
                              String[] stopIds,
                              OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeIds = routeIds;
        this.stopIds = stopIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }


    @Override
    protected Prediction[] doInBackground(Void... voids) {
        if (routeIds.length == 0 || stopIds.length == 0)
            return new Prediction[0];

        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder routeArgBuilder = new StringBuilder();

        for (String routeId : routeIds) {
            routeArgBuilder.append(routeId).append(",");
        }

        StringBuilder stopArgBuilder = new StringBuilder();

        for (String stopId : stopIds) {
            stopArgBuilder.append(stopId).append(",");
        }

        String[] scheduleArgs = {
                "filter[route]=" + routeArgBuilder.toString(),
                "filter[stop]=" + stopArgBuilder.toString(),
                "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                "filter[min_time]=" + DateUtil.getMbtaTime(0),
                "filter[max_time]=" + DateUtil.getMbtaTime(3),
                "include=route,trip,stop,prediction,vehicle"
        };

        return SchedulesJsonParser.parse(realTimeApiClient.get("schedules", scheduleArgs));
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        Log.i("SchedulesAsyncTask", "Count - " + predictions.length);
        onPostExecuteListener.onPostExecute(predictions, false);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(Prediction[] predictions, boolean live);
    }
}
