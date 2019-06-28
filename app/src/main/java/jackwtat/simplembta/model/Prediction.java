package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.GreenLineCombined;
import jackwtat.simplembta.model.routes.Route;

public class Prediction implements Comparable<Prediction>, Serializable {

    // Pick up types
    public static final int UNKNOWN_PICK_UP = -1;
    public static final int SCHEDULED_PICK_UP = 0;
    public static final int NO_PICK_UP = 1;
    public static final int PHONE_PICK_UP = 2;
    public static final int FLAG_PICK_UP = 3;

    // Status types
    public static final String UNKNOWN_STATUS = "UNKNOWN_STATUS";
    public static final String ADDED = "ADDED";
    public static final String CANCELLED = "CANCELLED";
    public static final String NO_DATA = "NO_DATA";
    public static final String SKIPPED = "SKIPPED";
    public static final String UNSCHEDULED = "UNSCHEDULED";

    // Prediction types
    public static final int ARRIVAL = 0;
    public static final int DEPARTURE = 1;

    // Prediction data
    private String id;
    private int stopSequence = -1;
    private String trackNumber = "null";
    private Date arrivalTime = null;
    private Date departureTime = null;
    private boolean isLive = false;
    private int pickUpType = UNKNOWN_PICK_UP;
    private String status = UNKNOWN_STATUS;

    // Route data
    private Route route = null;

    // Stop data
    private Stop stop = null;

    // Trip data
    private String tripId = "null";
    private int direction = Direction.NULL_DIRECTION;
    private String destination = "null";
    private String tripName = "null";

    // Vehicle data
    private String vehicleId = null;

    public Prediction(String id) {
        this.id = id;
    }

    // Prediction data getters
    public String getId() {
        return id;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public Date getPredictionTime() {
        if (arrivalTime != null) {
            return arrivalTime;
        } else {
            return departureTime;
        }
    }

    public long getCountdownTime() {
        if (arrivalTime != null) {
            return arrivalTime.getTime() - new Date().getTime();
        } else if (departureTime != null) {
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

    public String getStatus() {
        return status;
    }

    public int getPredictionType() {
        if (arrivalTime != null && departureTime == null) {
            return ARRIVAL;
        } else if (arrivalTime == null && departureTime != null) {
            return DEPARTURE;
        } else {
            return ARRIVAL;
        }
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

    public String getParentStopId() {
        return stop.getParentId();
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

    // Vehicle data gettings
    public String getVehicleId() {
        return vehicleId;
    }

    // Prediction data setters
    public void setId(String id) {
        this.id = id;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

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

    public void setStatus(String status) {
        this.status = status;
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

    // Vehicle data setters
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Override
    public int compareTo(@NonNull Prediction otherPred) {
        Date otherTime = otherPred.getPredictionTime();

        if (getPredictionTime() == null && otherTime == null) {
            return 0;
        } else if (getPredictionTime() == null) {
            return 1;
        } else if (otherTime == null) {
            return -1;
        } else {
            return getPredictionTime().compareTo(otherTime);
        }
    }
}
