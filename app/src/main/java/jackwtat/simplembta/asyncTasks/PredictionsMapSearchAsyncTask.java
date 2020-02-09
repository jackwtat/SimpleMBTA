package jackwtat.simplembta.asyncTasks;

import java.util.ArrayList;
import java.util.Arrays;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.model.Prediction;

public class PredictionsMapSearchAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String[] routeIds;
    private double latitude;
    private double longitude;
    private double radius;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsMapSearchAsyncTask(String realTimeApiKey,
                                         double latitude,
                                         double longitude,
                                         double radius,
                                         OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeIds = new String[0];
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    public PredictionsMapSearchAsyncTask(String realTimeApiKey,
                                         String[] routeIds,
                                         double latitude,
                                         double longitude,
                                         double radius,
                                         OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.routeIds = routeIds;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {
        ArrayList<Prediction> predictions = new ArrayList<>();
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        if (routeIds.length == 0) {
            predictions.addAll(makeQuery(realTimeApiClient, "0,1"));
            predictions.addAll(makeQuery(realTimeApiClient, "2,3,4"));
        } else {
            predictions.addAll(makeQuery(realTimeApiClient, routeIds));
        }

        return predictions.toArray(new Prediction[0]);
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null) {
            onPostExecuteListener.onSuccess(predictions, true);
        } else {
            onPostExecuteListener.onError();
        }
    }

    private ArrayList<Prediction> makeQuery(RealTimeApiClient client, String routeTypes) {
        String[] predictionsArgs = {
                "fields[prediction]=stop_sequence,arrival_time,departure_time,schedule_relationship",
                "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                "fields[trip]=direction_id,headsign,name",
                "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status",
                "fields[schedule]=pickup_type,arrival_time,departure_time",
                "fields[stop]=name,latitude,longitude,wheelchair_boarding,platform_code",
                "filter[latitude]=" + latitude,
                "filter[longitude]=" + longitude,
                "filter[radius]=" + radius,
                "filter[route_type]=" + routeTypes,
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = client.get("predictions", predictionsArgs);

        if (jsonResponse != null) {
            return new ArrayList<>(Arrays.asList(PredictionsJsonParser.parse(jsonResponse)));
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<Prediction> makeQuery(RealTimeApiClient client, String[] routeIds) {
        StringBuilder routeArgBuilder = new StringBuilder();

        for (String routeId : routeIds) {
            routeArgBuilder.append(routeId).append(",");
        }

        String[] predictionsArgs = {
                "fields[prediction]=stop_sequence,arrival_time,departure_time,schedule_relationship",
                "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                "fields[trip]=direction_id,headsign,name",
                "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status",
                "fields[schedule]=pickup_type,arrival_time,departure_time",
                "fields[stop]=name,latitude,longitude,wheelchair_boarding,platform_code",
                "filter[latitude]=" + latitude,
                "filter[longitude]=" + longitude,
                "filter[radius]=" + radius,
                "filter[route]=" + routeArgBuilder.toString(),
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = client.get("predictions", predictionsArgs);

        if (jsonResponse != null) {
            return new ArrayList<>(Arrays.asList(PredictionsJsonParser.parse(jsonResponse)));
        } else {
            return new ArrayList<>();
        }
    }
}
