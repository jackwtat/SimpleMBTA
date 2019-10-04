package jackwtat.simplembta.model;

import android.location.Location;

import java.io.Serializable;

public class Vehicle implements Serializable {
    public static final int INCOMING_AT = 0;
    public static final int STOPPED_AT = 1;
    public static final int IN_TRANSIT_TO = 2;

    public enum Status {
        UNKNOWN(""),
        INCOMING("Approaching"),
        STOPPED("Stopped at"),
        IN_TRANSIT("Enroute to");

        private String text;

        Status(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private String id;
    private String label;
    private String route;
    private String destination;
    private String tripName;
    private int direction;
    private double latitude;
    private double longitude;
    private float bearing;
    private int currentStopSequence;
    private Status currentStatus;

    public Vehicle(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getRoute() {
        return route;
    }

    public String getDestination() {
        return destination;
    }

    public String getTripName() {
        return tripName;
    }

    public int getDirection() {
        return direction;
    }

    public Location getLocation() {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setBearing(bearing);
        return location;
    }

    public int getCurrentStopSequence() {
        return currentStopSequence;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        bearing = location.getBearing();
    }

    public void setCurrentStopSequence(int currentStopSequence) {
        this.currentStopSequence = currentStopSequence;
    }

    public void setCurrentStatus(String status) {
        if (status.equalsIgnoreCase("INCOMING_AT")) {
            this.currentStatus = Status.INCOMING;
        } else if (status.equalsIgnoreCase("STOPPED_AT")) {
            this.currentStatus = Status.STOPPED;
        } else if (status.equalsIgnoreCase("IN_TRANSIT_TO")) {
            this.currentStatus = Status.IN_TRANSIT;
        } else {
            this.currentStatus = Status.UNKNOWN;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vehicle) {
            Vehicle otherVehicle = (Vehicle) obj;
            return id.equals(otherVehicle.id);
        } else {
            return false;
        }
    }
}
