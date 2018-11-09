package jackwtat.simplembta.model;

import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class Stop implements Comparable<Stop>, Serializable {
    private String id;
    private String name = "null";
    private String parentId = "";
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

    public void setDistanceFromOrigin(Location origin) {
        distanceFromOrigin = getLocation().distanceTo(origin);
    }

    public boolean isParentOf(String id) {
        for (String childId : childIds) {
            if (childId.equals(id)) {
                return true;
            }
        }

        // Special ID for Dudley Station
        if ((this.id.equals("64") || this.id.equals("64,64000")) && id.equals("64000")) {
            this.id="64,64000";
            return true;
        }

        return false;
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
