package jackwtat.simplembta.asyncTasks;

import android.location.Location;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.clients.RealTimeApiClient;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.CommuterRail;
import jackwtat.simplembta.model.routes.CommuterRailNorthSide;
import jackwtat.simplembta.model.routes.CommuterRailOldColony;
import jackwtat.simplembta.model.routes.CommuterRailSouthSide;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.utilities.DateUtil;
import jackwtat.simplembta.jsonParsers.PredictionsJsonParser;
import jackwtat.simplembta.jsonParsers.RoutesJsonParser;
import jackwtat.simplembta.jsonParsers.SchedulesJsonParser;
import jackwtat.simplembta.jsonParsers.ServiceAlertsJsonParser;
import jackwtat.simplembta.jsonParsers.StopsJsonParser;

public class MapSearchPredictionsAsyncTask extends AsyncTask<Void, Void, List<Route>> {
    private String realTimeApiKey;

    public final Location targetLocation;
    public final OnPostExecuteListener onPostExecuteListener;

    private HashMap<String, Route> routes;
    private HashMap<String, Stop> stops;
    private HashMap<String, Prediction> predictions = new HashMap<>();

    public MapSearchPredictionsAsyncTask(String realTimeApiKey,
                                         Location targetLocation,
                                         OnPostExecuteListener OnPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.targetLocation = targetLocation;
        this.onPostExecuteListener = OnPostExecuteListener;
    }

    @Override
    protected List<Route> doInBackground(Void... voids) {
        RealTimeApiClient realTimeApiClient = new RealTimeApiClient(realTimeApiKey);

        // Get the stops near the user
        String[] stopArgs = {
                "filter[latitude]=" + Double.toString(targetLocation.getLatitude()),
                "filter[longitude]=" + Double.toString(targetLocation.getLongitude()),
                "include=child_stops"
        };

        stops = StopsJsonParser.parse(realTimeApiClient.get("stops", stopArgs));

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
                "filter[latitude]=" + Double.toString(targetLocation.getLatitude()),
                "filter[longitude]=" + Double.toString(targetLocation.getLongitude()),
                "include=route,trip,stop,schedule"
        };

        processPredictions(PredictionsJsonParser
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

        if (routeArgBuilder.length() > 0) {
            String[] scheduleArgs = {
                    "filter[route]=" + routeArgBuilder.toString(),
                    "filter[stop]=" + stopArgBuilder.toString(),
                    "filter[date]=" + DateUtil.getCurrentMbtaDate(),
                    "filter[min_time]=" + DateUtil.getMbtaTime(0),
                    "filter[max_time]=" + DateUtil.getMbtaTime(6),
                    "include=route,trip,stop,prediction"
            };

            processPredictions(SchedulesJsonParser
                    .parse(realTimeApiClient.get("schedules", scheduleArgs)));
        }

        // Get all alerts for these routes
        routeArgBuilder = new StringBuilder();
        for (Route route : routes.values()) {
            routeArgBuilder.append(route.getId()).append(",");
        }

        if (routeArgBuilder.length() > 0) {
            String[] alertsArgs = {"filter[route]=" + routeArgBuilder.toString()};

            ServiceAlert[] alerts = ServiceAlertsJsonParser
                    .parse(realTimeApiClient.get("alerts", alertsArgs));

            for (ServiceAlert alert : alerts) {
                for (Route route : routes.values()) {
                    if (alert.affectsMode(route.getMode())) {
                        route.addServiceAlert(alert);
                    } else {
                        for (String affectedRouteId : alert.getAffectedRoutes()) {
                            if (route.equals(affectedRouteId)) {
                                route.addServiceAlert(alert);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(routes.values());
    }

    @Override
    protected void onPostExecute(List<Route> routes) {
        onPostExecuteListener.onPostExecute(routes);
    }

    private void processPredictions(Prediction[] livePredictions) {
        for (Prediction prediction : livePredictions) {
            if (!predictions.containsKey(prediction.getId())) {
                predictions.put(prediction.getId(), prediction);

                // Replace prediction's stop ID with its parent stop ID
                if (stops.containsKey(prediction.getParentStopId())) {
                    prediction.setStop(stops.get(prediction.getParentStopId()));
                }

                // If the prediction is for the eastbound Green Line, then replace the route
                // with the Green Line Grouped route. This is to reduce the maximum number of
                // prediction cards displayed and reduces UI clutter.
                if (prediction.getRoute().getMode() == Route.LIGHT_RAIL &&
                        prediction.getDirection() == Direction.EASTBOUND &&
                        GreenLine.isGreenLineSubwayStop(prediction.getStopId())) {
                    prediction.setRoute(new GreenLineCombined());
                }

                // If the prediction is for the inbound Commuter Rail, then replace the route
                // with the Commuter Rail Grouped route. This is to reduce the maximum number
                // of prediction cards displayed and reduces UI clutter.
                if (prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                        prediction.getDirection() == Direction.INBOUND &&
                        CommuterRail.isCommuterRailHub(prediction.getStopId(), false)) {

                    if (CommuterRailNorthSide.isNorthSideCommuterRail(prediction.getRoute().getId())) {
                        prediction.setRoute(new CommuterRailNorthSide());

                    } else if (CommuterRailSouthSide.isSouthSideCommuterRail(prediction.getRoute().getId())) {
                        prediction.setRoute(new CommuterRailSouthSide());

                    } else if (CommuterRailOldColony.isOldColonyCommuterRail(prediction.getRoute().getId())) {
                        prediction.setRoute(new CommuterRailOldColony());
                    }
                }

                // Add route to routes list if not already there
                if (!routes.containsKey(prediction.getRouteId())) {
                    routes.put(prediction.getRouteId(), prediction.getRoute());
                }

                // Add stop to stops list if not already there
                if (!stops.containsKey(prediction.getStopId())) {
                    stops.put(prediction.getStopId(), prediction.getStop());
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
                } else if (stop.getLocation().distanceTo(targetLocation) <
                        routes.get(routeId).getNearestStop(direction).getLocation()
                                .distanceTo(targetLocation)
                        && prediction.willPickUpPassengers()) {
                    routes.get(routeId).setNearestStop(direction, stop);
                    routes.get(routeId).addPrediction(prediction);
                }
            }
        }
    }

    public interface OnPostExecuteListener {
        void onPostExecute(List<Route> routes);
    }
}
