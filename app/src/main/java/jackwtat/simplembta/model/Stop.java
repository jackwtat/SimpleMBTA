package jackwtat.simplembta.model;

import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Stop implements Comparable<Stop>, Serializable {
    private String id;
    private String name = "null";
    private String parentId = "";
    private String[] childIds = new String[0];
    private double latitude = 0.0;
    private double longitude = 0.0;
    private ArrayList<Route> routes = new ArrayList<>();
    private ArrayList<ServiceAlert> serviceAlerts = new ArrayList<>();
    private boolean wheelchairAccessible = false;

    public Stop(String id) {
        if (id.equals("3") || id.equals("61"))
            this.id = "3,61";
        else if (id.equals("4") || id.equals("60"))
            this.id = "4,60";
        else if (id.equals("5") || id.equals("55"))
            this.id = "5,55";
        else if (id.equals("1787") || id.equals("15176"))
            this.id = "1787,15176";
        else if (id.equals("1788") || id.equals("19402"))
            this.id = "1788,19402";
        else if (id.equals("5093") || id.equals("5100"))
            this.id = "5093,5100";
        else if (id.equals("5095") || id.equals("5098"))
            this.id = "5095,5098";
        else if (id.equals("15095") || id.equals("49003"))
            this.id = "15095,49003";
        else if (id.equals("247") || id.equals("31256"))
            this.id = "247,31256";
        else if (id.equals("30249") || id.equals("31257"))
            this.id = "30249,31257";
        else
            this.id = id;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public ArrayList<ServiceAlert> getServiceAlerts() {
        return serviceAlerts;
    }

    public String getParentId() {
        return parentId;
    }

    public String[] getChildIds() {
        return childIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setChildIds(String[] childIds) {
        this.childIds = childIds;
    }

    public void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void addServiceAlert(ServiceAlert serviceAlert) {
        serviceAlerts.add(serviceAlert);
    }

    public boolean isParentOf(String id) {
        for (String childId : childIds) {
            if (childId.equals(id)) {
                return true;
            }
        }

        return false;
    }

    public boolean isTransferStop() {
        return id.equals("place-north") || // North Station
                id.equals("place-sstat") || // South Station
                id.equals("place-bbsta") || // Back Bay
                id.equals("place-bbsta-worcester") || // Back Bay - Worcester platform
                id.equals("place-pktrm") || // Park Street
                id.equals("place-dwnxg") || // Downtown Crossing
                id.equals("place-gover") || // Government Center
                id.equals("place-state") || // State
                id.equals("place-haecl") || // Haymarket
                id.equals("place-tumnl") || // Tufts Medical Center
                id.equals("place-mlmnl") || // Malden Center
                id.equals("place-rugg") || // Ruggles
                id.equals("place-forhl") || // Forest Hills
                id.equals("place-portr") || // Porter
                id.equals("place-jfk") || // JFK/UMass
                id.equals("place-qnctr") || // Quincy Center
                id.equals("place-brntn") || // Braintree
                id.equals("place-asmnl") || // Ashmont
                id.equals("place-aport"); // Airport
    }

    public boolean isWheelchairAccessible() {
        return wheelchairAccessible;
    }

    @Override
    public int compareTo(@NonNull Stop otherStop) {
        return id.compareTo(otherStop.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stop) {
            Stop otherStop = (Stop) obj;
            return id.equals(otherStop.getId());
        } else {
            return false;
        }
    }
}
