package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Shape implements Comparable<Shape>, Serializable {
    private String id;
    private String polyline = "";
    private String routeId = "";
    private int direction = 1;
    private int priority = -1;
    private Stop[] stops = new Stop[0];

    public Shape(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPolyline() {
        return polyline;
    }

    public String getRouteId() {
        return routeId;
    }

    public int getDirection() {
        return direction;
    }

    public int getPriority() {
        return priority;
    }

    public Stop[] getStops() {
        return stops;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setStops(Stop[] stops) {
        this.stops = stops;
    }

    @Override
    public int compareTo(@NonNull Shape shape) {
        return shape.priority - this.priority;
    }
}
