package jackwtat.simplembta.asyncTasks;

import java.util.Date;
import java.util.HashMap;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.utilities.DateUtil;

public class PredictionsByTripAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String tripId;
    private Date date;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsByTripAsyncTask(String realTimeApiKey,
                                      String tripId,
                                      Date date,
                                      OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.tripId = tripId;
        this.date = date;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        HashMap<String, Prediction> predictions = new HashMap<>();

        long lowestCountdown = 1000 * 60 * 60 * 24;

        String[] scheduleArgs = {
                "fields[schedule]=stop_sequence,arrival_time,departure_time,pickup_type",
                "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                "fields[trip]=direction_id,headsign,name",
                "fields[stop]=name,latitude,longitude,wheelchair_boarding",
                "fields[prediction]=stop_sequence",
                "filter[trip]=" + tripId,
                "filter[date]=" + DateUtil.getMbtaDate(date),
                "include=route,trip,stop,prediction"
        };

        String jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);
        if (jsonResponse != null) {
            for (Prediction p : SchedulesJsonParser.parse(jsonResponse)) {
                predictions.put(p.getId(), p);

                if (p.getCountdownTime() < lowestCountdown) {
                    lowestCountdown = p.getCountdownTime();
                }
            }
        }

        if (predictions.size() == 0 || lowestCountdown < 1000 * 60 * 60 * 6) {
            String[] predictionsArgs = {
                    "fields[prediction]=stop_sequence,arrival_time,departure_time,schedule_relationship",
                    "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                    "fields[trip]=direction_id,headsign,name",
                    "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status",
                    "fields[schedule]=pickup_type,arrival_time,departure_time",
                    "filter[trip]=" + tripId,
                    "include=route,trip,stop,schedule,vehicle"
            };

            jsonResponse = realTimeApiClient.get("predictions", predictionsArgs);

            if (jsonResponse != null) {
                for (Prediction p : PredictionsJsonParser.parse(jsonResponse)) {
                    predictions.put(p.getId(), p);
                }
            }
        }

        return predictions.values().toArray(new Prediction[0]);
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions, true);
        else
            onPostExecuteListener.onError();
    }
}
