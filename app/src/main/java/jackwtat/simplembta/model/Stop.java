package jackwtat.simplembta.model;

import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class Stop implements Comparable<Stop>, Serializable {
    private String id;
    private String name = "null";
    private String[] childIds = new String[0];
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double distanceFromOrigin = 0.0;

    public Stop(String id) {
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

    public double getDistanceFromOrigin() {
        return distanceFromOrigin;
    }

    public String[] getChildIds() {
        return childIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildIds(String[] childIds) {
        this.childIds = childIds;
    }

    public void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public void setDistanceFromOrigin(Location origin) {
        distanceFromOrigin = getLocation().distanceTo(origin);
    }

    public boolean isParentOf(String id) {
        for (String childId : childIds) {
            if (childId.equals(id)) {
                return true;
            }
        }

        if ((this.id.equals("64") || this.id.equals("64,64000")) && id.equals("64000")) {
            this.id="64,64000";
            return true;
        }

        return false;
    }

    public boolean isGreenLineHub() {
        return id.equals("place-lech") ||
                id.equals("place-spmnl") ||
                id.equals("place-north") ||
                id.equals("place-haecl") ||
                id.equals("place-gover") ||
                id.equals("place-pktrm") ||
                id.equals("place-boyls") ||
                id.equals("place-armnl") ||
                id.equals("place-coecl") ||
                id.equals("place-hymnl") ||
                id.equals("place-kencl");
    }

    public boolean isCommuterRailHub(boolean includeTerminals) {
        return (includeTerminals && (id.equals("place-north") || id.equals("place-sstat"))) ||
                id.equals("place-bbsta") ||
                id.equals("place-rugg") ||
                id.equals("place-jfk") ||
                id.equals("place-qnctr");
    }

    @Override
    public int compareTo(@NonNull Stop otherStop) {
        if (distanceFromOrigin == otherStop.getDistanceFromOrigin()) {
            return id.compareTo(otherStop.getId());
        } else {
            return Double.compare(distanceFromOrigin, otherStop.getDistanceFromOrigin());
        }
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
