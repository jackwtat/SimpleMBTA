package jackwtat.simplembta.asyncTasks;

import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
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

public class MapSearchPredictionsAsyncTask extends AsyncTask<Void, Void, List<Route>> {
    private String realTimeApiKey;
    private Location location;
    private Callbacks callbacks;

    private HashMap<String, Route> routes;
    private HashMap<String, Stop> stops;
    private ArrayList<String> predictionIds = new ArrayList<>();

    public MapSearchPredictionsAsyncTask(String realTimeApiKey,
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
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        // Get the stops near the user
        String[] stopArgs = {
                "filter[latitude]=" + Double.toString(location.getLatitude()),
                "filter[longitude]=" + Double.toString(location.getLongitude()),
                "include=child_stops"
        };

        stops = StopsJsonParser.parse(realTimeApiClient.get("stops", stopArgs));

        for (Stop stop : stops.values()) {
            stop.setDistanceFromOrigin(location);
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

        routes = RoutesJsonParser.parse(realTimeApiClient.get("routes", routesArgs));

        if (routes.size() == 0) {
            return new ArrayList<>();
        }

        // Get live predictions near the user
        String[] predictionsArgs = {
                "filter[latitude]=" + Double.toString(location.getLatitude()),
                "filter[longitude]=" + Double.toString(location.getLongitude()),
                "include=route,trip,stop,schedule"
        };

        processLivePredictions(PredictionsJsonParser
                .parse(realTimeApiClient.get("predictions", predictionsArgs)));

        // Get non-live schedule data for inbound routes
        StringBuilder routeArgBuilder;
        routeArgBuilder = new StringBuilder();

        for (Route route : routes.values()) {
            // We'll only query routes that don't already have live pick-ups in this direction
            // Light rail and heavy rail (Green, Red, Blue, and Orange Lines) on-time performances
            // are too erratic and unreliable for scheduled predictions to be reliable
            if (route.getMode() != Route.LIGHT_RAIL && route.getMode() != Route.HEAVY_RAIL) {
                routeArgBuilder.append(route.getId()).append(",");
            }
        }

        String[] scheduleArgs = {
                "filter[route]=" + routeArgBuilder.toString(),
                "filter[stop]=" + stopArgBuilder.toString(),
                "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                "filter[min_time]=" + DateUtil.getMbtaTime(0),
                "include=route,trip,stop,prediction"
        };

        processScheduledPredictions(SchedulesJsonParser
                .parse(realTimeApiClient.get("schedules", scheduleArgs)));

        // Get all alerts for these routes
        routeArgBuilder = new StringBuilder();
        for (Route route : routes.values()) {
            routeArgBuilder.append(route.getId()).append(",");
        }

        String[] alertsArgs = {"filter[route]=" + routeArgBuilder.toString()};

        ServiceAlert[] alerts = ServiceAlertsJsonParser
                .parse(realTimeApiClient.get("alerts", alertsArgs));

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

    private void processLivePredictions(Prediction[] livePredictions) {
        for (Prediction prediction : livePredictions) {
            if (!predictionIds.contains(prediction.getId())) {
                predictionIds.add(prediction.getId());

                // Add route to routes list if not already there
                if (!routes.containsKey(prediction.getRouteId())) {
                    routes.put(prediction.getRouteId(), prediction.getRoute());
                }

                // Add stop to stops list if not already there
                if (!stops.containsKey(prediction.getStopId())) {
                    prediction.getStop().setDistanceFromOrigin(location);
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
            }
        }
    }

    private void processScheduledPredictions(Prediction[] scheduledPredictions) {
        for (Prediction prediction : scheduledPredictions) {
            if (!predictionIds.contains(prediction.getId())) {
                predictionIds.add(prediction.getId());

                // Add route to routes list if not already there
                if (!routes.containsKey(prediction.getRouteId())) {
                    routes.put(prediction.getRouteId(), prediction.getRoute());
                }

                // Add stop to stops list if not already there
                if (!stops.containsKey(prediction.getStopId())) {
                    prediction.getStop().setDistanceFromOrigin(location);
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
            }
        }
    }
}
