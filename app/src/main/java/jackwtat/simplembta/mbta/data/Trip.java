package jackwtat.simplembta.mbta.data;

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

    public Trip (String id, Route route, int direction, String destination, String stopId, String stopName, long arrivalTime){
        this.id = id;
        this.route = route;
        this.direction = direction;
        this.destination = destination;
        this.stopId = stopId;
        this.stopName = stopName;
        this.arrivalTime = arrivalTime;
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

    public ArrayList<ServiceAlert> getAlerts() { return route.getServiceAlerts(); }

    public void setAlerts( ArrayList<ServiceAlert> serviceAlerts) { route.setServiceAlerts(serviceAlerts); }

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

    public boolean hasServiceAlert(){
        return route.getServiceAlerts() != null && route.getServiceAlerts().size() > 0;
    }

    public boolean hasHighUrgencyServiceAlert(){
        for (ServiceAlert alert : route.getServiceAlerts()){
            if (alert.getUrgency() == ServiceAlert.Urgency.ALERT) {
                return true;
            }
        }

        return false;
    }
}
