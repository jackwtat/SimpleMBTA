package jackwtat.simplembta.asyncTasks;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.utilities.DateUtil;

public class SchedulesAsyncTask extends PredictionsAsyncTask {
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

        int maxHours;
        if (routeIds.length > 15) {
            maxHours = 2;
        } else if (routeIds.length > 5) {
            maxHours = 3;
        } else {
            maxHours = 5;
        }

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
                "filter[max_time]=" + DateUtil.getMbtaTime(maxHours),
                "include=route,trip,stop,prediction,vehicle"
        };

        String jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

        if (jsonResponse != null)
            return SchedulesJsonParser.parse(jsonResponse);
        else
            return null;
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions, false);
        else
            onPostExecuteListener.onError();
    }
}
