package jackwtat.simplembta.mbta.structure;

import android.support.annotation.NonNull;

import java.util.Date;

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
}