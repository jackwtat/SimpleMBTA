package jackwtat.simplembta.controllers;

import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
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

    private HashMap<String, Route> routes;
    private HashMap<String, Stop> stops;
    HashMap<String, Prediction> predictions;

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
        RealTimeApiClient realTimeClient = new RealTimeApiClient(realTimeApiKey);

        // Get the stops near the user
        String[] stopArgs = {
                "filter[latitude]=" + Double.toString(location.getLatitude()),
                "filter[longitude]=" + Double.toString(location.getLongitude()),
                "include=child_stops"
        };

        stops = StopsJsonParser.parse(realTimeClient.get("stops", stopArgs));

        for (Stop stop : stops.values()) {
            stop.setDistance(location.getLatitude(), location.getLongitude());
        }

        if (stops.size() == 0) {
            return new ArrayList<>();
        }

        // Get all the routes at these stops
        StringBuilder stopArgBuilder = new StringBuilder();
        for (Stop stop : stops.values()) {
            stopArgBuilder.append(stop.getId()).append(",");
        }
        String[] routesArgs = {"filter[stop]=" + stopArgBuilder.toString()};

        routes = RoutesJsonParser.parse(realTimeClient.get("routes", routesArgs));

        if (routes.size() == 0) {
            return new ArrayList<>();
        }

        // Get live predictions near the user
        String[] predictionsArgs = {
                "filter[latitude]=" + Double.toString(location.getLatitude()),
                "filter[longitude]=" + Double.toString(location.getLongitude()),
                "include=route,trip,stop,schedule"
        };

        predictions = PredictionsJsonParser
                .parse(realTimeClient.get("predictions", predictionsArgs));
        processLivePredictions(predictions.values());

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
                "filter[max_time]=" + DateUtil.getMbtaTime(4),
                "include=route,trip,stop,prediction"
        };

        processScheduledPredictions(SchedulesJsonParser
                .parse(realTimeClient.get("schedules", scheduleArgs)).values());

        // Get all alerts for these routes
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

    private void processLivePredictions(Collection<Prediction> predictions) {
        for (Prediction prediction : predictions) {
            if (!routes.containsKey(prediction.getRouteId())) {
                routes.put(prediction.getRouteId(), prediction.getRoute());
            }

            if (!stops.containsKey(prediction.getStopId())) {
                prediction.getStop().setDistance(location.getLatitude(), location.getLongitude());
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

            if (prediction.willPickUpPassengers()) {
                // If this prediction's stop is the route's nearest stop
                if (stop.equals(routes.get(routeId).getNearestStop(direction))) {
                    routes.get(routeId).addPrediction(prediction);

                    // If route does not have predictions in this prediction's direction
                } else if (!routes.get(routeId).hasPredictions(direction)) {
                    routes.get(routeId).setNearestStop(direction, stop);
                    routes.get(routeId).addPrediction(prediction);

                    // If this prediction's stop is closer than route's current nearest stop
                } else if (stop.compareTo(routes.get(routeId).getNearestStop(direction)) < 0) {
                    routes.get(routeId).setNearestStop(direction, stop);
                    routes.get(routeId).addPrediction(prediction);
                }
            } else if (routes.get(routeId).getNearestStop(direction) == null ||
                    (!routes.get(routeId).hasPredictions(direction) &&
                            routes.get(routeId).getNearestStop(direction).compareTo(stop) > 0)) {
                routes.get(routeId).setNearestStop(direction, stop);
            }
        }
    }

    private void processScheduledPredictions(Collection<Prediction> scheduledPredictions) {
        for (Prediction prediction : scheduledPredictions) {
            if (!predictions.containsKey(prediction.getId())) {
                predictions.put(prediction.getId(), prediction);

                if (!routes.containsKey(prediction.getRouteId())) {
                    routes.put(prediction.getRouteId(), prediction.getRoute());
                }

                if (!stops.containsKey(prediction.getStopId())) {
                    prediction.getStop().setDistance(location.getLatitude(), location.getLongitude());
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

                if (prediction.willPickUpPassengers()) {
                    // If this prediction's stop is the route's nearest stop
                    if (stop.equals(routes.get(routeId).getNearestStop(direction))) {
                        routes.get(routeId).addPrediction(prediction);

                        // If route does not have predictions in this prediction's direction
                    } else if (!routes.get(routeId).hasPredictions(direction)) {
                        routes.get(routeId).setNearestStop(direction, stop);
                        routes.get(routeId).addPrediction(prediction);

                        // If this prediction's stop is closer than route's current nearest stop
                        // and the route doesn't already have live pick-ups in this direction
                    } else if (stop.compareTo(routes.get(routeId).getNearestStop(direction)) < 0
                            && !routes.get(routeId).hasLivePickUps(direction)) {
                        routes.get(routeId).setNearestStop(direction, stop);
                        routes.get(routeId).addPrediction(prediction);
                    }
                } else if (routes.get(routeId).getNearestStop(direction) == null ||
                        (!routes.get(routeId).hasPredictions(direction) &&
                                routes.get(routeId).getNearestStop(direction).compareTo(stop) > 0)) {
                    routes.get(routeId).setNearestStop(direction, stop);
                }
            }
        }
    }
}
