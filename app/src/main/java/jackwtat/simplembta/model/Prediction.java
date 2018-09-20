package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.util.Date;

public class Prediction implements Comparable<Prediction> {
    public static final int SCHEDULED_PICK_UP = 0;
    public static final int NO_PICK_UP = 1;
    public static final int PHONE_PICK_UP = 2;
    public static final int FLAG_PICK_UP = 3;

    // Prediction data
    private String id;
    private String trackNumber = "null";
    private Date arrivalTime = null;
    private Date departureTime = null;
    private boolean isLive = false;
    private int pickUpType = SCHEDULED_PICK_UP;

    // Route data
    private Route route = null;

    // Stop data
    private Stop stop = null;

    // Trip data
    private String tripId = "null";
    private int direction = Route.NULL_DIRECTION;
    private String destination = "null";
    private String tripName = "null";

    public Prediction(String id) {
        this.id = id;
    }

    // Prediction data getters
    public String getId() {
        return id;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public long getTimeUntilArrival() {
        if (arrivalTime != null) {
            return arrivalTime.getTime() - new Date().getTime();
        } else {
            return -1;
        }
    }

    public long getTimeUntilDeparture() {
        if (departureTime != null) {
            return departureTime.getTime() - new Date().getTime();
        } else {
            return -1;
        }
    }

    public String getRouteId() {
        return route.getId();
    }

    public Route getRoute() {
        return route;
    }

    public int getPickUpType() {
        return pickUpType;
    }

    public boolean isLive() {
        return isLive;
    }

    public boolean willPickUpPassengers() {
        return pickUpType != NO_PICK_UP && (arrivalTime != null || departureTime != null);
    }

    // Stop data getters
    public String getStopId() {
        return stop.getId();
    }

    public Stop getStop() {
        return stop;
    }

    // Trip data getters
    public String getTripId() {
        return tripId;
    }

    public int getDirection() {
        return direction;
    }

    public String getDestination() {
        return destination;
    }

    public String getTripName() {
        return tripName;
    }

    // Prediction data setters
    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setIsLive(boolean isLive) {
        this.isLive = isLive;
    }

    public void setPickUpType(int pickUpType) {
        this.pickUpType = pickUpType;
    }

    // Stop data setters
    public void setStop(Stop stop) {
        this.stop = stop;
    }

    // Trip data setters
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    @Override
    public int compareTo(@NonNull Prediction otherPred) {
        Date otherDeparture = otherPred.getDepartureTime();
        if (departureTime == null && otherDeparture == null) {
            return 0;
        } else if (departureTime == null) {
            return 1;
        } else if (otherDeparture == null) {
            return -1;
        } else {
            return this.departureTime.compareTo(otherDeparture);
        }
    }
}
