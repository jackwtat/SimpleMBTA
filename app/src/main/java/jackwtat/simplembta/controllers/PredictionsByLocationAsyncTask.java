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

        RealTimeApiClient realTimeClient = new RealTimeApiClient(realTimeApiKey);

        // Get the stops near the user
        String[] stopByLocationArgs = {
                "filter[latitude]=" + Double.toString(lat),
                "filter[longitude]=" + Double.toString(lon),
                "include=child_stops"
        };

        for (Stop stop : StopsJsonParser.parse(realTimeClient.get("stops", stopByLocationArgs))) {
            stop.setDistance(lat, lon);
            stops.put(stop.getId(), stop);
        }

        if (stops.size() == 0) {
            return new ArrayList<>(routes.values());
        }

        // Get all the routes at these stops
        StringBuilder routesByStopArgBuilder = new StringBuilder();
        for (Stop stop : stops.values()) {
            routesByStopArgBuilder.append(stop.getId()).append(",");
        }
        String[] routesByStopArgs = {"filter[stop]=" + routesByStopArgBuilder.toString()};

        for (Route route : RoutesJsonParser.parse(realTimeClient.get("routes", routesByStopArgs))) {
            routes.put(route.getId(), route);
        }

        if (routes.size() == 0) {
            return new ArrayList<>(routes.values());
        }

        // Get the predictions near the user
        String[] predictionByLocationArgs = {
                "filter[latitude]=" + Double.toString(lat),
                "filter[longitude]=" + Double.toString(lon),
                "include=route,trip"
        };
        String predictionsJsonResponse = realTimeClient.get("predictions", predictionByLocationArgs);

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
            } else if (prediction.getRoute() != null) {
                String routeId = prediction.getRouteId();
                Route route = prediction.getRoute();

                route.addPrediction(prediction);
                routes.put(routeId, route);
            }
        }

        // Get all alerts for these routes
        StringBuilder alertsByRouteArgBuilder = new StringBuilder();
        for (Route route : routes.values()) {
            alertsByRouteArgBuilder.append(route.getId()).append(",");
        }
        String[] alertsByRouteArgs = {"filter[route]=" + alertsByRouteArgBuilder.toString()};

        ServiceAlert[] alerts = ServiceAlertsJsonParser.parse(realTimeClient.get("alerts", alertsByRouteArgs));

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
