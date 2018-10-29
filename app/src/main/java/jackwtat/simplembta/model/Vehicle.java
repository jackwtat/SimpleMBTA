package jackwtat.simplembta.model;

import android.location.Location;

import java.io.Serializable;

public class Vehicle implements Serializable {
    private String id;
    private String label;
    private String route;
    private int direction;
    private double latitude;
    private double longitude;
    private float bearing;

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


    public void setLabel(String label) {
        this.label = label;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        bearing = location.getBearing();
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
