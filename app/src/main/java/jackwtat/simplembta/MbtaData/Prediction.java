package jackwtat.simplembta.MbtaData;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 8/26/2017.
 */

public class Prediction implements Comparable{
    private String tripId;
    private String routeId;
    private String routeName;
    private String destination;
    private String stopId;
    private String stopName;
    private int direction;
    private long arrivalTime;
    private long queryTime;

    public Prediction(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }

    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getStopId() { return stopId; }

    public void setStopId(String stopId) { this.stopId = stopId; }

    public String getStopName() { return stopName; }

    public void setStopName(String stopName) { this.stopName = stopName; }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        // TODO: Implement compareTo
        return 0;
    }
}
