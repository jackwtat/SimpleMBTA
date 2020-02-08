package jackwtat.simplembta.asyncTasks;


import java.util.ArrayList;
import java.util.Arrays;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.model.Prediction;

public class PredictionsByStopsAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String[] stopIds;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsByStopsAsyncTask(String realTimeApiKey,
                                       String[] stopIds,
                                       OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.stopIds = stopIds;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        ArrayList<Prediction> predictions = new ArrayList<>();
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        StringBuilder stopArgBuilder = new StringBuilder();
        for (String stopId : stopIds) {
            stopArgBuilder.append(stopId).append(",");
        }
        String stops = stopArgBuilder.toString();

        predictions.addAll(makeQuery(realTimeApiClient, stops, "0,1"));
        predictions.addAll(makeQuery(realTimeApiClient, stops, "2,3,4"));

        return predictions.toArray(new Prediction[0]);
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null)
            onPostExecuteListener.onSuccess(predictions, true);
        else
            onPostExecuteListener.onError();
    }

    private ArrayList<Prediction> makeQuery(RealTimeApiClient client, String stops, String routeTypes) {
        String[] predictionsArgs = {
                "fields[prediction]=stop_sequence,arrival_time,departure_time,schedule_relationship",
                "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                "fields[trip]=direction_id,headsign,name",
                "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status",
                "fields[schedule]=pickup_type,arrival_time,departure_time",
                "fields[stop]=name,latitude,longitude,wheelchair_boarding,platform_code",
                "filter[stop]=" + stops,
                "filter[route_type]=" + routeTypes,
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = client.get("predictions", predictionsArgs);

        if (jsonResponse != null)
            return new ArrayList<>(Arrays.asList(PredictionsJsonParser.parse(jsonResponse)));
        else
            return new ArrayList<>();
    }
}
