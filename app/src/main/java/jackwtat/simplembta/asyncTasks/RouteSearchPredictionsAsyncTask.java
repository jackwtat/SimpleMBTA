package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.DateUtil;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;

public class RouteSearchPredictionsAsyncTask extends AsyncTask<Void, Void, List<Prediction>> {
    private String realTimeApiKey;
    private OnPostExecuteListener onPostExecuteListener;

    private Route route;
    private int directionId;

    private HashMap<String, Prediction> predictions = new HashMap<>();

    public RouteSearchPredictionsAsyncTask(String realTimeApiKey,
                                           Route route,
                                           int directionId,
                                           OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.route = route;
        this.directionId = directionId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected List<Prediction> doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        // Get live predictions
        String[] predictionsArgs = {
                "fields[prediction]=stop_sequence,arrival_time,departure_time,schedule_relationship",
                "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                "fields[trip]=direction_id,headsign,name",
                "fields[vehicle]=label,direction_id,latitude,longitude,bearing,current_stop_sequence,current_status",
                "fields[schedule]=pickup_type,arrival_time,departure_time",
                "filter[route]=" + route.getId(),
                "filter[direction_id]=" + directionId,
                "filter[stop]=" + route.getNearestStop(directionId).getId(),
                "include=route,trip,stop,schedule,vehicle"
        };

        String jsonResponse = realTimeApiClient.get("predictions", predictionsArgs);

        if (jsonResponse != null) {
            for (Prediction p : PredictionsJsonParser.parse(jsonResponse)) {
                if (predictions.get(getHash(p)) == null || p.willPickUpPassengers()) {
                    predictions.put(getHash(p), p);
                }
            }
        } else {
            return null;
        }

        // Get today's scheduled predictions
        if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
            String[] scheduleArgs = {
                    "fields[schedule]=stop_sequence,arrival_time,departure_time,pickup_type",
                    "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                    "fields[trip]=direction_id,headsign,name",
                    "fields[stop]=name,latitude,longitude,wheelchair_boarding",
                    "fields[prediction]=stop_sequence",
                    "filter[route]=" + route.getId(),
                    "filter[direction_id]=" + directionId,
                    "filter[stop]=" + route.getNearestStop(directionId).getId(),
                    "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                    "filter[min_time]=" + DateUtil.getMbtaTime(0),
                    "include=route,trip,stop,prediction"
            };

            jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

            if (jsonResponse != null) {
                for (Prediction p : SchedulesJsonParser.parse(jsonResponse)) {
                    Prediction sameTrip = predictions.get(getHash(p));

                    if (sameTrip == null ||
                            (p.willPickUpPassengers() &&
                                    !sameTrip.isLive() &&
                                    sameTrip.getStatus() != Prediction.CANCELLED &&
                                    sameTrip.getStatus() != Prediction.SKIPPED)) {
                        predictions.put(getHash(p), p);
                    }
                }
            }
        }

        // Get the next day's scheduled predictions
        if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
            String[] scheduleArgs = {
                    "fields[schedule]=stop_sequence,arrival_time,departure_time,pickup_type",
                    "fields[route]=type,sort_order,short_name,long_name,color,text_color,direction_names",
                    "fields[trip]=direction_id,headsign,name",
                    "fields[stop]=name,latitude,longitude,wheelchair_boarding",
                    "fields[prediction]=stop_sequence",
                    "filter[route]=" + route.getId(),
                    "filter[direction_id]=" + directionId,
                    "filter[stop]=" + route.getNearestStop(directionId).getId(),
                    "filter[date]=" + DateUtil.getMbtaDate(1),
                    "filter[min_time]=" + "03:00",
                    "include=route,trip,stop,prediction"
            };

            jsonResponse = realTimeApiClient.get("schedules", scheduleArgs);

            if (jsonResponse != null) {
                for (Prediction p : SchedulesJsonParser.parse(jsonResponse)) {
                    Prediction sameTrip = predictions.get(getHash(p));

                    if (sameTrip == null ||
                            (p.willPickUpPassengers() &&
                                    !sameTrip.isLive() &&
                                    sameTrip.getStatus() != Prediction.CANCELLED &&
                                    sameTrip.getStatus() != Prediction.SKIPPED)) {
                        predictions.put(getHash(p), p);
                    }
                }
            }
        }

        return new ArrayList<>(predictions.values());
    }

    @Override
    protected void onPostExecute(List<Prediction> predictions) {
        if (predictions != null) {
            onPostExecuteListener.onSuccess(predictions);
        } else {
            onPostExecuteListener.onError();
        }
    }

    private String getHash(Prediction p) {
        if (p.getPredictionTime() != null) {
            return p.getPredictionDay() + "," + p.getTripId();
        } else {
            return "0," + p.getTripId();
        }
    }

    public interface OnPostExecuteListener {
        void onSuccess(List<Prediction> predictions);

        void onError();
    }
}
