package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jackw on 12/11/2017.
 */

public class Prediction implements Comparable<Prediction> {

    private String id = "";
    private String stopId = "";
    private String stopName = "";
    private String trackNumber = "null";
    private Route route = null;
    private Trip trip = null;
    private Date departureTime = null;
    private long timeUntilDeparture = 0;

    public Prediction(String id, String stopId, String stopName, String trackNumber, Route route,
                      Trip trip, Date departureTime) {
        this.id = id;
        this.stopId = stopId;
        this.stopName = stopName;
        this.trackNumber = trackNumber;
        this.route = route;
        this.trip = trip;
        this.departureTime = departureTime;
        if (departureTime != null) {
            this.timeUntilDeparture = departureTime.getTime() - new Date().getTime();
        }
    }

    public String getId() {
        return id;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public Route getRoute() {
        return route;
    }

    public Trip getTrip() {
        return trip;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public long getTimeUntilDeparture() {
        return timeUntilDeparture;
    }

    @Override
    public int compareTo(@NonNull Prediction prediction) {
        if (this.route.getMode() == prediction.route.getMode() &&
                (this.route.getMode() == Mode.LIGHT_RAIL ||
                        this.route.getMode() == Mode.COMMUTER_RAIL)) {
            if (this.trip.compareTo(prediction.getTrip()) == 0) {
                if (this.route.compareTo(prediction.getRoute()) == 0) {
                    if (this.departureTime == null) {
                        return 1;
                    } else if (prediction.getDepartureTime() == null) {
                        return -1;
                    } else {
                        return this.departureTime.compareTo(prediction.getDepartureTime());
                    }
                } else {
                    return this.route.compareTo(prediction.getRoute());
                }
            } else {
                return this.trip.compareTo(prediction.getTrip());
            }
        } else if (this.route.compareTo(prediction.getRoute()) == 0) {
            if (this.trip.compareTo(prediction.getTrip()) == 0) {
                if (this.departureTime == null) {
                    return 1;
                } else if (prediction.getDepartureTime() == null) {
                    return -1;
                } else {
                    return this.departureTime.compareTo(prediction.getDepartureTime());
                }
            } else {
                return this.trip.compareTo(prediction.getTrip());
            }
        } else {
            return this.route.compareTo(prediction.getRoute());
        }
    }

    public static ArrayList<ArrayList<Prediction>> getUniqueSortedPredictions(List<Stop> stops) {
        // Initialize the data structure that stores route-direction combos that have been processed
        ArrayList<String> processedRDs = new ArrayList<>();

        // Initialize HashMap to track predictions for route-directions that have been processed
        HashMap<String, ArrayList<Prediction>> toDisplay = new HashMap<>();

        // Sort the stops
        Collections.sort(stops);

        // Loop through each stop
        for (Stop s : stops) {

            // Sort the predictions
            Collections.sort(s.getPredictions());

            // Loop through each prediction
            for (int i = 0; i < s.getPredictions().size(); i++) {
                Prediction p = s.getPredictions().get(i);

                // Create a unique identifier for the route-direction combo by concatenating
                // the direction ID and the route ID
                String rd = p.getTrip().getDirection() + ":" + p.getRoute().getId();

                // If this route-direction has been processed with a prediction with null departure
                // and this prediction does not have a null departure, then we want to remove the
                // processed prediction and replace it with this one
                if (processedRDs.contains(rd) &&
                        toDisplay.get(rd).get(0).getDepartureTime() == null &&
                        p.getDepartureTime() != null) {
                    processedRDs.remove(rd);
                    toDisplay.remove(rd);
                }

                // If this route-direction has not processed or has been previously unprocessed,
                // then okay to process
                if (!processedRDs.contains(rd)) {
                    processedRDs.add(rd);

                    toDisplay.put(rd, new ArrayList<Prediction>());

                    toDisplay.get(rd).add(p);

                    // Add the next predictions, too, if same route-direction
                    for (int j = i + 1;
                         j < s.getPredictions().size() &&
                                 (s.getPredictions().get(j).getRoute().getId().equals(p.getRoute().getId()) &&
                                         s.getPredictions().get(j).getTrip().getDirection() == p.getTrip().getDirection());
                         j++) {
                        toDisplay.get(rd).add(s.getPredictions().get(j));
                        i = j;
                    }
                }
            }
        }

        ArrayList<ArrayList<Prediction>> sortedPredictions = new ArrayList<>();
        for (String rd : processedRDs) {
            sortedPredictions.add(toDisplay.get(rd));
        }

        return sortedPredictions;
    }
}