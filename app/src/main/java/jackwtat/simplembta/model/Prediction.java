package jackwtat.simplembta.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

import jackwtat.simplembta.utilities.DateUtil;

public class Prediction implements Comparable<Prediction>, Serializable {

    // Pick up types
    public static final int UNKNOWN_PICK_UP = -1;
    public static final int SCHEDULED_PICK_UP = 0;
    public static final int NO_PICK_UP = 1;
    public static final int PHONE_PICK_UP = 2;
    public static final int FLAG_PICK_UP = 3;

    // Status types
    public static final int UNKNOWN_STATUS = 0;
    public static final int ADDED = 1;
    public static final int CANCELLED = 2;
    public static final int NO_DATA = 3;
    public static final int SKIPPED = 4;
    public static final int UNSCHEDULED = 5;
    public static final int SCHEDULED = 6;

    // Prediction types
    public static final int ARRIVAL = 0;
    public static final int DEPARTURE = 1;

    // Sort methods
    public static final int PREDICTION_TIME = 0;
    public static final int STOP_SEQUENCE = 1;

    // Prediction data
    private String id;
    private int stopSequence = -1;
    private String trackNumber = "null";
    private Date arrivalTime = null;
    private Date departureTime = null;
    private int timeZoneOffset = -4;
    private boolean isLive = false;
    private int pickUpType = UNKNOWN_PICK_UP;
    private int status = UNKNOWN_STATUS;

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
    private Vehicle vehicle = null;

    // Meta data
    private int sortMethod = PREDICTION_TIME;

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

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public long getCountdownTime() {
        Date currentTime = DateUtil.getTimeZoneAdjustedDate(timeZoneOffset);

        if (arrivalTime != null) {
            return arrivalTime.getTime() - currentTime.getTime();
        } else if (departureTime != null) {
            return departureTime.getTime() - currentTime.getTime();
        } else {
            return -99999999;
        }
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public int getPredictionDay() {
        return DateUtil.getDayOfMonth(getPredictionTime());
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

    public int getStatus() {
        return status;
    }

    public int getPredictionType() {
        if (departureTime == null) {
            return ARRIVAL;
        } else {
            return DEPARTURE;
        }
    }

    public boolean isLive() {
        return isLive;
    }

    public boolean willPickUpPassengers() {
        return pickUpType != NO_PICK_UP && departureTime != null &&
                status != Prediction.SKIPPED && status != Prediction.CANCELLED;
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

    // Vehicle data getters
    public String getVehicleId() {
        return vehicleId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    // Meta data getters
    public int getSortMethod() {
        return sortMethod;
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

    public void setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
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

    public void setStatus(int status) {
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

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    // Meta data setters
    public void setSortMethod(int sortMethod) {
        this.sortMethod = sortMethod;
    }

    @Override
    public int compareTo(@NonNull Prediction otherPred) {
        if (sortMethod == STOP_SEQUENCE) {
            return stopSequence - otherPred.stopSequence;

        } else {
            Date otherTime = otherPred.getPredictionTime();

            if (getPredictionTime() == null && otherTime == null) {
                return 0;
            } else if (getPredictionTime() == null) {
                return 1;
            } else if (otherTime == null) {
                return -1;
            } else {
                return (int) (getCountdownTime() - otherPred.getCountdownTime());
            }
        }
    }
}
