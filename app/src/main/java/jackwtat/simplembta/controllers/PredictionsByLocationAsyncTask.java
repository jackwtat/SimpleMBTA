package jackwtat.simplembta.controllers;

import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.utilities.DateUtil;
import jackwtat.simplembta.utilities.PredictionsJsonParser;
import jackwtat.simplembta.utilities.RoutesJsonParser;
import jackwtat.simplembta.utilities.SchedulesJsonParser;
import jackwtat.simplembta.utilities.ServiceAlertsJsonParser;
import jackwtat.simplembta.utilities.StopsJsonParser;

public class PredictionsByLocationAsyncTask extends AsyncTask<Void, Void, List<Route>> {
    private String realTimeApiKey;
    private Location location;
    private Callbacks callbacks;

    public PredictionsByLocationAsyncTask(String realTimeApiKey,
                                          Location location,
                                          Callbacks callbacks) {
        this.realTimeApiKey = realTimeApiKey;
        this.location = location;
        this.callbacks = callbacks;
    }

    @Override
    protected void onPreExecute() {
        callbacks.onPreExecute();
    }

    @Override
    protected List<Route> doInBackground(Void... voids) {
        HashMap<String, Stop> stops = new HashMap<>();
        HashMap<String, Route> routes = new HashMap<>();

        Double lat = location.getLatitude();
        Double lon = location.getLongitude();

        RealTimeApiClient realTimeClient = new RealTimeApiClient(realTimeApiKey);

        // Get the stops near the user
        String[] stopArgs = {
                "filter[latitude]=" + Double.toString(lat),
                "filter[longitude]=" + Double.toString(lon),
                "include=child_stops"
        };

        for (Stop stop : StopsJsonParser.parse(realTimeClient.get("stops", stopArgs))) {
            stop.setDistance(lat, lon);
            stops.put(stop.getId(), stop);
        }

        if (stops.size() == 0) {
            return new ArrayList<>(routes.values());
        }

        // Get all the routes at these stops
        StringBuilder stopArgBuilder = new StringBuilder();
        for (Stop stop : stops.values()) {
            stopArgBuilder.append(stop.getId()).append(",");
        }
        String[] routesArgs = {"filter[stop]=" + stopArgBuilder.toString()};

        for (Route route : RoutesJsonParser.parse(realTimeClient.get("routes", routesArgs))) {
            routes.put(route.getId(), route);
        }

        if (routes.size() == 0) {
            return new ArrayList<>(routes.values());
        }

        // Get live predictions near the user
        String[] predictionsArgs = {
                "filter[latitude]=" + Double.toString(lat),
                "filter[longitude]=" + Double.toString(lon),
                "include=route,trip,stop"
        };

        ArrayList<Prediction> predictions = new ArrayList<>(Arrays.asList(
                PredictionsJsonParser.parse(realTimeClient.get("predictions", predictionsArgs))));

        // Get non-live schedule data
        StringBuilder routeArgBuilder = new StringBuilder();
        for (Route route : routes.values()) {
            if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
                routeArgBuilder.append(route.getId()).append(",");
            }
        }

        String[] scheduleArgs = {
                "filter[route]=" + routeArgBuilder.toString(),
                "filter[stop]=" + stopArgBuilder.toString(),
                "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                "filter[min_time]=" + DateUtil.getMbtaTime(0),
                "filter[max_time]=" + DateUtil.getMbtaTime(2),
                "include=route,trip,stop,prediction"
        };

        predictions.addAll(new ArrayList<>(Arrays.asList(
                SchedulesJsonParser.parse(realTimeClient.get("schedules", scheduleArgs)))));

        Collections.sort(predictions);

        for (Prediction prediction : predictions) {
            if (!routes.containsKey(prediction.getRouteId())) {
                routes.put(prediction.getRouteId(), prediction.getRoute());
            }

            if (!stops.containsKey(prediction.getStopId())) {
                prediction.getStop().setDistance(lat, lon);
                stops.put(prediction.getStopId(), prediction.getStop());
            }

            // Replace prediction's stop ID with its parent stop ID
            for (Stop stop : stops.values()) {
                if (stop.isParentOf(prediction.getStopId())) {
                    prediction.setStop(stop);
                    break;
                }
            }

            // Add prediction to its respective route
            int direction = prediction.getDirection();
            String routeId = prediction.getRouteId();
            Stop stop = stops.get(prediction.getStopId());

            if (prediction.getDepartureTime() != null) {
                if (stop.equals(routes.get(routeId).getNearestStop(direction))) {
                    routes.get(routeId).addPrediction(prediction);
                } else if (!routes.get(routeId).hasPredictions(direction) ||
                        (prediction.isLive() && stop.compareTo(routes.get(routeId).getNearestStop(direction)) < 0)) {
                    routes.get(routeId).setNearestStop(direction, stop);
                    routes.get(routeId).addPrediction(prediction);
                }
            } else if (prediction.getArrivalTime() == null) {
                if (routes.get(routeId).getNearestStop(direction) == null ||
                        (!routes.get(routeId).hasPredictions(direction) &&
                                routes.get(routeId).getNearestStop(direction).compareTo(stop) > 0)) {
                    routes.get(routeId).setNearestStop(direction, stop);
                }
            }
        }

        // Get all alerts for these routes
        routeArgBuilder = new StringBuilder();
        for (Route route : routes.values()) {
            routeArgBuilder.append(route.getId()).append(",");
        }

        String[] alertsArgs = {"filter[route]=" + routeArgBuilder.toString()};

        ServiceAlert[] alerts = ServiceAlertsJsonParser.parse(realTimeClient.get("alerts", alertsArgs));

        for (ServiceAlert alert : alerts) {
            for (Route route : routes.values()) {
                if (alert.getAffectedRoutes().contains(route.getId()) ||
                        alert.isAffectedMode(route.getMode())) {
                    route.addServiceAlert(alert);
                }
            }
        }

        return new ArrayList<>(routes.values());
    }

    @Override
    protected void onPostExecute(List<Route> routes) {
        callbacks.onPostExecute(routes);
    }

    public interface Callbacks {
        void onPreExecute();

        void onPostExecute(List<Route> routes);
    }
}
