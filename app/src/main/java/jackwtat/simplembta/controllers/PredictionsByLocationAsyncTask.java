package jackwtat.simplembta.controllers;

import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.clients.MbtaApiClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.utilities.PredictionsJsonParser;
import jackwtat.simplembta.utilities.RoutesJsonParser;
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

        MbtaApiClient query = new MbtaApiClient(realTimeApiKey);

        // Get the stops near the user
        String[] stopArgs = {
                "filter[latitude]=" + Double.toString(lat),
                "filter[longitude]=" + Double.toString(lon),
                "include=child_stops"
        };
        String stopsJsonResponse = query.get("stops", stopArgs);
        for (Stop stop : StopsJsonParser.parse(stopsJsonResponse)) {
            stop.setDistance(lat, lon);
            stops.put(stop.getId(), stop);
        }

        if (stops.size() == 0) {
            return new ArrayList<>(routes.values());
        }

        // Get all the routes at these stops
        StringBuilder routesArgBuilder = new StringBuilder();
        for (Stop stop : stops.values()) {
            routesArgBuilder.append(stop.getId()).append(",");
        }
        String[] routesArgs = {"filter[stop]=" + routesArgBuilder.toString()};
        String routesJsonResponse = query.get("routes", routesArgs);
        for (Route route : RoutesJsonParser.parse(routesJsonResponse)) {
            routes.put(route.getId(), route);
        }

        if (routes.size() == 0) {
            return new ArrayList<>(routes.values());
        }

        // Get all alerts for these routes
        StringBuilder alertsArgBuilder = new StringBuilder();
        for (Route route : routes.values()) {
            alertsArgBuilder.append(route.getId()).append(",");
        }
        String[] alertsArgs = {"filter[route]=" + alertsArgBuilder.toString()};
        String alertsJsonResponse = query.get("alerts", alertsArgs);

        ServiceAlert[] alerts = ServiceAlertsJsonParser.parse(alertsJsonResponse);
        for (ServiceAlert alert : alerts) {
            for (Route route : routes.values()) {
                if (alert.getAffectedRoutes().contains(route.getId()) ||
                        alert.isAffectedMode(route.getMode())) {
                    route.addServiceAlert(alert);
                }
            }
        }

        // Get the predictions near the user
        String[] predictionArgs = {
                "filter[latitude]=" + Double.toString(lat),
                "filter[longitude]=" + Double.toString(lon),
                "include=route,trip"
        };
        String predictionsJsonResponse = query.get("predictions", predictionArgs);

        ArrayList<Prediction> predictions = new ArrayList<>(
                Arrays.asList(PredictionsJsonParser.parse(predictionsJsonResponse)));

        Collections.sort(predictions);

        for (Prediction prediction : predictions) {
            // Replace prediction's stop ID with its parent stop ID
            for (Stop stop : stops.values()) {
                if (stop.isParentOf(prediction.getStopId())) {
                    prediction.setStopId(stop.getId());
                    break;
                }
            }

            // Add prediction to its respective route
            if (stops.containsKey(prediction.getStopId()) && routes.containsKey(prediction.getRouteId())) {
                int direction = prediction.getDirection();
                String routeId = prediction.getRouteId();
                Stop stop = stops.get(prediction.getStopId());

                if (prediction.getDepartureTime() != null) {
                    if (stop.equals(routes.get(routeId).getNearestStop(direction))) {
                        routes.get(routeId).addPrediction(prediction);
                    } else if (routes.get(routeId).getNearestStop(direction) == null ||
                            stop.compareTo(routes.get(routeId).getNearestStop(direction)) < 0) {
                        routes.get(routeId).setNearestStop(direction, stop);
                        routes.get(routeId).addPrediction(prediction);
                    }
                } else if (prediction.getArrivalTime() == null) {
                    if (routes.get(routeId).getNearestStop(direction) == null ||
                            (routes.get(routeId).getPredictions(direction).size() == 0 &&
                                    routes.get(routeId).getNearestStop(direction).compareTo(stop) > 0)) {
                        routes.get(routeId).setNearestStop(direction, stop);
                    }
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
