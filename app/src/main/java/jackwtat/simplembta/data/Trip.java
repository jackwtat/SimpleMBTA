package jackwtat.simplembta.data;

import java.util.ArrayList;

/**
 * Created by jackw on 8/26/2017.
 */

public class Trip {
    private String id;
    private Route route;
    private int direction;
    private String destination;
    private String stopId;
    private String stopName;
    private long arrivalTime;

    public Trip(String id) {
        this.id = id;
        route = null;
        direction = Route.Direction.UNKNOWN;
        destination = "";
        stopId = "";
        stopName = "";
        arrivalTime = -1;
    }

    public String getId() {
        return id;
    }

    public void setRoute(Route route) { this.route = route;}

    public Route getRoute() { return route; }

    public String getRouteId() {
        return route.getId();
    }

    public String getRouteName() {
        return route.getName();
    }

    public String getRouteLongName() { return route.getLongName(); }

    public int getMode() {
        return route.getMode();
    }

    public ArrayList<Alert> getAlerts() { return route.getAlerts(); }

    public void setAlerts( ArrayList<Alert> alerts) { route.setAlerts(alerts); }

    public String getStopId() { return stopId; }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
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

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) { this.arrivalTime = arrivalTime; }

    public boolean hasAlerts(){
        if (route.getAlerts() != null && route.getAlerts().size() > 0){
            return true;
        } else {
            return false;
        }
    }
}
