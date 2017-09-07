package jackwtat.simplembta;

import static android.R.attr.mode;

/**
 * Created by jackw on 8/26/2017.
 */

public class Prediction {
    private String tripId;
    private String routeId;
    private String destination;
    private int direction;
    private long predictedArrivalTime;
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

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

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

    public long getPredictedArrivalTime() {
        return predictedArrivalTime;
    }

    public void setPredictedArrivalTime(long predictedArrivalTime) {
        this.predictedArrivalTime = predictedArrivalTime;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }
}
